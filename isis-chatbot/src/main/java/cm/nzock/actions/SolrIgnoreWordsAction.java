package cm.nzock.actions;

import cm.nzock.preprocessor.StringPrePreprocessor;
import cm.nzock.services.TokenizerFactoryService;
import cm.nzock.solr.Dictionnary;
import cm.nzock.solr.IgnoreWordRepository;
import cm.platform.basecommerce.core.actions.AbstractAction;
import cm.platform.basecommerce.core.actions.annotations.Action;
import cm.platform.basecommerce.core.actions.annotations.ActionService;
import cm.platform.basecommerce.core.knowledge.KnowledgeModel;
import cm.platform.basecommerce.core.settings.SettingModel;
import cm.platform.basecommerce.core.solr.SolrIgnoreWordsModel;
import cm.platform.basecommerce.services.FlexibleSearchService;
import cm.platform.basecommerce.services.MetaTypeService;
import cm.platform.basecommerce.services.ModelService;
import cm.platform.basecommerce.services.SettingService;
import cm.platform.basecommerce.services.exceptions.ModelServiceException;
import cm.platform.basecommerce.tools.persistence.RestrictionsContainer;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.deeplearning4j.text.tokenization.tokenizer.preprocessor.StringCleaning;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.repository.support.SolrRepositoryFactory;
import org.springframework.http.HttpMethod;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.stream.Collectors;

import static cm.platform.basecommerce.core.utils.IsisConstants.ActionsKeys.DATA;

@ActionService("solrIgnoreWordsAction")
public class SolrIgnoreWordsAction extends AbstractAction {

    private static final Logger LOG = LoggerFactory.getLogger(SolrIgnoreWordsAction.class);
    public static final String KEYWORD = "keyword";

    @Autowired
    private ModelService modelService;
    @Autowired
    private FlexibleSearchService flexibleSearchService;
    @Autowired
    private MetaTypeService metaTypeService;
    @Autowired
    private SettingService settingService;
    @Autowired
    private SolrTemplate solrTemplate;

    private IgnoreWordRepository repository;


    @Action(value = "synchronize", scope = HttpMethod.POST)
    public Map synchronize(Map<String, String> context) throws JsonProcessingException, ModelServiceException {
        final SolrIgnoreWordsModel solrIgnoreWords = mapper.readValue(context.get(DATA), SolrIgnoreWordsModel.class);
        final SettingModel setting = settingService.getSettings();
        final int batchSize = 50 ;
        RestrictionsContainer container = RestrictionsContainer.newInstance();
        final long totalItems = flexibleSearchService.count(KnowledgeModel.class, container);
        final long epochs = (totalItems / batchSize) +1 ;

        //Clean the repository
        repository.findAll().forEach(word -> repository.deleteById(word.getId()));
        //repository.deleteAll();
        for (int i=0; i < epochs; i++) {
            List<KnowledgeModel> items = flexibleSearchService.doSearch(KnowledgeModel.class, container, new HashMap<>(), new HashSet<>(), i, i+batchSize);
            CollectionUtils.emptyIfNull(items)
                    .stream()
                    .filter(Objects::nonNull)
                    .forEach(item -> {
                        List<String> templateWords = new ArrayList<>();
                        List<String> keywordsWords = new ArrayList<>();
                        LOG.info(String.format("--Template: --- %s -------- Keywords: %s", item.getTemplate(), item.getKeywords()));
                        if (StringUtils.isNoneBlank(item.getTemplate())) {
                            templateWords.addAll(Arrays.asList(StringCleaning.stripPunct(new StringPrePreprocessor().preProcess(item.getTemplate())).toLowerCase()
                                            .split(StringUtils.SPACE))
                                            .stream()
                                            .map(String::trim)
                                            .filter(StringUtils::isNotBlank)
                                            .collect(Collectors.toList()));
                        }
                        if (StringUtils.isNoneBlank(item.getKeywords())) {
                            keywordsWords.addAll(Arrays.asList(item.getKeywords().split(";"))
                                            .stream()
                                            .map(String::trim)
                                            .filter(StringUtils::isNotBlank)
                                            .map(word ->StringCleaning.stripPunct(word).toLowerCase())
                                            .collect(Collectors.toList()));
                        }
                        //Update the keyword
                        pullKeywords(keywordsWords);
                        List<String>  wordsToIgnore = templateWords.stream().filter(value -> !keywordsWords.contains(value)).collect(Collectors.toList());
                        LOG.info(String.format("template : %s ----------- keywords : %s ------------ ignore words: %s",templateWords, keywordsWords, wordsToIgnore));
                        pullOnusualWords(wordsToIgnore);
                    });
        }
        //Remove keywords previously mark as ignore
        cleanAllKeywords();
        solrIgnoreWords.setDate(new Date());
        modelService.save(solrIgnoreWords);
        context.put(DATA, modelService.findAndConvertToJson(solrIgnoreWords));
        return context;
    }

    private void pullOnusualWords(List<String> wordsToIgnore) {
        for (String val : CollectionUtils.emptyIfNull(wordsToIgnore)) {
            if (StringUtils.isNoneBlank(val)) {
                final Optional<Dictionnary> entry = repository.findById(val);

                if (!entry.isPresent()) {
                    repository.save(new Dictionnary(val, val, "ignore")) ;
                }
            }
        }
    }

    private void pullKeywords(List<String> keywordsWords) {
        for (String word: CollectionUtils.emptyIfNull(keywordsWords)) {
            final Optional<Dictionnary> entry = repository.findById(word);
            Dictionnary dictionnary = new Dictionnary(word, word, KEYWORD);
            if (entry.isPresent()) {
                repository.deleteById(entry.get().getId());
            }
            repository.save(dictionnary);
        }
    }

    private void cleanAllKeywords() {
        Iterator<Dictionnary> iterator = repository.findAll().iterator();
        while (iterator.hasNext()) {
            Dictionnary item = iterator.next();
            if (item.getType().equalsIgnoreCase(KEYWORD)) {
                repository.deleteById(item.getId());
            }
        }
    }


    @Override
    public Map<String, String> createOrUpdate(Map<String, String> context) throws ModelServiceException, ClassNotFoundException, JsonProcessingException {
        computeAndIndexIgnoreWordsInSolr(context);
        return super.createOrUpdate(context);
    }

    /**
     *
     * @param context
     * @throws JsonProcessingException
     */
    private void computeAndIndexIgnoreWordsInSolr(Map<String, String> context) throws JsonProcessingException {

    }


    @PostConstruct
    public void init() {
         repository = new SolrRepositoryFactory(solrTemplate).getRepository(IgnoreWordRepository.class);
    }

    @Override
    protected ModelService getModelService() {
        return modelService;
    }

    @Override
    protected FlexibleSearchService getFlexibleSearchService() {
        return flexibleSearchService;
    }

    @Override
    protected MetaTypeService getMetaTypeService() {
        return metaTypeService;
    }
}
