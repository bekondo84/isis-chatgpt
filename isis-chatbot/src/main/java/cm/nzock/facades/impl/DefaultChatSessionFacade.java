package cm.nzock.facades.impl;

import cm.nzock.beans.ChatSessionData;
import cm.nzock.controllers.ChatSessionController;
import cm.nzock.converters.ConvertChatSessionDataToModel;
import cm.nzock.converters.ConvertChatSessionModelToData;
import cm.nzock.facades.ChatSessionFacade;
import cm.platform.basecommerce.core.chat.ChatLogModel;
import cm.platform.basecommerce.core.chat.ChatSessionModel;
import cm.platform.basecommerce.core.security.EmployeeModel;
import cm.platform.basecommerce.core.security.UserModel;
import cm.platform.basecommerce.services.FlexibleSearchService;
import cm.platform.basecommerce.services.ModelService;
import cm.platform.basecommerce.services.UserService;
import cm.platform.basecommerce.services.exceptions.ModelServiceException;
import cm.platform.basecommerce.tools.persistence.RestrictionsContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;


@Component
public class DefaultChatSessionFacade implements ChatSessionFacade {

    private static final Logger LOG = LoggerFactory.getLogger(ChatSessionController.class);

    @Autowired
    private ModelService modelService;
    @Autowired
    private FlexibleSearchService flexibleSearchService;
    @Autowired
    private ConvertChatSessionDataToModel convert ;
    @Autowired
    private ConvertChatSessionModelToData revertConvert;
    @Autowired
    private UserService userService;


    @Override
    public ChatSessionData createOrUpdate(ChatSessionData chatSession) throws ModelServiceException {
        ChatSessionModel session = convert.convert(chatSession);

        String userId = userService.getCurrentUser().getCode() ;
        RestrictionsContainer container = RestrictionsContainer.newInstance();

        if (Objects.isNull(session.getAccount())) {
            container.addEq("code", userId);
            Optional hasUser = flexibleSearchService.doSearch(EmployeeModel.class, container, new HashMap<>(), new HashSet<>(), 0, -1).stream().findAny();

            if (hasUser.isPresent()) {
                session.setAccount((EmployeeModel) hasUser.get());
            }
        }
        //Save the change
        modelService.save(session);

        if (Objects.isNull(session.getPK())) {
            container = RestrictionsContainer.newInstance();
            container.addEq("label", session.getLabel());
            container.addEq("account.code", userId);
            session = (ChatSessionModel) flexibleSearchService.doSearch(ChatSessionModel.class, container, new HashMap<>(), new HashSet<>(), 0, 1)
                    .stream()
                    .findAny().get();
        }
        return new ChatSessionData(session.getPK(), session.getLabel());
    }

    @Override
    public void delete(Long pk) throws ModelServiceException {
        final Optional getChatSessionWithPK = flexibleSearchService.find(pk, ChatSessionModel._TYPECODE);

        if (getChatSessionWithPK.isPresent()) {
            try {
                modelService.delete(getChatSessionWithPK.get());
            } catch(ModelServiceException e) {
                final ChatSessionModel chatSession = (ChatSessionModel) getChatSessionWithPK.get();
                chatSession.setIsDelete(true);
                modelService.save(chatSession);
            }
        } else  {
            LOG.info("No chat session found with PK : %s", pk);
        }
    }

    @Override
    public List<ChatLogModel> initChatSession(Long session) {
        RestrictionsContainer container = RestrictionsContainer.newInstance();
        container.addEq("session.pk", session);
        List<ChatLogModel> chats = flexibleSearchService.doSearch(ChatLogModel.class, container, new HashMap<>(), new HashSet<>(), 0, 50);
        return chats;
    }

    @Override
    public List<ChatSessionData> getUserSessions() {
        final UserModel user = userService.getCurrentUser();
        RestrictionsContainer container = RestrictionsContainer.newInstance();
        container.addEq("account.pk", user.getPK());
        final List chatSessions = flexibleSearchService.doSearch(ChatSessionModel.class, container, new HashMap<>(), new HashSet<>(), 0, -1);
        return (List<ChatSessionData>) chatSessions
                .stream()
                .map(s -> revertConvert.convert((ChatSessionModel) s))
                .collect(Collectors.toList());
    }
}
