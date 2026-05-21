package com.examsystem.controller;

import com.examsystem.model.Exam;
import com.examsystem.model.Teacher;
import com.examsystem.model.User;
import com.examsystem.service.TeacherService;
import com.examsystem.util.FormValidator;
import com.examsystem.util.Session;
import com.examsystem.util.UiManager;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
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
        FormValidator.clearErrors(examNameField, subjectField, durationField, totalQuestionsField,
                totalMarksField, passingMarksField, examDateField, examTimeField);

        FormValidator.ValidationResult validation = FormValidator.combine(
                FormValidator.required(examNameField, "Exam name"),
                FormValidator.required(subjectField, "Subject"),
                FormValidator.positiveInteger(durationField, "Duration", true),
                FormValidator.positiveInteger(totalQuestionsField, "Total questions", true),
                FormValidator.positiveInteger(totalMarksField, "Total marks", true),
                FormValidator.positiveInteger(passingMarksField, "Passing marks", true),
                FormValidator.dateOptional(examDateField, "Exam date"),
                FormValidator.timeOptional(examTimeField, "Exam time"));

        if (!validation.isValid()) {
            FormValidator.applyResult(validation, statusLabel);
            return;
        }

        try {
            int totalMarks = Integer.parseInt(totalMarksField.getText().trim());
            int passingMarks = Integer.parseInt(passingMarksField.getText().trim());
            if (passingMarks > totalMarks) {
                FormValidator.ValidationResult fail = FormValidator.ValidationResult.fail(
                        "Passing marks cannot exceed total marks.", passingMarksField, totalMarksField);
                FormValidator.applyResult(fail, statusLabel);
                return;
            }

            Exam exam = new Exam();
            exam.setTeacherId(teacher.getTeacherId());
            exam.setExamName(examNameField.getText().trim());
            exam.setSubject(subjectField.getText().trim());
            exam.setDescription(descriptionField.getText().trim());
            exam.setDurationMinutes(Integer.parseInt(durationField.getText().trim()));
            exam.setTotalQuestions(Integer.parseInt(totalQuestionsField.getText().trim()));
            exam.setTotalMarks(totalMarks);
            exam.setPassingMarks(passingMarks);
            exam.setPublished(publishCheckBox.isSelected());

            if (!examDateField.getText().trim().isEmpty()) {
                exam.setExamDate(LocalDate.parse(examDateField.getText().trim()));
            }
            if (!examTimeField.getText().trim().isEmpty()) {
                exam.setExamTime(LocalTime.parse(examTimeField.getText().trim()));
            }

            teacherService.createExam(exam);
            statusLabel.getStyleClass().removeAll("status-error");
            statusLabel.getStyleClass().add("status-success");
            statusLabel.setText("Exam created successfully (ID: " + exam.getExamId() + ").");
        } catch (Exception e) {
            statusLabel.getStyleClass().removeAll("status-success");
            statusLabel.getStyleClass().add("status-error");
            statusLabel.setText("Error creating exam: " + e.getMessage());
        }
    }

    private void returnToDashboard() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/examsystem/fxml/TeacherDashboard.fxml"));
            Parent root = loader.load();
            TeacherDashboardController controller = loader.getController();
            controller.setUser(Session.getInstance().getCurrentUser());

            Stage stage = (Stage) backButton.getScene().getWindow();
            UiManager.navigateToApp(stage, root, "Teacher Dashboard - ExamSystem");
        } catch (Exception e) {
            statusLabel.setText("Unable to return: " + e.getMessage());
        }
    }
}
