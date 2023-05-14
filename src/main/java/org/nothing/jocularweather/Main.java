package org.nothing.jocularweather;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.*;

/**
 * Main class for JocularWeather application.
 *
 * @author Eric Liu
 * @author Ganning Xu
 */
public class Main extends Application {

    private static final Label titleLabel = new Label("JocularWeather.jar.EXE");
    private static final TextField rightSearchField = new TextField();
    private static final Button rightSearchButton = new Button("Search");
    private static final HBox rightSearchGroup = new HBox(rightSearchField, rightSearchButton);
    private static final Label locationLabel = new Label("");
    private static final Image conditionsIcon = new Image(String.valueOf(Main.class.getResource("/icons/01d.png")), 20, 20, false, false);
    private static final InfoBox feelsLikeBox = new InfoBox("Feels like", "");
    private static final InfoBox tempBox = new InfoBox("Current temperature", "");
    private static final InfoBox conditionsBox = new InfoBox("Conditions", "", conditionsIcon);
    private static final InfoBox humidityBox = new InfoBox("Humidity", "");
    private static final InfoBox pressureBox = new InfoBox("Sea level pressure", "");
    private static final InfoBox windBox = new InfoBox("Wind", "");
    private static final InfoBox sunriseBox = new InfoBox("Sunrise", "");
    private static final InfoBox sunsetBox = new InfoBox("Sunset", "");
    private static final FlowPane contentGroup = new FlowPane(feelsLikeBox, tempBox, conditionsBox, humidityBox, pressureBox, windBox, sunriseBox, sunsetBox);
    private static final VBox contentBox = new VBox(locationLabel, contentGroup);
    private static final Button jokeButton = new Button("Get Joke");
    private static final Label jokeLabel = new Label();
    private static final VBox jokeGroup = new VBox(jokeButton, jokeLabel);
    private static final VBox rightPane = new VBox();
    private static final TextField leftSearchField = new TextField();
    private static final Button leftSearchButton = new Button("Add");
    private static final HBox leftSearchGroup = new HBox(leftSearchField, leftSearchButton);
    private static final VBox locationsGroup = new VBox();
    private static final VBox leftPane = new VBox();

    private final Fetcher fetcher = new Fetcher();
    private final ArrayList<LocationBox> savedLocationBoxes = new ArrayList<>();

    /**
     * Returns formatted time from Unix timestamp.
     *
     * @param unixTime time at location in seconds after Unix epoch
     * @param timeZone time zone offset in seconds from UTC
     * @return String formatted value in hh:mm AM/PM
     */
    public static String getHourlyTime(long unixTime, long timeZone) {
        Date myDate = new Date(unixTime * 1000);
        Calendar time = Calendar.getInstance(Locale.UK);

        // Fix time zone to API output
        time.setTimeZone(new SimpleTimeZone((int) (timeZone * 1000), ""));
        time.setTime(myDate);

        return String.format("%d:%02d %s", time.get(Calendar.HOUR), time.get(Calendar.MINUTE), (time.get(Calendar.AM_PM) == Calendar.AM ? "AM" : "PM"));
    }

    /**
     * Runs program
     *
     * @param args Command line arguments; unused
     */
    public static void main(String[] args) {
        launch();
    }

    /**
     * Formats weather report elements in main frame given Report object.
     *
     * @param report Populated weather report
     */
    public void formatReport(ReportBase report) {
        switch (report.type()) {
            case OKAY -> {
                Report weather = (Report) report;
                // Pull required fields from weather report
                String cityName = weather.name();
                String country = weather.sys().country();
                double feelsLike = weather.main().feels_like();
                double temp = weather.main().temp();
                long timeZone = weather.timezone();
                String conditions = weather.weather().get(0).main();
                String icon = weather.weather().get(0).icon();
                int humidity = weather.main().humidity();
                int pressure = weather.main().pressure();
                int windDirection = weather.wind().deg();
                double windSpeed = weather.wind().gust();
                String sunriseTime = getHourlyTime(weather.sys().sunrise(), timeZone);
                String sunsetTime = getHourlyTime(weather.sys().sunset(), timeZone);

                feelsLikeBox.setContentText(String.format("%s °F", feelsLike));
                tempBox.setContentText(String.format("%s °F", temp));
                conditionsBox.setContentText(conditions);
                humidityBox.setContentText(String.format("%d%%", humidity));
                pressureBox.setContentText(String.format("%d hPa", pressure));
                windBox.setContentText(String.format("%03d°/%.02f mph", windDirection, windSpeed));
                sunriseBox.setContentText(sunriseTime);
                sunsetBox.setContentText(sunsetTime);

                // Set condition icon
                try {
                    conditionsBox.getIcon().setImage(new Image(Objects.requireNonNull(Main.class.getResource(String.format("/icons/%s.png", icon))).openStream(), 20, 20, false, false));
                } catch (IOException e) {
                    Logger.print(MessageType.JW_ERROR, "Could not find icon");
                    e.printStackTrace();
                }

                locationLabel.setText(String.format("Current Location: %s, %s", cityName, country));
            }
            // TODO clear all fields when done
            // TODO go over this and make sure it's valid
            case LOCATION_NOT_FOUND -> locationLabel.setText("Couldn't find location! Please try again.");
            case API_ERROR -> locationLabel.setText("Couldn't connect to weather API.");
            case NOT_OKAY -> locationLabel.setText("Something went wrong! AAAAA");
        }
    }

