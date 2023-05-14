package org.nothing.jocularweather;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

import org.nothing.jocularweather.Logger.MessageType;

public class Main extends Application {

    private static final Label titleLabel = new Label("JocularWeather.jar.EXE");
    private static final TextField searchField = new TextField();
    private static final Button searchButton = new Button("Search");
    private static final HBox searchGroup = new HBox(searchField, searchButton);
    private static final Label locationLabel = new Label("");
    // private static final ImageView conditionsIcon = new ImageView(new
    // Image("icons/01n.png"));
    private static final InfoBox feelsLikeBox = new InfoBox("Feels like", "");
    private static final InfoBox tempBox = new InfoBox("Current temperature", "");
    private static final InfoBox conditionsBox = new InfoBox("Conditions", ""); // , conditionsIcon); //
    private static final InfoBox sunriseBox = new InfoBox("Sunrise", "");
    private static final InfoBox sunsetBox = new InfoBox("Sunset", "");
    private static final FlowPane contentGroup = new FlowPane(feelsLikeBox, tempBox, conditionsBox, sunriseBox,
            sunsetBox);
    private static final VBox contentBox = new VBox(locationLabel, contentGroup);
    private static final Button jokeButton = new Button("Get Joke");
    private static final Label jokeLabel = new Label();
    private static final VBox jokeGroup = new VBox(jokeButton, jokeLabel);
    private static final VBox rightPane = new VBox();

    private static final VBox locationsGroup = new VBox();
    private static final VBox leftPane = new VBox();

    private final Fetcher fetcher = new Fetcher();

    /**
     * Returns formatted time from Unix timestamp
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

    public static void main(String[] args) {
        launch();
    }

    /**
     * Returns random weather joke
     *
     * @return String containing pure humor
     */
    public String getWeatherJoke() {
        // TODO extract this to Fetcher
        try {
            List<String> jokes = Files.readAllLines(Paths.get("src/main/resources/weather-jokes.txt"));
            return jokes.get((int) (Math.random() * jokes.size()));
        } catch (IOException e) {
            e.printStackTrace();
            return "No joke available :(";
        }
    }

    /**
     * Formats weather report elements in main frame given Report object.
     *
     * @param report Populated weather report
     */
    public void formatReport(Report report) {
        // Pull required fields from weather report
        String cityName = report.name();
        String country = report.sys().country();
        double feelsLike = report.main().feels_like();
        double temp = report.main().temp();
        long timeZone = report.timezone();
        String conditions = report.weather().get(0).main();
        String sunriseTime = getHourlyTime(report.sys().sunrise(), timeZone);
        String sunsetTime = getHourlyTime(report.sys().sunset(), timeZone);

        feelsLikeBox.setContentText(String.valueOf(feelsLike));
        tempBox.setContentText(String.valueOf(temp));
        conditionsBox.setContentText(conditions);
        sunriseBox.setContentText(sunriseTime);
        sunsetBox.setContentText(sunsetTime);

        locationLabel.setText(String.format("Current Location: %s, %s", cityName, country));
    }

    @Override
    public void start(Stage stage) {
        Logger.print(MessageType.JW_INFO, "Starting JocularWeather.jar.EXE");
        // Pull saved locations from database and get current report
        ArrayList<String> savedLocations = Fetcher.getSavedLocations();
        ArrayList<LocationBox> savedLocationBoxes = new ArrayList<>();

        Logger.print(MessageType.JW_INFO, "Requesting weather reports for saved locations");

        TreeMap<String, Report> savedLocationReports = fetcher.getWeatherReports(savedLocations);
        formatReport(fetcher.getWeatherReport(fetcher.getCurrentCity()));

        // Testing: print out reports
        Logger.print(MessageType.JW_INFO, "Rendering saved locations");
        for (Map.Entry<String, Report> entry : savedLocationReports.entrySet()) {
            Report report = entry.getValue();
            Logger.print(MessageType.JW_INFO, report.toString());

            LocationBox box = new LocationBox(String.format("%s, %s", report.name(), report.sys().country()),
                    entry.getKey());
            box.setCondition(report.weather().get(0).main());
            box.setTemperature(Math.round(report.main().temp()));

            // Set updater for clicking on it
            box.setOnMouseClicked((e) -> formatReport(fetcher.getWeatherReport(box.getZip())));

            savedLocationBoxes.add(box);
        }

        // Put boxes into group
        locationsGroup.getChildren().addAll(savedLocationBoxes);

        // Configure prompts
        searchField.setPromptText("Enter location...");
        searchField.setOnAction((e) -> formatReport(fetcher.getWeatherReport(searchField.getText())));
        searchButton.setOnAction((e) -> formatReport(fetcher.getWeatherReport(searchField.getText())));
        jokeButton.setOnAction((e) -> jokeLabel.setText(getWeatherJoke()));

        // Adjust spacing for everything
        searchGroup.setSpacing(20);
        contentGroup.setPrefWidth(400);
        contentGroup.setHgap(20);
        contentGroup.setPadding(new Insets(20));
        rightPane.setSpacing(10);

        // Align everything in right pane and push together
        searchGroup.setAlignment(Pos.CENTER);
        contentGroup.setAlignment(Pos.CENTER);
        contentBox.setAlignment(Pos.CENTER);
        jokeGroup.setAlignment(Pos.CENTER);
        rightPane.setAlignment(Pos.CENTER);

        // TODO add zip add search bar
        leftPane.getChildren().addAll(savedLocationBoxes);
        rightPane.getChildren().addAll(titleLabel, searchGroup, locationLabel, contentBox, jokeGroup);

        // create the scene
        Scene scene = new Scene(new HBox(leftPane, rightPane), 640, 480);
        scene.getStylesheets().add("style.css"); // add the CSS

        // set the stage and scene, and show the stage
        stage.setScene(scene);
        stage.show();
    }

}