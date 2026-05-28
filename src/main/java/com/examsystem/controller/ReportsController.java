package com.examsystem.controller;

import com.examsystem.model.ExamReportEntry;
import com.examsystem.model.Exam;
import com.examsystem.model.Teacher;
import com.examsystem.model.User;
import com.examsystem.service.TeacherService;
import com.examsystem.util.BackgroundLoader;
import com.examsystem.util.Session;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import com.examsystem.util.UiManager;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

public class ReportsController implements TeacherScreen {
    @FXML
    private ComboBox<Exam> examFilterComboBox;

    @FXML
    private TableView<ExamReportEntry> reportTableView;

    @FXML
    private TableColumn<ExamReportEntry, String> studentNameColumn;

    @FXML
    private TableColumn<ExamReportEntry, Integer> scoreColumn;

    @FXML
    private TableColumn<ExamReportEntry, String> gradeColumn;

    @FXML
    private TableColumn<ExamReportEntry, Double> percentageColumn;

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
        studentNameColumn.setCellValueFactory(new PropertyValueFactory<>("studentName"));
        scoreColumn.setCellValueFactory(new PropertyValueFactory<>("marksObtained"));
        gradeColumn.setCellValueFactory(new PropertyValueFactory<>("grade"));
        percentageColumn.setCellValueFactory(new PropertyValueFactory<>("percentage"));
        examFilterComboBox.setCellFactory(cb -> new javafx.scene.control.ListCell<>() {
            @Override
            protected void updateItem(Exam exam, boolean empty) {
                super.updateItem(exam, empty);
                setText(empty || exam == null ? null : exam.getExamName());
            }
        });
        examFilterComboBox.setButtonCell(examFilterComboBox.getCellFactory().call(null));
        examFilterComboBox.setOnAction(e -> refresh());
        refreshButton.setOnAction(e -> refresh());
        backButton.setOnAction(e -> returnToDashboard());
    }

    @Override
    public void setTeacherContext(Teacher teacher, User user) {
        this.teacher = teacher;
        loadCoursesAndRefresh();
    }

    private void loadCoursesAndRefresh() {
        BackgroundLoader.load(
                () -> teacherService.getTeacherCourses(teacher.getTeacherId()),
                exams -> {
                    examFilterComboBox.setItems(FXCollections.observableArrayList(exams));
                    if (!exams.isEmpty()) {
                        examFilterComboBox.getSelectionModel().selectFirst();
                    }
                    refresh();
                },
                error -> statusLabel.setText("Failed to load courses: " + error.getMessage()));
    }

    private void refresh() {
        Exam selectedExam = examFilterComboBox.getSelectionModel().getSelectedItem();
        if (selectedExam == null) {
            statusLabel.setText("Select an exam to view all student results.");
            reportTableView.setItems(FXCollections.observableArrayList());
            return;
        }
        statusLabel.setText("Generating reports...");
        BackgroundLoader.load(
                () -> teacherService.getSubmittedReportsByExam(teacher.getTeacherId(), selectedExam.getExamId()),
                reports -> {
                    reportTableView.setItems(FXCollections.observableArrayList(reports));
                    if (reports.isEmpty()) {
                        summaryLabel.setText("No submitted attempts for " + selectedExam.getExamName() + ".");
                        statusLabel.setText("");
                    } else {
                        double avg = reports.stream().mapToDouble(ExamReportEntry::getPercentage).average().orElse(0);
                        summaryLabel.setText(String.format("Exam: %s | Students: %d | Avg: %.1f%%",
                                selectedExam.getExamName(), reports.size(), avg));
                        statusLabel.setText("Reports generated successfully.");
                    }
                },
                error -> statusLabel.setText("Failed to generate reports: " + error.getMessage()));
    }

    private void returnToDashboard() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/examsystem/fxml/TeacherDashboard.fxml"));
            Parent root = loader.load();
            TeacherDashboardController controller = loader.getController();
            controller.setUser(Session.getInstance().getCurrentUser());

            Stage stage = (Stage) backButton.getScene().getWindow();
            UiManager.navigateToApp(stage, root, "Teacher Dashboard - ExamSystem");
            stage.setTitle("Teacher Dashboard - ExamSystem");
        } catch (Exception e) {
            statusLabel.setText("Unable to return: " + e.getMessage());
        }
    }
}
