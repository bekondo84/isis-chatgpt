package cm.nzock.facades;

import cm.nzock.beans.ChatSessionData;
import cm.platform.basecommerce.core.chat.ChatLogModel;
import cm.platform.basecommerce.services.exceptions.ModelServiceException;

import java.util.List;

public interface ChatSessionFacade {

    /**
     * Create and update if exists chat session
     * @param chatSession
     * @return
     */
    ChatSessionData createOrUpdate(final ChatSessionData chatSession) throws ModelServiceException;

    void delete(final Long pk) throws ModelServiceException;

    List<ChatLogModel> initChatSession(final Long session) ;

    List<ChatSessionData> getUserSessions() ;
}
