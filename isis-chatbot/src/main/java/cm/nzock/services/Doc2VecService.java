package cm.nzock.services;

import cm.platform.basecommerce.services.exceptions.ModelServiceException;
import org.deeplearning4j.models.paragraphvectors.ParagraphVectors;
import org.deeplearning4j.text.tokenization.tokenizerfactory.TokenizerFactory;

import java.io.IOException;

public interface Doc2VecService {

     String buildAndSaveModel() throws ModelServiceException, IOException;

     ParagraphVectors buiildParagraphVectorsFromModel() throws IOException;

}
