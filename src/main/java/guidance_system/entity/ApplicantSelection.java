package guidance_system.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(
    name = "applicant_selection",
    uniqueConstraints = {
        @UniqueConstraint(
            columnNames = {"applicant_id", "entrygroup_id"}
        )
    }
)
public class ApplicantSelection {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "application_id")
    private Long applicationId;

    @ManyToOne
    @JoinColumn(name = "applicant_id", nullable = false)
    private Applicant applicant;

    @ManyToOne
    @JoinColumn(name = "entrygroup_id", nullable = false)
    private EntryGroup entryGroup;

    @ManyToOne
    @JoinColumn(name = "quota_id", nullable = false)
    private Quota quota;

    @ManyToOne
    @JoinColumn(name = "status_code", nullable = false)
    private ApplicantStatus status;

    @Column(name = "applied_at")
    private LocalDateTime appliedAt;

    @ManyToOne
    @JoinColumn(name = "admin_id")
    private Admin admin;

    @Column(name = "published_at")
    private LocalDateTime publishedAt;

    public ApplicantSelection() {
    }

    public Long getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(Long applicationId) {
        this.applicationId = applicationId;
    }

    public Applicant getApplicant() {
        return applicant;
    }

    public void setApplicant(Applicant applicant) {
        this.applicant = applicant;
    }

    public EntryGroup getEntryGroup() {
        return entryGroup;
    }

    public void setEntryGroup(EntryGroup entryGroup) {
        this.entryGroup = entryGroup;
    }

    public Quota getQuota() {
        return quota;
    }

    public void setQuota(Quota quota) {
        this.quota = quota;
    }

    public ApplicantStatus getStatus() {
        return status;
    }

    public void setStatus(ApplicantStatus status) {
        this.status = status;
    }

    public LocalDateTime getAppliedAt() {
        return appliedAt;
    }

    public void setAppliedAt(LocalDateTime appliedAt) {
        this.appliedAt = appliedAt;
    }

    public Admin getAdmin() {
        return admin;
    }

    public void setAdmin(Admin admin) {
        this.admin = admin;
    }

    public LocalDateTime getPublishedAt() {
        return publishedAt;
    }

    public void setPublishedAt(LocalDateTime publishedAt) {
        this.publishedAt = publishedAt;
    }
}