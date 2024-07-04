package cm.nzock.services.impl;

import cm.nzock.services.TokenizerFactoryService;
import org.deeplearning4j.text.tokenization.tokenizer.preprocessor.CommonPreprocessor;
import org.deeplearning4j.text.tokenization.tokenizerfactory.DefaultTokenizerFactory;
import org.deeplearning4j.text.tokenization.tokenizerfactory.TokenizerFactory;
import org.springframework.stereotype.Service;

@Service
public class DefaultTokenizerFactoryService implements TokenizerFactoryService {

    @Override
    public TokenizerFactory tokenizerFactory() {
        final TokenizerFactory tokenizerFactory = new DefaultTokenizerFactory();
        tokenizerFactory.setTokenPreProcessor(new CommonPreprocessor());

        return tokenizerFactory;
    }
}
