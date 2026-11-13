package guidance_system.controller;

import guidance_system.dto.ResultDatasetSummaryResponse;
import guidance_system.entity.ResultDataset;
import guidance_system.service.ResultDatasetService;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/result-datasets")
@CrossOrigin(origins = "*")
@PreAuthorize("hasRole('ADMIN')")
public class ResultDatasetController {

    private final ResultDatasetService service;

    public ResultDatasetController(ResultDatasetService service) {
        this.service = service;
    }

    // View and filter all result datasets
    @GetMapping
    public List<ResultDataset> getDatasets(
            @RequestParam(required = false) Integer academicYear,
            @RequestParam(required = false) String round,
            @RequestParam(required = false) String program,
            @RequestParam(required = false) String status) {

        return service.searchDatasets(
                academicYear,
                round,
                program,
                status
        );
    }

    // View dataset summary
    @GetMapping("/summary")
    public ResultDatasetSummaryResponse getSummary() {
        return service.getSummary();
    }

    // View individual dataset details
    @GetMapping("/{id}")
    public ResponseEntity<ResultDataset> getDatasetDetails(
            @PathVariable Long id) {

        ResultDataset dataset = service.getDatasetDetails(id);

        if (dataset == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(dataset);
    }
}
