package guidance_system.repository;

import guidance_system.entity.Prefix;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PrefixRepository extends JpaRepository<Prefix, Long> {
}