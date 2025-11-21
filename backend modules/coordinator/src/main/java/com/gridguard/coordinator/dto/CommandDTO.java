package com.gridguard.coordinator.dto;

import com.gridguard.coordinator.enums.Commands;
import com.gridguard.coordinator.enums.DeviceReason;

public record CommandDTO(
        String deviceId,
        Commands command,
        DeviceReason reason,
        java.time.Instant timestamp
) {
}
