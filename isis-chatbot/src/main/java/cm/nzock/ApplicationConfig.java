package cm.nzock;

import cm.platform.BaseCommerceServiceConfig;
import cm.platform.basecommerce.BaseCommerceFacadesConfig;
import cm.platform.basecommerce.core.security.SecurityConfig;
import org.deeplearning4j.text.tokenization.tokenizer.preprocessor.CommonPreprocessor;
import org.deeplearning4j.text.tokenization.tokenizerfactory.DefaultTokenizerFactory;
import org.deeplearning4j.text.tokenization.tokenizerfactory.TokenizerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@ComponentScan(basePackages = {"cm.nzock"})
@Import({BaseCommerceServiceConfig.class, SecurityConfig.class, BaseCommerceFacadesConfig.class})
public class ApplicationConfig implements WebMvcConfigurer
{

    @Bean
    public TokenizerFactory tokenizerFactory() {
           final TokenizerFactory tokenizerFactory = new DefaultTokenizerFactory();
           tokenizerFactory.setTokenPreProcessor(new CommonPreprocessor());

           return tokenizerFactory;
    }
}