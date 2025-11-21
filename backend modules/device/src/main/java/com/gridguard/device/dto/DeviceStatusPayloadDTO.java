package com.gridguard.device.dto;

import com.gridguard.device.enums.DeviceReason;
import com.gridguard.device.enums.DeviceStatus;

import java.time.Instant;

public record DeviceStatusPayloadDTO(
        String deviceId,
        double voltage,
        DeviceStatus status,
        DeviceReason reason,
        Instant timestamp
) {}

