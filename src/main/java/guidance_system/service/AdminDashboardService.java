
package guidance_system.service;

import guidance_system.dto.AdminDashboardResponse;
import guidance_system.entity.AdminActivity;
import guidance_system.entity.Applicant;
import guidance_system.entity.ApplicantSelection;
import guidance_system.entity.Post;
import guidance_system.entity.ResultDataset;

import guidance_system.repository.AdminActivityRepository;
import guidance_system.repository.ApplicantRepository;
import guidance_system.repository.ApplicantSelectionRepository;
import guidance_system.repository.PostRepository;
import guidance_system.repository.ResultDatasetRepository;

import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@org.springframework.transaction.annotation.Transactional(readOnly=true)
public class AdminDashboardService {

    private final PostRepository postRepository;
    private final ApplicantRepository applicantRepository;
    private final ApplicantSelectionRepository selectionRepository;
    private final ResultDatasetRepository datasetRepository;
    private final AdminActivityRepository activityRepository;

    public AdminDashboardService(
            PostRepository postRepository,
            ApplicantRepository applicantRepository,
            ApplicantSelectionRepository selectionRepository,
            ResultDatasetRepository datasetRepository,
            AdminActivityRepository activityRepository) {

        this.postRepository = postRepository;
        this.applicantRepository = applicantRepository;
        this.selectionRepository = selectionRepository;
        this.datasetRepository = datasetRepository;
        this.activityRepository = activityRepository;
    }

