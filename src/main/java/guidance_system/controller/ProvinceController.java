
package guidance_system.controller;

import guidance_system.entity.Province;
import guidance_system.repository.ProvinceRepository;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/provinces")
@CrossOrigin(origins = "*")
public class ProvinceController {

    private final ProvinceRepository repository;

    public ProvinceController(ProvinceRepository repository) {
        this.repository = repository;
    }

    // Admin and Teacher can view all provinces
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public List<Province> getAll() {
        return repository.findAll();
    }

    // Admin and Teacher can view one province
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ResponseEntity<Province> getById(@PathVariable Long id) {
        return repository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Only Admin can create
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Province create(@RequestBody Province province) {
        return repository.save(province);
    }

    // Only Admin can update
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Province> update(
            @PathVariable Long id,
            @RequestBody Province details) {

        return repository.findById(id).map(province -> {
            province.setName(details.getName());
            province.setRegion(details.getRegion());
            return ResponseEntity.ok(repository.save(province));
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
