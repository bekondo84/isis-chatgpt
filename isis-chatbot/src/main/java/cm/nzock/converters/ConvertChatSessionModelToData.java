package cm.nzock.converters;

import cm.nzock.beans.ChatSessionData;
import cm.platform.basecommerce.core.chat.ChatSessionModel;
import cm.platform.basecommerce.exceptions.ConvertionException;
import cm.platform.basecommerce.populator.Converter;
import org.springframework.stereotype.Component;

@Component
public class ConvertChatSessionModelToData implements Converter <ChatSessionModel, ChatSessionData>{

    @Override
    public ChatSessionData convert(ChatSessionModel source) throws ConvertionException {
        return new ChatSessionData(source.getPK(), source.getLabel());
    }
}
