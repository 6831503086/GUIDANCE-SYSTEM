
package guidance_system.dto;

import java.util.List;

public class SchoolOverviewResponse {

    private long totalSchools;
    private long totalRegisteredTeachers;
    private long schoolsWithApplicants;

    private List<SchoolItem> schools;

    private int currentPage;
    private int totalPages;
    private long totalItems;

    public SchoolOverviewResponse(
            long totalSchools,
            long totalRegisteredTeachers,
            long schoolsWithApplicants,
            List<SchoolItem> schools,
            int currentPage,
            int totalPages,
            long totalItems) {

        this.totalSchools = totalSchools;
        this.totalRegisteredTeachers = totalRegisteredTeachers;
        this.schoolsWithApplicants = schoolsWithApplicants;
        this.schools = schools;
        this.currentPage = currentPage;
        this.totalPages = totalPages;
        this.totalItems = totalItems;
    }

    public long getTotalSchools() {
        return totalSchools;
    }

    public long getTotalRegisteredTeachers() {
        return totalRegisteredTeachers;
    }

    public long getSchoolsWithApplicants() {
        return schoolsWithApplicants;
    }

    public List<SchoolItem> getSchools() {
        return schools;
    }

    public int getCurrentPage() {
        return currentPage;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public long getTotalItems() {
        return totalItems;
    }

    public static class SchoolItem {

        private Long schoolId;
        private String schoolCode;
        private String schoolName;
        private String province;
        private String region;
        private String schoolType;
        private long teacherCount;
        private long applicantCount;
        private String status;

        public SchoolItem(
                Long schoolId,
                String schoolCode,
                String schoolName,
                String province,
                String region,
                String schoolType,
                long teacherCount,
                long applicantCount,
                String status) {

            this.schoolId = schoolId;
            this.schoolCode = schoolCode;
            this.schoolName = schoolName;
            this.province = province;
            this.region = region;
            this.schoolType = schoolType;
            this.teacherCount = teacherCount;
            this.applicantCount = applicantCount;
            this.status = status;
        }

        public Long getSchoolId() {
            return schoolId;
        }

        public String getSchoolCode() {
            return schoolCode;
        }

        public String getSchoolName() {
            return schoolName;
        }

        public String getProvince() {
            return province;
        }

        public String getRegion() {
            return region;
        }

        public String getSchoolType() {
            return schoolType;
        }

        public long getTeacherCount() {
            return teacherCount;
        }

        public long getApplicantCount() {
            return applicantCount;
        }

        public String getStatus() {
            return status;
        }
    }
}
