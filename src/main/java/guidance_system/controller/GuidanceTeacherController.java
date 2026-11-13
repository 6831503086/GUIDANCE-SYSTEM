package guidance_system.controller;

import guidance_system.entity.GuidanceTeacher;
import guidance_system.entity.Prefix;
import guidance_system.repository.GuidanceTeacherRepository;
import guidance_system.service.GuidanceTeacherService;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;

import guidance_system.service.ProfilePictureService;
import org.springframework.web.multipart.MultipartFile;
import guidance_system.repository.PrefixRepository;
import java.io.IOException;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/guidance-teachers")
@CrossOrigin(origins = "*")
@PreAuthorize("hasRole('ADMIN')")
public class GuidanceTeacherController {

    private final GuidanceTeacherRepository repository;
    private final GuidanceTeacherService service;
    private final ProfilePictureService profilePictureService;
    private final PrefixRepository prefixRepository;

    public GuidanceTeacherController(
            GuidanceTeacherRepository repository,
            GuidanceTeacherService service,
            ProfilePictureService profilePictureService,
            PrefixRepository prefixRepository){
        this.repository = repository;
        this.service = service;
        this.profilePictureService = profilePictureService;
        this.prefixRepository = prefixRepository;
    }

    @GetMapping
    public List<GuidanceTeacher> getAll() {
        return repository.findAll();
    }

