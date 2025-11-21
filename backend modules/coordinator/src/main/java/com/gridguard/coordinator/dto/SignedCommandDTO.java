package com.gridguard.coordinator.dto;

public record SignedCommandDTO(
        CommandDTO payload,
        String signature,
        String publicKey
) {
}
