package cm.nzock.actions;

import cm.platform.basecommerce.core.actions.AbstractAction;
import cm.platform.basecommerce.core.actions.annotations.Action;
import cm.platform.basecommerce.core.actions.annotations.ActionService;
import cm.platform.basecommerce.core.knowledge.KnowlegeLabelModel;
import cm.platform.basecommerce.core.security.UserGroupRigthsModel;
import cm.platform.basecommerce.services.FlexibleSearchService;
import cm.platform.basecommerce.services.MetaTypeService;
import cm.platform.basecommerce.services.ModelService;
import cm.platform.basecommerce.services.exceptions.ModelServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ActionService("knowledgeAction")
public class KnowledgeAction extends AbstractAction {

    private static final Logger LOG = LoggerFactory.getLogger(KnowledgeAction.class);

    @Autowired
    private ModelService modelService;
    @Autowired
    private FlexibleSearchService flexibleSearchService;
    @Autowired
    private MetaTypeService metaTypeService;

    @Action(value = "labelHandler", scope = HttpMethod.GET)
    public Map knowledgeLabelText(Map<String, String> context) throws ModelServiceException {
        LOG.info("------------------------------labelHandler ---------------------");
        final Map result = new HashMap<>(context);
        final long pk = Long.valueOf(String.valueOf(context.get("pk")));
        final KnowlegeLabelModel knowlegeLabel = (KnowlegeLabelModel) flexibleSearchService.find(pk, KnowlegeLabelModel._TYPECODE).get();
        result.put("data", knowlegeLabel.getLabel());
        return result ;
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
