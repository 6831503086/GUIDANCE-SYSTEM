package guidance_system.service;

import guidance_system.dto.CalendarEventResponse;
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
public class CalendarPdfService {
    public byte[] generatePdf(List<CalendarEventResponse> events) throws IOException {
        Font font=loadFont("NotoSansThai-Regular.ttf");
        Font latin=loadFont("NotoSans-Regular.ttf");
        Layout layout=new Layout(font,latin);
        layout.text("ปฏิทินกำหนดการรับสมัคร",34,true);
        layout.text("Guidance Teacher System · มหาวิทยาลัยแม่ฟ้าหลวง",22,false);
        layout.text("จำนวนกำหนดการ "+events.size()+" รายการ",22,false);
        layout.y+=20;
        if(events.isEmpty())layout.text("ไม่พบข้อมูลกำหนดการตามเงื่อนไข",26,false);
        int count=0;
        for(CalendarEventResponse event:events){
            if(layout.y>1370)layout.page();
            layout.text((++count)+". "+safe(event.getEventTitle()),26,true);
            layout.text("ประเภท: "+label(event.getEventType()),23,false);
            layout.text("วันที่: "+safe(event.getEventStartDate())+
                    (java.util.Objects.equals(event.getEventStartDate(),event.getEventEndDate())?"":" ถึง "+safe(event.getEventEndDate())),23,false);
            layout.text("ปีการศึกษา: "+safe(event.getAcademicYear())+" · รอบ: "+safe(event.getRound()),23,false);
            layout.text(event.getSourcePostId()==null?"แหล่งข้อมูล: รอบรับสมัคร":"แหล่งข้อมูล: ข่าว / ประกาศ #"+event.getSourcePostId(),20,false);
            layout.y+=22;
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
    private static String label(String type){return switch(type==null?"":type){
        case "APPLICATION_OPEN"->"เปิดรับสมัคร";case "APPLICATION_CLOSE"->"ปิดรับสมัคร";
        case "RESULT_ANNOUNCEMENT"->"ประกาศผล";case "RIGHTS_CONFIRMATION"->"ยืนยันสิทธิ์";
        default->safe(type);};}
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
