package guidance_system.controller;

import guidance_system.entity.Admin;
import guidance_system.repository.AdminRepository;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admins")
@CrossOrigin(origins = "*")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AdminRepository repository;
    private final PasswordEncoder passwordEncoder;

    public AdminController(
            AdminRepository repository,
            PasswordEncoder passwordEncoder) {

        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping
    public List<Admin> getAll() {
        return repository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Admin> getById(@PathVariable Long id) {
        return repository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody Admin admin) {

        if (admin.getPasswordHash() == null
                || admin.getPasswordHash().isBlank()) {
            return ResponseEntity.badRequest()
                    .body("Password is required.");
        }

        admin.setPasswordHash(
                passwordEncoder.encode(admin.getPasswordHash())
        );

        return ResponseEntity.ok(repository.save(admin));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Admin> update(
            @PathVariable Long id,
            @RequestBody Admin details) {

        return repository.findById(id).map(admin -> {

            admin.setUsername(details.getUsername());
            admin.setFullName(details.getFullName());
            admin.setAdminRole(details.getAdminRole());

            if (details.getPasswordHash() != null
                    && !details.getPasswordHash().isBlank()) {
                admin.setPasswordHash(
                        passwordEncoder.encode(details.getPasswordHash())
                );
            }

            return ResponseEntity.ok(repository.save(admin));

        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {

        if (!repository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }

        repository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}