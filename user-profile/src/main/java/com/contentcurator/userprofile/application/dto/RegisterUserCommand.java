package com.contentcurator.userprofile.application.dto;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
public record RegisterUserCommand(
    @Email @NotBlank String email,
    @NotBlank @Size(min = 8, max = 64) String password,
    @NotBlank @Size(min = 2, max = 50) String displayName
) {}
