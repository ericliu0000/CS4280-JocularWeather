package org.openjfx;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class Main extends Application {
    @Override
    public void start(Stage stage) {
        ArrayList<String> constants = new ArrayList<>();

        try {
            constants = (ArrayList<String>) Files.readAllLines(Path.of(".env"));
        } catch (FileNotFoundException e) {
            System.out.println("brr");
        } catch (IOException e) {
            System.out.println("Eeeee");
        }

        // TODO make this actually work

        var label = new Label("a");
        var scene = new Scene(new StackPane(label), 640, 480);
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }

}