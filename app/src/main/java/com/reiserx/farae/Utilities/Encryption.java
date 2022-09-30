package com.reiserx.farae.Utilities;

import static android.content.ContentValues.TAG;

import android.annotation.SuppressLint;
import android.util.Log;

import java.nio.charset.StandardCharsets;

public class Encryption {
    public String data, DecryptedString;

    public String Encrypt(final String datas, final String keys) {
        try {
            javax.crypto.SecretKey key = generateKey(keys);
            @SuppressLint("GetInstance") javax.crypto.Cipher c = javax.crypto.Cipher.getInstance("AES");
            c.init(javax.crypto.Cipher.ENCRYPT_MODE, key);
            byte[] encVal = c.doFinal(datas.getBytes());
            data = android.util.Base64.encodeToString(encVal, android.util.Base64.DEFAULT);
        } catch (Exception ex) {
            Log.d(TAG, String.valueOf(ex));
        }
        return data;
    }

    public String Decrypt (final String data, final String Decryptionkey) {
        try {
            javax.crypto.spec.SecretKeySpec key = (javax.crypto.spec.SecretKeySpec) generateKey(Decryptionkey);
            javax.crypto.Cipher c = javax.crypto.Cipher.getInstance("AES");
            c.init(javax.crypto.Cipher.DECRYPT_MODE, key);
            byte[] decode = android.util.Base64.decode(data, android.util.Base64.DEFAULT);
            byte[] decval = c.doFinal(decode);
            DecryptedString = new String(decval);
        } catch (Exception ex) {
            DecryptedString = null;
            Log.d("hffhsb", ex.getMessage());
        }
        return DecryptedString;
    }

    private javax.crypto.SecretKey generateKey(String pwd) throws Exception {
        final java.security.MessageDigest digest = java.security.MessageDigest.getInstance("SHA-256");
        byte[] b = pwd.getBytes(StandardCharsets.UTF_8);
        digest.update(b, 0, b.length);
        byte[] key = digest.digest();
        javax.crypto.spec.SecretKeySpec sec = new javax.crypto.spec.SecretKeySpec(key, "AES");
        return sec;
    }
}
