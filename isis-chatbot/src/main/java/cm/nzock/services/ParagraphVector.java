package cm.nzock.services;

import cm.platform.basecommerce.core.knowledge.KnowledgeModuleModel;
import org.deeplearning4j.models.paragraphvectors.ParagraphVectors;

import java.io.IOException;

public interface ParagraphVector {

    ParagraphVectors paragraphVectors(final KnowledgeModuleModel domain) throws IOException;

}
