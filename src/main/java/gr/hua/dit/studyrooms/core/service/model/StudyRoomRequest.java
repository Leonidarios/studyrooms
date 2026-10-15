package gr.hua.dit.studyrooms.core.service.model;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalTime;

/**
 * DTO for creating or updating a study room.
 */
public record StudyRoomRequest(
    @NotNull @NotBlank @Size(max = 100) String name,
    @Size(max = 200) String location,
    @NotNull @Min(1) @Max(100) Integer capacity,
    @NotNull LocalTime openingTime,
    @NotNull LocalTime closingTime
) {}
