package cm.nzock.services.impl;

import cm.nzock.beans.ChatData;
import cm.nzock.services.CellMemoryService;
import cm.nzock.services.ChatService;
import cm.nzock.services.Doc2VecService;
import cm.nzock.services.TokenizerFactoryService;
import cm.nzock.transformers.Processor;
import cm.platform.basecommerce.core.chat.CellMemoryModel;
import cm.platform.basecommerce.core.chat.ChatLogModel;
import cm.platform.basecommerce.core.chat.ChatSessionModel;
import cm.platform.basecommerce.core.enums.ChatLogState;
import cm.platform.basecommerce.core.knowledge.KnowlegeLabelModel;
import cm.platform.basecommerce.core.settings.SettingModel;
import cm.platform.basecommerce.services.*;
import cm.platform.basecommerce.tools.persistence.RestrictionsContainer;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.deeplearning4j.models.embeddings.inmemory.InMemoryLookupTable;
import org.deeplearning4j.models.paragraphvectors.ParagraphVectors;
import org.deeplearning4j.models.word2vec.VocabWord;
import org.deeplearning4j.text.tokenization.tokenizer.preprocessor.StringCleaning;
import org.deeplearning4j.text.tokenization.tokenizerfactory.TokenizerFactory;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.exception.ND4JIllegalStateException;
import org.nd4j.linalg.ops.transforms.Transforms;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class DefaultChatService implements ChatService {
    private static final Logger LOG = LoggerFactory.getLogger(DefaultChatService.class);

    @Autowired
    private TokenizerFactoryService tokenizerFactoryService;
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
    @Autowired(required = false)
    private ParagraphVectors paragraphVectors;


    @Override
    public ChatData converse(ChatSessionModel session, String uuid, String text) throws Exception {
        assert text != null : "Chat text can't be null";
        assert Objects.nonNull(paragraphVectors): "No model has detected\n Please set the model and restart the service";
        final ChatLogModel chaLog = new ChatLogModel();

        try {
            //Check if it is sequential discution so add the context of the prévious chat
            //The purpose of cellMemory is to store previous conversation
            CellMemoryModel cellMemory = cellMemoryService.read(uuid);

            LabelData result = computeLabelFromUserinput(text, paragraphVectors, cellMemory);
            chaLog.setCosim(result.cosim);

            if (result.cosim >= result.acceptanceRate) {
                //Answer the question
                final KnowlegeLabelModel knowlegeLabel = (KnowlegeLabelModel) flexibleSearchService.find(result.label,"code", KnowlegeLabelModel._TYPECODE).get();
                chaLog.setState(enumerationService.getEnumerationValue(ChatLogState.KNOWN.getCode(), ChatLogState.class));
                //Process the lknowledgeLabel
                LOG.info(String.format("inside --------converse------------------------ %s -------------StringUtils.isNotBlank(knowlegeLabel.getAction()) : %s", knowlegeLabel.getAction(), StringUtils.isNotBlank(knowlegeLabel.getAction())));
                if (StringUtils.isNotBlank(knowlegeLabel.getAction())) {
                    Processor processor = (Processor) applicationContext.getBean(knowlegeLabel.getAction());
                    chaLog.setOutput(processor.proceed(knowlegeLabel.getLabel()));
                } else if (StringUtils.isNotBlank(knowlegeLabel.getScript()) && Objects.nonNull(knowlegeLabel.getType())) {
                    //Execute the script on the label before return
                    chaLog.setOutput(knowlegeLabel.getLabel());
                } else {
                    chaLog.setOutput(knowlegeLabel.getLabel());
                }

                if (BooleanUtils.isTrue(knowlegeLabel.getEndLabel())) {
                    cellMemoryService.forget(uuid);
                } else {
                    cellMemoryService.save(uuid, chaLog.getOutput());
                }

            } else if (result.cosim >= result.uncertaintyrate) {
                //Need more information to best anwer the question
                chaLog.setState(enumerationService.getEnumerationValue(ChatLogState.UNCERTAIN.getCode(), ChatLogState.class));
                chaLog.setOutput(i18NService.getLabel("Uncertainly.message", "Sorry i don't have more information to answer this question\nPlease can you give me more details"));
                //Save the current context for next echange
                cellMemoryService.forget(uuid);
            } else {
                //Unkow question informe the user
                cellMemoryService.forget(uuid);
                chaLog.setState(enumerationService.getEnumerationValue(ChatLogState.UNKNOWN.getCode(), ChatLogState.class));
                chaLog.setOutput(i18NService.getLabel("Unknow.message", "Sorry i don't have more information to answer this question\nI will transfer it to our maintenance team for process"));
            }
        } catch (ND4JIllegalStateException ex) {
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
        //Reload the chatLog to fetch the PK
        RestrictionsContainer container = RestrictionsContainer.newInstance();
        container.addEq("code", chaLog.getCode());
        if (Objects.nonNull(session)) {
            container.addEq("session.pk", session.getPK());
        }
        final ChatLogModel dbChatLog = (ChatLogModel) flexibleSearchService.doSearch(ChatLogModel.class, container, new HashMap<>(), new HashSet<>(), 0, -1).stream()
                .findAny().get();
        return new ChatData(dbChatLog.getPK(), chaLog.getInput(), chaLog.getOutput(), false);
    }

       @Override
    public ChatData converse(ParagraphVectors paragraphVectors, String text) throws Exception {
        LabelData label = computeLabelFromUserinput(text, paragraphVectors, null);
        return new ChatData(null, text, label.label, label.cosim, false);
    }

    private LabelData computeLabelFromUserinput(String text, final ParagraphVectors paragraphVectors, CellMemoryModel cellMemory) {
        //final List<String> inputTokens = new ArrayList<>();
        final StringBuffer inputBuffer = new StringBuffer();

        if (Objects.nonNull(cellMemory)) {
            inputBuffer.append(cellMemory.getValue());
            inputBuffer.append(StringUtils.SPACE);
        }
        //Clean the user input to remove the noise
        //inputTokens.addAll(tokenizerFactory.create(text).getTokens());
        inputBuffer.append(text.toLowerCase());
        // String cleanText = inputBuffer.toString();
        final InMemoryLookupTable<VocabWord> lookupTable = (InMemoryLookupTable<VocabWord>) paragraphVectors.getLookupTable();
        INDArray userInputVector = paragraphVectors.inferVector(inputBuffer.toString().toLowerCase());
        //Find the label which most match the input
        final String label = paragraphVectors.nearestLabels(userInputVector, 1).iterator().next();
        final INDArray labelVec = lookupTable.vector(label);
        //Compute the cosine similaraty
        double cosim = Transforms.cosineSim(userInputVector, labelVec);
        LOG.info(String.format("---cleanText: %s   - cosim :%s", inputBuffer.toString(), cosim));
        //Build the ansew base on the value of the cosim
        final SettingModel setting = settingService.getSettings();
        final double acceptanceRate = (Objects.nonNull(setting) ? setting.getAcceptancerate() : 70d)/100;
        final double uncertaintyrate = (Objects.nonNull(setting) ? setting.getUncertaintyrate() : 30d)/100;
        LabelData result = new LabelData(label, cosim, acceptanceRate, uncertaintyrate);
        return result;
    }

    private static class LabelData {
        public final String label;
        public final double cosim;
        public final double acceptanceRate;
        public final double uncertaintyrate;

        public LabelData(String label, double cosim, double acceptanceRate, double uncertaintyrate) {
            this.label = label;
            this.cosim = cosim;
            this.acceptanceRate = acceptanceRate;
            this.uncertaintyrate = uncertaintyrate;
        }
    }
}
