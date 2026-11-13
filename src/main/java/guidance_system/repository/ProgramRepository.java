
package guidance_system.repository;

import guidance_system.entity.Program;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProgramRepository extends JpaRepository<Program, Long> {

    @Query("""
        SELECT p FROM Program p
        JOIN p.entryGroup eg
        WHERE (:keyword IS NULL OR LOWER(p.programName)
               LIKE LOWER(CONCAT('%', :keyword, '%')))
          AND (:academicYear IS NULL OR eg.academicYear = :academicYear)
          AND (:round IS NULL OR LOWER(eg.entrygroupName)
               = LOWER(:round))
        """)
    List<Program> searchPrograms(
            @Param("keyword") String keyword,
            @Param("academicYear") Integer academicYear,
            @Param("round") String round
    );
}
