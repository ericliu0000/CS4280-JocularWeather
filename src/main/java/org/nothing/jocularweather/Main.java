package org.nothing.jocularweather;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
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

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;

public class Main extends Application {
    private static final String API_KEY = getEnv("API_KEY");
    private static final String BASE_URL = getEnv("BASE_URL");

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

    public static String getEnv(String key) {
        ArrayList<String> constants;

        try {
            constants = (ArrayList<String>) Files.readAllLines(Path.of(".env"));
            for (String constant : constants) {
                String[] parts = constant.split("=");
                if (parts[0].equals(key)) {
                    return parts[1];
                }
            }
        } catch (FileNotFoundException e) {
            throw new RuntimeException("Environment variables not found");
        } catch (IOException e) {
            throw new RuntimeException(".env file could not be read");
        }

        return "";
    }

    public static String getWeatherReport(String zipCode, String units) {
        String combinedURL = BASE_URL + "?appid=" + API_KEY + "&zip=" + zipCode + "&units=" + units;

        if (!isNumeric(zipCode)) {
            combinedURL = BASE_URL + "?appid=" + API_KEY + "&q=" + zipCode + "&units=" + units;
        }

        try {
            HttpURLConnection connection = (HttpURLConnection) new URI(combinedURL).toURL().openConnection();
            connection.setRequestMethod("GET");

            // int status = connection.getResponseCode();

            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));

            String inputLine;

            StringBuilder content = new StringBuilder();
            while ((inputLine = in.readLine()) != null) {
                content.append(inputLine);
            }
            in.close();

            return content.toString();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public static boolean isNumeric(String str) {
        try {
            double d = Double.parseDouble(str);
            System.out.println(d);
        } catch (NumberFormatException e) {
            return false;
        }

        return true;
    }

    public static boolean isZipCode(String str) {
        return str.length() == 5 && isNumeric(str);
    }

    public static boolean pushToDB(String zipCode) {
        String useURL = "https://98q0kalf91.execute-api.us-east-1.amazonaws.com/?zip=" + zipCode;

        try {
            HttpURLConnection connection = (HttpURLConnection) new URI(useURL).toURL().openConnection();
            connection.setRequestMethod("GET");
            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));

            String inputLine;

            StringBuilder content = new StringBuilder();
            while ((inputLine = in.readLine()) != null) {
                content.append(inputLine);
            }
            in.close();

            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

    }

    public String getWeatherJoke() {
        // random shrek quote
        String origStr = null;
        try {
            origStr = Files.readString(Paths.get("src/main/resources/weather-jokes.txt"));
        } catch (IOException e) {
            System.out.println(e);
            System.exit(0);
        }

        String[] lines = origStr.split("\n");
        int selection = (int) (Math.random() * lines.length);
        return lines[selection];
    }

    public static String getHourlyTime(long unixTime, long timeZone) {
        Date myDate = new Date(unixTime * 1000);
        DateFormat format = DateFormat.getDateInstance(DateFormat.SHORT);
        String dateString = format.format(myDate);

        return dateString;
    }

    public String getCurrentCity() {
        String cityURL = "https://98q0kalf91.execute-api.us-east-1.amazonaws.com/ip";
        try {
            HttpURLConnection connection = (HttpURLConnection) new URI(cityURL).toURL().openConnection();
            connection.setRequestMethod("GET");
            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));

            String inputLine;

            StringBuilder content = new StringBuilder();
            while ((inputLine = in.readLine()) != null) {
                content.append(inputLine);
            }
            in.close();

            return content.toString();

        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    public static void main(String[] args) {
        launch();
    }

    /**
     * 
     * @param zip
     * @return
     */
    public Report getWeatherReport(String zip) {
        String report = getWeatherReport(zip, "imperial");
        Report processedReport;

        ObjectMapper mapper = new ObjectMapper();
        mapper.setPropertyNamingStrategy(new PropertyNamingStrategies.SnakeCaseStrategy());

        try {
            processedReport = mapper.readValue(report, Report.class);
            // TODO catch this and like make sure it's a zip code or something
            pushToDB(zip);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("THE REPORT IS NOT SET UP CORRECTLY!");
        }

        return processedReport;
    }

    

    public void formatReport(Report report) {
        double feelsLike = report.main().feels_like();
        double temp = report.main().temp();
        String conditions = report.weather().get(0).main();
        // String icon = report.weather().get(0).icon();
        long timeZone = report.timezone();
        String sunriseTime = getHourlyTime(report.sys().sunrise(), timeZone);
        String sunsetTime = getHourlyTime(report.sys().sunset(), timeZone);
        // String country = report.sys().country();
        // String city = report.name();

        feelsLikeBox.setContentText(String.valueOf(feelsLike));
        tempBox.setContentText(String.valueOf(temp));
        conditionsBox.setContentText(conditions);
        sunriseBox.setContentText(sunriseTime);
        sunsetBox.setContentText(sunsetTime);
    }

    @Override
    public void start(Stage stage) {
        // Report report = getWeatherReport("08540");

        // formatReport(report);
        // locationLabel.setText(String.format("%s, %s", report.name(), report.sys.country()));

        searchField.setPromptText("Enter ZIP Code...");
        searchButton.setOnAction((e) -> {
            Report report = getWeatherReport(searchField.getText());

            formatReport(report);
            locationLabel.setText(String.format("%s, %s", report.name(), report.sys().country()));
        });
        searchGroup.setAlignment(Pos.CENTER);

        contentGroup.setPrefWidth(400);
        contentGroup.setAlignment(Pos.CENTER);
        contentGroup.setHgap(20);
        contentGroup.setVgap(20);

        contentBox.setAlignment(Pos.CENTER);

        jokeButton.setOnAction((e) -> {
            jokeLabel.setText(getWeatherJoke());
        });

        jokeGroup.setAlignment(Pos.CENTER);

        rightPane.setAlignment(Pos.CENTER);
        rightPane.getChildren().addAll(titleLabel, searchGroup, locationLabel, contentBox, jokeGroup);

        System.out.println(getCurrentCity());

        var scene = new Scene(rightPane, 640, 480);
        scene.getStylesheets().add("style.css");

        stage.setScene(scene);
        stage.show();
    }
}