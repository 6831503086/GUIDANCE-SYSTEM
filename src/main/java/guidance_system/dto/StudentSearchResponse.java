
package guidance_system.dto;

public class StudentSearchResponse {

    private Long applicantId;
    private String studentName;
    private String studentId;
    private String schoolName;
    private String status;
    private String statusDescription;

    public StudentSearchResponse() {
    }

    public StudentSearchResponse(
            Long applicantId,
            String studentName,
            String studentId,
            String schoolName,
            String status,
            String statusDescription) {

        this.applicantId = applicantId;
        this.studentName = studentName;
        this.studentId = studentId;
        this.schoolName = schoolName;
        this.status = status;
        this.statusDescription = statusDescription;
    }

    public Long getApplicantId() {
        return applicantId;
    }

    public String getStudentName() {
        return studentName;
    }

    public String getStudentId() {
        return studentId;
    }

    public String getSchoolName() {
        return schoolName;
    }

    public String getStatus() {
        return status;
    }

    public String getStatusDescription() {
        return statusDescription;
    }
}
