package cm.nzock.converters;

import cm.nzock.beans.DomainData;
import cm.platform.basecommerce.core.knowledge.KnowledgeModuleModel;
import cm.platform.basecommerce.exceptions.ConvertionException;
import cm.platform.basecommerce.populator.Converter;
import org.springframework.stereotype.Component;

@Component
public class DomainModelConverter implements Converter<KnowledgeModuleModel, DomainData> {
    @Override
    public DomainData convert(KnowledgeModuleModel source) throws ConvertionException {
        return new DomainData().setPk(source.getPK())
                .setCode(source.getCode())
                .setDefault(source.getDefaultmodel())
                .setDescription(source.getDescription())
                .setLabel(source.getLabel());
    }
}
