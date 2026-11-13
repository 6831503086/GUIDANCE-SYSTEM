
package guidance_system.service;

import guidance_system.entity.Admin;
import guidance_system.entity.Post;
import guidance_system.repository.AdminRepository;
import guidance_system.repository.PostRepository;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class PostService {

    private final PostRepository repository;
    private final AdminRepository adminRepository;
    private final AdminActivityService activityService;

    public PostService(
            PostRepository repository,
            AdminRepository adminRepository,
            AdminActivityService activityService) {
        this.repository = repository;
        this.adminRepository = adminRepository;
        this.activityService = activityService;
    }

    // Get the currently logged-in Admin
    private Admin getCurrentAdmin() {
        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalStateException("Admin is not authenticated.");
        }

        String username = authentication.getName();

        return adminRepository.findByUsername(username)
                .orElseThrow(() ->
                        new IllegalArgumentException("Admin account not found."));
    }

    // Save a post as a draft
    public Post createDraft(Post post) {
        validateDates(post);
        validatePostType(post.getPostType());

        post.setAdmin(getCurrentAdmin());
        post.setPublishedAt(null);
        post.setIsHidden(false);
        post.setAutoHidden(false);

        Post savedPost = repository.save(post);

        activityService.logActivity(
                "POST_CREATED",
                "Created announcement: " + savedPost.getTitle(),
                "POST",
                savedPost.getPostId()
        );

        return savedPost;
    }

    // Publish an existing post
    public Optional<Post> publish(Long id) {
        return repository.findById(id).map(post -> {
            validatePostType(post.getPostType());
            validateRequiredFields(post);
            validateDates(post);

            post.setPublishedAt(LocalDateTime.now());
            post.setIsHidden(false);
            post.setAutoHidden(false);

            Post savedPost = repository.save(post);

            activityService.logActivity(
                    "POST_PUBLISHED",
                    "Published announcement: " + savedPost.getTitle(),
                    "POST",
                    savedPost.getPostId()
            );

            return savedPost;
        });
    }


    // Unpublish a post and return it to draft status
    public Optional<Post> unpublish(Long id) {
        return repository.findById(id).map(post -> {

            post.setPublishedAt(null);

            Post savedPost = repository.save(post);

            activityService.logActivity(
                    "POST_UNPUBLISHED",
                    "Unpublished announcement: " + savedPost.getTitle(),
                    "POST",
                    savedPost.getPostId()
            );

            return savedPost;
        });
    }


    // Hide a post
    public Optional<Post> hide(Long id) {
        return repository.findById(id).map(post -> {
            post.setIsHidden(true);
            post.setAutoHidden(false);

            Post savedPost = repository.save(post);

            activityService.logActivity(
                    "POST_HIDDEN",
                    "Hid announcement: " + savedPost.getTitle(),
                    "POST",
                    savedPost.getPostId()
            );

            return savedPost;
        });
    }

    // Show a post again
    public Optional<Post> show(Long id) {
        return repository.findById(id).map(post -> {
            post.setIsHidden(false);
            post.setAutoHidden(false);

            Post savedPost = repository.save(post);

            activityService.logActivity(
                    "POST_SHOWN",
                    "Showed announcement: " + savedPost.getTitle(),
                    "POST",
                    savedPost.getPostId()
            );

            return savedPost;
        });
    }

    private void validatePostType(String postType) {

    if (postType == null || postType.isBlank()) {
        throw new IllegalArgumentException(
                "Post type is required."
        );
    }

    boolean validType =
            "NEWS".equalsIgnoreCase(postType.trim())
            || "PR".equalsIgnoreCase(postType.trim())
            || "ANNOUNCEMENT".equalsIgnoreCase(postType.trim());

    if (!validType) {
        throw new IllegalArgumentException(
                "Invalid post type. Allowed values: NEWS, PR, ANNOUNCEMENT."
        );
    }
    }

    private void validateRequiredFields(Post post) {

    if (post.getTitle() == null || post.getTitle().isBlank()) {
        throw new IllegalArgumentException("Title is required.");
    }

    if (post.getSummary() == null || post.getSummary().isBlank()) {
        throw new IllegalArgumentException("Summary is required.");
    }

    if (post.getContent() == null || post.getContent().isBlank()) {
        throw new IllegalArgumentException("Content is required.");
    }

    if (post.getTargetAudience() == null
            || post.getTargetAudience().isBlank()) {
        throw new IllegalArgumentException("Target audience is required.");
    }

    if (post.getVisibleStartDate() == null) {
        throw new IllegalArgumentException("Display start date is required.");
    }

    if (post.getVisibleEndDate() == null) {
        throw new IllegalArgumentException("Display end date is required.");
    }
    }

    // Validate event and visibility dates
    private void validateDates(Post post) {

        LocalDate eventStart = post.getEventStartDate();
        LocalDate eventEnd = post.getEventEndDate();

        if (eventStart != null && eventEnd != null
                && eventEnd.isBefore(eventStart)) {
            throw new IllegalArgumentException(
                    "Event end date cannot be before event start date."
            );
        }

        LocalDate visibleStart = post.getVisibleStartDate();
        LocalDate visibleEnd = post.getVisibleEndDate();

        if (visibleStart != null && visibleEnd != null
                && visibleEnd.isBefore(visibleStart)) {
            throw new IllegalArgumentException(
                    "Visibility end date cannot be before visibility start date."
            );
        }
    }

    // Update an existing post
    // Update an existing post
    public Optional<Post> updatePost(Long id, Post details) {
        return repository.findById(id).map(post -> {
            validatePostType(details.getPostType());
            validateDates(details);

            if (post.getPublishedAt() != null) {
                validateRequiredFields(details);
            }

            // Check whether the post was automatically hidden
            boolean wasAutoHidden =
                    Boolean.TRUE.equals(post.getAutoHidden());

            post.setTitle(details.getTitle());
            post.setSummary(details.getSummary());
            post.setContent(details.getContent());
            post.setTargetAudience(details.getTargetAudience());
            post.setEventType(details.getEventType());
            post.setEventStartDate(details.getEventStartDate());
            post.setEventEndDate(details.getEventEndDate());
            post.setAcademicYear(details.getAcademicYear());
            post.setApplicationRound(details.getApplicationRound());
            post.setVisibleStartDate(details.getVisibleStartDate());
            post.setVisibleEndDate(details.getVisibleEndDate());
            post.setPostType(details.getPostType());

            // Restore automatically hidden posts if their end date is extended
            if (wasAutoHidden
                    && details.getVisibleEndDate() != null
                    && !details.getVisibleEndDate().isBefore(LocalDate.now())) {

                post.setIsHidden(false);
                post.setAutoHidden(false);
            }

            Post savedPost = repository.save(post);

            activityService.logActivity(
                    "POST_UPDATED",
                    "Updated announcement: " + savedPost.getTitle(),
                    "POST",
                    savedPost.getPostId()
            );

            return savedPost;
        });
    }
}
