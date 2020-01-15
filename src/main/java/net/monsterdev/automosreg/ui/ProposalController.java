package net.monsterdev.automosreg.ui;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import net.monsterdev.automosreg.model.ProposalData;
import net.monsterdev.automosreg.utils.SpringFXMLLoader;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;

@Controller
public class ProposalController extends AbstractUIController implements WindowController {

  @FXML
  private VBox rootPane;
  @FXML
  private TextField edtMinTradeVal;
  @FXML
  private TextField edtStartTradeVal;
  @FXML
  private TextField edtActivateTime;

  private boolean isOK = false;
  private ProposalData proposalData = new ProposalData();

  @Override
  public void initialize(URL location, ResourceBundle resources) {
  }

  @Override
  public Stage getStage() {
    return (Stage) rootPane.getScene().getWindow();
  }

  @FXML
  private void onOk(ActionEvent event) {
    if (!validate()) {
      return;
    }
    isOK = true;
    close();
  }

  @FXML
  private void onCancel(ActionEvent event) {
    isOK = false;
    close();
  }

  static Optional<ProposalData> showUI() {
    try {
      ProposalController controller = (ProposalController) SpringFXMLLoader
          .load("/net/monsterdev/automosreg/ui/proposal_data.fxml");
      Stage stage = new Stage();
      stage.setScene(new Scene((Parent) controller.getView(), 400, 245));
      stage.setTitle("AutoMosreg - Данные для заявок");
      stage.showAndWait();
      if (controller.isOK) {
        controller.proposalData.setMinTradeVal(new BigDecimal(controller.edtMinTradeVal.getText()));
        controller.proposalData.setStartTradeVal(StringUtils.isNumeric(controller.edtStartTradeVal.getText()) ?
            new BigDecimal(controller.edtStartTradeVal.getText()) : null);
        long activateTime = Long.parseLong(controller.edtActivateTime.getText());
        if (activateTime == 0) {
          activateTime = Long.MAX_VALUE;
        } else
        // переводим минуты в милисекунды
        {
          activateTime = activateTime * 60 * 1000;
        }
        controller.proposalData.setActivateTime(activateTime);
        return Optional.of(controller.proposalData);
      } else {
        return Optional.empty();
      }
    } catch (IOException e) {
      UIController.showErrorMessage("Ошибка открытия открытия окна фильтра закупок\n" +
          "Переустановка приложения может решить проблему");
      e.printStackTrace();
      return Optional.empty();
    }
  }

  private boolean validate() {
    if (edtMinTradeVal.getText().isEmpty()) {
      UIController.showErrorMessage("Поле 'Минимальное предложение цены (МПЦ)' не может быть пустым");
      return false;
    }
    if (!StringUtils.isNumeric(edtMinTradeVal.getText())) {
      UIController.showErrorMessage("Поле 'Минимальное предложение цены (МПЦ)' должно быть числовым значением");
      return false;
    }
    if (!StringUtils.isEmpty(edtStartTradeVal.getText()) && !StringUtils.isNumeric(edtStartTradeVal.getText())) {
        UIController.showErrorMessage("Поле 'Начальное предложение цены' должно быть числовым значением");
        return false;
    }
    if (edtActivateTime.getText().isEmpty()) {
      edtActivateTime.setText("0");
    }
    if (!StringUtils.isNumeric(edtActivateTime.getText())) {
      UIController.showErrorMessage("Поле 'Время активации' должно быть числовым значением");
      return false;
    }
    return true;
  }
}
