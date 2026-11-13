package guidance_system.controller;

import guidance_system.entity.ProgramInterest;
import guidance_system.repository.ProgramInterestRepository;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import guidance_system.dto.ProgramInterestAnalysisResponse;
import guidance_system.service.ProgramInterestAnalysisService;
import java.time.LocalDate;
import java.util.List;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import java.nio.charset.StandardCharsets;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

@RestController
@RequestMapping("/api/program-interests")
@CrossOrigin(origins = "*")
@PreAuthorize("hasRole('ADMIN')")
public class ProgramInterestController {

    private final ProgramInterestRepository repository;
    private final ProgramInterestAnalysisService analysisService;

    public ProgramInterestController(ProgramInterestRepository repository, ProgramInterestAnalysisService analysisService) {
        this.repository = repository;
        this.analysisService = analysisService;
    }

    @GetMapping
    public List<ProgramInterest> getAll() {
        return repository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProgramInterest> getById(@PathVariable Long id) {
        return repository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ProgramInterest create(@RequestBody ProgramInterest interest) {
        return repository.save(interest);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProgramInterest> update(
            @PathVariable Long id,
            @RequestBody ProgramInterest details) {

        return repository.findById(id).map(interest -> {
            interest.setApplicant(details.getApplicant());
            interest.setProgram(details.getProgram());
            interest.setInterestedAt(details.getInterestedAt());

            return ResponseEntity.ok(repository.save(interest));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (!repository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }

        repository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/analysis")
    public ProgramInterestAnalysisResponse analyze(
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate,
            @RequestParam(required = false) Long schoolId,
            @RequestParam(required = false) String faculty,
            @RequestParam(defaultValue = "COUNT") String rankingType){

        return analysisService.analyze(
            startDate, endDate, schoolId, faculty, rankingType);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleInvalidRequest(
            IllegalArgumentException ex) {
        return ResponseEntity.badRequest().body(ex.getMessage());
    }

    @GetMapping("/analysis/export/csv")
    public ResponseEntity<byte[]> exportAnalysisCsv(
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate,
            @RequestParam(required = false) Long schoolId,
            @RequestParam(required = false) String faculty,
            @RequestParam(defaultValue = "COUNT") String rankingType) {

        ProgramInterestAnalysisResponse report =
                analysisService.analyze(
                        startDate, endDate, schoolId, faculty, rankingType);

        StringBuilder csv = new StringBuilder();

        // Ranking section
        csv.append("Program Interest Ranking\n");
        csv.append("Rank,Program ID,Program Name,Faculty,Interest Count,Proportion (%)\n");

        for (ProgramInterestAnalysisResponse.ProgramRanking item
                : report.getRankings()) {

            csv.append(item.getRank()).append(",");
            csv.append(item.getProgramId()).append(",");
            csv.append("\"").append(item.getProgramName()).append("\",");
            csv.append("\"").append(item.getFaculty()).append("\",");
            csv.append(item.getInterestCount()).append(",");
            csv.append(item.getProportion()).append("\n");
        }

        // Daily trend section
        csv.append("\nDaily Interest Trend\n");
        csv.append("Date,Interest Count\n");

        for (ProgramInterestAnalysisResponse.DailyTrend item
                : report.getDailyTrends()) {

            csv.append(item.getDate()).append(",");
            csv.append(item.getInterestCount()).append("\n");
        }

        // Analysis summary
        csv.append("\nAnalysis Summary\n");
        csv.append("\"").append(report.getAnalysisSummary()).append("\"\n");
        csv.append("Total Interests,").append(report.getTotalInterests()).append("\n");

        // UTF-8 BOM helps Excel display text correctly
        byte[] fileContent = (
                "\uFEFF" + csv
        ).getBytes(StandardCharsets.UTF_8);

        return ResponseEntity.ok()
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=program-interest-analysis.csv")
                .contentType(new MediaType("text", "csv", StandardCharsets.UTF_8))
                .body(fileContent);
    }


    @GetMapping("/analysis/export/pdf")
    public ResponseEntity<byte[]> exportAnalysisPdf(
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate,
            @RequestParam(required = false) Long schoolId,
            @RequestParam(required = false) String faculty,
            @RequestParam(defaultValue = "COUNT") String rankingType)
            throws IOException {

        ProgramInterestAnalysisResponse report =
                analysisService.analyze(
                        startDate, endDate, schoolId, faculty, rankingType);

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
                content.showText("Program Interest Analysis");
                content.endText();

                y -= 35;

                content.beginText();
                content.setFont(font, 10);
                content.newLineAtOffset(50, y);
                content.showText("Period: " + startDate + " to " + endDate);
                content.endText();

                y -= 30;

                content.beginText();
                content.setFont(boldFont, 12);
                content.newLineAtOffset(50, y);
                content.showText("Program Interest Ranking");
                content.endText();

                y -= 22;

                for (ProgramInterestAnalysisResponse.ProgramRanking item
                        : report.getRankings()) {

                    String line = item.getRank()
                            + ". " + item.getProgramName()
                            + " | Interests: " + item.getInterestCount()
                            + " | Proportion: " + item.getProportion() + "%";

                    content.beginText();
                    content.setFont(font, 10);
                    content.newLineAtOffset(50, y);
                    content.showText(line);
                    content.endText();

                    y -= 18;
                }

                y -= 15;

                content.beginText();
                content.setFont(boldFont, 12);
                content.newLineAtOffset(50, y);
                content.showText("Daily Interest Trend");
                content.endText();

                y -= 22;

                for (ProgramInterestAnalysisResponse.DailyTrend item
                        : report.getDailyTrends()) {

                    content.beginText();
                    content.setFont(font, 10);
                    content.newLineAtOffset(50, y);
                    content.showText(
                            item.getDate() + " | Interests: "
                                    + item.getInterestCount());
                    content.endText();

                    y -= 18;
                }

                y -= 15;

                content.beginText();
                content.setFont(boldFont, 12);
                content.newLineAtOffset(50, y);
                content.showText("Analysis Summary");
                content.endText();

                y -= 22;

                content.beginText();
                content.setFont(font, 10);
                content.newLineAtOffset(50, y);
                content.showText(
                        "Total Interests: " + report.getTotalInterests());
                content.newLineAtOffset(0, -18);
                content.showText(report.getAnalysisSummary());
                content.endText();
            }

            document.save(output);

            return ResponseEntity.ok()
                    .header(
                            HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=program-interest-analysis.pdf")
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(output.toByteArray());
        }
    }

}