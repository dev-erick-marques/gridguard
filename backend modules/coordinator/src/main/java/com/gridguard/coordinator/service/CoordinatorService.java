package com.gridguard.coordinator.service;

import com.github.benmanes.caffeine.cache.Cache;
import com.gridguard.coordinator.crypto.CryptoUtils;
import com.gridguard.coordinator.dto.*;
import com.gridguard.coordinator.enums.Commands;
import com.gridguard.coordinator.enums.DeviceReason;
import lombok.Setter;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;


@Service
@EnableScheduling
public class CoordinatorService {
    private final Cache<String, Deque<DeviceStatusPayloadDTO>> cache;
    private final CryptoUtils cryptoUtils;
    private static final int MAX_RECORDS = 5;
    private static final double VARIATION_THRESHOLD_PERCENT = 3.0;
    private final Map<String, Integer> stableCounts = new HashMap<>();
    private final RestTemplate restTemplate = new RestTemplate();
    @Setter
    private boolean isShutdown = false;
    private final KeyPair keyPair;


    public CoordinatorService(
            Cache<String, Deque<DeviceStatusPayloadDTO>> cache, CryptoUtils cryptoUtils
    ) {
        this.cache = cache;
        this.cryptoUtils = cryptoUtils;
        try {
            KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
            kpg.initialize(2048);
            this.keyPair = kpg.generateKeyPair();
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate key pair", e);
        }
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
    public DevicesMetricsResponseDTO evaluateAllDevicesForInstability() {
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
                    if(!isShutdown) setShutdown(latest.status().equalsIgnoreCase("SHUTDOWN"));
                    System.out.println(isShutdown);
                    System.out.println(latest.status());
                    String deviceAddress = latest.deviceAddress();

                    return new DeviceMetricsWithInfo(deviceId, stats.mean, stats.std, variationPercent, isShutdown, deviceAddress);
                })
                .peek(dto -> {
                    String deviceId = dto.deviceId();
                    double variationPercent = dto.variationPercent();

                    if (variationPercent >= VARIATION_THRESHOLD_PERCENT) {
                        stableCounts.put(deviceId, 0);
                        if (!dto.isShutdown()){;
                            postDeviceCommand(dto.deviceId(), dto.deviceAddress(), Commands.SAFE_SHUTDOWN, DeviceReason.INSTABILITY);
                            setShutdown(true);
                        }
                    } else {
                        if (dto.isShutdown()) {
                            int count = stableCounts.getOrDefault(deviceId, 0) + 1;
                            stableCounts.put(deviceId, count);

                            if (count >= 10) {
                                System.out.println("Device stable for 10 cycles → sending SAFE_RESTART: " + deviceId);
                                postDeviceCommand(dto.deviceId(), dto.deviceAddress(), Commands.SAFE_RESTART, DeviceReason.NONE);
                                stableCounts.put(deviceId, 0);
                            }
                        } else {
                            stableCounts.put(deviceId, 0);
                        }
                    }
                }).map(e -> new DeviceMetricsDTO(e.deviceId(), e.mean(), e.std(), e.variationPercent()))
                .toList();

        return new DevicesMetricsResponseDTO(metricsList);

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
    private void postDeviceCommand(String deviceId, String address, Commands command, DeviceReason reason){
        CommandDTO payload =  new CommandDTO(deviceId, command, reason, Instant.now().truncatedTo(ChronoUnit.MILLIS));

        String payloadString = payload.deviceId() + "|" + payload.command() + "|" + payload.reason() + "|" + payload.timestamp();
        SignedData sign = cryptoUtils.sign(payloadString, keyPair.getPrivate(), keyPair.getPublic());
        SignedCommandDTO signed = new SignedCommandDTO(payload, sign.signature(), sign.publicKey());

        restTemplate.postForEntity(address, signed, Void.class);
    }
}
