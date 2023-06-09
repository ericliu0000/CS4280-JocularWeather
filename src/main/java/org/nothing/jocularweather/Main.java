package org.nothing.jocularweather;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.OverrunStyle;
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
 * Main class for JocularWeather application.<br>
 * JocularWeather is a free and open-source software as a service, developed with the goal of making weather data more accessible, light-hearted and enjoyable to those who need it the most. Plus, it collects less data than competing services but is much more transparent about it. It uses the <a href="https://openweathermap.org/api">OpenWeatherMap API</a> to read in weather data.
 *
 * <br>
 * CS4280
 * <br>
 * Mostly completed by 2023-05-17
 * <br><br>
 * We pledge that we honor code.
 *
 * @author Eric Liu
 * @author Ganning Xu
 */
public class Main extends Application {
    private static final Label titleLabel = new Label("JocularWeather.jar.EXE");
    private static final TextField rightSearchField = new TextField();
    private static final Button rightSearchButton = new Button("Search");
    private static final HBox rightSearchGroup = new HBox(rightSearchField, rightSearchButton);
    private static final Label statusLabel = new Label("");
    private static final Image conditionsIcon = new Image(String.valueOf(Main.class.getResource("/icons/01d.png")), 20, 20, false, false);
    private static final InfoBox feelsLikeBox = new InfoBox("Feels like", "");
    private static final InfoBox tempBox = new InfoBox("Current temperature", "");
    private static final InfoBox conditionsBox = new InfoBox("Conditions", "", conditionsIcon);
    private static final InfoBox humidityBox = new InfoBox("Humidity", "");
    private static final InfoBox pressureBox = new InfoBox("Sea level pressure", "");
    private static final InfoBox windBox = new InfoBox("Wind", "");
    private static final InfoBox sunriseBox = new InfoBox("Sunrise", "");
    private static final InfoBox sunsetBox = new InfoBox("Sunset", "");
    private static final FlowPane contentGroup = new FlowPane(feelsLikeBox, tempBox, conditionsBox, humidityBox,
            pressureBox, windBox, sunriseBox, sunsetBox);
    private static final VBox contentBox = new VBox(statusLabel, contentGroup);
    private static final Button jokeButton = new Button("Get Joke");
    private static final Label jokeLabel = new Label();
    private static final VBox jokeGroup = new VBox(jokeButton, jokeLabel);
    private static final VBox rightPane = new VBox();
    private static final TextField leftSearchField = new TextField();
    private static final Button leftSearchButton = new Button("Add");
    private static final HBox leftSearchGroup = new HBox(leftSearchField, leftSearchButton);
    private static final Label leftLabel = new Label("");
    private static final VBox locationsGroup = new VBox();
    private static final VBox leftPane = new VBox();

    private static final HBox allContent = new HBox();
    private static final Scene scene = new Scene(allContent, 780, 520);

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

