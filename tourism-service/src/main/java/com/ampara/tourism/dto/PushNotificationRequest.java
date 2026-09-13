package com.ampara.tourism.dto;

import jakarta.validation.constraints.NotBlank;

import java.util.Map;

public class PushNotificationRequest {

    @NotBlank
    private String title;

    @NotBlank
    private String body;

    /** Optional - defaults to the "all-tourists" broadcast topic. Use "events" to target event subscribers only. */
    private String topic;

    /** Optional key/value payload delivered alongside the notification (e.g. {"placeId":"12"}). */
    private Map<String, String> data;

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getBody() { return body; }
    public void setBody(String body) { this.body = body; }
    public String getTopic() { return topic; }
    public void setTopic(String topic) { this.topic = topic; }
    public Map<String, String> getData() { return data; }
    public void setData(Map<String, String> data) { this.data = data; }
}
