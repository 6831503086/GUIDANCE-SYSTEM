
package guidance_system.controller;

import guidance_system.entity.Post;
import guidance_system.entity.PostPin;
import guidance_system.entity.PostPinId;
import guidance_system.entity.GuidanceTeacher;

import guidance_system.repository.PostPinRepository;
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
@RequestMapping("/api/post-pins")
@CrossOrigin(origins = "*")
public class PostPinController {

    private final PostPinRepository pinRepository;
    private final PostRepository postRepository;
    private final GuidanceTeacherRepository teacherRepository;

    public PostPinController(
            PostPinRepository pinRepository,
            PostRepository postRepository,
            GuidanceTeacherRepository teacherRepository) {

        this.pinRepository = pinRepository;
        this.postRepository = postRepository;
        this.teacherRepository = teacherRepository;
    }

    // View only the logged-in teacher's pinned posts
    @GetMapping("/teacher/{teacherId}")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<?> getByTeacher(
            @PathVariable Long teacherId,
            Authentication authentication) {

        GuidanceTeacher teacher =
                teacherRepository.findByEmail(authentication.getName())
                        .orElse(null);

        if (teacher == null
                || !teacher.getTeacherId().equals(teacherId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("You can only view your own pinned posts.");
        }

        List<PostPin> pins =
                pinRepository.findByTeacherTeacherIdOrderByPinnedAtDesc(
                        teacherId);

        return ResponseEntity.ok(pins);
    }

    // Pin a post for the logged-in teacher
    @PostMapping
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<?> pinPost(
            @RequestBody PostPin details,
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

        Long teacherId = teacher.getTeacherId();

        if (pinRepository.existsByPostPostIdAndTeacherTeacherId(
                postId, teacherId)) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("This post is already pinned.");
        }

        PostPin pin = new PostPin();
        pin.setPost(post);
        pin.setTeacher(teacher);
        pin.setPinnedAt(LocalDateTime.now());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(pinRepository.save(pin));
    }

    // Unpin only the logged-in teacher's post
    @DeleteMapping("/{postId}/{teacherId}")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<?> unpinPost(
            @PathVariable Long postId,
            @PathVariable Long teacherId,
            Authentication authentication) {

        GuidanceTeacher teacher =
                teacherRepository.findByEmail(authentication.getName())
                        .orElse(null);

        if (teacher == null
                || !teacher.getTeacherId().equals(teacherId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("You can only unpin your own posts.");
        }

        PostPinId pinId = new PostPinId(
                postId, teacher.getTeacherId()
        );

        if (!pinRepository.existsById(pinId)) {
            return ResponseEntity.notFound().build();
        }

        pinRepository.deleteById(pinId);

        return ResponseEntity.noContent().build();
    }
}
