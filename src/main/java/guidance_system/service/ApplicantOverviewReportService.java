
package guidance_system.service;

import guidance_system.dto.ApplicantOverviewReportResponse;
import guidance_system.entity.Applicant;
import guidance_system.entity.ApplicantSelection;
import guidance_system.entity.GuidanceTeacher;
import guidance_system.repository.ApplicantRepository;
import guidance_system.repository.ApplicantSelectionRepository;
import guidance_system.repository.GuidanceTeacherRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class ApplicantOverviewReportService {

    private final ApplicantRepository applicantRepository;
    private final ApplicantSelectionRepository selectionRepository;
    private final GuidanceTeacherRepository teacherRepository;

    public ApplicantOverviewReportService(
            ApplicantRepository applicantRepository,
            ApplicantSelectionRepository selectionRepository,
            GuidanceTeacherRepository teacherRepository) {
        this.applicantRepository = applicantRepository;
        this.selectionRepository = selectionRepository;
        this.teacherRepository = teacherRepository;
    }

    @Transactional(readOnly = true)
    public ApplicantOverviewReportResponse getReport(
            String username,
            Integer semester,
            Integer academicYear,
            String faculty,
            String keyword) {

        GuidanceTeacher teacher = teacherRepository.findByUsername(username)
                .orElseThrow(() ->
                        new IllegalArgumentException("Guidance teacher not found."));

        Long schoolId = teacher.getSchool().getSchoolId();

        String searchKeyword = normalize(keyword);
        String searchFaculty = normalize(faculty);

        List<Applicant> applicants = applicantRepository.findAll().stream()
                .filter(a -> a.getSchool() != null
                        && Objects.equals(a.getSchool().getSchoolId(), schoolId))
                .filter(a -> semester == null
                        || Objects.equals(a.getTerm(), semester))
                .filter(a -> academicYear == null
                        || Objects.equals(a.getAcademicYear(), academicYear))
                .filter(a -> searchKeyword == null
                        || contains(a.getNationalId(), searchKeyword)
                        || contains(a.getFirstName(), searchKeyword)
                        || contains(a.getLastName(), searchKeyword)
                        || contains(
                                (a.getFirstName() == null ? "" : a.getFirstName())
                                + " "
                                + (a.getLastName() == null ? "" : a.getLastName()),
                                searchKeyword))
                .collect(Collectors.toList());

        List<Long> applicantIds = applicants.stream()
                .map(Applicant::getApplicantId)
                .toList();

        List<ApplicantSelection> selections = applicantIds.isEmpty()
                ? Collections.emptyList()
                : selectionRepository.findByApplicant_ApplicantIdIn(applicantIds);

        // Apply faculty filter using the applicant's quota faculty.
        if (searchFaculty != null) {
            applicants.removeIf(applicant ->
                    selections.stream()
                            .filter(selection ->
                                    selection.getApplicant() != null
                                            && Objects.equals(
                                                    selection.getApplicant().getApplicantId(),
                                                    applicant.getApplicantId()))
                            .noneMatch(selection ->
                                    selection.getQuota() != null
                                            && selection.getQuota().getFaculty() != null
                                            && selection.getQuota().getFaculty()
                                                    .trim()
                                                    .equalsIgnoreCase(searchFaculty))
            );
        }

        Set<Long> filteredApplicantIds = applicants.stream()
                .map(Applicant::getApplicantId)
                .collect(Collectors.toSet());

        List<ApplicantSelection> filteredSelections = selections.stream()
        .filter(selection ->
                selection.getApplicant() != null
                        && filteredApplicantIds.contains(
                                selection.getApplicant().getApplicantId()))
        .collect(Collectors.toList());

        Map<Long, ApplicantSelection> latestPublishedSelections =
                new HashMap<>();

        for (ApplicantSelection selection : filteredSelections) {

            if (selection.getPublishedAt() == null
                    || selection.getStatus() == null) {
                continue;
            }

            Long applicantId = selection.getApplicant().getApplicantId();

            ApplicantSelection current =
                    latestPublishedSelections.get(applicantId);

            boolean isNewer = false;

        if (current == null) {
        isNewer = true;
        } else if (selection.getAppliedAt() != null
                && (current.getAppliedAt() == null
                || selection.getAppliedAt().isAfter(current.getAppliedAt()))) {
        isNewer = true;
        } else if (Objects.equals(
                selection.getAppliedAt(), current.getAppliedAt())) {

        if (selection.getApplicationId() != null
                && (current.getApplicationId() == null
                || selection.getApplicationId()
                        > current.getApplicationId())) {
                isNewer = true;
        }
        }

        if (isNewer) {
        latestPublishedSelections.put(applicantId, selection);
        }
        }

        long eligible = 0;
        long passed = 0;
        long confirmed = 0;
        long waived = 0;
        long enrolled = 0;

        List<ApplicantOverviewReportResponse.ApplicantRecord> records =
                new ArrayList<>();

        for (Applicant applicant : applicants) {

            ApplicantSelection selection =
                    latestPublishedSelections.get(applicant.getApplicantId());

            String statusCode = null;
            String statusDescription = null;

            if (selection != null) {

                statusCode = selection.getStatus().getStatusCode();
                statusDescription = selection.getStatus().getDescription();

                if (isEligible(statusCode)) eligible++;
                if (isPassed(statusCode)) passed++;
                if (isRightsConfirmed(statusCode)) confirmed++;
                if ("42".equals(statusCode)) waived++;
                if ("50".equals(statusCode)) enrolled++;
            }

            String fullName =
                    ((applicant.getFirstName() == null ? "" : applicant.getFirstName())
                    + " "
                    + (applicant.getLastName() == null ? "" : applicant.getLastName()))
                    .trim();

            String schoolName = applicant.getSchool() == null
                    ? null
                    : applicant.getSchool().getName();

            records.add(
                    new ApplicantOverviewReportResponse.ApplicantRecord(
                            applicant.getApplicantId(),
                            applicant.getNationalId(),
                            fullName,
                            schoolName,
                            applicant.getTerm(),
                            applicant.getAcademicYear(),
                            statusCode,
                            statusDescription
                    )
            );
        }

        return new ApplicantOverviewReportResponse(
                applicants.size(),
                eligible,
                passed,
                confirmed,
                waived,
                enrolled,
                records
        );
    }

    private String normalize(String value) {
        if (value == null || value.isBlank()) return null;
        return value.trim();
    }

    private boolean contains(String value, String keyword) {
        return value != null
                && value.toLowerCase().contains(keyword.toLowerCase());
    }

    private boolean isEligible(String code) {
        if (code == null) return false;
        try {
            int value = Integer.parseInt(code);
            return value >= 20 && value <= 28;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private boolean isPassed(String code) {
        return "40".equals(code)
                || "41".equals(code)
                || "42".equals(code)
                || "43".equals(code)
                || "50".equals(code);
    }

    private boolean isRightsConfirmed(String code) {
        return "40".equals(code)
                || "43".equals(code)
                || "50".equals(code);
    }
}