    public AdminDashboardResponse generateDashboard(
            Integer academicYear,
            String status,
            Integer days) {

        if(academicYear!=null&&(academicYear<2400||academicYear>3000))throw new IllegalArgumentException("Invalid academic year.");
        if(days!=null&&!List.of(7,30,90,365).contains(days))throw new IllegalArgumentException("Unsupported time period.");
        int periodDays = days == null ? 30 : days;

        LocalDateTime periodEnd = LocalDateTime.now();
        LocalDateTime periodStart = periodEnd.minusDays(periodDays);
        LocalDate today = LocalDate.now();

        String cleanedStatus = clean(status);
        final String normalizedStatus = cleanedStatus==null?null:cleanedStatus.toUpperCase(java.util.Locale.ROOT);

        if (normalizedStatus != null) {
        List<String> allowedStatuses = List.of(
                "DRAFT",
                "UNPUBLISHED",
                "PUBLISHED",
                "HIDDEN",
                "EXPIRED",
                "PENDING_REVIEW",
                "NEWS",
                "ANNOUNCEMENT"
        );

        boolean validStatus = allowedStatuses.stream()
                .anyMatch(s -> s.equalsIgnoreCase(normalizedStatus))
                || selectionRepository.findAll().stream()
                        .anyMatch(selection ->
                                selection.getStatus() != null
                                && (normalizedStatus.equalsIgnoreCase(
                                        selection.getStatus().getStatusCode())
                                || normalizedStatus.equalsIgnoreCase(
                                        selection.getStatus().getDescription()))
                        );

        if (!validStatus) {
                throw new IllegalArgumentException(
                        "Unsupported dashboard status: " + normalizedStatus
                );
        }
        }

        // -----------------------------------------
        // 1. ANNOUNCEMENT STATISTICS
        // -----------------------------------------

        List<Post> posts = postRepository.findAll().stream()
                .filter(post ->
                        post.getPublishedAt() == null
                        || (!post.getPublishedAt().isBefore(periodStart)
                        && !post.getPublishedAt().isAfter(periodEnd)))
                .filter(post -> matchesPostStatus(post, normalizedStatus))
                .filter(post -> academicYear == null
                        || Objects.equals(post.getAcademicYear(), academicYear))
                .toList();

        long totalPosts = posts.size();

        long publishedPosts = posts.stream()
                .filter(post -> post.getPublishedAt() != null)
                .count();

        long draftPosts = posts.stream()
                .filter(post -> post.getPublishedAt() == null)
                .count();

        long hiddenPosts = posts.stream()
                .filter(post -> Boolean.TRUE.equals(post.getIsHidden()))
                .count();

        long expiredPosts = posts.stream()
                .filter(post -> post.getVisibleEndDate() != null
                        && post.getVisibleEndDate().isBefore(today))
                .count();

        Map<String, Long> contentByType = posts.stream()
        .filter(post -> post.getPostType() != null)
        .collect(Collectors.groupingBy(
                post -> String.valueOf(post.getPostType()).toUpperCase().trim(),
                LinkedHashMap::new,
                Collectors.counting()
        ));

        AdminDashboardResponse.AnnouncementStats announcementStats =
                new AdminDashboardResponse.AnnouncementStats(
                        totalPosts,
                        publishedPosts,
                        draftPosts,
                        hiddenPosts,
                        expiredPosts,
                        contentByType
                );

        // -----------------------------------------
        // 2. APPLICANT STATISTICS
        // -----------------------------------------

        List<Applicant> applicants = applicantRepository.findAll().stream()
                .filter(applicant -> academicYear == null
                        || Objects.equals(
                                applicant.getAcademicYear(), academicYear))
                .toList();

        AdminDashboardResponse.ApplicantStats applicantStats =
                new AdminDashboardResponse.ApplicantStats(
                        applicants.size(),
                        academicYear
                );

        // -----------------------------------------
        // 3. SELECTION RESULT STATISTICS
        // -----------------------------------------

        List<ApplicantSelection> selections =
                selectionRepository.findAll().stream()
                        .filter(selection ->
                                selection.getEntryGroup() != null)
                        .filter(selection -> academicYear == null
                                || Objects.equals(
                                        selection.getEntryGroup()
                                                .getAcademicYear(),
                                        academicYear))
                        .filter(selection ->
                                selection.getPublishedAt() == null
                                || (!selection.getPublishedAt()
                                        .isBefore(periodStart)
                                && !selection.getPublishedAt()
                                        .isAfter(periodEnd)))
                        .filter(selection ->
                                matchesSelectionStatus(
                                        selection, normalizedStatus))
                        .toList();

        long totalSelections = selections.size();

        long publishedSelections = selections.stream()
                .filter(selection -> selection.getPublishedAt() != null)
                .count();

        long unpublishedSelections = selections.stream()
                .filter(selection -> selection.getPublishedAt() == null)
                .count();

        Map<String, Long> selectionsByStatus = selections.stream()
                .filter(selection -> selection.getStatus() != null)
                .collect(Collectors.groupingBy(
                        selection -> {
                            String description =
                                    selection.getStatus().getDescription();

                            return description != null
                                    ? description
                                    : selection.getStatus().getStatusCode();
                        },
                        LinkedHashMap::new,
                        Collectors.counting()
                ));

        AdminDashboardResponse.SelectionStats selectionStats =
                new AdminDashboardResponse.SelectionStats(
                        totalSelections,
                        publishedSelections,
                        unpublishedSelections,
                        selectionsByStatus
                );

        // -----------------------------------------
        // 4. RESULT DATASET STATISTICS
        // -----------------------------------------

        List<ResultDataset> datasets = datasetRepository.findAll().stream()
                .filter(dataset -> dataset.getUploadedAt() != null)
                .filter(dataset ->
                        !dataset.getUploadedAt().isBefore(periodStart)
                        && !dataset.getUploadedAt().isAfter(periodEnd))
                .filter(dataset -> academicYear == null
                        || (dataset.getProgram() != null
                        && dataset.getProgram().getEntryGroup() != null
                        && Objects.equals(
                                dataset.getProgram().getEntryGroup()
                                        .getAcademicYear(),
                                academicYear)))
                .filter(dataset -> matchesDatasetStatus(dataset, normalizedStatus))
                .toList();

        long totalDatasets = datasets.size();

        long publishedDatasets = datasets.stream()
                .filter(dataset ->
                        "PUBLISHED".equalsIgnoreCase(
                                dataset.getDatasetStatus()))
                .count();

        long pendingDatasets = datasets.stream()
                .filter(dataset ->
                        "PENDING_REVIEW".equalsIgnoreCase(
                                dataset.getDatasetStatus()))
                .count();

        long erroneousDatasets = datasets.stream()
                .filter(dataset -> dataset.getErrorRows() != null
                        && dataset.getErrorRows() > 0)
                .count();

        AdminDashboardResponse.ResultDatasetStats datasetStats =
                new AdminDashboardResponse.ResultDatasetStats(
                        totalDatasets,
                        publishedDatasets,
                        pendingDatasets,
                        erroneousDatasets
                );

        // -----------------------------------------
        // 5. PENDING TASKS
        // -----------------------------------------

        List<AdminDashboardResponse.PendingTaskItem> pendingTaskItems =
                new java.util.ArrayList<>();

        // Add draft announcements
        posts.stream()
                .filter(post -> post.getPublishedAt() == null)
                .forEach(post -> pendingTaskItems.add(
                        new AdminDashboardResponse.PendingTaskItem(
                                "DRAFT_ANNOUNCEMENT",
                                post.getPostId(),
                                post.getTitle()==null?"ประกาศ #"+post.getPostId():post.getTitle(),
                                "DRAFT",
                                "Announcement is waiting to be published."
                        )
                ));

        // Add datasets pending review
        datasets.stream()
                .filter(dataset ->
                        "PENDING_REVIEW".equalsIgnoreCase(
                                dataset.getDatasetStatus()))
                .forEach(dataset -> {

                String title = dataset.getFileName();

                if (title == null || title.isBlank()) {
                        title = "Dataset #" + dataset.getDatasetId();
                }

                String description =
                        "Total records: " + dataset.getTotalRows()
                        + ", Error records: " + dataset.getErrorRows();

                pendingTaskItems.add(
                        new AdminDashboardResponse.PendingTaskItem(
                                "DATASET_REVIEW",
                                dataset.getDatasetId(),
                                title,
                                dataset.getDatasetStatus(),
                                description
                        )
                );
                });

        long pendingTasks = pendingTaskItems.size();

        AdminDashboardResponse.PendingTaskStats pendingTaskStats =
                new AdminDashboardResponse.PendingTaskStats(
                        pendingTasks,
                        pendingDatasets,
                        draftPosts,
                        pendingTaskItems
                );


        // -----------------------------------------
        // 6. RECENT ADMIN ACTIVITIES
        // -----------------------------------------

        List<AdminDashboardResponse.RecentActivity> recentActivities = activityRepository
                .findByCreatedAtBetweenOrderByCreatedAtDesc(
                        periodStart,
                        periodEnd
                )
                .stream()
                .map(this::toRecentActivity)
                .toList();

        // -----------------------------------------
        // 7. RETURN DASHBOARD RESPONSE
        // -----------------------------------------

        return new AdminDashboardResponse(
                periodStart,
                periodEnd,
                academicYear,
                normalizedStatus,
                announcementStats,
                applicantStats,
                selectionStats,
                datasetStats,
                pendingTaskStats,
                recentActivities
        );
    }

