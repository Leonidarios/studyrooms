package gr.hua.dit.studyrooms.core.service.impl;

import gr.hua.dit.studyrooms.core.exception.BusinessRuleException;
import gr.hua.dit.studyrooms.core.exception.NotFoundException;
import gr.hua.dit.studyrooms.core.model.Person;
import gr.hua.dit.studyrooms.core.model.PersonType;
import gr.hua.dit.studyrooms.core.model.Reservation;
import gr.hua.dit.studyrooms.core.model.ReservationStatus;
import gr.hua.dit.studyrooms.core.model.StudyRoom;
import gr.hua.dit.studyrooms.core.port.SmsNotificationPort;
import gr.hua.dit.studyrooms.core.repository.PersonRepository;
import gr.hua.dit.studyrooms.core.repository.ReservationRepository;
import gr.hua.dit.studyrooms.core.repository.StudyRoomRepository;
import gr.hua.dit.studyrooms.core.service.ReservationService;
import gr.hua.dit.studyrooms.core.service.mapper.ReservationMapper;
import gr.hua.dit.studyrooms.core.service.model.CreateReservationRequest;
import gr.hua.dit.studyrooms.core.service.model.ReservationView;
import gr.hua.dit.studyrooms.core.service.model.SlotAvailability;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Default implementation of {@link ReservationService}.
 */
