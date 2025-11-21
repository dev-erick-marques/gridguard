package com.gridguard.device.crypto;

import com.gridguard.device.dto.SignedData;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

@Component
public class CryptoUtils {
    public boolean validate(String payload, String signature, String publicKey) {
        try {
            byte[] publicKeyBytes = Base64.getDecoder().decode(publicKey);
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(publicKeyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            PublicKey pubKey = keyFactory.generatePublic(keySpec);

            Signature sig = Signature.getInstance("SHA256withRSA");
            sig.initVerify(pubKey);
            sig.update(payload.getBytes(StandardCharsets.UTF_8));

            byte[] signatureBytes = Base64.getDecoder().decode(signature);
            return sig.verify(signatureBytes);
        } catch (Exception e) {
            return false;
        }
    }

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
