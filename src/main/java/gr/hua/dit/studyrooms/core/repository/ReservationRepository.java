package gr.hua.dit.studyrooms.core.repository;

import gr.hua.dit.studyrooms.core.model.Reservation;
import gr.hua.dit.studyrooms.core.model.ReservationStatus;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * Repository for {@link Reservation} entity.
 */
@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    List<Reservation> findAllByOrderByReservationDateDescStartTimeDesc();

    List<Reservation> findAllByStudentIdOrderByReservationDateDescStartTimeDesc(final long studentId);

    List<Reservation> findAllByReservationDateOrderByStartTimeAsc(final LocalDate reservationDate);

    List<Reservation> findAllByReservationDateAndStatus(final LocalDate reservationDate, final ReservationStatus status);

    List<Reservation> findAllByRoomIdAndReservationDateAndStatusOrderByStartTimeAsc(final long roomId,
                                                                                   final LocalDate reservationDate,
                                                                                   final ReservationStatus status);

    List<Reservation> findAllByRoomIdAndReservationDateGreaterThanEqualAndStatus(final long roomId,
                                                                                final LocalDate fromDate,
                                                                                final ReservationStatus status);

    long countByStudentIdAndReservationDateAndStatus(final long studentId,
                                                     final LocalDate reservationDate,
                                                     final ReservationStatus status);

    @Query("""
        select count(r) from Reservation r
        where r.room.id = :roomId
          and r.reservationDate = :date
          and r.status = gr.hua.dit.studyrooms.core.model.ReservationStatus.ACTIVE
          and r.startTime < :endTime
          and r.endTime > :startTime
        """)
    long countOverlappingForRoom(@Param("roomId") long roomId,
                                 @Param("date") LocalDate date,
                                 @Param("startTime") LocalTime startTime,
                                 @Param("endTime") LocalTime endTime);

    @Query("""
        select count(r) from Reservation r
        where r.student.id = :studentId
          and r.reservationDate = :date
          and r.status = gr.hua.dit.studyrooms.core.model.ReservationStatus.ACTIVE
          and r.startTime < :endTime
          and r.endTime > :startTime
        """)
    long countOverlappingForStudent(@Param("studentId") long studentId,
                                    @Param("date") LocalDate date,
                                    @Param("startTime") LocalTime startTime,
                                    @Param("endTime") LocalTime endTime);
}
