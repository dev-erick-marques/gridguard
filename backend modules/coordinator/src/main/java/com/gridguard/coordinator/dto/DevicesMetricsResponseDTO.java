package com.gridguard.coordinator.dto;

import java.util.List;

public record DevicesMetricsResponseDTO(
        List<DeviceMetricsDTO> devices
) {}