package guidance_system.controller;

import guidance_system.entity.Post;
import guidance_system.repository.PostRepository;
import guidance_system.service.PostService;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import guidance_system.service.AdminActivityService;
import java.util.Locale;

@RestController
@RequestMapping("/api/posts")
@CrossOrigin(origins = "*")
public class PostController {

    private final PostRepository repository;
    private final PostService service;
    private final AdminActivityService activityService;

    public PostController(
            PostRepository repository,
            PostService service,
            AdminActivityService activityService) {
        this.repository = repository;
        this.service = service;
        this.activityService = activityService;
    }

    // Public users can view published and visible posts
    @GetMapping
    @PreAuthorize("permitAll()")
    public List<Post> getAll() {
        LocalDate today = LocalDate.now();

        return repository.findAll().stream()
                .filter(post -> post.getPublishedAt() != null)
                .filter(post -> !Boolean.TRUE.equals(post.getIsHidden()))
                .filter(post -> post.getVisibleStartDate() == null
                        || !post.getVisibleStartDate().isAfter(today))
                .filter(post -> post.getVisibleEndDate() == null
                        || !post.getVisibleEndDate().isBefore(today))
                .toList();
    }

    // Admins can filter and search all posts
    @GetMapping("/admin/all")
    @PreAuthorize("hasRole('ADMIN')")
    public List<Post> getAllForAdmin(
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String keyword) {

        LocalDate today = LocalDate.now();

        return repository.findAll().stream()

                // Filter by post type
                .filter(post -> type == null || type.isBlank()
                        || (post.getPostType() != null
                        && post.getPostType().equalsIgnoreCase(type.trim())))

                // Filter by status
                .filter(post -> {
                    if (status == null || status.isBlank()) {
                        return true;
                    }

                    return switch (status.trim().toUpperCase(Locale.ROOT)) {
                        case "DRAFT" ->
                                post.getPublishedAt() == null;

                        case "PUBLISHED" ->
                                post.getPublishedAt() != null;

                        case "HIDDEN" ->
                                Boolean.TRUE.equals(post.getIsHidden());

                        case "SHOWN" ->
                                !Boolean.TRUE.equals(post.getIsHidden());

                        case "EXPIRED" ->
                                post.getVisibleEndDate() != null
                                && post.getVisibleEndDate().isBefore(today);

                        default ->
                                throw new IllegalArgumentException(
                                        "Invalid status. Allowed values: DRAFT, PUBLISHED, HIDDEN, SHOWN, EXPIRED.");
                    };
                })

                // Search title, summary and content
                .filter(post -> {
                    if (keyword == null || keyword.isBlank()) {
                        return true;
                    }

                    String search = keyword.trim().toLowerCase(Locale.ROOT);

                    return (post.getTitle() != null
                            && post.getTitle().toLowerCase(Locale.ROOT).contains(search))
                            || (post.getSummary() != null
                            && post.getSummary().toLowerCase(Locale.ROOT).contains(search))
                            || (post.getContent() != null
                            && post.getContent().toLowerCase(Locale.ROOT).contains(search));
                })

                .toList();
    }

    // Public users can view an individual published post
    @GetMapping("/{id}")
    @PreAuthorize("permitAll()")
    public ResponseEntity<Post> getById(@PathVariable Long id) {
        return getAll().stream()
                .filter(post -> post.getPostId().equals(id))
                .findFirst()
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Only admins can create drafts
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Post create(@RequestBody Post post) {
        return service.createDraft(post);
    }

    // Only admins can update posts
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Post> update(
            @PathVariable Long id,
            @RequestBody Post details) {

        return service.updatePost(id, details)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Only admins can publish posts
    @PostMapping("/{id}/publish")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Post> publish(@PathVariable Long id) {
        return service.publish(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }


    // Only admins can unpublish posts
    @PatchMapping("/{id}/unpublish")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Post> unpublish(@PathVariable Long id) {
        return service.unpublish(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }


    // Only admins can hide posts
    @PatchMapping("/{id}/hide")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Post> hide(@PathVariable Long id) {
        return service.hide(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Only admins can show posts
    @PatchMapping("/{id}/show")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Post> show(@PathVariable Long id) {
        return service.show(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Only admins can delete posts

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {

        return repository.findById(id).map(post -> {

            activityService.logActivity(
                    "POST_DELETED",
                    "Deleted announcement: " + post.getTitle(),
                    "POST",
                    post.getPostId()
            );

            repository.delete(post);

            return ResponseEntity.noContent().<Void>build();

        }).orElse(ResponseEntity.notFound().build());
    }


    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleDateError(
            IllegalArgumentException exception) {
        return ResponseEntity.badRequest()
                .body(exception.getMessage());
    }
}