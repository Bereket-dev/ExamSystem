package com.examsystem.repository;

import com.examsystem.db.DatabaseConnection;
import com.examsystem.model.Question;
import com.examsystem.model.QuestionOption;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class QuestionRepositoryImpl implements QuestionRepository {
    private static final Logger logger = LoggerFactory.getLogger(QuestionRepositoryImpl.class);

    private static final String SELECT_QUESTION_BY_EXAM = "SELECT * FROM questions WHERE exam_id = ? ORDER BY sequence_order";
    private static final String SELECT_OPTIONS_BY_QUESTION = "SELECT * FROM options WHERE question_id = ? ORDER BY sequence_order";

    @Override
    public List<Question> findByExamId(int examId) {
        List<Question> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(SELECT_QUESTION_BY_EXAM)) {
            stmt.setInt(1, examId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Question question = new Question();
                    question.setQuestionId(rs.getInt("question_id"));
                    question.setExamId(rs.getInt("exam_id"));
                    question.setQuestionText(rs.getString("question_text"));
                    question.setMarks(rs.getInt("marks"));
                    question.setSequenceOrder(rs.getInt("sequence_order"));
                    question.setDifficultyLevel(
                            Question.DifficultyLevel.valueOf(rs.getString("difficulty_level").toUpperCase()));
                    String type = rs.getString("question_type");
                    if ("mcq".equalsIgnoreCase(type)) {
                        question.setQuestionType(Question.QuestionType.MCQ);
                    } else if ("true_false".equalsIgnoreCase(type)) {
                        question.setQuestionType(Question.QuestionType.TRUE_FALSE);
                    } else {
                        question.setQuestionType(Question.QuestionType.SHORT_ANSWER);
                    }
                    list.add(question);
                }
            }
        } catch (SQLException e) {
            logger.error("Error loading questions for exam", e);
        }
        return list;
    }

    @Override
    public List<QuestionOption> findOptionsByQuestionId(int questionId) {
        List<QuestionOption> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(SELECT_OPTIONS_BY_QUESTION)) {
            stmt.setInt(1, questionId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    QuestionOption option = new QuestionOption();
                    option.setOptionId(rs.getInt("option_id"));
                    option.setQuestionId(rs.getInt("question_id"));
                    option.setOptionText(rs.getString("option_text"));
                    option.setCorrect(rs.getBoolean("is_correct"));
                    option.setSequenceOrder(rs.getInt("sequence_order"));
                    list.add(option);
                }
            }
        } catch (SQLException e) {
            logger.error("Error loading options for question", e);
        }
        return list;
    }
}
