
package guidance_system.dto;

import java.time.LocalDate;

public class ProgramResponse {

    private Long programId;
    private String programName;
    private String faculty;
    private String description;
    private Integer academicYear;
    private String round;
    private String status;
    private LocalDate applyStartDate;
    private LocalDate applyEndDate;

    public ProgramResponse() {
    }

    public ProgramResponse(
            Long programId,
            String programName,
            String faculty,
            String description,
            Integer academicYear,
            String round,
            String status,
            LocalDate applyStartDate,
            LocalDate applyEndDate) {

        this.programId = programId;
        this.programName = programName;
        this.faculty = faculty;
        this.description = description;
        this.academicYear = academicYear;
        this.round = round;
        this.status = status;
        this.applyStartDate = applyStartDate;
        this.applyEndDate = applyEndDate;
    }

    public Long getProgramId() {
        return programId;
    }

    public String getProgramName() {
        return programName;
    }

    public String getFaculty() {
        return faculty;
    }

    public String getDescription() {
        return description;
    }

    public Integer getAcademicYear() {
        return academicYear;
    }

    public String getRound() {
        return round;
    }

    public String getStatus() {
        return status;
    }

    public LocalDate getApplyStartDate() {
        return applyStartDate;
    }

    public LocalDate getApplyEndDate() {
        return applyEndDate;
    }
}
