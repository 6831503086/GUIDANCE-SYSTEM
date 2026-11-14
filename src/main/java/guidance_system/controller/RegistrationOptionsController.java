package guidance_system.controller;

import guidance_system.repository.GuidanceTeacherRepository;
import guidance_system.repository.PrefixRepository;
import guidance_system.repository.SchoolRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

/** Minimal public choices for registration; never returns teacher account records. */
@RestController
public class RegistrationOptionsController {
    private final SchoolRepository schools;
    private final PrefixRepository prefixes;
    private final GuidanceTeacherRepository teachers;

    public RegistrationOptionsController(SchoolRepository schools,
            PrefixRepository prefixes, GuidanceTeacherRepository teachers) {
        this.schools = schools;
        this.prefixes = prefixes;
        this.teachers = teachers;
    }

    @GetMapping("/api/auth/registration-options")
    @PreAuthorize("permitAll()")
    @Transactional(readOnly = true)
    public Options getOptions() {
        List<SchoolOption> schoolOptions = schools.findAll().stream()
                .filter(s -> !teachers.existsBySchool_SchoolId(s.getSchoolId()))
                .map(s -> {
                    String province = null;
                    if (s.getSubdistrict() != null
                            && s.getSubdistrict().getDistrict() != null
                            && s.getSubdistrict().getDistrict().getProvince() != null) {
                        province = s.getSubdistrict().getDistrict().getProvince().getName();
                    }
                    return new SchoolOption(s.getSchoolId(), s.getName(), s.getSchoolCode(), province);
                })
                .sorted(Comparator.comparing(SchoolOption::name))
                .toList();
        List<PrefixOption> prefixOptions = prefixes.findAll().stream()
                .map(p -> new PrefixOption(p.getPrefixId(), p.getPrefixName()))
                .sorted(Comparator.comparing(PrefixOption::prefixId))
                .toList();
        return new Options(LocalDate.now().getYear() + 543, schoolOptions, prefixOptions);
    }

    public record SchoolOption(Long schoolId, String name, String schoolCode, String provinceName) {}
    public record PrefixOption(Long prefixId, String prefixName) {}
    public record Options(int academicYear, List<SchoolOption> schools, List<PrefixOption> prefixes) {}
}