        return String.format("%d:%02d %s", time.get(Calendar.HOUR), time.get(Calendar.MINUTE),
                (time.get(Calendar.AM_PM) == Calendar.AM ? "AM" : "PM"));
    }

    /**
     * Runs program.
     *
     * @param args Command line arguments; unused
     */
    public static void main(String[] args) {
        launch();
    }

    /**
     * Formats weather report elements in main frame given
     * {@link org.nothing.jocularweather.Report} object.
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

                titleLabel.setText(String.format("%s, %s", cityName, country));

                // Set condition icon
                try {
                    conditionsBox.getIcon()
                            .setImage(new Image(
                                    Objects.requireNonNull(Main.class.getResource(String.format("/icons/%s.png", icon)))
                                            .openStream(),
                                    20, 20, false, false));
                } catch (IOException e) {
                    Logger.print(MessageType.JW_ERROR, "Could not find icon");
                    e.printStackTrace();
                }

                statusLabel.setText("");
            }
            case LOCATION_NOT_FOUND -> {
                statusLabel.setText("Couldn't find location! Please try again.");
                clearFields();
            }
            case API_ERROR -> {
                statusLabel.setText("Couldn't connect to weather API.");
                clearFields();
            }
            case NOT_OKAY -> {
                statusLabel.setText("Something went wrong! AAAAA");
                clearFields();
            }
        }
    }

    /**
     * Instantializes empty weather report fields.
     */
    public void clearFields() {
        feelsLikeBox.setContentText("");
        tempBox.setContentText("");
        conditionsBox.setContentText("");
        humidityBox.setContentText("");
        pressureBox.setContentText("");
        windBox.setContentText("");
        sunriseBox.setContentText("");
        sunsetBox.setContentText("");
        titleLabel.setText("JocularWeather");
    }

    @Override
    public void start(Stage stage) {
        scene.getStylesheets().add("style.css");
        titleLabel.setId("title-label");

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

            // Add box if and only if it is valid
            Optional<LocationBox> box = formatLocationBox(zip, report);
            box.ifPresent(savedLocationBoxes::add);
        }

        // Put boxes into group
        locationsGroup.setSpacing(10);
        locationsGroup.getChildren().addAll(savedLocationBoxes);

        // Configure prompts
        leftSearchField.setPromptText("Enter ZIP...");
        leftSearchField.setOnAction((e) -> addLocation(leftSearchField.getText()));
        leftSearchButton.setOnAction((e) -> addLocation(leftSearchField.getText()));

        rightSearchField.setPromptText("Enter location...");
        rightSearchField.setOnAction((e) -> formatReport(fetcher.getWeatherReport(rightSearchField.getText())));
        rightSearchButton.setOnAction((e) -> formatReport(fetcher.getWeatherReport(rightSearchField.getText())));
        jokeButton.setOnAction((e) -> jokeLabel.setText(Fetcher.getWeatherJoke()));

        // Assign buttons and fields to style classes
        leftSearchButton.getStyleClass().add("left-button");
        rightSearchButton.getStyleClass().add("button");
        jokeButton.getStyleClass().add("button");
        leftSearchField.getStyleClass().add("search");
        rightSearchField.getStyleClass().add("search");

        // Adjust spacing for pane content
        leftSearchButton.setMinWidth(60);
        leftSearchGroup.setSpacing(20);
        leftSearchGroup.setPrefWidth(220);
        rightSearchGroup.setSpacing(20);

        contentGroup.setHgap(20);
        contentGroup.setPadding(new Insets(10));
        jokeGroup.setSpacing(10);

        // Align everything in right pane and push together
        leftSearchGroup.setAlignment(Pos.CENTER);
        rightSearchGroup.setAlignment(Pos.CENTER);
        contentGroup.setAlignment(Pos.CENTER);
        contentBox.setAlignment(Pos.CENTER);
        jokeGroup.setAlignment(Pos.CENTER);

        // Configure joke text wrapping
        jokeLabel.setPrefSize(400, 80);
        jokeLabel.setTextOverrun(OverrunStyle.CLIP);
        jokeLabel.setWrapText(true);

        // Set spacing for pane parent objects
        leftPane.setPadding(new Insets(30));
        leftPane.setSpacing(10);
        leftPane.setPrefWidth(280);
        leftPane.setMinWidth(280);
        leftPane.setAlignment(Pos.TOP_LEFT);
        leftPane.getStyleClass().add("left-pane");

        rightPane.setPadding(new Insets(30));
        rightPane.setSpacing(10);
        rightPane.setPrefWidth(500);
        rightPane.setAlignment(Pos.TOP_CENTER);
        HBox.setHgrow(rightPane, Priority.ALWAYS);

        leftPane.getChildren().addAll(leftSearchGroup, leftLabel, locationsGroup);
        rightPane.getChildren().addAll(titleLabel, rightSearchGroup, statusLabel, contentBox, jokeGroup);

        // Assemble entire scene
        allContent.getStyleClass().add("hbox");
        allContent.getChildren().addAll(leftPane, rightPane);
        HBox.setHgrow(allContent, Priority.ALWAYS);

        stage.getIcons().add(new Image("/ico.png"));
        stage.setTitle("JocularWeather");
        stage.setScene(scene);
        stage.show();
    }

    /**
     * Returns an optional {@link org.nothing.jocularweather.LocationBox} given a
     * specific location and its
     * corresponding weather report.
     *
     * @param zip    5 digit United States ZIP code
     * @param report Report object
     * @return LocationBox associated location box
     */
    private Optional<LocationBox> formatLocationBox(String zip, ReportBase report) {
        LocationBox box;
        switch (report.type()) {
            case OKAY -> {
                leftLabel.setText("");
                // Set up correctly formatted weather
                Report weather = (Report) report;
                box = new LocationBox(String.format("%s, %s", weather.name(), weather.sys().country()), zip);
                box.setCondition(weather.weather().get(0).main());
                box.setTemperature(String.format("%d°F", Math.round(weather.main().temp())));

                // Set updater for bringing saved location into focus
                box.setOnMouseClicked((e) -> formatReport(fetcher.getWeatherReport(box.getZip())));
            }
            case API_ERROR -> {
                leftLabel.setText("Couldn't connect to API");
                return Optional.empty();
            }
            case LOCATION_NOT_FOUND -> {
                leftLabel.setText("Couldn't find location: " + zip);
                return Optional.empty();
            }
            default -> {
                leftLabel.setText("Something went wrong!");
                return Optional.empty();
            }
        }

        // Configure delete action
        box.getDeleteButton().setOnAction((e) -> {
            fetcher.removeZipFromSaved(zip);
            savedLocationBoxes.remove(box);

            locationsGroup.getChildren().clear();
            locationsGroup.getChildren().addAll(savedLocationBoxes);
        });

        return Optional.of(box);
    }

    /**
     * Add location to group of location boxes.
     *
     * @param zip 5 digit United States ZIP code
     */
    private void addLocation(String zip) {
        if (fetcher.addZipToSaved(zip)) {
            leftLabel.setText("");

            // Add location box if valid
            Optional<LocationBox> box = formatLocationBox(zip, fetcher.getWeatherReport(zip));
            box.ifPresent(savedLocationBoxes::add);

            // Refresh locations
            locationsGroup.getChildren().clear();
            locationsGroup.getChildren().addAll(savedLocationBoxes);
        } else {
            leftLabel.setText("Not a valid ZIP code. Please try again.");
        }
    }
}