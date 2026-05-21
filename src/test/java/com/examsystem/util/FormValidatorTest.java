package com.examsystem.util;

import javafx.scene.control.TextField;
import org.junit.Test;

import static org.junit.Assert.*;

public class FormValidatorTest {

    @Test
    public void requiredFailsOnEmpty() {
        TextField field = new TextField("");
        FormValidator.ValidationResult result = FormValidator.required(field, "Username");
        assertFalse(result.isValid());
        assertTrue(result.getMessage().contains("Username"));
    }

    @Test
    public void positiveIntegerRejectsZero() {
        TextField field = new TextField("0");
        FormValidator.ValidationResult result = FormValidator.positiveInteger(field, "Marks", true);
        assertFalse(result.isValid());
    }

    @Test
    public void dateOptionalAcceptsEmpty() {
        TextField field = new TextField("");
        assertTrue(FormValidator.dateOptional(field, "Date").isValid());
    }

    @Test
    public void dateOptionalRejectsInvalidFormat() {
        TextField field = new TextField("not-a-date");
        assertFalse(FormValidator.dateOptional(field, "Date").isValid());
    }
}
