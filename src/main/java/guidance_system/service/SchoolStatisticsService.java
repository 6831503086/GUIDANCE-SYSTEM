
package guidance_system.service;

import guidance_system.dto.SchoolStatisticsResponse;
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
import guidance_system.repository.ProgramInterestRepository;

@Service
public class SchoolStatisticsService {

    private final ApplicantRepository applicantRepository;
    private final ApplicantSelectionRepository selectionRepository;
    private final GuidanceTeacherRepository teacherRepository;
    private final ProgramInterestRepository programInterestRepository;

    public SchoolStatisticsService(
            ApplicantRepository applicantRepository,
            ApplicantSelectionRepository selectionRepository,
            GuidanceTeacherRepository teacherRepository,
            ProgramInterestRepository programInterestRepository) 
            {
        this.applicantRepository = applicantRepository;
        this.selectionRepository = selectionRepository;
        this.teacherRepository = teacherRepository;
        this.programInterestRepository = programInterestRepository;
    }

    @Transactional(readOnly = true)
    public SchoolStatisticsResponse getStatistics(
            String username,
            Integer academicYear,
            String admissionRound,
            Long programId) {

        GuidanceTeacher teacher = teacherRepository.findByUsername(username)
                .orElseThrow(() ->
                        new IllegalArgumentException("Guidance teacher not found."));

        Long schoolId = teacher.getSchool().getSchoolId();

        Integer selectedYear = academicYear != null
                ? academicYear
                : teacher.getActiveAcademicYear();

        if (selectedYear == null) {
            throw new IllegalArgumentException(
                    "Academic year is not configured for this teacher.");
        }

        String selectedRound = normalize(admissionRound);

        List<Applicant> schoolApplicants = applicantRepository.findAll().stream()
        .filter(a -> a.getSchool() != null
                && Objects.equals(
                        a.getSchool().getSchoolId(), schoolId))
        .collect(Collectors.toList());

        if (programId != null) {
        List<Long> interestedApplicantIds =
                programInterestRepository.findApplicantIdsBySchoolAndProgram(
                        schoolId, programId);

        Set<Long> interestedIds = new HashSet<>(interestedApplicantIds);

        schoolApplicants = schoolApplicants.stream()
                .filter(a -> interestedIds.contains(a.getApplicantId()))
                .collect(Collectors.toList());
        }

        List<Long> applicantIds = schoolApplicants.stream()
                .map(Applicant::getApplicantId)
                .toList();

        List<ApplicantSelection> allSelections = applicantIds.isEmpty()
                ? Collections.emptyList()
                : selectionRepository.findByApplicant_ApplicantIdIn(
                        applicantIds);

        List<Applicant> currentApplicants = filterApplicants(
                schoolApplicants, allSelections, selectedYear, selectedRound);

        List<Applicant> previousApplicants = filterApplicants(
                schoolApplicants, allSelections, selectedYear - 1,
                selectedRound);

        Map<Long, ApplicantSelection> currentSelections =
                getLatestPublishedSelections(currentApplicants, allSelections,
                        selectedYear, selectedRound);

        Map<Long, ApplicantSelection> previousSelections =
                getLatestPublishedSelections(previousApplicants, allSelections,
                        selectedYear - 1, selectedRound);

        long total = currentApplicants.size();
        boolean selectionResultsPublished = !currentSelections.isEmpty();
        long eligible = countStatus(currentSelections, this::isEligible);
        long selected = countStatus(currentSelections, this::isSelected);
        long enrolled = countStatus(currentSelections,
                code -> "50".equals(code));
        long waived = countStatus(currentSelections,
                code -> "42".equals(code));
        long notEnrolled = countStatus(currentSelections,
                code -> "41".equals(code));

        long previousTotal = previousApplicants.size();
        long previousEligible = countStatus(previousSelections,
                this::isEligible);
        long previousSelected = countStatus(previousSelections,
                this::isSelected);
        long previousEnrolled = countStatus(previousSelections,
                code -> "50".equals(code));
        long previousWaived = countStatus(previousSelections,
                code -> "42".equals(code));
        long previousNotEnrolled = countStatus(previousSelections,
                code -> "41".equals(code));

        List<SchoolStatisticsResponse.ApplicantRow> rows =
                currentApplicants.stream()
                        .map(applicant -> toApplicantRow(
                                applicant,
                                currentSelections.get(
                                        applicant.getApplicantId())))
                        .toList();

        return new SchoolStatisticsResponse(
                teacher.getSchool().getName(),
                teacher.getActiveAcademicYear(),
                selectedYear,
                selectedRound,
                programId,
        new SchoolStatisticsResponse.SummaryCards(
                createCard(total, total, previousTotal),
                selectionResultsPublished
                        ? createCard(eligible, total, previousEligible) : null,
                selectionResultsPublished
                        ? createCard(selected, total, previousSelected) : null,
                selectionResultsPublished
                        ? createCard(enrolled, total, previousEnrolled) : null,
                selectionResultsPublished
                        ? createCard(waived, total, previousWaived) : null,
                selectionResultsPublished
                        ? createCard(notEnrolled, total, previousNotEnrolled) : null
),
        
        selectionResultsPublished
                ? List.of(
                        new SchoolStatisticsResponse.ChartItem("Applied", total),
                        new SchoolStatisticsResponse.ChartItem("Interview Eligible", eligible),
                        new SchoolStatisticsResponse.ChartItem("Selected", selected),
                        new SchoolStatisticsResponse.ChartItem("Enrolled", enrolled)
                )
                : List.of(
                        new SchoolStatisticsResponse.ChartItem("Applied", total)
                ),

        selectionResultsPublished
                ? List.of(
                        new SchoolStatisticsResponse.ChartItem("Enrolled", enrolled),
                        new SchoolStatisticsResponse.ChartItem("Waived", waived),
                        new SchoolStatisticsResponse.ChartItem("Not Enrolled", notEnrolled)
                )
                : List.of(),

        rows
        );
    }

