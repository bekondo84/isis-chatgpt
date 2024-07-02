package cm.nzock.converters;

import cm.platform.basecommerce.core.enums.ScriptType;
import cm.platform.basecommerce.core.knowledge.KnowlegeLabelModel;
import cm.platform.basecommerce.exceptions.ConvertionException;
import cm.platform.basecommerce.populator.Converter;
import cm.platform.basecommerce.services.EnumerationService;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ListToKnowledgeLabelConverter implements Converter <List<String>, KnowlegeLabelModel>{

    @Autowired
    private EnumerationService enumerationService;

    @Override
    public KnowlegeLabelModel convert(List<String> source) throws ConvertionException {
        final KnowlegeLabelModel label = new KnowlegeLabelModel();
        source.forEach(value -> {
            switch (source.indexOf(value)) {
                case 0 :
                    label.setCode(value); break;
                case 1 :
                    label.setLabel(value); break;
                case 2 :
                    label.setEndLabel(BooleanUtils.toBoolean(value)); break;
                case 3 :
                    label.setAction(value); break;
                case 4 :
                    if (StringUtils.isNoneBlank(value)) {
                        label.setType(enumerationService.getEnumerationValue(value, ScriptType.class));
                    }
                    break;
                case 5 :
                    label.setScript(value);
                    break;
                default:
            }
        });
        return label;
    }
}
