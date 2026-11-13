package guidance_system.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "applicant_status")
public class ApplicantStatus {

    @Id
    @Column(name = "status_code")
    private String statusCode;

    @Column(name = "description")
    private String description;

    public ApplicantStatus() {
    }

    public String getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(String statusCode) {
        this.statusCode = statusCode;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}