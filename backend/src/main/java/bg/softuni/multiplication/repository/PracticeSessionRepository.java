package bg.softuni.multiplication.repository;

import bg.softuni.multiplication.model.PracticeSession;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PracticeSessionRepository extends JpaRepository<PracticeSession, Long> {
}