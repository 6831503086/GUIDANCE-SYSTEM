
package guidance_system.controller;

import guidance_system.entity.School;
import guidance_system.repository.SchoolRepository;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import guidance_system.repository.GuidanceTeacherRepository;
import org.springframework.security.core.Authentication;

import java.util.List;

@RestController
@RequestMapping("/api/schools")
@CrossOrigin(origins = "*")
public class SchoolController {

    private final SchoolRepository schoolRepository;
    private final GuidanceTeacherRepository teacherRepository;

    public SchoolController(
            SchoolRepository schoolRepository,
            GuidanceTeacherRepository teacherRepository) {
        this.schoolRepository = schoolRepository;
        this.teacherRepository = teacherRepository;
    }

    // Admin and Teacher can view all schools
    
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
    public List<School> getAllSchools(Authentication authentication) {

        // Admin can view all schools
        if (authentication.getAuthorities().stream()
                .anyMatch(authority ->
                        authority.getAuthority().equals("ROLE_ADMIN"))) {
            return schoolRepository.findAll()
                .stream()
                .map(this::setAccountStatus)
                .toList();
        }

        // Teacher can view only their own school
        return teacherRepository.findByUsername(authentication.getName())
                .map(teacher -> {
                    if (teacher.getSchool() == null) {
                        return List.<School>of();
                    }
                    return List.of(teacher.getSchool());
                })
                .orElseGet(List::of);
    }


    // Admin can view any school; Teacher can view only their own school
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ResponseEntity<School> getSchoolById(
            @PathVariable Long id,
            Authentication authentication) {

        if (authentication.getAuthorities().stream()
                .anyMatch(authority ->
                        authority.getAuthority().equals("ROLE_ADMIN"))) {

            return schoolRepository.findById(id)
                .map(this::setAccountStatus)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
        }

        return teacherRepository.findByUsername(authentication.getName())
                .filter(teacher -> teacher.getSchool() != null)
                .filter(teacher ->
                        teacher.getSchool().getSchoolId().equals(id))
                .flatMap(teacher -> schoolRepository.findById(id))
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Only Admin can create a school
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public School createSchool(@RequestBody School school) {
        return schoolRepository.save(school);
    }

    // Only Admin can update a school
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<School> updateSchool(
            @PathVariable Long id,
            @RequestBody School schoolDetails) {

        return schoolRepository.findById(id)
                .map(school -> {
                    school.setName(schoolDetails.getName());
                    school.setSchoolCode(schoolDetails.getSchoolCode());
                    school.setSchoolType(schoolDetails.getSchoolType());
                    school.setSubdistrict(schoolDetails.getSubdistrict());

                    return ResponseEntity.ok(
                            schoolRepository.save(school)
                    );
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // Only Admin can delete a school
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteSchool(@PathVariable Long id) {
        if (!schoolRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }

        schoolRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }


    // FR-15: View own school information
    @GetMapping("/me")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<School> getMySchool(
            Authentication authentication) {

        return teacherRepository.findByUsername(authentication.getName())
                .map(teacher -> teacher.getSchool())
                .map(school -> ResponseEntity.ok(school))
                .orElse(ResponseEntity.notFound().build());
    }

    // FR-15: Update own school information
    @PutMapping("/me")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<?> updateMySchool(
            Authentication authentication,
            @RequestBody School details) {

        return teacherRepository.findByUsername(authentication.getName())
                .map(teacher -> {
                    School school = teacher.getSchool();

                    if (school == null) {
                        return ResponseEntity.notFound().build();
                    }

                    if (details.getName() == null
                            || details.getName().isBlank()) {
                        return ResponseEntity.badRequest()
                                .body("School name is required.");
                    }

                    school.setName(details.getName().trim());
                    school.setSchoolType(details.getSchoolType());
                    school.setSubdistrict(details.getSubdistrict());

                    return ResponseEntity.ok(schoolRepository.save(school));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    private School setAccountStatus(School school) {

    boolean hasTeacher = teacherRepository
            .existsBySchool_SchoolId(school.getSchoolId());

    school.setAccountStatus(
            hasTeacher ? "ACTIVE" : "NO_GUIDANCE_TEACHER"
    );

    return school;
    }

}
