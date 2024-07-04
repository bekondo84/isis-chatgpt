package cm.nzock;

import cm.nzock.services.TokenizerFactoryService;
import cm.platform.BaseCommerceServiceConfig;
import cm.platform.basecommerce.BaseCommerceFacadesConfig;
import cm.platform.basecommerce.core.security.SecurityConfig;
import cm.platform.basecommerce.core.settings.SettingModel;
import cm.platform.basecommerce.core.utils.FilesHelper;
import cm.platform.basecommerce.services.SettingService;
import org.apache.commons.lang3.StringUtils;
import org.deeplearning4j.models.embeddings.loader.WordVectorSerializer;
import org.deeplearning4j.models.paragraphvectors.ParagraphVectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.File;
import java.io.IOException;
import java.util.Objects;

@Configuration
@ComponentScan(basePackages = {"cm.nzock"})
@Import({BaseCommerceServiceConfig.class, SecurityConfig.class, BaseCommerceFacadesConfig.class})
public class ApplicationConfig implements WebMvcConfigurer
{
    private static final Logger LOG = LoggerFactory.getLogger(ApplicationConfig.class);

    @Autowired
    private SettingService settingService;
    @Autowired
    private TokenizerFactoryService tokenizerFactoryService;

    @Bean
    public ParagraphVectors paragraphVectors() throws IOException {
        LOG.info("********** Start initializing ParagraphVectors **********************************");
        ParagraphVectors paragraphVectors = null;
        final SettingModel setting = settingService.getSettings();
        if (Objects.nonNull(setting) && StringUtils.isNoneBlank(setting.getActivemodel())) {
            final File activeModelFile = new File(StringUtils.joinWith(File.separator, FilesHelper.getDataDir().getPath(), "model", setting.getActivemodel()));

            paragraphVectors = activeModelFile.exists() ? WordVectorSerializer.readParagraphVectors(activeModelFile) : null;

            if (Objects.nonNull(paragraphVectors)) {
                paragraphVectors.setTokenizerFactory(tokenizerFactoryService.tokenizerFactory());
            }
        }
        LOG.info("********** End initializing ParagraphVectors ********************************** : " + paragraphVectors);
        return paragraphVectors;
    }
}