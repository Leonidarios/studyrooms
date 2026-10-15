package gr.hua.dit.studyrooms.core.service.model;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * DTO for requesting a new reservation.
 */
public record CreateReservationRequest(
    @NotNull @Positive Long studentId,
    @NotNull @Positive Long roomId,
    @NotNull LocalDate date,
    @NotNull LocalTime startTime,
    @NotNull LocalTime endTime
) {}
