
package guidance_system.dto;

import java.util.List;

public record SchoolStatisticsResponse(
        String schoolName,
        Integer activeAcademicYear,
        Integer academicYear,
        String admissionRound,
        Long programId,
        SummaryCards summaryCards,
        List<ChartItem> applicationFunnel,
        List<ChartItem> postSelectionResults,
        List<ApplicantRow> applicants
) {

    public record SummaryCards(
            SummaryCard totalApplicants,
            SummaryCard interviewEligible,
            SummaryCard selected,
            SummaryCard enrolled,
            SummaryCard waived,
            SummaryCard notEnrolled
    ) {}

    public record SummaryCard(
            long count,
            double proportion,
            Long previousYearCount,
            Double yoyChangePercent
    ) {}

    public record ChartItem(
            String label,
            long count
    ) {}

    public record ApplicantRow(
            Long applicantId,
            String studentId,
            String fullName,
            Integer academicYear,
            Integer semester,
            String admissionRound,
            String statusCode,
            String statusDescription
    ) {}
}
