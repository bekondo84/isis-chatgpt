package cm.nzock.actions;

import cm.nzock.converters.ListToKnowledgeConverter;
import cm.nzock.converters.ListToKnowledgeLabelConverter;
import cm.platform.basecommerce.core.actions.AbstractAction;
import cm.platform.basecommerce.core.actions.annotations.Action;
import cm.platform.basecommerce.core.actions.annotations.ActionService;
import cm.platform.basecommerce.core.enums.ImportLogState;
import cm.platform.basecommerce.core.knowledge.ImportLogModel;
import cm.platform.basecommerce.core.knowledge.KnowledgeModel;
import cm.platform.basecommerce.core.knowledge.KnowlegeLabelModel;
import cm.platform.basecommerce.services.*;
import cm.platform.basecommerce.services.exceptions.ModelServiceException;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpMethod;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.*;
import java.util.stream.Collectors;

import static cm.platform.basecommerce.core.utils.IsisConstants.ActionsKeys.DATA;

@ActionService("importLogAction")
public class ImportLogAction extends AbstractAction {

    private static final Logger LOG = LoggerFactory.getLogger(ImportLogAction.class);

    @Autowired
    private ModelService modelService;
    @Autowired
    private FlexibleSearchService flexibleSearchService;
    @Autowired
    private MetaTypeService metaTypeService ;
    @Autowired
    private EnumerationService enumerationService;
    @Autowired
    private ExcelService excelService;
    @Autowired
    private ResourceService resourceService;
    @Autowired
    private I18NService i18NService;
    @Autowired
    private ListToKnowledgeLabelConverter knowledgeLabelConverter;
    @Autowired
    private ListToKnowledgeConverter knowledgeConverter;


    @Action(value = "importation", scope = HttpMethod.POST)
    public Map imporation(Map<String, String> context) throws ModelServiceException, IOException {
        ImportLogModel importLog = mapper.readValue(context.get(DATA) , ImportLogModel.class);

        if (importLog.getState().equals(ImportLogState.IMPORT)) {
            return context;
        }

        final Optional importLogOp = flexibleSearchService.find(importLog.getPK(), ImportLogModel._TYPECODE);

        if (!importLogOp.isPresent()) {
            LOG.error("No entity fond for %s with PK %s", "ImportLog", importLog.getPK());
        }

        importLog = (ImportLogModel) importLogOp.get();
        final Resource resource = resourceService.load(importLog.getPK(), importLog.getFilename());
        //Process forst sheet contai
        List<List<String>> knowledgeSheet = excelService.excelToJava(resource.getInputStream(),0)
                .stream().skip(1)
                .filter(line -> line.stream().allMatch(StringUtils::isNotBlank))
                .collect(Collectors.toList());
        List<List<String>> labelSheet = excelService.excelToJava(resource.getInputStream(),1)
                .stream().skip(1)
                .filter(line -> line.stream().allMatch(StringUtils::isNotBlank))
                .collect(Collectors.toList());
        //Container of errors
        final StringBuffer errors = new StringBuffer();

        final Map<Integer, Boolean> knowledgeCol = getKnowledgeCol();
        final Map<Integer, Boolean> labelCol = getLabelCol();

        if (!validated(errors,knowledgeSheet, labelSheet, knowledgeCol, labelCol)) {
            importLog.setOutput(errors.toString());
        }
        //This session process to transformation
        transformAndSaveLabelsData(labelSheet);
        knowledgeSheet.forEach(line -> {
            final KnowledgeModel knowledge = knowledgeConverter.convert(line);
            try {
                final Optional optional = flexibleSearchService.find(knowledge.getCode(), "code", KnowledgeModel._TYPECODE);

                optional.ifPresent(know -> {
                    knowledge.setTemplate(((KnowledgeModel)know).getTemplate());
                    knowledge.setLabel(((KnowledgeModel)know).getLabel());
                    knowledge.setCategory(((KnowledgeModel)know).getCategory());
                    knowledge.setKeywords(((KnowledgeModel)know).getKeywords());
                    knowledge.setPK(((KnowledgeModel)know).getPK());
                    knowledge.setModified(new Date());
                });
                getModelService().save(knowledge);
            } catch (ModelServiceException e) {
                LOG.error(String.format("Enable to save knowledge line %s", knowledgeSheet.indexOf(line)), e);
            }
        });
        //Update ImportLogState
        importLog.setState(enumerationService.getEnumerationValue(ImportLogState.IMPORT.getCode(), ImportLogState.class));
        importLog.setDateModification(new Date());
        modelService.save(importLog);
        context.put(DATA, getModelService().findAndConvertToJson(importLog));
        return context;
    }


