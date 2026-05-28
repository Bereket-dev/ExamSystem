package com.examsystem.controller;

import com.examsystem.model.Course;
import com.examsystem.model.Exam;
import com.examsystem.model.ExamAssignmentSyncResult;
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
import javafx.scene.control.ListCell;
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
    /** While true, student checkbox listeners must not update the Select All control. */
    private boolean syncingCheckboxes;

    @FXML
    public void initialize() {
        examComboBox.setCellFactory(cb -> new ListCell<>() {
            @Override
            protected void updateItem(Exam exam, boolean empty) {
                super.updateItem(exam, empty);
                setText(empty || exam == null ? null : exam.getExamName());
            }
        });
        examComboBox.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(Exam exam, boolean empty) {
                super.updateItem(exam, empty);
                setText(empty || exam == null ? null : exam.getExamName());
            }
        });
        examComboBox.setOnAction(e -> refreshStudents());

        // onAction only fires for user clicks — not when we sync Select All programmatically
        selectAllCheckBox.setOnAction(e -> setAllCheckboxesSelected(selectAllCheckBox.isSelected()));
    }

    @Override
    public void setTeacherContext(Teacher teacher, User user) {
        this.teacher = teacher;
        refreshExamList();
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
            showAssignmentError("Error loading exam: " + e.getMessage());
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
        selectAllCheckBox.setDisable(false);

        Exam exam = resolveSelectedExam();
        if (exam == null) {
            setStatusMessage("No exam selected for student assignment.", false);
            examCourseLabel.setText("");
            return;
        }

        Course course = teacherService.findCourseById(exam.getCourseId()).orElse(null);
        if (course == null) {
            examCourseLabel.setText("Exam course information missing.");
            setStatusMessage("No course information found for selected exam. No students to assign.", false);
            return;
        }

        examCourseLabel.setText("Course: " + course.getCourseName() + " | Dept: " + course.getDepartment()
                + " | Semester: " + course.getSemester());
        List<Student> students = teacherService.getStudentsForCourse(course);
        if (students.isEmpty()) {
            setStatusMessage("No students found for " + course.getCourseName() + " (" + course.getDepartment()
                    + " semester " + course.getSemester() + ").", false);
            return;
        }

        setStatusMessage("", true);

        for (Student student : students) {
            CheckBox checkBox = new CheckBox(teacherService.getStudentDisplayName(student));
            checkBox.selectedProperty().addListener((obs, oldVal, newVal) -> {
                if (!syncingCheckboxes) {
                    updateSelectAllCheckbox();
                }
            });
            if (teacherService.isStudentAssignedToExam(student.getStudentId(), exam.getExamId())) {
                checkBox.setSelected(true);
            }
            studentMap.put(checkBox, student);
            studentCheckboxContainer.getChildren().add(checkBox);
        }
        updateSelectAllCheckbox();
    }

    @FXML
    private void handleAssign() {
        Exam exam = resolveSelectedExam();
        if (exam == null) {
            showAssignmentError("Please select an exam.");
            return;
        }
        if (exam.getExamId() <= 0) {
            showAssignmentError("Invalid exam record. Return to the dashboard and open assignment again.");
            return;
        }

        List<Integer> selectedStudentIds = new ArrayList<>();
        for (Map.Entry<CheckBox, Student> entry : studentMap.entrySet()) {
            if (entry.getKey().isSelected()) {
                selectedStudentIds.add(entry.getValue().getStudentId());
            }
        }

        assignButton.setDisable(true);
        try {
            ExamAssignmentSyncResult result = teacherService.syncExamAssignments(
                    exam.getExamId(), selectedStudentIds);

            if (!selectedStudentIds.isEmpty() && !exam.isPublished()) {
                teacherService.publishExam(exam.getExamId(), true);
                exam.setPublished(true);
            }

            refreshStudents();

            String message = buildSyncMessage(exam.getExamName(), result, selectedStudentIds.isEmpty());
            if (result.hasChanges() || selectedStudentIds.isEmpty()) {
                showAssignmentSuccess(message);
            } else {
                setStatusMessage(message, true);
            }
        } catch (Exception e) {
            showAssignmentError("Assignment update failed: " + e.getMessage());
        } finally {
            assignButton.setDisable(false);
        }
    }

    private Exam resolveSelectedExam() {
        Exam exam = examComboBox != null ? examComboBox.getSelectionModel().getSelectedItem() : null;
        if (exam == null) {
            exam = selectedExam;
        }
        return exam;
    }

    private void setAllCheckboxesSelected(boolean selected) {
        syncingCheckboxes = true;
        try {
            for (CheckBox checkBox : studentMap.keySet()) {
                checkBox.setSelected(selected);
            }
        } finally {
            syncingCheckboxes = false;
        }
        updateSelectAllCheckbox();
    }

    private void updateSelectAllCheckbox() {
        if (studentMap.isEmpty()) {
            selectAllCheckBox.setSelected(false);
            selectAllCheckBox.setDisable(false);
            return;
        }
        selectAllCheckBox.setDisable(false);
        boolean allSelected = studentMap.keySet().stream().allMatch(CheckBox::isSelected);
        selectAllCheckBox.setSelected(allSelected);
    }

    private String buildSyncMessage(String examName, ExamAssignmentSyncResult result, boolean noneSelected) {
        if (noneSelected && result.getUnassigned() > 0) {
            return "Removed all assignable students from " + examName + " ("
                    + result.getUnassigned() + " unassigned).";
        }
        if (!result.hasChanges()) {
            return "Assignments for " + examName + " are already up to date.";
        }
        StringBuilder message = new StringBuilder("Updated assignments for ").append(examName).append(": ");
        if (result.getNewlyAssigned() > 0) {
            message.append(result.getNewlyAssigned()).append(" student(s) assigned");
        }
        if (result.getNewlyAssigned() > 0 && result.getUnassigned() > 0) {
            message.append(", ");
        }
        if (result.getUnassigned() > 0) {
            message.append(result.getUnassigned()).append(" student(s) unassigned");
        }
        message.append('.');
        return message.toString();
    }

    private void updateExamCourseLabel() {
        Exam exam = resolveSelectedExam();
        if (exam == null) {
            examCourseLabel.setText("");
            return;
        }
        teacherService.findCourseById(exam.getCourseId()).ifPresentOrElse(
                course -> examCourseLabel.setText("Course: " + course.getCourseName() + " | Dept: "
                        + course.getDepartment() + " | Semester: " + course.getSemester()),
                () -> examCourseLabel.setText("Course information unavailable."));
    }

    @FXML
    private void returnToDashboard() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/examsystem/fxml/TeacherDashboard.fxml"));
            Parent root = loader.load();
            TeacherDashboardController controller = loader.getController();
            controller.setUser(Session.getInstance().getCurrentUser());

            Stage stage = (Stage) backButton.getScene().getWindow();
            UiManager.navigateToApp(stage, root, "Teacher Dashboard - ExamSystem");
        } catch (Exception e) {
            showAssignmentError("Unable to return: " + e.getMessage());
        }
    }

    private void setStatusMessage(String message, boolean success) {
        statusLabel.setText(message);
        statusLabel.getStyleClass().removeAll("status-error", "status-success");
        statusLabel.getStyleClass().add(success ? "status-success" : "status-error");
    }

    private void showAssignmentSuccess(String message) {
        setStatusMessage(message, true);
    }

    private void showAssignmentError(String message) {
        setStatusMessage(message, false);
    }
}
