package guidance_system.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "post")
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "post_id")
    private Long postId;

    @ManyToOne
    @JoinColumn(name = "admin_id", nullable = false)
    private Admin admin;

    @ManyToOne
    @JoinColumn(name = "subdistrict_id")
    private Subdistrict subdistrict;

    @Column(name = "building_name", length = 150)
    private String buildingName;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "summary", columnDefinition = "TEXT")
    private String summary;

    @Column(name = "content", columnDefinition = "TEXT")
    private String content;

    @Column(name = "target_audience")
    private String targetAudience;

    @Column(name = "event_type")
    private String eventType;

    @Column(name = "event_start_date")
    private LocalDate eventStartDate;

    @Column(name = "event_end_date")
    private LocalDate eventEndDate;

    @Column(name = "academic_year")
    private Integer academicYear;

    @Column(name = "application_round", length = 100)
    private String applicationRound;

    @Column(name = "visible_start_date")
    private LocalDate visibleStartDate;

    @Column(name = "visible_end_date")
    private LocalDate visibleEndDate;

    @Column(name = "post_type", length = 20)
    private String postType;

    @Column(name = "published_at")
    private LocalDateTime publishedAt;

    @Column(name = "is_hidden")
    private Boolean isHidden = false;

    @Column(name = "auto_hidden")
    private Boolean autoHidden = false;

    public Post() {}

    public Long getPostId() {
        return postId;
    }

    public void setPostId(Long postId) {
        this.postId = postId;
    }

    public Admin getAdmin() {
        return admin;
    }

    public void setAdmin(Admin admin) {
        this.admin = admin;
    }

    public Subdistrict getSubdistrict() {
    return subdistrict;
    }

    public void setSubdistrict(Subdistrict subdistrict) {
        this.subdistrict = subdistrict;
    }

    public String getBuildingName() {
        return buildingName;
    }

    public void setBuildingName(String buildingName) {
        this.buildingName = buildingName;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getTargetAudience() {
        return targetAudience;
    }

    public void setTargetAudience(String targetAudience) {
        this.targetAudience = targetAudience;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public LocalDate getEventStartDate() {
        return eventStartDate;
    }

    public void setEventStartDate(LocalDate eventStartDate) {
        this.eventStartDate = eventStartDate;
    }

    public LocalDate getEventEndDate() {
        return eventEndDate;
    }

    public void setEventEndDate(LocalDate eventEndDate) {
        this.eventEndDate = eventEndDate;
    }

    public LocalDate getVisibleStartDate() {
        return visibleStartDate;
    }

    public void setVisibleStartDate(LocalDate visibleStartDate) {
        this.visibleStartDate = visibleStartDate;
    }

    public LocalDate getVisibleEndDate() {
        return visibleEndDate;
    }

    public void setVisibleEndDate(LocalDate visibleEndDate) {
        this.visibleEndDate = visibleEndDate;
    }

    public String getPostType() {
        return postType;
    }

    public void setPostType(String postType) {
        this.postType = postType;
    }

    public LocalDateTime getPublishedAt() {
        return publishedAt;
    }

    public void setPublishedAt(LocalDateTime publishedAt) {
        this.publishedAt = publishedAt;
    }

    public Boolean getIsHidden() {
        return isHidden;
    }

    public void setIsHidden(Boolean isHidden) {
        this.isHidden = isHidden;
    }

    public Boolean getAutoHidden() {
        return autoHidden;
    }

    public void setAutoHidden(Boolean autoHidden) {
        this.autoHidden = autoHidden;
    }

    public Integer getAcademicYear() {
        return academicYear;
    }

    public void setAcademicYear(Integer academicYear) {
        this.academicYear = academicYear;
    }

    public String getApplicationRound() {
        return applicationRound;
    }

    public void setApplicationRound(String applicationRound) {
        this.applicationRound = applicationRound;
    }

}