    @Override
    public Map<String, String> createOrUpdate(Map<String, String> context) throws ModelServiceException, ClassNotFoundException, JsonProcessingException {
        ImportLogModel item = mapper.readValue(context.get(DATA) , ImportLogModel.class);
        item.setDate(new Date());
        item.setDateCreation(new Date());
        item.setState(enumerationService.getEnumerationValue(ImportLogState.LOAD.getCode(), ImportLogState.class));
        getModelService().save(item);
        item = (ImportLogModel) getModelService().find(item);
        context.put(DATA, getModelService().findAndConvertToJson(item));
        return context;
    }

    @Override
    public Map delete(Map context) throws ModelServiceException, ClassNotFoundException {
        final List<Long> pks = (List<Long>) context.get("items");
        List items = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(pks)) {
            items.addAll(dataService.fetchItems(pks, ImportLogModel.class.getName()));
        } else {
            items.addAll(dataService.fetchItems(context));
        }
        getModelService().delete(items.toArray());
        //Delete link resources
        for (Object item : CollectionUtils.emptyIfNull(items)) {
            try {
                final Resource resource = resourceService.load(((ImportLogModel)item).getPK(), null);

                if (Objects.isNull(resource)) {
                    FileUtils.cleanDirectory(resource.getFile());
                }
            } catch (IOException e) {
                LOG.error(e.getMessage());
            }
        }
        return context;
    }


    @Action(value = "validation", scope = HttpMethod.POST)
    public Map validation(Map<String, String> context) throws IOException, ModelServiceException {
        //Get de importlog
        ImportLogModel importLog = mapper.readValue(context.get(DATA) , ImportLogModel.class);
        final Optional importLogOp = flexibleSearchService.find(importLog.getPK(), ImportLogModel._TYPECODE);

        if (!importLogOp.isPresent()) {
            LOG.error("No entity fond for %s with PK %s", "ImportLog", importLog.getPK());
        }

        importLog = (ImportLogModel) importLogOp.get();
        final Resource resource = resourceService.load(importLog.getPK(), importLog.getFilename());
        //Process forst sheet contai
        List<List<String>> knowledgeSheet = excelService.excelToJava(resource.getInputStream(),0)
                .stream().skip(1)
                .filter(line ->line.stream().allMatch(StringUtils::isNotBlank))
                .collect(Collectors.toList());
        List<List<String>> labelSheet = excelService.excelToJava(resource.getInputStream(),1)
                .stream().skip(1)
                .filter(line ->line.stream().allMatch(StringUtils::isNotBlank))
                .collect(Collectors.toList());
        //Container of errors
        final StringBuffer errors = new StringBuffer();

        final Map<Integer, Boolean> knowledgeCol = getKnowledgeCol();
        final Map<Integer, Boolean> labelCol = getLabelCol();

        importLog.setOutput(i18NService.getLabel("importLog.validation.okLabel", "Everything is OK"));
        if (!validated(errors,knowledgeSheet, labelSheet, knowledgeCol, labelCol)) {
            importLog.setOutput(errors.toString());
        }

        modelService.save(importLog);
        context.put(DATA, getModelService().findAndConvertToJson(importLog));
        return context;
    }

    /**
     *
     * @param errors
     * @param knowledge
     * @param labels
     * @param knowledgeCol
     * @param labelCol
     * @return
     */
    private boolean validated(StringBuffer errors,List<List<String>> knowledge,List<List<String>> labels, Map<Integer, Boolean> knowledgeCol, Map<Integer, Boolean> labelCol) {

        //Validation of label sheet sheet number 2
        validatedLabelSheet(errors, labels, labelCol);

        validatedKnowledgeSheet(errors, knowledge, labels, labelCol);

        return StringUtils.isBlank(errors.toString());
    }

    private void validatedKnowledgeSheet(StringBuffer errors, List<List<String>> knowledge, List<List<String>> labels, Map<Integer, Boolean> labelCol) {
        List<String> labelCodes = labels.stream().map(l ->l.get(0)).distinct().collect(Collectors.toList());

        knowledge.forEach(line -> {
            int lineIndex = knowledge.indexOf(line);
            if (CollectionUtils.isEmpty(line)) {
                errors.append(String.format(i18NService.getLabel("importLog.validation.empty.line", "Line number %s is empty"), lineIndex));
            }
            final StringBuffer lineBuffer = new StringBuffer();
            line.forEach(entry -> {
                int index = line.indexOf(entry) ;
                if (!labelCol.get(index) && StringUtils.isBlank(entry)) {//Check if column is no optional
                    lineBuffer.append(String.format(i18NService.getLabel("importLog.validation.empty.line", "Row number %s of line number %s is empty"), lineIndex, index));
                }
            });
            //Check if labelcode row 4 value exist in labelcodes
            if (!labelCodes.contains(line.get(3))) {
                lineBuffer.append(String.format(i18NService.getLabel("importLog.validation.unkownlabel", " the row 4 (label code) value don't existe in the list of possible value : line : %s "), lineIndex));
            }
        });
    }

    private void validatedLabelSheet(StringBuffer errors, List<List<String>> labels, Map<Integer, Boolean> labelCol) {
        labels.forEach(line -> {
            int lineIndex = labels.indexOf(line) ;
            if (CollectionUtils.isEmpty(line)) {
                errors.append(String.format(i18NService.getLabel("importLog.validation.empty.line", "Line number %s is empty"), lineIndex));
            }
            final StringBuffer lineBuffer = new StringBuffer();
            line.forEach(entry -> {
                  int index = line.indexOf(entry) ;
                  if (!labelCol.get(index) && StringUtils.isBlank(entry)) {//Check if column is no optional
                      lineBuffer.append(String.format(i18NService.getLabel("importLog.validation.empty.line", "Row number %s of line number %s is empty"), lineIndex, index));
                  }
            });
            if (StringUtils.isNoneBlank(lineBuffer.toString())) {
                errors.append(lineBuffer.toString());
            }
        });
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

    private static HashMap getLabelCol() {
        return new HashMap() {{
            put(0, false);
            put(1, true);
            put(2, false);
            put(3, true);
            put(4, true);
            put(5, true);
        }};
    }



    private static HashMap getKnowledgeCol() {
        return new HashMap() {{
            put(0, false);
            put(1, true);
            put(2, false);
            put(3, false);
            put(4, false);
            put(5, true);
        }};
    }

    private void transformAndSaveLabelsData(List<List<String>> labelSheet) {
        labelSheet.forEach(line -> {
            final KnowlegeLabelModel label = knowledgeLabelConverter.convert(line);
            try {
                Optional itemOp = flexibleSearchService.find(label.getCode(), "code", KnowlegeLabelModel._TYPECODE);

                if (itemOp.isPresent()) {
                    KnowlegeLabelModel item = (KnowlegeLabelModel) itemOp.get();
                    label.setPK(item.getPK());
                }
                getModelService().save(label);
            } catch (ModelServiceException e) {
                LOG.error(String.format("Error while processing line %s of Label sheet", labelSheet.indexOf(line)), e);
            }
        });
    }


}
