package guidance_system.entity;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name="school_details")
public class SchoolDetails {
    @Id @Column(name="school_id")
    private Long schoolId;
    @Column(name="email", length=150)
    private String email;
    @Column(name="phone", length=20)
    private String phone;
    @Column(name="principal_prefix_id")
    private Long principalPrefixId;
    @Column(name="principal_first_name", length=100)
    private String principalFirstName;
    @Column(name="principal_last_name", length=100)
    private String principalLastName;
    @Column(name="principal_position", length=100)
    private String principalPosition;
    @Column(name="principal_education", length=200)
    private String principalEducation;
    @Column(name="principal_license", length=50)
    private String principalLicense;
    @Column(name="principal_email", length=150)
    private String principalEmail;
    @Lob @Column(name="logo_bytes")
    private byte[] logoBytes;
    @Column(name="logo_mime",length=30)
    private String logoMime;
    @Column(name="updated_at")
    private LocalDateTime updatedAt;
    public SchoolDetails() {}
    public Long getSchoolId(){return schoolId;}
    public void setSchoolId(Long value){schoolId=value;}
    public String getEmail(){return email;}
    public void setEmail(String value){email=value;}
    public String getPhone(){return phone;}
    public void setPhone(String value){phone=value;}
    public Long getPrincipalPrefixId(){return principalPrefixId;}
    public void setPrincipalPrefixId(Long value){principalPrefixId=value;}
    public String getPrincipalFirstName(){return principalFirstName;}
    public void setPrincipalFirstName(String value){principalFirstName=value;}
    public String getPrincipalLastName(){return principalLastName;}
    public void setPrincipalLastName(String value){principalLastName=value;}
    public String getPrincipalPosition(){return principalPosition;}
    public void setPrincipalPosition(String value){principalPosition=value;}
    public String getPrincipalEducation(){return principalEducation;}
    public void setPrincipalEducation(String value){principalEducation=value;}
    public String getPrincipalLicense(){return principalLicense;}
    public void setPrincipalLicense(String value){principalLicense=value;}
    public String getPrincipalEmail(){return principalEmail;}
    public void setPrincipalEmail(String value){principalEmail=value;}
    public byte[] getLogoBytes(){return logoBytes;}
    public void setLogoBytes(byte[] value){logoBytes=value;}
    public String getLogoMime(){return logoMime;}
    public void setLogoMime(String value){logoMime=value;}
    public LocalDateTime getUpdatedAt(){return updatedAt;}
    public void setUpdatedAt(LocalDateTime value){updatedAt=value;}
}
