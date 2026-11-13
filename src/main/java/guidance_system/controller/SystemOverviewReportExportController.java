
package guidance_system.controller;

import guidance_system.dto.SystemOverviewReportResponse;
import guidance_system.dto.SystemOverviewReportResponse.ProgramInterestItem;
import guidance_system.dto.SystemOverviewReportResponse.ApplicantsBySchoolItem;
import guidance_system.dto.SystemOverviewReportResponse.AdmissionTrackItem;
import guidance_system.service.SystemOverviewReportService;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/admin/reports/system-overview/export")
@CrossOrigin(origins = "*")
@PreAuthorize("hasRole('ADMIN')")
public class SystemOverviewReportExportController {

    private final SystemOverviewReportService reportService;

    public SystemOverviewReportExportController(
            SystemOverviewReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping("/csv")
    public ResponseEntity<byte[]> exportCsv(
            @RequestParam(required = false) Integer academicYear,
            @RequestParam(required = false) String round,
            @RequestParam(required = false) String region) {

        SystemOverviewReportResponse report =
                reportService.generateReport(academicYear, round, region);

        StringBuilder csv = new StringBuilder();

        csv.append('\uFEFF');
        csv.append("SYSTEM-LEVEL OVERVIEW REPORT\n\n");

        csv.append("PROGRAM INTEREST RANKING\n");
        csv.append("Rank,Program ID,Program Name,Faculty,Admission Round,Interest Count\n");

        for (ProgramInterestItem item : report.getProgramInterestRanking()) {
            csv.append(item.getRank()).append(",");
            csv.append(item.getProgramId()).append(",");
            csv.append(csvValue(item.getProgramName())).append(",");
            csv.append(csvValue(item.getFaculty())).append(",");
            csv.append(csvValue(item.getAdmissionRound())).append(",");
            csv.append(item.getInterestCount()).append("\n");
        }

        csv.append("\nAPPLICANTS BY SCHOOL\n");
        csv.append("School ID,School Code,School Name,Province,Region,Applicant Count\n");

        for (ApplicantsBySchoolItem item : report.getApplicantsBySchool()) {
            csv.append(item.getSchoolId()).append(",");
            csv.append(csvValue(item.getSchoolCode())).append(",");
            csv.append(csvValue(item.getSchoolName())).append(",");
            csv.append(csvValue(item.getProvince())).append(",");
            csv.append(csvValue(item.getRegion())).append(",");
            csv.append(item.getApplicantCount()).append("\n");
        }

        csv.append("\nADMISSION TRACK PROPORTIONS\n");
        csv.append("Admission Track,Published Result Records,Proportion (%)\n");

        for (AdmissionTrackItem item : report.getAdmissionTrackProportions()) {
            csv.append(csvValue(item.getAdmissionTrack())).append(",");
            csv.append(item.getPublishedResultRecords()).append(",");
            csv.append(item.getProportion()).append("\n");
        }

        byte[] file = csv.toString().getBytes(StandardCharsets.UTF_8);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=system-overview-report.csv")
                .contentType(new MediaType("text", "csv", StandardCharsets.UTF_8))
                .body(file);
    }

    @GetMapping("/pdf")
    public ResponseEntity<byte[]> exportPdf(
            @RequestParam(required = false) Integer academicYear,
            @RequestParam(required = false) String round,
            @RequestParam(required = false) String region)
            throws IOException {

        SystemOverviewReportResponse report =
                reportService.generateReport(academicYear, round, region);

        List<String> lines = new ArrayList<>();

        lines.add("SYSTEM-LEVEL OVERVIEW REPORT");
        lines.add("");

        lines.add("PROGRAM INTEREST RANKING");
        for (ProgramInterestItem item : report.getProgramInterestRanking()) {
            lines.add(item.getRank() + ". " + item.getProgramName()
                    + " | " + item.getFaculty()
                    + " | Round: " + item.getAdmissionRound()
                    + " | Interests: " + item.getInterestCount());
        }

        lines.add("");
        lines.add("APPLICANTS BY SCHOOL");
        for (ApplicantsBySchoolItem item : report.getApplicantsBySchool()) {
            lines.add(item.getSchoolName()
                    + " | " + item.getSchoolCode()
                    + " | " + item.getProvince()
                    + " | Applicants: " + item.getApplicantCount());
        }

        lines.add("");
        lines.add("ADMISSION TRACK PROPORTIONS");
        lines.add("Based on published result records, not verified applicant totals.");

        for (AdmissionTrackItem item : report.getAdmissionTrackProportions()) {
            lines.add(item.getAdmissionTrack()
                    + " | Records: " + item.getPublishedResultRecords()
                    + " | Proportion: " + item.getProportion() + "%");
        }

        try (PDDocument document = new PDDocument();
             ByteArrayOutputStream output = new ByteArrayOutputStream()) {

            PDType1Font font =
                    new PDType1Font(Standard14Fonts.FontName.HELVETICA);

            PDType1Font boldFont =
                    new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);

            int lineIndex = 0;

            while (lineIndex < lines.size()) {

                PDPage page = new PDPage();
                document.addPage(page);

                try (PDPageContentStream content =
                             new PDPageContentStream(document, page)) {

                    float y = 750;

                    while (lineIndex < lines.size() && y > 50) {

                        String line = asciiSafe(lines.get(lineIndex));

                        content.beginText();
                        content.setFont(
                                lineIndex == 0
                                        ? boldFont
                                        : font,
                                lineIndex == 0 ? 16 : 10
                        );
                        content.newLineAtOffset(50, y);
                        content.showText(line);
                        content.endText();

                        y -= 16;
                        lineIndex++;
                    }
                }
            }

            document.save(output);

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=system-overview-report.pdf")
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(output.toByteArray());
        }
    }

    private String csvValue(String value) {
        if (value == null) {
            return "";
        }

        return "\"" + value.replace("\"", "\"\"") + "\"";
    }

    private String asciiSafe(String value) {
        if (value == null) {
            return "";
        }

        return value.replaceAll("[^\\x20-\\x7E]", "?");
    }
}
