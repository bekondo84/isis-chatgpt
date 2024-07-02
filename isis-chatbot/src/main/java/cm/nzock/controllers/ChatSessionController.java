package cm.nzock.controllers;

import cm.nzock.beans.ChatSessionData;
import cm.nzock.converters.ConvertChatSessionDataToModel;
import cm.nzock.facades.ChatSessionFacade;
import cm.platform.basecommerce.core.chat.ChatLogModel;
import cm.platform.basecommerce.core.utils.IsisConstants;
import cm.platform.basecommerce.services.FlexibleSearchService;
import cm.platform.basecommerce.services.ModelService;
import cm.platform.basecommerce.services.exceptions.ModelServiceException;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping(IsisConstants.ApiScope.API+"/session")
public class ChatSessionController {
    private static final Logger LOG = LoggerFactory.getLogger(ChatSessionController.class);

    @Autowired
    private ModelService modelService;
    @Autowired
    private FlexibleSearchService flexibleSearchService;
    @Autowired
    private ConvertChatSessionDataToModel convert ;
    @Autowired
    private ChatSessionFacade sessionFacade ;

    @DeleteMapping("/{pk}")
    public void delete(@PathVariable("pk")Long pk) throws ModelServiceException {
        sessionFacade.delete(pk);
    }

    @PostMapping
    public ResponseEntity<ChatSessionData> createOrUpdate(Authentication authentication, @RequestBody ChatSessionData sessionData) throws ModelServiceException {
        assert StringUtils.isNoneBlank(sessionData.getLabel()) : "Session label can't be empty or null";
        assert Objects.nonNull(authentication) : "No authentication found";

        return ResponseEntity.ok(sessionFacade.createOrUpdate(sessionData));
    }

    @GetMapping("/init/{session}")
    ResponseEntity<List<ChatLogModel>> sessionInit(Authentication authentication, @PathVariable("session")Long session) {
       assert Objects.nonNull(authentication): "No authentication found";
       return ResponseEntity.ok(sessionFacade.initChatSession(session));
    }

    @GetMapping
    ResponseEntity<List<ChatSessionData>> getUserSessions() {
       return ResponseEntity.ok(sessionFacade.getUserSessions());
    }
}
