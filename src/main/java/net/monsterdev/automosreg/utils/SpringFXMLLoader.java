package net.monsterdev.automosreg.utils;

import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import net.monsterdev.automosreg.config.AppConfig;
import net.monsterdev.automosreg.ui.UIController;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.io.IOException;
import java.io.InputStream;

public class SpringFXMLLoader {
    private static final String ERROR_COMMON_LOG_MSG = "SpringXMLLoader error: %s : %s";
    private static Logger LOG = LogManager.getLogger(SpringFXMLLoader.class);
    private static final ApplicationContext APPLICATION_CONTEXT = new AnnotationConfigApplicationContext(AppConfig.class);

    public static UIController load(String url) throws IOException {
        try (InputStream fxmlStream = SpringFXMLLoader.class.getResourceAsStream(url)) {
            FXMLLoader loader = new FXMLLoader();
            loader.setControllerFactory(APPLICATION_CONTEXT::getBean);
            Node view = loader.load(fxmlStream);
            UIController controller = loader.getController();
            controller.setView(view);
            return controller;
        } catch (IOException e) {
            LOG.error(String.format(ERROR_COMMON_LOG_MSG, e.getClass(), e.getMessage()));
            throw e;
        }
    }
}
