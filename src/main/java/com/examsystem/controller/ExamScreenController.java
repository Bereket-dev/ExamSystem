package com.examsystem.controller;

import com.examsystem.model.Exam;
import com.examsystem.model.Question;
import com.examsystem.model.QuestionOption;
import com.examsystem.model.StudentAnswer;
import com.examsystem.model.User;
import com.examsystem.service.StudentService;
import com.examsystem.util.AutoSaveThread;
import com.examsystem.util.Session;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextArea;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class ExamScreenController {
    @FXML
    private Label examHeaderLabel;

    @FXML
    private Label timerLabel;

    @FXML
    private Label questionNumberLabel;

    @FXML
    private Label questionTextLabel;

    @FXML
    private VBox optionsContainer;

    @FXML
    private TextArea shortAnswerField;

    @FXML
    private Label feedbackLabel;

    @FXML
    private Button previousButton;

    @FXML
    private Button nextButton;

    @FXML
    private Button saveButton;

    @FXML
    private Button submitButton;

    private final StudentService studentService = new StudentService();
    private Exam exam;
    private int assignmentId;
    private int attemptId;
    private List<Question> questions = new ArrayList<>();
    private Map<Integer, StudentAnswer> savedAnswers = new HashMap<>();
    private int currentIndex = 0;
    private Timeline examTimer;
    private int remainingSeconds;
    private ToggleGroup currentOptionGroup = new ToggleGroup();
    private AutoSaveThread autoSaveThread;

    @FXML
    public void initialize() {
        previousButton.setOnAction(event -> navigateQuestion(-1));
        nextButton.setOnAction(event -> navigateQuestion(1));
        saveButton.setOnAction(event -> saveCurrentAnswer());
        submitButton.setOnAction(event -> submitExam());
    }

    public void startExam(Exam exam, int assignmentId) {
        this.exam = exam;
        this.assignmentId = assignmentId;
        this.examHeaderLabel.setText("Exam: " + exam.getExamName());

        var attempt = studentService.createOrRetrieveAttempt(assignmentId);
        this.attemptId = attempt.getAttemptId();

        if ("submitted".equalsIgnoreCase(attempt.getSubmissionStatus())) {
            feedbackLabel.setText("This exam has already been submitted.");
            disableExamControls();
            return;
        }

        this.questions = studentService.getQuestions(exam.getExamId());
        studentService.getAnswersByAttempt(attemptId)
                .forEach(answer -> savedAnswers.put(answer.getQuestionId(), answer));

        if (exam.getDurationMinutes() <= 0) {
            this.remainingSeconds = 60 * 5;
        } else {
            this.remainingSeconds = exam.getDurationMinutes() * 60;
        }

        startTimer();
        startAutoSave();

        if (!questions.isEmpty()) {
            displayQuestion(0);
        } else {
            questionTextLabel.setText("No questions are available for this exam.");
            optionsContainer.getChildren().clear();
            shortAnswerField.setVisible(false);
            shortAnswerField.setManaged(false);
            disableExamControls();
        }
    }

    private void startAutoSave() {
        autoSaveThread = new AutoSaveThread(() -> {
            try {
                saveCurrentAnswerSilent();
                Platform.runLater(() -> feedbackLabel.setText("Auto-saved at " + LocalDateTime.now().toLocalTime()));
            } catch (Exception e) {
                Platform.runLater(() -> feedbackLabel.setText("Auto-save failed."));
            }
        });
        autoSaveThread.start();
    }

    private void startTimer() {
        timerLabel.setText(formatTime(remainingSeconds));
        examTimer = new Timeline(new KeyFrame(Duration.seconds(1), event -> {
            remainingSeconds--;
            timerLabel.setText(formatTime(remainingSeconds));
            if (remainingSeconds <= 0) {
                examTimer.stop();
                autoSubmit();
            }
        }));
        examTimer.setCycleCount(Timeline.INDEFINITE);
        examTimer.play();
    }

    private String formatTime(int seconds) {
        int minutes = Math.max(0, seconds / 60);
        int remainder = Math.max(0, seconds % 60);
        return String.format("Time left: %02d:%02d", minutes, remainder);
    }

    private void displayQuestion(int index) {
        currentIndex = Math.max(0, Math.min(index, questions.size() - 1));
        Question question = questions.get(currentIndex);

        questionNumberLabel.setText("Question " + (currentIndex + 1) + " of " + questions.size());
        questionTextLabel.setText(question.getQuestionText());
        feedbackLabel.setText("");

        optionsContainer.getChildren().clear();
        currentOptionGroup = new ToggleGroup();

        if (question.getQuestionType() == Question.QuestionType.SHORT_ANSWER) {
            shortAnswerField.setVisible(true);
            shortAnswerField.setManaged(true);
            shortAnswerField.setText(savedAnswers.containsKey(question.getQuestionId())
                    ? Optional.ofNullable(savedAnswers.get(question.getQuestionId()).getShortAnswerText()).orElse("")
                    : "");
        } else {
            shortAnswerField.setVisible(false);
            shortAnswerField.setManaged(false);
            List<QuestionOption> options = studentService.getOptions(question.getQuestionId());
            for (QuestionOption option : options) {
                RadioButton radioButton = new RadioButton(option.getOptionText());
                radioButton.setToggleGroup(currentOptionGroup);
                radioButton.setUserData(option);
                radioButton.setWrapText(true);
                radioButton.setMaxWidth(Double.MAX_VALUE);
                if (savedAnswers.containsKey(question.getQuestionId())
                        && savedAnswers.get(question.getQuestionId()).getSelectedOptionId() != null) {
                    Integer selectedOption = savedAnswers.get(question.getQuestionId()).getSelectedOptionId();
                    if (selectedOption.equals(option.getOptionId())) {
                        radioButton.setSelected(true);
                    }
                }
                optionsContainer.getChildren().add(radioButton);
            }
        }

        previousButton.setDisable(currentIndex == 0);
        nextButton.setDisable(currentIndex >= questions.size() - 1);
    }

    private void navigateQuestion(int delta) {
        saveCurrentAnswer();
        displayQuestion(currentIndex + delta);
    }

    private void saveCurrentAnswer() {
        saveCurrentAnswerSilent();
        feedbackLabel.setText("Answer saved.");
    }

    private void saveCurrentAnswerSilent() {
        if (questions.isEmpty()) {
            return;
        }

        Question currentQuestion = questions.get(currentIndex);
        StudentAnswer answer = buildAnswerForQuestion(currentQuestion);
        studentService.saveStudentAnswer(answer);
        savedAnswers.put(currentQuestion.getQuestionId(), answer);
    }

    private StudentAnswer buildAnswerForQuestion(Question currentQuestion) {
        StudentAnswer answer = new StudentAnswer();
        answer.setAttemptId(attemptId);
        answer.setQuestionId(currentQuestion.getQuestionId());

        if (currentQuestion.getQuestionType() == Question.QuestionType.SHORT_ANSWER) {
            answer.setShortAnswerText(shortAnswerField.getText().trim());
            answer.setSelectedOptionId(null);
            answer.setCorrect(null);
            answer.setMarksObtained(0);
        } else {
            RadioButton selectedRadio = (RadioButton) currentOptionGroup.getSelectedToggle();
            if (selectedRadio == null) {
                answer.setSelectedOptionId(null);
                answer.setShortAnswerText(null);
                answer.setCorrect(null);
                answer.setMarksObtained(0);
            } else {
                QuestionOption selectedOption = (QuestionOption) selectedRadio.getUserData();
                answer.setSelectedOptionId(selectedOption.getOptionId());
                answer.setShortAnswerText(null);
                answer.setCorrect(selectedOption.isCorrect());
                answer.setMarksObtained(selectedOption.isCorrect() ? currentQuestion.getMarks() : 0);
            }
        }
        return answer;
    }

    private void submitExam() {
        stopBackgroundTasks();
        saveCurrentAnswer();

        int totalMarks = studentService.getAnswersByAttempt(attemptId)
                .stream()
                .mapToInt(StudentAnswer::getMarksObtained)
                .sum();

        var attemptOptional = studentService.getAttemptByAssignment(assignmentId);
        if (attemptOptional.isEmpty()) {
            feedbackLabel.setText("Unable to complete submission. Attempt record missing.");
            return;
        }

        var attempt = attemptOptional.get();
        attempt.setEndTime(LocalDateTime.now());
        attempt.setTotalMarksObtained(totalMarks);
        attempt.setSubmissionStatus("submitted");
        studentService.updateAttempt(attempt);
        studentService.markAssignmentAttempted(assignmentId);

        openResultScreen(totalMarks);
    }

    private void openResultScreen(int totalMarks) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/examsystem/fxml/ResultScreen.fxml"));
            Parent root = loader.load();
            ResultScreenController controller = loader.getController();
            controller.setResult(exam, totalMarks, exam.getTotalMarks());

            Stage stage = (Stage) submitButton.getScene().getWindow();
            stage.setScene(new Scene(root, 900, 650));
            stage.setTitle("Exam Results - ExamSystem");
        } catch (Exception e) {
            feedbackLabel.setText("Unable to open results: " + e.getMessage());
        }
    }

    private void autoSubmit() {
        feedbackLabel.setText("Time is up. Submitting exam automatically...");
        submitExam();
    }

    private void stopBackgroundTasks() {
        if (examTimer != null) {
            examTimer.stop();
        }
        if (autoSaveThread != null) {
            autoSaveThread.shutdown();
        }
    }

    private void disableExamControls() {
        previousButton.setDisable(true);
        nextButton.setDisable(true);
        saveButton.setDisable(true);
        submitButton.setDisable(true);
    }
}
