package com.gridguard.coordinator.dto;

public record SignedStatusDTO(
        DeviceStatusPayloadDTO payload,
        String publicKey,
        String signature
) {}