    private List<Applicant> filterApplicants(
            List<Applicant> applicants,
            List<ApplicantSelection> selections,
            Integer academicYear,
            String admissionRound) {

        return applicants.stream()
                .filter(a -> Objects.equals(
                        a.getAcademicYear(), academicYear))
                .filter(a -> admissionRound == null
                        || selections.stream().anyMatch(selection ->
                                selection.getApplicant() != null
                                && Objects.equals(
                                        selection.getApplicant()
                                                .getApplicantId(),
                                        a.getApplicantId())
                                && matchesSelection(
                                        selection, academicYear,
                                        admissionRound)))
                .collect(Collectors.toList());
    }

    private boolean matchesSelection(
            ApplicantSelection selection,
            Integer academicYear,
            String admissionRound) {

        if (selection.getEntryGroup() == null) {
            return false;
        }

        return Objects.equals(
                    selection.getEntryGroup().getAcademicYear(),
                    academicYear)
                && selection.getEntryGroup().getEntrygroupName() != null
                && selection.getEntryGroup().getEntrygroupName()
                        .equalsIgnoreCase(admissionRound);
    }

    private Map<Long, ApplicantSelection> getLatestPublishedSelections(
            List<Applicant> applicants,
            List<ApplicantSelection> allSelections,
            Integer academicYear,
            String admissionRound) {

        Set<Long> applicantIds = applicants.stream()
                .map(Applicant::getApplicantId)
                .collect(Collectors.toSet());

        Map<Long, ApplicantSelection> latest = new HashMap<>();

        for (ApplicantSelection selection : allSelections) {

            if (selection.getApplicant() == null
                    || selection.getPublishedAt() == null
                    || selection.getStatus() == null) {
                continue;
            }

            Long applicantId =
                    selection.getApplicant().getApplicantId();

            if (!applicantIds.contains(applicantId)) {
                continue;
            }

            if (selection.getEntryGroup() == null
                    || !Objects.equals(
                            selection.getEntryGroup().getAcademicYear(),
                            academicYear)) {
                continue;
            }

            if (admissionRound != null
                    && (selection.getEntryGroup().getEntrygroupName() == null
                    || !selection.getEntryGroup().getEntrygroupName()
                            .equalsIgnoreCase(admissionRound))) {
                continue;
            }

            ApplicantSelection current = latest.get(applicantId);

            boolean newer = current == null;

            if (current != null) {
                if (selection.getAppliedAt() != null
                        && (current.getAppliedAt() == null
                        || selection.getAppliedAt()
                                .isAfter(current.getAppliedAt()))) {
                    newer = true;
                } else if (Objects.equals(
                        selection.getAppliedAt(), current.getAppliedAt())
                        && selection.getApplicationId() != null
                        && (current.getApplicationId() == null
                        || selection.getApplicationId()
                                > current.getApplicationId())) {
                    newer = true;
                }
            }

            if (newer) {
                latest.put(applicantId, selection);
            }
        }

        return latest;
    }

    private long countStatus(
            Map<Long, ApplicantSelection> selections,
            java.util.function.Predicate<String> condition) {

        return selections.values().stream()
                .filter(selection -> condition.test(
                        selection.getStatus().getStatusCode()))
                .count();
    }

    private SchoolStatisticsResponse.SummaryCard createCard(
            long count, long total, long previousCount) {

        double proportion = total == 0
                ? 0.0
                : (double) count / total * 100.0;

        Double yoyChange = previousCount == 0
                ? null
                : ((double) (count - previousCount)
                        / previousCount) * 100.0;

        return new SchoolStatisticsResponse.SummaryCard(
                count, proportion, previousCount, yoyChange);
    }

    private SchoolStatisticsResponse.ApplicantRow toApplicantRow(
            Applicant applicant,
            ApplicantSelection selection) {

        String fullName = (
                (applicant.getFirstName() == null
                        ? "" : applicant.getFirstName())
                + " "
                + (applicant.getLastName() == null
                        ? "" : applicant.getLastName())
        ).trim();

        String round = null;
        String statusCode = null;
        String statusDescription = null;

        if (selection != null) {
            if (selection.getEntryGroup() != null) {
                round = selection.getEntryGroup().getEntrygroupName();
            }

            statusCode = selection.getStatus().getStatusCode();
            statusDescription = selection.getStatus().getDescription();
        }

        return new SchoolStatisticsResponse.ApplicantRow(
                applicant.getApplicantId(),
                applicant.getNationalId(),
                fullName,
                applicant.getAcademicYear(),
                applicant.getTerm(),
                round,
                statusCode,
                statusDescription
        );
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

    private boolean isSelected(String code) {
        return "40".equals(code)
                || "41".equals(code)
                || "42".equals(code)
                || "43".equals(code)
                || "50".equals(code);
    }

    private String normalize(String value) {
        return value == null || value.isBlank()
                ? null : value.trim();
    }
}
