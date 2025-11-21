package com.gridguard.device.dto;

import com.gridguard.device.enums.CommandStatus;
import com.gridguard.device.enums.DeviceReason;

import java.time.Instant;

public record CommandRequestDTO(
        String deviceId,
        CommandStatus command,
        DeviceReason reason,
        Instant timestamp
) {}