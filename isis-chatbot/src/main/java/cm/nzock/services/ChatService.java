package cm.nzock.services;

import cm.nzock.beans.ChatData;
import cm.platform.basecommerce.core.chat.ChatSessionModel;
import cm.platform.basecommerce.core.knowledge.KnowledgeModuleModel;
import org.deeplearning4j.models.paragraphvectors.ParagraphVectors;

public interface ChatService {

    /**
     *
     * @param text
     * @return
     */
    ChatData converse(final ParagraphVectors paragraphVectors, final KnowledgeModuleModel domain, final ChatSessionModel session, final String uuid, final String text) throws Exception;

    ChatData converse(final ParagraphVectors paragraphVectors, final KnowledgeModuleModel domain, final String text) throws Exception;

}
