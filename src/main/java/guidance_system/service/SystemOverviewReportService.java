
package guidance_system.service;

import guidance_system.dto.SystemOverviewReportResponse;
import guidance_system.dto.SystemOverviewReportResponse.ProgramInterestItem;
import guidance_system.dto.SystemOverviewReportResponse.ApplicantsBySchoolItem;
import guidance_system.dto.SystemOverviewReportResponse.AdmissionTrackItem;

import guidance_system.entity.Applicant;
import guidance_system.entity.ProgramInterest;
import guidance_system.entity.ResultDataset;
import guidance_system.entity.School;

import guidance_system.repository.ApplicantRepository;
import guidance_system.repository.ProgramInterestRepository;
import guidance_system.repository.ResultDatasetRepository;

import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class SystemOverviewReportService {

    private final ApplicantRepository applicantRepository;
    private final ProgramInterestRepository programInterestRepository;
    private final ResultDatasetRepository resultDatasetRepository;

    public SystemOverviewReportService(
            ApplicantRepository applicantRepository,
            ProgramInterestRepository programInterestRepository,
            ResultDatasetRepository resultDatasetRepository) {

        this.applicantRepository = applicantRepository;
        this.programInterestRepository = programInterestRepository;
        this.resultDatasetRepository = resultDatasetRepository;
    }

    public SystemOverviewReportResponse generateReport(
            Integer academicYear,
            String round,
            String region) {

        round = clean(round);
        region = clean(region);

        List<ProgramInterestItem> interestRanking =
                getProgramInterestRanking(academicYear, round, region);

        List<ApplicantsBySchoolItem> applicantsBySchool =
                getApplicantsBySchool(academicYear, region);

        List<AdmissionTrackItem> admissionTracks =
                getAdmissionTrackProportions(academicYear, round);

        return new SystemOverviewReportResponse(
                interestRanking,
                applicantsBySchool,
                admissionTracks
        );
    }

    private List<ProgramInterestItem> getProgramInterestRanking(
            Integer academicYear,
            String round,
            String region) {

        Map<Long, ProgramInterestItem> rankingMap = new HashMap<>();

        List<ProgramInterest> interests =
                programInterestRepository.findAll();

        Map<Long, Long> counts = interests.stream()
                .filter(pi -> pi.getApplicant() != null)
                .filter(pi -> pi.getProgram() != null)
                .filter(pi -> academicYear == null
                        || Objects.equals(
                                pi.getApplicant().getAcademicYear(),
                                academicYear))
                .filter(pi -> round == null
                        || pi.getProgram().getEntryGroup() != null
                        && pi.getProgram().getEntryGroup().getEntrygroupName()
                        .equalsIgnoreCase(round))
                .filter(pi -> region == null
                        || matchesRegion(
                                pi.getApplicant().getSchool(), region))
                .collect(Collectors.groupingBy(
                        pi -> pi.getProgram().getProgramId(),
                        Collectors.counting()
                ));

        List<ProgramInterestItem> result = new ArrayList<>();

        for (ProgramInterest pi : interests) {

            if (pi.getProgram() == null
                    || pi.getProgram().getEntryGroup() == null) {
                continue;
            }

            Long programId = pi.getProgram().getProgramId();

            if (rankingMap.containsKey(programId)) {
                continue;
            }

            long count = counts.getOrDefault(programId, 0L);

            if (count == 0) {
                continue;
            }

            rankingMap.put(programId, new ProgramInterestItem(
                    0,
                    programId,
                    pi.getProgram().getProgramName(),
                    pi.getProgram().getFaculty(),
                    pi.getProgram().getEntryGroup().getEntrygroupName(),
                    count
            ));
        }

        result.addAll(rankingMap.values());

        result.sort(
                Comparator.comparingLong(
                        ProgramInterestItem::getInterestCount
                ).reversed()
        );

        List<ProgramInterestItem> ranked = new ArrayList<>();

        for (int i = 0; i < result.size(); i++) {
            ProgramInterestItem item = result.get(i);

            ranked.add(new ProgramInterestItem(
                    i + 1,
                    item.getProgramId(),
                    item.getProgramName(),
                    item.getFaculty(),
                    item.getAdmissionRound(),
                    item.getInterestCount()
            ));
        }

        return ranked;
    }

    private List<ApplicantsBySchoolItem> getApplicantsBySchool(
            Integer academicYear,
            String region) {

        Map<Long, List<Applicant>> grouped = applicantRepository.findAll()
                .stream()
                .filter(a -> academicYear == null
                        || Objects.equals(a.getAcademicYear(), academicYear))
                .filter(a -> a.getSchool() != null)
                .filter(a -> region == null
                        || matchesRegion(a.getSchool(), region))
                .collect(Collectors.groupingBy(
                        a -> a.getSchool().getSchoolId()
                ));

        List<ApplicantsBySchoolItem> result = new ArrayList<>();

        for (List<Applicant> schoolApplicants : grouped.values()) {

            School school = schoolApplicants.get(0).getSchool();

            String province = null;
            String schoolRegion = null;

            if (school.getSubdistrict() != null
                    && school.getSubdistrict().getDistrict() != null
                    && school.getSubdistrict().getDistrict().getProvince() != null) {

                province = school.getSubdistrict()
                        .getDistrict()
                        .getProvince()
                        .getName();

                schoolRegion = school.getSubdistrict()
                        .getDistrict()
                        .getProvince()
                        .getRegion();
            }

            result.add(new ApplicantsBySchoolItem(
                    school.getSchoolId(),
                    school.getSchoolCode(),
                    school.getName(),
                    province,
                    schoolRegion,
                    schoolApplicants.size()
            ));
        }

        result.sort(
                Comparator.comparingLong(
                        ApplicantsBySchoolItem::getApplicantCount
                ).reversed()
        );

        return result;
    }

    private List<AdmissionTrackItem> getAdmissionTrackProportions(
            Integer academicYear,
            String round) {

        List<ResultDataset> datasets =
                resultDatasetRepository.findByDatasetStatusIgnoreCase("PUBLISHED");

        // Keep only the latest published dataset for each program.
        Map<Long, ResultDataset> latestByProgram = new HashMap<>();

        for (ResultDataset dataset : datasets) {

            if (dataset.getProgram() == null
                    || dataset.getProgram().getEntryGroup() == null) {
                continue;
            }

            if (academicYear != null
                    && !Objects.equals(
                            dataset.getProgram().getEntryGroup().getAcademicYear(),
                            academicYear)) {
                continue;
            }

            if (round != null
                    && !dataset.getProgram().getEntryGroup()
                    .getEntrygroupName().equalsIgnoreCase(round)) {
                continue;
            }

            Long programId = dataset.getProgram().getProgramId();

            ResultDataset existing = latestByProgram.get(programId);

            if (existing == null
                    || dataset.getUploadedAt() != null
                    && (existing.getUploadedAt() == null
                    || dataset.getUploadedAt().isAfter(existing.getUploadedAt()))) {

                latestByProgram.put(programId, dataset);
            }
        }

        Map<String, Long> trackCounts = new HashMap<>();

        for (ResultDataset dataset : latestByProgram.values()) {

            String track = dataset.getProgram()
                    .getEntryGroup().getEntrygroupName();

            long rows = dataset.getTotalRows() == null
                    ? 0L
                    : dataset.getTotalRows();

            trackCounts.merge(track, rows, Long::sum);
        }

        long totalRecords = trackCounts.values()
                .stream()
                .mapToLong(Long::longValue)
                .sum();

        List<AdmissionTrackItem> result = new ArrayList<>();

        for (Map.Entry<String, Long> entry : trackCounts.entrySet()) {

            double proportion = totalRecords == 0
                    ? 0.0
                    : Math.round(
                            (entry.getValue() * 10000.0) / totalRecords
                    ) / 100.0;

            result.add(new AdmissionTrackItem(
                    entry.getKey(),
                    entry.getValue(),
                    proportion
            ));
        }

        result.sort(
                Comparator.comparingLong(
                        AdmissionTrackItem::getPublishedResultRecords
                ).reversed()
        );

        return result;
    }

    private boolean matchesRegion(School school, String region) {

        if (school == null
                || school.getSubdistrict() == null
                || school.getSubdistrict().getDistrict() == null
                || school.getSubdistrict().getDistrict().getProvince() == null) {
            return false;
        }

        String schoolRegion = school.getSubdistrict()
                .getDistrict()
                .getProvince()
                .getRegion();

        return schoolRegion != null
                && schoolRegion.equalsIgnoreCase(region);
    }

    private String clean(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        return value.trim();
    }
}
