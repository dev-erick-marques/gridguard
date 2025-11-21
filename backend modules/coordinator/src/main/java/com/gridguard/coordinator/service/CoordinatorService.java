package com.gridguard.coordinator.service;

import com.github.benmanes.caffeine.cache.Cache;
import com.gridguard.coordinator.crypto.CryptoUtils;
import com.gridguard.coordinator.dto.DeviceStatusPayloadDTO;
import com.gridguard.coordinator.dto.SignedStatusDTO;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;


@Service
@EnableScheduling
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
    @Scheduled(fixedDelay = 10000L)
    public void evaluateAllDevicesForInstability() {
        cache.asMap().forEach((deviceId, history) -> {
            System.out.println("[Evaluating] Device " + deviceId + " with " + history.size() + " records");
        });
    }

    private void computeStats(double[] values) {
        System.out.println("[Computing Stats] " + Arrays.toString(values));
    }
    public void validateSignature(SignedStatusDTO dto){
        var payloadDto = dto.payload();
        String signingContent = payloadDto.deviceId() + "|" + payloadDto.voltage() + "|" + payloadDto.timestamp();

        boolean isValid = cryptoUtils.validate(signingContent, dto.signature(), dto.publicKey());
        if (!isValid) {
            throw new RuntimeException("Payload is not valid");
        }
    }
}
