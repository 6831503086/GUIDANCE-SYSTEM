package guidance_system.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(
    name = "program_interest",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"applicant_id", "program_id"})
    }
)
public class ProgramInterest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "interest_id")
    private Long interestId;

    @ManyToOne
    @JoinColumn(name = "applicant_id", nullable = false)
    private Applicant applicant;

    @ManyToOne
    @JoinColumn(name = "program_id", nullable = false)
    private Program program;

    @Column(name = "interested_at")
    private LocalDateTime interestedAt;

    public ProgramInterest() {}

    public Long getInterestId() {
        return interestId;
    }

    public void setInterestId(Long interestId) {
        this.interestId = interestId;
    }

    public Applicant getApplicant() {
        return applicant;
    }

    public void setApplicant(Applicant applicant) {
        this.applicant = applicant;
    }

    public Program getProgram() {
        return program;
    }

    public void setProgram(Program program) {
        this.program = program;
    }

    public LocalDateTime getInterestedAt() {
        return interestedAt;
    }

    public void setInterestedAt(LocalDateTime interestedAt) {
        this.interestedAt = interestedAt;
    }
}