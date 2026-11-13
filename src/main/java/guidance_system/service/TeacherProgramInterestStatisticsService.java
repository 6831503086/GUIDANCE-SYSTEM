package guidance_system.service;

import guidance_system.dto.TeacherProgramInterestStatisticsResponse;
import guidance_system.dto.TeacherProgramInterestStatisticsResponse.ProgramStatistics;
import guidance_system.entity.GuidanceTeacher;
import guidance_system.repository.GuidanceTeacherRepository;
import guidance_system.repository.ProgramInterestRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class TeacherProgramInterestStatisticsService {

    private final ProgramInterestRepository programInterestRepository;
    private final GuidanceTeacherRepository guidanceTeacherRepository;

    public TeacherProgramInterestStatisticsService(
            ProgramInterestRepository programInterestRepository,
            GuidanceTeacherRepository guidanceTeacherRepository) {

        this.programInterestRepository = programInterestRepository;
        this.guidanceTeacherRepository = guidanceTeacherRepository;
    }

    @Transactional(readOnly = true)
    public TeacherProgramInterestStatisticsResponse getStatistics(
            String username,
            Integer semester,
            Integer academicYear,
            String faculty,
            String major,
            String keyword) {

        GuidanceTeacher teacher = guidanceTeacherRepository
                .findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Guidance teacher account not found."));

        Long schoolId = teacher.getSchool().getSchoolId();

        String normalizedFaculty = normalize(faculty);
        String normalizedMajor = normalize(major);
        String normalizedKeyword = normalize(keyword);

        List<Object[]> rows =
                programInterestRepository.getTeacherProgramInterestStatistics(
                        schoolId,
                        semester,
                        academicYear,
                        normalizedFaculty,
                        normalizedMajor,
                        normalizedKeyword);

        long numberOfApplicants =
                programInterestRepository.countTeacherInterestedApplicants(
                        schoolId,
                        semester,
                        academicYear,
                        normalizedFaculty,
                        normalizedMajor,
                        normalizedKeyword);

        long totalInterests = rows.stream()
                .mapToLong(row -> ((Number) row[3]).longValue())
                .sum();

        long numberOfPrograms = rows.size();

        List<ProgramStatistics> statistics = new ArrayList<>();

        for (int i = 0; i < rows.size(); i++) {

            Object[] row = rows.get(i);

            long interestCount = ((Number) row[3]).longValue();

            double proportion = totalInterests == 0
                    ? 0
                    : (interestCount * 100.0) / totalInterests;

            statistics.add(new ProgramStatistics(
                    i + 1,
                    ((Number) row[0]).longValue(),
                    (String) row[1],
                    (String) row[2],
                    interestCount,
                    Math.round(proportion * 100.0) / 100.0
            ));
        }

        return new TeacherProgramInterestStatisticsResponse(
                numberOfApplicants,
                numberOfPrograms,
                totalInterests,
                statistics
        );
    }

    private String normalize(String value) {
        return value == null || value.isBlank()
                ? null
                : value.trim();
    }
}