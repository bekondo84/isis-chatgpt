package cm.nzock.controllers;

import cm.nzock.beans.ChatLabelData;
import cm.nzock.facades.ChatFacade;
import cm.nzock.services.ChatService;
import cm.platform.basecommerce.core.utils.IsisConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(IsisConstants.ApiScope.PUBLIC+"/chat")
public class OpenChatBotController {

    private static final Logger LOG = LoggerFactory.getLogger(OpenChatBotController.class);

    @Autowired
    private ChatFacade chatFacade;


    @PostMapping
    public ResponseEntity<ChatLabelData> tchat(@RequestParam("uuid")String uuid, @RequestParam("text") String text) {
        LOG.info(String.format("Inside OPEn Chat API ------------------- %s", text));
        return ResponseEntity.ok(chatFacade.converse(null, uuid, text)) ;
    }

    @GetMapping("/uuid")
    public ResponseEntity<String> getUuid() {
         return ResponseEntity.ok(chatFacade.generateUuid());
    }

}
