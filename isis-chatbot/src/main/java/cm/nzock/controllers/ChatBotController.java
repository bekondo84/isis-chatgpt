package cm.nzock.controllers;

import cm.nzock.facades.ChatFacade;
import cm.nzock.services.ChatService;
import cm.platform.basecommerce.core.utils.IsisConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping(IsisConstants.ApiScope.API+"/chat")
public class ChatBotController {

    private static final Logger LOG = LoggerFactory.getLogger(ChatBotController.class);

    @Autowired
    private ChatFacade chatFacade;


    @PostMapping
    public ResponseEntity<String> tchat(@RequestParam("session")Long session , @RequestParam("text") String text) {
        LOG.info(String.format("Inside Chat API ------------------- %s", text));
        return ResponseEntity.ok(chatFacade.converse(session, text)) ;
    }

    @GetMapping("/settings")
    public ResponseEntity<Map<String, String>> generalSettings() {
        return ResponseEntity.ok(chatFacade.getGeneralSettings());
    }
}
