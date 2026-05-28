package com.examsystem.controller;

import com.examsystem.model.Course;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Controller for Course Dialog.
 * Handles adding and editing courses.
 */
public class CourseDialogController {
    private static final Logger logger = LoggerFactory.getLogger(CourseDialogController.class);

    @FXML
    private TextField courseCodeField;
    @FXML
    private TextField courseNameField;
    @FXML
    private TextArea descriptionArea;
    @FXML
    private TextField departmentField;
    @FXML
    private Spinner<Integer> creditsSpinner;
    @FXML
    private Spinner<Integer> semesterSpinner;
    @FXML
    private CheckBox activeCheckBox;

    private Course course;

    @FXML
    public void initialize() {
        setupSpinners();
    }

    private void setupSpinners() {
        SpinnerValueFactory<Integer> creditsFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 10, 3);
        creditsSpinner.setValueFactory(creditsFactory);

        SpinnerValueFactory<Integer> semesterFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 8, 1);
        semesterSpinner.setValueFactory(semesterFactory);
    }

    /**
     * Set course for editing
     */
    public void setCourse(Course course) {
        this.course = course;

        courseCodeField.setText(course.getCourseCode());
        courseCodeField.setDisable(true);
        courseNameField.setText(course.getCourseName());
        descriptionArea.setText(course.getDescription());
        departmentField.setText(course.getDepartment());
        creditsSpinner.getValueFactory().setValue(course.getCredits());
        semesterSpinner.getValueFactory().setValue(course.getSemester());
        activeCheckBox.setSelected(course.isActive());
    }

    /**
     * Get the course object from form
     */
    public Course getCourse() {
        if (!validateInput()) {
            return null;
        }

        if (course == null) {
            course = new Course(
                    courseCodeField.getText().trim(),
                    courseNameField.getText().trim(),
                    descriptionArea.getText().trim(),
                    departmentField.getText().trim(),
                    creditsSpinner.getValue(),
                    semesterSpinner.getValue());
            course.setActive(activeCheckBox.isSelected());
        } else {
            // Update existing course
            course.setCourseName(courseNameField.getText().trim());
            course.setDescription(descriptionArea.getText().trim());
            course.setDepartment(departmentField.getText().trim());
            course.setCredits(creditsSpinner.getValue());
            course.setSemester(semesterSpinner.getValue());
            course.setActive(activeCheckBox.isSelected());
        }

        return course;
    }

    /**
     * Validate form input
     */
    private boolean validateInput() {
        StringBuilder errors = new StringBuilder();

        if (courseCodeField.getText().trim().isEmpty()) {
            errors.append("- Course code is required\n");
        }

        if (courseNameField.getText().trim().isEmpty()) {
            errors.append("- Course name is required\n");
        }

        if (departmentField.getText().trim().isEmpty()) {
            errors.append("- Department is required\n");
        }

        if (descriptionArea.getText().trim().isEmpty()) {
            errors.append("- Description is required\n");
        }

        if (errors.length() > 0) {
            showError("Validation Error", errors.toString());
            return false;
        }

        return true;
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
