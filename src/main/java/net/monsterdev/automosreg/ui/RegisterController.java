package net.monsterdev.automosreg.ui;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import net.monsterdev.automosreg.model.CertificateInfo;
import net.monsterdev.automosreg.domain.Document;
import net.monsterdev.automosreg.domain.User;
import net.monsterdev.automosreg.services.UserService;
import net.monsterdev.automosreg.ui.control.DocumentListCell;
import net.monsterdev.automosreg.utils.LincenceUtil;
import net.monsterdev.automosreg.utils.SpringFXMLLoader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

@Controller
public class RegisterController extends AbstractUIController implements WindowController {
    @Autowired
    private UserService userService;

    @FXML
    TextField edtName;
    @FXML
    TextField edtCertificate;
    @FXML
    TextField edtEmail;
    @FXML
    TextField edtFax;
    @FXML
    TextField edtPhone;
    @FXML
    TextField edtSurname;
    @FXML
    TextField edtFatherName;
    @FXML
    TextField edtFirstName;
    @FXML
    CheckBox chbNDS;
    @FXML
    ComboBox<Integer> cmbNDS;
    @FXML
    ListView<Document> lstDocuments;

    private CertificateInfo certificateInfo = null;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        UIController.primaryStage().setTitle("AutoMosreg - Регистрация пользователя");
        cmbNDS.getItems().setAll(0, 6, 10, 18, 20);
        lstDocuments.setCellFactory(param -> new DocumentListCell());
    }

    @Override
    public Stage getStage() {
        return UIController.primaryStage();
    }

    static void showUI() {
        try {
            RegisterController controller = (RegisterController)SpringFXMLLoader.load("/net/monsterdev/automosreg/ui/register.fxml");
            Scene registerScene = new Scene((Parent)controller.getView(), 600, 645);
            UIController.primaryStage().setScene(registerScene);
            UIController.primaryStage().show();
        } catch (IOException e) {
            UIController.showErrorMessage("Ошибка открытия открытия окна регистрации\n" +
                    "Переустановка приложения может решить проблему");
            e.printStackTrace();
        }
    }

    private void gotoLogin() {
        LoginController.showUI();
    }

    @FXML
    private void onCancel(ActionEvent event) {
        gotoLogin();
    }

    @FXML
    private void onAddDocument(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("AutoMosreg - Выбор документа");
        fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Все файлы", "*.*"));

        File file = fileChooser.showOpenDialog(getStage());

        lstDocuments.getItems().add(new Document(){{
            setName(file.toPath().getFileName().toString());
            setPath(file.toPath().toString());
        }});
    }

    @FXML
    private void onRemoveDocument(ActionEvent event) {
        int index = lstDocuments.getSelectionModel().getSelectedIndex();

        if (index < 0) {
            UIController.showErrorMessage("Выбере документ, который нужно удалить");
            return;
        }
        lstDocuments.getItems().remove(index);
    }

    @FXML
    private void onSelectCert(ActionEvent event) {
        Optional<CertificateInfo> result = CertificateListController.showUI();
        result.ifPresent(certificate -> {
            this.certificateInfo = certificate;
            edtCertificate.setText(certificate.getName());
        });
    }

    /**
     * Проверяет правильность заполнение полей формы
     * @return true, если заполнено все правильно, false - в противном случае
     */
    private boolean validate() {
        if (edtName.getText().isEmpty()) {
            UIController.showErrorMessage("Поле 'Наименование' не может быть пустым");
            return false;
        }
        if (certificateInfo == null) {
            UIController.showErrorMessage("Поле 'Сертификат' не может быть пустым");
            return false;
        }
        if (edtEmail.getText().isEmpty()) {
            UIController.showErrorMessage("Поле 'Email' не может быть пустым");
            return false;
        }
        if (edtPhone.getText().isEmpty()) {
            UIController.showErrorMessage("Поле 'Телефон' не может быть пустым");
            return false;
        }
        if (edtSurname.getText().isEmpty() || edtFatherName.getText().isEmpty() || edtFirstName.getText().isEmpty()) {
            UIController.showErrorMessage("Поля 'Фамилия', 'Имя', 'Отчество' не могут быть пустыми");
            return false;
        }
        return true;
    }

    @FXML
    private void onRegister(ActionEvent event) {
        if (!LincenceUtil.check(userService.getCount() + 1)) {
            UIController.showErrorMessage("Вы превысили ограничение по количеству пользователей,\n" +
                    "зарегистрированных в системе, установленное вашей лицензией.\n" +
                    "Обратитесь к диллеру для получения соответсвующей лицензии");
            return;
        }
        if (validate()) {
            User newUser = new User();
            newUser.setName(edtName.getText());
            newUser.setCertName(certificateInfo.getName());
            newUser.setCertHash(certificateInfo.getHash());
            newUser.setSurname(edtSurname.getText());
            newUser.setFirstName(edtFirstName.getText());
            newUser.setFatherName(edtFatherName.getText());
            newUser.setEmail(edtEmail.getText());
            newUser.setFax(edtFax.getText());
            newUser.setPhone(edtPhone.getText());
            newUser.setUseNDS(chbNDS.isSelected());
            newUser.setNDS(cmbNDS.getValue() != null ? cmbNDS.getValue() : 0);
            newUser.getDocuments().addAll(lstDocuments.getItems());
            userService.register(newUser);
            gotoLogin();
        }
    }

}
