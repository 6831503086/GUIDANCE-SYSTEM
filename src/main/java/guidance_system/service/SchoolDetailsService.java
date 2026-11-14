package guidance_system.service;

import guidance_system.entity.*;
import guidance_system.repository.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Locale;

@Service
@Transactional
public class SchoolDetailsService {
    private final GuidanceTeacherRepository teachers;
    private final SchoolRepository schools;
    private final SchoolDetailsRepository details;
    private final PrefixRepository prefixes;
    private final SubdistrictRepository subdistricts;
    private final EntityManager em;
    public SchoolDetailsService(GuidanceTeacherRepository teachers,SchoolRepository schools,
            SchoolDetailsRepository details,PrefixRepository prefixes,SubdistrictRepository subdistricts,EntityManager em){
        this.teachers=teachers;this.schools=schools;this.details=details;
        this.prefixes=prefixes;this.subdistricts=subdistricts;this.em=em;
    }
    public record Principal(Long prefixId,String firstName,String lastName,String position,
            String education,String license,String email) {}
    public record SchoolView(Long schoolId,String name,String schoolCode,String schoolType,Subdistrict subdistrict,
            String email,String phone,Principal principal,boolean hasLogo,LocalDateTime updatedAt) {}
    public record GeneralRequest(String name,String schoolType,Long subdistrictId,String email,String phone) {}
    public record Logo(byte[] bytes,String mime) {}
    @Transactional(readOnly=true)
    public SchoolView get(String email){School s=ownSchool(email,false);return view(s,details.findById(s.getSchoolId()).orElse(null));}
    public SchoolView saveGeneral(String email,GeneralRequest request){
        School s=ownSchool(email,true);
        String name=required(request.name(),200,"School name"),type=optional(request.schoolType(),50,"School type");
        String contact=validEmail(request.email(),"School email"),phone=required(request.phone(),20,"Phone");
        if(!phone.matches("[+0-9() -]{7,20}")||phone.replaceAll("[^0-9]","").length()<7)throw bad("Enter a valid school phone number.");
        Subdistrict location=null;
        if(request.subdistrictId()!=null)location=subdistricts.findById(request.subdistrictId()).orElseThrow(()->bad("Subdistrict not found."));
        SchoolDetails d=extension(s);
        s.setName(name);s.setSchoolType(type);s.setSubdistrict(location);schools.save(s);
        d.setEmail(contact);d.setPhone(phone);touch(d);
        return view(s,d);
    }
    @Transactional(rollbackFor=Exception.class)
    public SchoolView saveGeneralWithLogo(String email,GeneralRequest request,MultipartFile logo)throws IOException{
        Logo checked=logo==null||logo.isEmpty()?null:validateLogo(logo);
        SchoolView saved=saveGeneral(email,request);
        if(checked==null)return saved;
        School s=ownSchool(email,true);SchoolDetails d=extension(s);
        d.setLogoBytes(checked.bytes());d.setLogoMime(checked.mime());touch(d);return view(s,d);
    }
    public SchoolView savePrincipal(String email,Principal request){
        School s=ownSchool(email,true);
        if(request.prefixId()==null||!prefixes.existsById(request.prefixId()))throw bad("Select a valid prefix.");
        String first=required(request.firstName(),100,"Principal first name"),last=required(request.lastName(),100,"Principal last name");
        String position=required(request.position(),100,"Principal position"),contact=validEmail(request.email(),"Principal email");
        String education=optional(request.education(),200,"Education"),license=optional(request.license(),50,"License");
        SchoolDetails d=extension(s);d.setPrincipalPrefixId(request.prefixId());d.setPrincipalFirstName(first);
        d.setPrincipalLastName(last);d.setPrincipalPosition(position);d.setPrincipalEducation(education);
        d.setPrincipalLicense(license);d.setPrincipalEmail(contact);touch(d);return view(s,d);
    }
    private Logo validateLogo(MultipartFile file)throws IOException{
        if(file==null||file.isEmpty()||file.getSize()>2*1024*1024)throw bad("Select a JPG/PNG image up to 2 MB.");
        byte[] bytes=file.getBytes();String mime;
        try(ImageInputStream stream=ImageIO.createImageInputStream(new ByteArrayInputStream(bytes))){
            if(stream==null)throw bad("Invalid image.");
            var readers=ImageIO.getImageReaders(stream);if(!readers.hasNext())throw bad("Invalid image.");
            ImageReader reader=readers.next();
            try{
                reader.setInput(stream,true,true);String format=reader.getFormatName().toLowerCase(Locale.ROOT);
                if(!format.equals("png")&&!format.equals("jpeg")&&!format.equals("jpg"))throw bad("Only JPG/PNG images are supported.");
                if(reader.getWidth(0)>4096||reader.getHeight(0)>4096)throw bad("Image dimensions must not exceed 4096 pixels.");
                if(reader.read(0)==null)throw bad("Invalid image.");mime=format.equals("png")?"image/png":"image/jpeg";
            }finally{reader.dispose();}
        }
        return new Logo(bytes,mime);
    }
    @Transactional(readOnly=true)
    public Logo logo(String email){School s=ownSchool(email,false);SchoolDetails d=details.findById(s.getSchoolId()).orElse(null);
        return d==null||d.getLogoBytes()==null?null:new Logo(d.getLogoBytes(),d.getLogoMime());}
    private School ownSchool(String email,boolean lock){
        GuidanceTeacher teacher=teachers.findByEmail(email).orElseThrow(()->bad("Teacher account not found."));
        if(teacher.getSchool()==null)throw bad("No school is assigned to this account.");
        Long id=teacher.getSchool().getSchoolId();
        School s=lock?em.find(School.class,id,LockModeType.PESSIMISTIC_WRITE):teacher.getSchool();
        if(s==null)throw bad("School not found.");return s;
    }
    private SchoolDetails extension(School s){return details.findById(s.getSchoolId()).orElseGet(()->{SchoolDetails d=new SchoolDetails();d.setSchoolId(s.getSchoolId());return d;});}
    private void touch(SchoolDetails d){d.setUpdatedAt(LocalDateTime.now());details.save(d);}
    private SchoolView view(School s,SchoolDetails d){
        Principal p=d==null?new Principal(null,"","","","","",""):new Principal(d.getPrincipalPrefixId(),d.getPrincipalFirstName(),d.getPrincipalLastName(),d.getPrincipalPosition(),d.getPrincipalEducation(),d.getPrincipalLicense(),d.getPrincipalEmail());
        return new SchoolView(s.getSchoolId(),s.getName(),s.getSchoolCode(),s.getSchoolType(),s.getSubdistrict(),d==null?null:d.getEmail(),d==null?null:d.getPhone(),p,d!=null&&d.getLogoBytes()!=null,d==null?null:d.getUpdatedAt());
    }
    private static String validEmail(String value,String label){String email=required(value,150,label);if(!email.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$"))throw bad(label+" is invalid.");return email;}
    private static String required(String value,int max,String label){String result=optional(value,max,label);if(result==null)throw bad(label+" is required.");return result;}
    private static String optional(String value,int max,String label){if(value==null||value.isBlank())return null;String s=value.trim();if(s.length()>max)throw bad(label+" is too long.");return s;}
    private static IllegalArgumentException bad(String message){return new IllegalArgumentException(message);}
}
