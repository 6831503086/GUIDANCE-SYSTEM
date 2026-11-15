
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
    private final guidance_system.service.SchoolStatisticsPdfService pdfService;

    public SchoolStatisticsController(
            SchoolStatisticsService statisticsService, guidance_system.service.SchoolStatisticsPdfService pdfService) {
        this.statisticsService = statisticsService;
        this.pdfService = pdfService;
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

        return ResponseEntity.ok().header("Cache-Control", "no-store").body(response);
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

        return ResponseEntity.ok().header("Cache-Control", "no-store")
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

            return ResponseEntity.ok().header("Cache-Control", "no-store")
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

    private byte[] generatePdf(SchoolStatisticsResponse report) throws IOException {
        return pdfService.generatePdf(report);
    }
}
