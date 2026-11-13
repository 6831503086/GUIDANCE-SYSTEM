package guidance_system.repository;

import guidance_system.entity.PostRegistration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface PostRegistrationRepository
        extends JpaRepository<PostRegistration, Long> {

    List<PostRegistration> findByTeacherTeacherId(Long teacherId);

    List<PostRegistration> findByPostPostId(Long postId);

    boolean existsByPostPostIdAndTeacherTeacherId(Long postId, Long teacherId);

    void deleteByPostPostIdAndTeacherTeacherId(Long postId, Long teacherId);

    @Transactional
    void deleteByTeacher_TeacherId(Long teacherId);
}