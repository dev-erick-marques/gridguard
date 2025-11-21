package com.gridguard.device.service;

import com.gridguard.device.dto.DeviceStatusDTO;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Random;

@Service
@EnableScheduling
public class DeviceService {

    private final RestTemplate http = new RestTemplate();
    private final Random rand = new Random();

    @Scheduled(fixedRate = 2000)
    public void sendHeartbeat() {
        DeviceStatusDTO dto = new DeviceStatusDTO(
                "device-1",
                220 + (rand.nextDouble() - 0.5) * 5,
                0,
                Instant.now().truncatedTo(ChronoUnit.MILLIS),
                "signature",
                "public-key"
        );
         try {
             http.postForEntity(
                     "http://localhost:8080/coordinator/heartbeat",
                     dto,
                     Void.class
             );
         } catch (RestClientException e) {
             System.out.println("Failed to send heartbeat");
         }

    }
}
