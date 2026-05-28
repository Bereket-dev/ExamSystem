package com.examsystem.controller;

import com.examsystem.model.Exam;
import com.examsystem.model.Question;
import com.examsystem.model.QuestionOption;
import com.examsystem.model.Teacher;
import com.examsystem.model.User;
import com.examsystem.service.TeacherService;
import com.examsystem.util.FormValidator;
import com.examsystem.util.Session;
import com.examsystem.util.UiManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Alert;
import javafx.stage.Stage;
import java.util.ArrayList;
import java.util.List;

public class QuestionManagerController implements TeacherScreen {
    @FXML
    private Label examTitleLabel;

    @FXML
    private ListView<Question> questionListView;

    @FXML
    private TextArea questionTextField;

    @FXML
    private ComboBox<Question.QuestionType> questionTypeCombo;

    @FXML
    private TextField marksField;

    @FXML
    private RadioButton option1Radio;

    @FXML
    private RadioButton option2Radio;

    @FXML
    private RadioButton option3Radio;

    @FXML
    private RadioButton option4Radio;

    @FXML
    private TextField option1Field;

    @FXML
    private TextField option2Field;

    @FXML
    private TextField option3Field;

    @FXML
    private TextField option4Field;

    private final ToggleGroup correctToggleGroup = new ToggleGroup();

    @FXML
    private Label statusLabel;

    @FXML
    private Button addQuestionButton;
    @FXML
    private Button updateQuestionButton;
    @FXML
    private Button deleteQuestionButton;

    @FXML
    private Button backButton;

    private final TeacherService teacherService = new TeacherService();
    private Exam exam;
    private ObservableList<Question> questions = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        questionTypeCombo.setItems(FXCollections.observableArrayList(
                Question.QuestionType.MCQ,
                Question.QuestionType.TRUE_FALSE));
        questionTypeCombo.getSelectionModel().selectFirst();
        updateOptionFields(questionTypeCombo.getValue());
        questionTypeCombo.valueProperty().addListener((obs, oldType, newType) -> updateOptionFields(newType));

