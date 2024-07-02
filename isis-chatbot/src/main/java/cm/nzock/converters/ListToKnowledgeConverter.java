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
import java.util.Optional;

@Component
public class ListToKnowledgeConverter implements Converter<List<String>, KnowledgeModel> {

    @Autowired
    private FlexibleSearchService flexibleSearchService;
    @Autowired
    private UserService userService;


    @Override
    public KnowledgeModel convert(List<String> source) throws ConvertionException {
        final KnowledgeModel knowledge = new KnowledgeModel();
        knowledge.setCreated(new Date());
       // knowledge.setCreatedby(userService.getCurrentUser());
        source.forEach(entry -> {
            switch (source.indexOf(entry)) {
                case 0 :
                   knowledge.setCode(entry); break;
                case 1 :
                    knowledge.setTemplate(entry); break;
                case 2 :
                    knowledge.setKeywords(entry); break;
                case 3 :
                    if (StringUtils.isNoneBlank(entry)) {
                        flexibleSearchService.find(entry, "code", KnowledgeTypeModel._TYPECODE)
                                .ifPresent(type -> knowledge.setCategory((KnowledgeTypeModel) type));
                    }
                    break;
                case 4 :
                    if (StringUtils.isNoneBlank(entry)) {
                        flexibleSearchService.find(entry, "code", KnowlegeLabelModel._TYPECODE)
                                .ifPresent(output -> {
                                    knowledge.setLabel((KnowlegeLabelModel) output);
                                    knowledge.setLabeltext(((KnowlegeLabelModel)output).getLabel());
                                });
                    }
                    break;
                default:
            }
        });
        return knowledge;
    }
}
