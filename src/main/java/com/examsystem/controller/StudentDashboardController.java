package com.examsystem.controller;

import com.examsystem.model.AssignedExam;
import com.examsystem.model.Exam;
import com.examsystem.model.Student;
import com.examsystem.model.User;
import com.examsystem.network.NetworkManager;
import com.examsystem.rmi.RMIManager;
import com.examsystem.service.StudentService;
import com.examsystem.util.Session;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.stage.Stage;

import java.util.List;
import java.util.Optional;

public class StudentDashboardController {
    @FXML
    private Label welcomeLabel;

    @FXML
    private ListView<AssignedExam> examListView;

    @FXML
    private Label examNameLabel;

    @FXML
    private Label examSubjectLabel;

    @FXML
    private Label examDateLabel;

    @FXML
    private Label examDurationLabel;

    @FXML
    private Label examStatusLabel;

    @FXML
    private Label examDescriptionLabel;

    @FXML
    private Label statusLabel;

    @FXML
    private Button startExamButton;

    @FXML
    private Button logoutButton;

    private final StudentService studentService = new StudentService();
    private User currentUser;
    private Student currentStudent;
    private ObservableList<AssignedExam> assignedExams = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        examListView.setItems(assignedExams);
        examListView.setCellFactory(listView -> new ListCell<>() {
            @Override
            protected void updateItem(AssignedExam item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    Exam exam = item.getExam();
                    String status = item.isAttempted() ? " [Completed]" : " [Available]";
                    setText(String.format("%s [%s] - %s%s", exam.getExamName(), exam.getSubject(),
                            exam.getExamDate() == null ? "TBD" : exam.getExamDate(), status));
                }
            }
        });

        examListView.getSelectionModel().selectedItemProperty().addListener((obs, oldExam, newExam) -> {
            if (newExam != null) {
                updateExamDetails(newExam);
                statusLabel.setText("");
            }
        });

        startExamButton.setOnAction(event -> handleStartExam());
        logoutButton.setOnAction(event -> handleLogout());
    }

    public void setUser(User user) {
        this.currentUser = user;
        if (welcomeLabel != null) {
            welcomeLabel.setText("Welcome, " + currentUser.getFullName());
        }
        loadDashboard();
    }

    private void loadDashboard() {
        assignedExams.clear();
        if (currentUser == null) {
            setStatus("Student information is not available.");
            return;
        }

        Optional<Student> studentOptional = studentService.findByUserId(currentUser.getUserId());
        if (studentOptional.isEmpty()) {
            setStatus("Student profile not found for user.");
            return;
        }

        currentStudent = studentOptional.get();
        List<AssignedExam> exams = studentService.getAssignedExamsWithStatus(currentStudent.getStudentId());
        assignedExams.addAll(exams);

        if (exams.isEmpty()) {
            setStatus("No assigned exams available right now.");
        } else {
            examListView.getSelectionModel().selectFirst();
        }
    }

    private void updateExamDetails(AssignedExam assignedExam) {
        Exam exam = assignedExam.getExam();
        examNameLabel.setText(exam.getExamName());
        examSubjectLabel.setText("Subject: " + exam.getSubject());
        examDateLabel.setText("Date: " + (exam.getExamDate() == null ? "TBD" : exam.getExamDate().toString()));
        examDurationLabel.setText("Duration: " + exam.getDurationMinutes() + " minutes");
        examDescriptionLabel
                .setText(exam.getDescription() == null ? "No description provided." : exam.getDescription());
        String publishStatus = exam.isPublished() ? "Published" : "Draft";
        String attemptStatus = assignedExam.isAttempted() ? "Completed" : "Not started";
        examStatusLabel.setText("Status: " + publishStatus + " | Attempt: " + attemptStatus);
        startExamButton.setDisable(assignedExam.isAttempted());
    }

    private void handleStartExam() {
        AssignedExam selected = examListView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            setStatus("Please select an exam to start.");
            return;
        }

        if (selected.isAttempted()) {
            setStatus("You have already completed this exam.");
            return;
        }

        openExamScreen(selected.getExam(), selected.getAssignmentId());
    }

    private void openExamScreen(Exam exam, int assignmentId) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/examsystem/fxml/ExamScreen.fxml"));
            Parent root = loader.load();
            ExamScreenController controller = loader.getController();
            controller.startExam(exam, assignmentId);

            Stage stage = (Stage) startExamButton.getScene().getWindow();
            stage.setScene(new Scene(root, 900, 650));
            stage.setTitle("Exam: " + exam.getExamName());
        } catch (Exception e) {
            setStatus("Unable to open exam screen: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void handleLogout() {
        try {
            NetworkManager.getInstance().disconnectClient();
            RMIManager.getInstance().disconnectClient();
            Session.getInstance().logout();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/examsystem/fxml/login.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) logoutButton.getScene().getWindow();
            stage.setScene(new Scene(root, 800, 600));
            stage.setTitle("ExamSystem - Login");
        } catch (Exception e) {
            setStatus("Unable to return to login screen: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void setStatus(String message) {
        if (statusLabel != null) {
            statusLabel.setText(message);
        }
    }
}
