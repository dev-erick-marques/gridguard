package com.gridguard.coordinator.service;

import com.github.benmanes.caffeine.cache.Cache;
import com.gridguard.coordinator.crypto.CryptoUtils;
import com.gridguard.coordinator.dto.*;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.*;


@Service
@EnableScheduling
public class CoordinatorService {
    private final Cache<String, Deque<DeviceStatusPayloadDTO>> cache;
    private final CryptoUtils cryptoUtils;
    private static final int MAX_RECORDS = 5;
    private static final double VARIATION_THRESHOLD_PERCENT = 3.0;
    private final Map<String, Integer> stableCounts = new HashMap<>();

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
    @Scheduled(fixedDelay = 5000L)
    public void evaluateAllDevicesForInstability() {
        List<DeviceMetricsDTO> metricsList = cache.asMap().entrySet().stream()
                .filter(entry -> entry.getValue().size() >= MAX_RECORDS)
                .map(entry -> {
                    String deviceId = entry.getKey();
                    Deque<DeviceStatusPayloadDTO> history = entry.getValue();

                    double[] voltages = history.stream()
                            .mapToDouble(DeviceStatusPayloadDTO::voltage)
                            .toArray();
                    Stats stats = computeStats(voltages);
                    double variationPercent = (stats.std / stats.mean) * 100.0;

                    DeviceStatusPayloadDTO latest = history.peekLast();
                    assert latest != null;
                    boolean isShutdown = latest.status().equalsIgnoreCase("SHUTDOWN");
                    String deviceAddress = latest.deviceAddress();

                    return new DeviceMetricsWithInfo(deviceId, stats.mean, stats.std, variationPercent, isShutdown, deviceAddress);
                })
                .peek(dto -> {
                    String deviceId = dto.deviceId();
                    double variationPercent = dto.variationPercent();

                    if (variationPercent >= VARIATION_THRESHOLD_PERCENT) {
                        stableCounts.put(deviceId, 0);
                    } else {
                        if (dto.isShutdown()) {
                            int count = stableCounts.getOrDefault(deviceId, 0) + 1;
                            stableCounts.put(deviceId, count);

                            if (count >= 10) {
                                System.out.println("Device stable for 10 cycles → sending SAFE_RESTART: " + deviceId);
                                stableCounts.put(deviceId, 0);
                            }
                        } else {
                            stableCounts.put(deviceId, 0);
                        }
                    }
                }).map(e -> new DeviceMetricsDTO(e.deviceId(), e.mean(), e.std(), e.variationPercent()))
                .toList();

        System.out.println(new DevicesMetricsResponseDTO(metricsList));

    }

    private Stats computeStats(double[] values) {
        DoubleSummaryStatistics dss = Arrays.stream(values).summaryStatistics();
        double mean = dss.getAverage();

        double variance = Arrays.stream(values)
                .map(v -> Math.pow(v - mean, 2))
                .sum() / values.length;

        double std = Math.sqrt(variance);

        return new Stats(mean, std);
    }
    private record Stats(double mean, double std) {}

    public void validateSignature(SignedStatusDTO dto){
        var payload = dto.payload();
        String signingContent = payload.deviceId() + "|" + payload.voltage() + "|" + payload.status() + "|" + payload.reason() + "|" + payload.timestamp() + payload.deviceAddress();

        boolean isValid = cryptoUtils.validate(signingContent, dto.signature(), dto.publicKey());
        if (!isValid) {
            throw new RuntimeException("Payload is not valid");
        }
    }
}
