package com.ampara.tourism.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * A registered Firebase Cloud Messaging device token (mobile app or web push).
 * Every registered token is auto-subscribed to the "all-tourists" and "events"
 * topics so admins can broadcast without tracking individual tokens.
 */
@Entity
@Table(name = "device_token", uniqueConstraints = @UniqueConstraint(columnNames = "token"))
public class DeviceToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 512)
    private String token;

    /** ANDROID, IOS, or WEB. */
    private String platform;

    /** Tourist's preferred language (en/ta/si) - lets you send localized push copy later. */
    private String lang;

    private LocalDateTime registeredAt = LocalDateTime.now();

    public DeviceToken() {
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
    public String getPlatform() { return platform; }
    public void setPlatform(String platform) { this.platform = platform; }
    public String getLang() { return lang; }
    public void setLang(String lang) { this.lang = lang; }
    public LocalDateTime getRegisteredAt() { return registeredAt; }
    public void setRegisteredAt(LocalDateTime registeredAt) { this.registeredAt = registeredAt; }
}
