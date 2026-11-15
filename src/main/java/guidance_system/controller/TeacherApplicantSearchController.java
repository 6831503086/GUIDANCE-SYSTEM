package guidance_system.controller;

import guidance_system.entity.*;
import guidance_system.repository.GuidanceTeacherRepository;
import guidance_system.repository.ApplicantStatusRepository;
import jakarta.persistence.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import java.util.*;
import java.util.function.IntFunction;

@RestController
@RequestMapping("/api/reports/applicant-search")
@PreAuthorize("hasRole('TEACHER')")
public class TeacherApplicantSearchController {
 @PersistenceContext private EntityManager em;
 private final GuidanceTeacherRepository teachers;
 private final ApplicantStatusRepository statuses;
 public TeacherApplicantSearchController(GuidanceTeacherRepository teachers,ApplicantStatusRepository statuses){this.teachers=teachers;this.statuses=statuses;}
 public record Row(Long applicantId,String studentId,String fullName,Integer academicYear,String round,String statusCode,String statusDescription){}
 public record Count(String label,long count){}
 public record StatusOption(String statusCode,String description){}
 public record Summary(long totalApplicants,Long eligibleForExamination,Long passedSelection,Long rightsConfirmed,Long rightsWaived,Long enrolled){}
 public record Report(String schoolName,int selectedAcademicYear,Summary summary,List<Count> rounds,List<Count> history,List<Row> applicants,List<StatusOption> statuses,List<Integer> years,List<String> availableRounds){}
 @GetMapping
 @Transactional(readOnly=true)
 public ResponseEntity<Report> search(Authentication authentication,
    @RequestParam(required=false) Integer academicYear,@RequestParam(required=false) String admissionRound,
    @RequestParam(required=false) Long programId,@RequestParam(required=false) String statusCode,
    @RequestParam(required=false) String keyword){
    GuidanceTeacher teacher=teachers.findByEmail(authentication.getName()).orElseThrow(()->new ResponseStatusException(HttpStatus.FORBIDDEN,"Teacher account not found."));
    if(teacher.getSchool()==null)throw new ResponseStatusException(HttpStatus.FORBIDDEN,"School is not assigned.");
    Integer year=academicYear!=null?academicYear:teacher.getActiveAcademicYear();
    if(year==null||year<2400||year>3000)throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Invalid academic year.");
    String round=normalize(admissionRound),status=normalize(statusCode),q=normalize(keyword);
    if(q!=null&&q.length()>255)throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Search keyword is too long.");
    if(status!=null&&!statuses.existsById(status))throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Unknown applicant status.");
    Long schoolId=teacher.getSchool().getSchoolId();
    List<Applicant> applicants=em.createQuery("SELECT a FROM Applicant a WHERE a.school.schoolId=:school AND a.academicYear BETWEEN :first AND :last ORDER BY a.applicantId",Applicant.class).setParameter("school",schoolId).setParameter("first",year-2).setParameter("last",year).getResultList();
    List<ApplicantSelection> selections=em.createQuery("SELECT s FROM ApplicantSelection s JOIN FETCH s.entryGroup eg JOIN FETCH s.status WHERE s.applicant.school.schoolId=:school AND eg.academicYear BETWEEN :first AND :last",ApplicantSelection.class).setParameter("school",schoolId).setParameter("first",year-2).setParameter("last",year).getResultList();
    Set<Long> interested=null;
    if(programId!=null){
      Program program=em.find(Program.class,programId);
      if(program==null)throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Unknown program.");
      // Program IDs are round/year-specific. Compare equivalent program names for historical years.
      interested=new HashSet<>(em.createQuery("SELECT DISTINCT i.applicant.applicantId FROM ProgramInterest i WHERE i.applicant.school.schoolId=:school AND i.applicant.academicYear BETWEEN :first AND :last AND ((i.applicant.academicYear=:last AND i.program.programId=:program) OR (i.applicant.academicYear<:last AND i.program.programName=:name))",Long.class).setParameter("school",schoolId).setParameter("first",year-2).setParameter("last",year).setParameter("program",programId).setParameter("name",program.getProgramName()).getResultList());
    }
    Map<Long,ApplicantSelection> latest=new HashMap<>();
    Comparator<ApplicantSelection> order=Comparator.comparing(ApplicantSelection::getAppliedAt,Comparator.nullsFirst(Comparator.naturalOrder())).thenComparing(ApplicantSelection::getApplicationId,Comparator.nullsFirst(Comparator.naturalOrder()));
    Map<Long,Integer> applicantYears=new HashMap<>();applicants.forEach(a->applicantYears.put(a.getApplicantId(),a.getAcademicYear()));
    for(ApplicantSelection selection:selections){
      if(!Objects.equals(applicantYears.get(selection.getApplicant().getApplicantId()),selection.getEntryGroup().getAcademicYear()))continue;
      if(round!=null&&!round.equalsIgnoreCase(selection.getEntryGroup().getEntrygroupName()))continue;
      latest.merge(selection.getApplicant().getApplicantId(),selection,(a,b)->order.compare(a,b)>=0?a:b);
    }
    final Set<Long> interestIds=interested;
    final String search=q==null?null:q.toLowerCase(Locale.ROOT);
    List<Row> filtered=new ArrayList<>();
    for(Applicant a:applicants){
      ApplicantSelection selection=latest.get(a.getApplicantId());
      if(round!=null&&selection==null)continue;
      if(interestIds!=null&&!interestIds.contains(a.getApplicantId()))continue;
      String name=(Objects.toString(a.getFirstName(),"")+" "+Objects.toString(a.getLastName(),"")).trim();
      if(search!=null){
        boolean numeric=search.matches("[0-9]+");
        boolean matches=numeric
            ? a.getApplicantId().toString().equals(search)||Objects.toString(a.getNationalId(),"").equals(search)
            : name.toLowerCase(Locale.ROOT).contains(search)||Objects.toString(a.getNationalId(),"").toLowerCase(Locale.ROOT).contains(search);
        if(!matches)continue;
      }
      // Never filter by, count, or return unpublished result codes.
      boolean published=selection!=null&&selection.getPublishedAt()!=null&&selection.getStatus()!=null;
      String code=published?selection.getStatus().getStatusCode():null;
      if(status!=null&&!status.equals(code))continue;
      filtered.add(new Row(a.getApplicantId(),a.getNationalId(),name,a.getAcademicYear(),selection==null?null:selection.getEntryGroup().getEntrygroupName(),code,published?selection.getStatus().getDescription():null));
    }
    List<Row> current=filtered.stream().filter(a->a.academicYear()==year.intValue()).toList();
    long total=current.size();boolean published=current.stream().anyMatch(a->a.statusCode()!=null);
    long eligible=current.stream().filter(a->Set.of("20","21","22","23","24","25","26","27","28").contains(Objects.toString(a.statusCode(),""))).count();
    long selected=current.stream().filter(a->Set.of("40","41","42","43","50").contains(Objects.toString(a.statusCode(),""))).count();
    // Match the existing ApplicantOverviewReportService status mapping.
    long confirmed=current.stream().filter(a->Set.of("40","43","50").contains(Objects.toString(a.statusCode(),""))).count();
    long waived=current.stream().filter(a->"42".equals(a.statusCode())).count(),enrolled=current.stream().filter(a->"50".equals(a.statusCode())).count();
    Summary summary=new Summary(total,published||total==0?eligible:null,published||total==0?selected:null,published||total==0?confirmed:null,published||total==0?waived:null,published||total==0?enrolled:null);
    Map<String,Long> counts=new TreeMap<>();current.forEach(a->counts.merge(Objects.toString(a.round(),"ยังไม่ระบุรอบ"),1L,Long::sum));
    List<Count> rounds=counts.entrySet().stream().map(e->new Count(e.getKey(),e.getValue())).toList();
    List<Count> history=new ArrayList<>();for(int y=year-2;y<=year;y++){final int selectedYear=y;history.add(new Count(String.valueOf(y),filtered.stream().filter(a->a.academicYear()==selectedYear).count()));}
    List<StatusOption> options=statuses.findAll().stream().sorted(Comparator.comparing(ApplicantStatus::getStatusCode)).map(s->new StatusOption(s.getStatusCode(),s.getDescription())).toList();
    List<Integer> years=em.createQuery("SELECT DISTINCT e.academicYear FROM EntryGroup e ORDER BY e.academicYear DESC",Integer.class).getResultList();
    Set<Integer> yearSet=new TreeSet<>(Comparator.reverseOrder());yearSet.addAll(years);yearSet.add(year);for(int y=year-2;y<=year;y++)yearSet.add(y);
    List<String> availableRounds=em.createQuery("SELECT DISTINCT e.entrygroupName FROM EntryGroup e WHERE e.academicYear=:year ORDER BY e.entrygroupName",String.class).setParameter("year",year).getResultList();
    return ResponseEntity.ok().header("Cache-Control","no-store").body(new Report(teacher.getSchool().getName(),year,summary,rounds,history,current,options,new ArrayList<>(yearSet),availableRounds));
 }
 private String normalize(String value){return value==null||value.isBlank()?null:value.trim();}
}
