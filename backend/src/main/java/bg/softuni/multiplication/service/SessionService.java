package bg.softuni.multiplication.service;

import bg.softuni.multiplication.dto.SessionDtos.AnswerRequest;
import bg.softuni.multiplication.dto.SessionDtos.AnswerResult;
import bg.softuni.multiplication.dto.SessionDtos.CreateSessionRequest;
import bg.softuni.multiplication.dto.SessionDtos.Operation;
import bg.softuni.multiplication.dto.SessionDtos.PracticeMode;
import bg.softuni.multiplication.dto.SessionDtos.PracticeQuestion;
import bg.softuni.multiplication.dto.SessionDtos.ResultResponse;
import bg.softuni.multiplication.dto.SessionDtos.SessionResponse;
import bg.softuni.multiplication.repository.PracticeSessionRepository;
import bg.softuni.multiplication.model.PracticeSession;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class SessionService {
    private static final Set<Integer> FACTORS = IntStream.rangeClosed(1, 10).boxed().collect(Collectors.toSet());
    private final PracticeSessionRepository sessions;

    public SessionService(PracticeSessionRepository sessions) {
        this.sessions = sessions;
    }

    public SessionResponse create(CreateSessionRequest request) {
        if (request.timeLimitMinutes() != null && !Set.of(2, 3, 5).contains(request.timeLimitMinutes())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Времето трябва да е 2, 3 или 5 минути.");
        }

        PracticeSession session = sessions.save(new PracticeSession(
                request.participantName().trim(),
                request.multiplier(),
                request.operation().name(),
                request.mode().name(),
                request.timeLimitMinutes()));
        return new SessionResponse(session.getId(), session.getParticipantName(), session.getMultiplier(),
                Operation.valueOf(session.getOperation()), PracticeMode.valueOf(session.getMode()),
                session.getTimeLimitMinutes(), questionsFor(session));
    }

    @Transactional
    public ResultResponse submit(long id, List<AnswerRequest> submittedAnswers) {
        PracticeSession session = sessions.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Сесията не е намерена."));
        Map<Integer, AnswerRequest> answers = submittedAnswers.stream()
                .collect(Collectors.toMap(AnswerRequest::factor, Function.identity(), (first, second) -> first));
        if (!answers.keySet().equals(FACTORS)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Изпратете отговор за всяка задача от 1 до 10.");
        }

        Map<Integer, PracticeQuestion> questionsByFactor = questionsFor(session).stream()
            .collect(Collectors.toMap(PracticeQuestion::factor, Function.identity()));
        List<AnswerResult> details = IntStream.rangeClosed(1, 10)
                .mapToObj(factor -> {
                PracticeQuestion question = questionsByFactor.get(factor);
                int expected = Operation.MULTIPLICATION.name().equals(session.getOperation())
                    ? question.leftOperand() * question.rightOperand()
                    : question.leftOperand() / question.rightOperand();
                Integer submitted = answers.get(factor).answer();
                return new AnswerResult(factor, expected, submitted, submitted != null && expected == submitted);
                })
                .toList();
        int correct = (int) details.stream().filter(AnswerResult::correct).count();
        session.complete(correct, details.size() - correct);
        return new ResultResponse(correct, details.size() - correct, details);
    }

    private List<PracticeQuestion> questionsFor(PracticeSession session) {
        List<Integer> firstNumbers = new ArrayList<>(IntStream.rangeClosed(1, 10).boxed().toList());
        List<Integer> secondNumbers = new ArrayList<>(IntStream.rangeClosed(1, 10).boxed().toList());
        if (PracticeMode.MIXED.name().equals(session.getMode())) {
            Collections.shuffle(firstNumbers, new Random(session.getId()));
            Collections.shuffle(secondNumbers, new Random(session.getId() ^ 0x9E3779B97F4A7C15L));
        }

        List<PracticeQuestion> questions = IntStream.range(0, 10)
                .mapToObj(index -> {
                    int factor = index + 1;
                    if (PracticeMode.MIXED.name().equals(session.getMode())) {
                        int first = firstNumbers.get(index);
                        int second = secondNumbers.get(index);
                        return Operation.MULTIPLICATION.name().equals(session.getOperation())
                                ? new PracticeQuestion(factor, first, second)
                                : new PracticeQuestion(factor, first * second, first);
                    }

                    return Operation.MULTIPLICATION.name().equals(session.getOperation())
                            ? new PracticeQuestion(factor, session.getMultiplier(), factor)
                            : new PracticeQuestion(factor, session.getMultiplier() * factor, session.getMultiplier());
                })
                .collect(Collectors.toCollection(ArrayList::new));
            Collections.shuffle(questions, new Random(session.getId() ^ 0xD1B54A32D192ED03L));
            return List.copyOf(questions);
    }
}