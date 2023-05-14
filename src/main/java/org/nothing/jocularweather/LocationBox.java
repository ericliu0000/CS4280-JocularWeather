package org.nothing.jocularweather;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

public class LocationBox extends HBox {
    private final Label conditionLabel = new Label("Not available");
    private final Label temperatureLabel = new Label("nn");

    private final String zip;

    /**
     * Creates new location box.
     * 
     * @param locationName name of city
     * @param zip          zip code of city
     */
    public LocationBox(String locationName, String zip) {
        this.zip = zip;
        Label locationLabel = new Label();
        locationLabel.setText(locationName);
        temperatureLabel.setAlignment(Pos.CENTER_RIGHT);

        VBox leftGroup = new VBox(locationLabel, conditionLabel);
        leftGroup.setAlignment(Pos.CENTER_LEFT);
        leftGroup.setSpacing(10);

        Region centerBuffer = new Region();
        HBox.setHgrow(centerBuffer, Priority.ALWAYS);

        this.setPrefWidth(150);
        this.setAlignment(Pos.CENTER_LEFT);
        this.setPadding(new Insets(10));
        this.getChildren().addAll(leftGroup, centerBuffer, temperatureLabel);
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
    public void setTemperature(long temp) {
        temperatureLabel.setText(String.format("%s °F", temp));
    }

    /**
     * Returns zip code of city
     * 
     * @return String zip code
     */
    public String getZip() {
        return zip;
    }
}
