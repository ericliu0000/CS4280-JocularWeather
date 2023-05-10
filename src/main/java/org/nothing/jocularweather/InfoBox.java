package org.nothing.jocularweather;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

public class InfoBox extends VBox {
    private Label headerLabel;
    private Label contentLabel;

    /**
     * 
     * @param title
     * @param body
     * @param children
     */
    public InfoBox(String title, String body, Node... children) {
        headerLabel = new Label(title);
        contentLabel = new Label(body);

        this.setAlignment(Pos.CENTER);
        this.setPadding(new Insets(10, 10, 10, 10));

        this.getChildren().addAll(headerLabel, contentLabel);
        this.getChildren().addAll(children);
    }
    
    /**
     * 
     * @param text
     */
    public void setContentText(String text) {
        contentLabel.setText(text);
    }

    /**
     * 
     * @param text
     */
    public void setHeaderText(String text) {
        headerLabel.setText(text);
    }
}
