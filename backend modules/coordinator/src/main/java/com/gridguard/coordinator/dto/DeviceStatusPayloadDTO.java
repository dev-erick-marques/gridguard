package com.gridguard.coordinator.dto;

import com.gridguard.coordinator.enums.DeviceReason;

import java.time.Instant;

public record DeviceStatusPayloadDTO(
        String deviceId,
        double voltage,
        String status,
        DeviceReason reason,
        String deviceAddress,
        String deviceName,
        Instant timestamp
) {}
