package net.monsterdev.automosreg.ui;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.stream.Collectors;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.value.ChangeListener;
import javafx.concurrent.Worker;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.Pagination;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.Duration;
import net.monsterdev.automosreg.AutoMosreg;
import net.monsterdev.automosreg.domain.FilterOption;
import net.monsterdev.automosreg.domain.Trade;
import net.monsterdev.automosreg.enums.FilterType;
import net.monsterdev.automosreg.enums.TradeStatus;
import net.monsterdev.automosreg.exceptions.AutoMosregException;
import net.monsterdev.automosreg.model.ProposalData;
import net.monsterdev.automosreg.model.StatusFilterOption;
import net.monsterdev.automosreg.model.dto.TradeInfoDto;
import net.monsterdev.automosreg.repository.TradesRepository;
import net.monsterdev.automosreg.services.DictionaryService;
import net.monsterdev.automosreg.services.ProductsService;
import net.monsterdev.automosreg.services.TradeService;
import net.monsterdev.automosreg.services.UpdateTradesService;
import net.monsterdev.automosreg.services.UserService;
import net.monsterdev.automosreg.ui.control.WaitIndicator;
import net.monsterdev.automosreg.ui.model.TradeProposalItem;
import net.monsterdev.automosreg.utils.SpringFXMLLoader;
import netscape.javascript.JSObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;

@Controller
public class MainController extends AbstractUIController implements WindowController {

  private static final String ERROR_COMMON_LOG_MSG = "Failed to complete operation due error %s : %s";
  private static final String PENDING_MSG = "Ожидайте, идет обновление...";
  private static final String IDLE_MSG = "Простой";

  private static final Integer UPDATE_MAIN_VIEW_TICK = 60 * 1000;

  private final Logger LOG = LogManager.getLogger(MainController.class);

  @FXML
  private StackPane rootPane;
  @FXML
  private BorderPane wrapPane;
  @FXML
  private Label lblTradesCount;
  @FXML
  private DatePicker dateBeginFrom;
  @FXML
  private DatePicker dateBeginTo;
  @FXML
  private TextField edtTradeNum;
  @FXML
  private TextField edtTradeName;
  @FXML
  private ComboBox<StatusFilterOption> cmbStatus;
  @FXML
  private DatePicker dateFinishFrom;
  @FXML
  private DatePicker dateFinishTo;
  @FXML
  private ComboBox<FilterOption> cmbFilters;
  @FXML
  private WebView mainView;
  @FXML
  private Pagination pages;
  @FXML
  private ComboBox<Integer> cmbItemsPerPage;
  @FXML
  private Label lblStatusText;
  private WaitIndicator waitIndicator;

  @Autowired
  private UserService userService;
  @Autowired
  private DictionaryService dictionaryService;
  @Autowired
  private ProductsService productsService;
  @Autowired
  private UpdateTradesService updateTradesService;
  @Autowired
  private TradeService tradeService;
  @Autowired
  private TradesRepository tradesRepository;

  private WebEngine webEngine;

  private ChangeListener<Number> currentPageChangeListener;

  private Map<String, Object> filterOptions = new Hashtable<>();

  private Timeline timeline = new Timeline(new KeyFrame(
      Duration.millis(UPDATE_MAIN_VIEW_TICK),
      event -> refreshProposals(filterOptions)
  ));

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    UIController.primaryStage().setTitle("AutoMosreg - Главная (" + userService.getCurrentUser().toString() + ")");
    //TODO: привязать поле lblStatusText к работе потоков
    lblStatusText.setText(IDLE_MSG);
    cmbFilters.setOnAction(value -> {
      FilterOption filter = cmbFilters.getValue();
      if (filter != null) {
        clearFilter(true);
        setFilterFields(filter.getFields());
      }
    });
    cmbFilters.getItems().addAll(dictionaryService.findAllFilters(FilterType.PROPOSAL));

    cmbStatus.getItems().addAll(
        new StatusFilterOption(StatusFilterOption.ALL, "Все"),
        new StatusFilterOption(StatusFilterOption.OPENED, "Открытые закупки"),
        new StatusFilterOption(StatusFilterOption.CLOSED, "Закрытые закупки"),
        new StatusFilterOption(StatusFilterOption.ACTIVE, "Активные"),
        new StatusFilterOption(StatusFilterOption.ARCHIVED, "Архивные")
    );
    cmbStatus.getSelectionModel().select(0);

