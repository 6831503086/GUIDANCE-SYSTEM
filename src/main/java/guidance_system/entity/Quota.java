package guidance_system.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "quota")
public class Quota {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "quota_id")
    private Long quotaId;

    @Column(name = "quota_name", nullable = false)
    private String quotaName;

    @Column(name = "faculty")
    private String faculty;

    public Quota() {
    }

    public Long getQuotaId() {
        return quotaId;
    }

    public void setQuotaId(Long quotaId) {
        this.quotaId = quotaId;
    }

    public String getQuotaName() {
        return quotaName;
    }

    public void setQuotaName(String quotaName) {
        this.quotaName = quotaName;
    }

    public String getFaculty() {
    return faculty;
    }
    
    public void setFaculty(String faculty) {
        this.faculty = faculty;
    }
}