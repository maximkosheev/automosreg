package net.monsterdev.automosreg.ui;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import net.monsterdev.automosreg.model.CertificateInfo;
import net.monsterdev.automosreg.services.CryptoService;
import net.monsterdev.automosreg.utils.SpringFXMLLoader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import java.io.IOException;
import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

@Controller
public class CertificateListController extends AbstractUIController implements WindowController {
    @FXML
    Pane rootPane;
    @FXML
    TableView<CertificateInfo> tblCertificates;
    // текущий выбранный сертификат
    private CertificateInfo certificateInfo = null;

    @Autowired
    private CryptoService cryptoService;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        TableColumn<CertificateInfo, String> c1 = new TableColumn<>("Название");
        TableColumn<CertificateInfo, String> c2 = new TableColumn<>("Срок действия");
        c1.setCellValueFactory(new PropertyValueFactory<>("name"));
        c2.setCellValueFactory(new PropertyValueFactory<>("validity"));
        c1.prefWidthProperty().bind(tblCertificates.widthProperty().multiply(0.5));
        c2.prefWidthProperty().bind(tblCertificates.widthProperty().multiply(0.5));
        tblCertificates.getColumns().setAll(c1, c2);
        tblCertificates.getItems().setAll(cryptoService.getCertificatesList());
    }

    @Override
    public Stage getStage() {
        return (Stage) rootPane.getScene().getWindow();
    }

    private CertificateInfo getCertificateInfo() {
        return certificateInfo;
    }

    static Optional<CertificateInfo> showUI() {
        try {
            CertificateListController controller = (CertificateListController) SpringFXMLLoader.load("/net/monsterdev/automosreg/ui/certificates.fxml");
            Stage stage = new Stage();
            stage.setScene(new Scene((Parent)controller.getView(), 450, 355));
            stage.setTitle("AutoMosreg - Выбор сертификата");
            stage.showAndWait();
            return Optional.ofNullable(controller.getCertificateInfo());
        } catch (IOException e) {
            UIController.showErrorMessage("Ошибка открытия открытия окна выбора сетификата\n" +
                    "Переустановка приложения может решить проблему");
            e.printStackTrace();
            return Optional.empty();
        }
    }

    @FXML
    private void onOk(ActionEvent event) {
        certificateInfo = tblCertificates.getSelectionModel().getSelectedItem();
        if (certificateInfo == null) {
            UIController.showErrorMessage("Необходимо выбрать сертификат");
            return;
        }
        getStage().fireEvent(new WindowEvent(getStage(), WindowEvent.WINDOW_CLOSE_REQUEST));
    }
}
