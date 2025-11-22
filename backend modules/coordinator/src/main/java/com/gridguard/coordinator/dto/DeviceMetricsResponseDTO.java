package com.gridguard.coordinator.dto;

import java.util.List;

public record DeviceMetricsResponseDTO(
        List<DeviceMetricsDTO> devices
) {}