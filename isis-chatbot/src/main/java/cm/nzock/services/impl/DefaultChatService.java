package cm.nzock.services.impl;

import cm.nzock.beans.ChatData;
import cm.nzock.preprocessor.StringPrePreprocessor;
import cm.nzock.services.CellMemoryService;
import cm.nzock.services.ChatService;
import cm.nzock.services.Doc2VecService;
import cm.nzock.services.TokenizerFactoryService;
import cm.nzock.transformers.Processor;
import cm.platform.basecommerce.core.chat.CellMemoryModel;
import cm.platform.basecommerce.core.chat.ChatLogModel;
import cm.platform.basecommerce.core.chat.ChatSessionModel;
import cm.platform.basecommerce.core.enums.ChatLogState;
import cm.platform.basecommerce.core.knowledge.KnowledgeModuleModel;
import cm.platform.basecommerce.core.knowledge.KnowlegeLabelModel;
import cm.platform.basecommerce.core.security.EmployeeModel;
import cm.platform.basecommerce.core.security.UserModel;
import cm.platform.basecommerce.core.settings.SettingModel;
import cm.platform.basecommerce.services.*;
import cm.platform.basecommerce.services.exceptions.ModelServiceException;
import cm.platform.basecommerce.tools.persistence.RestrictionsContainer;
import org.apache.commons.collections4.CollectionUtils;
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
import java.util.stream.Collectors;

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
    //@Autowired(required = false)
   // private ParagraphVectors paragraphVectors;


    @Override
    public ChatData converse(ParagraphVectors paragraphVectors, KnowledgeModuleModel domain, ChatSessionModel session, String uuid, String text) throws Exception {
        assert text != null : "Chat text can't be null";
        assert Objects.nonNull(paragraphVectors): "No model has detected\n Please set the model and restart the service";
        final ChatLogModel chaLog = new ChatLogModel();
        LOG.info(String.format("INSIDE DEFAULT SERVICE ----------------- %s", paragraphVectors));

        try {
            //Check if it is sequential discution so add the context of the prévious chat
            //The purpose of cellMemory is to store previous conversation
            CellMemoryModel cellMemory = cellMemoryService.read(uuid);

            LabelData result = computeLabelFromUserinput(text, paragraphVectors, cellMemory, domain);
            chaLog.setCosim(result.cosim);

            if (result.cosim >= result.acceptanceRate) {
                //Answer the question
                final KnowlegeLabelModel knowlegeLabel = (KnowlegeLabelModel) flexibleSearchService.find(result.label,"code", KnowlegeLabelModel._TYPECODE).get();
                final UserModel user = userService.getCurrentUser();

                if (!checkIfUserCanAccessLabel(user, knowlegeLabel)) {
                    String labelAcls = StringUtils.joinWith(";", CollectionUtils.emptyIfNull(knowlegeLabel.getAcls())
                            .stream().map(acl -> acl.getCode()).collect(Collectors.toList())) ;
                    String answer = String.format(i18NService.getLabel("chatbot.label.unauthorized", "chatbot.label.unauthorized"), labelAcls);
                    chaLog.setOutput(answer);
                } else {
                    chaLog.setState(enumerationService.getEnumerationValue(ChatLogState.KNOWN.getCode(), ChatLogState.class));
                    Boolean notCompletQuestion = false;
                    //Check if the user have the rigth to accès this level of informations
                    //LOG.info(String.format("inside --------converse------------------------ %s -------------StringUtils.isNotBlank(knowlegeLabel.getAction()) : %s", knowlegeLabel.getAction(), StringUtils.isNotBlank(knowlegeLabel.getAction())));
                    if (StringUtils.isNotBlank(knowlegeLabel.getAction())) {
                        Processor processor = (Processor) applicationContext.getBean(knowlegeLabel.getAction());
                        Map ctx = processor.proceed(knowlegeLabel.getLabel());
                        chaLog.setOutput((String) ctx.get(Processor.TEXT_ENTRY));

                        notCompletQuestion = ( ctx.get(Processor.ENDLABEL_ENTRY) instanceof Boolean && BooleanUtils.isTrue((Boolean) ctx.get("ENDLABEL")));

                    } else if (StringUtils.isNotBlank(knowlegeLabel.getScript()) && Objects.nonNull(knowlegeLabel.getType())) {
                        //Execute the script on the label before return
                        chaLog.setOutput(knowlegeLabel.getLabel());
                    } else {
                        chaLog.setOutput(knowlegeLabel.getLabel());
                    }

                    if (BooleanUtils.isTrue(knowlegeLabel.getEndLabel()) && !notCompletQuestion) {
                        cellMemoryService.forget(uuid);
                    } else {
                        cellMemoryService.save(uuid, chaLog.getOutput());
                    }
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
        return new ChatData(dbChatLog.getPK(), chaLog.getInput(), chaLog.getOutput(), false, chaLog.getReview());
    }

    /**
     * Ckeck if the current user has the access level required to access this label
     * @param user
     * @param knowlegeLabel
     * @return
     * @throws ModelServiceException
     */
    private boolean checkIfUserCanAccessLabel(UserModel user, KnowlegeLabelModel knowlegeLabel) throws ModelServiceException {
        List<String> labelAcls = CollectionUtils.emptyIfNull(knowlegeLabel.getAcls())
                .stream().map(acl -> acl.getCode()).collect(Collectors.toList());

        //Label with no acl is open to all
        if (CollectionUtils.isEmpty(labelAcls)) {
            return true;
        }
        //Anonymous user can't access to label with acl set
        if (Objects.isNull(user)) {
            return  false;
        }
        //Load the user account to extract user acl
        final EmployeeModel connectAccount = (EmployeeModel) flexibleSearchService.find(user.getPK(), EmployeeModel._TYPECODE).get();
        List<String> userAcls = CollectionUtils.emptyIfNull(connectAccount.getAccessLevel())
                .stream().map(acl -> acl.getCode()).collect(Collectors.toList());
        //Check if any user acl is in the label acl list
        return CollectionUtils.containsAny(userAcls, labelAcls);
    }

    @Override
    public ChatData converse(ParagraphVectors paragraphVectors, KnowledgeModuleModel domain, String text) throws Exception {
        LabelData label = computeLabelFromUserinput(text, paragraphVectors, null, domain);
        return new ChatData(null, text, label.label, label.cosim, false, true);
    }

    private LabelData computeLabelFromUserinput(String text, final ParagraphVectors paragraphVectors, CellMemoryModel cellMemory, KnowledgeModuleModel domain) {
        //final List<String> inputTokens = new ArrayList<>();
        final StringBuffer inputBuffer = new StringBuffer();
        if (Objects.nonNull(cellMemory)) {
            inputBuffer.append(cellMemory.getValue());
            inputBuffer.append(StringUtils.SPACE);
        }
        //Clean the user input to remove the noise
        //inputTokens.addAll(tokenizerFactory.create(text).getTokens());
        inputBuffer.append(StringCleaning.stripPunct(new StringPrePreprocessor().preProcess(text.toLowerCase())));
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
        final double acceptanceRate = domain.getAcceptancerate()/100;
        final double uncertaintyrate = domain.getUncertaintyrate()/100;
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
