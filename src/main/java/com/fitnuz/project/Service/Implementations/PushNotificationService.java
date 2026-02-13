package com.fitnuz.project.Service.Implementations;

import com.fitnuz.project.Model.PushSubscription;
import com.fitnuz.project.Model.User;
import com.fitnuz.project.Repository.PushSubscriptionRepository;
import com.fitnuz.project.Repository.UserRepository;
import jakarta.annotation.PostConstruct;
import jakarta.transaction.Transactional;
import nl.martijndwars.webpush.Notification;
import nl.martijndwars.webpush.PushService;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.Security;
import java.util.List;

@Service
public class PushNotificationService {

    private static final Logger logger = LoggerFactory.getLogger(PushNotificationService.class);

    @Autowired
    private PushSubscriptionRepository pushSubscriptionRepository;

    @Autowired
    private UserRepository userRepository;

    @Value("${vapid.public.key}")
    private String vapidPublicKey;

    @Value("${vapid.private.key}")
    private String vapidPrivateKey;

    private PushService pushService;

    @PostConstruct
    public void init() {
        if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null) {
            Security.addProvider(new BouncyCastleProvider());
        }
    }

    private PushService getPushService() throws Exception {
        if (pushService == null) {
            pushService = new PushService(vapidPublicKey, vapidPrivateKey);
        }
        return pushService;
    }

    public void saveSubscription(String endpoint, String p256dh, String auth, String userEmail) {
        if (pushSubscriptionRepository.existsByEndpoint(endpoint)) {
            return;
        }
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + userEmail));

        PushSubscription subscription = new PushSubscription(endpoint, p256dh, auth, user);
        pushSubscriptionRepository.save(subscription);
    }

    @Transactional
    public void removeSubscription(String endpoint) {
        pushSubscriptionRepository.deleteByEndpoint(endpoint);
    }

    public void sendNotificationToUser(String email, String title, String message, Long orderId) {
        List<PushSubscription> subscriptions = pushSubscriptionRepository.findByUserEmail(email);
        if (subscriptions.isEmpty()) {
            return;
        }

        String payload = String.format(
                "{\"title\":\"%s\",\"body\":\"%s\",\"orderId\":%d,\"url\":\"/profile/orders\"}",
                escapeJson(title), escapeJson(message), orderId
        );

        for (PushSubscription sub : subscriptions) {
            try {
                Notification notification = new Notification(
                        sub.getEndpoint(),
                        sub.getP256dh(),
                        sub.getAuth(),
                        payload
                );
                getPushService().send(notification);
            } catch (Exception e) {
                logger.error("Failed to send push notification to endpoint: {}", sub.getEndpoint(), e);
            }
        }
    }

    public String getVapidPublicKey() {
        return vapidPublicKey;
    }

    private String escapeJson(String value) {
        if (value == null) return "";
        return value.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}
