package guidance_system.service;

import guidance_system.dto.ResultDatasetSummaryResponse;
import guidance_system.entity.ResultDataset;
import guidance_system.repository.ResultDatasetRepository;

import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class ResultDatasetService {

    private final ResultDatasetRepository repository;

    public ResultDatasetService(ResultDatasetRepository repository) {
        this.repository = repository;
    }

    public List<ResultDataset> searchDatasets(
            Integer academicYear,
            String round,
            String program,
            String status) {

        return repository.searchDatasets(
                academicYear,
                round,
                program,
                status
        );
    }

    public ResultDatasetSummaryResponse getSummary() {

        List<ResultDataset> datasets = repository.findAll();

        long total = datasets.size();

        long published = datasets.stream()
                .filter(d -> "PUBLISHED".equalsIgnoreCase(d.getDatasetStatus()))
                .count();

        long pending = datasets.stream()
                .filter(d -> "PENDING_REVIEW".equalsIgnoreCase(d.getDatasetStatus()))
                .count();

        long erroneous = datasets.stream()
                .mapToLong(d -> d.getErrorRows() == null ? 0 : d.getErrorRows())
                .sum();

        return new ResultDatasetSummaryResponse(
                total,
                published,
                pending,
                erroneous
        );
    }

    public ResultDataset getDatasetDetails(Long id) {
        return repository.findById(id).orElse(null);
    }
}
