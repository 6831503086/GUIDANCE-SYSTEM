
package guidance_system.repository;

import guidance_system.entity.ApplicantSelection;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface ApplicantSelectionRepository
        extends JpaRepository<ApplicantSelection, Long> {

    List<ApplicantSelection> findByApplicant_ApplicantIdIn(
            Collection<Long> applicantIds);
}