    webEngine = mainView.getEngine();
    webEngine.getLoadWorker().stateProperty().addListener((observable, oldValue, newValue) -> {
      if (newValue == Worker.State.SUCCEEDED) {
        refreshProposals(filterOptions);
      }
    });

    cmbItemsPerPage.getItems().addAll(5, 10, 20, 50, 100);
    cmbItemsPerPage.setValue(10);
    cmbItemsPerPage.setOnAction(event -> doApplyFilter());

    webEngine.load(AutoMosreg.getResource("/net/monsterdev/automosreg/ui/mainView.html").toExternalForm());

    waitIndicator = new WaitIndicator();

    currentPageChangeListener = (observable, oldValue, newValue) -> doApplyFilter();
    pages.currentPageIndexProperty().addListener(currentPageChangeListener);

    updateTradesService.start();
    tradeService.start();
    timeline.setCycleCount(Animation.INDEFINITE);
    timeline.play();
  }


  @Override
  public Stage getStage() {
    return UIController.primaryStage();
  }

  static void showUI() {
    try {
      MainController controller = (MainController) SpringFXMLLoader.load("/net/monsterdev/automosreg/ui/main.fxml");
      Scene mainScene = new Scene((Parent) controller.getView(), 1024, 768);
      UIController.primaryStage().setScene(mainScene);
      UIController.primaryStage().setMaximized(true);
      UIController.primaryStage().show();
    } catch (IOException e) {
      UIController.showErrorMessage("Ошибка открытия открытия главного окна приложения\n" +
          "Переустановка приложения может решить проблему");
      e.printStackTrace();
    }
  }

  private void prepareFilterOptions() {
    filterOptions.clear();

    if (!StringUtils.isEmpty(edtTradeNum.getText())) {
      filterOptions.put("TradeNum", Long.parseLong(edtTradeNum.getText()));
    }
    if (!StringUtils.isEmpty(edtTradeName.getText())) {
      filterOptions.put("TradeName", edtTradeName.getText());
    }
    if (dateBeginFrom.getValue() != null) {
      filterOptions.put("BeginFrom", dateBeginFrom.getValue());
    }
    if (dateBeginTo.getValue() != null) {
      filterOptions.put("BeginTo", dateBeginTo.getValue());
    }
    if (dateFinishFrom.getValue() != null) {
      filterOptions.put("FinishFrom", dateFinishFrom.getValue());
    }
    if (dateFinishTo.getValue() != null) {
      filterOptions.put("FinishTo", dateFinishTo.getValue());
    }
    if (cmbStatus.getValue() != null) {
      filterOptions.put("Status", cmbStatus.getValue());
    }
  }

  /**
   * Выполняет поиск предложений в БД, удовлетворяющих параметрам фильтра и
   * отображает их на экране
   */
  private void refreshProposals(Map<String, Object> filterOptions) {
    lockUI();
    int countPerPage = cmbItemsPerPage.getValue();
    int startIndex = pages.getCurrentPageIndex() * countPerPage;
    try {
      List<Trade> trades = tradesRepository.findAll(userService.getCurrentUser().getId(), filterOptions);
      List<TradeProposalItem> items = trades.stream()
          .skip(startIndex)
          .limit(startIndex + countPerPage)
          .map(TradeProposalItem::new)
          .collect(Collectors.toList());
      JSObject mainView = (JSObject) webEngine.executeScript("mainView");
      mainView.call("clearProposals");
      mainView.call("showProposals", items);
      pages.setPageCount(Math.floorDiv(trades.size(), countPerPage) + 1);
      lblTradesCount.setText(String.valueOf(trades.size()));
    } catch (Throwable t) {
      t.printStackTrace();
      pages.setPageCount(1);
      lblTradesCount.setText(String.valueOf(0));
    } finally {
      releaseUI();
    }
  }

  @FXML
  private void onFileClose(ActionEvent event) {
    Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
    confirm.setTitle("AutoMosreg");
    confirm.setHeaderText("Завершить работу приложения?");
    confirm.setContentText("Работа приложения для текущего пользователя будет полностью остановлена");
    if (confirm.showAndWait().get() == ButtonType.OK) {
      getStage().fireEvent(new WindowEvent(getStage(), WindowEvent.WINDOW_CLOSE_REQUEST));
    }
  }

  @FXML
  private void onTradeRefresh(ActionEvent event) {
    doApplyFilter();
  }

  @FXML
  private void onTradeNew(ActionEvent event) {
    Optional<List<TradeInfoDto>> tradeInfoList = TradeFilterController.showUI();
    if (!tradeInfoList.isPresent()) {
      return;
    }

    // запрашиваем у пользователя ограничения по контракту и время активации предложения
    Optional<ProposalData> optProposalData = ProposalController.showUI();
    if (!optProposalData.isPresent()) {
      return;
    }

    ProposalData proposalData = optProposalData.get();
    try {
      for (TradeInfoDto tradeInfo : tradeInfoList.get()) {
        Trade trade = new Trade();
        trade.setTradeId(tradeInfo.getId());
        trade.setName(tradeInfo.getTradeName());
        trade.setBeginDT(tradeInfo.getPublicationDate());
        trade.setEndDT(tradeInfo.getFillingApplicationEndDate());
        trade.setNmc(tradeInfo.getInitialPrice());
        trade.setStatus(TradeStatus.valueOf(tradeInfo.getTradeState()));
        trade.setActivateTime(proposalData.getActivateTime());
        trade.setMinTradeVal(proposalData.getMinTradeVal());
        trade.setStartPrice(Objects.isNull(proposalData.getStartTradeVal()) ? tradeInfo.getInitialPrice() : proposalData.getStartTradeVal());
        trade.getTradeProducts().addAll(productsService.getProductsForTrade(tradeInfo.getId()));
        userService.getCurrentUser().addTrade(trade);
      }
      userService.update();
      doApplyFilter();
    } catch (Throwable t) {
      LOG.error(String.format(ERROR_COMMON_LOG_MSG, t.getClass(), t.getMessage()));
      UIController.showErrorMessage(t.getMessage());
    }
  }

  @FXML
  private void onTradeEdit(ActionEvent event) {
    try {
      Optional<ProposalData> proposalData = ProposalController.showUI();
      if (!proposalData.isPresent()) {
        return;
      }
      JSObject mainView = (JSObject) webEngine.executeScript("mainView");
      String result = mainView.call("getSelected").toString();
      String[] ids = result.split(",");
      for (String id : ids) {
        Trade trade = userService.getCurrentUser().getTrade(Long.parseLong(id));
        trade.setMinTradeVal(proposalData.get().getMinTradeVal());
        trade.setActivateTime(proposalData.get().getActivateTime());
      }
      userService.update();
      doApplyFilter();
    } catch (Throwable t) {
      UIController.showErrorMessage(t.getMessage());
    }
  }

  @FXML
  private void onTradeDelete(ActionEvent event) {
    try {
      JSObject mainView = (JSObject) webEngine.executeScript("mainView");
      String result = mainView.call("getSelected").toString();
      if (result.isEmpty()) {
        throw new AutoMosregException("Выберете закупки, которые нужно удалить, а затем повторите операцию");
      }

      if (!UIController.showConfirmMessage("Вы действительно хотите удалить выбранные закупки?")) {
        return;
      }
      String[] ids = result.split(",");
      for (String id : ids) {
        userService.getCurrentUser().removeTrade(Long.parseLong(id));
      }
      userService.update();
      doApplyFilter();
    } catch (Throwable t) {
      UIController.showErrorMessage(t.getMessage());
    }
  }

  @FXML
  private void onTenderStart(ActionEvent event) {
  }

  @FXML
  private void onTenderStop(ActionEvent event) {
    //
  }

  @FXML
  private void onProfile(ActionEvent event) {
    EditProfileController.showUI();
  }

  @FXML
  private void onAbout(ActionEvent event) {
    //
  }

  private void doApplyFilter() {
    prepareFilterOptions();
    pages.currentPageIndexProperty().removeListener(currentPageChangeListener);
    pages.setCurrentPageIndex(0);
    pages.currentPageIndexProperty().addListener(currentPageChangeListener);
    webEngine.reload();
  }

  @FXML
  private void onFilterApply(ActionEvent event) {
    doApplyFilter();
  }

  @FXML
  private void onFilterSaveAs(ActionEvent event) {
    TextInputDialog dlg = new TextInputDialog();
    dlg.setTitle("AutoMosreg");
    dlg.setHeaderText("Имя фильтра (под этим именем фильтр будет сохранен в БД)");
    dlg.setContentText("имя фильтра (не более 50 символов)");
    String filterName = dlg.showAndWait().get();
    if (filterName.isEmpty()) {
      UIController.showErrorMessage("Вы должны задать имя фильтра");
      return;
    }
    if (filterName.length() > 50) {
      UIController.showErrorMessage("Длина имени фильтра не может превышать 50 символов");
      return;
    }

    Map<String, String> fields = new HashMap<>();
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    if (!edtTradeNum.getText().isEmpty()) {
      fields.put("TradeNum", FilterOption.prepareString(edtTradeNum.getText()));
    }
    if (!edtTradeName.getText().isEmpty()) {
      fields.put("TradeName", FilterOption.prepareString(edtTradeName.getText()));
    }
    if (dateBeginFrom.getValue() != null) {
      fields.put("BeginFrom", dateBeginFrom.getValue().format(formatter));
    }
    if (dateBeginTo.getValue() != null) {
      fields.put("BeginTo", dateBeginTo.getValue().format(formatter));
    }
    if (dateFinishFrom.getValue() != null) {
      fields.put("FinishFrom", dateFinishFrom.getValue().format(formatter));
    }
    if (dateFinishTo.getValue() != null) {
      fields.put("FinishTo", dateFinishTo.getValue().format(formatter));
    }
    if (cmbStatus.getValue() != null) {
      fields.put("Status", String.valueOf(cmbStatus.getValue().getCode()));
    }

    FilterOption filter = new FilterOption();
    filter.setName(filterName);
    filter.setFields(fields);
    dictionaryService.saveFilter(FilterType.PROPOSAL, filter);
    cmbFilters.getItems().add(filter);
    cmbFilters.setValue(filter);
  }

  private void clearFilter(boolean onlyFields) {
    edtTradeNum.clear();
    edtTradeName.clear();
    dateBeginFrom.setValue(null);
    dateBeginTo.setValue(null);
    dateFinishFrom.setValue(null);
    dateFinishTo.setValue(null);
    cmbStatus.getSelectionModel().select(0);
    if (!onlyFields) {
      cmbFilters.getSelectionModel().clearSelection();
    }
  }

  private void setFilterFields(Map<String, String> fields) {
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    edtTradeNum.setText(fields.getOrDefault("TradeNum", ""));
    edtTradeName.setText(fields.getOrDefault("TradeName", ""));
    dateBeginFrom
        .setValue(fields.containsKey("BeginFrom") ? LocalDate.parse(fields.get("BeginFrom"), formatter) : null);
    dateBeginTo.setValue(fields.containsKey("BeginTo") ? LocalDate.parse(fields.get("BeginTo"), formatter) : null);
    dateFinishFrom
        .setValue(fields.containsKey("FinishFrom") ? LocalDate.parse(fields.get("FinishFrom"), formatter) : null);
    dateFinishTo.setValue(fields.containsKey("FinishTo") ? LocalDate.parse(fields.get("FinishTo"), formatter) : null);
    cmbStatus.getSelectionModel().select(0);
    for (StatusFilterOption status : cmbStatus.getItems()) {
      if (status.getCode() == Integer.parseInt(fields.getOrDefault("Status", "0"))) {
        cmbStatus.setValue(status);
      }
    }
  }

  @FXML
  private void onFilterClear(ActionEvent event) {
    clearFilter(false);
  }

  private void lockUI() {
    lblTradesCount.setText(PENDING_MSG);
    wrapPane.setDisable(true);
    rootPane.getChildren().add(waitIndicator);
  }

  private void releaseUI() {
    rootPane.getChildren().remove(waitIndicator);
    wrapPane.setDisable(false);
  }
}
