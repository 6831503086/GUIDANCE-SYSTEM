package guidance_system.repository;

import guidance_system.entity.EntryGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EntryGroupRepository extends JpaRepository<EntryGroup, Long> {
}
