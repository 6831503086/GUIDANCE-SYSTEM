package guidance_system.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(
    name = "post_registration",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"post_id", "teacher_id"})
    }
)
public class PostRegistration {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "registration_id")
    private Long registrationId;

    @ManyToOne
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    @ManyToOne
    @JoinColumn(name = "teacher_id", nullable = false)
    private GuidanceTeacher teacher;

    @Column(name = "registered_at")
    private LocalDateTime registeredAt;

    public PostRegistration() {
    }

    public Long getRegistrationId() {
        return registrationId;
    }

    public void setRegistrationId(Long registrationId) {
        this.registrationId = registrationId;
    }

    public Post getPost() {
        return post;
    }

    public void setPost(Post post) {
        this.post = post;
    }

    public GuidanceTeacher getTeacher() {
        return teacher;
    }

    public void setTeacher(GuidanceTeacher teacher) {
        this.teacher = teacher;
    }

    public LocalDateTime getRegisteredAt() {
        return registeredAt;
    }

    public void setRegisteredAt(LocalDateTime registeredAt) {
        this.registeredAt = registeredAt;
    }
}