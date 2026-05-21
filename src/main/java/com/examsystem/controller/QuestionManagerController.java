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
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

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
    private TextField sequenceField;

    @FXML
    private TextField option1Field;

    @FXML
    private TextField option2Field;

    @FXML
    private TextField option3Field;

    @FXML
    private TextField option4Field;

    @FXML
    private TextField correctOptionField;

    @FXML
    private Label statusLabel;

    @FXML
    private Button addQuestionButton;

    @FXML
    private Button backButton;

    private final TeacherService teacherService = new TeacherService();
    private Exam exam;
    private ObservableList<Question> questions = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        questionTypeCombo.setItems(FXCollections.observableArrayList(
                Question.QuestionType.MCQ,
                Question.QuestionType.TRUE_FALSE,
                Question.QuestionType.SHORT_ANSWER));
        questionTypeCombo.getSelectionModel().selectFirst();

        questionListView.setItems(questions);
        questionListView.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Question q, boolean empty) {
                super.updateItem(q, empty);
                setText(empty || q == null ? null : q.getSequenceOrder() + ". " + q.getQuestionText());
            }
        });

        addQuestionButton.setOnAction(e -> handleAddQuestion());
        backButton.setOnAction(e -> returnToDashboard());
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
        FormValidator.clearErrors(questionTextField, marksField, sequenceField, option1Field,
                option2Field, option3Field, option4Field, correctOptionField);

        FormValidator.ValidationResult validation = FormValidator.combine(
                FormValidator.required(questionTextField, "Question text"),
                FormValidator.positiveInteger(marksField, "Marks", true));

        Question.QuestionType type = questionTypeCombo.getValue();
        if (type == Question.QuestionType.MCQ) {
            validation = FormValidator.combine(validation, validateMcqFields());
        } else if (type == Question.QuestionType.TRUE_FALSE) {
            validation = FormValidator.combine(validation,
                    FormValidator.required(correctOptionField, "Correct option (1=True, 2=False)"));
        }

        if (!validation.isValid()) {
            FormValidator.applyResult(validation, statusLabel);
            return;
        }

        try {
            int sequence = parseInt(sequenceField.getText(), questions.size() + 1);
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

    private FormValidator.ValidationResult validateMcqFields() {
        int filled = 0;
        TextField[] optionFields = { option1Field, option2Field, option3Field, option4Field };
        for (TextField field : optionFields) {
            if (field.getText() != null && !field.getText().trim().isEmpty()) {
                filled++;
            }
        }
        if (filled < 2) {
            return FormValidator.ValidationResult.fail(
                    "MCQ requires at least two options.", option1Field, option2Field);
        }
        return FormValidator.positiveInteger(correctOptionField, "Correct option number", true);
    }

    private void addMcqOptions(Question question) {
        String[] texts = { option1Field.getText(), option2Field.getText(), option3Field.getText(),
                option4Field.getText() };
        int correct = parseInt(correctOptionField.getText(), 1);
        for (int i = 0; i < texts.length; i++) {
            if (texts[i] != null && !texts[i].trim().isEmpty()) {
                QuestionOption option = new QuestionOption();
                option.setQuestionId(question.getQuestionId());
                option.setOptionText(texts[i].trim());
                option.setCorrect(i + 1 == correct);
                option.setSequenceOrder(i + 1);
                teacherService.addOption(option);
            }
        }
    }

    private void addTrueFalseOptions(Question question) {
        QuestionOption trueOpt = new QuestionOption();
        trueOpt.setQuestionId(question.getQuestionId());
        trueOpt.setOptionText("True");
        trueOpt.setCorrect(correctOptionField.getText().trim().equalsIgnoreCase("1")
                || correctOptionField.getText().trim().equalsIgnoreCase("true"));
        trueOpt.setSequenceOrder(1);
        teacherService.addOption(trueOpt);

        QuestionOption falseOpt = new QuestionOption();
        falseOpt.setQuestionId(question.getQuestionId());
        falseOpt.setOptionText("False");
        falseOpt.setCorrect(!trueOpt.isCorrect());
        falseOpt.setSequenceOrder(2);
        teacherService.addOption(falseOpt);
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
