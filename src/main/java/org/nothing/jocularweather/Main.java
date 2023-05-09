package org.nothing.jocularweather;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Label;
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
// import HttpUrlConnection

public class Main extends Application {

    static String API_KEY = getEnv("API_KEY");
    static String BASE_URL = getEnv("BASE_URL");
    static String ICON_URL = getEnv("ICON_URL");

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
            System.out.println(constants.get(0));
        } catch (FileNotFoundException e) {
            System.out.println("brr");
        } catch (IOException e) {
            System.out.println("Eeeee");
        }

        return "";
    }

    public static String getWeatherReport(String zipCode, String units) {
        String combinedURL = BASE_URL + "?appid=" + API_KEY + "&zip=" + zipCode + "&units=" + units;
        System.out.println(combinedURL);
        try {
            HttpURLConnection connection = (HttpURLConnection) new URI(combinedURL).toURL().openConnection();
            connection.setRequestMethod("GET");

            int status = connection.getResponseCode();

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

    public static void main(String[] args) {
        launch();
    }

    @Override
    public void start(Stage stage) {
        String report = getWeatherReport("27560", "imperial");
        System.out.println(report);
        ObjectMapper mapper = new ObjectMapper();
        try {
            System.out.println("Created mapper");
            Report processedReport = mapper.readValue(report, Report.class);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        var label = new Label(API_KEY);
        var scene = new Scene(new StackPane(label), 640, 480);
        stage.setScene(scene);
        stage.show();
    }
}