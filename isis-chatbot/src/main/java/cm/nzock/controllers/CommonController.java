package cm.nzock.controllers;

import cm.nzock.facades.ChatFacade;
import cm.platform.basecommerce.core.utils.IsisConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(IsisConstants.ApiScope.PUBLIC+"/commons")
public class CommonController {

    @Autowired
    private ChatFacade chatFacade;

    @GetMapping("/title")
    public ResponseEntity<String> chatTitle() {
         return ResponseEntity.ok(chatFacade.getTitle());
    }
}
