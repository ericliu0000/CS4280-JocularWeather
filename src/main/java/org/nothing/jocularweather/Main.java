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

    public String getCurrentIP() {
        String ipURL = "https://98q0kalf91.execute-api.us-east-1.amazonaws.com/ip";
        try {
            HttpURLConnection connection = (HttpURLConnection) new URI(ipURL).toURL().openConnection();
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
            pushToDB(zip);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("THE REPORT IS NOT SET UP CORRECTLY!");
        }

        return processedReport;
    }

    // public void 

    @Override
    public void start(Stage stage) {
        Report report = getWeatherReport("08540");
        // get variables needed from weather report
        double feelsLike = report.main().feels_like();
        double temp = report.main().temp();
        String conditions = report.weather().get(0).main();
        String icon = report.weather().get(0).icon();
        long timeZone = report.timezone();
        String sunriseTime = getHourlyTime(report.sys().sunrise(), timeZone);
        String sunsetTime = getHourlyTime(report.sys().sunset(), timeZone);
        String country = report.sys().country();
        String city = report.name();

        var titleLabel = new Label("JocularWeather.jar.EXE");

        var searchField = new TextField();
        searchField.setPromptText("Enter ZIP Code...");
        var searchButton = new Button("Search");
        searchButton.setOnAction((e) -> {
            Report newReport = getWeatherReport(searchButton.getText());
            // getData(e.value()));
            // Report report_ = getWeatherReport(searchButton.getText());
        });
        var searchGroup = new HBox(searchField, searchButton);
        searchGroup.setAlignment(Pos.CENTER);
        
        var locationLabel = new Label(String.format("%s, %s", city, country));
        
        var feelsLikeBox = new InfoBox("feels like", String.valueOf(feelsLike));
        var tempBox = new InfoBox("temperature like", String.valueOf(temp));
        var conditionsBox = new InfoBox("conditions like", conditions);
        var sunriseBox = new InfoBox("sunrise like", sunriseTime);
        var sunsetBox = new InfoBox("sunset like", sunsetTime);
        var contentGroup = new FlowPane(feelsLikeBox, tempBox, conditionsBox, sunriseBox, sunsetBox);
        
        contentGroup.setPrefWidth(400);
        contentGroup.setAlignment(Pos.CENTER);
        contentGroup.setHgap(20);
        contentGroup.setVgap(20);
        
        var contentBox = new VBox(locationLabel, contentGroup);
        contentBox.setAlignment(Pos.CENTER);
        
        var jokeButton = new Button("Get Joke");
        var jokeLabel = new Label();
        jokeButton.setOnAction((e) -> {
            String joke = getWeatherJoke();
            jokeLabel.setText(joke);
            System.out.println(joke);
        });

        var jokeGroup = new HBox(jokeButton, jokeLabel);
        jokeGroup.setAlignment(Pos.CENTER);

        var leftPane = new AnchorPane();
        var rightPane = new VBox();
        rightPane.setAlignment(Pos.CENTER);
        rightPane.getChildren().addAll(titleLabel, searchGroup, locationLabel, contentBox, jokeGroup);
        
        var scene = new Scene(rightPane, 640, 480);
        scene.getStylesheets().add("style.css");

        stage.setScene(scene);
        stage.show();
    }
}