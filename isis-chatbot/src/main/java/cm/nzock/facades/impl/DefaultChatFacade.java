package cm.nzock.facades.impl;

import cm.nzock.beans.ChatData;
import cm.nzock.facades.ChatFacade;
import cm.nzock.services.ChatService;
import cm.platform.basecommerce.core.chat.ChatSessionModel;
import cm.platform.basecommerce.core.exception.NzockException;
import cm.platform.basecommerce.core.security.UserModel;
import cm.platform.basecommerce.core.settings.SettingModel;
import cm.platform.basecommerce.services.*;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class DefaultChatFacade implements ChatFacade {

    private static final Logger LOG = LoggerFactory.getLogger(DefaultChatFacade.class);

    @Autowired
    private ChatService chatService;
    @Autowired
    private ModelService modelService;
    @Autowired
    private FlexibleSearchService flexibleSearchService;
    @Autowired
    private UserService userService;
    @Autowired
    private I18NService i18NService;
    @Autowired
    private SettingService settingService;
   // private static final String DEFAULT_SESSION= "";


    @Override
    public ChatData converse(Long session, String uuid, String text) {
       // assert Objects.nonNull(session): "Session PK is null";
        try {
            ChatSessionModel chatSession = null;

            if (Objects.nonNull(session)) {
                Optional sessionExist = flexibleSearchService.find(session, ChatSessionModel._TYPECODE);

                chatSession = sessionExist.isPresent() ? chatSession = (ChatSessionModel) sessionExist.get() : null;
            }
            return chatService.converse(chatSession, uuid, text);
        } catch (Exception e) {
            LOG.error("There is error during processing", e);
            throw new  NzockException(e);
        }
    }

    @Override
    public Map<String, String> getGeneralSettings() {
        final UserModel user = userService.getCurrentUser();
        final SettingModel setting = settingService.getSettings();
        final Map<String, String> settings = new HashMap<>();

        settings.put("username", user.getName());
        settings.put("chatname", setting.getChatname());

        return settings;
    }

    @Override
    public String generateUuid() {
        final UserModel user = userService.getCurrentUser();
        String uuid = null ;
        if (Objects.isNull(user)) {
            uuid = UUID.randomUUID().toString();
        } else {
            uuid = UUID.nameUUIDFromBytes(user.getCode().getBytes()).toString();
        }
        uuid = UUID.nameUUIDFromBytes(StringUtils.join(uuid, new Date().toString(), new Random().nextInt(50000)).getBytes()).toString();

        return uuid;
    }
}
