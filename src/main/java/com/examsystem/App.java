package com.examsystem;

import com.examsystem.network.NetworkManager;
import com.examsystem.rmi.RMIManager;
import com.examsystem.util.ThreadPoolManager;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.Parent;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Main entry point for the ExamSystem JavaFX application.
 */
public class App extends Application {
    private static final Logger logger = LoggerFactory.getLogger(App.class);

    @Override
    public void start(Stage primaryStage) {
        try {
            logger.info("Starting ExamSystem Application...");

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

    @Override
    public void stop() {
        logger.info("Shutting down ExamSystem...");
        NetworkManager.getInstance().shutdown();
        RMIManager.getInstance().shutdown();
        ThreadPoolManager.getInstance().shutdown();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
