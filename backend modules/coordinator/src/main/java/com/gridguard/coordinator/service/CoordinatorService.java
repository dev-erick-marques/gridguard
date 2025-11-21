package com.gridguard.coordinator.service;

import com.github.benmanes.caffeine.cache.Cache;
import com.gridguard.coordinator.crypto.CryptoUtils;
import com.gridguard.coordinator.dto.DeviceStatusPayloadDTO;
import com.gridguard.coordinator.dto.SignedStatusDTO;
import org.springframework.stereotype.Service;

import java.util.ArrayDeque;
import java.util.Deque;


@Service
public class CoordinatorService {
    private final Cache<String, Deque<DeviceStatusPayloadDTO>> cache;
    private final CryptoUtils cryptoUtils;
    private static final int MAX_RECORDS = 10;

    public CoordinatorService(
            Cache<String, Deque<DeviceStatusPayloadDTO>> cache, CryptoUtils cryptoUtils
    ) {
        this.cache = cache;
        this.cryptoUtils = cryptoUtils;
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
    public void validateSignature(SignedStatusDTO dto){
        cryptoUtils.validate();
    }
}
