
package guidance_system.controller;

import guidance_system.entity.District;
import guidance_system.repository.DistrictRepository;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/districts")
@CrossOrigin(origins = "*")
public class DistrictController {

    private final DistrictRepository repository;

    public DistrictController(DistrictRepository repository) {
        this.repository = repository;
    }

    // Admin and Teacher can view all districts
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public List<District> getAll() {
        return repository.findAll();
    }

    // Admin and Teacher can view one district
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ResponseEntity<District> getById(@PathVariable Long id) {
        return repository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Only Admin can create
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public District create(@RequestBody District district) {
        return repository.save(district);
    }

    // Only Admin can update
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<District> update(
            @PathVariable Long id,
            @RequestBody District details) {

        return repository.findById(id).map(district -> {
            district.setName(details.getName());
            district.setProvince(details.getProvince());
            return ResponseEntity.ok(repository.save(district));
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
