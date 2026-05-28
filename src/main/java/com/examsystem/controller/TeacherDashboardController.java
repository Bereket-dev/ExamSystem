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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import com.examsystem.util.UiManager;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.stage.Stage;

import java.util.List;
import java.util.Optional;

public class TeacherDashboardController {
    private static final Logger logger = LoggerFactory.getLogger(TeacherDashboardController.class);

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
            private final HBox row = new HBox(10);
            private final Label examLabel = new Label();
            private final HBox actionBox = new HBox(8);
            private final Button editButton = new Button("Edit Exam");
            private final Button deleteButton = new Button("Delete Exam");
            private final Button addQuestionButton = new Button("Add Question");
            private final Button assignButton = new Button("Assign Students");
            private final Region spacer = new Region();

            {
                addQuestionButton.getStyleClass().add("btn-secondary");
                assignButton.getStyleClass().add("btn-purple");
                editButton.getStyleClass().add("btn-secondary");
                deleteButton.getStyleClass().add("btn-danger");
                actionBox.getChildren().addAll(addQuestionButton, assignButton, editButton, deleteButton);
                actionBox.setVisible(false);
                actionBox.setManaged(false);
                row.setAlignment(Pos.CENTER_LEFT);
                HBox.setHgrow(spacer, Priority.ALWAYS);
                row.getChildren().addAll(examLabel, spacer, actionBox);
                editButton.setOnAction(e -> handleEditExam(getItem()));
                deleteButton.setOnAction(e -> handleDeleteExam(getItem()));
                addQuestionButton.setOnAction(e -> openQuestionManager(getItem()));
                assignButton.setOnAction(e -> openAssignStudents(getItem()));
            }

            @Override
            protected void updateItem(Exam exam, boolean empty) {
                super.updateItem(exam, empty);
                if (empty || exam == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    String published = exam.isPublished() ? "Published" : "Draft";
                    examLabel.setText(exam.getExamName() + " [" + exam.getSubject() + "] - " + published);
                    setText(null);
                    setGraphic(row);
                    actionBox.setVisible(isSelected());
                    actionBox.setManaged(isSelected());
                }
            }

            @Override
            public void updateSelected(boolean selected) {
                super.updateSelected(selected);
                actionBox.setVisible(selected);
                actionBox.setManaged(selected);
            }
        });

        createExamButton.setOnAction(
                e -> openScreen("/com/examsystem/fxml/CreateExam.fxml", "Create Exam", CreateExamController.class));
        monitoringButton.setOnAction(
                e -> openScreen("/com/examsystem/fxml/Monitoring.fxml", "Live Monitoring", MonitoringController.class));
        reportsButton
                .setOnAction(e -> openScreen("/com/examsystem/fxml/Reports.fxml", "Reports", ReportsController.class));
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

    private void openQuestionManager(Exam selected) {
        if (selected == null) {
            setStatus("Select an exam first to manage questions.");
            return;
        }
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/examsystem/fxml/QuestionManager.fxml"));
            Parent root = loader.load();
            QuestionManagerController controller = loader.getController();
            controller.setExam(selected);

            Stage stage = (Stage) createExamButton.getScene().getWindow();
            UiManager.navigateToApp(stage, root, "Question Manager - " + selected.getExamName());
        } catch (Exception e) {
            setStatus("Unable to open question manager: " + e.getMessage());
        }
    }

    private void openAssignStudents(Exam selected) {
        if (selected == null) {
            setStatus("Select an exam first to assign students.");
            return;
        }
        if (currentTeacher == null) {
            setStatus("Teacher profile is still loading. Please wait and try again.");
            return;
        }
        try {
            Exam exam = teacherService.getExamsByTeacher(currentTeacher.getTeacherId()).stream()
                    .filter(e -> e.getExamId() == selected.getExamId())
                    .findFirst()
                    .orElse(selected);

            var fxmlUrl = getClass().getResource("/com/examsystem/fxml/AssignStudents.fxml");
            if (fxmlUrl == null) {
                setStatus("Assign Students screen resource not found.");
                return;
            }
            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            Parent root = loader.load();
            AssignStudentsController controller = loader.getController();
            controller.setTeacherContext(currentTeacher, currentUser);
            controller.setExam(exam);

            Stage stage = (Stage) createExamButton.getScene().getWindow();
            UiManager.navigateToApp(stage, root, "Assign Students - " + exam.getExamName());
        } catch (Exception e) {
            logger.error("Failed to open assign students screen", e);
            setStatus("Unable to open assignment screen: " + e.getMessage());
        }
    }

    private void handleEditExam(Exam selected) {
        if (selected == null) {
            setStatus("Select an exam to edit.");
            return;
        }
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/examsystem/fxml/CreateExam.fxml"));
            Parent root = loader.load();
            CreateExamController controller = loader.getController();
            controller.setTeacherContext(currentTeacher, currentUser);
            controller.setExam(selected);

            Stage stage = (Stage) createExamButton.getScene().getWindow();
            UiManager.navigateToApp(stage, root, "Edit Exam - " + selected.getExamName());
        } catch (Exception e) {
            setStatus("Unable to open edit form: " + e.getMessage());
        }
    }

    private void handleDeleteExam(Exam selected) {
        if (selected == null) {
            setStatus("Select an exam to delete.");
            return;
        }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setHeaderText("Delete exam?");
        confirm.setContentText("This removes exam '" + selected.getExamName() + "' and related questions/attempts.");
        var result = confirm.showAndWait();
        if (result.isEmpty() || result.get().getButtonData().isCancelButton()) {
            return;
        }
        try {
            teacherService.deleteExam(currentTeacher.getTeacherId(), selected.getExamId());
            setStatus("Exam deleted.");
            loadDashboard();
        } catch (Exception e) {
            setStatus("Delete failed: " + e.getMessage());
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
            UiManager.navigateToApp(stage, root, title + " - ExamSystem");
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
            UiManager.navigateToLogin(stage, root);
        } catch (Exception e) {
            setStatus("Logout failed: " + e.getMessage());
        }
    }

    private void setStatus(String message) {
        statusLabel.setText(message);
    }
}
