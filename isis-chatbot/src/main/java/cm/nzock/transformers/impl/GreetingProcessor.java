package cm.nzock.transformers.impl;

import cm.nzock.transformers.Processor;
import cm.platform.basecommerce.core.security.UserModel;
import cm.platform.basecommerce.core.settings.SettingModel;
import cm.platform.basecommerce.services.SettingService;
import cm.platform.basecommerce.services.UserService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Component(value = "greetingProcessor")
public class GreetingProcessor implements Processor {

    private static final Logger LOG = LoggerFactory.getLogger(GreetingProcessor.class);

    @Autowired
    private SettingService settingService;
    @Autowired
    private UserService userService;

    @Override
    public Map proceed(String text) throws Exception {

        final Map ctx = new HashMap<>();
        final SettingModel setting = settingService.getSettings();
        final UserModel user = userService.getCurrentUser();

        String userName = null;
        String chatname = null;

        if (Objects.nonNull(setting)) {
            chatname = setting.getChatname();
        }
        if (Objects.nonNull(user)) {
            userName = user.getName();
        }
        String value = String.format(text, userName, chatname);
        ctx.put(TEXT_ENTRY, value.replace("null", StringUtils.EMPTY));

        return ctx;
    }
}