    private boolean matchesPostStatus(Post post,String status) {
        if(status==null)return true;
        return switch(status){
            case "UNPUBLISHED","DRAFT" -> post.getPublishedAt()==null;
            case "PUBLISHED" -> post.getPublishedAt()!=null;
            case "HIDDEN" -> Boolean.TRUE.equals(post.getIsHidden());
            case "EXPIRED" -> post.getVisibleEndDate()!=null&&post.getVisibleEndDate().isBefore(LocalDate.now());
            case "NEWS","ANNOUNCEMENT" -> status.equalsIgnoreCase(post.getPostType());
            default -> false;
        };
    }
    private boolean matchesSelectionStatus(ApplicantSelection selection,String status) {
        if(status==null)return true;
        if("PUBLISHED".equals(status))return selection.getPublishedAt()!=null;
        if("UNPUBLISHED".equals(status))return selection.getPublishedAt()==null;
        return selection.getStatus()!=null&&(status.equalsIgnoreCase(selection.getStatus().getStatusCode())||status.equalsIgnoreCase(selection.getStatus().getDescription()));
    }
    private boolean matchesDatasetStatus(ResultDataset dataset,String status){
        if(status==null)return true;
        if("UNPUBLISHED".equals(status))return !"PUBLISHED".equalsIgnoreCase(dataset.getDatasetStatus());
        return status.equalsIgnoreCase(dataset.getDatasetStatus());
    }

    private String clean(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }


        private AdminDashboardResponse.RecentActivity toRecentActivity(
                AdminActivity activity) {

        String adminName = "Unknown administrator";

        if (activity.getAdmin() != null) {
                adminName = activity.getAdmin().getFullName();

                if (adminName == null || adminName.isBlank()) {
                adminName = activity.getAdmin().getEmail();
                }
        }

        return new AdminDashboardResponse.RecentActivity(
                activity.getActivityId(),
                adminName,
                activity.getActionType(),
                activity.getDescription(),
                activity.getEntityType(),
                activity.getEntityId(),
                activity.getCreatedAt()
        );
        }

}
