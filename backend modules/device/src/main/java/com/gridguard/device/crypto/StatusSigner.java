package com.gridguard.device.crypto;

import com.gridguard.device.dto.DeviceStatusPayloadDTO;
import com.gridguard.device.dto.SignedData;
import com.gridguard.device.dto.SignedStatusDTO;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.util.Base64;

public class StatusSigner {

    public SignedData sign(String payload, PrivateKey privateKey, PublicKey publicKey) {
        try {
            Signature signer = Signature.getInstance("SHA256withRSA");
            signer.initSign(privateKey);

            signer.update(payload.getBytes());

            String signature = Base64.getEncoder().encodeToString(signer.sign());
            String encodedPublicKey = Base64.getEncoder().encodeToString(publicKey.getEncoded());

            return new SignedData(encodedPublicKey, signature);
        } catch (Exception e) {
            throw new RuntimeException("Error signing payload", e);
        }
    }
}
