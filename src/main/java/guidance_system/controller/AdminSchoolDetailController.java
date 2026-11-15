package guidance_system.controller;

import guidance_system.entity.Prefix;
import guidance_system.repository.PrefixRepository;
import guidance_system.service.SchoolDetailsService;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/schools")
@PreAuthorize("hasRole('ADMIN')")
@Transactional(readOnly = true)
public class AdminSchoolDetailController {
    private final SchoolDetailsService details;
    private final PrefixRepository prefixes;

    public AdminSchoolDetailController(SchoolDetailsService details, PrefixRepository prefixes) {
        this.details = details;
        this.prefixes = prefixes;
    }

    public record DetailResponse(SchoolDetailsService.SchoolView school, String principalPrefixName) {}

    @GetMapping("/{schoolId}/details")
    public ResponseEntity<DetailResponse> get(@PathVariable Long schoolId) {
        var school = details.getForAdmin(schoolId);
        Long prefixId = school.principal() == null ? null : school.principal().prefixId();
        String prefixName = prefixId == null ? null
                : prefixes.findById(prefixId).map(Prefix::getPrefixName).orElse(null);
        return ResponseEntity.ok().cacheControl(CacheControl.noStore())
                .body(new DetailResponse(school, prefixName));
    }

    @GetMapping("/{schoolId}/details/logo")
    public ResponseEntity<byte[]> logo(@PathVariable Long schoolId) {
        var logo = details.logoForAdmin(schoolId);
        if (logo == null || logo.bytes() == null || logo.bytes().length == 0
                || !("image/png".equals(logo.mime()) || "image/jpeg".equals(logo.mime()))) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok().cacheControl(CacheControl.noStore())
                .header("X-Content-Type-Options", "nosniff")
                .contentType(MediaType.parseMediaType(logo.mime())).body(logo.bytes());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> invalidSchool(IllegalArgumentException exception) {
        return ResponseEntity.badRequest().body(exception.getMessage());
    }
}
