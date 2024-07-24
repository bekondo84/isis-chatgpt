package cm.nzock.iterators;

import cm.nzock.services.TokenizerFactoryService;
import cm.platform.basecommerce.core.knowledge.KnowledgeModuleModel;
import cm.platform.basecommerce.services.FlexibleSearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicInteger;

@Component
public class SentenceIteratorBuilder {

    @Autowired
    private FlexibleSearchService flexibleSearchService;
    @Autowired
    private TokenizerFactoryService tokenizerFactoryService;

    /**
     * Build Iterator for given domin
     * @param domain
     * @return
     */
    public SetenceIterator build(final KnowledgeModuleModel domain) {
          return SetenceIterator.instance(domain, flexibleSearchService, tokenizerFactoryService);
    }
}
