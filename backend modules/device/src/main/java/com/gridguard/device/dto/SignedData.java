package com.gridguard.device.dto;

public record SignedData(
        String publicKey,
        String signature) {
}
