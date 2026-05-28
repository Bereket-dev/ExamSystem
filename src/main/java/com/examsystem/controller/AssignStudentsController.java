package com.examsystem.controller;

import com.examsystem.model.Exam;
import com.examsystem.model.Student;
import com.examsystem.model.Teacher;
import com.examsystem.model.User;
import com.examsystem.service.TeacherService;
import com.examsystem.util.FormValidator;
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
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AssignStudentsController implements TeacherScreen {
    @FXML
    private ComboBox<Exam> examComboBox;

    @FXML
    private VBox studentCheckboxContainer;

    @FXML
    private Label statusLabel;

    @FXML
    private Button assignButton;

    @FXML
    private Button backButton;

    private final TeacherService teacherService = new TeacherService();
    private Teacher teacher;
    private Exam selectedExam;
    private final Map<CheckBox, Student> studentMap = new HashMap<>();

    @FXML
    public void initialize() {
        examComboBox.setCellFactory(cb -> new javafx.scene.control.ListCell<>() {
            @Override
            protected void updateItem(Exam exam, boolean empty) {
                super.updateItem(exam, empty);
                setText(empty || exam == null ? null : exam.getExamName());
            }
        });
        examComboBox.setButtonCell(examComboBox.getCellFactory().call(null));
        examComboBox.setOnAction(e -> refreshStudents());

        assignButton.setOnAction(e -> handleAssign());
        backButton.setOnAction(e -> returnToDashboard());
    }

    @Override
    public void setTeacherContext(Teacher teacher, User user) {
        this.teacher = teacher;
        refreshExamList();
        refreshStudents();
    }

    public void setExam(Exam exam) {
        this.selectedExam = exam;
        if (examComboBox != null) {
            if (teacher != null) {
                refreshExamList();
            }
            examComboBox.getSelectionModel().select(exam);
            examComboBox.setDisable(true);
        }
    }

    private void refreshExamList() {
        if (teacher == null) {
            return;
        }
        List<Exam> exams = teacherService.getExamsByTeacher(teacher.getTeacherId());
        if (selectedExam != null && exams.stream().noneMatch(e -> e.getExamId() == selectedExam.getExamId())) {
            exams.add(0, selectedExam);
        }
        examComboBox.setItems(FXCollections.observableArrayList(exams));
        if (selectedExam != null) {
            Exam matching = exams.stream()
                    .filter(e -> e.getExamId() == selectedExam.getExamId())
                    .findFirst()
                    .orElse(null);
            if (matching != null) {
                examComboBox.getSelectionModel().select(matching);
            }
            examComboBox.setDisable(true);
        } else if (!exams.isEmpty()) {
            examComboBox.getSelectionModel().selectFirst();
        }
    }

    private void refreshStudents() {
        studentCheckboxContainer.getChildren().clear();
        studentMap.clear();
        Exam exam = examComboBox.getSelectionModel().getSelectedItem();
        List<Student> students = teacherService.getAllStudents();
        for (Student student : students) {
            CheckBox checkBox = new CheckBox(teacherService.getStudentDisplayName(student));
            if (exam != null && teacherService.isStudentAssignedToExam(student.getStudentId(), exam.getExamId())) {
                checkBox.setSelected(true);
                checkBox.setStyle("-fx-opacity: 0.8;");
            }
            studentMap.put(checkBox, student);
            studentCheckboxContainer.getChildren().add(checkBox);
        }
    }

    private void handleAssign() {
        statusLabel.setText("");
        if (examComboBox.getSelectionModel().getSelectedItem() == null) {
            statusLabel.setText("Please select an exam.");
            return;
        }
        List<Student> selectedStudents = new ArrayList<>();
        for (Map.Entry<CheckBox, Student> entry : studentMap.entrySet()) {
            if (entry.getKey().isSelected()) {
                selectedStudents.add(entry.getValue());
            }
        }
        if (selectedStudents.isEmpty()) {
            statusLabel.setText("Please select at least one student to assign.");
            return;
        }

        Exam exam = examComboBox.getSelectionModel().getSelectedItem();
        try {
            for (Student student : selectedStudents) {
                teacherService.assignExamToStudent(exam.getExamId(), student.getStudentId());
            }
            if (!exam.isPublished()) {
                teacherService.publishExam(exam.getExamId(), true);
            }
            statusLabel.getStyleClass().removeAll("status-error");
            statusLabel.getStyleClass().add("status-success");
            statusLabel.setText("Assigned exam to " + selectedStudents.size() + " student(s).");
        } catch (Exception e) {
            statusLabel.setText("Assignment failed: " + e.getMessage());
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
