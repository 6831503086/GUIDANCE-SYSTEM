package guidance_system.service;

import guidance_system.dto.SchoolStatisticsResponse;
import guidance_system.entity.Program;
import jakarta.persistence.*;
import org.springframework.transaction.annotation.Transactional;
import org.apache.pdfbox.pdmodel.*;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.image.LosslessFactory;
import org.springframework.stereotype.Service;
import java.awt.*;
import java.awt.font.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.text.*;
import java.util.ArrayList;
import java.util.List;

/** Renders with an embedded Thai font and Java text shaping, so Thai marks stay readable. */
@Service
public class SchoolStatisticsPdfService {
    @PersistenceContext private EntityManager em;
    @Transactional(readOnly=true)
    public byte[] generatePdf(SchoolStatisticsResponse report) throws IOException {
        Font font=loadFont("NotoSansThai-Regular.ttf");
        Font latin=loadFont("NotoSans-Regular.ttf");
        Layout layout=new Layout(font,latin);
        layout.text("รายงานสถิติโรงเรียน",34,true);
        layout.text("โรงเรียน: "+safe(report.schoolName()),26,true);
        layout.text("ปีการศึกษาที่ใช้งาน: "+safe(report.activeAcademicYear())+" · ปีที่ค้นหา: "+safe(report.academicYear()),22,false);
        layout.text("รอบรับสมัคร: "+(report.admissionRound()==null?"ทุกรอบ":report.admissionRound()),22,false);
        Program program=report.programId()==null?null:em.find(Program.class,report.programId());
        layout.text("หลักสูตร: "+(program==null?"ทั้งหมด":program.getProgramName()),22,false);
        layout.text("เทียบปีการศึกษาก่อนหน้า: "+(report.academicYear()-1),22,false);
        layout.y+=16;
        var c=report.summaryCards();
        String[] names={"ผู้สมัครทั้งหมด","ผู้มีสิทธิ์สอบคัดเลือก","ผู้ผ่านการคัดเลือก","ผู้ขึ้นทะเบียนเป็นนักศึกษา","ผู้สละสิทธิ์","ผู้ไม่ขึ้นทะเบียน"};
        SchoolStatisticsResponse.SummaryCard[] cards={c.totalApplicants(),c.interviewEligible(),c.selected(),c.enrolled(),c.waived(),c.notEnrolled()};
        for(int i=0;i<cards.length;i++){
            var card=cards[i];
            if(card==null)continue;
            layout.text(names[i]+": "+card.count()+" คน · "+String.format(java.util.Locale.ROOT,"%.2f%%",card.proportion()),22,true);
            layout.text("ปีก่อน: "+(card.previousYearCount()==null?"ไม่แสดงผลที่ยังไม่เผยแพร่":card.previousYearCount())+" · เปลี่ยนแปลง: "+(card.yoyChangePercent()==null?"คำนวณไม่ได้ / ไม่มีฐานเปรียบเทียบ":String.format(java.util.Locale.ROOT,"%+.2f%%",card.yoyChangePercent())),18,false);
            layout.y+=4;
        }
        if(report.applicants().isEmpty())layout.text("ไม่พบข้อมูลตามเงื่อนไข",24,false);
        if(c.selected()==null)layout.text("ซ่อนสถิติผลคัดเลือกและผลหลังคัดเลือกที่ยังไม่เผยแพร่",22,false);
        layout.text("ขั้นตอนการสมัคร",28,true);
        long max=report.applicationFunnel().stream().mapToLong(SchoolStatisticsResponse.ChartItem::count).max().orElse(0);
        for(var item:report.applicationFunnel())layout.bar(label(item.label()),item.count(),max);
        if(!report.postSelectionResults().isEmpty()){
            layout.text("ผลหลังการคัดเลือก",28,true);
            long postMax=report.postSelectionResults().stream().mapToLong(SchoolStatisticsResponse.ChartItem::count).max().orElse(0);
            for(var item:report.postSelectionResults())layout.bar(label(item.label()),item.count(),postMax);
        }
        layout.graphics.dispose();
        try(PDDocument document=new PDDocument();ByteArrayOutputStream output=new ByteArrayOutputStream()){
            for(int index=0;index<layout.pages.size();index++){
                BufferedImage image=layout.pages.get(index);Graphics2D footer=image.createGraphics();
                footer.setColor(new Color(99,99,102));
                new TextLayout(attributed("หน้า "+(index+1)+" / "+layout.pages.size(),font.deriveFont(20f),latin.deriveFont(20f)).getIterator(),footer.getFontRenderContext()).draw(footer,70,1630);footer.dispose();
                PDPage page=new PDPage(PDRectangle.A4);document.addPage(page);
                try(PDPageContentStream content=new PDPageContentStream(document,page)){
                    content.drawImage(LosslessFactory.createFromImage(document,image),0,0,PDRectangle.A4.getWidth(),PDRectangle.A4.getHeight());
                }
            }
            document.save(output);return output.toByteArray();
        }
    }
    private Font loadFont(String name) throws IOException {
        try(InputStream stream=getClass().getResourceAsStream("/fonts/calendar/"+name)){
            if(stream==null)throw new IOException("Calendar font is missing: "+name);
            return Font.createFont(Font.TRUETYPE_FONT,stream);
        }catch(FontFormatException error){throw new IOException("Cannot load calendar font.",error);}
    }
    private static AttributedString attributed(String value,Font thai,Font latin){
        AttributedString result=new AttributedString(value);result.addAttribute(TextAttribute.FONT,latin);
        for(int i=0;i<value.length();i++)if(!latin.canDisplay(value.charAt(i))&&thai.canDisplay(value.charAt(i)))result.addAttribute(TextAttribute.FONT,thai,i,i+1);
        return result;
    }
    private static String safe(Object value){return value==null?"ยังไม่ระบุ":value.toString();}
    private static String label(String label){return switch(label){
        case "Applied"->"สมัคร";case "Interview Eligible"->"มีสิทธิ์สอบคัดเลือก";
        case "Selected"->"ผ่านการคัดเลือก";case "Enrolled"->"ขึ้นทะเบียนเป็นนักศึกษา";
        case "Waived"->"สละสิทธิ์";case "Not Enrolled"->"ไม่ขึ้นทะเบียน";default->label;};}
    private static class Layout {
        final Font font,latin;final List<BufferedImage> pages=new ArrayList<>();Graphics2D graphics;float y;
        Layout(Font font,Font latin){this.font=font;this.latin=latin;page();}
        void page(){
            if(graphics!=null)graphics.dispose();
            BufferedImage image=new BufferedImage(1190,1684,BufferedImage.TYPE_INT_RGB);pages.add(image);
            graphics=image.createGraphics();graphics.setColor(Color.WHITE);graphics.fillRect(0,0,1190,1684);
            graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
            graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            y=80;
        }
        void bar(String label,long count,long max){
            if(y>1460)page();
            text(label+": "+count+" คน",22,false);
            graphics.setColor(new Color(229,229,224));graphics.fillRoundRect(70,(int)y,1050,22,10,10);
            graphics.setColor(new Color(122,34,34));
            if(max>0&&count>0)graphics.fillRoundRect(70,(int)y,(int)Math.round(1050.0*count/max),22,10,10);
            y+=40;
        }
        void text(String value,int size,boolean bold){
            Font selected=font.deriveFont(bold?Font.BOLD:Font.PLAIN,(float)size);
            String cleaned=value.replaceAll("[\\p{Cntrl}]"," ");
            if(cleaned.isBlank()){y+=size;return;}
            AttributedString attributed=attributed(cleaned,selected,latin.deriveFont(bold?Font.BOLD:Font.PLAIN,(float)size));
            LineBreakMeasurer measure=new LineBreakMeasurer(attributed.getIterator(),BreakIterator.getLineInstance(new java.util.Locale("th")),graphics.getFontRenderContext());
            while(measure.getPosition()<cleaned.length()){
                TextLayout line=measure.nextLayout(1050);
                float height=line.getAscent()+line.getDescent()+line.getLeading()+8;
                if(y+height>1570)page();
                graphics.setColor(bold?new Color(94,26,20):new Color(28,28,30));
                y+=line.getAscent();line.draw(graphics,70,y);y+=line.getDescent()+line.getLeading()+8;
            }
        }
    }
}
