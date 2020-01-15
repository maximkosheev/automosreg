package net.monsterdev.automosreg.ui;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import net.monsterdev.automosreg.http.tasks.GetFilteredTradesTask;
import net.monsterdev.automosreg.domain.FilterOption;
import net.monsterdev.automosreg.enums.FilterType;
import net.monsterdev.automosreg.model.StatusFilterOption;
import net.monsterdev.automosreg.model.dto.TradeInfoDto;
import net.monsterdev.automosreg.model.dto.TradesInfoDto;
import net.monsterdev.automosreg.services.DictionaryService;
import net.monsterdev.automosreg.ui.control.MultilineCellFactory;
import net.monsterdev.automosreg.ui.control.WaitIndicator;
import net.monsterdev.automosreg.ui.model.TradeItem;
import net.monsterdev.automosreg.utils.SpringFXMLLoader;
import net.monsterdev.automosreg.utils.StringUtil;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Controller;

import java.io.IOException;
import java.math.RoundingMode;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Controller
public class TradeFilterController extends AbstractUIController implements WindowController {
    private static final String START_MSG = "Нет данных для отображения";
    private static final String PENDING_MSG = "Ожидайте, идет обновление...";
    @FXML
    private StackPane contentPane;
    @FXML
    private VBox wrapPane;
    @FXML
    private Label lblTradesCount;
    @FXML
    private TextField edtTradeNum;
    @FXML
    private TextField edtCustomerName;
    @FXML
    private TextField edtEASUZNum;
    @FXML
    private TextField edtCustomerLoc;
    @FXML
    private TextField edtTradeName;
    @FXML
    private DatePicker dateStartFrom;
    @FXML
    private DatePicker dateStartTo;
    @FXML
    private DatePicker dateEndFrom;
    @FXML
    private DatePicker dateEndTo;
    @FXML
    private TextField edtSummMin;
    @FXML
    private TextField edtSummMax;
    @FXML
    private TextField edtKOZ;
    @FXML
    private ComboBox<StatusFilterOption> cmbStatus;
    @FXML
    private ComboBox<FilterOption> cmbFilters;
    @FXML
    private TableView<TradeItem> tblTrades;
    @FXML
    private Pagination pagination;
    @FXML
    private ComboBox<Integer> cmbItemsPerPage;
    private WaitIndicator waitIndicator;

    @Autowired
    private DictionaryService dictionaryService;

    @Autowired
    private ApplicationContext applicationContext;

    private DateFormat onlyDate = new SimpleDateFormat("dd.MM.yyyy");
    private DateFormat withTime = new SimpleDateFormat("dd.MM.yyyy HH:mm");

    private boolean isOK = false;

    private ChangeListener<Number> currentPageChangeListener;

