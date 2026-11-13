
package guidance_system.repository;

import guidance_system.entity.GuidanceTeacher;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;


public interface GuidanceTeacherRepository
        extends JpaRepository<GuidanceTeacher, Long> {

    Optional<GuidanceTeacher> findByUsername(String username);

    Optional<GuidanceTeacher> findByEmail(String email);

    Optional<GuidanceTeacher> findBySchool_SchoolIdAndEmail(
            Long schoolId, String email);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    long countBySchool_SchoolId(Long schoolId);

    Optional<GuidanceTeacher> findBySchool_SchoolId(Long schoolId);

    boolean existsBySchool_SchoolId(Long schoolId);
}