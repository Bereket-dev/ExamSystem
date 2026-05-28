package com.examsystem.controller;

import com.examsystem.model.*;
import com.examsystem.service.AdminService;
import com.examsystem.util.Session;
import com.examsystem.util.UiManager;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;

/**
 * Controller for the Admin Panel.
 * Manages teachers, students, courses, and teacher-course assignments.
 */
public class AdminPanelController {
    private static final Logger logger = LoggerFactory.getLogger(AdminPanelController.class);

    private AdminService adminService;
    private User currentAdmin;

    // Top Navigation
    @FXML
    private Label adminNameLabel;
    @FXML
    private Button logoutButton;

    // Teachers Tab
    @FXML
    private TextField teacherSearchField;
    @FXML
    private Button searchTeacherButton;
    @FXML
    private Button resetTeacherSearchButton;
    @FXML
    private Button addTeacherButton;
    @FXML
    private TableView<TeacherTableEntry> teachersTable;

    // Students Tab
    @FXML
    private TextField studentSearchField;
    @FXML
    private Button searchStudentButton;
    @FXML
    private Button resetStudentSearchButton;
    @FXML
    private Button addStudentButton;
    @FXML
    private TableView<StudentTableEntry> studentsTable;

    // Courses Tab
    @FXML
    private TextField courseSearchField;
    @FXML
    private Button searchCourseButton;
    @FXML
    private Button resetCourseSearchButton;
    @FXML
    private Button addCourseButton;
    @FXML
    private TableView<CourseTableEntry> coursesTable;

    // Assignment Tab
    @FXML
    private ComboBox<TeacherComboEntry> teacherComboBox;
    @FXML
    private Label selectedTeacherLabel;
    @FXML
    private ComboBox<CourseComboEntry> courseComboBox;
    @FXML
    private Label selectedCourseLabel;
    @FXML
    private Button assignButton;
    @FXML
    private Button removeAssignmentButton;
    @FXML
    private TableView<TeacherCourseTableEntry> teacherCoursesTable;

    // Status Bar
    @FXML
    private Label totalTeachersLabel;
    @FXML
    private Label totalStudentsLabel;
    @FXML
    private Label totalCoursesLabel;
    @FXML
    private Label statusLabel;

    @FXML
    public void initialize() {
        try {
            adminService = new AdminService();
            setupUI();
            setupTableColumns();
            loadAllData();
        } catch (Exception e) {
            logger.error("Error initializing Admin Panel", e);
            showError("Initialization Error", "Failed to initialize admin panel: " + e.getMessage());
        }
    }

    /**
     * Set the current admin user
     */
    public void setCurrentAdmin(User admin) {
        this.currentAdmin = admin;
        adminNameLabel.setText("Welcome, " + admin.getFullName() + " (" + admin.getUsername() + ")");
    }

    private void setupUI() {
        // Teachers Tab Handlers
        addTeacherButton.setOnAction(e -> openAddTeacherDialog());
        searchTeacherButton.setOnAction(e -> searchTeachers());
        resetTeacherSearchButton.setOnAction(e -> resetTeacherSearch());

        // Students Tab Handlers
        addStudentButton.setOnAction(e -> openAddStudentDialog());
        searchStudentButton.setOnAction(e -> searchStudents());
        resetStudentSearchButton.setOnAction(e -> resetStudentSearch());

        // Courses Tab Handlers
        addCourseButton.setOnAction(e -> openAddCourseDialog());
        searchCourseButton.setOnAction(e -> searchCourses());
        resetCourseSearchButton.setOnAction(e -> resetCourseSearch());

        // Assignment Tab Handlers
        teacherComboBox.setOnAction(e -> onTeacherSelected());
        courseComboBox.setOnAction(e -> onCourseSelected());
        assignButton.setOnAction(e -> assignTeacherToCourse());
        removeAssignmentButton.setOnAction(e -> removeTeacherAssignment());

        // Logout Handler
        logoutButton.setOnAction(e -> logout());
    }

