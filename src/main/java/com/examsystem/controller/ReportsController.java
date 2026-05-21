package com.examsystem.controller;

import com.examsystem.model.ExamReportEntry;
import com.examsystem.model.Teacher;
import com.examsystem.model.User;
import com.examsystem.service.TeacherService;
import com.examsystem.util.Session;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.stage.Stage;

public class ReportsController implements TeacherScreen {
    @FXML
    private ListView<ExamReportEntry> reportListView;

    @FXML
    private Label summaryLabel;

    @FXML
    private Label statusLabel;

    @FXML
    private Button refreshButton;

    @FXML
    private Button backButton;

    private final TeacherService teacherService = new TeacherService();
    private Teacher teacher;

    @FXML
    public void initialize() {
        reportListView.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(ExamReportEntry entry, boolean empty) {
                super.updateItem(entry, empty);
                if (empty || entry == null) {
                    setText(null);
                } else {
                    setText(String.format("%s | %s: %d/%d (%.1f%%)",
                            entry.getStudentName(),
                            entry.getExamName(),
                            entry.getMarksObtained(),
                            entry.getTotalMarks(),
                            entry.getPercentage()));
                }
            }
        });
        refreshButton.setOnAction(e -> refresh());
        backButton.setOnAction(e -> returnToDashboard());
    }

    @Override
    public void setTeacherContext(Teacher teacher, User user) {
        this.teacher = teacher;
        refresh();
    }

    private void refresh() {
        var reports = teacherService.getSubmittedReports(teacher.getTeacherId());
        reportListView.setItems(FXCollections.observableArrayList(reports));

        if (reports.isEmpty()) {
            summaryLabel.setText("No submitted exams yet.");
            statusLabel.setText("");
        } else {
            double avg = reports.stream().mapToDouble(ExamReportEntry::getPercentage).average().orElse(0);
            summaryLabel.setText(String.format("Total submissions: %d | Average score: %.1f%%", reports.size(), avg));
            statusLabel.setText("Reports generated successfully.");
        }
    }

    private void returnToDashboard() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/examsystem/fxml/TeacherDashboard.fxml"));
            Parent root = loader.load();
            TeacherDashboardController controller = loader.getController();
            controller.setUser(Session.getInstance().getCurrentUser());

            Stage stage = (Stage) backButton.getScene().getWindow();
            stage.setScene(new Scene(root, 900, 650));
            stage.setTitle("Teacher Dashboard - ExamSystem");
        } catch (Exception e) {
            statusLabel.setText("Unable to return: " + e.getMessage());
        }
    }
}
