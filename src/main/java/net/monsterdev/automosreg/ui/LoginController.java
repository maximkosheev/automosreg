package net.monsterdev.automosreg.ui;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.ListView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import net.monsterdev.automosreg.events.LoginEvent;
import net.monsterdev.automosreg.http.tasks.LoginTask;
import net.monsterdev.automosreg.model.CertificateInfo;
import net.monsterdev.automosreg.domain.User;
import net.monsterdev.automosreg.services.CryptoService;
import net.monsterdev.automosreg.services.UserService;
import net.monsterdev.automosreg.ui.control.PasswordDialog;
import net.monsterdev.automosreg.ui.control.WaitIndicator;
import net.monsterdev.automosreg.utils.SpringFXMLLoader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Controller;

import java.net.URL;
import java.security.UnrecoverableKeyException;
import java.util.Objects;
import java.util.Optional;
import java.util.ResourceBundle;

@Controller
public class LoginController extends AbstractUIController implements WindowController {
    @FXML
    private StackPane rootPane;
    @FXML
    private GridPane wrapPane;
    @FXML
    ListView<User> lstUsers;
    @FXML
    Hyperlink lnkRegister;
    @FXML
    Button btnLogin;
    @FXML
    Button btnCancel;
    private WaitIndicator waitIndicator;

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private UserService userService;
    @Autowired
    private CryptoService cryptoService;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        UIController.primaryStage().setTitle("AutoMosreg - Вход");
        lstUsers.getItems().addAll(userService.findAll());

        btnCancel.setOnAction(event -> {
            UIController.primaryStage().fireEvent(new WindowEvent(UIController.primaryStage(), WindowEvent.WINDOW_CLOSE_REQUEST));
        });
        getStage().addEventHandler(LoginEvent.LOGIN_SUCCESS, event -> {
            userService.setCurrentUser(lstUsers.getSelectionModel().getSelectedItem());
            MainController.showUI();
        });
        getStage().addEventHandler(LoginEvent.LOGIN_FAILED, event -> {
            releaseUI();
            UIController.showErrorMessage(event.getMessage());
        });
        waitIndicator = new WaitIndicator();
    }

    @Override
    public Stage getStage() {
        return UIController.primaryStage();
    }

    public static void showUI() {
        try {
            LoginController controller = (LoginController) SpringFXMLLoader.load("/net/monsterdev/automosreg/ui/login.fxml");
            UIController.primaryStage().setTitle("AutoMosreg - Вход");
            Scene loginScene = new Scene((Parent) controller.getView(), 600, 400);
            UIController.primaryStage().setScene(loginScene);
            UIController.primaryStage().show();
        } catch (Exception e) {
            UIController.showErrorMessage("Ошибка открытия открытия окна входа\n" +
                    "Переустановка приложения может решить проблему");
        }
    }

    @FXML
    private void onRegister(ActionEvent event) {
        RegisterController.showUI();
    }

    private boolean loginToMarket(User user) {
        CertificateInfo certInfo = null;
        try {
            certInfo = Objects.requireNonNull(cryptoService.getCertificateByHash(user.getCertHash()),
                    "Сертификат не найден. " +
                            "Возможно контейнер закрытого ключа не установлен. " +
                            "Установите контейнер закрытого ключа и перезапустите приложение.");

            // Цикл до тех пор пока пользователь не введет корректный пароль для загрузки приватного ключа подписи
            while (certInfo.getPrivateKey() == null) {
                try {
                    certInfo.loadInfo();
                } catch (UnrecoverableKeyException ex) {
                    PasswordDialog dlg = new PasswordDialog();
                    dlg.setHeaderText("Введите пароль для контейнера: " + certInfo.getAlias());
                    Optional<String> result = dlg.showAndWait();
                    if (result.isPresent())
                        certInfo.setPassword(result.get());
                }
            }
            LoginTask loginTask = new LoginTask(certInfo);
            loginTask.setOnFailed(event -> {
                releaseUI();
                UIController.showErrorMessage(loginTask.getException().getMessage());
            });
            loginTask.setOnSucceeded(event -> {
                userService.setCurrentUser(user);
                MainController.showUI();
            });
            lockUI();
            Thread loginThread = new Thread(loginTask);
            applicationContext.getAutowireCapableBeanFactory().autowireBean(loginTask);
            loginThread.start();
        } catch (Exception ex) {
            ex.printStackTrace();
            UIController.showErrorMessage(ex.getMessage());
            return false;
        }
        return true;
    }

    @FXML
    private void onLogin(ActionEvent event) {
        User selectedUser = lstUsers.getSelectionModel().getSelectedItem();
        if (selectedUser == null) {
            UIController.showErrorMessage("Необходимо выбрать пользователя из списка");
            return;
        }
        loginToMarket(selectedUser);
    }

    private void lockUI() {
        wrapPane.setDisable(true);
        rootPane.getChildren().add(waitIndicator);
    }

    private void releaseUI() {
        rootPane.getChildren().remove(waitIndicator);
        wrapPane.setDisable(false);
    }
}
