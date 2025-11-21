package com.gridguard.coordinator.dto;

import java.time.Instant;

public record DeviceStatusDTO(
        String deviceId,
        double voltage,
        double deviation,
        Instant timestamp,
        String signature,
        String publicKey
) {}
