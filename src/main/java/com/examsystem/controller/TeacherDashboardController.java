package com.examsystem.controller;

import com.examsystem.model.Exam;
import com.examsystem.model.Teacher;
import com.examsystem.model.User;
import com.examsystem.network.NetworkManager;
import com.examsystem.rmi.RMIManager;
import com.examsystem.service.TeacherService;
import com.examsystem.util.BackgroundLoader;
import com.examsystem.util.ConfigManager;
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

public class TeacherDashboardController {
    @FXML
    private Label welcomeLabel;

    @FXML
    private ListView<Exam> examListView;

    @FXML
    private Label statusLabel;

    @FXML
    private Label networkStatusLabel;

    @FXML
    private Button createExamButton;

    @FXML
    private Button addQuestionsButton;

    @FXML
    private Button assignStudentsButton;

    @FXML
    private Button monitoringButton;

    @FXML
    private Button reportsButton;

    @FXML
    private Button logoutButton;

    private final TeacherService teacherService = new TeacherService();
    private User currentUser;
    private Teacher currentTeacher;
    private ObservableList<Exam> teacherExams = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        examListView.setItems(teacherExams);
        examListView.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Exam exam, boolean empty) {
                super.updateItem(exam, empty);
                if (empty || exam == null) {
                    setText(null);
                } else {
                    String published = exam.isPublished() ? "Published" : "Draft";
                    setText(exam.getExamName() + " [" + exam.getSubject() + "] - " + published);
                }
            }
        });

        createExamButton.setOnAction(e -> openScreen("/com/examsystem/fxml/CreateExam.fxml", "Create Exam", CreateExamController.class));
        addQuestionsButton.setOnAction(e -> openQuestionManager());
        assignStudentsButton.setOnAction(e -> openScreen("/com/examsystem/fxml/AssignStudents.fxml", "Assign Students", AssignStudentsController.class));
        monitoringButton.setOnAction(e -> openScreen("/com/examsystem/fxml/Monitoring.fxml", "Live Monitoring", MonitoringController.class));
        reportsButton.setOnAction(e -> openScreen("/com/examsystem/fxml/Reports.fxml", "Reports", ReportsController.class));
        logoutButton.setOnAction(e -> handleLogout());
    }

    public void setUser(User user) {
        this.currentUser = user;
        welcomeLabel.setText("Welcome, " + user.getFullName());
        loadDashboard();
    }

    private void loadDashboard() {
        teacherExams.clear();
        setStatus("Loading dashboard...");
        BackgroundLoader.load(
                () -> {
                    Optional<Teacher> teacherOpt = teacherService.findByUserId(currentUser.getUserId());
                    if (teacherOpt.isEmpty()) {
                        throw new IllegalStateException("Teacher profile not found.");
                    }
                    Teacher teacher = teacherOpt.get();
                    List<Exam> exams = teacherService.getExamsByTeacher(teacher.getTeacherId());
                    return new TeacherDashboardData(teacher, exams);
                },
                data -> {
                    currentTeacher = data.teacher();
                    teacherExams.setAll(data.exams());
                    if (data.exams().isEmpty()) {
                        setStatus("No exams created yet. Use Create Exam to add one.");
                    } else {
                        setStatus("");
                    }
                    startTcpServer();
                },
                error -> setStatus(error.getMessage()));
    }

    private record TeacherDashboardData(Teacher teacher, List<Exam> exams) {
    }

    private void startTcpServer() {
        try {
            NetworkManager network = NetworkManager.getInstance();
            network.startServer();
            int port = ConfigManager.getIntProperty("network.server.port", 5000);
            int clients = network.getActiveClientCount();
            boolean rmiRunning = RMIManager.getInstance().isServerRunning();
            int rmiPort = ConfigManager.getIntProperty("rmi.registry.port", 1099);
            if (networkStatusLabel != null) {
                networkStatusLabel.setText(String.format(
                        "TCP: port %d (%d clients) | RMI: port %d (%s)",
                        port, clients, rmiPort, rmiRunning ? "running" : "stopped"));
            }
        } catch (Exception e) {
            if (networkStatusLabel != null) {
                networkStatusLabel.setText("TCP Server: failed to start - " + e.getMessage());
            }
        }
    }

    public Teacher getCurrentTeacher() {
        return currentTeacher;
    }

    public Exam getSelectedExam() {
        return examListView.getSelectionModel().getSelectedItem();
    }

    private void openQuestionManager() {
        Exam selected = examListView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            setStatus("Select an exam first to manage questions.");
            return;
        }
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/examsystem/fxml/QuestionManager.fxml"));
            Parent root = loader.load();
            QuestionManagerController controller = loader.getController();
            controller.setExam(selected);

            Stage stage = (Stage) addQuestionsButton.getScene().getWindow();
            stage.setScene(new Scene(root, 900, 650));
            stage.setTitle("Question Manager - " + selected.getExamName());
        } catch (Exception e) {
            setStatus("Unable to open question manager: " + e.getMessage());
        }
    }

    private <T> void openScreen(String fxmlPath, String title, Class<T> controllerClass) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            Object controller = loader.getController();
            if (controller instanceof TeacherScreen teacherScreen) {
                teacherScreen.setTeacherContext(currentTeacher, currentUser);
            }

            Stage stage = (Stage) createExamButton.getScene().getWindow();
            stage.setScene(new Scene(root, 900, 650));
            stage.setTitle(title + " - ExamSystem");
        } catch (Exception e) {
            setStatus("Unable to open screen: " + e.getMessage());
        }
    }

    private void handleLogout() {
        try {
            NetworkManager.getInstance().stopServer();
            RMIManager.getInstance().stopServer();
            Session.getInstance().logout();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/examsystem/fxml/login.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) logoutButton.getScene().getWindow();
            stage.setScene(new Scene(root, 800, 600));
            stage.setTitle("ExamSystem - Login");
        } catch (Exception e) {
            setStatus("Logout failed: " + e.getMessage());
        }
    }

    private void setStatus(String message) {
        statusLabel.setText(message);
    }
}
