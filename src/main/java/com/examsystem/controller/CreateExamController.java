package com.examsystem.controller;

import com.examsystem.model.Exam;
import com.examsystem.model.Teacher;
import com.examsystem.model.User;
import com.examsystem.service.TeacherService;
import com.examsystem.util.Session;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.time.LocalTime;

public class CreateExamController implements TeacherScreen {
    @FXML
    private TextField examNameField;

    @FXML
    private TextField subjectField;

    @FXML
    private TextArea descriptionField;

    @FXML
    private TextField durationField;

    @FXML
    private TextField totalQuestionsField;

    @FXML
    private TextField totalMarksField;

    @FXML
    private TextField passingMarksField;

    @FXML
    private TextField examDateField;

    @FXML
    private TextField examTimeField;

    @FXML
    private CheckBox publishCheckBox;

    @FXML
    private Label statusLabel;

    @FXML
    private Button saveButton;

    @FXML
    private Button backButton;

    private final TeacherService teacherService = new TeacherService();
    private Teacher teacher;

    @FXML
    public void initialize() {
        saveButton.setOnAction(e -> handleSave());
        backButton.setOnAction(e -> returnToDashboard());
    }

    @Override
    public void setTeacherContext(Teacher teacher, User user) {
        this.teacher = teacher;
    }

    private void handleSave() {
        try {
            String name = examNameField.getText().trim();
            String subject = subjectField.getText().trim();
            if (name.isEmpty() || subject.isEmpty()) {
                statusLabel.setText("Exam name and subject are required.");
                return;
            }

            Exam exam = new Exam();
            exam.setTeacherId(teacher.getTeacherId());
            exam.setExamName(name);
            exam.setSubject(subject);
            exam.setDescription(descriptionField.getText().trim());
            exam.setDurationMinutes(parseInt(durationField.getText(), 60));
            exam.setTotalQuestions(parseInt(totalQuestionsField.getText(), 0));
            exam.setTotalMarks(parseInt(totalMarksField.getText(), 100));
            exam.setPassingMarks(parseInt(passingMarksField.getText(), 40));
            exam.setPublished(publishCheckBox.isSelected());

            if (!examDateField.getText().trim().isEmpty()) {
                exam.setExamDate(LocalDate.parse(examDateField.getText().trim()));
            }
            if (!examTimeField.getText().trim().isEmpty()) {
                exam.setExamTime(LocalTime.parse(examTimeField.getText().trim()));
            }

            teacherService.createExam(exam);
            statusLabel.setText("Exam created successfully (ID: " + exam.getExamId() + ").");
        } catch (Exception e) {
            statusLabel.setText("Error creating exam: " + e.getMessage());
        }
    }

    private int parseInt(String value, int defaultValue) {
        if (value == null || value.trim().isEmpty()) {
            return defaultValue;
        }
        return Integer.parseInt(value.trim());
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
