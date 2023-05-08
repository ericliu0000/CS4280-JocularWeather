package org.openjfx;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
// import HttpUrlConnection

public class Main extends Application {

    static String API_KEY = getEnv("API_KEY");
    static String BASE_URL = getEnv("BASE_URL");
    static String ICON_URL = getEnv("ICON_URL");

    @Override
    public void start(Stage stage) {

        String report = getWeatherReport("27560", "imperial");
        System.out.println(report);
        var label = new Label(API_KEY);
        var scene = new Scene(new StackPane(label), 640, 480);
        stage.setScene(scene);
        stage.show();
    }

    public static String getEnv(String key) {
        ArrayList<String> constants = new ArrayList<>();

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
        try {
            String combinedURL = BASE_URL + "?appid=" + API_KEY + "&zip=" + zipCode + "&units=" + units;
            System.out.println(combinedURL);
            URL url = new URL(combinedURL);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();

            con.setRequestMethod("GET");

            int status = con.getResponseCode();

            BufferedReader in = new BufferedReader(
                    new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuffer content = new StringBuffer();
            while ((inputLine = in.readLine()) != null) {
                content.append(inputLine);
            }
            in.close();

            return content.toString();

        } catch (Exception e) {
            System.out.println(e);
        }

        return null;
    }

    public static void main(String[] args) {
        launch();
    }

}