package com.example.chatlesson7;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class UIClient extends Application {
    public static void main(String[] args) {
        launch();
    }

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(UIClient.class.getResource("client-ui.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 600, 400);
        scene.getRoot().setStyle("-fx-font-family: 'serif'");
        stage.setTitle("Client/Server Chat");
        stage.setScene(scene);
        stage.show();

    }
}