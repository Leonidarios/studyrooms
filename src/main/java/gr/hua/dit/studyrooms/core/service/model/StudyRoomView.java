package gr.hua.dit.studyrooms.core.service.model;

import java.time.LocalTime;

/**
 * General view of {@link gr.hua.dit.studyrooms.core.model.StudyRoom} entity.
 */
public record StudyRoomView(
    long id,
    String name,
    String location,
    int capacity,
    LocalTime openingTime,
    LocalTime closingTime,
    boolean open
) {}
