package com.solcho.bootcamp.blank.service;

import com.solcho.bootcamp.blank.dto.BlankQuestionResponse;
import com.solcho.bootcamp.blank.dto.SubmitAnswerResponse;
import com.solcho.bootcamp.blank.entity.BlankAnswer;
import com.solcho.bootcamp.blank.entity.BlankQuestion;
import com.solcho.bootcamp.blank.repository.BlankAnswerRepository;
import com.solcho.bootcamp.blank.repository.BlankQuestionRepository;
import com.solcho.bootcamp.common.exception.ApiException;
import com.solcho.bootcamp.unit.entity.Unit;
import com.solcho.bootcamp.unit.repository.UnitRepository;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BlankService {

    private final BlankQuestionRepository questionRepository;
    private final BlankAnswerRepository answerRepository;
    private final UnitRepository unitRepository;

    public BlankService(BlankQuestionRepository questionRepository,
                        BlankAnswerRepository answerRepository,
                        UnitRepository unitRepository) {
        this.questionRepository = questionRepository;
        this.answerRepository = answerRepository;
        this.unitRepository = unitRepository;
    }

    /**
     * 단원의 빈칸 문제 목록. userId 가 있으면(로그인) 이전 답안/정답 여부를 함께 채운다.
     * 정답 문자열은 응답에 포함하지 않는다.
     */
    @Transactional(readOnly = true)
    public List<BlankQuestionResponse> getBlanks(String code, Long userId) {
        Unit unit = unitRepository.findByCode(code)
                .orElseThrow(() -> ApiException.notFound("존재하지 않는 단원입니다."));
        List<BlankQuestion> questions = questionRepository.findByUnitIdOrderBySortOrderAsc(unit.getId());

        final Map<Long, BlankAnswer> answerByQuestion;
        if (userId != null && !questions.isEmpty()) {
            List<Long> ids = questions.stream().map(BlankQuestion::getId).toList();
            answerByQuestion = answerRepository.findByUserIdAndBlankQuestionIdIn(userId, ids).stream()
                    .collect(Collectors.toMap(BlankAnswer::getBlankQuestionId, Function.identity()));
        } else {
            answerByQuestion = Map.of();
        }

        return questions.stream().map(q -> {
            BlankAnswer a = answerByQuestion.get(q.getId());
            return new BlankQuestionResponse(
                    q.getId(),
                    q.getSentenceTemplate(),
                    q.getScore(),
                    q.getSortOrder(),
                    a != null ? a.getUserAnswer() : null,
                    a != null ? a.isCorrect() : null);
        }).toList();
    }

    /** 답안 채점 + upsert. 정답 문자열을 응답으로 돌려준다(오답 시 회색 표시용). */
    @Transactional
    public SubmitAnswerResponse submitAnswer(Long userId, Long questionId, String answer) {
        BlankQuestion question = questionRepository.findById(questionId)
                .orElseThrow(() -> ApiException.notFound("존재하지 않는 문제입니다."));
        boolean correct = question.isCorrect(answer);

        answerRepository.findByUserIdAndBlankQuestionId(userId, questionId)
                .ifPresentOrElse(
                        existing -> existing.resubmit(answer, correct),
                        () -> answerRepository.save(BlankAnswer.builder()
                                .userId(userId)
                                .blankQuestionId(questionId)
                                .userAnswer(answer)
                                .isCorrect(correct)
                                .build()));

        return new SubmitAnswerResponse(correct, question.getAnswer());
    }
}
