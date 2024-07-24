package cm.nzock.actions;

import cm.platform.basecommerce.core.actions.AbstractAction;
import cm.platform.basecommerce.core.actions.annotations.Action;
import cm.platform.basecommerce.core.actions.annotations.ActionService;
import cm.platform.basecommerce.core.enums.KnowledgeModStatus;
import cm.platform.basecommerce.core.knowledge.KnowledgeModuleModel;
import cm.platform.basecommerce.services.EnumerationService;
import cm.platform.basecommerce.services.FlexibleSearchService;
import cm.platform.basecommerce.services.MetaTypeService;
import cm.platform.basecommerce.services.ModelService;
import cm.platform.basecommerce.services.exceptions.ModelServiceException;
import cm.platform.basecommerce.tools.persistence.RestrictionsContainer;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;

import java.util.*;
import java.util.stream.Collectors;

import static cm.platform.basecommerce.core.utils.IsisConstants.ActionsKeys.DATA;

@ActionService("knowledgeModuleAction")
public class KnowledgeModuleAction extends AbstractAction {
    private static final Logger LOG = LoggerFactory.getLogger(KnowledgeModuleAction.class);

    @Autowired
    private FlexibleSearchService flexibleSearchService;
    @Autowired
    private MetaTypeService metaTypeService;
    @Autowired
    private ModelService modelService;
    @Autowired
    private EnumerationService enumerationService;


    @Action(value = "setAsdefault", scope = HttpMethod.POST)
    public Map setAsDefaultModel(final Map<String, String> context) throws JsonProcessingException, ModelServiceException {
        KnowledgeModuleModel module = mapper.readValue(context.get(DATA), KnowledgeModuleModel.class);
        //Desable previous default if exist
        List modules = flexibleSearchService.doSearch(KnowledgeModuleModel.class, RestrictionsContainer.newInstance(), new HashMap<>(), new HashSet<>(), 0, -1);

        for (Object mod : CollectionUtils.emptyIfNull(modules)) {
            KnowledgeModuleModel elt = (KnowledgeModuleModel) mod;
            if (BooleanUtils.isTrue(elt.getDefaultmodel())) {
                elt.setDefaultmodel(Boolean.FALSE);
                getModelService().save(elt);
            }
        }
        module = (KnowledgeModuleModel) flexibleSearchService.find(module.getPK(), KnowledgeModuleModel._TYPECODE).get();
        module.setDefaultmodel(Boolean.TRUE);
        getModelService().save(module);
        return context;
    }

    @Action(value = "openclose", scope = HttpMethod.POST)
    public Map openCloseModelToPublic(final Map<String, String> context) throws JsonProcessingException, ModelServiceException {
        KnowledgeModuleModel module = mapper.readValue(context.get(DATA), KnowledgeModuleModel.class);
        module = (KnowledgeModuleModel) flexibleSearchService.find(module.getPK(), KnowledgeModuleModel._TYPECODE).get();

        final KnowledgeModStatus openState = enumerationService.getEnumerationValue(KnowledgeModStatus.OPEN.getCode(), KnowledgeModStatus.class);
        final KnowledgeModStatus closeState = enumerationService.getEnumerationValue(KnowledgeModStatus.CLOSE.getCode(), KnowledgeModStatus.class);

        if (Objects.isNull(module.getStatus())) {
            module.setStatus(openState);
        } else if (module.getStatus().getCode().equalsIgnoreCase(openState.getCode())) {
            module.setStatus(closeState);
        } else {
            module.setStatus(openState);
        }
        getModelService().save(module);

        module = (KnowledgeModuleModel) flexibleSearchService.find(module.getPK(), KnowledgeModuleModel._TYPECODE).get();

        context.put(DATA, mapper.writeValueAsString(module)) ;

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
