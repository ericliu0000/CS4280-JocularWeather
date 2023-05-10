package org.nothing.jocularweather;

import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

public class InfoBox extends VBox {
    private Label headerLabel;

    public InfoBox(String title, Node... children) {
        super(children);
        headerLabel = new Label(title);        
    }
}
