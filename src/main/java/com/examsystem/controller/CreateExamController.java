package com.examsystem.controller;

import com.examsystem.model.Course;
import com.examsystem.model.Exam;
import com.examsystem.model.Teacher;
import com.examsystem.model.User;
import com.examsystem.service.TeacherService;
import com.examsystem.util.FormValidator;
import com.examsystem.sync.PendingChangeType;
import com.examsystem.sync.SyncManager;
import com.examsystem.util.Session;
import com.examsystem.util.UiManager;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.time.LocalTime;

public class CreateExamController implements TeacherScreen {
    @FXML
    private Label titleLabel;

    @FXML
    private TextField examNameField;

    @FXML
    private ComboBox<Course> courseComboBox;

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
    private Exam currentExam;
    private boolean editMode = false;

    @FXML
    public void initialize() {
        saveButton.setOnAction(e -> handleSave());
        backButton.setOnAction(e -> returnToDashboard());
        courseComboBox.setCellFactory(cb -> new javafx.scene.control.ListCell<>() {
            @Override
            protected void updateItem(Course course, boolean empty) {
                super.updateItem(course, empty);
                setText(empty || course == null ? null : course.getCourseName());
            }
        });
        courseComboBox.setButtonCell(courseComboBox.getCellFactory().call(null));
    }

    @Override
    public void setTeacherContext(Teacher teacher, User user) {
        this.teacher = teacher;
        refreshCourseList();
        populateFormIfEditing();
    }

    public void setExam(Exam exam) {
        this.currentExam = exam;
        this.editMode = exam != null;
        if (editMode) {
            titleLabel.setText("Edit Exam");
            saveButton.setText("Update Exam");
        }
        populateFormIfEditing();
    }

    private void populateFormIfEditing() {
        if (!editMode || teacher == null || currentExam == null) {
            return;
        }

        examNameField.setText(currentExam.getExamName());
        descriptionField.setText(currentExam.getDescription());
        durationField.setText(String.valueOf(currentExam.getDurationMinutes()));
        totalQuestionsField.setText(String.valueOf(currentExam.getTotalQuestions()));
        totalMarksField.setText(String.valueOf(currentExam.getTotalMarks()));
        passingMarksField.setText(String.valueOf(currentExam.getPassingMarks()));
        examDateField.setText(currentExam.getExamDate() == null ? "" : currentExam.getExamDate().toString());
        examTimeField.setText(currentExam.getExamTime() == null ? "" : currentExam.getExamTime().toString());
        publishCheckBox.setSelected(currentExam.isPublished());
        selectCurrentCourse();
    }

    private void selectCurrentCourse() {
        if (currentExam == null || courseComboBox.getItems().isEmpty()) {
            return;
        }

        var existingCourse = courseComboBox.getItems().stream()
                .filter(c -> c.getCourseId() == currentExam.getCourseId())
                .findFirst();
        if (existingCourse.isPresent()) {
            courseComboBox.getSelectionModel().select(existingCourse.get());
            return;
        }

        teacherService.findCourseById(currentExam.getCourseId()).ifPresent(course -> {
            if (!courseComboBox.getItems().contains(course)) {
                courseComboBox.getItems().add(0, course);
            }
            courseComboBox.getSelectionModel().select(course);
        });
    }

    private void refreshCourseList() {
        if (teacher == null) {
            return;
        }
        var courses = teacherService.getAssignedCourses(teacher.getTeacherId());
        courseComboBox.setItems(FXCollections.observableArrayList(courses));
        if (!courses.isEmpty()) {
            courseComboBox.getSelectionModel().selectFirst();
            statusLabel.setText("");
        } else {
            statusLabel.getStyleClass().removeAll("status-success");
            statusLabel.getStyleClass().add("status-error");
            statusLabel.setText("No assigned courses found. Please ask your admin to assign a course.");
        }
    }

    private void handleSave() {
        FormValidator.clearErrors(examNameField, durationField, totalQuestionsField,
                totalMarksField, passingMarksField, examDateField, examTimeField);

        FormValidator.ValidationResult validation = FormValidator.combine(
                FormValidator.required(examNameField, "Exam name"),
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

        Course selectedCourse = courseComboBox.getSelectionModel().getSelectedItem();
        if (selectedCourse == null) {
            statusLabel.getStyleClass().removeAll("status-success");
            statusLabel.getStyleClass().add("status-error");
            statusLabel.setText("Please select a course for the exam.");
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

            Exam exam = editMode ? currentExam : new Exam();
            exam.setTeacherId(teacher.getTeacherId());
            exam.setExamName(examNameField.getText().trim());
            exam.setSubject(selectedCourse.getCourseName());
            exam.setCourseId(selectedCourse.getCourseId());
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

            if (editMode) {
                teacherService.updateExam(teacher.getTeacherId(), exam);
                statusLabel.getStyleClass().removeAll("status-error");
                statusLabel.getStyleClass().add("status-success");
                statusLabel.setText("Exam updated successfully.");
            } else {
                teacherService.createExam(exam);
                statusLabel.getStyleClass().removeAll("status-error");
                statusLabel.getStyleClass().add("status-success");
                statusLabel.setText("Exam created successfully (ID: " + exam.getExamId() + ").");
            }
            SyncManager.getInstance().recordPendingChange(PendingChangeType.EXAM_EDIT);
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
