package com.gridguard.device.dto;

import com.gridguard.device.enums.ValidCommands;
import com.gridguard.device.enums.ValidReasons;

public record CommandRequestDTO(
        String deviceId,
        ValidCommands command,
        ValidReasons reason,
        long timestamp,
        String signature,
        String publicKey
) {}