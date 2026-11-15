package guidance_system.service;
import guidance_system.entity.*;
import guidance_system.repository.GuidanceTeacherRepository;
import jakarta.persistence.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import java.io.IOException;
import java.nio.file.*;

@Service
public class SelfAccountDeletionService {
 private final GuidanceTeacherRepository teachers;
 @PersistenceContext private EntityManager em;
 public SelfAccountDeletionService(GuidanceTeacherRepository teachers){this.teachers=teachers;}
 @Transactional(rollbackFor=Exception.class)
 public Long deleteOwnAccount(String email,Boolean consent)throws IOException{
  if(!Boolean.TRUE.equals(consent))throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Accept the account deletion terms first.");
  var found=teachers.findByEmail(email).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Teacher account no longer exists."));
  GuidanceTeacher teacher=em.find(GuidanceTeacher.class,found.getTeacherId(),LockModeType.PESSIMISTIC_WRITE);
  if(teacher==null)throw new ResponseStatusException(HttpStatus.NOT_FOUND,"Teacher account no longer exists.");
  Long id=teacher.getTeacherId();
  Path picture=null;
  if(teacher.getProfilePicture()!=null&&!teacher.getProfilePicture().isBlank()){
   String url=teacher.getProfilePicture();
   if(!url.matches("/uploads/profile-pictures/[a-fA-F0-9-]+\\.(?:jpg|png)"))throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Profile picture path is invalid. Contact the administrator.");
   Path directory=Paths.get("uploads/profile-pictures").toAbsolutePath().normalize();
   picture=directory.resolve(url.substring(url.lastIndexOf('/')+1)).normalize();
   if(!picture.getParent().equals(directory))throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Invalid profile picture path.");
  }
  em.persist(new TeacherDeletionAudit(id,teacher.getPdpaConsent()));
  em.createQuery("DELETE FROM VerificationToken t WHERE t.teacher.teacherId=:id").setParameter("id",id).executeUpdate();
  em.createQuery("DELETE FROM PostPin p WHERE p.teacher.teacherId=:id").setParameter("id",id).executeUpdate();
  em.createQuery("DELETE FROM PostRegistration p WHERE p.teacher.teacherId=:id").setParameter("id",id).executeUpdate();
  em.remove(teacher);em.flush();
  // Report a file-removal failure instead of claiming successful personal-data removal.
  if(picture!=null)Files.deleteIfExists(picture);
  return id;
 }
}
