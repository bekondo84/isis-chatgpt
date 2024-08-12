package cm.nzock.services.impl;

import cm.nzock.iterators.SentenceIteratorBuilder;
import cm.nzock.iterators.SetenceIterator;
import cm.nzock.services.Doc2VecService;
import cm.nzock.services.TokenizerFactoryService;
import cm.platform.basecommerce.core.exception.NzockException;
import cm.platform.basecommerce.core.knowledge.EvaluationModel;
import cm.platform.basecommerce.core.knowledge.KnowledgeModuleModel;
import cm.platform.basecommerce.core.settings.SettingModel;
import cm.platform.basecommerce.core.utils.FilesHelper;
import cm.platform.basecommerce.services.ModelService;
import cm.platform.basecommerce.services.SettingService;
import cm.platform.basecommerce.services.exceptions.ModelServiceException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.deeplearning4j.models.embeddings.loader.WordVectorSerializer;
import org.deeplearning4j.models.paragraphvectors.ParagraphVectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;


@Service
public class DefaultDoc2VecService implements Doc2VecService {

    private static final Logger LOG = LoggerFactory.getLogger(DefaultDoc2VecService.class);
    public static final String CONFIGURATION_ERROR = "No configuration is detected\nPlease set the configurations parameters and try agian";

    private SimpleDateFormat SDF = new SimpleDateFormat("yyyyMMdd-hhmmss");

    @Autowired
    private SettingService settingService ;
    @Autowired
    private SentenceIteratorBuilder iteratorBuilder;
    @Autowired
    private ModelService modelService;
    @Autowired
    private TokenizerFactoryService tokenizerFactoryService;


    @Override
    public String buildAndSaveModel(final KnowledgeModuleModel domain) throws ModelServiceException, IOException {
        final SettingModel setting = settingService.getSettings();

        if (Objects.isNull(setting)) {
            throw new NzockException(String.format(CONFIGURATION_ERROR));
        }
        double learningRate = domain.getLearningrate();
        double minLearningRate = domain.getMinlearningrate();
        int batchSize = domain.getBatchsize();
        int numEpochs = domain.getEpochs();
        final ParagraphVectors paragraphVectors = getParagraphVectors(domain, learningRate, minLearningRate, batchSize, numEpochs);

        final File modelSaveDirectory = new File(StringUtils.joinWith(File.separator, FilesHelper.getDataDir().getPath(), "model", domain.getCode().toLowerCase().trim().concat(SDF.format(new Date())).concat(".zip")));
        //Model currentFile if exist to archive
        final File currentFile = new File(StringUtils.joinWith(File.separator, FilesHelper.getDataDir().getPath(), "model", domain.getModelfile()));

        if (currentFile.exists() && currentFile.isFile()) {
            final File archiveFile = new File(StringUtils.joinWith(File.separator, FilesHelper.getDataDir().getPath(), "model", "archives", currentFile.getName()));
            FileUtils.moveFile(currentFile, archiveFile);
        }

        WordVectorSerializer.writeParagraphVectors(paragraphVectors, modelSaveDirectory);

        //Update Setting
        domain.setModelfile(modelSaveDirectory.getName());
        modelService.save(domain);

        return modelSaveDirectory.getName();
    }


    @Override
    public ParagraphVectors buildParagraphVectorsFromModel(final KnowledgeModuleModel domain)  {

        ParagraphVectors paragraphVectors = null;

        try {
            //final SettingModel setting = settingService.getSettings();
            if (Objects.nonNull(domain) && StringUtils.isNoneBlank(domain.getModelfile())) {
                final File activeModelFile = new File(StringUtils.joinWith(File.separator, FilesHelper.getDataDir().getPath(), "model", domain.getModelfile()));

                paragraphVectors = activeModelFile.exists() ? WordVectorSerializer.readParagraphVectors(activeModelFile) : null;

                if (Objects.nonNull(paragraphVectors)) {
                    paragraphVectors.setTokenizerFactory(tokenizerFactoryService.keyWordTokenizerFactory());
                    //paragraphVectors.fit();
                }
            } else if (Objects.isNull(domain)) {
                throw new NzockException(String.format(CONFIGURATION_ERROR));
            } else {
                throw new NzockException("No model is configure\n Please generate new model and try again");
            }
        } catch (IOException ex) {
            LOG.error("", ex);
        }
        return paragraphVectors;
    }

    @Override
    public ParagraphVectors buildParagraphVectors(EvaluationModel evaluation) throws IOException {
        assert Objects.nonNull(evaluation):"Evaluation object is null";
        return getParagraphVectors(evaluation.getDomain(), evaluation.getLearningrate(), evaluation.getMinlearningrate(), evaluation.getBatchsize(), evaluation.getEpochs());
    }

    private ParagraphVectors getParagraphVectors(final KnowledgeModuleModel domain,double learningRate, double minLearningRate, int batchSize, int numEpochs) {
        final SetenceIterator iterator = iteratorBuilder.build(domain);

        final ParagraphVectors paragraphVectors =new ParagraphVectors.Builder()
                //.minWordFrequency(1)
                .learningRate(learningRate)
                .minLearningRate(minLearningRate)
                .batchSize(batchSize)
                .epochs(numEpochs)
                .labelsSource(iterator.getLabelsSource())
                .iterate(iterator)
                .trainWordVectors(true)
                .tokenizerFactory(tokenizerFactoryService.ignoreWordTokenizerFactory())
                //.windowSize(5)
                .build();

        paragraphVectors.fit();
        return paragraphVectors;
    }

}
