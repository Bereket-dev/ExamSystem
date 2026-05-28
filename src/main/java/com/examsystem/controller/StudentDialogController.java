package com.examsystem.controller;

import com.examsystem.model.User;
import com.examsystem.model.Student;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Controller for Student Dialog.
 * Handles adding and editing student accounts.
 */
public class StudentDialogController {
    private static final Logger logger = LoggerFactory.getLogger(StudentDialogController.class);

    @FXML
    private TextField usernameField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private TextField fullNameField;
    @FXML
    private TextField emailField;
    @FXML
    private TextField enrollmentField;
    @FXML
    private TextField departmentField;
    @FXML
    private Spinner<Integer> semesterSpinner;
    @FXML
    private CheckBox activeCheckBox;

    private User user;
    private Student student;

    @FXML
    public void initialize() {
        setupSpinner();
    }

    private void setupSpinner() {
        SpinnerValueFactory<Integer> valueFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 8, 1);
        semesterSpinner.setValueFactory(valueFactory);
    }

    /**
     * Set student for editing
     */
    public void setStudent(User user, Student student) {
        this.user = user;
        this.student = student;

        usernameField.setText(user.getUsername());
        fullNameField.setText(user.getFullName());
        emailField.setText(user.getEmail());
        usernameField.setDisable(true);

        enrollmentField.setText(student.getEnrollmentNumber());
        departmentField.setText(student.getDepartment());
        semesterSpinner.getValueFactory().setValue(student.getSemester());
        activeCheckBox.setSelected(user.isActive());
    }

    /**
     * Get the user object from form
     */
    public User getUser() {
        if (!validateInput()) {
            return null;
        }

        if (user == null) {
            user = new User(
                    usernameField.getText().trim(),
                    passwordField.getText(),
                    emailField.getText().trim(),
                    fullNameField.getText().trim(),
                    User.UserRole.STUDENT);
        } else {
            // Update existing user
            user.setEmail(emailField.getText().trim());
            user.setFullName(fullNameField.getText().trim());
            if (!passwordField.getText().isEmpty()) {
                user.setPassword(passwordField.getText());
            }
            user.setActive(activeCheckBox.isSelected());
        }

        return user;
    }

    /**
     * Get the student object from form
     */
    public Student getStudent() {
        if (!validateInput()) {
            return null;
        }

        if (student == null) {
            student = new Student();
        }

        student.setEnrollmentNumber(enrollmentField.getText().trim());
        student.setDepartment(departmentField.getText().trim());
        student.setSemester(semesterSpinner.getValue());

        return student;
    }

    /**
     * Validate form input
     */
    private boolean validateInput() {
        StringBuilder errors = new StringBuilder();

        if (usernameField.getText().trim().isEmpty()) {
            errors.append("- Username is required\n");
        }

        if (user == null && passwordField.getText().isEmpty()) {
            errors.append("- Password is required for new student\n");
        }

        if (fullNameField.getText().trim().isEmpty()) {
            errors.append("- Full name is required\n");
        }

        if (emailField.getText().trim().isEmpty()) {
            errors.append("- Email is required\n");
        } else if (!isValidEmail(emailField.getText().trim())) {
            errors.append("- Invalid email format\n");
        }

        if (enrollmentField.getText().trim().isEmpty()) {
            errors.append("- Enrollment number is required\n");
        }

        if (departmentField.getText().trim().isEmpty()) {
            errors.append("- Department is required\n");
        }

        if (errors.length() > 0) {
            showError("Validation Error", errors.toString());
            return false;
        }

        return true;
    }

    private boolean isValidEmail(String email) {
        return email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
