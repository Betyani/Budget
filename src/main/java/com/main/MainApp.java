package com.main;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.util.Objects;

public class MainApp extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {
        Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/views/calendar.fxml")));
        // 또는 /view/calendar.fxml 라면 경로에 맞게 수정

        stage.setScene(new Scene(root, 600, 400));
        stage.setTitle("Budget 가계부");
        stage.show();
    }

}
