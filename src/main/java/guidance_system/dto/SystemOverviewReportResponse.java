
package guidance_system.dto;

import java.util.List;

public class SystemOverviewReportResponse {

    private List<ProgramInterestItem> programInterestRanking;
    private List<ApplicantsBySchoolItem> applicantsBySchool;
    private List<AdmissionTrackItem> admissionTrackProportions;

    public SystemOverviewReportResponse(
            List<ProgramInterestItem> programInterestRanking,
            List<ApplicantsBySchoolItem> applicantsBySchool,
            List<AdmissionTrackItem> admissionTrackProportions) {

        this.programInterestRanking = programInterestRanking;
        this.applicantsBySchool = applicantsBySchool;
        this.admissionTrackProportions = admissionTrackProportions;
    }

    public List<ProgramInterestItem> getProgramInterestRanking() {
        return programInterestRanking;
    }

    public List<ApplicantsBySchoolItem> getApplicantsBySchool() {
        return applicantsBySchool;
    }

    public List<AdmissionTrackItem> getAdmissionTrackProportions() {
        return admissionTrackProportions;
    }

    public static class ProgramInterestItem {

        private int rank;
        private Long programId;
        private String programName;
        private String faculty;
        private String admissionRound;
        private long interestCount;

        public ProgramInterestItem(
                int rank,
                Long programId,
                String programName,
                String faculty,
                String admissionRound,
                long interestCount) {

            this.rank = rank;
            this.programId = programId;
            this.programName = programName;
            this.faculty = faculty;
            this.admissionRound = admissionRound;
            this.interestCount = interestCount;
        }

        public int getRank() { return rank; }
        public Long getProgramId() { return programId; }
        public String getProgramName() { return programName; }
        public String getFaculty() { return faculty; }
        public String getAdmissionRound() { return admissionRound; }
        public long getInterestCount() { return interestCount; }
    }

    public static class ApplicantsBySchoolItem {

        private Long schoolId;
        private String schoolCode;
        private String schoolName;
        private String province;
        private String region;
        private long applicantCount;

        public ApplicantsBySchoolItem(
                Long schoolId,
                String schoolCode,
                String schoolName,
                String province,
                String region,
                long applicantCount) {

            this.schoolId = schoolId;
            this.schoolCode = schoolCode;
            this.schoolName = schoolName;
            this.province = province;
            this.region = region;
            this.applicantCount = applicantCount;
        }

        public Long getSchoolId() { return schoolId; }
        public String getSchoolCode() { return schoolCode; }
        public String getSchoolName() { return schoolName; }
        public String getProvince() { return province; }
        public String getRegion() { return region; }
        public long getApplicantCount() { return applicantCount; }
    }

    public static class AdmissionTrackItem {

        private String admissionTrack;
        private long publishedResultRecords;
        private double proportion;

        public AdmissionTrackItem(
                String admissionTrack,
                long publishedResultRecords,
                double proportion) {

            this.admissionTrack = admissionTrack;
            this.publishedResultRecords = publishedResultRecords;
            this.proportion = proportion;
        }

        public String getAdmissionTrack() { return admissionTrack; }
        public long getPublishedResultRecords() { return publishedResultRecords; }
        public double getProportion() { return proportion; }
    }
}
