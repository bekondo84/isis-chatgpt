package cm.nzock.facades.impl;

import cm.nzock.beans.ChatSessionData;
import cm.nzock.beans.ChatData;
import cm.nzock.controllers.ChatSessionController;
import cm.nzock.converters.ConvertChatSessionDataToModel;
import cm.nzock.converters.ConvertChatSessionModelToData;
import cm.nzock.facades.ChatSessionFacade;
import cm.platform.basecommerce.core.chat.ChatLogModel;
import cm.platform.basecommerce.core.chat.ChatSessionModel;
import cm.platform.basecommerce.core.security.EmployeeModel;
import cm.platform.basecommerce.core.security.UserModel;
import cm.platform.basecommerce.services.FlexibleSearchService;
import cm.platform.basecommerce.services.I18NService;
import cm.platform.basecommerce.services.ModelService;
import cm.platform.basecommerce.services.UserService;
import cm.platform.basecommerce.services.exceptions.ModelServiceException;
import cm.platform.basecommerce.tools.persistence.RestrictionsContainer;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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
    @Autowired
    private I18NService i18NService;


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
    public List<ChatData> initChatSession(Long session, String uuid) throws ModelServiceException {
        RestrictionsContainer container = RestrictionsContainer.newInstance();

        if (Objects.nonNull(session)) {
            container.addEq("session.pk", session);
        }
        container.addEq("uuid", uuid);
        int count = (int) flexibleSearchService.count(ChatLogModel.class, container);
        final List<ChatLogModel> chats = flexibleSearchService.doSearch(ChatLogModel.class, container, new HashMap<>(), new HashSet<>(), count-50, 50);

        ChatLogModel chatLog;
        if (CollectionUtils.isEmpty(chats)) {
            chatLog = new ChatLogModel();
            chatLog.setInitial(Boolean.TRUE);
            chatLog.setUuid(uuid);
            chatLog.setDate(new Date());
            chatLog.setInput(null);
            chatLog.setOutput(i18NService.getLabel("chatbot.welcome.message","chatbot.welcome.message"));
            if (Objects.nonNull(session)) {
                chatLog.setSession((ChatSessionModel) flexibleSearchService.find(session, ChatSessionModel._TYPECODE).orElse(null));
            }
            chatLog.setDateCreation(new Date());
            chatLog.setCode(UUID.nameUUIDFromBytes(StringUtils.join(uuid, new Date().toString(), (new Random().nextInt(10000))).getBytes()).toString());
            modelService.save(chatLog);
           flexibleSearchService.find(chatLog.getCode(), "code", ChatLogModel._TYPECODE)
                   .ifPresent(obj -> chats.add((ChatLogModel) obj));
        }
        return chats.stream()
                .map(cht -> new ChatData(cht.getPK(), cht.getInput(), cht.getOutput(), cht.getInitial()))
                .sorted(new Comparator<ChatData>() {
                    @Override
                    public int compare(ChatData o1, ChatData o2) {
                        return o1.getPk().compareTo(o2.getPk());
                    }
                }).collect(Collectors.toList());
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
