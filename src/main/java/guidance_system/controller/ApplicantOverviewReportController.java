
package guidance_system.controller;

import guidance_system.dto.ApplicantOverviewReportResponse;
import guidance_system.service.ApplicantOverviewReportService;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;

@RestController
@RequestMapping("/api/reports/applicant-overview")
@CrossOrigin(origins = "*")
@PreAuthorize("hasRole('TEACHER')")
public class ApplicantOverviewReportController {

    private final ApplicantOverviewReportService reportService;

    public ApplicantOverviewReportController(
            ApplicantOverviewReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping
    public ResponseEntity<ApplicantOverviewReportResponse> getReport(
            @RequestParam(required = false) Integer semester,
            @RequestParam(required = false) Integer academicYear,
            @RequestParam(required = false) String faculty,
            @RequestParam(required = false) String keyword,
            Authentication authentication) {

        if (semester != null && semester < 1) {
            return ResponseEntity.badRequest().build();
        }

        if (academicYear != null && academicYear < 1) {
            return ResponseEntity.badRequest().build();
        }

        ApplicantOverviewReportResponse response =
                reportService.getReport(
                        authentication.getName(),
                        semester,
                        academicYear,
                        faculty,
                        keyword
                );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/export/pdf")
    public ResponseEntity<byte[]> exportPdf(
            @RequestParam(required = false) Integer semester,
            @RequestParam(required = false) Integer academicYear,
            @RequestParam(required = false) String faculty,
            @RequestParam(required = false) String keyword,
            Authentication authentication) throws IOException {

        if (semester != null && semester < 1) {
            return ResponseEntity.badRequest().build();
        }

        if (academicYear != null && academicYear < 1) {
            return ResponseEntity.badRequest().build();
        }

        ApplicantOverviewReportResponse report =
                reportService.getReport(
                        authentication.getName(),
                        semester,
                        academicYear,
                        faculty,
                        keyword
                );

        byte[] pdf;

        try (PDDocument document = new PDDocument()) {

            PDType1Font boldFont =
                    new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);

            PDType1Font normalFont =
                    new PDType1Font(Standard14Fonts.FontName.HELVETICA);

            float margin = 50;
            float startY = 750;
            float bottomY = 50;
            float y;

            // First page
            PDPage page = new PDPage();
            document.addPage(page);

            PDPageContentStream content =
                    new PDPageContentStream(document, page);

            y = startY;

            content.beginText();
            content.setFont(boldFont, 18);
            content.newLineAtOffset(margin, y);
            content.showText("Applicant Overview Report");
            content.endText();

            y -= 30;

            content.beginText();
            content.setFont(normalFont, 10);
            content.newLineAtOffset(margin, y);
            content.showText("Generated: " + LocalDate.now());
            content.endText();

            y -= 35;

            String[] summaries = {
                    "Total Applicants: " + report.getTotalApplicants(),
                    "Eligible for Examination: "
                            + report.getEligibleForExamination(),
                    "Passed Selection: " + report.getPassedSelection(),
                    "Rights Confirmed: " + report.getRightsConfirmed(),
                    "Rights Waived: " + report.getRightsWaived(),
                    "Enrolled: " + report.getEnrolled()
            };

            for (String summary : summaries) {
                content.beginText();
                content.setFont(normalFont, 11);
                content.newLineAtOffset(margin, y);
                content.showText(summary);
                content.endText();
                y -= 18;
            }

            y -= 15;

            content.beginText();
            content.setFont(boldFont, 11);
            content.newLineAtOffset(margin, y);
            content.showText("Applicant List");
            content.endText();

            y -= 22;

            content.beginText();
            content.setFont(boldFont, 8);
            content.newLineAtOffset(margin, y);
            content.showText(
                    "Student ID       Name                 Year   Semester   Status"
            );
            content.endText();

            y -= 15;

            for (ApplicantOverviewReportResponse.ApplicantRecord applicant
                    : report.getApplicants()) {

                if (y < bottomY) {
                    content.close();

                    page = new PDPage();
                    document.addPage(page);

                    content = new PDPageContentStream(document, page);
                    y = startY;

                    content.beginText();
                    content.setFont(boldFont, 12);
                    content.newLineAtOffset(margin, y);
                    content.showText("Applicant Overview Report - Continued");
                    content.endText();

                    y -= 30;

                    content.beginText();
                    content.setFont(boldFont, 8);
                    content.newLineAtOffset(margin, y);
                    content.showText(
                            "Student ID       Name                 Year   Semester   Status"
                    );
                    content.endText();

                    y -= 18;
                }

                String name = pdfText(applicant.getFullName());
                String studentId = pdfText(applicant.getStudentId());

                String status = applicant.getStatusCode() == null
                        ? "Not published"
                        : applicant.getStatusCode() + " - "
                                + pdfText(applicant.getStatusDescription());

                String row = String.format(
                        "%-16s %-20s %-6s %-10s %s",
                        studentId,
                        name,
                        applicant.getAcademicYear(),
                        applicant.getSemester(),
                        status
                );

                content.beginText();
                content.setFont(normalFont, 8);
                content.newLineAtOffset(margin, y);
                content.showText(pdfText(row));
                content.endText();

                y -= 14;
            }

            content.close();

            ByteArrayOutputStream output = new ByteArrayOutputStream();
            document.save(output);
            pdf = output.toByteArray();
        }

        return ResponseEntity.ok()
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=applicant-overview-report.pdf"
                )
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    private String pdfText(String value) {
        if (value == null) {
            return "";
        }

        return value.replaceAll("[^\\x20-\\x7E]", "?");
    }
}
