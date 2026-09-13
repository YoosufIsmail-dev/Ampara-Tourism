package com.ampara.tourism.dto;

import jakarta.validation.constraints.NotBlank;

public class DeviceTokenRequest {

    @NotBlank
    private String token;

    /** ANDROID, IOS, or WEB. */
    private String platform;

    private String lang;

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
    public String getPlatform() { return platform; }
    public void setPlatform(String platform) { this.platform = platform; }
    public String getLang() { return lang; }
    public void setLang(String lang) { this.lang = lang; }
}
