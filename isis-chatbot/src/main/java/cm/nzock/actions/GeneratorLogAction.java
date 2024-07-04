package cm.nzock.actions;

import cm.nzock.services.ChatService;
import cm.nzock.services.Doc2VecService;
import cm.platform.basecommerce.core.actions.AbstractAction;
import cm.platform.basecommerce.core.actions.annotations.ActionService;
import cm.platform.basecommerce.core.knowledge.GeneratorLogModel;
import cm.platform.basecommerce.services.FlexibleSearchService;
import cm.platform.basecommerce.services.MetaTypeService;
import cm.platform.basecommerce.services.ModelService;
import cm.platform.basecommerce.services.exceptions.ModelServiceException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.util.Date;
import java.util.Map;

import static cm.platform.basecommerce.core.utils.IsisConstants.ActionsKeys.DATA;

@ActionService("generatorLogAction")
public class GeneratorLogAction extends AbstractAction {
    private static final Logger LOG = LoggerFactory.getLogger(GeneratorLogModel.class);

    @Autowired
    private ModelService modelService;
    @Autowired
    private FlexibleSearchService flexibleSearchService;
    @Autowired
    private MetaTypeService metaTypeService;
    @Autowired
    protected ObjectMapper mapper;
    @Autowired
    private Doc2VecService doc2VecService;
    @Autowired
    private ChatService chatService;


    @Override
    public Map<String, String> createOrUpdate(Map<String, String> context) throws ModelServiceException, ClassNotFoundException, JsonProcessingException {
        GeneratorLogModel item = mapper.readValue(context.get(DATA) , GeneratorLogModel.class);
        //Object item = gson.fromJson(context.get("data"), type);
        //Build the paragraph doc2vec model
        final String modelfilename;
        try {
            modelfilename = doc2VecService.buildAndSaveModel();
            //Update and save
            item.setModelname(modelfilename);
            item.setDate(new Date());
            item.setDateCreation(new Date());
            getModelService().save(item);
            item = (GeneratorLogModel) getModelService().find(item);
            context.put(DATA, getModelService().findAndConvertToJson(item));
            //Reload model
            //chatService.reloadModel();
        } catch (Exception e) {
            LOG.error("There is error during model generation", e);
        }

        return context;
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
