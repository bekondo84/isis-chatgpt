package cm.nzock.actions;

import cm.platform.basecommerce.core.actions.AbstractAction;
import cm.platform.basecommerce.core.actions.annotations.ActionService;
import cm.platform.basecommerce.services.FlexibleSearchService;
import cm.platform.basecommerce.services.MetaTypeService;
import cm.platform.basecommerce.services.ModelService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

@ActionService("userGroupAction")
public class UserGroupAction extends AbstractAction {
    private static Logger LOG = LoggerFactory.getLogger(UserGroupAction.class);
    @Autowired
    private ModelService modelService;
    @Autowired
    private FlexibleSearchService flexibleSearchService;
    @Autowired
    private MetaTypeService metaTypeService;


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
