package com.gridguard.device.controller;

import com.gridguard.device.dto.CommandRequestDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/device")
public class DeviceController {

    @PostMapping("/shutdown")
    public ResponseEntity<String> safeShutdown(@RequestBody CommandRequestDTO dto) {
        System.out.println(dto);
        return ResponseEntity.ok("Safe shutdown initiated");
    }

    @PostMapping("/restart")
    public ResponseEntity<String> safeRestart(@RequestBody CommandRequestDTO dto) {
        System.out.println(dto);
        return ResponseEntity.ok("Safe restart initiated");
    }
}