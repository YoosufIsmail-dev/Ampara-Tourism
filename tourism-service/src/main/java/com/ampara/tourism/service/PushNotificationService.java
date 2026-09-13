package com.ampara.tourism.service;

import com.ampara.tourism.entity.DeviceToken;
import com.ampara.tourism.repository.DeviceTokenRepository;
import com.google.firebase.FirebaseApp;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * Registers device tokens and sends push notifications via Firebase Cloud Messaging.
 * Every registered device is auto-subscribed to two topics so admins can broadcast
 * without maintaining their own per-device fan-out:
 *  - "all-tourists": general announcements
 *  - "events": fired automatically whenever a new Event is created (see EventController)
 *
 * If Firebase isn't configured (see FirebaseConfig), sends are logged instead of fired
 * so the rest of the app keeps working with zero code changes once credentials are added.
 */
@Service
public class PushNotificationService {

    private static final Logger log = LoggerFactory.getLogger(PushNotificationService.class);

    public static final String BROADCAST_TOPIC = "all-tourists";
    public static final String EVENTS_TOPIC = "events";

    private final DeviceTokenRepository deviceTokenRepository;
    private final FirebaseApp firebaseApp;

    public PushNotificationService(DeviceTokenRepository deviceTokenRepository, FirebaseApp firebaseApp) {
        this.deviceTokenRepository = deviceTokenRepository;
        this.firebaseApp = firebaseApp;
    }

    public boolean isConfigured() {
        return firebaseApp != null;
    }

    public DeviceToken register(String token, String platform, String lang) {
        DeviceToken record = deviceTokenRepository.findByToken(token).orElseGet(DeviceToken::new);
        record.setToken(token);
        record.setPlatform(platform);
        record.setLang(lang);
        DeviceToken saved = deviceTokenRepository.save(record);
        subscribe(token, BROADCAST_TOPIC);
        subscribe(token, EVENTS_TOPIC);
        return saved;
    }

    public void unregister(String token) {
        deviceTokenRepository.findByToken(token).ifPresent(deviceTokenRepository::delete);
        unsubscribe(token, BROADCAST_TOPIC);
        unsubscribe(token, EVENTS_TOPIC);
    }

    /** Send to a topic (e.g. "all-tourists" or "events"). Returns a status/message-id string. */
    public String sendToTopic(String topic, String title, String body, Map<String, String> data) {
        if (!isConfigured()) {
            log.info("[push-disabled] would send to topic '{}': {} - {}", topic, title, body);
            return "not-configured";
        }
        try {
            Message.Builder builder = Message.builder()
                    .setTopic(topic)
                    .setNotification(Notification.builder().setTitle(title).setBody(body).build());
            if (data != null) {
                builder.putAllData(data);
            }
            return FirebaseMessaging.getInstance(firebaseApp).send(builder.build());
        } catch (FirebaseMessagingException e) {
            log.error("Failed to send push to topic {}: {}", topic, e.getMessage());
            return "error: " + e.getMessage();
        }
    }

    public String sendToToken(String token, String title, String body, Map<String, String> data) {
        if (!isConfigured()) {
            log.info("[push-disabled] would send to a single device: {} - {}", title, body);
            return "not-configured";
        }
        try {
            Message.Builder builder = Message.builder()
                    .setToken(token)
                    .setNotification(Notification.builder().setTitle(title).setBody(body).build());
            if (data != null) {
                builder.putAllData(data);
            }
            return FirebaseMessaging.getInstance(firebaseApp).send(builder.build());
        } catch (FirebaseMessagingException e) {
            log.error("Failed to send push to device token: {}", e.getMessage());
            return "error: " + e.getMessage();
        }
    }

    private void subscribe(String token, String topic) {
        if (!isConfigured()) return;
        try {
            FirebaseMessaging.getInstance(firebaseApp).subscribeToTopic(List.of(token), topic);
        } catch (FirebaseMessagingException e) {
            log.warn("Failed to subscribe device to topic '{}': {}", topic, e.getMessage());
        }
    }

    private void unsubscribe(String token, String topic) {
        if (!isConfigured()) return;
        try {
            FirebaseMessaging.getInstance(firebaseApp).unsubscribeFromTopic(List.of(token), topic);
        } catch (FirebaseMessagingException e) {
            log.warn("Failed to unsubscribe device from topic '{}': {}", topic, e.getMessage());
        }
    }
}
