package guidance_system.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "post_pin")
@IdClass(PostPinId.class)
public class PostPin {

    @Id
    @ManyToOne
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    @Id
    @ManyToOne
    @JoinColumn(name = "teacher_id", nullable = false)
    private GuidanceTeacher teacher;

    @Column(name = "pinned_at")
    private LocalDateTime pinnedAt;

    public PostPin() {
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

    public LocalDateTime getPinnedAt() {
        return pinnedAt;
    }

    public void setPinnedAt(LocalDateTime pinnedAt) {
        this.pinnedAt = pinnedAt;
    }
}