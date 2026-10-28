package bg.softuni.multiplication.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.NotBlank;
import java.util.List;

public final class SessionDtos {
    private SessionDtos() {
    }

        public record CreateSessionRequest(
            @NotBlank @Size(max = 80) String participantName,
            @Min(1) @Max(10) int multiplier,
            @NotNull Operation operation,
            @NotNull PracticeMode mode,
            @Min(2) @Max(5) Integer timeLimitMinutes) {
    }

        public record SessionResponse(
            long id,
            String participantName,
            int multiplier,
            Operation operation,
                PracticeMode mode,
            Integer timeLimitMinutes,
                List<PracticeQuestion> questions) {
    }

            public record PracticeQuestion(int factor, int leftOperand, int rightOperand) {
            }

        public record AnswerRequest(@NotNull @Min(1) @Max(10) Integer factor, Integer answer) {
    }

    public record SubmitRequest(@NotNull @Size(min = 10, max = 10) List<@Valid AnswerRequest> answers) {
    }

    public record AnswerResult(int factor, int expected, Integer submitted, boolean correct) {
    }

    public record ResultResponse(int correctCount, int incorrectCount, List<AnswerResult> details) {
    }

    public enum Operation {
        MULTIPLICATION,
        DIVISION
    }

    public enum PracticeMode {
        TABLE,
        MIXED
    }
}