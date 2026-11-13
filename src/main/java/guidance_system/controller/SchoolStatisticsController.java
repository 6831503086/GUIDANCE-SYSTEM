
package guidance_system.controller;

import guidance_system.dto.SchoolStatisticsResponse;
import guidance_system.service.SchoolStatisticsService;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/reports/school-statistics")
@CrossOrigin(origins = "*")
@PreAuthorize("hasRole('TEACHER')")

public class SchoolStatisticsController {

    private final SchoolStatisticsService statisticsService;

    public SchoolStatisticsController(
            SchoolStatisticsService statisticsService) {
        this.statisticsService = statisticsService;
    }

    @GetMapping
    public ResponseEntity<SchoolStatisticsResponse> getStatistics(
            @RequestParam(required = false) Integer academicYear,
            @RequestParam(required = false) String admissionRound,
            @RequestParam(required = false) Long programId,
            Authentication authentication) {

        validateAcademicYear(academicYear);

        SchoolStatisticsResponse response =
                statisticsService.getStatistics(
                        authentication.getName(),
                        academicYear,
                        admissionRound,
                        programId
                        
                );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/export/pdf")
    public ResponseEntity<byte[]> exportStatisticsPdf(
            @RequestParam(required = false) Integer academicYear,
            @RequestParam(required = false) String admissionRound,
            @RequestParam(required = false) Long programId,
            Authentication authentication) throws IOException {

        validateAcademicYear(academicYear);

        SchoolStatisticsResponse report =
                statisticsService.getStatistics(
                        authentication.getName(),
                        academicYear,
                        admissionRound,
                        programId
                );

        byte[] pdf = generatePdf(report);

        return ResponseEntity.ok()
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=school-statistics.pdf"
                )
                .contentType(MediaType.APPLICATION_PDF)
                .contentLength(pdf.length)
                .body(pdf);
    }

    @GetMapping("/export/excel")
    public ResponseEntity<byte[]> exportExcel(
            @RequestParam(required = false) Integer academicYear,
            @RequestParam(required = false) String admissionRound,
            @RequestParam(required = false) Long programId,
            Authentication authentication) {

        validateAcademicYear(academicYear);

        SchoolStatisticsResponse report =
                statisticsService.getStatistics(
                        authentication.getName(),
                        academicYear,
                        admissionRound,
                        programId
                );

        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream outputStream =
                     new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("Applicants");

            String[] headers = {
                    "Applicant ID",
                    "Student ID",
                    "Full Name",
                    "Academic Year",
                    "Semester",
                    "Admission Round",
                    "Status Code",
                    "Status Description"
            };

            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);

            Row headerRow = sheet.createRow(0);

            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            int rowNumber = 1;

            for (SchoolStatisticsResponse.ApplicantRow applicant
                    : report.applicants()) {

                Row row = sheet.createRow(rowNumber++);

                row.createCell(0).setCellValue(
                        applicant.applicantId() == null
                                ? 0 : applicant.applicantId()
                );

                row.createCell(1).setCellValue(
                        applicant.studentId() == null
                                ? "" : applicant.studentId()
                );

                row.createCell(2).setCellValue(
                        applicant.fullName() == null
                                ? "" : applicant.fullName()
                );

                row.createCell(3).setCellValue(
                        applicant.academicYear() == null
                                ? 0 : applicant.academicYear()
                );

                row.createCell(4).setCellValue(
                        applicant.semester() == null
                                ? 0 : applicant.semester()
                );

                row.createCell(5).setCellValue(
                        applicant.admissionRound() == null
                                ? "" : applicant.admissionRound()
                );

                row.createCell(6).setCellValue(
                        applicant.statusCode() == null
                                ? "" : applicant.statusCode()
                );

                row.createCell(7).setCellValue(
                        applicant.statusDescription() == null
                                ? "" : applicant.statusDescription()
                );
            }

            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(outputStream);
            byte[] excelBytes = outputStream.toByteArray();

