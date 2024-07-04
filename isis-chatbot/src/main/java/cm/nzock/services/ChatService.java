package cm.nzock.services;

import cm.nzock.beans.ChatData;
import cm.platform.basecommerce.core.chat.ChatSessionModel;

public interface ChatService {

    /**
     *
     * @param text
     * @return
     */
    ChatData converse(final ChatSessionModel session, final String uuid, final String text) throws Exception;

}