    @Override
    @SuppressWarnings("unchecked")
    public void initialize(URL location, ResourceBundle resources) {
        UIController.primaryStage().setTitle("AutoMosreg - Поиск закупок");
        lblTradesCount.setText(START_MSG);
        cmbFilters.setOnAction(value -> {
            FilterOption filter = cmbFilters.getValue();
            if (filter != null) {
                clearFilter(true);
                setFilterFields(filter.getFields());
            }
        });
        cmbFilters.getItems().addAll(dictionaryService.findAllFilters(FilterType.PROPOSAL));
        cmbStatus.getItems().addAll(
                new StatusFilterOption(0, "Все"),
                new StatusFilterOption(15, "Прием предложений"),
                new StatusFilterOption(20, "Согласование"),
                new StatusFilterOption(40, "Заключение договора"),
                new StatusFilterOption(50, "Договор заключен"),
                new StatusFilterOption(25, "Нет предложений"),
                new StatusFilterOption(30, "Отменена")
        );
        cmbStatus.getSelectionModel().select(0);
        cmbItemsPerPage.getItems().addAll(5, 10, 20, 50, 100);
        cmbItemsPerPage.setValue(100);
        cmbItemsPerPage.setOnAction(event -> doFilter(0));

        CheckBox chbSelectAll = new CheckBox();
        chbSelectAll.selectedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                tblTrades.getItems().forEach(trade -> trade.setSelected(newValue));
            }
        });

        TableColumn<TradeItem, Boolean> c0 = new TableColumn<>("");
        c0.setCellValueFactory(new PropertyValueFactory<>("selected"));
        c0.setCellFactory(CheckBoxTableCell.forTableColumn(c0));
        c0.setGraphic(chbSelectAll);
        c0.setPrefWidth(34);

        TableColumn<TradeItem, Long> c1 = new TableColumn<>("Номер\nзакупки");
        c1.setCellValueFactory(new PropertyValueFactory<>("id"));
        c1.setStyle("-fx-alignment: CENTER;");
        c1.prefWidthProperty().bind(tblTrades.widthProperty().subtract(34).multiply(0.05));

        TableColumn<TradeItem, String> c2 = new TableColumn<>("Заказчик");
        c2.setCellValueFactory(new PropertyValueFactory<>("customer"));
        c2.setCellFactory(new MultilineCellFactory());
        c2.prefWidthProperty().bind(tblTrades.widthProperty().subtract(34).multiply(0.337));

        TableColumn<TradeItem, String> c3 = new TableColumn<>("Наименование закупки");
        c3.setCellValueFactory(new PropertyValueFactory<>("name"));
        c3.setCellFactory(new MultilineCellFactory());
        c3.prefWidthProperty().bind(tblTrades.widthProperty().subtract(34).multiply(0.305));

        TableColumn<TradeItem, String> c4 = new TableColumn<>("НМЦ");
        c4.setCellValueFactory(new PropertyValueFactory<>("initialPrice"));
        c4.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getInitialPrice().setScale(2, RoundingMode.UP).toString()));
        c4.setStyle("-fx-alignment: CENTER;");
		c4.prefWidthProperty().bind(tblTrades.widthProperty().subtract(34).multiply(0.05));

        TableColumn<TradeItem, String> c5 = new TableColumn<>("Дата и время\nначала подачи\nпредложений");
        c5.setCellValueFactory(param -> new SimpleStringProperty(onlyDate.format(param.getValue().getPublicationDate())));
        c5.setStyle("-fx-alignment: CENTER;");
		c5.prefWidthProperty().bind(tblTrades.widthProperty().subtract(34).multiply(0.073));

        TableColumn<TradeItem, String> c6 = new TableColumn<>("Дата и время\nокончания подачи\nпредложений");
        c6.setCellValueFactory(new PropertyValueFactory<>("fillingApplicationEndDate"));
        c6.setCellValueFactory(param -> new SimpleStringProperty(withTime.format(param.getValue().getFillingApplicationEndDate())));
        c6.setStyle("-fx-alignment: CENTER;");
		c6.prefWidthProperty().bind(tblTrades.widthProperty().subtract(34).multiply(0.073));

        TableColumn<TradeItem, String> c7 = new TableColumn<>("Статус");
        c7.setCellValueFactory(new PropertyValueFactory<>("stateName"));
        c7.setStyle("-fx-alignment: CENTER;");
		c7.prefWidthProperty().bind(tblTrades.widthProperty().subtract(34).multiply(0.097));

		currentPageChangeListener = (observable, oldValue, newValue) -> doFilter(newValue.intValue());
        tblTrades.getColumns().addAll(c0, c1, c2, c3, c4, c5, c6, c7);
        waitIndicator = new WaitIndicator();

        onFilterApply(null);
    }

    @Override
    public Stage getStage() {
        return (Stage) contentPane.getScene().getWindow();
    }

    static Optional<List<TradeInfoDto>> showUI() {
        try {
            TradeFilterController controller = (TradeFilterController) SpringFXMLLoader.load("/net/monsterdev/automosreg/ui/trades_filter.fxml");
            Stage stage = new Stage();
            stage.setScene(new Scene((Parent) controller.getView(), 860, 770));
            stage.setTitle("AutoMosreg - Поиск закупок");
            stage.setMaximized(true);
            stage.showAndWait();
            if (controller.isOK) {
                List<TradeInfoDto> selectedTrades = new ArrayList<>();
                controller.tblTrades.getItems().stream()
                        .filter(TradeItem::isSelected)
                        .forEach(tradeItem -> selectedTrades.add(tradeItem.getInfo()));
                return Optional.of(selectedTrades);
            } else
                return Optional.empty();
        } catch (IOException e) {
            UIController.showErrorMessage("Ошибка открытия открытия окна фильтра закупок\n" +
                    "Переустановка приложения может решить проблему");
            e.printStackTrace();
            return Optional.empty();
        }
    }

    // TODO: Избавится от JSONObject
    private void doFilter(int page) {
        JSONObject filter = new JSONObject();

        String classificatorCodes = StringUtil.toNotNull(edtKOZ.getText());
        if (!classificatorCodes.isEmpty())
            filter.put("classificatorCodes", classificatorCodes.split(","));
        else
            filter.put("classificatorCodes", new ArrayList<String>());

        filter.put("CustomerAddress", StringUtil.toNotNull(edtCustomerLoc.getText()));
        filter.put("CustomerFullNameOrInn", StringUtil.toNotNull(edtCustomerName.getText()));

        if (dateStartFrom.getValue() != null)
            filter.put("filterDateFrom", StringUtil.fromLocalDate(dateStartFrom.getValue()));
        else
            filter.put("filterDateFrom", JSONObject.NULL);

        if (dateStartTo.getValue() != null)
            filter.put("filterDateTo", StringUtil.fromLocalDate(dateStartTo.getValue()));
        else
            filter.put("filterDateTo", JSONObject.NULL);

        if (dateEndFrom.getValue() != null)
            filter.put("filterFillingApplicationEndDateFrom", StringUtil.fromLocalDate(dateEndFrom.getValue()));
        else
            filter.put("filterFillingApplicationEndDateFrom", JSONObject.NULL);

        if (dateEndTo.getValue() != null)
            filter.put("FilterFillingApplicationEndDateTo", StringUtil.fromLocalDate(dateEndTo.getValue()));
        else
            filter.put("FilterFillingApplicationEndDateTo", JSONObject.NULL);

        filter.put("filterPriceMin", StringUtil.toLong(edtSummMin.getText()));
        filter.put("filterPriceMax", StringUtil.toLong(edtSummMax.getText()));
        filter.put("filterTradeEasuzNumber", StringUtil.toNotNull(edtEASUZNum.getText()));
        filter.put("IsImmediate", false);
        filter.put("itemsPerPage", cmbItemsPerPage.getValue());
        filter.put("OnlyTradesWithMyApplications", false);
        filter.put("page", page + 1);
        filter.put("ParticipantHasApplicationsOnTrade", "");
        filter.put("showOnlyOwnTrades", false);
        filter.put("sortingParams", new ArrayList<>());
        if (!StringUtil.toNotNull(edtTradeNum.getText()).isEmpty())
            filter.put("Id", StringUtil.toLong(edtTradeNum.getText()));
        if (!StringUtil.toNotNull(edtTradeName.getText()).isEmpty())
            filter.put("tradeName", edtTradeName.getText());
        if (cmbStatus.getValue().getCode() > 0)
            filter.put("tradeState", cmbStatus.getValue().getCode());
        else
            filter.put("tradeState", "");
        filter.put("UsedClassificatorType", 10);

        GetFilteredTradesTask filteringTask = new GetFilteredTradesTask(filter);
        filteringTask.setOnFailed(event1 -> {
            releaseUI();
            UIController.showErrorMessage(filteringTask.getException().getMessage());
        });
        filteringTask.setOnSucceeded(event1 -> {
            releaseUI();
            TradesInfoDto tradesInfoList = filteringTask.getValue();
            ObservableList<TradeItem> items = FXCollections.observableArrayList();
            for (TradeInfoDto tradeInfo : tradesInfoList.getTrades()) {
                items.add(new TradeItem(tradeInfo));
            }
            tblTrades.setItems(items);
            lblTradesCount.setText(String.valueOf(tradesInfoList.getTotalrecords()));
            pagination.setPageCount(tradesInfoList.getTotalpages() > 0 ? tradesInfoList.getTotalpages() : 1);
            pagination.currentPageIndexProperty().removeListener(currentPageChangeListener);
            // setCurrentPageIndex генерирует событие CHANGE, на которое повешен слушатель currentPageChangeListener,
            // который отправлят запрос на получения списка закупок. При обработке этого списка снова задается CurrentPageIndex
            // и так далее до бесконечности. Чтобы этого избежать, перед указанием CurrentPageIndex сначала удаляем слушатель,
            // а затем снова его добавляем
            pagination.setCurrentPageIndex(tradesInfoList.getCurrpage() - 1);
            pagination.currentPageIndexProperty().addListener(currentPageChangeListener);
            tblTrades.refresh();
        });
        lblTradesCount.setText(PENDING_MSG);
        lockUI();
        Thread loginThread = new Thread(filteringTask);
        applicationContext.getAutowireCapableBeanFactory().autowireBean(filteringTask);
        loginThread.start();
    }

    private void clearFilter(boolean onlyFields) {
        edtTradeNum.clear();
        edtCustomerName.clear();
        edtEASUZNum.clear();
        edtCustomerLoc.clear();
        edtTradeName.clear();
        dateStartFrom.setValue(null);
        dateStartTo.setValue(null);
        dateEndFrom.setValue(null);
        dateEndTo.setValue(null);
        edtSummMin.clear();
        edtSummMax.clear();
        edtKOZ.clear();
        cmbStatus.getSelectionModel().select(0);
        if (!onlyFields)
            cmbFilters.setValue(null);
    }

    private void setFilterFields(Map<String, String> fields) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");

        edtTradeNum.setText(fields.getOrDefault("TradeNum", ""));
        edtCustomerName.setText(fields.getOrDefault("CustomerName", ""));
        edtEASUZNum.setText(fields.getOrDefault("EASUZNum", ""));
        edtCustomerLoc.setText(fields.getOrDefault("CustomerLoc", ""));
        edtTradeName.setText(fields.getOrDefault("TradeName", ""));
        dateStartFrom.setValue(fields.containsKey("StartFrom") ? LocalDate.parse(fields.get("StartFrom"), formatter) : null);
        dateStartTo.setValue(fields.containsKey("StartTo") ? LocalDate.parse(fields.get("StartTo"), formatter) : null);
        dateEndFrom.setValue(fields.containsKey("EndFrom") ? LocalDate.parse(fields.get("EndFrom"), formatter) : null);
        dateEndTo.setValue(fields.containsKey("EndTo") ? LocalDate.parse(fields.get("EndTo"), formatter) : null);
        edtSummMin.setText(fields.getOrDefault("SummMin", ""));
        edtSummMax.setText(fields.getOrDefault("SummMax", ""));
        edtKOZ.setText(fields.getOrDefault("KOZ", ""));
        cmbStatus.getSelectionModel().select(0);
        for (StatusFilterOption status : cmbStatus.getItems()) {
            if (status.getCode() == Integer.parseInt(fields.getOrDefault("Status", "0")))
                cmbStatus.setValue(status);
        }
    }

    @FXML
    @SuppressWarnings("unused")
    private void onKOZSelect(ActionEvent event) {
        //
    }

    @FXML
    @SuppressWarnings("unused")
    private void onOk(ActionEvent event) {
        if (tblTrades.getItems().stream().noneMatch(TradeItem::isSelected)) {
            UIController.showErrorMessage("Не выбрано ни одной закупки");
            return;
        }
        isOK = true;
        close();
    }

    @FXML
    @SuppressWarnings("unused")
    private void onCancel(ActionEvent event) {
        isOK = false;
        close();
    }

    private void lockUI() {
        wrapPane.setDisable(true);
        contentPane.getChildren().add(waitIndicator);
    }

    private void releaseUI() {
        contentPane.getChildren().remove(waitIndicator);
        wrapPane.setDisable(false);
    }

    @FXML
    @SuppressWarnings("unused")
    private void onFilterClear(ActionEvent event) {
        clearFilter(false);
    }

    @FXML
    @SuppressWarnings("unused")
    private void onFilterApply(ActionEvent event) {
        doFilter(0);
    }

    @FXML
    @SuppressWarnings("unused")
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
        if (!edtTradeNum.getText().isEmpty())
            fields.put("TradeNum", FilterOption.prepareString(edtTradeNum.getText()));
        if (!edtEASUZNum.getText().isEmpty())
            fields.put("EASUZNum", FilterOption.prepareString(edtEASUZNum.getText()));
        if (!edtTradeName.getText().isEmpty())
            fields.put("TradeName", FilterOption.prepareString(edtTradeName.getText()));
        if (!edtCustomerName.getText().isEmpty())
            fields.put("CustomerName", FilterOption.prepareString(edtCustomerName.getText()));
        if (!edtCustomerLoc.getText().isEmpty())
            fields.put("CustomerLoc", FilterOption.prepareString(edtCustomerLoc.getText()));
        if (!edtSummMin.getText().isEmpty())
            fields.put("SummMin", FilterOption.prepareString(edtSummMin.getText()));
        if (!edtSummMax.getText().isEmpty())
            fields.put("SummMax", FilterOption.prepareString(edtSummMax.getText()));
        if (dateStartFrom.getValue() != null)
            fields.put("StartFrom", dateStartFrom.getValue().format(formatter));
        if (dateStartTo.getValue() != null)
            fields.put("StartTo", dateStartTo.getValue().format(formatter));
        if (dateEndFrom.getValue() != null)
            fields.put("EndFrom", dateEndFrom.getValue().format(formatter));
        if (dateEndTo.getValue() != null)
            fields.put("EndTo", dateEndTo.getValue().format(formatter));
        if (!edtKOZ.getText().isEmpty())
            fields.put("KOZ", FilterOption.prepareString(edtKOZ.getText()));
        if (cmbStatus.getValue() != null)
            fields.put("Status", String.valueOf(cmbStatus.getValue().getCode()));

        FilterOption filter = new FilterOption();
        filter.setName(filterName);
        filter.setFields(fields);
        dictionaryService.saveFilter(FilterType.TRADE, filter);
        cmbFilters.getItems().add(filter);
        cmbFilters.setValue(filter);
    }
}
