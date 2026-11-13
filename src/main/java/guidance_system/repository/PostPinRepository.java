
package guidance_system.repository;

import guidance_system.entity.PostPin;
import guidance_system.entity.PostPinId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface PostPinRepository extends JpaRepository<PostPin, PostPinId> {

    List<PostPin> findByTeacherTeacherIdOrderByPinnedAtDesc(Long teacherId);

    List<PostPin> findByTeacherTeacherId(Long teacherId);

    boolean existsByPostPostIdAndTeacherTeacherId(
            Long postId, Long teacherId);

    void deleteByPostPostIdAndTeacherTeacherId(
            Long postId, Long teacherId);

    // Delete all pinned-post records belonging to a teacher
    @Transactional
    void deleteByTeacherTeacherId(Long teacherId);
}