        questionListView.setItems(questions);
        questionListView.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Question q, boolean empty) {
                super.updateItem(q, empty);
                setText(empty || q == null ? null : q.getSequenceOrder() + ". " + q.getQuestionText());
            }
        });

        option1Radio.setToggleGroup(correctToggleGroup);
        option2Radio.setToggleGroup(correctToggleGroup);
        option3Radio.setToggleGroup(correctToggleGroup);
        option4Radio.setToggleGroup(correctToggleGroup);

        addQuestionButton.setOnAction(e -> handleAddQuestion());
        updateQuestionButton.setOnAction(e -> handleUpdateQuestion());
        deleteQuestionButton.setOnAction(e -> handleDeleteQuestion());
        backButton.setOnAction(e -> returnToDashboard());
        questionListView.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, newValue) -> {
            if (newValue != null) {
                loadQuestionToForm(newValue);
            }
        });
    }

    public void setExam(Exam exam) {
        this.exam = exam;
        examTitleLabel.setText("Questions for: " + exam.getExamName());
        refreshQuestions();
    }

    @Override
    public void setTeacherContext(Teacher teacher, User user) {
        // Exam set via setExam when opened from dashboard
    }

    private void refreshQuestions() {
        questions.setAll(teacherService.getQuestions(exam.getExamId()));
    }

    private void handleAddQuestion() {
        FormValidator.clearErrors(questionTextField, marksField, option1Field,
                option2Field, option3Field, option4Field);

        FormValidator.ValidationResult validation = FormValidator.combine(
                FormValidator.required(questionTextField, "Question text"),
                FormValidator.positiveInteger(marksField, "Marks", true));

        Question.QuestionType type = questionTypeCombo.getValue();
        validation = FormValidator.combine(validation, validateOptionFields(type));

        if (!validation.isValid()) {
            FormValidator.applyResult(validation, statusLabel);
            return;
        }

        try {
            int sequence = questions.size() + 1;
            int marks = Integer.parseInt(marksField.getText().trim());
            String text = questionTextField.getText().trim();

            Question question = new Question(exam.getExamId(), text, type, marks, sequence);
            teacherService.addQuestion(question);

            if (type == Question.QuestionType.MCQ) {
                addMcqOptions(question);
            } else if (type == Question.QuestionType.TRUE_FALSE) {
                addTrueFalseOptions(question);
            }

            questionTextField.clear();
            refreshQuestions();
            statusLabel.getStyleClass().removeAll("status-error");
            statusLabel.getStyleClass().add("status-success");
            statusLabel.setText("Question added.");
        } catch (Exception e) {
            statusLabel.getStyleClass().removeAll("status-success");
            statusLabel.getStyleClass().add("status-error");
            statusLabel.setText("Error: " + e.getMessage());
        }
    }

    private void handleUpdateQuestion() {
        Question selected = questionListView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            statusLabel.setText("Select a question to update.");
            return;
        }
        try {
            applyFormToQuestion(selected);
            teacherService.updateQuestion(
                    exam.getTeacherId(),
                    selected,
                    buildOptions(selected.getQuestionType()));
            refreshQuestions();
            statusLabel.setText("Question updated.");
        } catch (Exception e) {
            statusLabel.setText("Update failed: " + e.getMessage());
        }
    }

    private void handleDeleteQuestion() {
        Question selected = questionListView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            statusLabel.setText("Select a question to delete.");
            return;
        }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setHeaderText("Delete question?");
        confirm.setContentText(selected.getQuestionText());
        var result = confirm.showAndWait();
        if (result.isEmpty() || result.get().getButtonData().isCancelButton()) {
            return;
        }
        try {
            teacherService.deleteQuestion(exam.getTeacherId(), selected.getQuestionId(), exam.getExamId());
            refreshQuestions();
            clearForm();
            statusLabel.setText("Question deleted.");
        } catch (Exception e) {
            statusLabel.setText("Delete failed: " + e.getMessage());
        }
    }

    private void loadQuestionToForm(Question question) {
        questionTextField.setText(question.getQuestionText());
        questionTypeCombo.getSelectionModel().select(question.getQuestionType());
        marksField.setText(String.valueOf(question.getMarks()));
        option1Field.clear();
        option2Field.clear();
        option3Field.clear();
        option4Field.clear();
        correctToggleGroup.selectToggle(null);

        updateOptionFields(question.getQuestionType());
        List<QuestionOption> options = teacherService.getOptions(question.getQuestionId());
        if (question.getQuestionType() == Question.QuestionType.TRUE_FALSE) {
            option1Field.setText("True");
            option2Field.setText("False");
            option3Field.clear();
            option4Field.clear();
            option3Radio.setSelected(false);
            option4Radio.setSelected(false);
            for (QuestionOption option : options) {
                if (option.isCorrect()) {
                    if (option.getOptionText().equalsIgnoreCase("true")) {
                        option1Radio.setSelected(true);
                    } else {
                        option2Radio.setSelected(true);
                    }
                }
            }
        } else {
            for (int i = 0; i < options.size() && i < 4; i++) {
                String text = options.get(i).getOptionText();
                if (i == 0) option1Field.setText(text);
                if (i == 1) option2Field.setText(text);
                if (i == 2) option3Field.setText(text);
                if (i == 3) option4Field.setText(text);
                if (options.get(i).isCorrect()) {
                    if (i == 0) option1Radio.setSelected(true);
                    if (i == 1) option2Radio.setSelected(true);
                    if (i == 2) option3Radio.setSelected(true);
                    if (i == 3) option4Radio.setSelected(true);
                }
            }
        }
    }

    private void applyFormToQuestion(Question question) {
        question.setQuestionText(questionTextField.getText().trim());
        question.setQuestionType(questionTypeCombo.getValue());
        question.setMarks(Integer.parseInt(marksField.getText().trim()));
    }

    private List<QuestionOption> buildOptions(Question.QuestionType type) {
        List<QuestionOption> options = new ArrayList<>();
        if (type == Question.QuestionType.TRUE_FALSE) {
            QuestionOption t = new QuestionOption();
            t.setOptionText("True");
            t.setCorrect(option1Radio.isSelected());
            t.setSequenceOrder(1);
            QuestionOption f = new QuestionOption();
            f.setOptionText("False");
            f.setCorrect(option2Radio.isSelected());
            f.setSequenceOrder(2);
            options.add(t);
            options.add(f);
            return options;
        }
        String[] texts = { option1Field.getText(), option2Field.getText(), option3Field.getText(), option4Field.getText() };
        for (int i = 0; i < texts.length; i++) {
            if (texts[i] != null && !texts[i].trim().isEmpty()) {
                QuestionOption op = new QuestionOption();
                op.setOptionText(texts[i].trim());
                op.setCorrect((i == 0 && option1Radio.isSelected())
                        || (i == 1 && option2Radio.isSelected())
                        || (i == 2 && option3Radio.isSelected())
                        || (i == 3 && option4Radio.isSelected()));
                op.setSequenceOrder(i + 1);
                options.add(op);
            }
        }
        return options;
    }

    private void clearForm() {
        questionTextField.clear();
        marksField.clear();
        option1Field.clear();
        option2Field.clear();
        option3Field.clear();
        option4Field.clear();
        correctToggleGroup.selectToggle(null);
        updateOptionFields(questionTypeCombo.getValue());
    }

    private FormValidator.ValidationResult validateOptionFields(Question.QuestionType type) {
        int filled = 0;
        TextField[] optionFields = { option1Field, option2Field, option3Field, option4Field };
        for (TextField field : optionFields) {
            if (field.isVisible() && field.getText() != null && !field.getText().trim().isEmpty()) {
                filled++;
            }
        }
        if (filled < 2) {
            return FormValidator.ValidationResult.fail(
                    "At least two answer options are required.", option1Field, option2Field);
        }
        if (correctToggleGroup.getSelectedToggle() == null) {
            return FormValidator.ValidationResult.fail("Select the correct answer.", option1Radio, option2Radio);
        }
        return FormValidator.ValidationResult.ok();
    }

    private void addMcqOptions(Question question) {
        TextField[] optionFields = { option1Field, option2Field, option3Field, option4Field };
        RadioButton[] radios = { option1Radio, option2Radio, option3Radio, option4Radio };
        for (int i = 0; i < optionFields.length; i++) {
            String text = optionFields[i].getText();
            if (optionFields[i].isVisible() && text != null && !text.trim().isEmpty()) {
                QuestionOption option = new QuestionOption();
                option.setQuestionId(question.getQuestionId());
                option.setOptionText(text.trim());
                option.setCorrect(radios[i].isSelected());
                option.setSequenceOrder(i + 1);
                teacherService.addOption(option);
            }
        }
    }

    private void addTrueFalseOptions(Question question) {
        QuestionOption trueOpt = new QuestionOption();
        trueOpt.setQuestionId(question.getQuestionId());
        trueOpt.setOptionText("True");
        trueOpt.setCorrect(option1Radio.isSelected());
        trueOpt.setSequenceOrder(1);
        teacherService.addOption(trueOpt);

        QuestionOption falseOpt = new QuestionOption();
        falseOpt.setQuestionId(question.getQuestionId());
        falseOpt.setOptionText("False");
        falseOpt.setCorrect(option2Radio.isSelected());
        falseOpt.setSequenceOrder(2);
        teacherService.addOption(falseOpt);
    }

    private void updateOptionFields(Question.QuestionType type) {
        boolean isTrueFalse = type == Question.QuestionType.TRUE_FALSE;
        option1Field.setVisible(true);
        option1Field.setManaged(true);
        option2Field.setVisible(true);
        option2Field.setManaged(true);
        option1Field.setPromptText(isTrueFalse ? "True" : "Option A");
        option2Field.setPromptText(isTrueFalse ? "False" : "Option B");
        option1Field.setEditable(!isTrueFalse);
        option2Field.setEditable(!isTrueFalse);
        if (isTrueFalse) {
            option1Field.setText("True");
            option2Field.setText("False");
            option1Radio.setText("Correct");
            option2Radio.setText("Correct");
            option1Radio.setVisible(true);
            option2Radio.setVisible(true);
            option1Radio.setManaged(true);
            option2Radio.setManaged(true);
            option3Radio.setVisible(false);
            option3Radio.setManaged(false);
            option4Radio.setVisible(false);
            option4Radio.setManaged(false);
            option3Field.setVisible(false);
            option3Field.setManaged(false);
            option4Field.setVisible(false);
            option4Field.setManaged(false);
            if (correctToggleGroup.getSelectedToggle() == option3Radio
                    || correctToggleGroup.getSelectedToggle() == option4Radio) {
                correctToggleGroup.selectToggle(null);
            }
        } else {
            if ("True".equals(option1Field.getText())) {
                option1Field.clear();
            }
            if ("False".equals(option2Field.getText())) {
                option2Field.clear();
            }
            option1Radio.setText("Correct");
            option2Radio.setText("Correct");
            option3Radio.setText("Correct");
            option4Radio.setText("Correct");
            option1Radio.setVisible(true);
            option1Radio.setManaged(true);
            option2Radio.setVisible(true);
            option2Radio.setManaged(true);
            option3Radio.setVisible(true);
            option3Radio.setManaged(true);
            option4Radio.setVisible(true);
            option4Radio.setManaged(true);
            option3Field.setVisible(true);
            option3Field.setManaged(true);
            option4Field.setVisible(true);
            option4Field.setManaged(true);
        }
    }

    private int parseInt(String value, int defaultValue) {
        if (value == null || value.trim().isEmpty()) {
            return defaultValue;
        }
        return Integer.parseInt(value.trim());
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
