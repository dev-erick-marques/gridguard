package com.gridguard.device.dto;

public record SignedStatusDTO(
        DeviceStatusPayloadDTO payload,
        String publicKey,
        String signature
) {}