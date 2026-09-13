package com.ampara.tourism.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.ByteArrayInputStream;
import java.util.Base64;

/**
 * Initializes the Firebase Admin SDK for push notifications (Firebase Cloud Messaging).
 *
 * Set FIREBASE_CREDENTIALS_BASE64 to your service-account JSON, base64-encoded, e.g.:
 *   export FIREBASE_CREDENTIALS_BASE64=$(base64 -i service-account.json)
 *
 * If it's not set, this bean is a no-op (returns null) and PushNotificationService falls
 * back to logging what it would have sent - registration/unregistration endpoints still
 * work and store tokens, so nothing breaks and switching it on later needs no code change.
 */
@Configuration
public class FirebaseConfig {

    private static final Logger log = LoggerFactory.getLogger(FirebaseConfig.class);

    @Value("${app.firebase.credentials-base64:}")
    private String credentialsBase64;

    @Bean
    public FirebaseApp firebaseApp() {
        if (credentialsBase64 == null || credentialsBase64.isBlank()) {
            log.warn("FIREBASE_CREDENTIALS_BASE64 not set - push notifications are disabled " +
                    "(device token registration still works; sends will just log instead of firing).");
            return null;
        }
        try {
            if (!FirebaseApp.getApps().isEmpty()) {
                return FirebaseApp.getInstance();
            }
            byte[] decoded = Base64.getDecoder().decode(credentialsBase64.trim());
            GoogleCredentials credentials = GoogleCredentials.fromStream(new ByteArrayInputStream(decoded));
            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(credentials)
                    .build();
            return FirebaseApp.initializeApp(options);
        } catch (Exception e) {
            log.error("Failed to initialize Firebase from FIREBASE_CREDENTIALS_BASE64 - push notifications disabled: {}", e.getMessage());
            return null;
        }
    }
}
