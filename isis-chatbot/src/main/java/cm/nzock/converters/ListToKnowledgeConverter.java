package cm.nzock.converters;

import cm.platform.basecommerce.core.knowledge.KnowledgeModel;
import cm.platform.basecommerce.core.knowledge.KnowledgeTypeModel;
import cm.platform.basecommerce.core.knowledge.KnowlegeLabelModel;
import cm.platform.basecommerce.exceptions.ConvertionException;
import cm.platform.basecommerce.populator.Converter;
import cm.platform.basecommerce.services.FlexibleSearchService;
import cm.platform.basecommerce.services.UserService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
public class ListToKnowledgeConverter implements Converter<Map<String, List<String>>, KnowledgeModel> {

    public static final String HEADER = "header";
    public static final String ROW = "row";
    public static final String CODE = "code";
    public static final String TEXT = "text";
    public static final String KEYWORDS = "keywords";
    public static final String CLASS = "class";
    public static final String LABELCODE = "labelcode";
    @Autowired
    private FlexibleSearchService flexibleSearchService;
    @Autowired
    private UserService userService;


    @Override
    public KnowledgeModel convert(Map<String, List<String>> source) throws ConvertionException {
        final List<String> headers = source.get(HEADER);
        final List<String> values = source.get(ROW);
        final KnowledgeModel knowledge = new KnowledgeModel();
        knowledge.setCreated(new Date());
       // knowledge.setCreatedby(userService.getCurrentUser());

        for (String header : headers) {
            final int index = headers.indexOf(header);
            String entry = index < values.size() ? values.get(index) : null;
            if (header.equalsIgnoreCase(CODE)) {
                knowledge.setCode(entry);
            } else if (header.equalsIgnoreCase(TEXT)) {
                knowledge.setTemplate(entry);
            } else if (header.equalsIgnoreCase(KEYWORDS)) {
                knowledge.setKeywords(entry);
            } else if (header.equalsIgnoreCase(CLASS)) {
                if (StringUtils.isNoneBlank(entry)) {
                    flexibleSearchService.find(entry, CODE, KnowledgeTypeModel._TYPECODE)
                            .ifPresent(type -> knowledge.setCategory((KnowledgeTypeModel) type));
                }
            } else if (header.equalsIgnoreCase(LABELCODE)) {
                if (StringUtils.isNoneBlank(entry)) {
                    flexibleSearchService.find(entry, CODE, KnowlegeLabelModel._TYPECODE)
                            .ifPresent(output -> {
                                knowledge.setLabel((KnowlegeLabelModel) output);
                                knowledge.setLabeltext(((KnowlegeLabelModel)output).getLabel());
                            });
                }
            }
        }
        return knowledge;
    }
}
