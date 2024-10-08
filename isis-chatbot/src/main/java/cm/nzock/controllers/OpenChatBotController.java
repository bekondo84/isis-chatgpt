package cm.nzock.controllers;

import cm.nzock.beans.ChatData;
import cm.nzock.beans.DomainData;
import cm.nzock.facades.ChatFacade;
import cm.nzock.facades.ChatSessionFacade;
import cm.platform.basecommerce.core.utils.IsisConstants;
import cm.platform.basecommerce.services.exceptions.ModelServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(IsisConstants.ApiScope.PUBLIC+"/chat")
public class OpenChatBotController {

    private static final Logger LOG = LoggerFactory.getLogger(OpenChatBotController.class);

    @Autowired
    private ChatFacade chatFacade;
    @Autowired
    private ChatSessionFacade sessionFacade ;


    @GetMapping("/settings")
    public ResponseEntity<Map<String, String>> generalSettings() {
        return ResponseEntity.ok(chatFacade.getGeneralSettings());
    }

    @GetMapping("/domain")
    public ResponseEntity<DomainData> defaultDomain() {
        return ResponseEntity.ok(chatFacade.defaultDomain());
    }

    @PostMapping
    public ResponseEntity<ChatData> tchat(@RequestParam("uuid")String uuid, @RequestParam("domain")Long domain, @RequestParam("text") String text) {
        //LOG.info(String.format("Inside OPEn Chat API ------------------- %s", text));
        return ResponseEntity.ok(chatFacade.converse(null, uuid, domain, text)) ;
    }

    @GetMapping("/uuid")
    public ResponseEntity<String> getUuid() throws ModelServiceException {
         return ResponseEntity.ok(chatFacade.generateUuid());
    }

    @GetMapping("/init")
    ResponseEntity<List<ChatData>> sessionInit(@RequestParam(value = "session", required = false)Long session,
                                               @RequestParam(value = "uuid")String uuid) throws ModelServiceException {
        return ResponseEntity.ok(sessionFacade.initChatSession(session, uuid));
    }

    @PutMapping("/review/{pk}/{value}")
    public void userReview(@PathVariable("pk")Long pk, @PathVariable("value") Boolean value) throws ModelServiceException {
         chatFacade.userReview(pk, value);
    }
}
