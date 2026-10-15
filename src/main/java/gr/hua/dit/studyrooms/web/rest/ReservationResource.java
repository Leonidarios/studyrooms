package gr.hua.dit.studyrooms.web.rest;

import gr.hua.dit.studyrooms.core.exception.NotFoundException;
import gr.hua.dit.studyrooms.core.service.PersonDataService;
import gr.hua.dit.studyrooms.core.service.ReservationService;
import gr.hua.dit.studyrooms.core.service.model.CreateReservationRequest;
import gr.hua.dit.studyrooms.core.service.model.PersonView;
import gr.hua.dit.studyrooms.core.service.model.ReservationView;
import gr.hua.dit.studyrooms.web.rest.model.CreateReservationApiRequest;

import jakarta.validation.Valid;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

/**
 * REST controller for the {@code Reservation} resource.
 */
@RestController
@RequestMapping(value = "/api/v1/reservations", produces = MediaType.APPLICATION_JSON_VALUE)
public class ReservationResource {

    private final ReservationService reservationService;
    private final PersonDataService personDataService;

    public ReservationResource(final ReservationService reservationService,
                               final PersonDataService personDataService) {
        if (reservationService == null) throw new NullPointerException();
        if (personDataService == null) throw new NullPointerException();
        this.reservationService = reservationService;
        this.personDataService = personDataService;
    }

    @PreAuthorize("hasRole('INTEGRATION_READ')")
    @GetMapping("")
    public List<ReservationView> reservations(
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) final LocalDate date
    ) {
        return this.reservationService.getAllReservations(date);
    }

    @PreAuthorize("hasRole('INTEGRATION_READ')")
    @GetMapping("/{reservationId}")
    public ReservationView reservation(@PathVariable final long reservationId) {
        return this.reservationService.getReservation(reservationId)
            .orElseThrow(() -> new NotFoundException("Reservation " + reservationId + " not found"));
    }

    @PreAuthorize("hasRole('INTEGRATION_WRITE')")
    @PostMapping(value = "", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ReservationView> create(@RequestBody @Valid final CreateReservationApiRequest body) {
        final PersonView student = this.personDataService.getPersonByHuaId(body.studentHuaId())
            .orElseThrow(() -> new NotFoundException("Student " + body.studentHuaId() + " not found"));
        final CreateReservationRequest request = new CreateReservationRequest(
            student.id(),
            body.roomId(),
            body.date(),
            body.startTime(),
            body.endTime()
        );
        final ReservationView created = this.reservationService.createReservation(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PreAuthorize("hasRole('INTEGRATION_WRITE')")
    @DeleteMapping("/{reservationId}")
    public ReservationView cancel(@PathVariable final long reservationId,
                                  @RequestParam(required = false) final String reason) {
        return this.reservationService.cancelReservation(reservationId, null, reason);
    }
}
