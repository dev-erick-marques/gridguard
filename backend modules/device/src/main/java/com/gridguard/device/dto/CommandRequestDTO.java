package com.gridguard.device.dto;

import com.gridguard.device.enums.CommandStatus;
import com.gridguard.device.enums.DeviceReason;

public record CommandRequestDTO(
        String deviceId,
        CommandStatus command,
        DeviceReason reason,
        long timestamp,
        String signature,
        String publicKey
) {}