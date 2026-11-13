package guidance_system.controller;

import guidance_system.entity.EntryGroup;
import guidance_system.repository.EntryGroupRepository;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/entry-groups")
@CrossOrigin(origins = "*")
public class EntryGroupController {

    private final EntryGroupRepository repository;

    public EntryGroupController(EntryGroupRepository repository) {
        this.repository = repository;
    }

    // Teachers and admins can view entry groups
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public List<EntryGroup> getAll() {
        return repository.findAll();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ResponseEntity<EntryGroup> getById(@PathVariable Long id) {
        return repository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Only admins can create entry groups
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public EntryGroup create(@RequestBody EntryGroup entryGroup) {
        return repository.save(entryGroup);
    }

    // Only admins can update entry groups
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<EntryGroup> update(
            @PathVariable Long id,
            @RequestBody EntryGroup details) {

        return repository.findById(id)
                .map(entryGroup -> {
                    entryGroup.setAcademicYear(details.getAcademicYear());
                    entryGroup.setEntrygroupName(details.getEntrygroupName());
                    entryGroup.setApplyStartDate(details.getApplyStartDate());
                    entryGroup.setApplyEndDate(details.getApplyEndDate());
                    entryGroup.setResultAnnounceDate(details.getResultAnnounceDate());
                    entryGroup.setRightsConfirmDate(details.getRightsConfirmDate());

                    return ResponseEntity.ok(repository.save(entryGroup));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // Only admins can delete entry groups
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (!repository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }

        repository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}