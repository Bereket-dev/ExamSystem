package com.examsystem;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.Parent;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Main entry point for the ExamSystem JavaFX application.
 * Phase 3 - Authentication System
 * Loads the login screen for user authentication.
 */
public class App extends Application {
    private static final Logger logger = LoggerFactory.getLogger(App.class);

    @Override
    public void start(Stage primaryStage) {
        try {
            logger.info("Starting ExamSystem Application - Phase 3...");

            // Load login FXML
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/examsystem/fxml/login.fxml"));
            Parent root = loader.load();

            Scene scene = new Scene(root, 800, 600);
            primaryStage.setTitle("ExamSystem - Login");
            primaryStage.setScene(scene);
            primaryStage.show();

            logger.info("Application started successfully with login screen");
        } catch (Exception e) {
            logger.error("Error starting application", e);
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
