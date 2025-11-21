package com.gridguard.coordinator.service;

import com.github.benmanes.caffeine.cache.Cache;
import com.gridguard.coordinator.crypto.CryptoUtils;
import com.gridguard.coordinator.dto.DeviceMetricsDTO;
import com.gridguard.coordinator.dto.DeviceStatusPayloadDTO;
import com.gridguard.coordinator.dto.DevicesMetricsResponseDTO;
import com.gridguard.coordinator.dto.SignedStatusDTO;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.*;


@Service
@EnableScheduling
public class CoordinatorService {
    private final Cache<String, Deque<DeviceStatusPayloadDTO>> cache;
    private final CryptoUtils cryptoUtils;
    private static final int MAX_RECORDS = 10;
    private static final double VARIATION_THRESHOLD_PERCENT = 3.0;

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
        List<DeviceMetricsDTO> metricsList = cache.asMap().entrySet().stream()
                .filter(entry -> entry.getValue().size() >= MAX_RECORDS)
                .map(entry -> {
                    String deviceId = entry.getKey();
                    double[] voltages = entry.getValue().stream()
                            .mapToDouble(DeviceStatusPayloadDTO::voltage)
                            .toArray();

                    Stats stats = computeStats(voltages);
                    double variationPercent = (stats.std / stats.mean) * 100.0;

                    return new DeviceMetricsDTO(deviceId, stats.mean, stats.std, variationPercent);
                })
                .peek(dto -> {
                    if (dto.variationPercent() >= VARIATION_THRESHOLD_PERCENT) {
                        System.out.println("[Instability] Trigger: SAFE SHUTDOWN for " + dto.deviceId());
                    }
                })
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
        var payloadDto = dto.payload();
        String signingContent = payloadDto.deviceId() + "|" + payloadDto.voltage() + "|" + payloadDto.timestamp();

        boolean isValid = cryptoUtils.validate(signingContent, dto.signature(), dto.publicKey());
        if (!isValid) {
            throw new RuntimeException("Payload is not valid");
        }
    }
}
