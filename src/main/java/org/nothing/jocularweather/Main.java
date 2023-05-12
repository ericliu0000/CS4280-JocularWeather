package org.nothing.jocularweather;

import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class Main extends Application {

    private static final Label titleLabel = new Label("JocularWeather.jar.EXE");
    private static final TextField searchField = new TextField();
    private static final Button searchButton = new Button("Search");
    private static final HBox searchGroup = new HBox(searchField, searchButton);
    private static final Label locationLabel = new Label("");
    private static final InfoBox feelsLikeBox = new InfoBox("feels like", "");
    private static final InfoBox tempBox = new InfoBox("temperature like", "");
    private static final InfoBox conditionsBox = new InfoBox("conditions like", "");
    private static final InfoBox sunriseBox = new InfoBox("sunrise like", "");
    private static final InfoBox sunsetBox = new InfoBox("sunset like", "");
    private static final FlowPane contentGroup = new FlowPane(feelsLikeBox, tempBox, conditionsBox, sunriseBox, sunsetBox);
    private static final VBox contentBox = new VBox(locationLabel, contentGroup);
    private static final Button jokeButton = new Button("Get Joke");
    private static final Label jokeLabel = new Label();
    private static final VBox jokeGroup = new VBox(jokeButton, jokeLabel);
    private static final AnchorPane leftPane = new AnchorPane();
    private static final VBox rightPane = new VBox();
    private final Fetcher fetcher = new Fetcher();

    /**
     * Returns whether a string is only numbers
     *
     * @param str any string
     * @return boolean whether the string is only digits
     */
    public static boolean isNumeric(String str) {
        try {
            Double.parseDouble(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * Returns formatted time from unix timestamp
     *
     * @param unixTime time at location in seconds after Unix epoch
     * @param timeZone time zone offset in seconds from UTC
     * @return String formatted value
     */
    public static String getHourlyTime(long unixTime, long timeZone) {
        Date myDate = new Date(unixTime * 1000);
        Calendar time = Calendar.getInstance();
        time.setTime(myDate);
        DateFormat format = DateFormat.getTimeInstance(DateFormat.SHORT, Locale.UK);

        return format.format(myDate);
    }

    public static void main(String[] args) {
        launch();
    }

    /**
     * Returns random weather joke
     *
     * @return String a weather joke
     */
    public String getWeatherJoke() {
        String origStr = "No joke";

        try {
            origStr = Files.readString(Paths.get("src/main/resources/weather-jokes.txt"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        String[] lines = origStr.split("\n");
        int selection = (int) (Math.random() * lines.length);
        return lines[selection];
    }

    public void formatReport(Report report) {
        double feelsLike = report.main().feels_like();
        double temp = report.main().temp();
        String conditions = report.weather().get(0).main();
        long timeZone = report.timezone();
        String sunriseTime = getHourlyTime(report.sys().sunrise(), timeZone);
        String sunsetTime = getHourlyTime(report.sys().sunset(), timeZone);

        feelsLikeBox.setContentText(String.valueOf(feelsLike));
        tempBox.setContentText(String.valueOf(temp));
        conditionsBox.setContentText(conditions);
        sunriseBox.setContentText(sunriseTime);
        sunsetBox.setContentText(sunsetTime);
    }

    @Override
    public void start(Stage stage) {
        // formatReport(report);
        // locationLabel.setText(String.format("%s, %s", report.name(),
        // report.sys.country()));

        ArrayList<String> savedLocations = Fetcher.getSavedLocations();
        ArrayList<Report> savedLocationReports = fetcher.getWeatherReports(savedLocations);

        for (Report savedLocationReport : savedLocationReports) {
            System.out.println(savedLocationReport);
        }

        Report r = fetcher.getWeatherReport(fetcher.getCurrentCity());
        formatReport(r);

        locationLabel.setText(String.format("Current Location: %s,%s", r.name(), r.sys().country()));

        searchField.setPromptText("Enter ZIP Code...");
        searchButton.setOnAction((e) -> {
            Report report = fetcher.getWeatherReport(searchField.getText());

            formatReport(report);
            locationLabel.setText(String.format("%s, %s", report.name(), report.sys().country()));
        });
        searchGroup.setAlignment(Pos.CENTER);

        contentGroup.setPrefWidth(400);
        contentGroup.setAlignment(Pos.CENTER);
        contentGroup.setHgap(20);
        contentGroup.setVgap(20);

        contentBox.setAlignment(Pos.CENTER);

        jokeButton.setOnAction((e) -> jokeLabel.setText(getWeatherJoke()));

        jokeGroup.setAlignment(Pos.CENTER);

        rightPane.setAlignment(Pos.CENTER);
        rightPane.getChildren().addAll(titleLabel, searchGroup, locationLabel, contentBox, jokeGroup);

        System.out.println(fetcher.getCurrentCity());

        var scene = new Scene(rightPane, 640, 480);
        scene.getStylesheets().add("style.css");

        stage.setScene(scene);
        stage.show();
    }
}