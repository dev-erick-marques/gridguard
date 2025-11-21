package com.gridguard.coordinator.dto;

public record SignedData(
        String publicKey,
        String signature) {
}
