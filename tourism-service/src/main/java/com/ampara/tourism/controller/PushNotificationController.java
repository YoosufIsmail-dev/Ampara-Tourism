package com.ampara.tourism.controller;

import com.ampara.tourism.dto.DeviceTokenRequest;
import com.ampara.tourism.dto.PushNotificationRequest;
import com.ampara.tourism.service.PushNotificationService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Firebase Cloud Messaging push notifications.
 *
 * Client apps call /register on launch/login with their FCM device token, and /unregister
 * on logout. Registered devices are auto-subscribed to the "all-tourists" and "events"
 * topics. Admins can broadcast via /send; EventController also fires an automatic
 * "events" topic push whenever a new Event is created.
 */
@RestController
@RequestMapping("/api/push")
public class PushNotificationController {

    private final PushNotificationService pushNotificationService;

    public PushNotificationController(PushNotificationService pushNotificationService) {
        this.pushNotificationService = pushNotificationService;
    }

    @GetMapping("/status")
    public Map<String, Object> status() {
        return Map.of("configured", pushNotificationService.isConfigured());
    }

    @PostMapping("/register")
    public ResponseEntity<Void> register(@Valid @RequestBody DeviceTokenRequest request) {
        pushNotificationService.register(request.getToken(), request.getPlatform(), request.getLang());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/unregister")
    public ResponseEntity<Void> unregister(@Valid @RequestBody DeviceTokenRequest request) {
        pushNotificationService.unregister(request.getToken());
        return ResponseEntity.ok().build();
    }

    /** Admin: broadcast to all registered devices (default) or a specific topic ("events", etc). */
    @PostMapping("/send")
    public Map<String, String> send(@Valid @RequestBody PushNotificationRequest request) {
        String topic = (request.getTopic() == null || request.getTopic().isBlank())
                ? PushNotificationService.BROADCAST_TOPIC
                : request.getTopic();
        String result = pushNotificationService.sendToTopic(topic, request.getTitle(), request.getBody(), request.getData());
        return Map.of("topic", topic, "status", result);
    }
}
