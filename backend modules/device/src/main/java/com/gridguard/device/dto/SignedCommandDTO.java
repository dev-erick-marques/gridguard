package com.gridguard.device.dto;

public record SignedCommandDTO(
        CommandRequestDTO payload,
        String signature,
        String publicKey
) {
}
