package cm.nzock.controllers;

import cm.nzock.beans.DomainData;
import cm.nzock.facades.ChatFacade;
import cm.platform.basecommerce.core.knowledge.KnowledgeModuleModel;
import cm.platform.basecommerce.core.utils.IsisConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(IsisConstants.ApiScope.API+"/domain")
public class KnowledgeDomainController {

    @Autowired
    private ChatFacade facade;

    @GetMapping("/default")
    public ResponseEntity<DomainData> defaultDomain() {
         return ResponseEntity.ok(facade.defaultDomain());
    }
    @GetMapping
    public ResponseEntity<List<DomainData>> chatdomains() {
        return ResponseEntity.ok(facade.getDomains());
    }

}
