package gr.hua.dit.studyrooms.core.service.mapper;

import gr.hua.dit.studyrooms.core.model.StudyRoom;
import gr.hua.dit.studyrooms.core.service.model.StudyRoomView;

import org.springframework.stereotype.Component;

/**
 * Mapper to convert {@link StudyRoom} to {@link StudyRoomView}.
 */
@Component
public class StudyRoomMapper {

    public StudyRoomView convertStudyRoomToStudyRoomView(final StudyRoom room) {
        if (room == null) {
            return null;
        }
        return new StudyRoomView(
            room.getId(),
            room.getName(),
            room.getLocation(),
            room.getCapacity(),
            room.getOpeningTime(),
            room.getClosingTime(),
            room.isOpen()
        );
    }
}
