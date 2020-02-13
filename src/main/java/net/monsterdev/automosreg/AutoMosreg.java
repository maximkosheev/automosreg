package net.monsterdev.automosreg;

import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.zip.CRC32;
import java.util.zip.Checksum;
import javafx.application.Application;
import javafx.stage.Stage;
import net.monsterdev.automosreg.exceptions.AutoMosregException;
import net.monsterdev.automosreg.ui.LoginController;
import net.monsterdev.automosreg.utils.CipherUtil;
import net.monsterdev.automosreg.utils.LicenseUtil;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.h2.tools.Server;

public class AutoMosreg extends Application {

  private static final Logger LOG = LogManager.getLogger(AutoMosreg.class);

  public static Stage primaryStage = null;
  private static AutoMosreg instance = null;
  private static Server db;

  @Override
  public void start(Stage primaryStage) throws Exception {
    AutoMosreg.primaryStage = primaryStage;
    AutoMosreg.instance = this;
    LoginController.showUI();
    AutoMosreg.primaryStage.setOnCloseRequest(event -> {
      // TODO: Остановить поток UpdateProposalsThread
      db.stop();
      System.exit(0);
    });
  }

  public static AutoMosreg getInstance() {
    return instance;
  }

  public static URL getResource(String name) {
    return instance.getClass().getResource(name);
  }

  public static int work_thread_timeout;

  public static void main(String[] args) throws NoSuchAlgorithmException {

    //System.setProperty("javax.net.ssl.trustStore","c:\\Program Files\\Java\\jdk1.8.0_191\\jre\\lib\\security\\");
    //System.setProperty("javax.net.ssl.trustStorePassword","changeit");
    //Security.setProperty("ssl.KeyManagerFactory.algorithm", "GostX509");
    //Security.setProperty("ssl.TrustManagerFactory.algorithm", "GostX509");
    //Security.setProperty("ssl.SocketFactory.provider", "ru.CryptoPro.ssl.SSLSocketFactoryImpl");
    //Security.setProperty("ssl.ServerSocketFactory.provider", "ru.CryptoPro.ssl.SSLServerSocketFactoryImpl");
    System.setProperty("com.sun.security.enableCRLDP", "true");
    System.setProperty("com.ibm.security.enableCRLDP", "true");

    Options options = new Options();
    options.addOption(Option.builder()
        .longOpt("sleep")
        .hasArg()
        .argName("SLEEP")
        .build());
    options.addOption(Option.builder()
        .longOpt("license")
        .build());
    options.addOption(Option.builder()
        .longOpt("limit")
        .hasArg()
        .argName("COUNT")
        .build());
    CommandLineParser parser = new DefaultParser();
    try {
      CommandLine line = parser.parse(options, args);
      work_thread_timeout = Integer.parseInt(line.getOptionValue("sleep", "6000"));
      // нужно сгенерировать лицензионный ключ (максимальное кол-во зарегистрированных аккаунтов указывается в параметре limit)
      if (line.hasOption("license")) {
        Integer limit = Integer.parseInt(line.getOptionValue("limit", "1"));
        // формируем информацию о лицензии, с указанием кол-ва аккаунтов
        byte[] licenseInfo = String.format("usercount=%d", limit).getBytes(Charset.forName("UTF-8"));
        Checksum checksum = new CRC32();
        checksum.update(licenseInfo, 0, licenseInfo.length);
        // добавляем к информации о лицензии контрольную сумму
        String licenseData = String.format("usercount=%d;crc=%d", limit, checksum.getValue());
        // формируем файл лицензии при этом зашифровываем информацию о лицензии
        FileOutputStream fos = new FileOutputStream("license.key");
        fos.write(CipherUtil.encrypt(licenseData.getBytes(Charset.forName("UTF-8"))));
        fos.close();
      }
      LOG.trace("Reading license data from file license.key");
      Path licenseFilePath = Paths.get(System.getenv("APP_HOME")).resolve("bin").resolve("license.key");
      String licenseData = new String(CipherUtil.decrypt(Files.readAllBytes(licenseFilePath)), StandardCharsets.UTF_8);
      LicenseUtil.load(licenseData);
      LOG.trace("Starting database...");
      db = Server.createTcpServer("-tcpAllowOthers", "-tcpPort", "9092", "-trace").start();
      LOG.trace("Database started at: " + db.getURL());
      LOG.trace("Stating application");
      LOG.trace("Starting with trades update interval {}", work_thread_timeout);
      launch(args);
    } catch (ParseException | IOException | SQLException | AutoMosregException ex) {
      if (db != null)
        db.stop();
      ex.printStackTrace();
    }
  }
}
