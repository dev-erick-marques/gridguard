package com.gridguard.device.controller;

import com.gridguard.device.dto.CommandRequestDTO;
import com.gridguard.device.dto.SignedCommandDTO;
import com.gridguard.device.enums.CommandStatus;
import com.gridguard.device.service.DeviceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/device")
@RequiredArgsConstructor
public class DeviceController {
    private final DeviceService deviceService;

    @PostMapping("/command")
    public ResponseEntity<CommandStatus> receiveCommand(@RequestBody SignedCommandDTO dto) {
        deviceService.validateSignature(dto);
        deviceService.applyCommand(dto.payload());
        return ResponseEntity.ok(dto.payload().command());
    }
}