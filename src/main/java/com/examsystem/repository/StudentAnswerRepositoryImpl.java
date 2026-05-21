package com.examsystem.repository;

import com.examsystem.db.DatabaseConnection;
import com.examsystem.model.StudentAnswer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class StudentAnswerRepositoryImpl implements StudentAnswerRepository {
    private static final Logger logger = LoggerFactory.getLogger(StudentAnswerRepositoryImpl.class);

    private static final String SELECT_BY_ATTEMPT_QUESTION = "SELECT * FROM student_answers WHERE attempt_id = ? AND question_id = ?";
    private static final String INSERT_ANSWER = "INSERT INTO student_answers (attempt_id, question_id, selected_option_id, short_answer_text, is_correct, marks_obtained) VALUES (?, ?, ?, ?, ?, ?)";
    private static final String UPDATE_ANSWER = "UPDATE student_answers SET selected_option_id = ?, short_answer_text = ?, is_correct = ?, marks_obtained = ? WHERE answer_id = ?";
    private static final String SELECT_BY_ATTEMPT = "SELECT * FROM student_answers WHERE attempt_id = ?";

    @Override
    public Optional<StudentAnswer> findByAttemptAndQuestion(int attemptId, int questionId) {
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(SELECT_BY_ATTEMPT_QUESTION)) {
            stmt.setInt(1, attemptId);
            stmt.setInt(2, questionId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSet(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Error finding student answer", e);
        }
        return Optional.empty();
    }

    @Override
    public void saveOrUpdate(StudentAnswer answer) {
        Optional<StudentAnswer> existing = findByAttemptAndQuestion(answer.getAttemptId(), answer.getQuestionId());
        if (existing.isPresent()) {
            StudentAnswer current = existing.get();
            answer.setAnswerId(current.getAnswerId());
            updateAnswer(answer);
        } else {
            insertAnswer(answer);
        }
    }

    @Override
    public List<StudentAnswer> findByAttemptId(int attemptId) {
        List<StudentAnswer> answers = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(SELECT_BY_ATTEMPT)) {
            stmt.setInt(1, attemptId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    answers.add(mapResultSet(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Error finding answers by attempt", e);
        }
        return answers;
    }

    private void insertAnswer(StudentAnswer answer) {
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(INSERT_ANSWER, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, answer.getAttemptId());
            stmt.setInt(2, answer.getQuestionId());
            if (answer.getSelectedOptionId() == null) {
                stmt.setNull(3, Types.INTEGER);
            } else {
                stmt.setInt(3, answer.getSelectedOptionId());
            }
            stmt.setString(4, answer.getShortAnswerText());
            if (answer.isCorrect() == null) {
                stmt.setNull(5, Types.BOOLEAN);
            } else {
                stmt.setBoolean(5, answer.isCorrect());
            }
            stmt.setInt(6, answer.getMarksObtained());
            stmt.executeUpdate();
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    answer.setAnswerId(rs.getInt(1));
                }
            }
        } catch (SQLException e) {
            logger.error("Error inserting student answer", e);
        }
    }

    private void updateAnswer(StudentAnswer answer) {
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(UPDATE_ANSWER)) {
            if (answer.getSelectedOptionId() == null) {
                stmt.setNull(1, Types.INTEGER);
            } else {
                stmt.setInt(1, answer.getSelectedOptionId());
            }
            stmt.setString(2, answer.getShortAnswerText());
            if (answer.isCorrect() == null) {
                stmt.setNull(3, Types.BOOLEAN);
            } else {
                stmt.setBoolean(3, answer.isCorrect());
            }
            stmt.setInt(4, answer.getMarksObtained());
            stmt.setInt(5, answer.getAnswerId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            logger.error("Error updating student answer", e);
        }
    }

    private StudentAnswer mapResultSet(ResultSet rs) throws SQLException {
        StudentAnswer answer = new StudentAnswer();
        answer.setAnswerId(rs.getInt("answer_id"));
        answer.setAttemptId(rs.getInt("attempt_id"));
        answer.setQuestionId(rs.getInt("question_id"));
        int optionId = rs.getInt("selected_option_id");
        if (!rs.wasNull()) {
            answer.setSelectedOptionId(optionId);
        }
        answer.setShortAnswerText(rs.getString("short_answer_text"));
        boolean correct = rs.getBoolean("is_correct");
        if (!rs.wasNull()) {
            answer.setCorrect(correct);
        }
        answer.setMarksObtained(rs.getInt("marks_obtained"));
        return answer;
    }
}
