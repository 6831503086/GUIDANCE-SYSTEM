
package guidance_system.controller;

import guidance_system.entity.Applicant;
import guidance_system.entity.ApplicantSelection;
import guidance_system.entity.GuidanceTeacher;
import guidance_system.dto.StudentSearchResponse;

import guidance_system.repository.ApplicantRepository;
import guidance_system.repository.ApplicantSelectionRepository;
import guidance_system.repository.GuidanceTeacherRepository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;

import org.springframework.web.bind.annotation.*;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/applicants")
@CrossOrigin(origins = "*")
public class ApplicantController {

    private final ApplicantRepository repository;
    private final ApplicantSelectionRepository selectionRepository;
    private final GuidanceTeacherRepository teacherRepository;

    public ApplicantController(
            ApplicantRepository repository,
            ApplicantSelectionRepository selectionRepository,
            GuidanceTeacherRepository teacherRepository) {

        this.repository = repository;
        this.selectionRepository = selectionRepository;
        this.teacherRepository = teacherRepository;
    }

    // Admin only: View all applicants
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<Applicant> getAll() {
        return repository.findAll();
    }

    // Admin only: View individual applicant
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Applicant> getById(@PathVariable Long id) {
        return repository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Admin only: Create applicant
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Applicant create(@RequestBody Applicant applicant) {
        return repository.save(applicant);
    }

    // Admin only: Update applicant
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Applicant> update(
            @PathVariable Long id,
            @RequestBody Applicant details) {

        return repository.findById(id)
                .map(applicant -> {
                    applicant.setSchool(details.getSchool());
                    applicant.setNationalId(details.getNationalId());
                    applicant.setFirstName(details.getFirstName());
                    applicant.setLastName(details.getLastName());
                    applicant.setAcademicYear(details.getAcademicYear());
                    applicant.setTerm(details.getTerm());

                    return ResponseEntity.ok(repository.save(applicant));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // Admin only: Delete applicant
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {

        if (!repository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }

        repository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    // Admins can search across schools.
    // Teachers can search only applicants from their assigned school.
    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ResponseEntity<?> searchStudents(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long schoolId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Authentication authentication) {

        // Validate pagination
        if (page < 0 || size < 1 || size > 100) {
            return ResponseEntity.badRequest()
                    .body("Invalid pagination values.");
        }

        // Determine the effective school filter
        boolean isTeacher = authentication.getAuthorities()
                .stream()
                .anyMatch(authority ->
                        authority.getAuthority().equals("ROLE_TEACHER"));

        if (isTeacher) {

            GuidanceTeacher teacher = teacherRepository
                    .findByEmail(authentication.getName())
                    .orElse(null);

            if (teacher == null || teacher.getSchool() == null
                    || teacher.getSchool().getSchoolId() == null) {

                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body("Teacher school information is unavailable.");
            }

            // Always restrict teachers to their own school.
            // Ignore any schoolId supplied in the request.
            schoolId = teacher.getSchool().getSchoolId();
        }

        String searchKeyword =
                (keyword == null || keyword.isBlank())
                        ? null
                        : keyword.trim();

        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by("applicantId").descending()
        );

        Page<Applicant> applicants = repository.searchStudents(
                searchKeyword,
                schoolId,
                pageable
        );

        List<Long> applicantIds = applicants.getContent()
                .stream()
                .map(Applicant::getApplicantId)
                .toList();

        List<ApplicantSelection> selections =
                applicantIds.isEmpty()
                        ? List.of()
                        : selectionRepository
                                .findByApplicant_ApplicantIdIn(applicantIds);

        Map<Long, ApplicantSelection> latestSelections =
                selections.stream()
                        .collect(Collectors.toMap(
                                selection -> selection.getApplicant()
                                        .getApplicantId(),
                                selection -> selection,
                                (first, second) -> {

                                    Comparator<ApplicantSelection> comparator =
                                            Comparator.comparing(
                                                    ApplicantSelection::getAppliedAt,
                                                    Comparator.nullsFirst(
                                                            Comparator.naturalOrder()
                                                    )
                                            ).thenComparing(
                                                    ApplicantSelection::getApplicationId
                                            );

                                    return comparator.compare(first, second) >= 0
                                            ? first : second;
                                }
                        ));

        List<StudentSearchResponse> results =
                applicants.getContent().stream()
                        .map(applicant -> {

                            ApplicantSelection latest =
                                    latestSelections.get(
                                            applicant.getApplicantId());

                            String status = latest == null
                                    ? "No application"
                                    : latest.getStatus().getStatusCode();

                            String statusDescription = latest == null
                                    ? "No application record"
                                    : latest.getStatus().getDescription();

                            return new StudentSearchResponse(
                                    applicant.getApplicantId(),
                                    (applicant.getFirstName() == null ? "" :
                                            applicant.getFirstName()) + " " +
                                    (applicant.getLastName() == null ? "" :
                                            applicant.getLastName()),
                                    applicant.getNationalId(),
                                    applicant.getSchool().getName(),
                                    status,
                                    statusDescription
                            );
                        })
                        .toList();

        return ResponseEntity.ok(Map.of(
                "content", results,
                "currentPage", applicants.getNumber(),
                "totalPages", applicants.getTotalPages(),
                "totalItems", applicants.getTotalElements(),
                "pageSize", applicants.getSize()
        ));
    }
}
