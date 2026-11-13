
package guidance_system.controller;

import guidance_system.dto.SchoolOverviewResponse;
import guidance_system.service.SchoolOverviewService;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/schools/overview")
@CrossOrigin(origins = "*")
@PreAuthorize("hasRole('ADMIN')")
public class AdminSchoolOverviewController {

    private final SchoolOverviewService overviewService;

    public AdminSchoolOverviewController(
            SchoolOverviewService overviewService) {
        this.overviewService = overviewService;
    }

    @GetMapping
    public ResponseEntity<SchoolOverviewResponse> getSchoolOverview(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String province,
            @RequestParam(required = false) String schoolType,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        return ResponseEntity.ok(
                overviewService.getOverview(
                        keyword,
                        province,
                        schoolType,
                        page,
                        size
                )
        );
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleBadRequest(
            IllegalArgumentException exception) {
        return ResponseEntity.badRequest().body(exception.getMessage());
    }
}
