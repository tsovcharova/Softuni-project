package bg.softuni.multiplication.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import bg.softuni.multiplication.dto.SessionDtos.AnswerRequest;
import bg.softuni.multiplication.dto.SessionDtos.CreateSessionRequest;
import bg.softuni.multiplication.dto.SessionDtos.Operation;
import bg.softuni.multiplication.dto.SessionDtos.PracticeMode;
import bg.softuni.multiplication.dto.SessionDtos.PracticeQuestion;
import bg.softuni.multiplication.dto.SessionDtos.ResultResponse;
import bg.softuni.multiplication.dto.SessionDtos.SessionResponse;
import bg.softuni.multiplication.model.PracticeSession;
import bg.softuni.multiplication.repository.PracticeSessionRepository;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.mockito.ArgumentCaptor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class SessionServiceTest {
    @Mock
    private PracticeSessionRepository sessions;

    private SessionService service;

    @BeforeEach
    void setUp() {
        service = new SessionService(sessions);
    }

    @Test
    void acceptsTheSupportedTimerDurations() {
        when(sessions.save(any(PracticeSession.class))).thenAnswer(invocation -> persisted(invocation.getArgument(0), 51L));

        for (int minutes : List.of(2, 3, 5)) {
            SessionResponse response = service.create(request(Operation.MULTIPLICATION, PracticeMode.TABLE, minutes));
            assertThat(response.timeLimitMinutes()).isEqualTo(minutes);
        }
    }

    @Test
    void rejectsUnsupportedTimerDuration() {
        assertThatThrownBy(() -> service.create(request(Operation.MULTIPLICATION, PracticeMode.TABLE, 4)))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("2, 3 или 5");

        verifyNoInteractions(sessions);
    }

    @Test
    void createsTenMixedMultiplicationQuestionsWithUniqueOperands() {
        when(sessions.save(any(PracticeSession.class))).thenAnswer(invocation -> persisted(invocation.getArgument(0), 52L));

        SessionResponse response = service.create(request(Operation.MULTIPLICATION, PracticeMode.MIXED, null));
        Set<Integer> leftOperands = response.questions().stream()
                .map(PracticeQuestion::leftOperand)
                .collect(Collectors.toSet());
        Set<Integer> rightOperands = response.questions().stream()
                .map(PracticeQuestion::rightOperand)
                .collect(Collectors.toSet());

        assertThat(response.questions()).hasSize(10);
        assertThat(response.questions()).extracting(PracticeQuestion::factor).containsExactlyInAnyOrderElementsOf(
                IntStream.rangeClosed(1, 10).boxed().toList());
        assertThat(leftOperands).containsExactlyInAnyOrderElementsOf(IntStream.rangeClosed(1, 10).boxed().toList());
        assertThat(rightOperands).containsExactlyInAnyOrderElementsOf(IntStream.rangeClosed(1, 10).boxed().toList());
    }

    @Test
    void generatesDivisionQuestionsWithWholeNumberAnswers() {
        when(sessions.save(any(PracticeSession.class))).thenAnswer(invocation -> persisted(invocation.getArgument(0), 53L));

        SessionResponse response = service.create(request(Operation.DIVISION, PracticeMode.MIXED, null));

        assertThat(response.questions()).hasSize(10);
        assertThat(response.questions()).allSatisfy(question -> {
            assertThat(question.leftOperand() % question.rightOperand()).isZero();
            assertThat(question.leftOperand() / question.rightOperand()).isBetween(1, 10);
        });
    }

    @Test
    void gradesAnswersAgainstTheGeneratedQuestions() {
        when(sessions.save(any(PracticeSession.class))).thenAnswer(invocation -> persisted(invocation.getArgument(0), 54L));
        SessionResponse created = service.create(request(Operation.MULTIPLICATION, PracticeMode.TABLE, null));
        ArgumentCaptor<PracticeSession> sessionCaptor = ArgumentCaptor.forClass(PracticeSession.class);
        verify(sessions).save(sessionCaptor.capture());
        PracticeSession savedSession = sessionCaptor.getValue();
        when(sessions.findById(created.id())).thenReturn(Optional.of(savedSession));
        List<AnswerRequest> answers = created.questions().stream()
                .map(question -> new AnswerRequest(question.factor(), question.leftOperand() * question.rightOperand()))
                .map(answer -> answer.factor() == 4 ? new AnswerRequest(answer.factor(), answer.answer() + 1) : answer)
                .toList();

        ResultResponse result = service.submit(created.id(), answers);

        assertThat(result.correctCount()).isEqualTo(9);
        assertThat(result.incorrectCount()).isEqualTo(1);
        assertThat(result.details()).hasSize(10);
        verify(sessions).findById(created.id());
    }

    @Test
    void rejectsSubmissionsThatDoNotContainEveryFactor() {
        PracticeSession session = persisted(
                new PracticeSession("Ученик", 5, Operation.MULTIPLICATION.name(), PracticeMode.TABLE.name(), null), 55L);
        when(sessions.findById(55L)).thenReturn(Optional.of(session));
        List<AnswerRequest> answers = IntStream.rangeClosed(1, 9)
                .mapToObj(factor -> new AnswerRequest(factor, factor * 5))
                .toList();

        assertThatThrownBy(() -> service.submit(55L, answers))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("всяка задача от 1 до 10");
    }

    private CreateSessionRequest request(Operation operation, PracticeMode mode, Integer minutes) {
        return new CreateSessionRequest("  Ученик  ", 5, operation, mode, minutes);
    }

    private PracticeSession persisted(PracticeSession session, long id) {
        ReflectionTestUtils.setField(session, "id", id);
        return session;
    }
}