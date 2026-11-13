
package guidance_system.controller;

import guidance_system.entity.Prefix;
import guidance_system.repository.PrefixRepository;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/prefixes")
@CrossOrigin(origins = "*")
public class PrefixController {

    private final PrefixRepository repository;

    public PrefixController(PrefixRepository repository) {
        this.repository = repository;
    }

    // Admin and Teacher can view all prefixes
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public List<Prefix> getAll() {
        return repository.findAll();
    }

    // Admin and Teacher can view one prefix
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ResponseEntity<Prefix> getById(@PathVariable Long id) {
        return repository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Only Admin can create
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Prefix create(@RequestBody Prefix prefix) {
        return repository.save(prefix);
    }

    // Only Admin can update
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Prefix> update(
            @PathVariable Long id,
            @RequestBody Prefix details) {

        return repository.findById(id).map(prefix -> {
            prefix.setPrefixName(details.getPrefixName());
            return ResponseEntity.ok(repository.save(prefix));
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
