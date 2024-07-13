package cm.nzock.services.impl;

import cm.nzock.services.TokenizerFactoryService;
import cm.nzock.transformers.ChatBotPreprocessor;
import cm.platform.basecommerce.core.knowledge.KnowledgeModel;
import cm.platform.basecommerce.core.settings.SettingModel;
import cm.platform.basecommerce.services.FlexibleSearchService;
import cm.platform.basecommerce.services.SettingService;
import cm.platform.basecommerce.tools.persistence.RestrictionsContainer;
import org.deeplearning4j.text.tokenization.tokenizer.TokenPreProcess;
import org.deeplearning4j.text.tokenization.tokenizer.preprocessor.CommonPreprocessor;
import org.deeplearning4j.text.tokenization.tokenizer.preprocessor.CompositePreProcessor;
import org.deeplearning4j.text.tokenization.tokenizerfactory.DefaultTokenizerFactory;
import org.deeplearning4j.text.tokenization.tokenizerfactory.TokenizerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class DefaultTokenizerFactoryService implements TokenizerFactoryService {

    private static final Logger LOG = LoggerFactory.getLogger(DefaultTokenizerFactoryService.class);

    @Autowired
    private FlexibleSearchService flexibleSearchService;
    @Autowired
    private SettingService settingService;
    @Autowired
    private TokenPreProcess preprocessor;


    @Override
    public TokenizerFactory tokenizerFactory() {
        final TokenizerFactory tokenizerFactory = new DefaultTokenizerFactory();
        tokenizerFactory.setTokenPreProcessor(new CompositePreProcessor(new CommonPreprocessor(), preprocessor));

        return tokenizerFactory;
    }

}
