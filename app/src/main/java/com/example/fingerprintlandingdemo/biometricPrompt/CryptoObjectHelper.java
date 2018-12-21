package com.example.fingerprintlandingdemo.biometricPrompt;

import android.hardware.biometrics.BiometricPrompt;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Build;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyPermanentlyInvalidatedException;
import android.security.keystore.KeyProperties;
import android.support.annotation.RequiresApi;
import java.security.Key;
import java.security.KeyStore;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.spec.IvParameterSpec;

/**
 * Created by yudoudou on 2018/12/21.
 */
@RequiresApi(api = Build.VERSION_CODES.P)
public class CryptoObjectHelper {
    static final String KEYSTORE_NAME = "AndroidKeyStore";
    static final String KEY_ALGORITHM = KeyProperties.KEY_ALGORITHM_AES;
    static final String BLOCK_MODE = KeyProperties.BLOCK_MODE_CBC;
    static final String ENCRYPTION_PADDING = KeyProperties.ENCRYPTION_PADDING_PKCS7;
    static final String TRANSFORMATION = KEY_ALGORITHM + "/" +
            BLOCK_MODE + "/" +
            ENCRYPTION_PADDING;
    final KeyStore _keystore;

    public CryptoObjectHelper() throws Exception {
        _keystore = KeyStore.getInstance(KEYSTORE_NAME);
        _keystore.load(null);
    }

    public FingerprintManager.CryptoObject buildCryptoObject(int purposeEncrypt, byte[] iv) throws Exception {
        Cipher cipher = createCipher(purposeEncrypt,iv);
        return new FingerprintManager.CryptoObject(cipher);
    }
    public BiometricPrompt.CryptoObject buildCryptoObject(byte[] iv,int purposeEncrypt) throws Exception {
        Cipher cipher = createCipher(purposeEncrypt,iv);
        return new BiometricPrompt.CryptoObject(cipher);
    }

    public Cipher createCipher(int purpose, byte[] iv) throws Exception {
        Key key = GetKey();
        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        try {
            if (purpose == KeyProperties.PURPOSE_ENCRYPT) {
                cipher.init(purpose, key);
            } else {
                cipher.init(purpose, key, new IvParameterSpec(iv));
            }
        } catch (KeyPermanentlyInvalidatedException e) {
            _keystore.deleteEntry(Constant.SE_KEY_NAME);
        }
        return cipher;
    }

    Key GetKey() throws Exception {
        Key secretKey;
        if (!_keystore.isKeyEntry(Constant.SE_KEY_NAME)) {
            CreateKey();
        }

        secretKey = _keystore.getKey(Constant.SE_KEY_NAME, null);
        return secretKey;
    }

    void CreateKey() throws Exception {
        KeyGenerator keyGen = KeyGenerator.getInstance(KEY_ALGORITHM, KEYSTORE_NAME);
        KeyGenParameterSpec keyGenSpec =
                new KeyGenParameterSpec.Builder(Constant.SE_KEY_NAME, KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                        .setBlockModes(BLOCK_MODE)
                        .setEncryptionPaddings(ENCRYPTION_PADDING)
                        .setUserAuthenticationRequired(true)
                        .build();
        keyGen.init(keyGenSpec);
        keyGen.generateKey();
    }
}
