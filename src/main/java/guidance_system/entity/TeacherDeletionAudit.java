package guidance_system.entity;
import jakarta.persistence.*;
import java.time.Instant;
/** Minimal retained deletion record. Deliberately has no account FK or contact/profile fields. */
@Entity @Table(name="teacher_deletion_audit")
public class TeacherDeletionAudit {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long auditId;
 @Column(nullable=false) private Long formerTeacherId;
 @Column(nullable=false) private Instant deletedAt;
 @Column(nullable=false) private boolean deletionConsent;
 @Column private Boolean registrationConsent;
 public TeacherDeletionAudit(){}
 public TeacherDeletionAudit(Long teacherId,Boolean registrationConsent){this.formerTeacherId=teacherId;this.registrationConsent=registrationConsent;this.deletionConsent=true;this.deletedAt=Instant.now();}
 public Long getAuditId(){return auditId;}
 public Long getFormerTeacherId(){return formerTeacherId;}
 public Instant getDeletedAt(){return deletedAt;}
 public boolean isDeletionConsent(){return deletionConsent;}
 public Boolean getRegistrationConsent(){return registrationConsent;}
}
