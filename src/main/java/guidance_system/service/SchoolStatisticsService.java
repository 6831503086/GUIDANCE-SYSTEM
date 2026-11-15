package guidance_system.service;

import guidance_system.dto.SchoolStatisticsResponse;
import guidance_system.entity.*;
import guidance_system.repository.GuidanceTeacherRepository;
import jakarta.persistence.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import java.util.*;
import java.util.function.Predicate;

@Service
public class SchoolStatisticsService {
    private final GuidanceTeacherRepository teachers;
    @PersistenceContext private EntityManager em;
    public SchoolStatisticsService(GuidanceTeacherRepository teachers){this.teachers=teachers;}
    @Transactional(readOnly=true)
    public SchoolStatisticsResponse getStatistics(String email,Integer academicYear,String admissionRound,Long programId){
        GuidanceTeacher teacher=teachers.findByEmail(email).orElseThrow(()->bad("Teacher account not found."));
        if(teacher.getSchool()==null)throw bad("School is not assigned.");
        Integer year=academicYear==null?teacher.getActiveAcademicYear():academicYear;
        if(year==null||year<2400||year>3000)throw bad("Invalid academic year.");
        String round=admissionRound==null||admissionRound.isBlank()?null:admissionRound.trim();
        Long school=teacher.getSchool().getSchoolId();
        List<Applicant> applicants=em.createQuery("SELECT a FROM Applicant a WHERE a.school.schoolId=:school AND a.academicYear BETWEEN :first AND :last ORDER BY a.applicantId",Applicant.class)
            .setParameter("school",school).setParameter("first",year-1).setParameter("last",year).getResultList();
        List<ApplicantSelection> selections=em.createQuery("SELECT s FROM ApplicantSelection s JOIN FETCH s.applicant a JOIN FETCH s.entryGroup e JOIN FETCH s.status WHERE a.school.schoolId=:school AND e.academicYear BETWEEN :first AND :last",ApplicantSelection.class)
            .setParameter("school",school).setParameter("first",year-1).setParameter("last",year).getResultList();
        Set<Long> interestIds=null;
        if(programId!=null){
            Program program=em.find(Program.class,programId);
            if(program==null||!Objects.equals(program.getEntryGroup().getAcademicYear(),year)||
                (round!=null&&!round.equalsIgnoreCase(program.getEntryGroup().getEntrygroupName())))throw bad("Program does not match the selected year and round.");
            // Program IDs are specific to a year/round. Compare equivalent name and faculty for YoY.
            interestIds=new HashSet<>(em.createQuery("SELECT DISTINCT i.applicant.applicantId FROM ProgramInterest i WHERE i.applicant.school.schoolId=:school AND i.applicant.academicYear BETWEEN :first AND :last AND ((i.applicant.academicYear=:last AND i.program.programId=:id) OR (i.applicant.academicYear<:last AND i.program.programName=:name AND (i.program.faculty=:faculty OR (i.program.faculty IS NULL AND :faculty IS NULL))))",Long.class)
                .setParameter("school",school).setParameter("first",year-1).setParameter("last",year).setParameter("id",programId).setParameter("name",program.getProgramName()).setParameter("faculty",program.getFaculty()).getResultList());
        }
        Set<Long> accepted=interestIds;
        Map<Long,ApplicantSelection> latest=new HashMap<>();
        Comparator<ApplicantSelection> order=Comparator.comparing(ApplicantSelection::getAppliedAt,Comparator.nullsFirst(Comparator.naturalOrder()))
            .thenComparing(ApplicantSelection::getApplicationId,Comparator.nullsFirst(Comparator.naturalOrder()));
        for(ApplicantSelection s:selections){
            if(!Objects.equals(s.getApplicant().getAcademicYear(),s.getEntryGroup().getAcademicYear()))continue;
            if(round!=null&&!round.equalsIgnoreCase(s.getEntryGroup().getEntrygroupName()))continue;
            latest.merge(s.getApplicant().getApplicantId(),s,(a,b)->order.compare(a,b)>=0?a:b);
        }
        List<Applicant> filtered=applicants.stream().filter(a->accepted==null||accepted.contains(a.getApplicantId()))
            .filter(a->round==null||latest.containsKey(a.getApplicantId())).toList();
        List<SchoolStatisticsResponse.ApplicantRow> current=rows(filtered,latest,year),previous=rows(filtered,latest,year-1);
        boolean published=current.stream().anyMatch(r->r.statusCode()!=null),previousPublished=previous.stream().anyMatch(r->r.statusCode()!=null);
        long total=current.size(),prevTotal=previous.size();
        Predicate<String> eligible=c->Set.of("20","21","22","23","24","25","26","27","28").contains(c);
        Predicate<String> selected=c->Set.of("40","41","42","43","50").contains(c);
        long e=count(current,eligible),s=count(current,selected),en=count(current,"50"::equals),w=count(current,"42"::equals),n=count(current,"41"::equals);
        return new SchoolStatisticsResponse(teacher.getSchool().getName(),teacher.getActiveAcademicYear(),year,round,programId,
            new SchoolStatisticsResponse.SummaryCards(card(total,total,prevTotal),
                outcome(published,e,total,previousPublished,previous,eligible),outcome(published,s,total,previousPublished,previous,selected),
                outcome(published,en,total,previousPublished,previous,"50"::equals),outcome(published,w,total,previousPublished,previous,"42"::equals),
                outcome(published,n,total,previousPublished,previous,"41"::equals)),
            published?List.of(item("Applied",total),item("Interview Eligible",e),item("Selected",s),item("Enrolled",en)):List.of(item("Applied",total)),
            published?List.of(item("Enrolled",en),item("Waived",w),item("Not Enrolled",n)):List.of(),current);
    }
    private List<SchoolStatisticsResponse.ApplicantRow> rows(List<Applicant> applicants,Map<Long,ApplicantSelection> latest,int year){
        return applicants.stream().filter(a->Objects.equals(a.getAcademicYear(),year)).map(a->{
            ApplicantSelection s=latest.get(a.getApplicantId());boolean visible=s!=null&&s.getPublishedAt()!=null&&s.getStatus()!=null;
            return new SchoolStatisticsResponse.ApplicantRow(a.getApplicantId(),a.getNationalId(),(Objects.toString(a.getFirstName(),"")+" "+Objects.toString(a.getLastName(),"")).trim(),a.getAcademicYear(),a.getTerm(),s==null?null:s.getEntryGroup().getEntrygroupName(),visible?s.getStatus().getStatusCode():null,visible?s.getStatus().getDescription():null);
        }).toList();
    }
    private long count(List<SchoolStatisticsResponse.ApplicantRow> rows,Predicate<String> test){return rows.stream().filter(r->r.statusCode()!=null&&test.test(r.statusCode())).count();}
    private SchoolStatisticsResponse.SummaryCard outcome(boolean visible,long count,long total,boolean previousVisible,List<SchoolStatisticsResponse.ApplicantRow> previous,Predicate<String> test){
        return visible?card(count,total,previousVisible?count(previous,test):null):null;
    }
    private SchoolStatisticsResponse.SummaryCard card(long count,long total,Long previous){
        return new SchoolStatisticsResponse.SummaryCard(count,total==0?0:100.0*count/total,previous,previous==null||previous==0?null:100.0*(count-previous)/previous);
    }
    private SchoolStatisticsResponse.ChartItem item(String label,long count){return new SchoolStatisticsResponse.ChartItem(label,count);}
    private ResponseStatusException bad(String message){return new ResponseStatusException(HttpStatus.BAD_REQUEST,message);}
}
