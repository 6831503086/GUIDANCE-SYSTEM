package guidance_system.dto;

import java.util.List;

public class ProgramInterestAnalysisResponse {

    private List<ProgramRanking> rankings;
    private List<DailyTrend> dailyTrends;
    private long totalInterests;
    private String analysisSummary;

    public ProgramInterestAnalysisResponse(
            List<ProgramRanking> rankings,
            List<DailyTrend> dailyTrends,
            long totalInterests,
            String analysisSummary) {
        this.rankings = rankings;
        this.dailyTrends = dailyTrends;
        this.totalInterests = totalInterests;
        this.analysisSummary = analysisSummary;
    }

    public List<ProgramRanking> getRankings() {
        return rankings;
    }

    public List<DailyTrend> getDailyTrends() {
        return dailyTrends;
    }

    public long getTotalInterests() {
        return totalInterests;
    }

    public String getAnalysisSummary() {
        return analysisSummary;
    }

    public static class ProgramRanking {

        private Long programId;
        private String programName;
        private String faculty;
        private long interestCount;
        private double proportion;
        private int rank;

        public ProgramRanking(
                Long programId,
                String programName,
                String faculty,
                long interestCount,
                double proportion,
                int rank) {
            this.programId = programId;
            this.programName = programName;
            this.faculty = faculty;
            this.interestCount = interestCount;
            this.proportion = proportion;
            this.rank = rank;
        }

        public Long getProgramId() { return programId; }
        public String getProgramName() { return programName; }
        public String getFaculty() { return faculty; }
        public long getInterestCount() { return interestCount; }
        public double getProportion() { return proportion; }
        public int getRank() { return rank; }
    }

    public static class DailyTrend {

        private String date;
        private long interestCount;

        public DailyTrend(String date, long interestCount) {
            this.date = date;
            this.interestCount = interestCount;
        }

        public String getDate() { return date; }
        public long getInterestCount() { return interestCount; }
    }
}