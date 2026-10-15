package gr.hua.dit.studyrooms.core.service;

import gr.hua.dit.studyrooms.core.service.model.StudyRoomRequest;
import gr.hua.dit.studyrooms.core.service.model.StudyRoomView;

import java.util.List;
import java.util.Optional;

/**
 * Service for managing study rooms.
 */
public interface StudyRoomService {

    List<StudyRoomView> getAllRooms();

    Optional<StudyRoomView> getRoom(final long roomId);

    StudyRoomView createRoom(final StudyRoomRequest request);

    StudyRoomView updateRoom(final long roomId, final StudyRoomRequest request);

    StudyRoomView closeRoom(final long roomId, final String reason);

    StudyRoomView reopenRoom(final long roomId);
}
