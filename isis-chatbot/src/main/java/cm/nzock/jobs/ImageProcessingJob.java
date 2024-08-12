package cm.nzock.jobs;

import cm.platform.basecommerce.core.enums.CronJobResult;
import cm.platform.basecommerce.core.enums.CronJobStatus;
import cm.platform.basecommerce.core.jobs.AbstractJobPerformable;
import cm.platform.basecommerce.core.jobs.ImageCronJobModel;
import cm.platform.basecommerce.core.jobs.PerformResult;
import cm.platform.basecommerce.core.utils.FilesHelper;
import cm.platform.basecommerce.services.MediaService;
import cm.platform.basecommerce.services.exceptions.ModelServiceException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;


@Component
public class ImageProcessingJob extends AbstractJobPerformable<ImageCronJobModel> {

    private static final Logger LOG = LoggerFactory.getLogger(ImageProcessingJob.class);
    public static final String IMAGES_FOLDER = "images";
    public static final String PUBLIC_FOLDER = "public";
    public static final String PRIVATE_FOLDER = "private";
    public static final String ARCHIVES_FOLDER = "archives";
    public static final String ERRORS_FOLDER = "errors";

    @Autowired
    private MediaService mediaService;


    @Override
    public PerformResult perform(ImageCronJobModel cronJob) {
        LOG.info("Processing -------------------ImageProcessingJob----------");
        final File imageFolder = new File(StringUtils.joinWith(File.separator, FilesHelper.getDataDir().getPath(), IMAGES_FOLDER));
        if (!imageFolder.exists()) {
            imageFolder.mkdir();
        }
        final File publicImageFolder = new File(StringUtils.joinWith(File.separator, imageFolder.getPath(), PUBLIC_FOLDER));

        if (!publicImageFolder.exists()) {
            publicImageFolder.mkdir();
        }

        final File privateImageFolder = new File(StringUtils.joinWith(File.separator, imageFolder.getPath(), PRIVATE_FOLDER));

        if (!privateImageFolder.exists()) {
            privateImageFolder.mkdir();
        }

        final File archiveImageFolder = new File(StringUtils.joinWith(File.separator, imageFolder.getPath(), ARCHIVES_FOLDER));

        if (!archiveImageFolder.exists()) {
            archiveImageFolder.mkdir();
        }

        final File errorImageFolder = new File(StringUtils.joinWith(File.separator, imageFolder.getPath(), ERRORS_FOLDER));

        if (!errorImageFolder.exists()) {
            errorImageFolder.mkdir();
        }
        //Process Public
        processImages(publicImageFolder.listFiles(), archiveImageFolder, errorImageFolder, true);
        processImages(privateImageFolder.listFiles(), archiveImageFolder, errorImageFolder, false);
        return new PerformResult(CronJobResult.SUCCESS, CronJobStatus.FINISHED);
    }


    @Override
    public boolean isAbortable() {
        return super.isAbortable();
    }

    /**
     * Process Images
     * @param files
     * @param archiveImageFolder
     * @param open
     */
    private void processImages(File[] files, File archiveImageFolder, File errorFolder, boolean open) {
         for (File image : files) {
            try {
                //Create the media
                mediaService.createMediaFromFile(image, open);
                //Move file to archives
                FileUtils.moveFile(image, new File(StringUtils.joinWith(File.separator, archiveImageFolder.getPath(), image.getName())));
            } catch (Exception e) {
                LOG.error(String.format("Enable to process Image %s ", image.getName()),e);
                try {
                    FileUtils.moveFile(image, new File(StringUtils.joinWith(File.separator, errorFolder.getPath(), image.getName())));
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            }
        }
    }

}
