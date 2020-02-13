package net.monsterdev.automosreg.utils;

import java.nio.charset.StandardCharsets;
import java.security.spec.AlgorithmParameterSpec;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import net.monsterdev.automosreg.exceptions.AutoMosregException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CipherUtil {
  private static final Logger LOG = LoggerFactory.getLogger(CipherUtil.class);
  private static final String secretKey = "4ATxIA4lDqs=";
  private static final byte[] iv = { 11, 22, 33, 44, 99, 88, 77, 66 };

  public static byte[] encrypt(byte[] data) throws AutoMosregException {
    try {
      byte[] decodeKey = Base64.getDecoder().decode(secretKey.getBytes(StandardCharsets.UTF_8));
      SecretKey key = new SecretKeySpec(decodeKey, 0, decodeKey.length, "DES");
      AlgorithmParameterSpec paramSpec = new IvParameterSpec(iv);
      Cipher encryptCipher = Cipher.getInstance("DES/CBC/PKCS5Padding");
      encryptCipher.init(Cipher.ENCRYPT_MODE, key, paramSpec);
      return encryptCipher.doFinal(data);
    } catch (Exception ex) {
      LOG.error("Ошибка шифрования: ", ex);
      throw new AutoMosregException("Ошибка шифрования");
    }
  }

  public static byte[] decrypt(byte[] data) throws AutoMosregException {
    try {
      byte[] decodeKey = Base64.getDecoder().decode(secretKey.getBytes(StandardCharsets.UTF_8));
      SecretKey key = new SecretKeySpec(decodeKey, 0, decodeKey.length, "DES");
      AlgorithmParameterSpec paramSpec = new IvParameterSpec(iv);
      Cipher encryptCipher = Cipher.getInstance("DES/CBC/PKCS5Padding");
      encryptCipher.init(Cipher.DECRYPT_MODE, key, paramSpec);
      return encryptCipher.doFinal(data);
    } catch (Exception ex) {
      LOG.error("Ошибка расшифровки: ", ex);
      throw new AutoMosregException("Ошибка расшифровки");
    }
  }
}
