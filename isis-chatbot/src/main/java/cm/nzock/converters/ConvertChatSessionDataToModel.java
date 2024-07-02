package cm.nzock.converters;

import cm.nzock.beans.ChatSessionData;
import cm.platform.basecommerce.core.chat.ChatSessionModel;
import cm.platform.basecommerce.exceptions.ConvertionException;
import cm.platform.basecommerce.populator.Converter;
import cm.platform.basecommerce.services.FlexibleSearchService;
import cm.platform.basecommerce.services.exceptions.ModelServiceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.Objects;
import java.util.Optional;

@Component
public class ConvertChatSessionDataToModel  implements Converter<ChatSessionData, ChatSessionModel> {

    @Autowired
    private FlexibleSearchService flexibleSearchService;

    @Override
    public ChatSessionModel convert(ChatSessionData source) throws ConvertionException {

        try {
            final Optional findSession = Objects.nonNull(source.getPk()) ? flexibleSearchService.find(source.getPk(), ChatSessionModel._TYPECODE) : Optional.empty();
            final ChatSessionModel chatSession = findSession.isPresent() ? (ChatSessionModel) findSession.get() : new ChatSessionModel();
            chatSession.setLabel(source.getLabel());
            chatSession.setDate(new Date());
            chatSession.setPK(source.getPk());
            return chatSession;
        } catch (ModelServiceException e) {
            throw new ConvertionException(e);
        }

    }
}
