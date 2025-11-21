package com.gridguard.coordinator.controller;


import com.gridguard.coordinator.dto.SignedStatusDTO;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/coordinator")
public class CoordinatorController {

    @PostMapping("/heartbeat")
    public void receiveHeartbeat(@RequestBody SignedStatusDTO heartbeat){
        System.out.println(heartbeat);
    }
}
