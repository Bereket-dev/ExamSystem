package com.examsystem.controller;

import com.examsystem.model.User;
import com.examsystem.model.Teacher;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Controller for Teacher Dialog.
 * Handles adding and editing teacher accounts.
 */
public class TeacherDialogController {
    private static final Logger logger = LoggerFactory.getLogger(TeacherDialogController.class);

    @FXML
    private TextField usernameField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private TextField fullNameField;
    @FXML
    private TextField emailField;
    @FXML
    private TextField departmentField;
    @FXML
    private TextField qualificationField;
    @FXML
    private Spinner<Integer> experienceSpinner;
    @FXML
    private CheckBox activeCheckBox;

    private User user;
    private Teacher teacher;

    @FXML
    public void initialize() {
        setupSpinner();
    }

    private void setupSpinner() {
        SpinnerValueFactory<Integer> valueFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 50, 0);
        experienceSpinner.setValueFactory(valueFactory);
    }

    /**
     * Set teacher for editing
     */
    public void setTeacher(User user, Teacher teacher) {
        this.user = user;
        this.teacher = teacher;

        usernameField.setText(user.getUsername());
        fullNameField.setText(user.getFullName());
        emailField.setText(user.getEmail());
        usernameField.setDisable(true);

        departmentField.setText(teacher.getDepartment());
        qualificationField.setText(teacher.getQualification());
        experienceSpinner.getValueFactory().setValue(teacher.getExperienceYears());
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
                    User.UserRole.TEACHER);
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
     * Get the teacher object from form
     */
    public Teacher getTeacher() {
        if (!validateInput()) {
            return null;
        }

        if (teacher == null) {
            teacher = new Teacher();
        }

        teacher.setDepartment(departmentField.getText().trim());
        teacher.setQualification(qualificationField.getText().trim());
        teacher.setExperienceYears(experienceSpinner.getValue());

        return teacher;
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
            errors.append("- Password is required for new teacher\n");
        }

        if (fullNameField.getText().trim().isEmpty()) {
            errors.append("- Full name is required\n");
        }

        if (emailField.getText().trim().isEmpty()) {
            errors.append("- Email is required\n");
        } else if (!isValidEmail(emailField.getText().trim())) {
            errors.append("- Invalid email format\n");
        }

        if (departmentField.getText().trim().isEmpty()) {
            errors.append("- Department is required\n");
        }

        if (qualificationField.getText().trim().isEmpty()) {
            errors.append("- Qualification is required\n");
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
