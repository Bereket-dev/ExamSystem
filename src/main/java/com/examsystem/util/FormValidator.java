package com.examsystem.util;

import javafx.scene.control.ComboBox;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.TextInputControl;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Phase 9 — Reusable form validation for JavaFX screens.
 */
public final class FormValidator {

    private FormValidator() {
    }

    public static class ValidationResult {
        private final boolean valid;
        private final String message;
        private final List<Control> invalidFields;

        public ValidationResult(boolean valid, String message, List<Control> invalidFields) {
            this.valid = valid;
            this.message = message;
            this.invalidFields = invalidFields;
        }

        public boolean isValid() {
            return valid;
        }

        public String getMessage() {
            return message;
        }

        public List<Control> getInvalidFields() {
            return invalidFields;
        }

        public static ValidationResult ok() {
            return new ValidationResult(true, "", List.of());
        }

        public static ValidationResult fail(String message, Control... fields) {
            return new ValidationResult(false, message, Arrays.asList(fields));
        }
    }

    public static void clearErrors(Control... fields) {
        for (Control field : fields) {
            if (field != null) {
                field.getStyleClass().remove("field-error");
            }
        }
    }

    public static void markInvalid(Control... fields) {
        for (Control field : fields) {
            if (field != null && !field.getStyleClass().contains("field-error")) {
                field.getStyleClass().add("field-error");
            }
        }
    }

    public static void applyResult(ValidationResult result, Label statusLabel) {
        if (result.isValid()) {
            if (statusLabel != null) {
                statusLabel.getStyleClass().removeAll("status-error");
                statusLabel.getStyleClass().add("status-success");
                statusLabel.setText("");
            }
            return;
        }
        markInvalid(result.getInvalidFields().toArray(new Control[0]));
        if (statusLabel != null) {
            statusLabel.getStyleClass().removeAll("status-success");
            statusLabel.getStyleClass().add("status-error");
            statusLabel.setText(result.getMessage());
        }
    }

    public static ValidationResult required(TextInputControl field, String fieldName) {
        if (field == null || field.getText() == null || field.getText().trim().isEmpty()) {
            return ValidationResult.fail(fieldName + " is required.", field);
        }
        return ValidationResult.ok();
    }

    public static ValidationResult requiredSelection(ComboBox<?> combo, String fieldName) {
        if (combo == null || combo.getSelectionModel().getSelectedItem() == null) {
            return ValidationResult.fail("Please select " + fieldName + ".", combo);
        }
        return ValidationResult.ok();
    }

    public static ValidationResult positiveInteger(TextInputControl field, String fieldName, boolean required) {
        String text = field != null ? field.getText().trim() : "";
        if (text.isEmpty()) {
            return required ? ValidationResult.fail(fieldName + " is required.", field) : ValidationResult.ok();
        }
        try {
            int value = Integer.parseInt(text);
            if (value <= 0) {
                return ValidationResult.fail(fieldName + " must be greater than 0.", field);
            }
            return ValidationResult.ok();
        } catch (NumberFormatException e) {
            return ValidationResult.fail(fieldName + " must be a valid number.", field);
        }
    }

    public static ValidationResult dateOptional(TextInputControl field, String fieldName) {
        String text = field != null ? field.getText().trim() : "";
        if (text.isEmpty()) {
            return ValidationResult.ok();
        }
        try {
            LocalDate.parse(text);
            return ValidationResult.ok();
        } catch (DateTimeParseException e) {
            return ValidationResult.fail(fieldName + " must be YYYY-MM-DD.", field);
        }
    }

    public static ValidationResult timeOptional(TextInputControl field, String fieldName) {
        String text = field != null ? field.getText().trim() : "";
        if (text.isEmpty()) {
            return ValidationResult.ok();
        }
        try {
            LocalTime.parse(text);
            return ValidationResult.ok();
        } catch (DateTimeParseException e) {
            return ValidationResult.fail(fieldName + " must be HH:MM:SS.", field);
        }
    }

    public static ValidationResult combine(ValidationResult... results) {
        List<Control> invalid = new ArrayList<>();
        StringBuilder message = new StringBuilder();
        boolean valid = true;

        for (ValidationResult result : results) {
            if (!result.isValid()) {
                valid = false;
                invalid.addAll(result.getInvalidFields());
                if (message.isEmpty()) {
                    message.append(result.getMessage());
                }
            }
        }

        if (valid) {
            return ValidationResult.ok();
        }
        return new ValidationResult(false, message.toString(), invalid);
    }
}
