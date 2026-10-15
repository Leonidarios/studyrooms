package gr.hua.dit.studyrooms.core.service.impl;

import gr.hua.dit.studyrooms.core.exception.BusinessRuleException;
import gr.hua.dit.studyrooms.core.exception.NotFoundException;
import gr.hua.dit.studyrooms.core.model.StudyRoom;
import gr.hua.dit.studyrooms.core.repository.StudyRoomRepository;
import gr.hua.dit.studyrooms.core.service.ReservationService;
import gr.hua.dit.studyrooms.core.service.StudyRoomService;
import gr.hua.dit.studyrooms.core.service.mapper.StudyRoomMapper;
import gr.hua.dit.studyrooms.core.service.model.StudyRoomRequest;
import gr.hua.dit.studyrooms.core.service.model.StudyRoomView;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Default implementation of {@link StudyRoomService}.
 */
@Service
public class StudyRoomServiceImpl implements StudyRoomService {

    private static final Logger LOGGER = LoggerFactory.getLogger(StudyRoomServiceImpl.class);

    private final StudyRoomRepository studyRoomRepository;
    private final StudyRoomMapper studyRoomMapper;
    private final ReservationService reservationService;

    public StudyRoomServiceImpl(final StudyRoomRepository studyRoomRepository,
                                final StudyRoomMapper studyRoomMapper,
                                final ReservationService reservationService) {
        if (studyRoomRepository == null) throw new NullPointerException();
        if (studyRoomMapper == null) throw new NullPointerException();
        if (reservationService == null) throw new NullPointerException();
        this.studyRoomRepository = studyRoomRepository;
        this.studyRoomMapper = studyRoomMapper;
        this.reservationService = reservationService;
    }

    private StudyRoom requireRoom(final long roomId) {
        return this.studyRoomRepository.findById(roomId)
            .orElseThrow(() -> new NotFoundException("Study room " + roomId + " not found"));
    }

    private void checkRequest(final StudyRoomRequest request) {
        if (request == null) throw new NullPointerException();
        if (!request.openingTime().isBefore(request.closingTime())) {
            throw new BusinessRuleException("Opening time must be before closing time");
        }
    }

    @Transactional(readOnly = true)
    @Override
    public List<StudyRoomView> getAllRooms() {
        return this.studyRoomRepository.findAllByOrderByNameAsc()
            .stream()
            .map(this.studyRoomMapper::convertStudyRoomToStudyRoomView)
            .toList();
    }

    @Transactional(readOnly = true)
    @Override
    public Optional<StudyRoomView> getRoom(final long roomId) {
        return this.studyRoomRepository.findById(roomId)
            .map(this.studyRoomMapper::convertStudyRoomToStudyRoomView);
    }

    @Transactional
    @Override
    public StudyRoomView createRoom(final StudyRoomRequest request) {
        this.checkRequest(request);
        final String name = request.name().strip();
        if (this.studyRoomRepository.existsByNameIgnoreCase(name)) {
            throw new BusinessRuleException("A room named '" + name + "' already exists");
        }
        StudyRoom room = new StudyRoom(
            name,
            request.location() == null ? null : request.location().strip(),
            request.capacity(),
            request.openingTime(),
            request.closingTime()
        );
        room = this.studyRoomRepository.save(room);
        LOGGER.info("Created {}", room);
        return this.studyRoomMapper.convertStudyRoomToStudyRoomView(room);
    }

    @Transactional
    @Override
    public StudyRoomView updateRoom(final long roomId, final StudyRoomRequest request) {
        this.checkRequest(request);
        final StudyRoom room = this.requireRoom(roomId);
        final String name = request.name().strip();
        if (this.studyRoomRepository.existsByNameIgnoreCaseAndIdNot(name, roomId)) {
            throw new BusinessRuleException("A room named '" + name + "' already exists");
        }
        room.setName(name);
        room.setLocation(request.location() == null ? null : request.location().strip());
        room.setCapacity(request.capacity());
        room.setOpeningTime(request.openingTime());
        room.setClosingTime(request.closingTime());
        final StudyRoom saved = this.studyRoomRepository.save(room);
        LOGGER.info("Updated {}", saved);
        return this.studyRoomMapper.convertStudyRoomToStudyRoomView(saved);
    }

    @Transactional
    @Override
    public StudyRoomView closeRoom(final long roomId, final String reason) {
        final StudyRoom room = this.requireRoom(roomId);
        if (!room.isOpen()) {
            throw new BusinessRuleException("Room is already closed");
        }
        room.setOpen(false);
        final StudyRoom saved = this.studyRoomRepository.save(room);
        final String finalReason = (reason == null || reason.isBlank()) ? "Room closed by library staff" : reason.strip();
        final int cancelled = this.reservationService.cancelFutureReservationsOfRoom(roomId, finalReason);
        LOGGER.info("Closed room {} - {} future reservations cancelled", roomId, cancelled);
        return this.studyRoomMapper.convertStudyRoomToStudyRoomView(saved);
    }

    @Transactional
    @Override
    public StudyRoomView reopenRoom(final long roomId) {
        final StudyRoom room = this.requireRoom(roomId);
        room.setOpen(true);
        final StudyRoom saved = this.studyRoomRepository.save(room);
        return this.studyRoomMapper.convertStudyRoomToStudyRoomView(saved);
    }
}
