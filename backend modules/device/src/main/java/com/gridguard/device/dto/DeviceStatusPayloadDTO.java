package com.gridguard.device.dto;

import java.time.Instant;

public record DeviceStatusPayloadDTO(
        String deviceId,
        double voltage,
        Instant timestamp
) {}

