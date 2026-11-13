
package guidance_system.service;

import guidance_system.dto.CalendarEventResponse;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

@Service
public class CalendarPdfService {

    public byte[] generatePdf(List<CalendarEventResponse> events)
            throws IOException {

        try (PDDocument document = new PDDocument();
             ByteArrayOutputStream output = new ByteArrayOutputStream()) {

            PDType1Font font =
                    new PDType1Font(Standard14Fonts.FontName.HELVETICA);

            PDType1Font boldFont =
                    new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);

            int eventsPerPage = 35;
            int pageCount = Math.max(
                    1, (int) Math.ceil(events.size() / (double) eventsPerPage));

            for (int pageNumber = 0; pageNumber < pageCount; pageNumber++) {

                PDPage page = new PDPage(PDRectangle.A4);
                document.addPage(page);

                try (PDPageContentStream content =
                             new PDPageContentStream(document, page)) {

                    content.beginText();
                    content.setFont(boldFont, 18);
                    content.newLineAtOffset(50, 790);
                    content.showText("Application Schedule Calendar");
                    content.endText();

                    content.beginText();
                    content.setFont(font, 10);
                    content.newLineAtOffset(50, 765);
                    content.showText("Guidance Teacher System");
                    content.endText();

                    int start = pageNumber * eventsPerPage;
                    int end = Math.min(start + eventsPerPage, events.size());

                    if (events.isEmpty()) {
                        content.beginText();
                        content.setFont(font, 12);
                        content.newLineAtOffset(50, 730);
                        content.showText("No events found.");
                        content.endText();
                    }

                    float y = 735;

                    for (int i = start; i < end; i++) {

                        CalendarEventResponse event = events.get(i);

                        String line = event.getEventDate()
                                + " | " + event.getEventTitle()
                                + " | Academic Year: " + event.getAcademicYear()
                                + " | Round: " + event.getRound();

                        // Keep the standard PDF font compatible with text.
                        line = line.replaceAll("[^\\x20-\\x7E]", "?");

                        content.beginText();
                        content.setFont(font, 9);
                        content.newLineAtOffset(50, y);
                        content.showText(line);
                        content.endText();

                        y -= 18;
                    }
                }
            }

            document.save(output);
            return output.toByteArray();
        }
    }
}
