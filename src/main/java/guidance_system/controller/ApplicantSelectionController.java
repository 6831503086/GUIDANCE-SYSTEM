
package guidance_system.controller;

import guidance_system.entity.ApplicantSelection;
import guidance_system.repository.ApplicantSelectionRepository;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/applicant-selections")
@CrossOrigin(origins = "*")
@PreAuthorize("hasRole('ADMIN')")
public class ApplicantSelectionController {

    private final ApplicantSelectionRepository repository;

    public ApplicantSelectionController(
            ApplicantSelectionRepository repository) {
        this.repository = repository;
    }

    // Only Admin can view all selections
    @GetMapping
    public List<ApplicantSelection> getAll() {
        return repository.findAll();
    }

    // Only Admin can view one selection
    @GetMapping("/{id}")
    public ResponseEntity<ApplicantSelection> getById(
            @PathVariable Long id) {
        return repository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Only Admin can create
    @PostMapping
    public ApplicantSelection create(
            @RequestBody ApplicantSelection selection) {
        return repository.save(selection);
    }

    // Only Admin can update
    @PutMapping("/{id}")
    public ResponseEntity<ApplicantSelection> update(
            @PathVariable Long id,
            @RequestBody ApplicantSelection details) {

        return repository.findById(id).map(selection -> {
            selection.setApplicant(details.getApplicant());
            selection.setEntryGroup(details.getEntryGroup());
            selection.setQuota(details.getQuota());
            selection.setStatus(details.getStatus());
            selection.setAppliedAt(details.getAppliedAt());
            selection.setAdmin(details.getAdmin());
            selection.setPublishedAt(details.getPublishedAt());

            return ResponseEntity.ok(repository.save(selection));
        }).orElse(ResponseEntity.notFound().build());
    }

    // Only Admin can delete
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (!repository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        repository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
