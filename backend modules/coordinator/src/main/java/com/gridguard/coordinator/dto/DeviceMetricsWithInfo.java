package com.gridguard.coordinator.dto;

public record DeviceMetricsWithInfo(
        String deviceId,
        double mean,
        double std,
        double variationPercent,
        boolean isShutdown,
        String deviceAddress,
        String deviceName
) {}
