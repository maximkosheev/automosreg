package net.monsterdev.automosreg.utils;

import java.nio.charset.Charset;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.CRC32;
import java.util.zip.Checksum;
import lombok.NonNull;
import net.monsterdev.automosreg.exceptions.AutoMosregException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LicenseUtil {
  private static final Logger LOG = LoggerFactory.getLogger(LicenseUtil.class);

  private static int usersCount = 1;

  private static boolean validateChecksum(String data, long crc32) {
    byte[] bytes = data.getBytes(Charset.forName("UTF-8"));
    Checksum checksum = new CRC32();
    checksum.update(bytes, 0, bytes.length);
    long value = checksum.getValue();
    return value == crc32;
  }
  /**
   * Выполняет загрузку информации о лицензии
   *
   * @param licenseData строка, содержащая данные о лицензии
   */
  public static void load(@NonNull String licenseData) throws AutoMosregException {
    try {
      Pattern dataPattern = Pattern.compile("^usercount=(\\d+);crc=(\\d+)$");
      Matcher matcher = dataPattern.matcher(licenseData);
      if (!matcher.find())
        throw new AutoMosregException("Ошибка чтения лицензионной информации, возможно файл лицензии поврежден");
      int count = Integer.parseInt(matcher.group(1));
      long crc = Long.parseLong(matcher.group(2));
      if (!validateChecksum(String.format("usercount=%d", count), crc))
        throw new AutoMosregException("Ошибка чтения лицензионной информации, возможно файл лицензии поврежден");
      LicenseUtil.usersCount = count;
    } catch (Exception ex) {
      LOG.error("Ошибка чтения лицензионной информации: ", ex);
      throw new AutoMosregException("Ошибка чтения лицензионной информации, возможно файл лицензии поврежден");
    }
  }

  /**
   * Выполняет проверку ограничения лицензии на кол-во зарегистрированных пользователей
   *
   * @param usersCount текущее число пользователей
   * @return истина, если ограничение не нарушено и ложь в противном случае
   */
  public static boolean check(long usersCount) {
    return LicenseUtil.usersCount >= usersCount;
  }
}
