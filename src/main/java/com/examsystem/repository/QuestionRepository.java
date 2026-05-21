package com.examsystem.repository;

import com.examsystem.model.Question;
import com.examsystem.model.QuestionOption;

import java.util.List;

public interface QuestionRepository {
    List<Question> findByExamId(int examId);

    List<QuestionOption> findOptionsByQuestionId(int questionId);

    void saveQuestion(Question question);

    void saveOption(QuestionOption option);

    int countByExamId(int examId);
}
