package cm.nzock.services;

import cm.platform.basecommerce.core.knowledge.EvaluationModel;
import cm.platform.basecommerce.core.knowledge.KnowledgeModuleModel;
import cm.platform.basecommerce.services.exceptions.ModelServiceException;
import org.deeplearning4j.models.paragraphvectors.ParagraphVectors;

import java.io.IOException;

public interface Doc2VecService {

     String buildAndSaveModel(final KnowledgeModuleModel domain) throws ModelServiceException, IOException;

     ParagraphVectors buildParagraphVectorsFromModel(final KnowledgeModuleModel domain) throws IOException;

     ParagraphVectors buildParagraphVectors(final EvaluationModel evaluation) throws IOException;
}
