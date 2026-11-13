package guidance_system.dto;

import java.time.LocalDate;

public class CalendarEventResponse {

    private String eventId;
    private String eventTitle;
    private String eventType;
    private LocalDate eventDate;
    private LocalDate eventStartDate;
    private LocalDate eventEndDate;
    private Integer academicYear;
    private String round;
    private Long sourcePostId;

    public CalendarEventResponse() {
    }

    public CalendarEventResponse(
            String eventId,
            String eventTitle,
            String eventType,
            LocalDate eventDate,
            Integer academicYear,
            String round) {

        this.eventId = eventId;
        this.eventTitle = eventTitle;
        this.eventType = eventType;
        this.eventDate = eventDate;
        this.eventStartDate = eventDate;
        this.eventEndDate = eventDate;
        this.academicYear = academicYear;
        this.round = round;
    }

    public String getEventId() {
        return eventId;
    }

    public String getEventTitle() {
        return eventTitle;
    }

    public String getEventType() {
        return eventType;
    }

    public LocalDate getEventDate() {
        return eventDate;
    }

    public LocalDate getEventStartDate() {
        return eventStartDate;
    }

    public LocalDate getEventEndDate() {
        return eventEndDate;
    }

    public Integer getAcademicYear() {
        return academicYear;
    }

    public String getRound() {
        return round;
    }

    public Long getSourcePostId() {
        return sourcePostId;
    }

    public void setEventStartDate(LocalDate eventStartDate) {
        this.eventStartDate = eventStartDate;
    }

    public void setEventEndDate(LocalDate eventEndDate) {
        this.eventEndDate = eventEndDate;
    }

    public void setSourcePostId(Long sourcePostId) {
        this.sourcePostId = sourcePostId;
    }
}
