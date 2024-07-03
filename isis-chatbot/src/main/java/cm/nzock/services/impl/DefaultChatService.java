package cm.nzock.services.impl;

import cm.nzock.beans.ChatLabelData;
import cm.nzock.services.CellMemoryService;
import cm.nzock.services.ChatService;
import cm.nzock.services.Doc2VecService;
import cm.nzock.transformers.Processor;
import cm.platform.basecommerce.core.chat.CellMemoryModel;
import cm.platform.basecommerce.core.chat.ChatLogModel;
import cm.platform.basecommerce.core.chat.ChatSessionModel;
import cm.platform.basecommerce.core.enums.ChatLogState;
import cm.platform.basecommerce.core.knowledge.KnowlegeLabelModel;
import cm.platform.basecommerce.core.settings.SettingModel;
import cm.platform.basecommerce.services.*;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.deeplearning4j.models.embeddings.inmemory.InMemoryLookupTable;
import org.deeplearning4j.models.paragraphvectors.ParagraphVectors;
import org.deeplearning4j.models.word2vec.VocabWord;
import org.deeplearning4j.text.tokenization.tokenizerfactory.TokenizerFactory;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.ops.transforms.Transforms;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class DefaultChatService implements ChatService, InitializingBean {
    private static final Logger LOG = LoggerFactory.getLogger(DefaultChatService.class);

    @Autowired
    private TokenizerFactory tokenizerFactory;
    @Autowired
    private Doc2VecService doc2VecService;
    @Autowired
    private CellMemoryService cellMemoryService;
    @Autowired
    private UserService userService;
    @Autowired
    private SettingService settingService;
    @Autowired
    private EnumerationService enumerationService;
    @Autowired
    private I18NService i18NService;
    @Autowired
    private FlexibleSearchService flexibleSearchService;
    @Autowired
    private ApplicationContext applicationContext;
    @Autowired
    private ModelService modelService;
    private ParagraphVectors paragraphVectors;


    @Override
    public ChatLabelData converse(ChatSessionModel session, String uuid, String text) throws Exception {
        assert text != null : "Chat text can't be null";
        assert Objects.nonNull(paragraphVectors) : "No model found \nPlease generate new model and try again";

        final List<String> inputTokens = new ArrayList<>();

        //Check if it is sequential discution so add the context of the prévious chat
        //The purpose of cellMemory is to store previous conversation
        CellMemoryModel cellMemory = cellMemoryService.read(uuid);

        if (Objects.nonNull(cellMemory)) {
            inputTokens.addAll(tokenizerFactory.create(cellMemory.getValue()).getTokens());
        }
        //Clean the user input to remove the noise
        inputTokens.addAll(tokenizerFactory.create(text).getTokens());
        final String cleanText = StringUtils.joinWith(StringUtils.SPACE, inputTokens.stream().distinct().collect(Collectors.toList()));
        final InMemoryLookupTable<VocabWord> lookupTable = (InMemoryLookupTable<VocabWord>) paragraphVectors.getLookupTable();
        INDArray userInputVector = paragraphVectors.inferVector(cleanText);
        //Find the label which most match the input
        final String label = paragraphVectors.nearestLabels(userInputVector, 1).iterator().next();
        final INDArray labelVec = lookupTable.vector(label);
        //Compute the cosine similaraty
        double cosim = Transforms.cosineSim(userInputVector, labelVec);
         LOG.info(String.format("---cleanText: %s   - cosim :%s", cleanText, cosim));
        //Build the ansew base on the value of the cosim
        final SettingModel setting = settingService.getSettings();
        final double acceptanceRate = (Objects.nonNull(setting) ? setting.getAcceptancerate() : 70d)/100;
        final double uncertaintyrate = (Objects.nonNull(setting) ? setting.getUncertaintyrate() : 30d)/100;
        final ChatLogModel chaLog = new ChatLogModel();

        if (cosim >= acceptanceRate) {
            //Answer the question
            final KnowlegeLabelModel knowlegeLabel = (KnowlegeLabelModel) flexibleSearchService.find(label ,"code", KnowlegeLabelModel._TYPECODE).get();
            chaLog.setState(enumerationService.getEnumerationValue(ChatLogState.KNOWN.getCode(), ChatLogState.class));
             //Process the lknowledgeLabel
            if (StringUtils.isNoneBlank(knowlegeLabel.getAction())) {
                Processor processor = (Processor) applicationContext.getBean(knowlegeLabel.getAction());
                chaLog.setOutput(processor.proceed(label));
            } else if (StringUtils.isNoneBlank(knowlegeLabel.getScript()) && Objects.nonNull(knowlegeLabel.getType())) {
                //Execute the script on the label before return
                chaLog.setOutput(label);
            } else {
                chaLog.setOutput(label);

            }
            chaLog.setOutput(knowlegeLabel.getLabel());

            if (BooleanUtils.isTrue(knowlegeLabel.getEndLabel())) {
                cellMemoryService.forget(uuid);
            } else {
                cellMemoryService.save(uuid, knowlegeLabel.getLabel());
            }

        } else if (cosim >= uncertaintyrate) {
            //Need more information to best anwer the question
            chaLog.setState(enumerationService.getEnumerationValue(ChatLogState.UNCERTAIN.getCode(), ChatLogState.class));
            chaLog.setOutput(i18NService.getLabel("Uncertainly.message", "Sorry i don't have more information to answer this question\nPlease can you give me more details"));
            //Save the current context for next echange
            cellMemoryService.save(uuid, text);
        } else {
            //Unkow question informe the user
            cellMemoryService.forget(uuid);
            chaLog.setState(enumerationService.getEnumerationValue(ChatLogState.UNKNOWN.getCode(), ChatLogState.class));
            chaLog.setOutput(i18NService.getLabel("Unknow.message", "Sorry i don't have more information to answer this question\nI will transfer it to our maintenance team for process"));
        }
        chaLog.setUuid(uuid);
        chaLog.setDate(new Date());
        chaLog.setInput(text);
        chaLog.setSession(session);
        chaLog.setDateCreation(new Date());
        chaLog.setCode(UUID.nameUUIDFromBytes(StringUtils.join(uuid, new Date().toString() ,(new Random().nextInt(10000))).getBytes()).toString());
        modelService.save(chaLog);

        return new ChatLabelData(chaLog.getCode(), chaLog.getOutput());
    }

    @Override
    public void reloadModel() throws Exception {
        afterPropertiesSet();
    }

    @Override
    public void afterPropertiesSet() throws Exception {
          LOG.info("******************** Loading active model *****************");
          paragraphVectors = doc2VecService.buiildParagraphVectorsFromModel();
    }
}
