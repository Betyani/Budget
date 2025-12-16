package com.main;

import com.router.Router;
import javafx.application.Application;
import javafx.stage.Stage;

public class MainApp extends Application {

    @Override
    public void start(Stage stage) {
        Router router = new Router();
        router.openCalendar(stage);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
