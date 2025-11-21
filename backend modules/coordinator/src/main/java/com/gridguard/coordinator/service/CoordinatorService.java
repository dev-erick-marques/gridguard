package com.gridguard.coordinator.service;

import com.gridguard.coordinator.dto.DeviceStatusPayloadDTO;
import org.springframework.stereotype.Service;


@Service
public class CoordinatorService {
    public void process(DeviceStatusPayloadDTO dto) {
        System.out.println(dto);
    }
}
