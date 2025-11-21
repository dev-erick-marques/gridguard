package com.gridguard.coordinator.dto;

public record DeviceMetricsDTO(
        String deviceId,
        double mean,
        double std,
        double variationPercent
) {}