
package guidance_system.controller;

import guidance_system.dto.SystemOverviewReportResponse;
import guidance_system.service.SystemOverviewReportService;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/reports/system-overview")
@CrossOrigin(origins = "*")
@PreAuthorize("hasRole('ADMIN')")
public class SystemOverviewReportController {

    private final SystemOverviewReportService reportService;

    public SystemOverviewReportController(
            SystemOverviewReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping
    public ResponseEntity<SystemOverviewReportResponse> getSystemOverview(
            @RequestParam(required = false) Integer academicYear,
            @RequestParam(required = false) String round,
            @RequestParam(required = false) String region) {

        return ResponseEntity.ok(
                reportService.generateReport(
                        academicYear,
                        round,
                        region
                )
        );
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleBadRequest(
            IllegalArgumentException exception) {
        return ResponseEntity.badRequest()
                .body(exception.getMessage());
    }
}
