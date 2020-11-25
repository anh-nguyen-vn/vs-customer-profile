package com.anhndn.assessment.user.util;

import org.apache.commons.codec.binary.Base64;

import java.security.MessageDigest;

public class EncryptionUtil {

    private static final String SHA256 = "SHA-256";
    private static final String ENCODING_UTF8 = "UTF-8";

    public static String sha256(String data) {
        try{
            MessageDigest digest = MessageDigest.getInstance(SHA256);
            byte[] hash = digest.digest(data.getBytes(ENCODING_UTF8));
            return Base64.encodeBase64String(hash);
        } catch(Exception ex){
            throw new RuntimeException(ex);
        }
    }
}
