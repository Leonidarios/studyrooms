package gr.hua.dit.studyrooms.core.service;

import gr.hua.dit.studyrooms.core.model.Reservation;
import gr.hua.dit.studyrooms.core.model.ReservationStatus;
import gr.hua.dit.studyrooms.core.port.SmsNotificationPort;
import gr.hua.dit.studyrooms.core.repository.ReservationRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * Service for managing reservation reminders.
 */
@Service
public class ReservationReminderService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReservationReminderService.class);

    private final ReservationRepository reservationRepository;
    private final SmsNotificationPort smsNotificationPort;

    public ReservationReminderService(final ReservationRepository reservationRepository,
                                      final SmsNotificationPort smsNotificationPort) {
        if (reservationRepository == null) throw new NullPointerException();
        if (smsNotificationPort == null) throw new NullPointerException();
        this.reservationRepository = reservationRepository;
        this.smsNotificationPort = smsNotificationPort;
    }

    @Scheduled(cron = "0 0 8 * * *") // every day at 08:00
    @Transactional(readOnly = true)
    public void remindStudentsOfTodaysReservations() {
        final List<Reservation> reservations = this.reservationRepository
            .findAllByReservationDateAndStatus(LocalDate.now(), ReservationStatus.ACTIVE);
        LOGGER.info("Sending reminders for {} reservations today", reservations.size());
        for (final Reservation r : reservations) {
            final String content = String.format("StudyRooms reminder: today %s-%s in %s.",
                r.getStartTime(), r.getEndTime(), r.getRoom().getName());
            this.smsNotificationPort.sendSms(r.getStudent().getMobilePhoneNumber(), content);
        }
    }
}
