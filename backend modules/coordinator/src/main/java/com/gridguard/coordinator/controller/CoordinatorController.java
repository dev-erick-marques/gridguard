package com.gridguard.coordinator.controller;


import com.gridguard.coordinator.dto.DeviceMetricsDTO;
import com.gridguard.coordinator.dto.DeviceMetricsResponseDTO;
import com.gridguard.coordinator.dto.SignedStatusDTO;
import com.gridguard.coordinator.service.CoordinatorService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/coordinator")
@RequiredArgsConstructor
@CrossOrigin("http://localhost:5173/")
public class CoordinatorController {
    private final CoordinatorService coordinatorService;

    @PostMapping("/heartbeat")
    public void receiveHeartbeat(@RequestBody SignedStatusDTO heartbeat){
        coordinatorService.validateSignature(heartbeat);
        coordinatorService.process(heartbeat.payload());
    }
    private volatile DeviceMetricsResponseDTO latestMetrics = new DeviceMetricsResponseDTO(List.of());


    @GetMapping(path = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<DeviceMetricsResponseDTO> streamMetrics() {
        return Flux.interval(Duration.ofSeconds(5))
                .map(tick -> latestMetrics);
    }

    @Scheduled(fixedRate = 5000)
    public void refreshMetrics() {
        latestMetrics = coordinatorService.evaluateAllDevicesForInstability();
    }

    @GetMapping("/history")
    public Map<String, List<DeviceMetricsDTO>> getHistory() {
        System.out.println(coordinatorService.getMetricsHistory());
        return coordinatorService.getMetricsHistory();
    }
}
