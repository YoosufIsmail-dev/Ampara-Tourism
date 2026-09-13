package com.ampara.tourism.dto;

public record AuthResponse(String token, String name, String email, String role) {
}
