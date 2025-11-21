package com.gridguard.device.service;

import com.gridguard.device.crypto.StatusSigner;
import com.gridguard.device.dto.DeviceStatusPayloadDTO;
import com.gridguard.device.dto.SignedStatusDTO;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Random;

@Service
@EnableScheduling
public class DeviceService {

    private final RestTemplate http = new RestTemplate();
    private final Random rand = new Random();
    private static final String DEVICE_ID = "device-1";
    private static final String HEARTBEAT_ENDPOINT = "http://localhost:8080/coordinator/heartbeat";
    private static final long HEARTBEAT_INTERVAL_MS = 2000;
    private static final double BASE_VOLTAGE = 220.0;
    private static final double VOLTAGE_VARIATION = 5.0;

    private final KeyPair keyPair;

    public DeviceService() {
        try {
            KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
            kpg.initialize(2048);
            this.keyPair = kpg.generateKeyPair();
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate key pair", e);
        }
    }

    @Scheduled(fixedRate = HEARTBEAT_INTERVAL_MS)
    public void sendHeartbeat() {
        SignedStatusDTO heartbeat = buildHeartbeatPayload();

        try {
            http.postForEntity(HEARTBEAT_ENDPOINT, heartbeat, Void.class);
        } catch (RestClientException e) {
            System.err.println("Failed to send heartbeat");
        }
    }

    private SignedStatusDTO buildHeartbeatPayload() {
        StatusSigner signer = new StatusSigner();
        double voltage = BASE_VOLTAGE + (rand.nextDouble() - 0.5) * VOLTAGE_VARIATION;

        DeviceStatusPayloadDTO payload = new DeviceStatusPayloadDTO(
                DEVICE_ID,
                voltage,
                Instant.now().truncatedTo(ChronoUnit.MILLIS)
        );
        return signer.sign(payload, keyPair.getPrivate(), keyPair.getPublic());
    }
}
