package com.gridguard.device.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/device")
public class DeviceController {

    @PostMapping("/shutdown")
    public void safeShutdown(){
        System.out.println("Safe shutdown initiated");
    }

    @PostMapping("/restart")
    public void safeRestart(){
        System.out.println("Safe restart initiated");

    }
}
