
package guidance_system.controller;

import guidance_system.entity.Subdistrict;
import guidance_system.repository.SubdistrictRepository;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/subdistricts")
@CrossOrigin(origins = "*")
public class SubdistrictController {

    private final SubdistrictRepository repository;

    public SubdistrictController(SubdistrictRepository repository) {
        this.repository = repository;
    }

    // Admin and Teacher can view all subdistricts
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public List<Subdistrict> getAll() {
        return repository.findAll();
    }

    // Admin and Teacher can view one subdistrict
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ResponseEntity<Subdistrict> getById(@PathVariable Long id) {
        return repository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Only Admin can create
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Subdistrict create(@RequestBody Subdistrict subdistrict) {
        return repository.save(subdistrict);
    }

    // Only Admin can update
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Subdistrict> update(
            @PathVariable Long id,
            @RequestBody Subdistrict details) {

        return repository.findById(id).map(subdistrict -> {
            subdistrict.setName(details.getName());
            subdistrict.setZipcode(details.getZipcode());
            subdistrict.setDistrict(details.getDistrict());
            return ResponseEntity.ok(repository.save(subdistrict));
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
