package cm.nzock.services.impl;

import cm.nzock.iterators.SetenceIterator;
import cm.nzock.services.Doc2VecService;
import cm.platform.basecommerce.core.settings.SettingModel;
import cm.platform.basecommerce.core.utils.FilesHelper;
import cm.platform.basecommerce.services.ModelService;
import cm.platform.basecommerce.services.SettingService;
import cm.platform.basecommerce.services.exceptions.ModelServiceException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.deeplearning4j.models.embeddings.loader.WordVectorSerializer;
import org.deeplearning4j.models.paragraphvectors.ParagraphVectors;
import org.deeplearning4j.text.tokenization.tokenizerfactory.TokenizerFactory;
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

    private SimpleDateFormat SDF = new SimpleDateFormat("yyyyMMdd-hhmmss");

    @Autowired
    private SettingService settingService ;
    @Autowired
    private SetenceIterator iterator;
    @Autowired
    private TokenizerFactory tokenizerFactory;
    @Autowired
    private ModelService modelService;

    @Override
    public String buildAndSaveModel() throws ModelServiceException, IOException {
        final SettingModel setting = settingService.getSettings();
        final ParagraphVectors paragraphVectors =new ParagraphVectors.Builder()
                .learningRate(Objects.nonNull(setting) ? setting.getLearningrate():0.2d)
                .minLearningRate(Objects.nonNull(setting) ? setting.getMinlearningrate() : 0.08)
                .batchSize(Objects.nonNull(setting) ? setting.getBatchsize(): 50)
                .epochs(Objects.nonNull(setting) ? setting.getEpochs() : 10)
                .iterate(iterator)
                .trainWordVectors(true)
                .tokenizerFactory(tokenizerFactory)
                .build();

        paragraphVectors.fit();

        final File modelSaveDirectory = new File(StringUtils.joinWith(File.separator, FilesHelper.getDataDir().getPath(), "model", "isis-".concat(SDF.format(new Date())).concat(".zip")));

        if (Objects.nonNull(setting) && StringUtils.isNoneBlank(setting.getActivemodel())) {
            final File currentFile = new File(setting.getActivemodel());
            final File archiveFile = new File(StringUtils.joinWith(File.separator, FilesHelper.getDataDir().getPath(), "model", "archives", currentFile.getName()));
            FileUtils.moveFile(currentFile, archiveFile);
        }

        WordVectorSerializer.writeParagraphVectors(paragraphVectors, modelSaveDirectory);

        //Update Setting
        setting.setActivemodel(modelSaveDirectory.getName());
        modelService.save(setting);

        return modelSaveDirectory.getName();
    }

    @Override
    public ParagraphVectors buiildParagraphVectorsFromModel()  {

        ParagraphVectors paragraphVectors = null;

        try {
            final SettingModel setting = settingService.getSettings();
            if (Objects.nonNull(setting) && StringUtils.isNoneBlank(setting.getActivemodel())) {
                final File activeModelFile = new File(StringUtils.joinWith(File.separator, FilesHelper.getDataDir().getPath(), "model", setting.getActivemodel()));

                paragraphVectors = activeModelFile.exists() ? WordVectorSerializer.readParagraphVectors(activeModelFile) : null;

                if (Objects.nonNull(paragraphVectors)) {
                    paragraphVectors.setTokenizerFactory(tokenizerFactory);
                }
            }
        } catch (IOException ex) {
            LOG.error("", ex);
        }
        return paragraphVectors;
    }
}
