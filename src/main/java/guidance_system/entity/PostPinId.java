package guidance_system.entity;

import java.io.Serializable;
import java.util.Objects;

public class PostPinId implements Serializable {

    private Long post;
    private Long teacher;

    public PostPinId() {
    }

    public PostPinId(Long post, Long teacher) {
        this.post = post;
        this.teacher = teacher;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (!(object instanceof PostPinId)) return false;

        PostPinId that = (PostPinId) object;

        return Objects.equals(post, that.post)
                && Objects.equals(teacher, that.teacher);
    }

    @Override
    public int hashCode() {
        return Objects.hash(post, teacher);
    }
}