package guidance_system.dto;

import java.util.List;

public class TeacherProgramInterestStatisticsResponse {

    private long numberOfApplicants;
    private long numberOfPrograms;
    private long totalInterests;
    private List<ProgramStatistics> programStatistics;

    public TeacherProgramInterestStatisticsResponse(
            long numberOfApplicants,
            long numberOfPrograms,
            long totalInterests,
            List<ProgramStatistics> programStatistics) {

        this.numberOfApplicants = numberOfApplicants;
        this.numberOfPrograms = numberOfPrograms;
        this.totalInterests = totalInterests;
        this.programStatistics = programStatistics;
    }

    public long getNumberOfApplicants() {
        return numberOfApplicants;
    }

    public long getNumberOfPrograms() {
        return numberOfPrograms;
    }

    public long getTotalInterests() {
        return totalInterests;
    }

    public List<ProgramStatistics> getProgramStatistics() {
        return programStatistics;
    }

    public static class ProgramStatistics {

        private int rank;
        private Long programId;
        private String programName;
        private String faculty;
        private long interestCount;
        private double proportion;

        public ProgramStatistics(
                int rank,
                Long programId,
                String programName,
                String faculty,
                long interestCount,
                double proportion) {

            this.rank = rank;
            this.programId = programId;
            this.programName = programName;
            this.faculty = faculty;
            this.interestCount = interestCount;
            this.proportion = proportion;
        }

        public int getRank() {
            return rank;
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

        public long getInterestCount() {
            return interestCount;
        }

        public double getProportion() {
            return proportion;
        }
    }
}