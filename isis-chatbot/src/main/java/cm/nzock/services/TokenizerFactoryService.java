package cm.nzock.services;

import org.deeplearning4j.text.tokenization.tokenizerfactory.TokenizerFactory;

public interface TokenizerFactoryService {

    TokenizerFactory ignoreWordTokenizerFactory();
    TokenizerFactory keyWordTokenizerFactory();
}
