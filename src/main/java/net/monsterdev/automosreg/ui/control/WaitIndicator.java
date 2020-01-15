package net.monsterdev.automosreg.ui.control;

import javafx.geometry.Pos;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.VBox;

public class WaitIndicator extends VBox {
    public WaitIndicator() {
        super(new ProgressIndicator());
        setAlignment(Pos.CENTER);
    }
}
