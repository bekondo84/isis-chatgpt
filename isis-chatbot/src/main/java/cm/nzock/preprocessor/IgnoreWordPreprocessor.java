package cm.nzock.preprocessor;

import cm.nzock.solr.Dictionnary;
import cm.nzock.solr.IgnoreWordRepository;
import org.apache.commons.lang3.StringUtils;
import org.deeplearning4j.text.tokenization.tokenizer.TokenPreProcess;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.repository.support.SolrRepositoryFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component(value = "ignoreWordPreprocessor")
public class IgnoreWordPreprocessor implements TokenPreProcess {

    private static final Logger LOG = LoggerFactory.getLogger(IgnoreWordPreprocessor.class);

    @Autowired
    private SolrTemplate solrTemplate;
    private IgnoreWordRepository repository;

    @Override
    public String preProcess(String token) {
        final List<String> words = Arrays.asList(token.toLowerCase().split(StringUtils.SPACE))
                .stream()
                .filter(StringUtils::isNotBlank)
                .collect(Collectors.toList());
        String value = token.toLowerCase();
        final StringBuffer textWithoutNoises = new StringBuffer();
        for (String word : words) {
            final Optional<Dictionnary> ignoreWord = repository.findByIdAndType(word, IgnoreWordRepository.IGNORE_STR);
           // LOG.info(String.format("----------- Dictionnary ----------- %s", repository.findByIdAndType(word, "ignore").orElse(null)));
            /**
            ignoreWord.filter(wd -> !wd.getType().equalsIgnoreCase(SolrIgnoreWordsAction.KEYWORD))
                    .ifPresent(keywd -> textWithoutNoises.append(keywd.getValue()));
             */
            if (ignoreWord.isPresent()) {
                //LOG.info(String.format("-------------------Cleaning word  %s on text : %s", ignoreWord.get().getValue(), value));
                value = StringUtils.replace(value, ignoreWord.get().getValue(), StringUtils.EMPTY);
            }

        }
        LOG.info(String.format("Receive text : %s-------------------Clean text : %s", token, value.toString()));
        return value;//textWithoutNoises.toString();
    }


    @PostConstruct
    public void init() {
        repository = new SolrRepositoryFactory(solrTemplate).getRepository(IgnoreWordRepository.class);
    }
}