    private void setupTableColumns() {
        // Teachers table columns
        if (teachersTable != null && teachersTable.getColumns().size() > 0) {
            ((TableColumn<TeacherTableEntry, Integer>) teachersTable.getColumns().get(0))
                    .setCellValueFactory(new PropertyValueFactory<>("userId"));
            ((TableColumn<TeacherTableEntry, String>) teachersTable.getColumns().get(1))
                    .setCellValueFactory(new PropertyValueFactory<>("username"));
            ((TableColumn<TeacherTableEntry, String>) teachersTable.getColumns().get(2))
                    .setCellValueFactory(new PropertyValueFactory<>("fullName"));
            ((TableColumn<TeacherTableEntry, String>) teachersTable.getColumns().get(3))
                    .setCellValueFactory(new PropertyValueFactory<>("email"));
            ((TableColumn<TeacherTableEntry, String>) teachersTable.getColumns().get(4))
                    .setCellValueFactory(new PropertyValueFactory<>("department"));
            ((TableColumn<TeacherTableEntry, String>) teachersTable.getColumns().get(5))
                    .setCellValueFactory(new PropertyValueFactory<>("qualification"));
            ((TableColumn<TeacherTableEntry, Integer>) teachersTable.getColumns().get(6))
                    .setCellValueFactory(new PropertyValueFactory<>("experienceYears"));
            ((TableColumn<TeacherTableEntry, String>) teachersTable.getColumns().get(7))
                    .setCellValueFactory(new PropertyValueFactory<>("status"));

            addTeacherActions((TableColumn<TeacherTableEntry, Void>) teachersTable.getColumns().get(8));
        }

        // Students table columns
        if (studentsTable != null && studentsTable.getColumns().size() > 0) {
            ((TableColumn<StudentTableEntry, Integer>) studentsTable.getColumns().get(0))
                    .setCellValueFactory(new PropertyValueFactory<>("userId"));
            ((TableColumn<StudentTableEntry, String>) studentsTable.getColumns().get(1))
                    .setCellValueFactory(new PropertyValueFactory<>("username"));
            ((TableColumn<StudentTableEntry, String>) studentsTable.getColumns().get(2))
                    .setCellValueFactory(new PropertyValueFactory<>("fullName"));
            ((TableColumn<StudentTableEntry, String>) studentsTable.getColumns().get(3))
                    .setCellValueFactory(new PropertyValueFactory<>("email"));
            ((TableColumn<StudentTableEntry, String>) studentsTable.getColumns().get(4))
                    .setCellValueFactory(new PropertyValueFactory<>("enrollmentNumber"));
            ((TableColumn<StudentTableEntry, String>) studentsTable.getColumns().get(5))
                    .setCellValueFactory(new PropertyValueFactory<>("department"));
            ((TableColumn<StudentTableEntry, Integer>) studentsTable.getColumns().get(6))
                    .setCellValueFactory(new PropertyValueFactory<>("semester"));
            ((TableColumn<StudentTableEntry, String>) studentsTable.getColumns().get(7))
                    .setCellValueFactory(new PropertyValueFactory<>("status"));

            addStudentActions((TableColumn<StudentTableEntry, Void>) studentsTable.getColumns().get(8));
        }

        // Courses table columns
        if (coursesTable != null && coursesTable.getColumns().size() > 0) {
            ((TableColumn<CourseTableEntry, Integer>) coursesTable.getColumns().get(0))
                    .setCellValueFactory(new PropertyValueFactory<>("courseId"));
            ((TableColumn<CourseTableEntry, String>) coursesTable.getColumns().get(1))
                    .setCellValueFactory(new PropertyValueFactory<>("courseCode"));
            ((TableColumn<CourseTableEntry, String>) coursesTable.getColumns().get(2))
                    .setCellValueFactory(new PropertyValueFactory<>("courseName"));
            ((TableColumn<CourseTableEntry, String>) coursesTable.getColumns().get(3))
                    .setCellValueFactory(new PropertyValueFactory<>("department"));
            ((TableColumn<CourseTableEntry, Integer>) coursesTable.getColumns().get(4))
                    .setCellValueFactory(new PropertyValueFactory<>("credits"));
            ((TableColumn<CourseTableEntry, Integer>) coursesTable.getColumns().get(5))
                    .setCellValueFactory(new PropertyValueFactory<>("semester"));
            ((TableColumn<CourseTableEntry, String>) coursesTable.getColumns().get(6))
                    .setCellValueFactory(new PropertyValueFactory<>("status"));

            addCourseActions((TableColumn<CourseTableEntry, Void>) coursesTable.getColumns().get(7));
        }

        // Teacher courses table columns
        if (teacherCoursesTable != null && teacherCoursesTable.getColumns().size() > 0) {
            ((TableColumn<TeacherCourseTableEntry, Integer>) teacherCoursesTable.getColumns().get(0))
                    .setCellValueFactory(new PropertyValueFactory<>("courseId"));
            ((TableColumn<TeacherCourseTableEntry, String>) teacherCoursesTable.getColumns().get(1))
                    .setCellValueFactory(new PropertyValueFactory<>("courseCode"));
            ((TableColumn<TeacherCourseTableEntry, String>) teacherCoursesTable.getColumns().get(2))
                    .setCellValueFactory(new PropertyValueFactory<>("courseName"));
            ((TableColumn<TeacherCourseTableEntry, String>) teacherCoursesTable.getColumns().get(3))
                    .setCellValueFactory(new PropertyValueFactory<>("department"));
            ((TableColumn<TeacherCourseTableEntry, String>) teacherCoursesTable.getColumns().get(4))
                    .setCellValueFactory(new PropertyValueFactory<>("assignedDateStr"));

            addRemoveAssignmentAction(
                    (TableColumn<TeacherCourseTableEntry, Void>) teacherCoursesTable.getColumns().get(5));
        }
    }

    private void loadAllData() {
        loadTeachers();
        loadStudents();
        loadCourses();
        loadTeacherComboBox();
        loadCourseComboBox();
        updateStats();
    }

    // ============ TEACHERS MANAGEMENT ============

    private void loadTeachers() {
        try {
            List<User> teachers = adminService.getAllTeachers();
            ObservableList<TeacherTableEntry> entries = FXCollections.observableArrayList();

            for (User user : teachers) {
                Optional<Teacher> teacher = adminService.getTeacherByUserId(user.getUserId());
                if (teacher.isPresent()) {
                    entries.add(new TeacherTableEntry(user, teacher.get()));
                }
            }

            teachersTable.setItems(entries);
            setupTeachersTableColumns();
        } catch (Exception e) {
            logger.error("Error loading teachers", e);
            showError("Load Error", "Failed to load teachers: " + e.getMessage());
        }
    }

