package cm.nzock.actions;

import cm.nzock.beans.ChatData;
import cm.nzock.services.ChatService;
import cm.nzock.services.Doc2VecService;
import cm.platform.basecommerce.core.actions.AbstractAction;
import cm.platform.basecommerce.core.actions.annotations.Action;
import cm.platform.basecommerce.core.actions.annotations.ActionService;
import cm.platform.basecommerce.core.knowledge.EvaluationModel;
import cm.platform.basecommerce.core.knowledge.EvalutionEntryModel;
import cm.platform.basecommerce.core.knowledge.KnowledgeModel;
import cm.platform.basecommerce.services.*;
import cm.platform.basecommerce.services.exceptions.ModelServiceException;
import cm.platform.basecommerce.tools.persistence.RestrictionsContainer;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.deeplearning4j.models.paragraphvectors.ParagraphVectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpMethod;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.charset.Charset;
import java.util.*;
import java.util.stream.Collectors;

import static cm.platform.basecommerce.core.utils.IsisConstants.ActionsKeys.DATA;

@ActionService("evaluationAction")
public class EvaluationAction extends AbstractAction {
    private static final Logger LOG = LoggerFactory.getLogger(EvaluationAction.class);

    @Autowired
    private ModelService modelService;
    @Autowired
    private FlexibleSearchService flexibleSearchService;
    @Autowired
    private MetaTypeService metaTypeService;
    @Autowired
    private ResourceService resourceService;
    @Autowired
    private ChatService chatService;
    @Autowired
    private Doc2VecService doc2VecService;
    @Autowired
    private ExcelService excelService;

    @Override
    public Map<String, String> createOrUpdate(Map<String, String> context) throws ModelServiceException, ClassNotFoundException, JsonProcessingException {
        EvaluationModel item = mapper.readValue(context.get(DATA) , EvaluationModel.class);
        getModelService().save(item);
        RestrictionsContainer container = RestrictionsContainer.newInstance();
        container.addEq("filename", item.getFilename());
        Optional optional = flexibleSearchService.doSearch(EvaluationModel.class, container, new HashMap<>(), new HashSet<>(), 0, -1)
                .stream()
                .max(new Comparator() {
                    @Override
                    public int compare(Object o1, Object o2) {
                        return ((EvaluationModel)o1).getPK().compareTo(((EvaluationModel)o2).getPK());
                    }
                });
        if (optional.isPresent()) {
            context.put(DATA, mapper.writeValueAsString(optional.get()));
        }
        return context;
    }

    @Action(value = "evaluate", scope = HttpMethod.POST)
    public Map evaluate(Map<String, String> context) throws IOException, ModelServiceException {
         LOG.info("Vous êtes a l'interieur de evaluationAction");
        EvaluationModel evaluation = mapper.readValue(context.get(DATA), EvaluationModel.class);
        evaluation = (EvaluationModel) flexibleSearchService.find(evaluation.getPK(), EvaluationModel._TYPECODE).get();
        final Resource resource = resourceService.load(evaluation.getPK(), evaluation.getFilename());
        final ParagraphVectors paragraphVectors = doc2VecService.buildParagraphVectors(evaluation);
        //Process forst sheet contai
        List<List<String>> values = excelService.excelToJava(resource.getInputStream(),0);
        final List<EvalutionEntryModel> evaluations = new ArrayList<>();

        for (List<String> line: extractOnlyData(values)) {

            if (StringUtils.isBlank(line.get(0))) {
                continue;
            }

            final EvalutionEntryModel entry = new EvalutionEntryModel();
            entry.setText(line.get(0));
            try {
                final ChatData chat = chatService.converse(paragraphVectors, line.get(0));
                entry.setValue(chat.getValue());
                entry.setCosim(chat.getCosim());
            } catch (Exception e) {
                entry.setValue(e.getMessage());
                entry.setCosim(0.0d);
            }
            //Check the knowledge
            flexibleSearchService.find(line.get(1), "code", KnowledgeModel._TYPECODE).ifPresent(obj -> {
                        final KnowledgeModel knowledge = (KnowledgeModel) obj;
                        entry.setLabel(knowledge.getLabel().getCode());
                        entry.setKnowledge(knowledge.getCode());
                        entry.setStatus(entry.getValue().equals(entry.getLabel()));
                    });
            evaluations.add(entry);
        }
        evaluation.setResults(evaluations);

        modelService.save(evaluation);

        evaluation = (EvaluationModel) flexibleSearchService.find(evaluation.getPK(), EvaluationModel._TYPECODE).get();
        context.put(DATA, mapper.writeValueAsString(evaluation));
        return  context;
    }

    private static List<List<String>> extractOnlyData(List<List<String>> values) {
        return CollectionUtils.emptyIfNull(values).stream().skip(1).collect(Collectors.toList());
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
