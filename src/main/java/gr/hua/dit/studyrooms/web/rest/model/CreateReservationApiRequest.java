package gr.hua.dit.studyrooms.web.rest.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * DTO for creating a reservation via the REST API.
 */
public record CreateReservationApiRequest(
    @NotNull @NotBlank String studentHuaId,
    @NotNull @Positive Long roomId,
    @NotNull LocalDate date,
    @NotNull LocalTime startTime,
    @NotNull LocalTime endTime
) {}
