
package guidance_system.service;

import guidance_system.dto.SchoolOverviewResponse;
import guidance_system.dto.SchoolOverviewResponse.SchoolItem;
import guidance_system.entity.GuidanceTeacher;
import guidance_system.entity.School;
import guidance_system.repository.ApplicantRepository;
import guidance_system.repository.GuidanceTeacherRepository;
import guidance_system.repository.SchoolRepository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SchoolOverviewService {

    private final SchoolRepository schoolRepository;
    private final GuidanceTeacherRepository teacherRepository;
    private final ApplicantRepository applicantRepository;

    public SchoolOverviewService(
            SchoolRepository schoolRepository,
            GuidanceTeacherRepository teacherRepository,
            ApplicantRepository applicantRepository) {

        this.schoolRepository = schoolRepository;
        this.teacherRepository = teacherRepository;
        this.applicantRepository = applicantRepository;
    }

    public SchoolOverviewResponse getOverview(
            String keyword,
            String province,
            String schoolType,
            int page,
            int size) {

        if (page < 0) {
            throw new IllegalArgumentException("Page must be zero or greater.");
        }

        if (size < 1 || size > 100) {
            throw new IllegalArgumentException("Page size must be between 1 and 100.");
        }

        keyword = clean(keyword);
        province = clean(province);
        schoolType = clean(schoolType);

        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(Sort.Direction.ASC, "name")
        );

        Page<School> schoolPage = schoolRepository.searchSchools(
                keyword,
                province,
                schoolType,
                pageable
        );

        List<SchoolItem> schoolItems = schoolPage.getContent()
                .stream()
                .map(this::toSchoolItem)
                .toList();

        return new SchoolOverviewResponse(
                schoolRepository.count(),
                teacherRepository.count(),
                applicantRepository.countDistinctSchoolsWithApplicants(),
                schoolItems,
                schoolPage.getNumber(),
                schoolPage.getTotalPages(),
                schoolPage.getTotalElements()
        );
    }

    private SchoolItem toSchoolItem(School school) {

        Long schoolId = school.getSchoolId();

        long teacherCount =
                teacherRepository.countBySchool_SchoolId(schoolId);

        long applicantCount =
                applicantRepository.countBySchool_SchoolId(schoolId);

        GuidanceTeacher teacher =
                teacherRepository.findBySchool_SchoolId(schoolId)
                        .orElse(null);

        String status = "Pending";

        if (teacher != null
                && Boolean.TRUE.equals(teacher.getIsEmailVerified())
                && Boolean.TRUE.equals(teacher.getPdpaConsent())) {
            status = "Active";
        }

        String provinceName = null;
        String region = null;

        if (school.getSubdistrict() != null
                && school.getSubdistrict().getDistrict() != null
                && school.getSubdistrict().getDistrict().getProvince() != null) {

            provinceName = school.getSubdistrict()
                    .getDistrict()
                    .getProvince()
                    .getName();

            region = school.getSubdistrict()
                    .getDistrict()
                    .getProvince()
                    .getRegion();
        }

        return new SchoolItem(
                schoolId,
                school.getSchoolCode(),
                school.getName(),
                provinceName,
                region,
                school.getSchoolType(),
                teacherCount,
                applicantCount,
                status
        );
    }

    private String clean(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
