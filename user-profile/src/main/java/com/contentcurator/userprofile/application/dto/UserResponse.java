package com.contentcurator.userprofile.application.dto;
import java.time.Instant;
import java.util.List;
public record UserResponse(String id, String email, String displayName, List<String> interests, Instant createdAt) {}
