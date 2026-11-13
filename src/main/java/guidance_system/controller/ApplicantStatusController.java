
package guidance_system.controller;

import guidance_system.entity.ApplicantStatus;
import guidance_system.repository.ApplicantStatusRepository;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/applicant-statuses")
@CrossOrigin(origins = "*")
public class ApplicantStatusController {

    private final ApplicantStatusRepository repository;

    public ApplicantStatusController(ApplicantStatusRepository repository) {
        this.repository = repository;
    }

    // Admin and Teacher can view all applicant statuses
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public List<ApplicantStatus> getAll() {
        return repository.findAll();
    }

    // Admin and Teacher can view one applicant status
    @GetMapping("/{code}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ResponseEntity<ApplicantStatus> getById(
            @PathVariable String code) {
        return repository.findById(code)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Only Admin can create
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ApplicantStatus create(@RequestBody ApplicantStatus status) {
        return repository.save(status);
    }

    // Only Admin can update
    @PutMapping("/{code}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApplicantStatus> update(
            @PathVariable String code,
            @RequestBody ApplicantStatus details) {

        return repository.findById(code).map(status -> {
            status.setDescription(details.getDescription());
            return ResponseEntity.ok(repository.save(status));
        }).orElse(ResponseEntity.notFound().build());
    }

    // Only Admin can delete
    @DeleteMapping("/{code}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable String code) {
        if (!repository.existsById(code)) {
            return ResponseEntity.notFound().build();
        }
        repository.deleteById(code);
        return ResponseEntity.noContent().build();
    }
}
