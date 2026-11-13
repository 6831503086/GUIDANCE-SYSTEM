package guidance_system.service;

import guidance_system.dto.CalendarEventResponse;
import guidance_system.entity.EntryGroup;
import guidance_system.entity.Post;
import guidance_system.repository.EntryGroupRepository;
import guidance_system.repository.PostRepository;

import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class CalendarService {

    private final EntryGroupRepository repository;
    private final PostRepository postRepository;

    public CalendarService(
            EntryGroupRepository repository,
            PostRepository postRepository) {

        this.repository = repository;
        this.postRepository = postRepository;
    }

    public List<CalendarEventResponse> getCalendarEvents(
            String keyword,
            Integer academicYear,
            String round,
            LocalDate date) {

        List<CalendarEventResponse> events = new ArrayList<>();

        // Existing application schedule events
        List<EntryGroup> entryGroups = repository.findAll();

        for (EntryGroup group : entryGroups) {

            addEvent(events, group, "APPLICATION_OPEN",
                    "Application Opens", group.getApplyStartDate());

            addEvent(events, group, "APPLICATION_CLOSE",
                    "Application Closes", group.getApplyEndDate());

            addEvent(events, group, "RESULT_ANNOUNCEMENT",
                    "Results Announced", group.getResultAnnounceDate());

            addEvent(events, group, "RIGHTS_CONFIRMATION",
                    "Rights Confirmation", group.getRightsConfirmDate());
        }

        // Published announcement events
        LocalDate today = LocalDate.now();

        List<Post> posts = postRepository.findAll();

        for (Post post : posts) {

            if (post.getPublishedAt() == null
                    || Boolean.TRUE.equals(post.getIsHidden())
                    || post.getEventType() == null
                    || post.getEventStartDate() == null) {
                continue;
            }

            if (post.getVisibleStartDate() != null
                    && post.getVisibleStartDate().isAfter(today)) {
                continue;
            }

            if (post.getVisibleEndDate() != null
                    && post.getVisibleEndDate().isBefore(today)) {
                continue;
            }

            CalendarEventResponse event = new CalendarEventResponse(
                    "POST-" + post.getPostId(),
                    post.getTitle(),
                    post.getEventType(),
                    post.getEventStartDate(),
                    post.getAcademicYear(),
                    post.getApplicationRound()
            );

            event.setEventStartDate(post.getEventStartDate());
            event.setEventEndDate(
                    post.getEventEndDate() == null
                            ? post.getEventStartDate()
                            : post.getEventEndDate()
            );
            event.setSourcePostId(post.getPostId());

            events.add(event);
        }

        return events.stream()
                .filter(event -> academicYear == null
                        || academicYear.equals(event.getAcademicYear()))
                .filter(event -> round == null || round.isBlank()
                        || (event.getRound() != null
                        && event.getRound().equalsIgnoreCase(round.trim())))
                .filter(event -> keyword == null || keyword.isBlank()
                        || event.getEventTitle().toLowerCase()
                        .contains(keyword.trim().toLowerCase())
                        || (event.getRound() != null
                        && event.getRound().toLowerCase()
                        .contains(keyword.trim().toLowerCase())))
                .filter(event -> date == null
                        || (!date.isBefore(event.getEventStartDate())
                        && !date.isAfter(event.getEventEndDate())))
                .sorted(Comparator.comparing(
                        CalendarEventResponse::getEventDate))
                .toList();
    }

    private void addEvent(
            List<CalendarEventResponse> events,
            EntryGroup group,
            String eventType,
            String eventTitle,
            LocalDate eventDate) {

        if (eventDate == null) {
            return;
        }

        events.add(new CalendarEventResponse(
                group.getEntrygroupId() + "-" + eventType,
                eventTitle,
                eventType,
                eventDate,
                group.getAcademicYear(),
                group.getEntrygroupName()
        ));
    }

    public List<CalendarEventResponse> getUpcomingEvents() {

        LocalDate today = LocalDate.now();

        return getCalendarEvents(null, null, null, null)
                .stream()
                .filter(event -> !event.getEventEndDate().isBefore(today))
                .toList();
    }

    public List<CalendarEventResponse> getMonthlyEvents(
            Integer year,
            Integer month) {

        LocalDate firstDay = LocalDate.of(year, month, 1);
        LocalDate lastDay = firstDay.withDayOfMonth(
                firstDay.lengthOfMonth());

        return getCalendarEvents(null, null, null, null)
                .stream()
                .filter(event ->
                        !event.getEventStartDate().isAfter(lastDay)
                        && !event.getEventEndDate().isBefore(firstDay))
                .toList();
    }

    public CalendarEventResponse getEventById(String eventId) {

        return getCalendarEvents(null, null, null, null)
                .stream()
                .filter(event -> event.getEventId().equals(eventId))
                .findFirst()
                .orElse(null);
    }
}
