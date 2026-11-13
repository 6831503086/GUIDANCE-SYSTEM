package guidance_system.controller;

import guidance_system.dto.TeacherProgramInterestStatisticsResponse;
import guidance_system.service.TeacherProgramInterestStatisticsService;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;

@RestController
@RequestMapping("/api/reports/program-interest-statistics")
@CrossOrigin(origins = "*")
@PreAuthorize("hasRole('TEACHER')")
public class TeacherProgramInterestStatisticsController {

    private final TeacherProgramInterestStatisticsService statisticsService;

    public TeacherProgramInterestStatisticsController(
            TeacherProgramInterestStatisticsService statisticsService) {
        this.statisticsService = statisticsService;
    }

    @GetMapping
    public ResponseEntity<TeacherProgramInterestStatisticsResponse> getStatistics(
            Authentication authentication,
            @RequestParam(required = false) Integer semester,
            @RequestParam(required = false) Integer academicYear,
            @RequestParam(required = false) String faculty,
            @RequestParam(required = false) String major,
            @RequestParam(required = false) String keyword) {

        if (semester != null && semester < 1) {
            return ResponseEntity.badRequest().build();
        }

        if (academicYear != null && academicYear < 1) {
            return ResponseEntity.badRequest().build();
        }

        TeacherProgramInterestStatisticsResponse report =
                statisticsService.getStatistics(
                        authentication.getName(),
                        semester,
                        academicYear,
                        faculty,
                        major,
                        keyword
                );

        return ResponseEntity.ok(report);
    }

    @GetMapping("/export/pdf")
    public ResponseEntity<byte[]> exportStatisticsPdf(
            Authentication authentication,
            @RequestParam(required = false) Integer semester,
            @RequestParam(required = false) Integer academicYear,
            @RequestParam(required = false) String faculty,
            @RequestParam(required = false) String major,
            @RequestParam(required = false) String keyword) throws IOException {

        TeacherProgramInterestStatisticsResponse report =
                statisticsService.getStatistics(
                        authentication.getName(),
                        semester,
                        academicYear,
                        faculty,
                        major,
                        keyword
                );

        try (PDDocument document = new PDDocument();
            ByteArrayOutputStream output = new ByteArrayOutputStream()) {

            PDPage page = new PDPage();
            document.addPage(page);

            try (PDPageContentStream content =
                        new PDPageContentStream(document, page)) {

                PDType1Font font = new PDType1Font(
                        Standard14Fonts.FontName.HELVETICA);

                PDType1Font boldFont = new PDType1Font(
                        Standard14Fonts.FontName.HELVETICA_BOLD);

                float y = 750;

                content.beginText();
                content.setFont(boldFont, 16);
                content.newLineAtOffset(50, y);
                content.showText("Program Interest Statistics Report");
                content.endText();

                y -= 25;

                content.beginText();
                content.setFont(font, 10);
                content.newLineAtOffset(50, y);
                content.showText("Generated: " + LocalDate.now());
                content.endText();

                y -= 25;

                String[] filters = {
                        "Academic Year: " + (academicYear == null ? "All" : academicYear),
                        "Semester: " + (semester == null ? "All" : semester),
                        "Faculty: " + pdfText(faculty),
                        "Major: " + pdfText(major),
                        "Program Search: " + pdfText(keyword)
                };

                for (String filter : filters) {
                    content.beginText();
                    content.setFont(font, 10);
                    content.newLineAtOffset(50, y);
                    content.showText(filter);
                    content.endText();
                    y -= 18;
                }

                y -= 10;

                content.beginText();
                content.setFont(boldFont, 11);
                content.newLineAtOffset(50, y);
                content.showText("Summary");
                content.endText();

                y -= 20;

                String[] summary = {
                        "Number of Applicants: " + report.getNumberOfApplicants(),
                        "Number of Programs: " + report.getNumberOfPrograms(),
                        "Total Interests: " + report.getTotalInterests()
                };

                for (String item : summary) {
                    content.beginText();
                    content.setFont(font, 10);
                    content.newLineAtOffset(50, y);
                    content.showText(item);
                    content.endText();
                    y -= 18;
                }

                y -= 10;

                content.beginText();
                content.setFont(boldFont, 12);
                content.newLineAtOffset(50, y);
                content.showText("Program Statistics");
                content.endText();

                y -= 22;

                for (var item : report.getProgramStatistics()) {

                    if (y < 70) {
                        // Keep this first version within one page.
                        content.beginText();
                        content.setFont(font, 9);
                        content.newLineAtOffset(50, y);
                        content.showText("Additional records omitted from this page.");
                        content.endText();
                        break;
                    }

                    String line = item.getRank()
                            + ". " + pdfText(item.getProgramName())
                            + " | Faculty: " + pdfText(item.getFaculty())
                            + " | Interests: " + item.getInterestCount()
                            + " | Proportion: " + item.getProportion() + "%";

                    content.beginText();
                    content.setFont(font, 9);
                    content.newLineAtOffset(50, y);
                    content.showText(line);
                    content.endText();

                    y -= 18;
                }
            }

            document.save(output);

            return ResponseEntity.ok()
                    .header(
                            HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=program-interest-statistics.pdf")
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(output.toByteArray());
        }
    }

    private String pdfText(String value) {
        if (value == null || value.isBlank()) {
            return "All";
        }

        return value.replaceAll("[^\\x20-\\x7E]", "?");
    }
}