            return ResponseEntity.ok()
                    .header(
                            HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=school-applicants.xlsx"
                    )
                    .contentType(MediaType.parseMediaType(
                            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                    ))
                    .contentLength(excelBytes.length)
                    .body(excelBytes);

        } catch (IOException e) {
            throw new IllegalStateException(
                    "Unable to generate Excel report.", e
            );
        }
    }

    private void validateAcademicYear(Integer academicYear) {
        if (academicYear != null && academicYear < 1) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Academic year must be greater than zero."
            );
        }
    }

    private byte[] generatePdf(
            SchoolStatisticsResponse report) throws IOException {

        try (PDDocument document = new PDDocument();
             ByteArrayOutputStream output = new ByteArrayOutputStream()) {

            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);

            PDType1Font regular = new PDType1Font(
                    Standard14Fonts.FontName.HELVETICA);

            PDType1Font bold = new PDType1Font(
                    Standard14Fonts.FontName.HELVETICA_BOLD);

            try (PDPageContentStream content =
                         new PDPageContentStream(document, page)) {

                float y = 780;

                y = drawLine(content, bold, 18,
                        "SCHOOL STATISTICS REPORT", 50, y);

                y = drawLine(content, regular, 11,
                        "School: " + report.schoolName(), 50, y - 25);

                y = drawLine(content, regular, 11,
                        "Academic Year: " + report.academicYear(), 50, y);

                y = drawLine(content, regular, 11,
                        "Active Academic Year: "
                                + report.activeAcademicYear(), 50, y);

                y = drawLine(content, regular, 11,
                        "Admission Round: "
                                + valueOrNA(report.admissionRound()), 50, y);

                y -= 15;

                y = drawLine(content, bold, 14,
                        "SUMMARY STATISTICS", 50, y);

                SchoolStatisticsResponse.SummaryCards cards =
                        report.summaryCards();

                y = drawCard(content, regular, bold,
                        "Total Applicants", cards.totalApplicants(), y);

                y = drawCard(content, regular, bold,
                        "Interview Eligible", cards.interviewEligible(), y);

                y = drawCard(content, regular, bold,
                        "Selected", cards.selected(), y);

                y = drawCard(content, regular, bold,
                        "Enrolled", cards.enrolled(), y);

                y = drawCard(content, regular, bold,
                        "Waived", cards.waived(), y);

                y = drawCard(content, regular, bold,
                        "Not Enrolled", cards.notEnrolled(), y);

                y -= 15;

                y = drawLine(content, bold, 14,
                        "APPLICATION FUNNEL", 50, y);

                for (SchoolStatisticsResponse.ChartItem item
                        : report.applicationFunnel()) {

                    y = drawLine(content, regular, 11,
                            item.label() + ": " + item.count(),
                            60, y);
                }

                y -= 15;

                y = drawLine(content, bold, 14,
                        "POST-SELECTION RESULTS", 50, y);

                for (SchoolStatisticsResponse.ChartItem item
                        : report.postSelectionResults()) {

                    y = drawLine(content, regular, 11,
                            item.label() + ": " + item.count(),
                            60, y);
                }

                y -= 20;

                drawLine(content, regular, 9,
                        "Generated by Guidance Teacher System",
                        50, y);
            }

            document.save(output);
            return output.toByteArray();
        }
    }

    private float drawCard(

        
            PDPageContentStream content,
            PDType1Font regular,
            PDType1Font bold,
            String label,
            SchoolStatisticsResponse.SummaryCard card,
            float y) throws IOException {

        if (card == null) {
                return y;
        }

        y = drawLine(content, bold, 11,
                label + ": " + card.count(),
                60, y - 5);

        y = drawLine(content, regular, 10,
                "Proportion: "
                        + String.format("%.2f%%", card.proportion()),
                75, y);

        String yoy = card.yoyChangePercent() == null
                ? "N/A"
                : String.format("%.2f%%", card.yoyChangePercent());

        y = drawLine(content, regular, 10,
                "Previous Year: "
                        + card.previousYearCount()
                        + " | YoY Change: " + yoy,
                75, y);

        return y - 5;
    }

    private float drawLine(
            PDPageContentStream content,
            PDType1Font font,
            float fontSize,
            String text,
            float x,
            float y) throws IOException {

        content.beginText();
        content.setFont(font, fontSize);
        content.newLineAtOffset(x, y);
        content.showText(toPdfText(text));
        content.endText();

        return y - fontSize - 5;
    }

    private String toPdfText(String text) {
        if (text == null) {
            return "";
        }

        return text.replaceAll("[^\\x20-\\x7E]", "?");
    }

    private String valueOrNA(String value) {
        return value == null || value.isBlank()
                ? "N/A" : value;
    }
}
