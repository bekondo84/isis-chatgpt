package cm.nzock.facades.impl;

import cm.nzock.facades.ChatFacade;
import cm.nzock.services.ChatService;
import cm.platform.basecommerce.core.chat.ChatSessionModel;
import cm.platform.basecommerce.core.exception.NzockException;
import cm.platform.basecommerce.core.security.UserModel;
import cm.platform.basecommerce.core.settings.SettingModel;
import cm.platform.basecommerce.services.*;
import cm.platform.basecommerce.tools.persistence.RestrictionsContainer;
import org.apache.commons.collections4.CollectionUtils;
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
    public String converse(Long session, String text) {
        assert Objects.nonNull(session): "Session PK is null";

        try {
            Optional sessionExist = flexibleSearchService.find(session, ChatSessionModel._TYPECODE);

            if (!sessionExist.isPresent()) {
                LOG.error("No chatSesion found PK : %s", session);
            }
            return chatService.converse((ChatSessionModel) sessionExist.get(), text);
        } catch (Exception e) {
            LOG.error("There is error during processing", e);
            return i18NService.getLabel("error.message", "error.message");
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
}
