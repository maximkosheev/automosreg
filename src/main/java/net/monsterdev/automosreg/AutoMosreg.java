package net.monsterdev.automosreg;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;
import net.monsterdev.automosreg.ui.LoginController;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.h2.tools.Server;

import java.net.URL;
import java.sql.SQLException;

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

  public static URL getResource(String name) {
    return instance.getClass().getResource(name);
  }

  public static int work_thread_timeout;

  public static void main(String[] args) {
    if (args.length > 0)
      work_thread_timeout = Integer.parseInt(args[0]);
    else
      work_thread_timeout = 60000;
    //System.setProperty("javax.net.ssl.trustStore","c:\\Program Files\\Java\\jdk1.8.0_191\\jre\\lib\\security\\");
    //System.setProperty("javax.net.ssl.trustStorePassword","changeit");
    //Security.setProperty("ssl.KeyManagerFactory.algorithm", "GostX509");
    //Security.setProperty("ssl.TrustManagerFactory.algorithm", "GostX509");
    //Security.setProperty("ssl.SocketFactory.provider", "ru.CryptoPro.ssl.SSLSocketFactoryImpl");
    //Security.setProperty("ssl.ServerSocketFactory.provider", "ru.CryptoPro.ssl.SSLServerSocketFactoryImpl");
    System.setProperty("com.sun.security.enableCRLDP", "true");
    System.setProperty("com.ibm.security.enableCRLDP", "true");

    try {
      LOG.trace("Starting database...");
      db = Server.createTcpServer("-tcpAllowOthers", "-tcpPort", "9092", "-trace").start();
      LOG.trace("Database started at: " + db.getURL());
      LOG.trace("Stating application");
      LOG.trace("Starting with trades update interval {}", work_thread_timeout);
      launch(args);
    } catch (SQLException ex) {
      ex.printStackTrace();
    }
  }
}
