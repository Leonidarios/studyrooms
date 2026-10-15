package gr.hua.dit.studyrooms.core.service;

import gr.hua.dit.studyrooms.core.service.model.CreateReservationRequest;
import gr.hua.dit.studyrooms.core.service.model.ReservationView;
import gr.hua.dit.studyrooms.core.service.model.SlotAvailability;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Service for managing reservations.
 */
public interface ReservationService {

    List<ReservationView> getAllReservations(final LocalDate date);

    List<ReservationView> getReservationsOfStudent(final long studentId);

    Optional<ReservationView> getReservation(final long reservationId);

    List<SlotAvailability> getAvailability(final long roomId, final LocalDate date);

    ReservationView createReservation(final CreateReservationRequest request, final boolean notify);

    default ReservationView createReservation(final CreateReservationRequest request) {
        return this.createReservation(request, true);
    }

    ReservationView cancelReservation(final long reservationId, final Long actingStudentId, final String reason);

    int cancelFutureReservationsOfRoom(final long roomId, final String reason);
}
