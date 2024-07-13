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
import com.fasterxml.jackson.core.JsonProcessingException;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.deeplearning4j.models.paragraphvectors.ParagraphVectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpMethod;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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
        context.put(DATA, getModelService().findAndConvertToJson(evaluation));
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
