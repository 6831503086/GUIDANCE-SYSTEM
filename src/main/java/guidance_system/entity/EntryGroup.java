package guidance_system.entity;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(
    name = "entrygroup",
    uniqueConstraints = 
    {
        @UniqueConstraint
        (
            columnNames = {"academic_year", "entrygroup_name"}
        )
    }
)
public class EntryGroup {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    
    @Column(name = "entrygroup_id")
    private Long entrygroupId;

    @Column(name = "academic_year", nullable = false)
    private Integer academicYear;

    @Column(name = "entrygroup_name", nullable = false, length = 100)
    private String entrygroupName;

    @Column(name = "apply_start_date")
    private LocalDate applyStartDate;

    @Column(name = "apply_end_date")
    private LocalDate applyEndDate;

    @Column(name = "result_announce_date")
    private LocalDate resultAnnounceDate;

    @Column(name = "rights_confirm_date")
    private LocalDate rightsConfirmDate;

    public EntryGroup() {
    }

    public Long getEntrygroupId() {
        return entrygroupId;
    }

    public void setEntrygroupId(Long entrygroupId) {
        this.entrygroupId = entrygroupId;
    }

    public Integer getAcademicYear() {
        return academicYear;
    }

    public void setAcademicYear(Integer academicYear) {
        this.academicYear = academicYear;
    }

    public String getEntrygroupName() {
        return entrygroupName;
    }

    public void setEntrygroupName(String entrygroupName) {
        this.entrygroupName = entrygroupName;
    }

    public LocalDate getApplyStartDate() {
        return applyStartDate;
    }

    public void setApplyStartDate(LocalDate applyStartDate) {
        this.applyStartDate = applyStartDate;
    }

    public LocalDate getApplyEndDate() {
        return applyEndDate;
    }

    public void setApplyEndDate(LocalDate applyEndDate) {
        this.applyEndDate = applyEndDate;
    }

    public LocalDate getResultAnnounceDate() {
        return resultAnnounceDate;
    }

    public void setResultAnnounceDate(LocalDate resultAnnounceDate) {
        this.resultAnnounceDate = resultAnnounceDate;
    }

    public LocalDate getRightsConfirmDate() {
        return rightsConfirmDate;
    }

    public void setRightsConfirmDate(LocalDate rightsConfirmDate) {
        this.rightsConfirmDate = rightsConfirmDate;
    }
}
