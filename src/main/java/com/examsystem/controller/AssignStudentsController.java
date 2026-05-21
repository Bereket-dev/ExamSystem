package com.examsystem.controller;

import com.examsystem.model.Exam;
import com.examsystem.model.Student;
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
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.stage.Stage;

import java.util.List;

public class AssignStudentsController implements TeacherScreen {
    @FXML
    private ComboBox<Exam> examComboBox;

    @FXML
    private ListView<String> studentListView;

    @FXML
    private Label statusLabel;

    @FXML
    private Button assignButton;

    @FXML
    private Button backButton;

    private final TeacherService teacherService = new TeacherService();
    private Teacher teacher;

    @FXML
    public void initialize() {
        examComboBox.setCellFactory(cb -> new ListCell<>() {
            @Override
            protected void updateItem(Exam exam, boolean empty) {
                super.updateItem(exam, empty);
                setText(empty || exam == null ? null : exam.getExamName());
            }
        });
        examComboBox.setButtonCell(examComboBox.getCellFactory().call(null));

        assignButton.setOnAction(e -> handleAssign());
        backButton.setOnAction(e -> returnToDashboard());
    }

    @Override
    public void setTeacherContext(Teacher teacher, User user) {
        this.teacher = teacher;
        List<Exam> exams = teacherService.getExamsByTeacher(teacher.getTeacherId());
        examComboBox.setItems(FXCollections.observableArrayList(exams));
        if (!exams.isEmpty()) {
            examComboBox.getSelectionModel().selectFirst();
        }

        List<String> studentLabels = teacherService.getAllStudents().stream()
                .map(teacherService::getStudentDisplayName)
                .toList();
        studentListView.setItems(FXCollections.observableArrayList(studentLabels));
    }

    private void handleAssign() {
        Exam exam = examComboBox.getSelectionModel().getSelectedItem();
        String selectedLabel = studentListView.getSelectionModel().getSelectedItem();
        if (exam == null || selectedLabel == null) {
            statusLabel.setText("Select an exam and a student.");
            return;
        }

        Student student = teacherService.getAllStudents().stream()
                .filter(s -> teacherService.getStudentDisplayName(s).equals(selectedLabel))
                .findFirst()
                .orElse(null);

        if (student == null) {
            statusLabel.setText("Student not found.");
            return;
        }

        try {
            teacherService.assignExamToStudent(exam.getExamId(), student.getStudentId());
            if (!exam.isPublished()) {
                teacherService.publishExam(exam.getExamId(), true);
            }
            statusLabel.setText("Exam assigned to " + selectedLabel);
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
            stage.setScene(new Scene(root, 900, 650));
            stage.setTitle("Teacher Dashboard - ExamSystem");
        } catch (Exception e) {
            statusLabel.setText("Unable to return: " + e.getMessage());
        }
    }
}
