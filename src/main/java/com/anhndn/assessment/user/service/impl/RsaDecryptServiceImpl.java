package com.anhndn.assessment.user.service.impl;

import com.anhndn.assessment.user.config.RsaConfiguration;
import com.anhndn.assessment.user.exception.RsaDecryptException;
import com.anhndn.assessment.user.service.RsaDecryptService;
import org.apache.commons.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import java.nio.charset.StandardCharsets;

@Service
public class RsaDecryptServiceImpl implements RsaDecryptService {

    @Autowired
    private RsaConfiguration rsaConfiguration;

    public String decrypt(String encryptedText) {
        if (encryptedText == null || encryptedText.isEmpty())
            return encryptedText;
        try {
            byte[] bytes = Base64.decodeBase64(encryptedText);
            Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            cipher.init(Cipher.DECRYPT_MODE, rsaConfiguration.getPrivateKey());
            byte[] decryptedText = cipher.doFinal(bytes);
            return new String(decryptedText, StandardCharsets.UTF_8);
        } catch (Exception ex) {
            throw new RsaDecryptException(ex);
        }
    }
}
