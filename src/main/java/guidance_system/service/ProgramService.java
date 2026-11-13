
package guidance_system.service;

import guidance_system.dto.ProgramResponse;
import guidance_system.entity.Program;
import guidance_system.repository.ProgramRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class ProgramService {

    private final ProgramRepository repository;

    public ProgramService(ProgramRepository repository) {
        this.repository = repository;
    }

    public List<ProgramResponse> searchPrograms(
            String keyword,
            Integer academicYear,
            String round,
            String status) {

        List<Program> programs = repository.searchPrograms(
                keyword == null || keyword.isBlank() ? null : keyword.trim(),
                academicYear,
                round == null || round.isBlank() ? null : round.trim()
        );

        return programs.stream()
                .filter(program -> status == null || status.isBlank()
                        || getProgramStatus(program)
                        .equalsIgnoreCase(status.trim()))
                .map(this::convertToResponse)
                .toList();
    }

    public String getProgramStatus(Program program) {
        if (program.getEntryGroup() == null
                || program.getEntryGroup().getApplyStartDate() == null
                || program.getEntryGroup().getApplyEndDate() == null) {
            return "Unknown";
        }

        LocalDate today = LocalDate.now();
        LocalDate startDate = program.getEntryGroup().getApplyStartDate();
        LocalDate endDate = program.getEntryGroup().getApplyEndDate();

        if (today.isBefore(startDate)) return "Opening Soon";
        if (today.isAfter(endDate)) return "Closed";

        return "Open";
    }

    private ProgramResponse convertToResponse(Program program) {

        return new ProgramResponse(
                program.getProgramId(),
                program.getProgramName(),
                program.getFaculty(),
                program.getDescription(),
                program.getEntryGroup().getAcademicYear(),
                program.getEntryGroup().getEntrygroupName(),
                getProgramStatus(program),
                program.getEntryGroup().getApplyStartDate(),
                program.getEntryGroup().getApplyEndDate()
        );
    }

    public ProgramResponse getProgramById(Long id) {
        return repository.findById(id)
                .map(this::convertToResponse)
                .orElse(null);
    }
}
