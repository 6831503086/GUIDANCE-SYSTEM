package guidance_system.controller;

import guidance_system.dto.CalendarEventResponse;
import guidance_system.service.CalendarPdfService;
import guidance_system.service.CalendarService;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import org.springframework.http.ResponseEntity;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ContentDisposition;
import java.io.IOException;


@RestController
@RequestMapping("/api/calendar")
@CrossOrigin(origins = "*")
public class CalendarController {

    private final CalendarService service;
    private final CalendarPdfService pdfService;

    public CalendarController(CalendarService service, CalendarPdfService pdfService) {
        this.service = service;
        this.pdfService = pdfService;
    }

    // Teachers and admins can view calendar events
    @GetMapping("/events")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public List<CalendarEventResponse> getCalendarEvents(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer academicYear,
            @RequestParam(required = false) String round,
            @RequestParam(required = false) LocalDate date) {

        return service.getCalendarEvents(
                keyword, academicYear, round, date);
    }

    @GetMapping("/events/upcoming")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public List<CalendarEventResponse> getUpcomingEvents() {
        return service.getUpcomingEvents();
    }


    @GetMapping("/events/monthly")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public List<CalendarEventResponse> getMonthlyEvents(
            @RequestParam Integer year,
            @RequestParam Integer month) {

        return service.getMonthlyEvents(year, month);
    }


    @GetMapping("/events/{eventId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ResponseEntity<CalendarEventResponse> getEventById(
            @PathVariable String eventId) {

        CalendarEventResponse event = service.getEventById(eventId);

        if (event == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(event);
    }

    @GetMapping("/events/pdf")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ResponseEntity<byte[]> downloadCalendarPdf(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer academicYear,
            @RequestParam(required = false) String round,
            @RequestParam(required = false) LocalDate date)
            throws IOException {

        List<CalendarEventResponse> events =
                service.getCalendarEvents(keyword, academicYear, round, date);

        byte[] pdf = pdfService.generatePdf(events);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment()
                                .filename("application-schedule.pdf")
                                .build().toString())
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }



}