    @Override
    public void start(Stage stage) {
        // Pull saved locations from database and get current report
        Logger.print(MessageType.JW_INFO, "Starting JocularWeather.jar.EXE");
        ArrayList<String> savedLocations = Fetcher.getSavedLocations();

        // Map ZIP code and weather report objects together
        Logger.print(MessageType.JW_INFO, "Requesting weather reports for saved locations");
        TreeMap<String, ReportBase> savedLocationReports = fetcher.getWeatherReports(savedLocations);
        formatReport(fetcher.getWeatherReport(fetcher.getCurrentCity()));

        Logger.print(MessageType.JW_INFO, "Rendering saved locations");

        for (Map.Entry<String, ReportBase> entry : savedLocationReports.entrySet()) {
            // Update location boxes in sidebar
            String zip = entry.getKey();
            ReportBase report = entry.getValue();
            Logger.print(MessageType.JW_INFO, report.toString());

            savedLocationBoxes.add(formatLocationBox(zip, report));
        }

        // Put boxes into group
        locationsGroup.getChildren().addAll(savedLocationBoxes);

        // Configure prompts
        leftSearchField.setPromptText("Enter ZIP...");
        leftSearchField.setOnAction((e) -> {
            // TODO handle invalid ZIPs properly
            if (fetcher.addZipToSaved(leftSearchField.getText())) {
                addLocation(leftSearchField.getText());
            }
        });
        leftSearchButton.setOnAction((e) -> {
            if (fetcher.addZipToSaved(leftSearchField.getText())) {
                addLocation(leftSearchField.getText());
            }
        });

        rightSearchField.setPromptText("Enter location...");
        rightSearchField.setOnAction((e) -> formatReport(fetcher.getWeatherReport(rightSearchField.getText())));
        rightSearchButton.setOnAction((e) -> formatReport(fetcher.getWeatherReport(rightSearchField.getText())));
        jokeButton.setOnAction((e) -> jokeLabel.setText(Fetcher.getWeatherJoke()));

        // Adjust spacing for right pane content
        leftSearchGroup.setSpacing(20);
        leftSearchGroup.setPrefWidth(220);
        rightSearchGroup.setSpacing(20);
        contentGroup.setPrefWidth(600);
        contentGroup.setHgap(20);
        contentGroup.setPadding(new Insets(10));

        // Align everything in right pane and push together
        rightSearchGroup.setAlignment(Pos.CENTER);
        contentGroup.setAlignment(Pos.CENTER);
        contentBox.setAlignment(Pos.CENTER);
        jokeGroup.setAlignment(Pos.CENTER);

        // Set spacing for pane parent object
        leftPane.setPadding(new Insets(10));
        leftPane.setSpacing(10);
        rightPane.setSpacing(10);
        leftPane.setAlignment(Pos.TOP_LEFT);
        rightPane.setAlignment(Pos.TOP_CENTER);

        leftPane.getChildren().addAll(leftSearchGroup, locationsGroup);
        rightPane.getChildren().addAll(titleLabel, rightSearchGroup, locationLabel, contentBox, jokeGroup);

        // Assemble entire scene
        HBox allContent = new HBox(leftPane, rightPane);
        HBox.setHgrow(allContent, Priority.ALWAYS);
        allContent.setPadding(new Insets(20));

        Scene scene = new Scene(allContent, 720, 480);
        scene.getStylesheets().add("style.css");

        stage.setScene(scene);
        stage.show();
    }

    /**
     * Returns location box given a specific location and its corresponding weather report.
     *
     * @param zip    5 digit United States ZIP code
     * @param report Report object
     * @return LocationBox associated location box
     */
    private LocationBox formatLocationBox(String zip, ReportBase report) {
        LocationBox box;
        if (report.type().equals(ResultType.OKAY)) {
            // Set up correctly formatted weather
            Report weather = (Report) report;
            box = new LocationBox(String.format("%s, %s", weather.name(), weather.sys().country()), zip);
            box.setCondition(weather.weather().get(0).main());
            box.setTemperature(String.format("%s °F", weather.main().temp()));

            // Set updater for bringing saved location into focus
            box.setOnMouseClicked((e) -> formatReport(fetcher.getWeatherReport(box.getZip())));
        } else {
            // Show malformed location box
            box = new LocationBox("Couldn't find", zip);
            box.setCondition(zip);
            box.setTemperature("☒☒ °F");
        }

        // Configure delete action
        box.getDeleteButton().setOnAction((e) -> {
            fetcher.removeZipFromSaved(zip);
            savedLocationBoxes.remove(box);

            locationsGroup.getChildren().clear();
            locationsGroup.getChildren().addAll(savedLocationBoxes);
        });

        return box;
    }

    /**
     * Add location to group of location boxes.
     *
     * @param zip 5 digit United States ZIP code
     */
    private void addLocation(String zip) {
        if (fetcher.addZipToSaved(zip)) {
            savedLocationBoxes.add(formatLocationBox(zip, fetcher.getWeatherReport(zip)));

            // Refresh locations
            locationsGroup.getChildren().clear();
            locationsGroup.getChildren().addAll(savedLocationBoxes);
        }
    }
}