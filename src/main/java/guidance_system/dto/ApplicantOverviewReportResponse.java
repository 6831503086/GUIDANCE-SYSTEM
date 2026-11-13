package guidance_system.dto;

import java.util.List;

public class ApplicantOverviewReportResponse {

    private long totalApplicants;
    private long eligibleForExamination;
    private long passedSelection;
    private long rightsConfirmed;
    private long rightsWaived;
    private long enrolled;

    private List<ApplicantRecord> applicants;

    public ApplicantOverviewReportResponse() {
    }

    public ApplicantOverviewReportResponse(
            long totalApplicants,
            long eligibleForExamination,
            long passedSelection,
            long rightsConfirmed,
            long rightsWaived,
            long enrolled,
            List<ApplicantRecord> applicants) {

        this.totalApplicants = totalApplicants;
        this.eligibleForExamination = eligibleForExamination;
        this.passedSelection = passedSelection;
        this.rightsConfirmed = rightsConfirmed;
        this.rightsWaived = rightsWaived;
        this.enrolled = enrolled;
        this.applicants = applicants;
    }

    public long getTotalApplicants() {
        return totalApplicants;
    }

    public long getEligibleForExamination() {
        return eligibleForExamination;
    }

    public long getPassedSelection() {
        return passedSelection;
    }

    public long getRightsConfirmed() {
        return rightsConfirmed;
    }

    public long getRightsWaived() {
        return rightsWaived;
    }

    public long getEnrolled() {
        return enrolled;
    }

    public List<ApplicantRecord> getApplicants() {
        return applicants;
    }

    public static class ApplicantRecord {

        private Long applicantId;
        private String studentId;
        private String fullName;
        private String schoolName;
        private Integer semester;
        private Integer academicYear;
        private String statusCode;
        private String statusDescription;

        public ApplicantRecord() {
        }

        public ApplicantRecord(
                Long applicantId,
                String studentId,
                String fullName,
                String schoolName,
                Integer semester,
                Integer academicYear,
                String statusCode,
                String statusDescription) {

            this.applicantId = applicantId;
            this.studentId = studentId;
            this.fullName = fullName;
            this.schoolName = schoolName;
            this.semester = semester;
            this.academicYear = academicYear;
            this.statusCode = statusCode;
            this.statusDescription = statusDescription;
        }

        public Long getApplicantId() {
            return applicantId;
        }

        public String getStudentId() {
            return studentId;
        }

        public String getFullName() {
            return fullName;
        }

        public String getSchoolName() {
            return schoolName;
        }

        public Integer getSemester() {
            return semester;
        }

        public Integer getAcademicYear() {
            return academicYear;
        }

        public String getStatusCode() {
            return statusCode;
        }

        public String getStatusDescription() {
            return statusDescription;
        }
    }
}