package cm.nzock.converters;

import cm.platform.basecommerce.core.enums.ScriptType;
import cm.platform.basecommerce.core.knowledge.KnowlegeLabelModel;
import cm.platform.basecommerce.exceptions.ConvertionException;
import cm.platform.basecommerce.populator.Converter;
import cm.platform.basecommerce.services.EnumerationService;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class ListToKnowledgeLabelConverter implements Converter <Map<String, List<String>>, KnowlegeLabelModel>{

    private static final Logger LOG = LoggerFactory.getLogger(ListToKnowledgeLabelConverter.class);

    public static final String HEADER = "header";
    public static final String ROW = "row";
    public static final String CODE = "code";
    public static final String TEMPLATE = "template";
    public static final String ENDLABEL = "endlabel";
    public static final String ACTION = "action";
    public static final String SCRIPTLANGUAGE = "scriptlanguage";
    public static final String SCRIPT = "script";
    @Autowired
    private EnumerationService enumerationService;

    @Override
    public KnowlegeLabelModel convert(Map<String, List<String>> source) throws ConvertionException {
         final  List<String> headers = source.get(HEADER);
         final List<String> values = source.get(ROW);
         KnowlegeLabelModel label = new KnowlegeLabelModel();
         for (String header : headers) {
             int index = headers.indexOf(header);
             String value = index < values.size() ? values.get(index) : null;
             if (header.equalsIgnoreCase(CODE)) {
                 label.setCode(value);
             } else if (header.equalsIgnoreCase(TEMPLATE)) {
                 label.setLabel(value);
             } else if (header.equalsIgnoreCase(ENDLABEL)) {
                 label.setEndLabel(BooleanUtils.toBoolean(value));
             } else if (header.equalsIgnoreCase(ACTION)) {
                 label.setAction(value);
             } else if (header.equalsIgnoreCase(SCRIPTLANGUAGE)) {
                 if (StringUtils.isNoneBlank(value)) {
                     label.setType(enumerationService.getEnumerationValue(value, ScriptType.class));
                 }
             } else  if (header.equalsIgnoreCase(SCRIPT)) {
                 label.setScript(value);
             }
         }
         return label;
    }
}
