package guidance_system;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {

    @GetMapping("/api/test")
    public String test() 
    {
        return "Guidance Counselor System Backend is working!";
    }
}