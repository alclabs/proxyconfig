package com.alcshare.proxyconfig.util;

import org.apache.commons.codec.binary.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

/**
 *  AES Encrypt / Decrypt based on the example at the blog
 *  http://techie-experience.blogspot.com/2012/10/encryption-and-decryption-using-aes.html
 */
public class AESUtil
{
    private static byte[] key = {
                0x74, 0x07, 0x03, 0x14, 0x15, 0x09, 0x26, 0x53, 0x58, 0x09, 0x79, 0x32, 0x38, 0x53, 0x2c, 0x6f
        };

        public static String encrypt(String strToEncrypt)
        {
            try
            {
                Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
                final SecretKeySpec secretKey = new SecretKeySpec(key, "AES");
                cipher.init(Cipher.ENCRYPT_MODE, secretKey);
                final String encryptedString = Base64.encodeBase64String(cipher.doFinal(strToEncrypt.getBytes()));
                return encryptedString;
            } catch (Exception e)
            {
                Logging.println("Error while encrypting", e);
                throw new RuntimeException("Unexpected exception during encryption", e);
            }
        }

        public static String decrypt(String strToDecrypt)
        {
            try
            {
                Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5PADDING");
                final SecretKeySpec secretKey = new SecretKeySpec(key, "AES");
                cipher.init(Cipher.DECRYPT_MODE, secretKey);
                final String decryptedString = new String(cipher.doFinal(Base64.decodeBase64(strToDecrypt)));
                return decryptedString;
            }
            catch (Exception e)
            {
                Logging.println("Error while decrypting", e);
                throw new RuntimeException("Unexpected exception during decryption", e);
            }
        }

}
