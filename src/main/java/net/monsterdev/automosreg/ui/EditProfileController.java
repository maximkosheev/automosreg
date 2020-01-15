package net.monsterdev.automosreg.ui;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import net.monsterdev.automosreg.domain.Document;
import net.monsterdev.automosreg.domain.User;
import net.monsterdev.automosreg.model.CertificateInfo;
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
public class EditProfileController extends AbstractUIController implements WindowController {
    @Autowired
    private UserService userService;

    @FXML
    GridPane rootPane;
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
        cmbNDS.getItems().setAll(0, 6, 10, 18, 20);
        lstDocuments.setCellFactory(param -> new DocumentListCell());

        User user = userService.getCurrentUser();

        edtName.setText(user.getName());
        edtCertificate.setText(user.getCertName());
        edtEmail.setText(user.getEmail());
        edtFax.setText(user.getFax());
        edtPhone.setText(user.getPhone());
        edtSurname.setText(user.getSurname());
        edtFatherName.setText(user.getFatherName());
        edtFirstName.setText(user.getFirstName());
        chbNDS.setSelected(user.isUseNDS());
        cmbNDS.getSelectionModel().select((Integer)user.getNDS());
        for (Document doc : user.getDocuments()) {
            lstDocuments.getItems().add(doc);
        }

        certificateInfo = new CertificateInfo();
        certificateInfo.setName(user.getCertName());
        certificateInfo.setHash(user.getCertHash());
    }

    @Override
    public Stage getStage() {
        return (Stage) rootPane.getScene().getWindow();
    }

    static void showUI() {
        try {
            EditProfileController controller = (EditProfileController) SpringFXMLLoader.load("/net/monsterdev/automosreg/ui/edit_profile.fxml");
            Stage stage = new Stage();
            stage.setScene(new Scene((Parent)controller.getView(), 600, 645));
            stage.setTitle("AutoMosreg - Редактирование профиля");
            stage.showAndWait();
        } catch (IOException e) {
            UIController.showErrorMessage("Ошибка открытия открытия окна редактирования профиля\n" +
                    "Переустановка приложения может решить проблему");
            e.printStackTrace();
        }
    }

    @FXML
    private void onCancel(ActionEvent event) {
        close();
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
    private void onUpdate(ActionEvent event) {
        if (!LincenceUtil.check(userService.getCount() + 1)) {
            UIController.showErrorMessage("Вы превысили ограничение по количеству пользователей,\n" +
                    "зарегистрированных в системе, установленное вашей лицензией.\n" +
                    "Обратитесь к диллеру для получения соответсвующей лицензии");
            return;
        }
        if (validate()) {
            userService.getCurrentUser().setName(edtName.getText());
            userService.getCurrentUser().setCertName(certificateInfo.getName());
            userService.getCurrentUser().setCertHash(certificateInfo.getHash());
            userService.getCurrentUser().setSurname(edtSurname.getText());
            userService.getCurrentUser().setFirstName(edtFirstName.getText());
            userService.getCurrentUser().setFatherName(edtFatherName.getText());
            userService.getCurrentUser().setEmail(edtEmail.getText());
            userService.getCurrentUser().setFax(edtFax.getText());
            userService.getCurrentUser().setPhone(edtPhone.getText());
            userService.getCurrentUser().setUseNDS(chbNDS.isSelected());
            userService.getCurrentUser().setNDS(cmbNDS.getValue() != null ? cmbNDS.getValue() : 0);
            userService.getCurrentUser().getDocuments().clear();
            userService.getCurrentUser().getDocuments().addAll(lstDocuments.getItems());
            userService.update();
            close();
        }
    }
}
