package cm.nzock.iterators;

import cm.platform.basecommerce.core.knowledge.KnowledgeModel;
import cm.platform.basecommerce.core.knowledge.KnowlegeLabelModel;
import cm.platform.basecommerce.services.FlexibleSearchService;
import cm.platform.basecommerce.tools.persistence.RestrictionsContainer;
import org.apache.commons.lang3.StringUtils;
import org.deeplearning4j.text.documentiterator.LabelAwareIterator;
import org.deeplearning4j.text.documentiterator.LabelledDocument;
import org.deeplearning4j.text.documentiterator.LabelsSource;
import org.deeplearning4j.text.tokenization.tokenizerfactory.TokenizerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Component
public class SetenceIterator implements LabelAwareIterator {
    private static final Logger LOG = LoggerFactory.getLogger(SetenceIterator.class);

    @Autowired
    private FlexibleSearchService flexibleSearchService;
    @Autowired
    private TokenizerFactory tokenizerFactory;
    private AtomicInteger  cursor = new AtomicInteger(0);
    private LabelsSource labelsSource ;

    @Override
    public boolean hasNextDocument() {
        RestrictionsContainer container = RestrictionsContainer.newInstance();
        long totalSize = flexibleSearchService.count(KnowledgeModel.class, container);
        return cursor.get() < totalSize;
    }

    @Override
    public LabelledDocument nextDocument() {
        RestrictionsContainer container = RestrictionsContainer.newInstance();
        int index = cursor.getAndIncrement();
        final KnowledgeModel knowledge = (KnowledgeModel) flexibleSearchService.doSearch(KnowledgeModel.class, container, new HashMap<>(), new HashSet<>(), index, index).get(0);

       LOG.info(String.format("nextDocument ------ question : %s", knowledge.getCode()));

        LabelledDocument document =new LabelledDocument();
        //Tokenized
        List<String> words =new ArrayList<>();

        if (StringUtils.isNoneBlank(knowledge.getTemplate())) {
            words.addAll(tokenizerFactory.create(knowledge.getTemplate()).getTokens());
        }
        if (StringUtils.isNoneBlank(knowledge.getKeywords())) {
            words.addAll(tokenizerFactory.create(knowledge.getKeywords()).getTokens());
        }
        document.setContent(StringUtils.joinWith(" ", words));
        document.addLabel(knowledge.getLabel().getCode());
        return document;
    }

    @Override
    public void reset() {
          cursor.set(0);
    }

    @Override
    public LabelsSource getLabelsSource() {
        if (Objects.isNull(labelsSource)) {
            RestrictionsContainer container = RestrictionsContainer.newInstance();
            final List<String> labels = (List<String>) flexibleSearchService.doSearch(KnowlegeLabelModel.class, container, new HashMap<>(), new HashSet<>(), 0, -1)
                    .stream()
                    .map(item -> ((KnowlegeLabelModel)item).getCode())
                    .collect(Collectors.toList());
            labelsSource = new LabelsSource(labels) ;
        }
        return labelsSource;
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
}
