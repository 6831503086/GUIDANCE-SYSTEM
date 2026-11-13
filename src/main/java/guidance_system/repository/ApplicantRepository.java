
package guidance_system.repository;

import guidance_system.entity.Applicant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

@Repository
public interface ApplicantRepository extends JpaRepository<Applicant, Long> {

    long countBySchool_SchoolId(Long schoolId);

    @Query("SELECT COUNT(DISTINCT a.school.schoolId) FROM Applicant a")
    long countDistinctSchoolsWithApplicants();

    @Query("""
        SELECT a FROM Applicant a
        WHERE
            (:keyword IS NULL OR
                LOWER(a.nationalId) LIKE LOWER(CONCAT('%', :keyword, '%')) OR
                LOWER(CONCAT(
                    COALESCE(a.firstName, ''), ' ',
                    COALESCE(a.lastName, '')
                )) LIKE LOWER(CONCAT('%', :keyword, '%')) OR
                LOWER(a.firstName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR
                LOWER(a.lastName) LIKE LOWER(CONCAT('%', :keyword, '%'))
            )
        AND (:schoolId IS NULL OR a.school.schoolId = :schoolId)
        ORDER BY a.applicantId DESC
        """)
    Page<Applicant> searchStudents(
            @Param("keyword") String keyword,
            @Param("schoolId") Long schoolId,
            Pageable pageable);
}
