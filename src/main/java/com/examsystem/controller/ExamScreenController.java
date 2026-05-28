package com.examsystem.controller;

import com.examsystem.model.Exam;
import com.examsystem.model.Question;
import com.examsystem.model.QuestionOption;
import com.examsystem.model.StudentAnswer;
import com.examsystem.model.User;
import com.examsystem.network.NetworkManager;
import com.examsystem.rmi.RMIManager;
import com.examsystem.sync.PendingChangeType;
import com.examsystem.sync.SyncManager;
import com.examsystem.service.StudentService;
import com.examsystem.util.AutoSaveThread;
import com.examsystem.util.ExamTimerThread;
import com.examsystem.util.Session;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import com.examsystem.util.UiManager;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextArea;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

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
    private Button submitButton;

    private final StudentService studentService = new StudentService();
    private final Object saveLock = new Object();
    private Exam exam;
    private int assignmentId;
    private int attemptId;
    private List<Question> questions = new ArrayList<>();
    private Map<Integer, StudentAnswer> savedAnswers = new HashMap<>();
    private final Map<Integer, List<QuestionOption>> randomizedOptions = new HashMap<>();
    private int currentIndex = 0;
    private ExamTimerThread examTimerThread;
    private AutoSaveThread autoSaveThread;

    @FXML
    public void initialize() {
        previousButton.setOnAction(event -> navigateQuestion(-1));
        nextButton.setOnAction(event -> navigateQuestion(1));
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

        this.questions = studentService.getRandomizedQuestions(exam.getExamId());
        randomizedOptions.clear();
        studentService.getAnswersByAttempt(attemptId)
                .forEach(answer -> savedAnswers.put(answer.getQuestionId(), answer));

        int durationSeconds = exam.getDurationMinutes() <= 0 ? 60 * 5 : exam.getDurationMinutes() * 60;

        startTimer(durationSeconds);
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
            synchronized (saveLock) {
                try {
                    saveCurrentAnswerSilent();
                    Platform.runLater(() -> feedbackLabel.setText("Auto-saved at " + LocalDateTime.now().toLocalTime()));
                } catch (Exception e) {
                    Platform.runLater(() -> feedbackLabel.setText("Auto-save failed."));
                }
            }
        });
        autoSaveThread.start();
    }

    private void startTimer(int durationSeconds) {
        timerLabel.setText(formatTime(durationSeconds));
        examTimerThread = new ExamTimerThread(
                durationSeconds,
                seconds -> Platform.runLater(() -> timerLabel.setText(formatTime(seconds))),
                () -> Platform.runLater(this::autoSubmit));
        examTimerThread.start();
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
        ToggleGroup optionGroup = new ToggleGroup();

        if (question.getQuestionType() == Question.QuestionType.SHORT_ANSWER) {
            shortAnswerField.setVisible(true);
            shortAnswerField.setManaged(true);
            shortAnswerField.setText(savedAnswers.containsKey(question.getQuestionId())
                    ? Optional.ofNullable(savedAnswers.get(question.getQuestionId()).getShortAnswerText()).orElse("")
                    : "");
        } else {
            shortAnswerField.setVisible(false);
            shortAnswerField.setManaged(false);
            List<QuestionOption> options = randomizedOptions.computeIfAbsent(question.getQuestionId(), studentService::getRandomizedOptions);
            for (QuestionOption option : options) {
                RadioButton radioButton = new RadioButton(option.getOptionText());
                radioButton.setToggleGroup(optionGroup);
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
        synchronized (saveLock) {
            saveCurrentAnswerSilent();
            feedbackLabel.setText("Answer saved.");
        }
    }

    private void saveCurrentAnswerSilent() {
        if (questions.isEmpty()) {
            return;
        }

        Question currentQuestion = questions.get(currentIndex);
        StudentAnswer answer = buildAnswerForQuestion(currentQuestion);
        studentService.saveStudentAnswer(answer);
        savedAnswers.put(currentQuestion.getQuestionId(), answer);
        SyncManager.getInstance().recordPendingChange(PendingChangeType.ANSWER_SAVE);
        boolean synced = SyncManager.getInstance().tryRemoteSync(() -> {
            NetworkManager.getInstance().syncSaveAnswer(answer);
            RMIManager.getInstance().syncSaveAnswer(answer);
        });
        if (!synced) {
            NetworkManager.getInstance().syncSaveAnswer(answer);
            RMIManager.getInstance().syncSaveAnswer(answer);
        }
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
            ToggleGroup group = null;
            for (var node : optionsContainer.getChildren()) {
                if (node instanceof RadioButton rb && rb.getToggleGroup() != null) {
                    group = rb.getToggleGroup();
                    break;
                }
            }
            RadioButton selectedRadio = group == null ? null : (RadioButton) group.getSelectedToggle();
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
        synchronized (saveLock) {
            saveCurrentAnswerSilent();
        }

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
        SyncManager.getInstance().recordPendingChange(PendingChangeType.EXAM_SUBMIT);
        boolean synced = SyncManager.getInstance().tryRemoteSync(() -> {
            NetworkManager.getInstance().syncSubmitExam(assignmentId, totalMarks);
            RMIManager.getInstance().syncSubmitExam(assignmentId, totalMarks);
        });
        if (!synced) {
            NetworkManager.getInstance().syncSubmitExam(assignmentId, totalMarks);
            RMIManager.getInstance().syncSubmitExam(assignmentId, totalMarks);
        }
        SyncManager.getInstance().onExamSubmitted(synced && RMIManager.getInstance().isClientConnected());

        openResultScreen(totalMarks);
    }

    private void openResultScreen(int totalMarks) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/examsystem/fxml/ResultScreen.fxml"));
            Parent root = loader.load();
            ResultScreenController controller = loader.getController();
            controller.setResult(exam, totalMarks, exam.getTotalMarks());

            Stage stage = (Stage) submitButton.getScene().getWindow();
            UiManager.navigateToApp(stage, root, "Exam Results - ExamSystem");
        } catch (Exception e) {
            feedbackLabel.setText("Unable to open results: " + e.getMessage());
        }
    }

    private void autoSubmit() {
        feedbackLabel.setText("Time is up. Submitting exam automatically...");
        submitExam();
    }

    private void stopBackgroundTasks() {
        if (examTimerThread != null) {
            examTimerThread.shutdown();
        }
        if (autoSaveThread != null) {
            autoSaveThread.shutdown();
        }
    }

    private void disableExamControls() {
        previousButton.setDisable(true);
        nextButton.setDisable(true);
        submitButton.setDisable(true);
    }
}
