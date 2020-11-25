package com.anhndn.assessment.user.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;

@Configuration
@Slf4j
public class RsaConfiguration {

    @Value("${rsa.private.key}")
    private String rsaPrivateKey;

    private PrivateKey privateKey;

    public PrivateKey getPrivateKey() {
        return privateKey;
    }

    @PostConstruct
    private void readPrivateKey() throws InvalidKeySpecException, NoSuchAlgorithmException {
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(Base64.decodeBase64(rsaPrivateKey));
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        privateKey = keyFactory.generatePrivate(keySpec);
    }
}
