
package guidance_system.service;

import guidance_system.dto.StudentInterestReportResponse;
import guidance_system.entity.ApplicantSelection;
import guidance_system.entity.GuidanceTeacher;
import guidance_system.entity.ProgramInterest;
import guidance_system.repository.ApplicantSelectionRepository;
import guidance_system.repository.GuidanceTeacherRepository;
import guidance_system.repository.ProgramInterestRepository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class StudentInterestReportService {

    private final ProgramInterestRepository programInterestRepository;
    private final ApplicantSelectionRepository applicantSelectionRepository;
    private final GuidanceTeacherRepository guidanceTeacherRepository;

    public StudentInterestReportService(
            ProgramInterestRepository programInterestRepository,
            ApplicantSelectionRepository applicantSelectionRepository,
            GuidanceTeacherRepository guidanceTeacherRepository) {

        this.programInterestRepository = programInterestRepository;
        this.applicantSelectionRepository = applicantSelectionRepository;
        this.guidanceTeacherRepository = guidanceTeacherRepository;
    }

    @Transactional(readOnly = true)
    public Page<StudentInterestReportResponse> getStudentInterestReport(
            String email,
            Integer semester,
            Integer academicYear,
            String faculty,
            String major,
            String keyword,
            int page,
            int size) {

        GuidanceTeacher teacher = guidanceTeacherRepository
                .findByEmail(email)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Guidance teacher account not found."));

        Long schoolId = teacher.getSchool().getSchoolId();

        Pageable pageable = PageRequest.of(page, size);

        Page<ProgramInterest> interests =
                programInterestRepository.searchStudentInterests(
                        schoolId,
                        semester,
                        academicYear,
                        normalize(faculty),
                        normalize(major),
                        normalize(keyword),
                        pageable
                );

        List<Long> applicantIds = interests.getContent()
                .stream()
                .map(pi -> pi.getApplicant().getApplicantId())
                .distinct()
                .collect(Collectors.toList());

        List<ApplicantSelection> selections = applicantIds.isEmpty()
                ? List.of()
                : applicantSelectionRepository
                        .findByApplicant_ApplicantIdIn(applicantIds);

        Map<String, ApplicantSelection> selectionMap = new HashMap<>();

        for (ApplicantSelection selection : selections) {
            if (selection.getPublishedAt() != null) {
                String key = selection.getApplicant().getApplicantId()
                        + "-" + selection.getEntryGroup().getEntrygroupId();

                selectionMap.put(key, selection);
            }
        }

        long offset = (long) page * size;

        return interests.map(interest -> {

            var applicant = interest.getApplicant();
            var program = interest.getProgram();

            String key = applicant.getApplicantId()
                    + "-" + program.getEntryGroup().getEntrygroupId();

            ApplicantSelection selection = selectionMap.get(key);

            String status = selection == null
                    ? null
                    : selection.getStatus().getDescription();

            StudentInterestReportResponse response =
                    new StudentInterestReportResponse();

            response.setNumber((int) (offset + interests.getContent()
                    .indexOf(interest) + 1));

            response.setApplicantId(applicant.getApplicantId());
            response.setStudentId(applicant.getNationalId());

            response.setFullName(
                    ((applicant.getFirstName() == null ? "" : applicant.getFirstName())
                    + " "
                    + (applicant.getLastName() == null ? "" : applicant.getLastName()))
                    .trim()
            );

            response.setSchoolName(applicant.getSchool().getName());
            response.setFaculty(program.getFaculty());
            response.setMajor(program.getProgramName());
            response.setSemester(applicant.getTerm());
            response.setAcademicYear(applicant.getAcademicYear());
            response.setStatus(status);

            return response;
        });
    }

    private String normalize(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
