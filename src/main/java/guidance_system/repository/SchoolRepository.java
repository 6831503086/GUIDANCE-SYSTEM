
package guidance_system.repository;

import guidance_system.entity.School;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface SchoolRepository extends JpaRepository<School, Long> {

    @Query("""
        SELECT s FROM School s
        LEFT JOIN s.subdistrict sd
        LEFT JOIN sd.district d
        LEFT JOIN d.province p
        WHERE
            (:keyword IS NULL OR
                LOWER(s.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR
                LOWER(s.schoolCode) LIKE LOWER(CONCAT('%', :keyword, '%')))
            AND (:province IS NULL OR
                LOWER(p.name) LIKE LOWER(CONCAT('%', :province, '%')))
            AND (:schoolType IS NULL OR
                LOWER(s.schoolType) = LOWER(:schoolType))
        """)
    Page<School> searchSchools(
            @Param("keyword") String keyword,
            @Param("province") String province,
            @Param("schoolType") String schoolType,
            Pageable pageable
    );

    
}
