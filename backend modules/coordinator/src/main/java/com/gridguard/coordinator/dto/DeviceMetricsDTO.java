package com.gridguard.coordinator.dto;

import java.time.Instant;

public record DeviceMetricsDTO(
        String deviceId,
        String deviceName,
        double voltage,
        double std,
        double variationPercent,
        Instant timestamp) {}