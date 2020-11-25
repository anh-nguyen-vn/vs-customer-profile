package com.anhndn.assessment.user.service;

import com.anhndn.assessment.user.config.RsaConfiguration;
import com.anhndn.assessment.user.exception.RsaDecryptException;
import com.anhndn.assessment.user.service.impl.RsaDecryptServiceImpl;
import org.apache.commons.codec.binary.Base64;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.util.io.pem.PemObject;
import org.bouncycastle.util.io.pem.PemReader;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import javax.crypto.Cipher;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Security;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class RsaDecryptServiceTest {

    @InjectMocks
    private RsaDecryptServiceImpl rsaDecryptService;

    @Mock
    private RsaConfiguration rsaConfigurationMock;

    private String testRsaPrivateKey;
    private String testRsaPublicKey;

    @Before
    public void setup() {
        testRsaPrivateKey = "-----BEGIN RSA PRIVATE KEY-----\n" +
                "MIICXQIBAAKBgQCsfOnDzDh8d7ZvlMnXZaZhVZ5hrU85VhXCAGQNokTVw6NvHbv0\n" +
                "sOJF6ttehfsDlokpAy6SMsfuOK2pXVzV6Y4PcFgl8CO5foxv0Xw0BjP8Le6NhDSF\n" +
                "fJGzzLNxBiNDtSuE+GSRzggyF/a8YyJP2xOTBEM4Owdoyv4YmjBnl5L0xwIDAQAB\n" +
                "AoGBAKjzXYIUiguYstDOm3npLjROyekA+gW+RWeWPGqCVAxSKcaQCGefzrMPXTpT\n" +
                "38/e5pCOdlJrfRvg1nF7apB4yVTAlGUWjqTr3uFSKKZVjoXqpqOgAeBAqkRJcTsk\n" +
                "0RBysjqO/+5yEKK+8l0Ofsg+39bEX3BBwZg8MVMhudJ8g+xJAkEA/yh9zEQ55NXJ\n" +
                "vqffkWRcdY9GDN3YxRVrF9P7kJTBv/46+29X6LohOQgui/mQFplwuPX9vyDqm6mx\n" +
                "IIUFLOflawJBAK0OmQq2x4FrZh1BDE8dACwyWXw4yrAtbQ7eD4nSngCKOUcQgZBI\n" +
                "9kjgPLrJDwaeBvOmMI2biPUOIX14XpMtKRUCQAK2sofOnfMCFxAxBt6r+5PAf1U5\n" +
                "ssl9zdLGDWHfQyRAlu3/pCa0fA/4N06Dy/WBkkJVU2qJ9hTLvDeFUqXEnZsCQBLg\n" +
                "ALk2blQjTqPqMFmApEAtzazK1PCaQ8bXWYKCwlD0woKJvlfqXVJdgsIso8LpAYEZ\n" +
                "ozoOuMVhoS16L3aF+nECQQCeHuJQDosOxI4idJxgA9LrctP4gtJCksGqgTa4Pkyi\n" +
                "WCeF9ljDkZ8OtgP9X2veEDfm+UwoctHT0a5PPZ+rFdIP\n" +
                "-----END RSA PRIVATE KEY-----";

        testRsaPublicKey = "-----BEGIN PUBLIC KEY-----\n" +
                "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCsfOnDzDh8d7ZvlMnXZaZhVZ5\n" +
                "hrU85VhXCAGQNokTVw6NvHbv0sOJF6ttehfsDlokpAy6SMsfuOK2pXVzV6Y4PcF\n" +
                "gl8CO5foxv0Xw0BjP8Le6NhDSFfJGzzLNxBiNDtSuE+GSRzggyF/a8YyJP2xOTB\n" +
                "EM4Owdoyv4YmjBnl5L0xwIDAQAB\n" +
                "-----END PUBLIC KEY-----";
    }


    @Test
    public void test__decrypt__success__returnPlaintext() throws Exception {
        PrivateKey privateKey = this.readPrivateKey();
        when(rsaConfigurationMock.getPrivateKey()).thenReturn(privateKey);

        String text = "any text";
        String encryptedText = this.encrypt(text);

        String plainText = rsaDecryptService.decrypt(encryptedText);

        assertEquals(text, plainText);
    }

    @Test
    public void test__decrypt__success__whenTestIsEmpty__returnEmpty() {
        String text = "";

        String plainText = rsaDecryptService.decrypt(text);

        assertEquals(text, plainText);
    }

    @Test
    public void test__decrypt__success__whenTestIsNull__returnNull() {
        String plainText = rsaDecryptService.decrypt(null);

        assertNull(plainText);
    }

    @Test(expected = RsaDecryptException.class)
    public void test__decrypt__error__whenGotAnyException__throwsRSADecryptException() throws Exception {
        String text = "password";
        String encryptedText = this.encrypt(text);

        rsaDecryptService.decrypt(encryptedText);

    }

    public String encrypt(String message) throws Exception {
        byte[] publicBytes = readPemObject(testRsaPublicKey).getContent();
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(publicBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PublicKey pubKey = keyFactory.generatePublic(keySpec);
        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        cipher.init(Cipher.ENCRYPT_MODE, pubKey);
        return Base64.encodeBase64String(cipher.doFinal(message.getBytes(StandardCharsets.UTF_8)));
    }

    private PrivateKey readPrivateKey() throws NoSuchAlgorithmException, IOException, NoSuchProviderException, InvalidKeySpecException {
        if (Security.getProvider("BC") == null)
            Security.addProvider(new BouncyCastleProvider());
        byte[] keyBytes = readPemObject(testRsaPrivateKey).getContent();
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory kf = KeyFactory.getInstance("RSA", "BC");
        return kf.generatePrivate(spec);
    }

    private PemObject readPemObject(String data) throws IOException {
        PemReader pemReader = new PemReader(new InputStreamReader(new ByteArrayInputStream(data.getBytes(StandardCharsets.UTF_8))));
        try {
            return pemReader.readPemObject();
        } finally {
            pemReader.close();
        }
    }
}