package cm.nzock;

import cm.platform.basecommerce.core.annotations.RemoteMethod;
import cm.platform.basecommerce.core.annotations.RemoteService;
import cm.platform.basecommerce.core.setup.IsisSetupService;
import cm.platform.basecommerce.services.ImpexImportService;
import cm.platform.basecommerce.services.MetaTypeCvsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;

@RemoteService("setupService")
public class SetupService implements IsisSetupService {
    private static final Logger LOG = LoggerFactory.getLogger(SetupService.class);

    @Autowired
    private ImpexImportService impexImportService;

    @Autowired
    private MetaTypeCvsService metaTypeCvsService;

    @Override
    public void onStartUp() throws Exception {

        final URL path = getClass().getClassLoader().getResource("import"+ File.separator+"init-data");
        File initFile = new File(path.toURI());
        for(File file : initFile.listFiles()){
            if(file.getName().contains("-metatype")){//It is generate inti file
                metaTypeCvsService.initializedModule(file);
            }
        }
        //Init default Users accounts
        //impexImportService.importImpexFromResources("import/init-data/backoffice-init.csv");
    }

    @RemoteMethod(name = "setup")
    @Override
    public void setup() throws Exception {
        //LOG.info("-------------------------------------Inside Setup Backoffice ----------------");
        impexImportService.importImpexFromResources("import/init-data/isis-chatbot-data.csv");
    }
}