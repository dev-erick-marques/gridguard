package com.gridguard.coordinator.dto;

import java.time.Instant;

public record DeviceStatusPayloadDTO(
        String deviceId,
        double voltage,
        Instant timestamp
) {}
