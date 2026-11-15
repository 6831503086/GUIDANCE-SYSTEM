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
    @jakarta.persistence.PersistenceContext
    private jakarta.persistence.EntityManager entityManager;
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
        @SuppressWarnings("unchecked")
        List<Object[]> rows = entityManager.createNativeQuery("""
                SELECT s.school_id, s.name, s.school_code, p.name
                FROM school s
                LEFT JOIN subdistrict sd ON sd.subdistrict_id = s.subdistrict_id
                LEFT JOIN district d ON d.district_id = sd.district_id
                LEFT JOIN province p ON p.province_id = d.province_id
                ORDER BY s.name, s.school_id
                """).getResultList();
        List<SchoolOption> schoolOptions = rows.stream().map(row -> new SchoolOption(
                ((Number) row[0]).longValue(), (String) row[1], (String) row[2], (String) row[3])).toList();
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
