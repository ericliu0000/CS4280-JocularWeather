package org.nothing.jocularweather;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

public class InfoBox extends VBox {
    private final Label contentLabel;

    /**
     * Creates InfoBox object for displaying weather report data
     *
     * @param title    text to go in title
     * @param body     body text
     * @param children any additional nodes
     */
    public InfoBox(String title, String body, Node... children) {
        Label headerLabel = new Label(title);
        contentLabel = new Label(body);

        this.setAlignment(Pos.CENTER);
        this.setSpacing(5);
        this.setPadding(new Insets(10));
        this.setPrefWidth(200);

        this.getChildren().addAll(headerLabel, contentLabel);
        this.getChildren().addAll(children);
    }

    /**
     * Set body text
     *
     * @param text desired text
     */
    public void setContentText(String text) {
        contentLabel.setText(text);
    }

}
