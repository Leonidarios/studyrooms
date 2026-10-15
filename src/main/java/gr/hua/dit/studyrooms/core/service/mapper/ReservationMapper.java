package gr.hua.dit.studyrooms.core.service.mapper;

import gr.hua.dit.studyrooms.core.model.Reservation;
import gr.hua.dit.studyrooms.core.service.model.ReservationView;

import org.springframework.stereotype.Component;

/**
 * Mapper to convert {@link Reservation} to {@link ReservationView}.
 */
@Component
public class ReservationMapper {

    private final PersonMapper personMapper;
    private final StudyRoomMapper studyRoomMapper;

    public ReservationMapper(final PersonMapper personMapper, final StudyRoomMapper studyRoomMapper) {
        if (personMapper == null) throw new NullPointerException();
        if (studyRoomMapper == null) throw new NullPointerException();
        this.personMapper = personMapper;
        this.studyRoomMapper = studyRoomMapper;
    }

    public ReservationView convertReservationToReservationView(final Reservation reservation) {
        if (reservation == null) {
            return null;
        }
        return new ReservationView(
            reservation.getId(),
            this.personMapper.convertPersonToPersonView(reservation.getStudent()),
            this.studyRoomMapper.convertStudyRoomToStudyRoomView(reservation.getRoom()),
            reservation.getReservationDate(),
            reservation.getStartTime(),
            reservation.getEndTime(),
            reservation.getStatus(),
            reservation.getCreatedAt(),
            reservation.getCancelledAt(),
            reservation.getCancelReason()
        );
    }
}
