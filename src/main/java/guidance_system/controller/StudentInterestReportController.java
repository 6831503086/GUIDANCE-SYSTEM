
package guidance_system.controller;

import guidance_system.dto.StudentInterestReportResponse;
import guidance_system.service.StudentInterestReportService;

import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.HttpStatus;
import org.springframework.http.ContentDisposition;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/reports/student-interests")
@CrossOrigin(origins = "*")
@PreAuthorize("hasRole('TEACHER')")
public class StudentInterestReportController {

    private final StudentInterestReportService reportService;

    public StudentInterestReportController(
            StudentInterestReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping
    public ResponseEntity<Page<StudentInterestReportResponse>> getReport(
            Authentication authentication,
            @RequestParam(required = false) Integer semester,
            @RequestParam(required = false) Integer academicYear,
            @RequestParam(required = false) String faculty,
            @RequestParam(required = false) String major,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        if (page < 0 || size < 1 || size > 100) {
            return ResponseEntity.badRequest().build();
        }

        Page<StudentInterestReportResponse> report =
                reportService.getStudentInterestReport(
                        authentication.getName(),
                        semester,
                        academicYear,
                        faculty,
                        major,
                        keyword,
                        page,
                        size
                );

        return ResponseEntity.ok(report);
    }

    @GetMapping("/export/pdf")
    public ResponseEntity<byte[]> exportStudentInterestPdf(
            Authentication authentication,
            @RequestParam(required = false) Integer semester,
            @RequestParam(required = false) Integer academicYear,
            @RequestParam(required = false) String faculty,
            @RequestParam(required = false) String major,
            @RequestParam(required = false) String keyword) throws IOException {

        // Retrieve every matching record, not only the first page.
        List<StudentInterestReportResponse> records = new ArrayList<>();

        Page<StudentInterestReportResponse> firstPage =
                reportService.getStudentInterestReport(
                        authentication.getName(),
                        semester, academicYear, faculty, major, keyword,
                        0, 100);

        records.addAll(firstPage.getContent());

        for (int page = 1; page < firstPage.getTotalPages(); page++) {
            records.addAll(
                    reportService.getStudentInterestReport(
                            authentication.getName(),
                            semester, academicYear, faculty, major, keyword,
                            page, 100).getContent()
            );
        }

        try (PDDocument document = new PDDocument();
            ByteArrayOutputStream output = new ByteArrayOutputStream()) {

            PDType1Font font = new PDType1Font(
                    Standard14Fonts.FontName.HELVETICA);
            PDType1Font boldFont = new PDType1Font(
                    Standard14Fonts.FontName.HELVETICA_BOLD);

            PDPage page = new PDPage();
            document.addPage(page);

            PDPageContentStream content =
                    new PDPageContentStream(document, page);

            float y = 750;

            content.beginText();
            content.setFont(boldFont, 16);
            content.newLineAtOffset(50, y);
            content.showText("Student Interest Report");
            content.endText();

            y -= 25;

            content.beginText();
            content.setFont(font, 10);
            content.newLineAtOffset(50, y);
            content.showText("Generated: " + LocalDate.now());
            content.endText();

            y -= 20;

            content.beginText();
            content.setFont(font, 10);
            content.newLineAtOffset(50, y);
            content.showText("Academic Year: "
                    + (academicYear == null ? "All" : academicYear));
            content.endText();

            y -= 20;

            content.beginText();
            content.setFont(font, 10);
            content.newLineAtOffset(50, y);
            content.showText("Semester: "
                    + (semester == null ? "All" : semester));
            content.endText();

            y -= 20;

            content.beginText();
            content.setFont(font, 10);
            content.newLineAtOffset(50, y);
            content.showText("Faculty: "
                    + (faculty == null || faculty.isBlank() ? "All" : faculty));
            content.endText();

            y -= 20;

            content.beginText();
            content.setFont(font, 10);
            content.newLineAtOffset(50, y);
            content.showText("Major: "
                    + (major == null || major.isBlank() ? "All" : major));
            content.endText();

            y -= 20;

            content.beginText();
            content.setFont(font, 10);
            content.newLineAtOffset(50, y);
            content.showText("Keyword: "
                    + (keyword == null || keyword.isBlank() ? "All" : keyword));
            content.endText();

            y -= 30;

            content.beginText();
            content.setFont(boldFont, 11);
            content.newLineAtOffset(50, y);
            content.showText("Total Records: " + records.size());
            content.endText();

            y -= 30;

            content.beginText();
            content.setFont(boldFont, 10);
            content.newLineAtOffset(50, y);
            content.showText("Student Details");
            content.endText();

            y -= 20;

            for (StudentInterestReportResponse record : records) {

                if (y < 70) {
                    content.close();

                    page = new PDPage();
                    document.addPage(page);

                    content = new PDPageContentStream(document, page);
                    y = 750;
                }

                String[] lines = {
                        "No.: " + record.getNumber()
                                + " | Student ID: " + safePdfText(record.getStudentId()),
                        "Name: " + safePdfText(record.getFullName()),
                        "School: " + safePdfText(record.getSchoolName()),
                        "Faculty: " + safePdfText(record.getFaculty()),
                        "Major: " + safePdfText(record.getMajor()),
                        "Semester: " + record.getSemester()
                                + " | Academic Year: " + record.getAcademicYear(),
                        "Status: " + safePdfText(record.getStatus())
                };

                for (String line : lines) {
                    content.beginText();
                    content.setFont(font, 9);
                    content.newLineAtOffset(50, y);
                    content.showText(line);
                    content.endText();

                    y -= 13;
                }

                y -= 12;
            }

            content.close();

            document.save(output);

            return ResponseEntity.ok()
                    .header(
                            HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=student-interest-report.pdf")
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(output.toByteArray());
        }
    }

    private String safePdfText(String value) {
        if (value == null || value.isBlank()) {
            return "Not available";
        }

        // Prevent unsupported characters from causing PDFBox encoding errors.
        return value.replaceAll("[^\\x20-\\x7E]", "?");
}
}
