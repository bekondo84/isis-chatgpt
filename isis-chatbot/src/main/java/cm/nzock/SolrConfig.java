package cm.nzock;

import cm.platform.basecommerce.core.settings.SettingModel;
import cm.platform.basecommerce.services.SettingService;
import org.apache.commons.lang3.StringUtils;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.repository.config.EnableSolrRepositories;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.util.Objects;

@Configuration
@EnableSolrRepositories(
        basePackages = "com.nzock.solr"
        //namedQueriesLocation =
)
@ComponentScan(basePackages = "com.nzock.solr")
public class SolrConfig {

    private static final Logger LOG = LoggerFactory.getLogger(SolrConfig.class);

    @Autowired
    private SettingService settingService;

    @Bean
    public SolrClient solrClient() {
        final SettingModel setting = settingService.getSettings();
        String solrUrl = "http://localhost:8983/solr";
        if (Objects.nonNull(setting) && StringUtils.isNoneBlank(setting.getSolrhost())) {
            solrUrl = setting.getSolrhost();
        }
        return new HttpSolrClient.Builder(solrUrl).build();
    }

    /**
    @Bean
   public EmbeddedSolrServer solrServer() throws ParserConfigurationException, IOException, SAXException {
        final String solrHome = getClass().getClassLoader().getResource("solr-home").getPath();
        EmbeddedSolrServerFactory solrServerFactory = new EmbeddedSolrServerFactory(solrHome);
        return solrServerFactory.getSolrClient();
   }
     */

    @Bean
    public SolrTemplate solrTemplate(SolrClient solrClient) throws ParserConfigurationException, IOException, SAXException {
        return new SolrTemplate(solrClient);

    }
}
