package guidance_system.controller;
import guidance_system.service.SchoolDetailsService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/api/schools/me/details")
@PreAuthorize("hasRole('TEACHER')")
public class SchoolDetailsController {
    private final SchoolDetailsService service;
    public SchoolDetailsController(SchoolDetailsService service){this.service=service;}
    @GetMapping
    public SchoolDetailsService.SchoolView get(Authentication auth){return service.get(auth.getName());}
    @PutMapping
    public SchoolDetailsService.SchoolView save(@RequestBody SchoolDetailsService.GeneralRequest request,Authentication auth){return service.saveGeneral(auth.getName(),request);}
    @PutMapping("/principal")
    public SchoolDetailsService.SchoolView principal(@RequestBody SchoolDetailsService.Principal request,Authentication auth){return service.savePrincipal(auth.getName(),request);}
    @PostMapping(value="/general",consumes="multipart/form-data")
    public SchoolDetailsService.SchoolView saveWithLogo(@RequestPart("details") SchoolDetailsService.GeneralRequest request,
            @RequestPart(value="logo",required=false) MultipartFile logo,Authentication auth)throws IOException{
        return service.saveGeneralWithLogo(auth.getName(),request,logo);
    }
    @GetMapping("/logo")
    public ResponseEntity<byte[]> logo(Authentication auth){var logo=service.logo(auth.getName());if(logo==null)return ResponseEntity.notFound().build();
        return ResponseEntity.ok().header("Content-Type",logo.mime()).header("Cache-Control","no-store")
                .header("X-Content-Type-Options","nosniff").body(logo.bytes());}
    @ExceptionHandler({IllegalArgumentException.class,IOException.class})
    public ResponseEntity<Map<String,String>> invalid(Exception ex){return ResponseEntity.badRequest().body(Map.of("message",ex.getMessage()==null?"Unable to read image.":ex.getMessage()));}
}