@Service
public class ReservationServiceImpl implements ReservationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReservationServiceImpl.class);

    static final int MAX_ACTIVE_PER_DAY = 2;
    static final int MAX_DURATION_HOURS = 3;
    static final int MAX_DAYS_AHEAD = 14;

    private final ReservationRepository reservationRepository;
    private final StudyRoomRepository studyRoomRepository;
    private final PersonRepository personRepository;
    private final ReservationMapper reservationMapper;
    private final SmsNotificationPort smsNotificationPort;

    public ReservationServiceImpl(final ReservationRepository reservationRepository,
                                  final StudyRoomRepository studyRoomRepository,
                                  final PersonRepository personRepository,
                                  final ReservationMapper reservationMapper,
                                  final SmsNotificationPort smsNotificationPort) {
        if (reservationRepository == null) throw new NullPointerException();
        if (studyRoomRepository == null) throw new NullPointerException();
        if (personRepository == null) throw new NullPointerException();
        if (reservationMapper == null) throw new NullPointerException();
        if (smsNotificationPort == null) throw new NullPointerException();
        this.reservationRepository = reservationRepository;
        this.studyRoomRepository = studyRoomRepository;
        this.personRepository = personRepository;
        this.reservationMapper = reservationMapper;
        this.smsNotificationPort = smsNotificationPort;
    }

    private List<ReservationView> toViews(final List<Reservation> reservations) {
        return reservations.stream()
            .map(this.reservationMapper::convertReservationToReservationView)
            .toList();
    }

    private static boolean isInFuture(final Reservation reservation) {
        final LocalDate today = LocalDate.now();
        if (reservation.getReservationDate().isAfter(today)) return true;
        if (reservation.getReservationDate().isBefore(today)) return false;
        return reservation.getStartTime().isAfter(LocalTime.now());
    }

    private void notifyStudent(final Person student, final String content) {
        final String e164 = student.getMobilePhoneNumber();
        if (e164 == null || e164.isBlank()) return;
        try {
            final boolean sent = this.smsNotificationPort.sendSms(e164, content);
            if (!sent) {
                LOGGER.warn("SMS to {} was not sent", e164);
            }
        } catch (Exception e) {
            LOGGER.warn("SMS to {} failed: {}", e164, e.getMessage());
        }
    }

    private static String describe(final Reservation r) {
        return String.format("#%d (%s, %s %s-%s)",
            r.getId(), r.getRoom().getName(), r.getReservationDate(), r.getStartTime(), r.getEndTime());
    }

    @Transactional(readOnly = true)
    @Override
    public List<ReservationView> getAllReservations(final LocalDate date) {
        if (date == null) {
            return this.toViews(this.reservationRepository.findAllByOrderByReservationDateDescStartTimeDesc());
        }
        return this.toViews(this.reservationRepository.findAllByReservationDateOrderByStartTimeAsc(date));
    }

    @Transactional(readOnly = true)
    @Override
    public List<ReservationView> getReservationsOfStudent(final long studentId) {
        return this.toViews(this.reservationRepository.findAllByStudentIdOrderByReservationDateDescStartTimeDesc(studentId));
    }

    @Transactional(readOnly = true)
    @Override
    public Optional<ReservationView> getReservation(final long reservationId) {
        return this.reservationRepository.findById(reservationId)
            .map(this.reservationMapper::convertReservationToReservationView);
    }

    @Transactional(readOnly = true)
    @Override
    public List<SlotAvailability> getAvailability(final long roomId, final LocalDate date) {
        if (date == null) throw new NullPointerException();
        final StudyRoom room = this.studyRoomRepository.findById(roomId)
            .orElseThrow(() -> new NotFoundException("Study room " + roomId + " not found"));

        final List<Reservation> active = this.reservationRepository
            .findAllByRoomIdAndReservationDateAndStatusOrderByStartTimeAsc(roomId, date, ReservationStatus.ACTIVE);

        final List<SlotAvailability> slots = new ArrayList<>();
        LocalTime slotStart = room.getOpeningTime();
        while (slotStart.isBefore(room.getClosingTime())) {
            LocalTime slotEnd = slotStart.plusHours(1);
            if (slotEnd.isAfter(room.getClosingTime()) || slotEnd.isBefore(slotStart)) {
                slotEnd = room.getClosingTime();
            }
            final LocalTime s = slotStart;
            final LocalTime e = slotEnd;
            final int booked = (int) active.stream()
                .filter(r -> r.getStartTime().isBefore(e) && r.getEndTime().isAfter(s))
                .count();
            slots.add(new SlotAvailability(s, e, booked, room.getCapacity()));
            slotStart = slotEnd;
        }
        return slots;
    }

    @Transactional
    @Override
    public ReservationView createReservation(final CreateReservationRequest request, final boolean notify) {
        if (request == null) throw new NullPointerException();

        final LocalDate date = request.date();
        final LocalTime start = request.startTime();
        final LocalTime end = request.endTime();

        final Person student = this.personRepository.findById(request.studentId())
            .orElseThrow(() -> new NotFoundException("Student " + request.studentId() + " not found"));
        if (student.getType() != PersonType.STUDENT) {
            throw new BusinessRuleException("Only students can make reservations");
        }

        final StudyRoom room = this.studyRoomRepository.findById(request.roomId())
            .orElseThrow(() -> new NotFoundException("Study room " + request.roomId() + " not found"));
        if (!room.isOpen()) {
            throw new BusinessRuleException("Room '" + room.getName() + "' is currently closed");
        }

        final LocalDate today = LocalDate.now();
        if (date.isBefore(today) || (date.equals(today) && !start.isAfter(LocalTime.now()))) {
            throw new BusinessRuleException("You cannot book a slot in the past");
        }
        if (date.isAfter(today.plusDays(MAX_DAYS_AHEAD))) {
            throw new BusinessRuleException("You can book at most " + MAX_DAYS_AHEAD + " days ahead");
        }

        if (!start.isBefore(end)) {
            throw new BusinessRuleException("Start time must be before end time");
        }
        if (Duration.between(start, end).compareTo(Duration.ofHours(MAX_DURATION_HOURS)) > 0) {
            throw new BusinessRuleException("A reservation can last at most " + MAX_DURATION_HOURS + " hours");
        }

        if (start.isBefore(room.getOpeningTime()) || end.isAfter(room.getClosingTime())) {
            throw new BusinessRuleException("Room '" + room.getName() + "' is open "
                + room.getOpeningTime() + "-" + room.getClosingTime());
        }

        final long activeThatDay = this.reservationRepository
            .countByStudentIdAndReservationDateAndStatus(student.getId(), date, ReservationStatus.ACTIVE);
        if (activeThatDay >= MAX_ACTIVE_PER_DAY) {
            throw new BusinessRuleException("You already have " + MAX_ACTIVE_PER_DAY + " active reservations on " + date);
        }

        if (this.reservationRepository.countOverlappingForStudent(student.getId(), date, start, end) > 0) {
            throw new BusinessRuleException("You already have a reservation that overlaps with this time");
        }

        final long overlapping = this.reservationRepository.countOverlappingForRoom(room.getId(), date, start, end);
        if (overlapping >= room.getCapacity()) {
            throw new BusinessRuleException("Room '" + room.getName() + "' is full for this time slot");
        }

        Reservation reservation = new Reservation();
        reservation.setStudent(student);
        reservation.setRoom(room);
        reservation.setReservationDate(date);
        reservation.setStartTime(start);
        reservation.setEndTime(end);
        reservation.setStatus(ReservationStatus.ACTIVE);
        reservation = this.reservationRepository.save(reservation);
        LOGGER.info("Created reservation {}", describe(reservation));

        if (notify) {
            this.notifyStudent(student, "StudyRooms: reservation " + describe(reservation) + " confirmed.");
        }

        return this.reservationMapper.convertReservationToReservationView(reservation);
    }

    @Transactional
    @Override
    public ReservationView cancelReservation(final long reservationId, final Long actingStudentId, final String reason) {
        final Reservation reservation = this.reservationRepository.findById(reservationId)
            .orElseThrow(() -> new NotFoundException("Reservation " + reservationId + " not found"));

        if (actingStudentId != null && reservation.getStudent().getId() != actingStudentId.longValue()) {
            throw new SecurityException("You can only cancel your own reservations");
        }

        if (reservation.getStatus() != ReservationStatus.ACTIVE) {
            throw new BusinessRuleException("Reservation is already cancelled");
        }
        if (!isInFuture(reservation)) {
            throw new BusinessRuleException("A reservation that has already started cannot be cancelled");
        }

        reservation.setStatus(ReservationStatus.CANCELLED);
        reservation.setCancelledAt(Instant.now());
        final String finalReason = (reason == null || reason.isBlank())
            ? (actingStudentId != null ? "Cancelled by student" : "Cancelled by library staff")
            : reason.strip();
        reservation.setCancelReason(finalReason);
        final Reservation saved = this.reservationRepository.save(reservation);
        LOGGER.info("Cancelled reservation {} ({})", describe(saved), finalReason);

        this.notifyStudent(saved.getStudent(),
            "StudyRooms: reservation " + describe(saved) + " cancelled. " + finalReason);

        return this.reservationMapper.convertReservationToReservationView(saved);
    }

    @Transactional
    @Override
    public int cancelFutureReservationsOfRoom(final long roomId, final String reason) {
        final List<Reservation> candidates = this.reservationRepository
            .findAllByRoomIdAndReservationDateGreaterThanEqualAndStatus(roomId, LocalDate.now(), ReservationStatus.ACTIVE);
        int count = 0;
        for (final Reservation reservation : candidates) {
            if (!isInFuture(reservation)) continue;
            reservation.setStatus(ReservationStatus.CANCELLED);
            reservation.setCancelledAt(Instant.now());
            reservation.setCancelReason(reason);
            this.reservationRepository.save(reservation);
            this.notifyStudent(reservation.getStudent(),
                "StudyRooms: reservation " + describe(reservation) + " cancelled. " + reason);
            count++;
        }
        return count;
    }
}
