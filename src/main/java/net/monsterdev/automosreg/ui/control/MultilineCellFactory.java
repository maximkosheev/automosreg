package net.monsterdev.automosreg.ui.control;

import javafx.scene.control.Control;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.util.Callback;
import net.monsterdev.automosreg.ui.model.TradeItem;

public class MultilineCellFactory implements Callback<TableColumn<TradeItem, String>, TableCell<TradeItem, String>> {
    @Override
    public TableCell<TradeItem, String> call(TableColumn<TradeItem, String> param) {
        final TableCell<TradeItem, String> cell = new TableCell<TradeItem, String>() {
            private Text text = null;
            private static final int PADDING = 10;

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setPrefHeight(Control.USE_COMPUTED_SIZE);
                if (!empty) {
                    text = new Text(item);
                    text.setTextAlignment(TextAlignment.CENTER);
                    text.wrappingWidthProperty().bind(getTableColumn().widthProperty().subtract(PADDING));
                    setGraphic(text);
                }
            }
        };
        return cell;
    }
}
