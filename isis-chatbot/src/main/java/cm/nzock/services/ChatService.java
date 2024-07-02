package cm.nzock.services;

import cm.platform.basecommerce.core.chat.ChatSessionModel;
import cm.platform.basecommerce.services.exceptions.ModelServiceException;

public interface ChatService {

    /**
     *
     * @param text
     * @return
     */
    String converse(final ChatSessionModel session, final String text) throws Exception;
}
