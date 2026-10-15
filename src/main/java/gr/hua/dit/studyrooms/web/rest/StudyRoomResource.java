package gr.hua.dit.studyrooms.web.rest;

import gr.hua.dit.studyrooms.core.exception.NotFoundException;
import gr.hua.dit.studyrooms.core.service.ReservationService;
import gr.hua.dit.studyrooms.core.service.StudyRoomService;
import gr.hua.dit.studyrooms.core.service.model.SlotAvailability;
import gr.hua.dit.studyrooms.core.service.model.StudyRoomView;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

/**
 * REST controller for the {@code StudyRoom} resource.
 */
@RestController
@RequestMapping(value = "/api/v1/rooms", produces = MediaType.APPLICATION_JSON_VALUE)
public class StudyRoomResource {

    private final StudyRoomService studyRoomService;
    private final ReservationService reservationService;

    public StudyRoomResource(final StudyRoomService studyRoomService,
                             final ReservationService reservationService) {
        if (studyRoomService == null) throw new NullPointerException();
        if (reservationService == null) throw new NullPointerException();
        this.studyRoomService = studyRoomService;
        this.reservationService = reservationService;
    }

    @PreAuthorize("hasRole('INTEGRATION_READ')")
    @GetMapping("")
    public List<StudyRoomView> rooms() {
        return this.studyRoomService.getAllRooms();
    }

    @PreAuthorize("hasRole('INTEGRATION_READ')")
    @GetMapping("/{roomId}")
    public StudyRoomView room(@PathVariable final long roomId) {
        return this.studyRoomService.getRoom(roomId)
            .orElseThrow(() -> new NotFoundException("Study room " + roomId + " not found"));
    }

    @PreAuthorize("hasRole('INTEGRATION_READ')")
    @GetMapping("/{roomId}/availability")
    public List<SlotAvailability> availability(
        @PathVariable final long roomId,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) final LocalDate date
    ) {
        return this.reservationService.getAvailability(roomId, date);
    }
}
