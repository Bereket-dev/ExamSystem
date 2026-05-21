package com.examsystem.controller;

import com.examsystem.model.Exam;
import com.examsystem.model.User;
import com.examsystem.util.Session;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import com.examsystem.util.UiManager;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;

public class ResultScreenController {
    @FXML
    private Label examNameLabel;

    @FXML
    private Label marksLabel;

    @FXML
    private Label totalMarksLabel;

    @FXML
    private Label statusLabel;

    @FXML
    private Button backButton;

    private Exam exam;
    private int marksObtained;

    @FXML
    public void initialize() {
        backButton.setOnAction(event -> returnToDashboard());
    }

    public void setResult(Exam exam, int marksObtained, int totalMarks) {
        this.exam = exam;
        this.marksObtained = marksObtained;
        examNameLabel.setText(exam.getExamName());
        marksLabel.setText(String.valueOf(marksObtained));
        totalMarksLabel.setText(String.valueOf(totalMarks));
        double percentage = totalMarks > 0 ? (marksObtained * 100.0) / totalMarks : 0;
        String result = percentage >= 40 ? "Passed" : "Review Required";
        statusLabel.setText(String.format("%s (%.1f%%)", result, percentage));
    }

    private void returnToDashboard() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/examsystem/fxml/StudentDashboard.fxml"));
            Parent root = loader.load();
            StudentDashboardController controller = loader.getController();
            User currentUser = Session.getInstance().getCurrentUser();
            controller.setUser(currentUser);

            Stage stage = (Stage) backButton.getScene().getWindow();
            UiManager.navigateToApp(stage, root, "Student Dashboard - ExamSystem");
            stage.setTitle("Student Dashboard - ExamSystem");
        } catch (Exception e) {
            statusLabel.setText("Unable to return: " + e.getMessage());
        }
    }
}
