package cm.nzock.services.impl;

import cm.nzock.services.Doc2VecService;
import cm.nzock.services.ParagraphVector;
import org.deeplearning4j.models.paragraphvectors.ParagraphVectors;
import cm.platform.basecommerce.core.knowledge.KnowledgeModuleModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Service
public class DefaultParagraphVector implements ParagraphVector {

    @Autowired
    private Doc2VecService doc2VecService;
    //Map of ParagraphVectors in use
    private Map<String, ParagraphVectors> ctx = new HashMap<>() ;

    @Override
    public ParagraphVectors paragraphVectors(KnowledgeModuleModel domain) throws IOException {
        assert Objects.nonNull(domain): "Knownledge domain is not null";

        if (Objects.isNull(ctx.get(domain.getCode()))) {
            ctx.put(domain.getCode(), doc2VecService.buildParagraphVectorsFromModel(domain));
        }

        return ctx.get(domain.getCode());
    }
}
