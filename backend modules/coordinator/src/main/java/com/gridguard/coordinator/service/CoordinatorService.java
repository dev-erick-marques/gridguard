package com.gridguard.coordinator.service;

import com.github.benmanes.caffeine.cache.Cache;
import com.gridguard.coordinator.dto.DeviceStatusPayloadDTO;
import org.springframework.stereotype.Service;

import java.util.ArrayDeque;
import java.util.Deque;


@Service
public class CoordinatorService {
    private final Cache<String, Deque<DeviceStatusPayloadDTO>> cache;
    private static final int MAX_RECORDS = 10;

    public CoordinatorService(
            Cache<String, Deque<DeviceStatusPayloadDTO>> cache
    ) {
        this.cache = cache;
    }

    public void process(DeviceStatusPayloadDTO dto) {
        Deque<DeviceStatusPayloadDTO> history = cache.get(dto.deviceId(), k -> new ArrayDeque<>());
        assert history != null;
        pushAndTrim(history, dto);
    }

    private void pushAndTrim(Deque<DeviceStatusPayloadDTO> history, DeviceStatusPayloadDTO dto) {
        if (history.size() == MAX_RECORDS) {
            history.removeFirst();
        }
        history.addLast(dto);
    }
}
