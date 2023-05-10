package org.nothing.jocularweather;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;

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

    public static void main(String[] args) {
        launch();
    }

    @Override
    public void start(Stage stage) {
        String report = getWeatherReport("65622", "imperial");
        Report processedReport;

        System.out.println(report);

        ObjectMapper mapper = new ObjectMapper();
        mapper.setPropertyNamingStrategy(new PropertyNamingStrategies.SnakeCaseStrategy());

        try {
            processedReport = mapper.readValue(report, Report.class);

            double feelsLike = processedReport.main().feels_like();
            double temp = processedReport.main().temp();
            String conditions = processedReport.weather().get(0).main();
            String icon = processedReport.weather().get(0).icon();
            long sunrise = processedReport.sys().sunrise();
            long sunset = processedReport.sys().sunset();

            System.out.println("feels like: " + feelsLike);
            System.out.println("temp: " + temp);
            System.out.println("conditions: " + conditions);
            System.out.println("icon: " + icon);
            System.out.println("sunrise: " + sunrise);
            System.out.println("sunset: " + sunset);

            pushToDB("27560");
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            throw new RuntimeException("nope the report is not set up correctly");
        }

        // TODO add this
        var leftPane = new AnchorPane();

        var titleLabel = new Label("JocularWeather");

        var rightPane = new GridPane();

        var contentPane = new FlowPane();

        var label = new Label(report);
        var scene = new Scene(new StackPane(label, new InfoBox("fart", new Label("poop"))), 640, 480);
        stage.setScene(scene);
        stage.show();
    }
}