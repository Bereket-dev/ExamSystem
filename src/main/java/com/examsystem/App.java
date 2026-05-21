package com.examsystem;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Main entry point for the ExamSystem JavaFX application.
 * This is Phase 1 - Project Setup
 */
public class App extends Application {
    private static final Logger logger = LoggerFactory.getLogger(App.class);

    @Override
    public void start(Stage primaryStage) {
        try {
            logger.info("Starting ExamSystem Application...");

            // Temporary UI for Phase 1
            StackPane root = new StackPane();
            Label welcomeLabel = new Label("ExamSystem - Phase 1 Setup Complete");
            welcomeLabel.setStyle("-fx-font-size: 18;");
            root.getChildren().add(welcomeLabel);

            Scene scene = new Scene(root, 800, 600);
            primaryStage.setTitle("ExamSystem");
            primaryStage.setScene(scene);
            primaryStage.show();

            logger.info("Application started successfully");
        } catch (Exception e) {
            logger.error("Error starting application", e);
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
