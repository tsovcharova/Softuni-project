package bg.softuni.multiplication.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "practice_sessions")
public class PracticeSession {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String participantName;
    private int multiplier;
    private String operation;
    private String mode;
    private Integer timeLimitMinutes;
    private Integer correctCount;
    private Integer incorrectCount;
    private Instant createdAt;

    protected PracticeSession() {
    }

    public PracticeSession(String participantName, int multiplier, String operation, String mode, Integer timeLimitMinutes) {
        this.participantName = participantName;
        this.multiplier = multiplier;
        this.operation = operation;
        this.mode = mode;
        this.timeLimitMinutes = timeLimitMinutes;
        this.createdAt = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public int getMultiplier() {
        return multiplier;
    }

    public String getParticipantName() {
        return participantName;
    }

    public String getOperation() {
        return operation;
    }

    public String getMode() {
        return mode;
    }

    public Integer getTimeLimitMinutes() {
        return timeLimitMinutes;
    }

    public void complete(int correctCount, int incorrectCount) {
        this.correctCount = correctCount;
        this.incorrectCount = incorrectCount;
    }
}