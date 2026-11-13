
package guidance_system.controller;

import guidance_system.entity.Post;
import guidance_system.entity.PostRegistration;
import guidance_system.entity.GuidanceTeacher;

import guidance_system.repository.PostRegistrationRepository;
import guidance_system.repository.PostRepository;
import guidance_system.repository.GuidanceTeacherRepository;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/post-registrations")
@CrossOrigin(origins = "*")
public class PostRegistrationController {

    private final PostRegistrationRepository registrationRepository;
    private final PostRepository postRepository;
    private final GuidanceTeacherRepository teacherRepository;

    public PostRegistrationController(
            PostRegistrationRepository registrationRepository,
            PostRepository postRepository,
            GuidanceTeacherRepository teacherRepository) {

        this.registrationRepository = registrationRepository;
        this.postRepository = postRepository;
        this.teacherRepository = teacherRepository;
    }

    // Only admins can view all registrations
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<PostRegistration> getAll() {
        return registrationRepository.findAll();
    }

    // Teachers can view their own registrations; admins can view any teacher
    @GetMapping("/teacher/{teacherId}")
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
    public ResponseEntity<?> getByTeacher(
            @PathVariable Long teacherId,
            Authentication authentication) {

        if (!isAdmin(authentication)) {
            GuidanceTeacher loggedInTeacher =
                    teacherRepository.findByEmail(authentication.getName())
                            .orElse(null);

            if (loggedInTeacher == null
                    || !loggedInTeacher.getTeacherId().equals(teacherId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body("You can only view your own registrations.");
            }
        }

        return ResponseEntity.ok(
                registrationRepository.findByTeacherTeacherId(teacherId)
        );
    }

    // Only admins can view registrations for a post
    @GetMapping("/post/{postId}")
    @PreAuthorize("hasRole('ADMIN')")
    public List<PostRegistration> getByPost(
            @PathVariable Long postId) {

        return registrationRepository.findByPostPostId(postId);
    }

    // A teacher can register themselves using their JWT identity
    @PostMapping
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<?> register(
            @RequestBody PostRegistration details,
            Authentication authentication) {

        if (details.getPost() == null
                || details.getPost().getPostId() == null) {
            return ResponseEntity.badRequest()
                    .body("Post ID is required.");
        }

        Long postId = details.getPost().getPostId();

        Post post = postRepository.findById(postId).orElse(null);

        GuidanceTeacher teacher =
                teacherRepository.findByEmail(authentication.getName())
                        .orElse(null);

        if (post == null || teacher == null) {
            return ResponseEntity.notFound().build();
        }

        if (registrationRepository
                .existsByPostPostIdAndTeacherTeacherId(
                        postId, teacher.getTeacherId())) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("You have already registered for this post.");
        }

        PostRegistration registration = new PostRegistration();
        registration.setPost(post);
        registration.setTeacher(teacher);
        registration.setRegisteredAt(LocalDateTime.now());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(registrationRepository.save(registration));
    }

    // Teachers can cancel their own registration; admins can cancel any
    @DeleteMapping("/{registrationId}")
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
    public ResponseEntity<?> cancel(
            @PathVariable Long registrationId,
            Authentication authentication) {

        PostRegistration registration =
                registrationRepository.findById(registrationId).orElse(null);

        if (registration == null) {
            return ResponseEntity.notFound().build();
        }

        if (!isAdmin(authentication)) {
            GuidanceTeacher loggedInTeacher =
                    teacherRepository.findByEmail(authentication.getName())
                            .orElse(null);

            if (loggedInTeacher == null
                    || !loggedInTeacher.getTeacherId().equals(
                            registration.getTeacher().getTeacherId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body("You can only cancel your own registration.");
            }
        }

        registrationRepository.delete(registration);

        return ResponseEntity.noContent().build();
    }

    private boolean isAdmin(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .anyMatch(authority ->
                        authority.getAuthority().equals("ROLE_ADMIN"));
    }
}
