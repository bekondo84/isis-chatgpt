package cm.nzock.iterators;

import cm.nzock.services.TokenizerFactoryService;
import cm.platform.basecommerce.core.knowledge.KnowledgeModel;
import cm.platform.basecommerce.core.knowledge.KnowledgeModuleModel;
import cm.platform.basecommerce.core.knowledge.KnowledgeTypeModel;
import cm.platform.basecommerce.core.knowledge.KnowlegeLabelModel;
import cm.platform.basecommerce.services.FlexibleSearchService;
import cm.platform.basecommerce.tools.persistence.RestrictionsContainer;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.deeplearning4j.text.documentiterator.LabelAwareIterator;
import org.deeplearning4j.text.documentiterator.LabelledDocument;
import org.deeplearning4j.text.documentiterator.LabelsSource;
import org.deeplearning4j.text.tokenization.tokenizer.preprocessor.StringCleaning;
import org.deeplearning4j.text.tokenization.tokenizerfactory.TokenizerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;


public class SetenceIterator implements LabelAwareIterator {
    private static final Logger LOG = LoggerFactory.getLogger(SetenceIterator.class);

    //@Autowired
    private FlexibleSearchService flexibleSearchService;
   // @Autowired
    private TokenizerFactoryService tokenizerFactoryService;
    private AtomicInteger  cursor = new AtomicInteger(0);
    private LabelsSource labelsSource ;

    private KnowledgeModuleModel domain ;

    private SetenceIterator() {

    }

    /**
     *
     * @param domain
     * @param flexibleSearchService
     * @param tokenizerFactoryService
     * @return
     */
    public static final SetenceIterator instance(final KnowledgeModuleModel domain,
                                 final FlexibleSearchService flexibleSearchService, final TokenizerFactoryService tokenizerFactoryService) {
       assert Objects.nonNull(domain) : "Domain is not null" ;
       assert Objects.nonNull(flexibleSearchService) : "FlexibleSearchService is not initialize";
       assert Objects.nonNull(tokenizerFactoryService) : "TokenizerFactoryService is not initialize";

       return new SetenceIterator().setCursor(new AtomicInteger(0))
               .setDomain(domain)
               .setTokenizerFactoryService(tokenizerFactoryService)
               .setFlexibleSearchService(flexibleSearchService);
    }
    @Override
    public boolean hasNextDocument() {
        long totalSize = flexibleSearchService.count(KnowledgeModel.class, buildRestrictionsContainer());
        return cursor.get() < totalSize;
    }

    @Override
    public LabelledDocument nextDocument() {
        int index = cursor.getAndIncrement();
        final KnowledgeModel knowledge = (KnowledgeModel) flexibleSearchService.doSearch(KnowledgeModel.class, buildRestrictionsContainer(), new HashMap<>(), new HashSet<>(), index, index).get(0);

       LOG.info(String.format("nextDocument ------ question : %s", knowledge.getCode()));

        LabelledDocument document =new LabelledDocument();
        //Tokenized
        final List<String> words =new ArrayList<>();
        final StringBuffer stringBuffer = new StringBuffer();

        //final TokenizerFactory tokenizerFactory = tokenizerFactoryService.tokenizerFactory();

        if (StringUtils.isNoneBlank(knowledge.getTemplate())) {
            //words.addAll(tokenizerFactory.create(knowledge.getTemplate()).getTokens());
            stringBuffer.append(knowledge.getTemplate().toLowerCase()).append(StringUtils.SPACE);
        }
        if (StringUtils.isNoneBlank(knowledge.getKeywords())) {
            //words.addAll(Arrays.stream(knowledge.getKeywords().split(";")).collect(Collectors.toList()));
           // stringBuffer.append(knowledge.getKeywords().replace(";", StringUtils.SPACE).toLowerCase());
        }
        //StringUtils.joinWith(StringUtils.SPACE, tokenizerFactory.create(stringBuffer.toString()).getTokens())
        document.setContent(StringCleaning.stripPunct(stringBuffer.toString()));
        LOG.info(String.format("Iterator text : %s", document.getContent()));
        document.addLabel(knowledge.getLabel().getCode());
        document.setId(String.valueOf(index));
        return document;
    }

    @Override
    public void reset() {
          cursor.set(0);
    }

    @Override
    public LabelsSource getLabelsSource() {
        if (Objects.isNull(labelsSource)) {
            final List<String> labels = (List<String>) flexibleSearchService.doSearch(KnowledgeModel.class, buildRestrictionsContainer(), new HashMap<>(), new HashSet<>(), 0, -1)
                    .stream()
                    .map(val -> ((KnowledgeModel)val).getLabel().getCode())
                    .distinct()
                    .collect(Collectors.toList());
            labelsSource = new LabelsSource(labels) ;
        }
        return labelsSource;
    }


    private RestrictionsContainer buildRestrictionsContainer() {
        //Get the list of KnowledgeType
        List<String> types = CollectionUtils.emptyIfNull(domain.getClasses())
                .stream().map(KnowledgeTypeModel::getCode)
                .collect(Collectors.toList());
        RestrictionsContainer container = RestrictionsContainer.newInstance();
        container.addIn("category.code", types.toArray(new String[types.size()]));
        return container;
    }


    @Override
    public void shutdown() {

    }

    @Override
    public boolean hasNext() {
        return hasNextDocument();
    }

    @Override
    public LabelledDocument next() {
        return nextDocument();
    }

    public SetenceIterator setFlexibleSearchService(FlexibleSearchService flexibleSearchService) {
        this.flexibleSearchService = flexibleSearchService;
        return this;
    }

    public SetenceIterator setTokenizerFactoryService(TokenizerFactoryService tokenizerFactoryService) {
        this.tokenizerFactoryService = tokenizerFactoryService;
        return this;
    }

    public SetenceIterator setCursor(AtomicInteger cursor) {
        this.cursor = cursor;
        return this;
    }

    public SetenceIterator setDomain(KnowledgeModuleModel domain) {
        this.domain = domain;
        return this;
    }
}
