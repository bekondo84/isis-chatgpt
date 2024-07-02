package cm.nzock.services;

import cm.nzock.beans.ChatLabelData;
import cm.platform.basecommerce.core.chat.ChatSessionModel;
import cm.platform.basecommerce.services.exceptions.ModelServiceException;

public interface ChatService {

    /**
     *
     * @param text
     * @return
     */
    ChatLabelData converse(final ChatSessionModel session, final String uuid, final String text) throws Exception;
}
