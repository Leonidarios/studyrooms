package gr.hua.dit.studyrooms.core.service;

import gr.hua.dit.studyrooms.core.model.Client;
import gr.hua.dit.studyrooms.core.model.PersonType;
import gr.hua.dit.studyrooms.core.model.StudyRoom;
import gr.hua.dit.studyrooms.core.repository.ClientRepository;
import gr.hua.dit.studyrooms.core.repository.StudyRoomRepository;
import gr.hua.dit.studyrooms.core.service.model.CreatePersonRequest;
import gr.hua.dit.studyrooms.core.service.model.CreatePersonResult;
import gr.hua.dit.studyrooms.core.service.model.CreateReservationRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Initializes application.
 */
@Service
public class InitializationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(InitializationService.class);

    private final ClientRepository clientRepository;
    private final StudyRoomRepository studyRoomRepository;
    private final PersonBusinessLogicService personBusinessLogicService;
    private final ReservationService reservationService;
    private final AtomicBoolean initialized;

    public InitializationService(final ClientRepository clientRepository,
                                 final StudyRoomRepository studyRoomRepository,
                                 final PersonBusinessLogicService personBusinessLogicService,
                                 final ReservationService reservationService) {
        if (clientRepository == null) throw new NullPointerException();
        if (studyRoomRepository == null) throw new NullPointerException();
        if (personBusinessLogicService == null) throw new NullPointerException();
        if (reservationService == null) throw new NullPointerException();
        this.clientRepository = clientRepository;
        this.studyRoomRepository = studyRoomRepository;
        this.personBusinessLogicService = personBusinessLogicService;
        this.reservationService = reservationService;
        this.initialized = new AtomicBoolean(false);
    }

    @EventListener(ApplicationReadyEvent.class)
    public void populateDatabaseWithInitialData() {
        final boolean alreadyInitialized = this.initialized.getAndSet(true);
        if (alreadyInitialized) {
            LOGGER.warn("Database initialization skipped: initial data has already been populated.");
            return;
        }
        LOGGER.info("Starting database initialization with initial data...");

        this.clientRepository.saveAll(List.of(
            new Client(null, "client01", "s3cr3t", "INTEGRATION_READ,INTEGRATION_WRITE"),
            new Client(null, "client02", "s3cr3t", "INTEGRATION_READ")
        ));

        final List<StudyRoom> rooms = this.studyRoomRepository.saveAll(List.of(
            new StudyRoom("Room A1 (silent)", "Library, 1st floor", 4, LocalTime.of(8, 0), LocalTime.of(20, 0)),
            new StudyRoom("Room B2 (group)", "Library, 2nd floor", 8, LocalTime.of(9, 0), LocalTime.of(21, 0)),
            new StudyRoom("Reading Hall", "Library, ground floor", 30, LocalTime.of(8, 0), LocalTime.of(22, 0))
        ));

        final List<CreatePersonRequest> people = List.of(
            new CreatePersonRequest(PersonType.STAFF, "s0001", "Library", "Staff",
                "s0001@hua.gr", "+306900000010", "1234"),
            new CreatePersonRequest(PersonType.STUDENT, "it2023001", "Test 1", "Student",
                "it2023001@hua.gr", "+306900000001", "1234"),
            new CreatePersonRequest(PersonType.STUDENT, "it2023002", "Test 2", "Student",
                "it2023002@hua.gr", "+306900000002", "1234")
        );
        Long firstStudentId = null;
        for (final CreatePersonRequest request : people) {
            final CreatePersonResult result = this.personBusinessLogicService.createPerson(request, false);
            if (result.created()) {
                if (firstStudentId == null && request.type() == PersonType.STUDENT) {
                    firstStudentId = result.personView().id();
                }
            } else {
                LOGGER.error("Could not create demo user {}: {} (is NOC running on port 8081?)",
                    request.huaId(), result.reason());
            }
        }

        if (firstStudentId != null) {
            try {
                this.reservationService.createReservation(new CreateReservationRequest(
                    firstStudentId,
                    rooms.get(0).getId(),
                    LocalDate.now().plusDays(1),
                    LocalTime.of(10, 0),
                    LocalTime.of(12, 0)
                ), false);
            } catch (Exception e) {
                LOGGER.warn("Could not create demo reservation: {}", e.getMessage());
            }
        }

        LOGGER.info("Database initialization completed.");
    }
}
