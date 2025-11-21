package com.gridguard.device.crypto;

import com.gridguard.device.dto.DeviceStatusPayloadDTO;
import com.gridguard.device.dto.SignedStatusDTO;

public class StatusSigner {

    public SignedStatusDTO sign(DeviceStatusPayloadDTO payload) {

        System.out.println("Simulating signing process...");
        System.out.println("Payload received: " + payload);
        System.out.println("Generating fake public key...");
        System.out.println("Generating fake signature...");

        String fakePublicKey = "PUBLIC_KEY_123";
        String fakeSignature = "SIGNATURE_ABC";

        return new SignedStatusDTO(payload, fakePublicKey, fakeSignature);
    }
}
