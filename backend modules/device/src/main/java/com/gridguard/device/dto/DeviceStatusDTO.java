package com.gridguard.device.dto;

import java.time.Instant;

public record DeviceStatusDTO(
        String deviceId,
        double voltage,
        Instant timestamp,
        String signature,
        String publicKey
) {}

