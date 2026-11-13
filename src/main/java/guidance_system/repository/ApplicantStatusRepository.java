package guidance_system.repository;

import guidance_system.entity.ApplicantStatus;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ApplicantStatusRepository extends JpaRepository<ApplicantStatus, String> {
}