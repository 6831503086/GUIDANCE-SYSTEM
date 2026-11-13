
package guidance_system.controller;

import guidance_system.dto.AdminDashboardResponse;
import guidance_system.service.AdminDashboardService;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/dashboard")
@CrossOrigin(origins = "*")
@PreAuthorize("hasRole('ADMIN')")
public class AdminDashboardController {

    private final AdminDashboardService dashboardService;

    public AdminDashboardController(
            AdminDashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping
    public ResponseEntity<AdminDashboardResponse> getDashboard(
            @RequestParam(required = false) Integer academicYear,
            @RequestParam(required = false) String status,
            @RequestParam(required = false, defaultValue = "30") Integer days) {

        return ResponseEntity.ok(
                dashboardService.generateDashboard(
                        academicYear,
                        status,
                        days
                )
        );
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleInvalidDashboardFilter(
            IllegalArgumentException exception) {

        return ResponseEntity.badRequest().body(exception.getMessage());
    }
}
