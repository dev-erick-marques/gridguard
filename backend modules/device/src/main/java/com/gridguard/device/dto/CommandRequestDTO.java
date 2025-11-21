package com.gridguard.device.dto;

public record CommandRequestDTO(
        String deviceId,
        String command,
        long timestamp,
        String signature,
        String publicKey
) {}