    private void setupTeachersTableColumns() {
        // Implementation of table columns will be done in TableColumn setup
        ObservableList<TableColumn<TeacherTableEntry, ?>> columns = teachersTable.getColumns();
        if (columns.size() > 0) {
            ((TableColumn<TeacherTableEntry, Integer>) columns.get(0)).setCellValueFactory(
                    cell -> new javafx.beans.property.SimpleObjectProperty<>(cell.getValue().getUserId()));
            ((TableColumn<TeacherTableEntry, String>) columns.get(1)).setCellValueFactory(
                    cell -> new javafx.beans.property.SimpleStringProperty(cell.getValue().getUsername()));
            ((TableColumn<TeacherTableEntry, String>) columns.get(2)).setCellValueFactory(
                    cell -> new javafx.beans.property.SimpleStringProperty(cell.getValue().getFullName()));
            ((TableColumn<TeacherTableEntry, String>) columns.get(3)).setCellValueFactory(
                    cell -> new javafx.beans.property.SimpleStringProperty(cell.getValue().getEmail()));
            ((TableColumn<TeacherTableEntry, String>) columns.get(4)).setCellValueFactory(
                    cell -> new javafx.beans.property.SimpleStringProperty(cell.getValue().getDepartment()));
            ((TableColumn<TeacherTableEntry, String>) columns.get(5)).setCellValueFactory(
                    cell -> new javafx.beans.property.SimpleStringProperty(cell.getValue().getQualification()));
            ((TableColumn<TeacherTableEntry, Integer>) columns.get(6)).setCellValueFactory(
                    cell -> new javafx.beans.property.SimpleObjectProperty<>(cell.getValue().getExperienceYears()));
            ((TableColumn<TeacherTableEntry, String>) columns.get(7))
                    .setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(
                            cell.getValue().isActive() ? "Active" : "Inactive"));

            addTableActions(columns.get(8), true);
        }
    }

    private void searchTeachers() {
        String query = teacherSearchField.getText().toLowerCase();
        if (query.isEmpty()) {
            loadTeachers();
            return;
        }

        try {
            List<User> allTeachers = adminService.getAllTeachers();
            ObservableList<TeacherTableEntry> filtered = FXCollections.observableArrayList();

            for (User user : allTeachers) {
                Optional<Teacher> teacher = adminService.getTeacherByUserId(user.getUserId());
                if (teacher.isPresent()) {
                    if (user.getUsername().toLowerCase().contains(query) ||
                            user.getFullName().toLowerCase().contains(query) ||
                            user.getEmail().toLowerCase().contains(query)) {
                        filtered.add(new TeacherTableEntry(user, teacher.get()));
                    }
                }
            }

            teachersTable.setItems(filtered);
            updateStats();
        } catch (Exception e) {
            logger.error("Error searching teachers", e);
            showError("Search Error", "Failed to search teachers: " + e.getMessage());
        }
    }

    private void resetTeacherSearch() {
        teacherSearchField.clear();
        loadTeachers();
    }

    private void openAddTeacherDialog() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/examsystem/fxml/TeacherDialog.fxml"));
            DialogPane dialogPane = loader.load();
            TeacherDialogController controller = loader.getController();

            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setDialogPane(dialogPane);
            dialog.setTitle("Add New Teacher");
            dialog.setHeaderText("Enter Teacher Information");

            Optional<ButtonType> result = dialog.showAndWait();
            if (result.isPresent() && result.get().getButtonData() == ButtonBar.ButtonData.OK_DONE) {
                Teacher teacher = controller.getTeacher();
                User user = controller.getUser();

                if (teacher != null && user != null) {
                    adminService.addTeacher(user, teacher);
                    loadTeachers();
                    updateStats();
                    showSuccess("Teacher Added", "Teacher " + user.getFullName() + " has been added successfully.");
                }
            }
        } catch (IOException e) {
            logger.error("Error opening teacher dialog", e);
            showError("Dialog Error", "Failed to open teacher dialog: " + e.getMessage());
        }
    }

    private void openEditTeacherDialog(int userId) {
        try {
            Optional<User> userOpt = adminService.getAllTeachers().stream()
                    .filter(u -> u.getUserId() == userId)
                    .findFirst();
            if (userOpt.isEmpty()) {
                showError("Edit Error", "Teacher user not found.");
                return;
            }

            Optional<Teacher> teacherOpt = adminService.getTeacherByUserId(userId);
            if (teacherOpt.isEmpty()) {
                showError("Edit Error", "Teacher profile not found.");
                return;
            }

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/examsystem/fxml/TeacherDialog.fxml"));
            DialogPane dialogPane = loader.load();
            TeacherDialogController controller = loader.getController();
            controller.setTeacher(userOpt.get(), teacherOpt.get());

            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setDialogPane(dialogPane);
            dialog.setTitle("Edit Teacher");
            dialog.setHeaderText("Update Teacher Information");

            Optional<ButtonType> result = dialog.showAndWait();
            if (result.isPresent() && result.get().getButtonData() == ButtonBar.ButtonData.OK_DONE) {
                User user = controller.getUser();
                Teacher teacher = controller.getTeacher();
                if (user != null && teacher != null) {
                    adminService.updateTeacher(user, teacher);
                    loadTeachers();
                    loadTeacherComboBox();
                    updateStats();
                    showSuccess("Teacher Updated", "Teacher information updated successfully.");
                }
            }
        } catch (Exception e) {
            logger.error("Error editing teacher", e);
            showError("Edit Error", "Failed to edit teacher: " + e.getMessage());
        }
    }

    // ============ STUDENTS MANAGEMENT ============

    private void loadStudents() {
        try {
            List<User> students = adminService.getAllStudents();
            ObservableList<StudentTableEntry> entries = FXCollections.observableArrayList();

            for (User user : students) {
                Optional<Student> student = adminService.getStudentByUserId(user.getUserId());
                if (student.isPresent()) {
                    entries.add(new StudentTableEntry(user, student.get()));
                }
            }

            studentsTable.setItems(entries);
            setupStudentsTableColumns();
        } catch (Exception e) {
            logger.error("Error loading students", e);
            showError("Load Error", "Failed to load students: " + e.getMessage());
        }
    }

    private void setupStudentsTableColumns() {
        ObservableList<TableColumn<StudentTableEntry, ?>> columns = studentsTable.getColumns();
        if (columns.size() > 0) {
            ((TableColumn<StudentTableEntry, Integer>) columns.get(0)).setCellValueFactory(
                    cell -> new javafx.beans.property.SimpleObjectProperty<>(cell.getValue().getUserId()));
            ((TableColumn<StudentTableEntry, String>) columns.get(1)).setCellValueFactory(
                    cell -> new javafx.beans.property.SimpleStringProperty(cell.getValue().getUsername()));
            ((TableColumn<StudentTableEntry, String>) columns.get(2)).setCellValueFactory(
                    cell -> new javafx.beans.property.SimpleStringProperty(cell.getValue().getFullName()));
            ((TableColumn<StudentTableEntry, String>) columns.get(3)).setCellValueFactory(
                    cell -> new javafx.beans.property.SimpleStringProperty(cell.getValue().getEmail()));
            ((TableColumn<StudentTableEntry, String>) columns.get(4)).setCellValueFactory(
                    cell -> new javafx.beans.property.SimpleStringProperty(cell.getValue().getEnrollmentNumber()));
            ((TableColumn<StudentTableEntry, String>) columns.get(5)).setCellValueFactory(
                    cell -> new javafx.beans.property.SimpleStringProperty(cell.getValue().getDepartment()));
            ((TableColumn<StudentTableEntry, Integer>) columns.get(6)).setCellValueFactory(
                    cell -> new javafx.beans.property.SimpleObjectProperty<>(cell.getValue().getSemester()));
            ((TableColumn<StudentTableEntry, String>) columns.get(7))
                    .setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(
                            cell.getValue().isActive() ? "Active" : "Inactive"));

            addTableActions(columns.get(8), false);
        }
    }

    private void searchStudents() {
        String query = studentSearchField.getText().toLowerCase();
        if (query.isEmpty()) {
            loadStudents();
            return;
        }

        try {
            List<User> allStudents = adminService.getAllStudents();
            ObservableList<StudentTableEntry> filtered = FXCollections.observableArrayList();

            for (User user : allStudents) {
                Optional<Student> student = adminService.getStudentByUserId(user.getUserId());
                if (student.isPresent()) {
                    if (user.getUsername().toLowerCase().contains(query) ||
                            user.getFullName().toLowerCase().contains(query) ||
                            user.getEmail().toLowerCase().contains(query) ||
                            student.get().getEnrollmentNumber().toLowerCase().contains(query)) {
                        filtered.add(new StudentTableEntry(user, student.get()));
                    }
                }
            }

            studentsTable.setItems(filtered);
            updateStats();
        } catch (Exception e) {
            logger.error("Error searching students", e);
            showError("Search Error", "Failed to search students: " + e.getMessage());
        }
    }

    private void resetStudentSearch() {
        studentSearchField.clear();
        loadStudents();
    }

    private void openAddStudentDialog() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/examsystem/fxml/StudentDialog.fxml"));
            DialogPane dialogPane = loader.load();
            StudentDialogController controller = loader.getController();

            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setDialogPane(dialogPane);
            dialog.setTitle("Add New Student");
            dialog.setHeaderText("Enter Student Information");

            Optional<ButtonType> result = dialog.showAndWait();
            if (result.isPresent() && result.get().getButtonData() == ButtonBar.ButtonData.OK_DONE) {
                Student student = controller.getStudent();
                User user = controller.getUser();

                if (student != null && user != null) {
                    adminService.addStudent(user, student);
                    loadStudents();
                    updateStats();
                    showSuccess("Student Added", "Student " + user.getFullName() + " has been added successfully.");
                }
            }
        } catch (IOException e) {
            logger.error("Error opening student dialog", e);
            showError("Dialog Error", "Failed to open student dialog: " + e.getMessage());
        }
    }

    private void openEditStudentDialog(int userId) {
        try {
            Optional<User> userOpt = adminService.getAllStudents().stream()
                    .filter(u -> u.getUserId() == userId)
                    .findFirst();
            if (userOpt.isEmpty()) {
                showError("Edit Error", "Student user not found.");
                return;
            }

            Optional<Student> studentOpt = adminService.getStudentByUserId(userId);
            if (studentOpt.isEmpty()) {
                showError("Edit Error", "Student profile not found.");
                return;
            }

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/examsystem/fxml/StudentDialog.fxml"));
            DialogPane dialogPane = loader.load();
            StudentDialogController controller = loader.getController();
            controller.setStudent(userOpt.get(), studentOpt.get());

            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setDialogPane(dialogPane);
            dialog.setTitle("Edit Student");
            dialog.setHeaderText("Update Student Information");

            Optional<ButtonType> result = dialog.showAndWait();
            if (result.isPresent() && result.get().getButtonData() == ButtonBar.ButtonData.OK_DONE) {
                User user = controller.getUser();
                Student student = controller.getStudent();
                if (user != null && student != null) {
                    adminService.updateStudent(user, student);
                    loadStudents();
                    updateStats();
                    showSuccess("Student Updated", "Student information updated successfully.");
                }
            }
        } catch (Exception e) {
            logger.error("Error editing student", e);
            showError("Edit Error", "Failed to edit student: " + e.getMessage());
        }
    }

    // ============ COURSES MANAGEMENT ============

    private void loadCourses() {
        try {
            List<Course> courses = adminService.getAllCourses();
            ObservableList<CourseTableEntry> entries = FXCollections.observableArrayList();

            for (Course course : courses) {
                entries.add(new CourseTableEntry(course));
            }

            coursesTable.setItems(entries);
            setupCoursesTableColumns();
        } catch (Exception e) {
            logger.error("Error loading courses", e);
            showError("Load Error", "Failed to load courses: " + e.getMessage());
        }
    }

    private void setupCoursesTableColumns() {
        ObservableList<TableColumn<CourseTableEntry, ?>> columns = coursesTable.getColumns();
        if (columns.size() > 0) {
            ((TableColumn<CourseTableEntry, Integer>) columns.get(0)).setCellValueFactory(
                    cell -> new javafx.beans.property.SimpleObjectProperty<>(cell.getValue().getCourseId()));
            ((TableColumn<CourseTableEntry, String>) columns.get(1)).setCellValueFactory(
                    cell -> new javafx.beans.property.SimpleStringProperty(cell.getValue().getCourseCode()));
            ((TableColumn<CourseTableEntry, String>) columns.get(2)).setCellValueFactory(
                    cell -> new javafx.beans.property.SimpleStringProperty(cell.getValue().getCourseName()));
            ((TableColumn<CourseTableEntry, String>) columns.get(3)).setCellValueFactory(
                    cell -> new javafx.beans.property.SimpleStringProperty(cell.getValue().getDepartment()));
            ((TableColumn<CourseTableEntry, Integer>) columns.get(4)).setCellValueFactory(
                    cell -> new javafx.beans.property.SimpleObjectProperty<>(cell.getValue().getCredits()));
            ((TableColumn<CourseTableEntry, Integer>) columns.get(5)).setCellValueFactory(
                    cell -> new javafx.beans.property.SimpleObjectProperty<>(cell.getValue().getSemester()));
            ((TableColumn<CourseTableEntry, String>) columns.get(6))
                    .setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(
                            cell.getValue().isActive() ? "Active" : "Inactive"));

            // Add edit/delete buttons
            TableColumn<CourseTableEntry, Void> actionsColumn = (TableColumn<CourseTableEntry, Void>) columns.get(7);
            addCourseActions(actionsColumn);
        }
    }

    private void addCourseActions(TableColumn<CourseTableEntry, Void> column) {
        column.setCellFactory(param -> new TableCell<CourseTableEntry, Void>() {
            private final Button editBtn = new Button("Edit");
            private final Button deleteBtn = new Button("Delete");
            private final HBox container = new HBox(5);

            {
                editBtn.setStyle("-fx-font-size: 10; -fx-padding: 5;");
                deleteBtn.setStyle("-fx-font-size: 10; -fx-padding: 5;");
                container.getChildren().addAll(editBtn, deleteBtn);

                editBtn.setOnAction(event -> {
                    CourseTableEntry entry = getTableView().getItems().get(getIndex());
                    openEditCourseDialog(entry.getCourseId());
                });

                deleteBtn.setOnAction(event -> {
                    CourseTableEntry entry = getTableView().getItems().get(getIndex());
                    if (confirmDelete("Delete Course", "Are you sure you want to delete this course?")) {
                        try {
                            adminService.deleteCourse(entry.getCourseId());
                            loadCourses();
                            updateStats();
                            showSuccess("Course Deleted", "Course has been deleted successfully.");
                        } catch (Exception e) {
                            showError("Delete Error", "Failed to delete course: " + e.getMessage());
                        }
                    }
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : container);
            }
        });
    }

    private void searchCourses() {
        String query = courseSearchField.getText().toLowerCase();
        if (query.isEmpty()) {
            loadCourses();
            return;
        }

        try {
            List<Course> allCourses = adminService.getAllCourses();
            ObservableList<CourseTableEntry> filtered = FXCollections.observableArrayList();

            for (Course course : allCourses) {
                if (course.getCourseCode().toLowerCase().contains(query) ||
                        course.getCourseName().toLowerCase().contains(query)) {
                    filtered.add(new CourseTableEntry(course));
                }
            }

            coursesTable.setItems(filtered);
            updateStats();
        } catch (Exception e) {
            logger.error("Error searching courses", e);
            showError("Search Error", "Failed to search courses: " + e.getMessage());
        }
    }

    private void resetCourseSearch() {
        courseSearchField.clear();
        loadCourses();
    }

    private void openAddCourseDialog() {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/examsystem/fxml/CourseDialog.fxml"));
        DialogPane dialogPane;
        try {
            dialogPane = loader.load();
        } catch (IOException e) {
            logger.error("Error loading course dialog FXML", e);
            showError("Dialog Error", "Failed to open course dialog: " + e.getMessage());
            return;
        }

        CourseDialogController controller = loader.getController();
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setDialogPane(dialogPane);
        dialog.setTitle("Add New Course");
        dialog.setHeaderText("Enter Course Information");

        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isPresent() && result.get().getButtonData() == ButtonBar.ButtonData.OK_DONE) {
            Course course = controller.getCourse();
            if (course == null) {
                return;
            }

            try {
                adminService.createCourse(course);
                loadCourses();
                loadCourseComboBox();
                updateStats();
                showSuccess("Course Added", "Course " + course.getCourseName() + " has been added successfully.");
            } catch (IllegalArgumentException e) {
                showError("Course Error", e.getMessage());
            } catch (Exception e) {
                logger.error("Error saving course", e);
                showError("Course Error", "Failed to save course: " + e.getMessage());
            }
        }
    }

    private void openEditCourseDialog(int courseId) {
        try {
            Optional<Course> courseOpt = adminService.getCourseById(courseId);
            if (courseOpt.isEmpty()) {
                showError("Error", "Course not found");
                return;
            }

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/examsystem/fxml/CourseDialog.fxml"));
            DialogPane dialogPane = loader.load();
            CourseDialogController controller = loader.getController();
            controller.setCourse(courseOpt.get());

            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setDialogPane(dialogPane);
            dialog.setTitle("Edit Course");
            dialog.setHeaderText("Update Course Information");

            Optional<ButtonType> result = dialog.showAndWait();
            if (result.isPresent() && result.get().getButtonData() == ButtonBar.ButtonData.OK_DONE) {
                Course course = controller.getCourse();
                if (course != null) {
                    adminService.updateCourse(course);
                    loadCourses();
                    loadCourseComboBox();
                    updateStats();
                    showSuccess("Course Updated", "Course has been updated successfully.");
                }
            }
        } catch (Exception e) {
            logger.error("Error opening edit course dialog", e);
            showError("Dialog Error", "Failed to open course dialog: " + e.getMessage());
        }
    }

    // ============ TEACHER-COURSE ASSIGNMENT ============

    private void loadTeacherComboBox() {
        try {
            List<User> teachers = adminService.getAllTeachers();
            ObservableList<TeacherComboEntry> items = FXCollections.observableArrayList();

            for (User user : teachers) {
                Optional<Teacher> teacherProfile = adminService.getTeacherByUserId(user.getUserId());
                if (teacherProfile.isPresent()) {
                    items.add(new TeacherComboEntry(teacherProfile.get().getTeacherId(), user.getFullName(),
                            user.getUsername()));
                } else {
                    logger.warn("Teacher profile missing for user id {}", user.getUserId());
                }
            }

            teacherComboBox.setItems(items);
        } catch (Exception e) {
            logger.error("Error loading teacher combo box", e);
        }
    }

    private void loadCourseComboBox() {
        try {
            List<Course> courses = adminService.getAllActiveCourses();
            ObservableList<CourseComboEntry> items = FXCollections.observableArrayList();

            for (Course course : courses) {
                items.add(new CourseComboEntry(course.getCourseId(), course.getCourseCode(), course.getCourseName()));
            }

            courseComboBox.setItems(items);
        } catch (Exception e) {
            logger.error("Error loading course combo box", e);
        }
    }

    private void onTeacherSelected() {
        TeacherComboEntry selected = teacherComboBox.getValue();
        if (selected != null) {
            selectedTeacherLabel.setText(selected.getFullName() + " (" + selected.getUsername() + ")");
            loadTeacherCoursesTable(selected.getTeacherId());
        } else {
            selectedTeacherLabel.setText("No teacher selected");
            teacherCoursesTable.setItems(FXCollections.observableArrayList());
        }
    }

    private void onCourseSelected() {
        CourseComboEntry selected = courseComboBox.getValue();
        if (selected != null) {
            selectedCourseLabel.setText(selected.getCourseCode() + " - " + selected.getCourseName());
        } else {
            selectedCourseLabel.setText("No course selected");
        }
    }

    private void loadTeacherCoursesTable(int teacherId) {
        try {
            List<TeacherCourse> assignments = adminService.getCoursesForTeacher(teacherId);
            ObservableList<TeacherCourseTableEntry> entries = FXCollections.observableArrayList();

            for (TeacherCourse assignment : assignments) {
                Optional<Course> course = adminService.getCourseById(assignment.getCourseId());
                if (course.isPresent()) {
                    entries.add(new TeacherCourseTableEntry(assignment, course.get()));
                }
            }

            teacherCoursesTable.setItems(entries);
            setupTeacherCoursesTableColumns();
        } catch (Exception e) {
            logger.error("Error loading teacher courses table", e);
        }
    }

    private void setupTeacherCoursesTableColumns() {
        ObservableList<TableColumn<TeacherCourseTableEntry, ?>> columns = teacherCoursesTable.getColumns();
        if (columns.size() > 0) {
            ((TableColumn<TeacherCourseTableEntry, Integer>) columns.get(0)).setCellValueFactory(
                    cell -> new javafx.beans.property.SimpleObjectProperty<>(cell.getValue().getCourseId()));
            ((TableColumn<TeacherCourseTableEntry, String>) columns.get(1)).setCellValueFactory(
                    cell -> new javafx.beans.property.SimpleStringProperty(cell.getValue().getCourseCode()));
            ((TableColumn<TeacherCourseTableEntry, String>) columns.get(2)).setCellValueFactory(
                    cell -> new javafx.beans.property.SimpleStringProperty(cell.getValue().getCourseName()));
            ((TableColumn<TeacherCourseTableEntry, String>) columns.get(3)).setCellValueFactory(
                    cell -> new javafx.beans.property.SimpleStringProperty(cell.getValue().getDepartment()));
            ((TableColumn<TeacherCourseTableEntry, String>) columns.get(4))
                    .setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(
                            cell.getValue().getAssignedDate() != null ? cell.getValue().getAssignedDate().toString()
                                    : "N/A"));

            TableColumn<TeacherCourseTableEntry, Void> actionColumn = (TableColumn<TeacherCourseTableEntry, Void>) columns
                    .get(5);
            addRemoveAssignmentAction(actionColumn);
        }
    }

    private void addRemoveAssignmentAction(TableColumn<TeacherCourseTableEntry, Void> column) {
        column.setCellFactory(param -> new TableCell<TeacherCourseTableEntry, Void>() {
            private final Button removeBtn = new Button("Remove");

            {
                removeBtn.setStyle("-fx-font-size: 10; -fx-padding: 5;");
                removeBtn.setOnAction(event -> {
                    TeacherCourseTableEntry entry = getTableView().getItems().get(getIndex());
                    TeacherComboEntry selectedTeacher = teacherComboBox.getValue();
                    if (selectedTeacher != null) {
                        if (confirmDelete("Remove Assignment",
                                "Are you sure you want to remove this teacher from the course?")) {
                            try {
                                adminService.removeTeacherFromCourse(selectedTeacher.getTeacherId(),
                                        entry.getCourseId());
                                loadTeacherCoursesTable(selectedTeacher.getTeacherId());
                                showSuccess("Assignment Removed",
                                        "Teacher has been removed from the course.");
                            } catch (Exception e) {
                                showError("Error", "Failed to remove assignment: " + e.getMessage());
                            }
                        }
                    }
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : removeBtn);
            }
        });
    }

    private void assignTeacherToCourse() {
        TeacherComboEntry selectedTeacher = teacherComboBox.getValue();
        CourseComboEntry selectedCourse = courseComboBox.getValue();

        if (selectedTeacher == null || selectedCourse == null) {
            showError("Validation Error", "Please select both teacher and course");
            return;
        }

        try {
            adminService.assignTeacherToCourse(selectedTeacher.getTeacherId(), selectedCourse.getCourseId());
            loadTeacherCoursesTable(selectedTeacher.getTeacherId());
            showSuccess("Assignment Successful",
                    "Teacher " + selectedTeacher.getFullName() + " has been assigned to course " +
                            selectedCourse.getCourseName());
        } catch (Exception e) {
            showError("Assignment Error", e.getMessage());
        }
    }

    private void removeTeacherAssignment() {
        TeacherComboEntry selectedTeacher = teacherComboBox.getValue();
        CourseComboEntry selectedCourse = courseComboBox.getValue();

        if (selectedTeacher == null || selectedCourse == null) {
            showError("Validation Error", "Please select both teacher and course");
            return;
        }

        if (confirmDelete("Remove Assignment",
                "Are you sure you want to remove this teacher from the course?")) {
            try {
                adminService.removeTeacherFromCourse(selectedTeacher.getTeacherId(), selectedCourse.getCourseId());
                loadTeacherCoursesTable(selectedTeacher.getTeacherId());
                showSuccess("Assignment Removed", "Teacher has been removed from the course.");
            } catch (Exception e) {
                showError("Error", "Failed to remove assignment: " + e.getMessage());
            }
        }
    }

    // ============ UTILITY METHODS ============

    private <T> void addTableActions(TableColumn<?, ?> column, boolean isTeacher) {
        // This will be handled by specific column setup methods
    }

    private void addTeacherActions(TableColumn<TeacherTableEntry, Void> column) {
        column.setCellFactory(param -> new TableCell<TeacherTableEntry, Void>() {
            private final Button editBtn = new Button("Edit");
            private final Button deleteBtn = new Button("Delete");
            private final HBox container = new HBox(5, editBtn, deleteBtn);

            {
                editBtn.setStyle("-fx-font-size: 10; -fx-padding: 5;");
                deleteBtn.setStyle("-fx-font-size: 10; -fx-padding: 5;");

                editBtn.setOnAction(event -> {
                    TeacherTableEntry entry = getTableView().getItems().get(getIndex());
                    openEditTeacherDialog(entry.getUserId());
                });

                deleteBtn.setOnAction(event -> {
                    TeacherTableEntry entry = getTableView().getItems().get(getIndex());
                    if (confirmDelete("Delete Teacher",
                            "Are you sure you want to delete teacher " + entry.getFullName() + "?")) {
                        try {
                            adminService.removeTeacher(entry.getUserId());
                            loadTeachers();
                            loadTeacherComboBox();
                            updateStats();
                            showSuccess("Teacher Deleted", "Teacher has been deleted successfully.");
                        } catch (Exception e) {
                            showError("Delete Error", "Failed to delete teacher: " + e.getMessage());
                        }
                    }
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : container);
            }
        });
    }

    private void addStudentActions(TableColumn<StudentTableEntry, Void> column) {
        column.setCellFactory(param -> new TableCell<StudentTableEntry, Void>() {
            private final Button editBtn = new Button("Edit");
            private final Button deleteBtn = new Button("Delete");
            private final HBox container = new HBox(5, editBtn, deleteBtn);

            {
                editBtn.setStyle("-fx-font-size: 10; -fx-padding: 5;");
                deleteBtn.setStyle("-fx-font-size: 10; -fx-padding: 5;");

                editBtn.setOnAction(event -> {
                    StudentTableEntry entry = getTableView().getItems().get(getIndex());
                    openEditStudentDialog(entry.getUserId());
                });

                deleteBtn.setOnAction(event -> {
                    StudentTableEntry entry = getTableView().getItems().get(getIndex());
                    if (confirmDelete("Delete Student",
                            "Are you sure you want to delete student " + entry.getFullName() + "?")) {
                        try {
                            adminService.removeStudent(entry.getUserId());
                            loadStudents();
                            updateStats();
                            showSuccess("Student Deleted", "Student has been deleted successfully.");
                        } catch (Exception e) {
                            showError("Delete Error", "Failed to delete student: " + e.getMessage());
                        }
                    }
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : container);
            }
        });
    }

    private void updateStats() {
        try {
            totalTeachersLabel.setText(String.valueOf(adminService.getAllTeachers().size()));
            totalStudentsLabel.setText(String.valueOf(adminService.getAllStudents().size()));
            totalCoursesLabel.setText(String.valueOf(adminService.getAllCourses().size()));
            statusLabel.setText("Ready");
        } catch (Exception e) {
            statusLabel.setText("Error loading stats");
        }
    }

    private void showSuccess(String title, String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }

    private void showError(String title, String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }

    private boolean confirmDelete(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }

    private void logout() {
        if (confirmDelete("Logout", "Are you sure you want to logout?")) {
            try {
                Session.getInstance().logout();
                Stage stage = (Stage) logoutButton.getScene().getWindow();
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/examsystem/fxml/login.fxml"));
                UiManager.navigateToLogin(stage, loader.load());
            } catch (IOException e) {
                logger.error("Error loading login screen", e);
            }
        }
    }

    // ============ INNER CLASSES FOR TABLE ENTRIES ============

    public static class TeacherTableEntry {
        private User user;
        private Teacher teacher;

        public TeacherTableEntry(User user, Teacher teacher) {
            this.user = user;
            this.teacher = teacher;
        }

        public int getUserId() {
            return user.getUserId();
        }

        public String getUsername() {
            return user.getUsername();
        }

        public String getFullName() {
            return user.getFullName();
        }

        public String getEmail() {
            return user.getEmail();
        }

        public String getDepartment() {
            return teacher.getDepartment();
        }

        public String getQualification() {
            return teacher.getQualification();
        }

        public int getExperienceYears() {
            return teacher.getExperienceYears();
        }

        public boolean isActive() {
            return user.isActive();
        }

        public int getTeacherId() {
            return teacher.getTeacherId();
        }
    }

    public static class StudentTableEntry {
        private User user;
        private Student student;

        public StudentTableEntry(User user, Student student) {
            this.user = user;
            this.student = student;
        }

        public int getUserId() {
            return user.getUserId();
        }

        public String getUsername() {
            return user.getUsername();
        }

        public String getFullName() {
            return user.getFullName();
        }

        public String getEmail() {
            return user.getEmail();
        }

        public String getEnrollmentNumber() {
            return student.getEnrollmentNumber();
        }

        public String getDepartment() {
            return student.getDepartment();
        }

        public int getSemester() {
            return student.getSemester();
        }

        public boolean isActive() {
            return user.isActive();
        }

        public int getStudentId() {
            return student.getStudentId();
        }
    }

    public static class CourseTableEntry {
        private Course course;

        public CourseTableEntry(Course course) {
            this.course = course;
        }

        public int getCourseId() {
            return course.getCourseId();
        }

        public String getCourseCode() {
            return course.getCourseCode();
        }

        public String getCourseName() {
            return course.getCourseName();
        }

        public String getDepartment() {
            return course.getDepartment();
        }

        public int getCredits() {
            return course.getCredits();
        }

        public int getSemester() {
            return course.getSemester();
        }

        public boolean isActive() {
            return course.isActive();
        }
    }

    public static class TeacherComboEntry {
        private int teacherId;
        private String fullName;
        private String username;

        public TeacherComboEntry(int teacherId, String fullName, String username) {
            this.teacherId = teacherId;
            this.fullName = fullName;
            this.username = username;
        }

        public int getTeacherId() {
            return teacherId;
        }

        public String getFullName() {
            return fullName;
        }

        public String getUsername() {
            return username;
        }

        @Override
        public String toString() {
            return fullName + " (" + username + ")";
        }
    }

    public static class CourseComboEntry {
        private int courseId;
        private String courseCode;
        private String courseName;

        public CourseComboEntry(int courseId, String courseCode, String courseName) {
            this.courseId = courseId;
            this.courseCode = courseCode;
            this.courseName = courseName;
        }

        public int getCourseId() {
            return courseId;
        }

        public String getCourseCode() {
            return courseCode;
        }

        public String getCourseName() {
            return courseName;
        }

        @Override
        public String toString() {
            return courseCode + " - " + courseName;
        }
    }

    public static class TeacherCourseTableEntry {
        private TeacherCourse assignment;
        private Course course;

        public TeacherCourseTableEntry(TeacherCourse assignment, Course course) {
            this.assignment = assignment;
            this.course = course;
        }

        public int getCourseId() {
            return course.getCourseId();
        }

        public String getCourseCode() {
            return course.getCourseCode();
        }

        public String getCourseName() {
            return course.getCourseName();
        }

        public String getDepartment() {
            return course.getDepartment();
        }

        public java.time.LocalDateTime getAssignedDate() {
            return assignment.getAssignedDate();
        }

        public String getAssignedDateStr() {
            return assignment.getAssignedDate() != null ? assignment.getAssignedDate().toString() : "N/A";
        }
    }
}
