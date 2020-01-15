package net.monsterdev.automosreg.ui.control;

import javafx.scene.control.ListCell;
import net.monsterdev.automosreg.domain.Document;

public class DocumentListCell extends ListCell<Document> {
    @Override
    protected void updateItem(Document item, boolean empty) {
        super.updateItem(item, empty);
        if (!empty)
            setText(item.getName());
    }
}
