package com.examsystem.controller;

import com.examsystem.model.ExamMonitoringEntry;
import com.examsystem.model.Teacher;
import com.examsystem.model.User;
import com.examsystem.network.NetworkManager;
import com.examsystem.rmi.RMIManager;
import com.examsystem.rmi.remote.MonitoringSummary;
import com.examsystem.service.TeacherService;
import com.examsystem.util.BackgroundLoader;
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

import java.time.format.DateTimeFormatter;

public class MonitoringController implements TeacherScreen {
    @FXML
    private ListView<ExamMonitoringEntry> monitoringListView;

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
        monitoringListView.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(ExamMonitoringEntry entry, boolean empty) {
                super.updateItem(entry, empty);
                if (empty || entry == null) {
                    setText(null);
                } else {
                    String start = entry.getStartTime() == null ? "N/A"
                            : entry.getStartTime().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
                    setText(entry.getStudentName() + " - " + entry.getExamName()
                            + " [" + entry.getSubmissionStatus() + "] since " + start);
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
        statusLabel.setText("Loading monitoring data...");
        BackgroundLoader.load(
                () -> {
                    var entries = teacherService.getActiveMonitoring(teacher.getTeacherId());
                    int tcpClients = NetworkManager.getInstance().getActiveClientCount();
                    MonitoringSummary rmiSummary = RMIManager.getInstance().getMonitoringSummary(teacher.getTeacherId());
                    return new MonitoringData(entries, tcpClients, rmiSummary);
                },
                data -> {
                    monitoringListView.setItems(FXCollections.observableArrayList(data.entries()));
                    String base = data.entries().isEmpty()
                            ? "No active exam attempts."
                            : data.entries().size() + " active attempt(s).";
                    statusLabel.setText(base + " | TCP clients: " + data.tcpClients()
                            + " | RMI active/submitted: " + data.rmiSummary().getActiveAttemptCount()
                            + "/" + data.rmiSummary().getSubmittedReportCount());
                },
                error -> statusLabel.setText("Failed to load: " + error.getMessage()));
    }

    private record MonitoringData(
            java.util.List<ExamMonitoringEntry> entries,
            int tcpClients,
            MonitoringSummary rmiSummary) {
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
