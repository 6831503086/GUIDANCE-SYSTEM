
package guidance_system.controller;

import guidance_system.dto.ProgramResponse;
import guidance_system.entity.Program;
import guidance_system.repository.ProgramRepository;
import guidance_system.service.ProgramService;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/programs")
@CrossOrigin(origins = "*")
public class ProgramController {

    private final ProgramRepository repository;
    private final ProgramService service;

    public ProgramController(
            ProgramRepository repository,
            ProgramService service) {
        this.repository = repository;
        this.service = service;
    }

    // Teachers and admins can search and view programs
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public List<ProgramResponse> getAll(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer academicYear,
            @RequestParam(required = false) String round,
            @RequestParam(required = false) String status) {

        return service.searchPrograms(
                keyword, academicYear, round, status);
    }

    // View individual program details
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ResponseEntity<ProgramResponse> getById(@PathVariable Long id) {

        ProgramResponse response = service.getProgramById(id);

        if (response == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(response);
    }

    // Only admins can create programs
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Program create(@RequestBody Program program) {
        return repository.save(program);
    }

    // Only admins can update programs
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Program> update(
            @PathVariable Long id,
            @RequestBody Program details) {

        return repository.findById(id)
                .map(program -> {
                    program.setEntryGroup(details.getEntryGroup());
                    program.setProgramName(details.getProgramName());
                    program.setFaculty(details.getFaculty());
                    program.setDescription(details.getDescription());

                    return ResponseEntity.ok(repository.save(program));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // Only admins can delete programs
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