    @GetMapping("/{id:[0-9]+}")
    public ResponseEntity<GuidanceTeacher> getById(
            @PathVariable Long id) {
        return repository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Public teacher registration
    @PostMapping
    @PreAuthorize("permitAll()")
    public ResponseEntity<?> create(
            @RequestBody GuidanceTeacher teacher) {
        try {
            return ResponseEntity.ok(
                    service.registerTeacher(teacher));
        } catch (IllegalArgumentException exception) {
            return ResponseEntity.badRequest()
                    .body(exception.getMessage());
        }
    }

    @GetMapping("/me")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<GuidanceTeacher> getMyProfile(
            Authentication authentication) {

        return repository.findByUsername(authentication.getName())
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/me/renew")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<?> renewMyAcademicYear(
            Authentication authentication,
            @RequestBody java.util.Map<String, Object> request) {

        try {
            @SuppressWarnings("unchecked")
            java.util.Map<String, Object> teacherData =
                    (java.util.Map<String, Object>) request.get("teacher");

            @SuppressWarnings("unchecked")
            java.util.Map<String, Object> prefixData =
                    (java.util.Map<String, Object>) teacherData.get("prefix");

            @SuppressWarnings("unchecked")
            java.util.Map<String, Object> schoolData =
                    (java.util.Map<String, Object>) teacherData.get("school");

            GuidanceTeacher details = new GuidanceTeacher();

            guidance_system.entity.Prefix prefix =
                    new guidance_system.entity.Prefix();
            prefix.setPrefixId(
                    Long.valueOf(prefixData.get("prefixId").toString()));
            details.setPrefix(prefix);

            details.setFirstName((String) teacherData.get("firstName"));
            details.setLastName((String) teacherData.get("lastName"));
            details.setPhone((String) teacherData.get("phone"));
            details.setPositionTitle(
                    (String) teacherData.get("positionTitle"));

            guidance_system.entity.School school =
                    new guidance_system.entity.School();
            school.setName((String) schoolData.get("name"));
            school.setSchoolCode((String) schoolData.get("schoolCode"));
            school.setSchoolType((String) schoolData.get("schoolType"));

            details.setSchool(school);

            Integer targetYear = Integer.valueOf(
                    request.get("targetAcademicYear").toString());

            GuidanceTeacher renewed = service.renewAcademicYear(
                    authentication.getName(),
                    details,
                    targetYear);

            return ResponseEntity.ok(
                    java.util.Map.of(
                            "message", "Academic year renewed successfully.",
                            "activeAcademicYear",
                            renewed.getActiveAcademicYear()));

        } catch (IllegalArgumentException exception) {
            return ResponseEntity.badRequest()
                    .body(exception.getMessage());
        }
    }

    @PutMapping("/me")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<?> updateMyProfile(
        Authentication authentication,
        @RequestBody GuidanceTeacher details) {

        Prefix prefix = prefixRepository
                .findById(details.getPrefix().getPrefixId())
                .orElse(null);

        if (prefix == null) {
        return ResponseEntity.badRequest()
                .body("Invalid prefix ID.");
        }

        if (details.getPhone() != null &&
        !details.getPhone().matches("^0[0-9]{9}$")) {

        return ResponseEntity.badRequest()
                .body("Please enter a valid 10-digit phone number.");
        }

        return repository.findByUsername(authentication.getName())
            .map(teacher -> {

                if (details.getFirstName() == null ||
                    details.getFirstName().isBlank() ||
                    details.getLastName() == null ||
                    details.getLastName().isBlank() ||
                    details.getPrefix() == null ||
                    details.getPrefix().getPrefixId() == null) {

                    return ResponseEntity.badRequest()
                            .body("Prefix, first name and last name are required.");
                }

                teacher.setPrefix(prefix);
                teacher.setFirstName(details.getFirstName());
                teacher.setLastName(details.getLastName());
                teacher.setPhone(details.getPhone());

                return ResponseEntity.ok(repository.save(teacher));
            })
            .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping(value = "/me/profile-picture",
        consumes = "multipart/form-data")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<?> uploadMyProfilePicture(
        Authentication authentication,
        @RequestParam("file") MultipartFile file) {

    try {
        GuidanceTeacher teacher = repository
                .findByUsername(authentication.getName())
                .orElse(null);

        if (teacher == null) {
            return ResponseEntity.notFound().build();
        }

        String pictureUrl =
                profilePictureService.uploadPicture(teacher, file);

        return ResponseEntity.ok(
                java.util.Map.of(
                        "message", "Profile picture uploaded successfully.",
                        "profilePicture", pictureUrl
                )
        );

    } 
    
    catch (IllegalArgumentException exception) {
        return ResponseEntity.badRequest()
                .body(exception.getMessage());

    } 
    
    catch (IOException exception) {
        return ResponseEntity.internalServerError()
                .body("Unable to upload profile picture.");
    }
}

    @PutMapping("/me/email")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<String> requestEmailChange(
        Authentication authentication,
        @RequestBody java.util.Map<String, String> request) {

        try 
        {
            service.requestEmailChange(
                    authentication.getName(),
                    request.get("newEmail")
            );

            return ResponseEntity.ok(
                    "Verification email sent to your new email address."
            );

        } 
    
        catch (IllegalArgumentException exception) 
        {
            return ResponseEntity.badRequest()
                    .body(exception.getMessage());
        }
    }

    @DeleteMapping("/me")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<?> deleteMyAccount(
        Authentication authentication) {

    GuidanceTeacher teacher = repository
            .findByUsername(authentication.getName())
            .orElse(null);

    if (teacher == null) {
        return ResponseEntity.notFound().build();
    }

    Long teacherId = teacher.getTeacherId();

    try {
        service.deleteTeacherAccount(teacherId);

        return ResponseEntity.ok(
                Map.of(
                        "message", "Your account has been deleted successfully.",
                        "teacherId", teacherId
                )
        );

    } catch (IllegalArgumentException exception) {
        return ResponseEntity.notFound().build();
    }
    }

    @PutMapping("/{id}")
    public ResponseEntity<GuidanceTeacher> update(
            @PathVariable Long id,
            @RequestBody GuidanceTeacher details) {

        return repository.findById(id).map(teacher -> {
            teacher.setSchool(details.getSchool());
            teacher.setPrefix(details.getPrefix());
            teacher.setFirstName(details.getFirstName());
            teacher.setLastName(details.getLastName());
            teacher.setPositionTitle(details.getPositionTitle());
            teacher.setPhone(details.getPhone());
            teacher.setEmail(details.getEmail());
            teacher.setUsername(details.getUsername());
            teacher.setPdpaConsent(details.getPdpaConsent());
            teacher.setThaidSub(details.getThaidSub());
            teacher.setActiveAcademicYear(
                    details.getActiveAcademicYear());

            return ResponseEntity.ok(repository.save(teacher));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {

        try {
        service.deleteTeacherAccount(id);

        return ResponseEntity.ok(
                Map.of(
                        "message", "Teacher account deleted successfully.",
                        "teacherId", id
                )
        );

        } 
        catch (IllegalArgumentException e) {
                return ResponseEntity.notFound().build();
        }

    }
}