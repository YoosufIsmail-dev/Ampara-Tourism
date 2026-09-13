package com.ampara.tourism.dto;

public class VoiceGuideResponse {

    private Long placeId;
    private String placeName;
    private String lang;
    private String script;
    /** BCP-47 locale tag for client-side TTS (e.g. Web Speech API / mobile TTS engines). */
    private String ttsLocale;

    /** URL for real synthesized MP3 audio (OpenAI TTS), or null if OPENAI_API_KEY isn't configured. */
    private String audioUrl;

    public VoiceGuideResponse() {
    }

    public VoiceGuideResponse(Long placeId, String placeName, String lang, String script, String ttsLocale, String audioUrl) {
        this.placeId = placeId;
        this.placeName = placeName;
        this.lang = lang;
        this.script = script;
        this.ttsLocale = ttsLocale;
        this.audioUrl = audioUrl;
    }

    public Long getPlaceId() { return placeId; }
    public void setPlaceId(Long placeId) { this.placeId = placeId; }
    public String getPlaceName() { return placeName; }
    public void setPlaceName(String placeName) { this.placeName = placeName; }
    public String getLang() { return lang; }
    public void setLang(String lang) { this.lang = lang; }
    public String getScript() { return script; }
    public void setScript(String script) { this.script = script; }
    public String getTtsLocale() { return ttsLocale; }
    public void setTtsLocale(String ttsLocale) { this.ttsLocale = ttsLocale; }
    public String getAudioUrl() { return audioUrl; }
    public void setAudioUrl(String audioUrl) { this.audioUrl = audioUrl; }
}
