
package guidance_system.repository;

import guidance_system.entity.ResultDataset;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ResultDatasetRepository extends JpaRepository<ResultDataset, Long> {

    @Query("""
        SELECT d FROM ResultDataset d
        WHERE (:academicYear IS NULL
            OR d.program.entryGroup.academicYear = :academicYear)
        AND (:round IS NULL
            OR LOWER(d.program.entryGroup.entrygroupName) = LOWER(:round))
        AND (:program IS NULL
            OR LOWER(d.program.programName) LIKE LOWER(CONCAT('%', :program, '%')))
        AND (:status IS NULL
            OR LOWER(d.datasetStatus) = LOWER(:status))
        ORDER BY d.uploadedAt DESC
    """)
    List<ResultDataset> searchDatasets(
            @Param("academicYear") Integer academicYear,
            @Param("round") String round,
            @Param("program") String program,
            @Param("status") String status
    );

    List<ResultDataset> findByDatasetStatusIgnoreCase(String datasetStatus);    
}
