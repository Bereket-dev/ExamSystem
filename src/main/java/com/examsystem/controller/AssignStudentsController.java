package com.examsystem.controller;

import com.examsystem.model.Course;
import com.examsystem.model.Exam;
import com.examsystem.model.Student;
import com.examsystem.model.Teacher;
import com.examsystem.model.User;
import com.examsystem.service.TeacherService;
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
    private Label examCourseLabel;

    @FXML
    private CheckBox selectAllCheckBox;

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
        try {
            examComboBox.setCellFactory(cb -> new javafx.scene.control.ListCell<>() {
                @Override
                protected void updateItem(Exam exam, boolean empty) {
                    super.updateItem(exam, empty);
                    setText(empty || exam == null ? null : exam.getExamName());
                }
            });
            examComboBox.setButtonCell(examComboBox.getCellFactory().call(null));
            examComboBox.setOnAction(e -> refreshStudents());
            selectAllCheckBox.setOnAction(e -> setAllCheckboxesSelected(selectAllCheckBox.isSelected()));

            assignButton.setOnAction(e -> handleAssign());
            backButton.setOnAction(e -> returnToDashboard());
        } catch (Exception e) {
            System.err.println("Error initializing AssignStudentsController: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void setTeacherContext(Teacher teacher, User user) {
        this.teacher = teacher;
        refreshExamList();
        // refreshStudents will be called by setExam or after exam selection
    }

    public void setExam(Exam exam) {
        this.selectedExam = exam;
        try {
            if (examComboBox == null || exam == null) {
                return;
            }
            if (teacher != null) {
                refreshExamList();
            }
            Exam matching = examComboBox.getItems().stream()
                    .filter(e -> e.getExamId() == exam.getExamId())
                    .findFirst()
                    .orElse(exam);
            examComboBox.getSelectionModel().select(matching);
            examComboBox.setDisable(true);
            refreshStudents();
        } catch (Exception e) {
            System.err.println("Error setting exam in AssignStudentsController: " + e.getMessage());
            e.printStackTrace();
            if (statusLabel != null) {
                statusLabel.setText("Error loading exam: " + e.getMessage());
            }
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
        updateExamCourseLabel();
    }

    private void refreshStudents() {
        studentCheckboxContainer.getChildren().clear();
        studentMap.clear();
        selectAllCheckBox.setSelected(false);

        Exam exam = examComboBox.getSelectionModel().getSelectedItem();
        if (exam == null) {
            statusLabel.setText("No exam selected for student assignment.");
            examCourseLabel.setText("");
            return;
        }

        Course course = teacherService.findCourseById(exam.getCourseId()).orElse(null);
        List<Student> students;
        if (course == null) {
            examCourseLabel.setText("Exam course information missing.");
            statusLabel.setText("No course information found for selected exam. No students to assign.");
            return;
        } else {
            examCourseLabel.setText("Course: " + course.getCourseName() + " | Dept: " + course.getDepartment()
                    + " | Semester: " + course.getSemester());
            students = teacherService.getStudentsForCourse(course);
            if (students.isEmpty()) {
                statusLabel.setText("No students found for " + course.getCourseName() + " (" + course.getDepartment()
                        + " semester " + course.getSemester() + ").");
            } else {
                statusLabel.setText("");
            }
        }

        for (Student student : students) {
            CheckBox checkBox = new CheckBox(teacherService.getStudentDisplayName(student));
            checkBox.selectedProperty().addListener((obs, oldVal, newVal) -> updateSelectAllCheckbox());
            if (teacherService.isStudentAssignedToExam(student.getStudentId(), exam.getExamId())) {
                checkBox.setSelected(true);
            }
            studentMap.put(checkBox, student);
            studentCheckboxContainer.getChildren().add(checkBox);
        }
    }

    private void handleAssign() {
        statusLabel.setText("");
        Exam exam = examComboBox.getSelectionModel().getSelectedItem();
        if (exam == null) {
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
            refreshStudents();
        } catch (Exception e) {
            statusLabel.getStyleClass().removeAll("status-success");
            statusLabel.getStyleClass().add("status-error");
            statusLabel.setText("Assignment failed: " + e.getMessage());
        }
    }

    private void setAllCheckboxesSelected(boolean selected) {
        for (CheckBox checkBox : studentMap.keySet()) {
            checkBox.setSelected(selected);
        }
    }

    private void updateSelectAllCheckbox() {
        if (studentMap.isEmpty()) {
            selectAllCheckBox.setSelected(false);
            return;
        }
        boolean allSelected = studentMap.keySet().stream().allMatch(CheckBox::isSelected);
        selectAllCheckBox.setSelected(allSelected);
    }

    private void updateExamCourseLabel() {
        Exam exam = examComboBox.getSelectionModel().getSelectedItem();
        if (exam == null) {
            examCourseLabel.setText("");
            return;
        }
        teacherService.findCourseById(exam.getCourseId()).ifPresentOrElse(
                course -> examCourseLabel.setText("Course: " + course.getCourseName() + " | Dept: "
                        + course.getDepartment() + " | Semester: " + course.getSemester()),
                () -> examCourseLabel.setText("Course information unavailable."));
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
