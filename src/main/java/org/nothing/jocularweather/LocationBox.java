package org.nothing.jocularweather;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.OverrunStyle;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

/**
 * Object containing elements that display current weather information of saved
 * weather locations.
 *
 * @author Eric Liu
 * @author Ganning Xu
 */
public class LocationBox extends HBox {
    private final Label conditionLabel = new Label("Not available");
    private final Label temperatureLabel = new Label("nn");
    private final Button deleteButton = new Button("X");

    private final String zip;

    /**
     * Creates new location box.
     *
     * @param locationName name of city
     * @param zip          zip code of city
     */
    public LocationBox(String locationName, String zip) {
        this.zip = zip;
        // Create labels for information to go in
        Label locationLabel = new Label();
        locationLabel.setText(locationName);
        temperatureLabel.setAlignment(Pos.CENTER_RIGHT);

        // Group and align data
        VBox leftGroup = new VBox(locationLabel, conditionLabel);
        leftGroup.setAlignment(Pos.CENTER_LEFT);
        leftGroup.setSpacing(5);

        // Set padding between to space out right and left sections
        Region centerBuffer = new Region();
        HBox.setHgrow(centerBuffer, Priority.ALWAYS);

        // Configure deletion button
        deleteButton.setAlignment(Pos.CENTER_RIGHT);
        deleteButton.setTextOverrun(OverrunStyle.CLIP);
        deleteButton.setPrefSize(20, 20);

        this.setPrefWidth(220);
        this.setMinWidth(200);

        // Style elements
        conditionLabel.getStyleClass().add("condition-label");
        temperatureLabel.getStyleClass().add("temperature-label");
        locationLabel.getStyleClass().add("location-label");
        deleteButton.getStyleClass().add("delete-button");
        this.getStyleClass().add("location-box");

        // Configure alignment and set up object
        this.setAlignment(Pos.CENTER_LEFT);
        this.setPadding(new Insets(10));
        this.setSpacing(5);
        this.getChildren().addAll(leftGroup, centerBuffer, temperatureLabel, deleteButton);
        setMargin(centerBuffer, getInsets());
    }

    /**
     * Sets text field of condition box.
     *
     * @param text qualitative weather at site
     */
    public void setCondition(String text) {
        conditionLabel.setText(text);
    }

    /**
     * Sets temperature field of location box.
     *
     * @param temp numeric temperature value
     */
    public void setTemperature(String temp) {
        temperatureLabel.setText(temp);
    }

    /**
     * Returns zip code of city.
     *
     * @return String zip code
     */
    public String getZip() {
        return zip;
    }

    /**
     * Returns delete button object.
     *
     * @return Button
     */
    public Button getDeleteButton() {
        return deleteButton;
    }
}
