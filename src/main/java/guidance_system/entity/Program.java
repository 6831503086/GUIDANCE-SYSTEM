package guidance_system.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "program")
public class Program {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    
    @Column(name = "program_id")
    private Long programId;

    @ManyToOne
    @JoinColumn(name = "entrygroup_id", nullable = false)
    private EntryGroup entryGroup;

    @Column(name = "program_name", nullable = false, length = 255)
    private String programName;

    @Column(name = "faculty", length = 255)
    private String faculty;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    public Program() {
    }

    public Long getProgramId() {
        return programId;
    }

    public void setProgramId(Long programId) {
        this.programId = programId;
    }

    public EntryGroup getEntryGroup() {
        return entryGroup;
    }

    public void setEntryGroup(EntryGroup entryGroup) {
        this.entryGroup = entryGroup;
    }

    public String getProgramName() {
        return programName;
    }

    public void setProgramName(String programName) {
        this.programName = programName;
    }

    public String getFaculty() {
        return faculty;
    }

    public void setFaculty(String faculty) {
        this.faculty = faculty;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
