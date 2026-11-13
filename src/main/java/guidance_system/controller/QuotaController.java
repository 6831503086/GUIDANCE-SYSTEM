
package guidance_system.controller;

import guidance_system.entity.Quota;
import guidance_system.repository.QuotaRepository;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/quotas")
@CrossOrigin(origins = "*")
public class QuotaController {

    private final QuotaRepository repository;

    public QuotaController(QuotaRepository repository) {
        this.repository = repository;
    }

    // Admin and Teacher can view all quotas
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public List<Quota> getAll() {
        return repository.findAll();
    }

    // Admin and Teacher can view one quota
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ResponseEntity<Quota> getById(@PathVariable Long id) {
        return repository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Only Admin can create
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Quota create(@RequestBody Quota quota) {
        return repository.save(quota);
    }

    // Only Admin can update
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Quota> update(
            @PathVariable Long id,
            @RequestBody Quota details) {

        return repository.findById(id).map(quota -> {
            quota.setQuotaName(details.getQuotaName());
            return ResponseEntity.ok(repository.save(quota));
        }).orElse(ResponseEntity.notFound().build());
    }

    // Only Admin can delete
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
