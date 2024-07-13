package cm.nzock.services;

import cm.platform.basecommerce.core.knowledge.EvaluationModel;
import cm.platform.basecommerce.services.exceptions.ModelServiceException;
import org.deeplearning4j.models.paragraphvectors.ParagraphVectors;

import java.io.IOException;

public interface Doc2VecService {

     String buildAndSaveModel() throws ModelServiceException, IOException;

     ParagraphVectors buildParagraphVectorsFromModel() throws IOException;

     ParagraphVectors buildParagraphVectors(final EvaluationModel evaluation) throws IOException;
}
