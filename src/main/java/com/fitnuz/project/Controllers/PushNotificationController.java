package com.fitnuz.project.Controllers;

import com.fitnuz.project.Service.Implementations.PushNotificationService;
import com.fitnuz.project.Util.AuthUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/push")
public class PushNotificationController {

    @Autowired
    private PushNotificationService pushNotificationService;

    @Autowired
    private AuthUtil authUtil;

    @PostMapping("/subscribe")
    public ResponseEntity<String> subscribe(@RequestBody Map<String, Object> body) {
        String email = authUtil.getUserEmail();
        String endpoint = (String) body.get("endpoint");

        @SuppressWarnings("unchecked")
        Map<String, String> keys = (Map<String, String>) body.get("keys");
        String p256dh = keys.get("p256dh");
        String auth = keys.get("auth");

        pushNotificationService.saveSubscription(endpoint, p256dh, auth, email);
        return ResponseEntity.ok("Subscribed successfully");
    }

    @PostMapping("/unsubscribe")
    public ResponseEntity<String> unsubscribe(@RequestBody Map<String, String> body) {
        String endpoint = body.get("endpoint");
        pushNotificationService.removeSubscription(endpoint);
        return ResponseEntity.ok("Unsubscribed successfully");
    }

    @GetMapping("/vapid-key")
    public ResponseEntity<Map<String, String>> getVapidKey() {
        return ResponseEntity.ok(Map.of("publicKey", pushNotificationService.getVapidPublicKey()));
    }
}
