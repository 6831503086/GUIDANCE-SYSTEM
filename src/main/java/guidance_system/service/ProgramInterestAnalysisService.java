
package guidance_system.service;

import guidance_system.dto.ProgramInterestAnalysisResponse;
import guidance_system.dto.ProgramInterestAnalysisResponse.ProgramRanking;
import guidance_system.dto.ProgramInterestAnalysisResponse.DailyTrend;
import guidance_system.repository.ProgramInterestRepository;

import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class ProgramInterestAnalysisService {

    private final ProgramInterestRepository repository;

    public ProgramInterestAnalysisService(
            ProgramInterestRepository repository) {
        this.repository = repository;
    }

    public ProgramInterestAnalysisResponse analyze(
            LocalDate startDate,
            LocalDate endDate,
            Long schoolId,
            String faculty,
            String rankingType) {

        if (startDate == null || endDate == null) {
            throw new IllegalArgumentException(
                    "Start date and end date are required.");
        }

        if (endDate.isBefore(startDate)) {
            throw new IllegalArgumentException(
                    "End date cannot be before start date.");
        }

        String type = rankingType == null
                ? "COUNT"
                : rankingType.trim().toUpperCase();

        if (!type.equals("COUNT") && !type.equals("PROPORTION")) {
            throw new IllegalArgumentException(
                    "Ranking type must be COUNT or PROPORTION.");
        }

        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.plusDays(1).atStartOfDay();

        List<Object[]> rankingRows =
                repository.getProgramInterestRanking(
                        start, end, schoolId, faculty);

        List<Object[]> trendRows =
                repository.getDailyInterestTrend(
                        start, end, schoolId, faculty);

        long total = rankingRows.stream()
                .mapToLong(row -> ((Number) row[3]).longValue())
                .sum();

        List<ProgramRanking> rankings = new ArrayList<>();

        for (Object[] row : rankingRows) {
            long count = ((Number) row[3]).longValue();

            double proportion = total == 0
                    ? 0
                    : (count * 100.0) / total;

            rankings.add(new ProgramRanking(
                    ((Number) row[0]).longValue(),
                    (String) row[1],
                    (String) row[2],
                    count,
                    Math.round(proportion * 100.0) / 100.0,
                    0
            ));
        }

        if (type.equals("COUNT")) {
            rankings.sort(Comparator.comparingLong(
                    ProgramRanking::getInterestCount).reversed());
        } else {
            rankings.sort(Comparator.comparingDouble(
                    ProgramRanking::getProportion).reversed());
        }

        List<ProgramRanking> rankedResults = new ArrayList<>();
        for (int i = 0; i < rankings.size(); i++) {
            ProgramRanking item = rankings.get(i);
            rankedResults.add(new ProgramRanking(
                    item.getProgramId(),
                    item.getProgramName(),
                    item.getFaculty(),
                    item.getInterestCount(),
                    item.getProportion(),
                    i + 1
            ));
        }

        List<DailyTrend> dailyTrends = new ArrayList<>();

        for (Object[] row : trendRows) {
            dailyTrends.add(new DailyTrend(
                    row[0].toString(),
                    ((Number) row[1]).longValue()
            ));
        }

        String summary = total == 0
                ? "No data found for the selected period."
                : "A total of " + total
                  + " program interests were recorded. "
                  + "The highest-ranked program is "
                  + rankedResults.get(0).getProgramName() + ".";

        return new ProgramInterestAnalysisResponse(
                rankedResults, dailyTrends, total, summary);
    }
}
