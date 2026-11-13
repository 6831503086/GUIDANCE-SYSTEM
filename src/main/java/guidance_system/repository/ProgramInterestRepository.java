package guidance_system.repository;

import guidance_system.entity.ProgramInterest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProgramInterestRepository
        extends JpaRepository<ProgramInterest, Long> {

    @Query("""
        SELECT pi.program.programId,
               pi.program.programName,
               pi.program.faculty,
               COUNT(pi.interestId)
        FROM ProgramInterest pi
        WHERE pi.interestedAt >= :startDate
          AND pi.interestedAt < :endDate
          AND (:schoolId IS NULL
               OR pi.applicant.school.schoolId = :schoolId)
          AND (:faculty IS NULL
               OR LOWER(pi.program.faculty) = LOWER(:faculty))
        GROUP BY pi.program.programId,
                 pi.program.programName,
                 pi.program.faculty
        ORDER BY COUNT(pi.interestId) DESC
    """)
    List<Object[]> getProgramInterestRanking(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            @Param("schoolId") Long schoolId,
            @Param("faculty") String faculty
    );

    @Query("""
        SELECT CAST(pi.interestedAt AS LocalDate),
               COUNT(pi.interestId)
        FROM ProgramInterest pi
        WHERE pi.interestedAt >= :startDate
          AND pi.interestedAt < :endDate
          AND (:schoolId IS NULL
               OR pi.applicant.school.schoolId = :schoolId)
          AND (:faculty IS NULL
               OR LOWER(pi.program.faculty) = LOWER(:faculty))
        GROUP BY CAST(pi.interestedAt AS LocalDate)
        ORDER BY CAST(pi.interestedAt AS LocalDate)
    """)
    List<Object[]> getDailyInterestTrend(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            @Param("schoolId") Long schoolId,
            @Param("faculty") String faculty
    );

    @Query(
        value = """
            SELECT pi
            FROM ProgramInterest pi
            JOIN pi.applicant a
            JOIN pi.program p
            WHERE a.school.schoolId = :schoolId
              AND (:semester IS NULL OR a.term = :semester)
              AND (:academicYear IS NULL OR a.academicYear = :academicYear)
              AND (:faculty IS NULL OR
                   LOWER(p.faculty) = LOWER(:faculty))
              AND (:major IS NULL OR
                   LOWER(p.programName) LIKE
                   LOWER(CONCAT('%', :major, '%')))
              AND (
                   :keyword IS NULL OR
                   LOWER(a.nationalId) LIKE
                   LOWER(CONCAT('%', :keyword, '%')) OR
                   LOWER(CONCAT(
                       COALESCE(a.firstName, ''),
                       ' ',
                       COALESCE(a.lastName, '')
                   )) LIKE LOWER(CONCAT('%', :keyword, '%'))
              )
            ORDER BY pi.interestedAt DESC
            """,
        countQuery = """
            SELECT COUNT(pi)
            FROM ProgramInterest pi
            JOIN pi.applicant a
            JOIN pi.program p
            WHERE a.school.schoolId = :schoolId
              AND (:semester IS NULL OR a.term = :semester)
              AND (:academicYear IS NULL OR a.academicYear = :academicYear)
              AND (:faculty IS NULL OR
                   LOWER(p.faculty) = LOWER(:faculty))
              AND (:major IS NULL OR
                   LOWER(p.programName) LIKE
                   LOWER(CONCAT('%', :major, '%')))
              AND (
                   :keyword IS NULL OR
                   LOWER(a.nationalId) LIKE
                   LOWER(CONCAT('%', :keyword, '%')) OR
                   LOWER(CONCAT(
                       COALESCE(a.firstName, ''),
                       ' ',
                       COALESCE(a.lastName, '')
                   )) LIKE LOWER(CONCAT('%', :keyword, '%'))
              )
            """
    )
    Page<ProgramInterest> searchStudentInterests(
            @Param("schoolId") Long schoolId,
            @Param("semester") Integer semester,
            @Param("academicYear") Integer academicYear,
            @Param("faculty") String faculty,
            @Param("major") String major,
            @Param("keyword") String keyword,
            Pageable pageable
    );

        // FR-13: Program interest statistics grouped by program
    @Query("""
        SELECT pi.program.programId,
               pi.program.programName,
               pi.program.faculty,
               COUNT(pi.interestId)
        FROM ProgramInterest pi
        JOIN pi.applicant a
        JOIN pi.program p
        WHERE a.school.schoolId = :schoolId
          AND (:semester IS NULL OR a.term = :semester)
          AND (:academicYear IS NULL OR a.academicYear = :academicYear)
          AND (:faculty IS NULL OR
               LOWER(p.faculty) = LOWER(:faculty))
          AND (:major IS NULL OR
               LOWER(p.programName) LIKE
               LOWER(CONCAT('%', :major, '%')))
          AND (:keyword IS NULL OR
               LOWER(p.programName) LIKE
               LOWER(CONCAT('%', :keyword, '%')))
        GROUP BY pi.program.programId,
                 pi.program.programName,
                 pi.program.faculty
        ORDER BY COUNT(pi.interestId) DESC
    """)
    List<Object[]> getTeacherProgramInterestStatistics(
            @Param("schoolId") Long schoolId,
            @Param("semester") Integer semester,
            @Param("academicYear") Integer academicYear,
            @Param("faculty") String faculty,
            @Param("major") String major,
            @Param("keyword") String keyword
    );


    // FR-13: Count distinct applicants with matching program interests
    @Query("""
        SELECT COUNT(DISTINCT a.applicantId)
        FROM ProgramInterest pi
        JOIN pi.applicant a
        JOIN pi.program p
        WHERE a.school.schoolId = :schoolId
          AND (:semester IS NULL OR a.term = :semester)
          AND (:academicYear IS NULL OR a.academicYear = :academicYear)
          AND (:faculty IS NULL OR
               LOWER(p.faculty) = LOWER(:faculty))
          AND (:major IS NULL OR
               LOWER(p.programName) LIKE
               LOWER(CONCAT('%', :major, '%')))
          AND (:keyword IS NULL OR
               LOWER(p.programName) LIKE
               LOWER(CONCAT('%', :keyword, '%')))
    """)
    long countTeacherInterestedApplicants(
            @Param("schoolId") Long schoolId,
            @Param("semester") Integer semester,
            @Param("academicYear") Integer academicYear,
            @Param("faculty") String faculty,
            @Param("major") String major,
            @Param("keyword") String keyword
    );


    // FR-17: Find applicants interested in a specific program within a school
    @Query("""
        SELECT DISTINCT a.applicantId
        FROM ProgramInterest pi
        JOIN pi.applicant a
        WHERE a.school.schoolId = :schoolId
          AND pi.program.programId = :programId
    """)
    List<Long> findApplicantIdsBySchoolAndProgram(
            @Param("schoolId") Long schoolId,
            @Param("programId") Long programId
    );

}   