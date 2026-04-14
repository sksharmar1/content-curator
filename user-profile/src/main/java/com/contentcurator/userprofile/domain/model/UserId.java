package com.contentcurator.userprofile.domain.model;
import java.util.UUID;
public record UserId(String value) {
    public UserId { if (value == null || value.isBlank()) throw new IllegalArgumentException("UserId cannot be blank"); }
    public static UserId generate() { return new UserId(UUID.randomUUID().toString()); }
    public static UserId of(String value) { return new UserId(value); }
}
