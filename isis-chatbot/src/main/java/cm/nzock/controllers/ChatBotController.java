package cm.nzock.controllers;

import cm.nzock.beans.ChatData;
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
@RequestMapping(IsisConstants.ApiScope.API+"/chat")
public class ChatBotController {

    private static final Logger LOG = LoggerFactory.getLogger(ChatBotController.class);

    @Autowired
    private ChatFacade chatFacade;
    @Autowired
    private ChatSessionFacade sessionFacade ;


    @PostMapping
    public ResponseEntity<ChatData> tchat(@RequestParam("uuid") String uuid, @RequestParam(value = "session", required = false)Long session , @RequestParam("text") String text) {
        LOG.info(String.format("Inside Chat API ------------------- %s", text));
        return ResponseEntity.ok(chatFacade.converse(session, uuid, text)) ;
    }

    @GetMapping("/settings")
    public ResponseEntity<Map<String, String>> generalSettings() {
        return ResponseEntity.ok(chatFacade.getGeneralSettings());
    }

    @GetMapping("/uuid")
    public ResponseEntity<String> getUuid() {
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
