package org.nothing.jocularweather;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class InfoBox extends VBox {
    private final Label contentLabel;
    private final HBox contentGroup = new HBox();
    private final ImageView icon = new ImageView();

    /**
     * Creates InfoBox object for displaying weather report data with icon.
     *
     * @param title text to go in title
     * @param body  body text
     * @param image image to display in icon
     */
    public InfoBox(String title, String body, Image image) {
        icon.setImage(image);

        Label headerLabel = new Label(title);
        contentLabel = new Label(body);
        contentGroup.setAlignment(Pos.CENTER);
        contentGroup.getChildren().addAll(contentLabel, icon);

        this.setAlignment(Pos.CENTER);
        this.setSpacing(5);
        this.setPadding(new Insets(10));
        this.setPrefWidth(200);

        this.getChildren().addAll(headerLabel, contentGroup);
    }

    /**
     * Creates InfoBox object for displaying weather report data with text only.
     *
     * @param title text to go in title
     * @param body  body text
     */

    public InfoBox(String title, String body) {
        Label headerLabel = new Label(title);
        contentLabel = new Label(body);
        contentGroup.setAlignment(Pos.CENTER);

        contentGroup.getChildren().add(contentLabel);

        this.setAlignment(Pos.CENTER);
        this.setSpacing(5);
        this.setPadding(new Insets(10));
        this.setPrefWidth(200);

        this.getChildren().addAll(headerLabel, contentGroup);
    }

    /**
     * Set body text
     *
     * @param text desired text
     */
    public void setContentText(String text) {
        contentLabel.setText(text);
    }

    /**
     * Returns icon object
     *
     * @return Icon current icon object
     */
    public ImageView getIcon() {
        return icon;
    }
}
