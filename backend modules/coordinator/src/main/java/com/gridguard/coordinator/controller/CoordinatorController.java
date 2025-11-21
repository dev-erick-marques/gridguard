package com.gridguard.coordinator.controller;


import com.gridguard.coordinator.dto.SignedStatusDTO;
import com.gridguard.coordinator.service.CoordinatorService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/coordinator")
@RequiredArgsConstructor
public class CoordinatorController {
    private final CoordinatorService coordinatorService;

    @PostMapping("/heartbeat")
    public void receiveHeartbeat(@RequestBody SignedStatusDTO heartbeat){
        coordinatorService.process(heartbeat.payload());
    }
}
