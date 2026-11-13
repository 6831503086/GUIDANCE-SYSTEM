package guidance_system.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public record AdminDashboardResponse(
        LocalDateTime periodStart,
        LocalDateTime periodEnd,
        Integer academicYear,
        String status,
        AnnouncementStats announcements,
        ApplicantStats applicants,
        SelectionStats selectionResults,
        ResultDatasetStats resultDatasets,
        PendingTaskStats pendingTasks,
        List<RecentActivity> recentActivities
) {
    public record AnnouncementStats(
        long total,
        long published,
        long drafts,
        long hidden,
        long expired,
        Map<String, Long> contentByType
     ) {}

    public record ApplicantStats(
            long total,
            Integer academicYear
    ) {}

    public record SelectionStats(
            long total,
            long published,
            long unpublished,
            Map<String, Long> byStatus
    ) {}

    public record ResultDatasetStats(
            long total,
            long published,
            long pendingReview,
            long erroneous
    ) {}

    public record PendingTaskStats(
            long total,
            long pendingReviewDatasets,
            long draftAnnouncements,
            List<PendingTaskItem> items
    ) {}

    public record PendingTaskItem(
            String taskType,
            Long entityId,
            String title,
            String status,
            String description
    ) {}

    public record RecentActivity(
            Long activityId,
            String adminName,
            String actionType,
            String description,
            String entityType,
            Long entityId,
            LocalDateTime createdAt
    ) {}
}