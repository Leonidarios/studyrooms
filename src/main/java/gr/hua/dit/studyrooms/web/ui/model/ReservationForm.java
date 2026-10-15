package gr.hua.dit.studyrooms.web.ui.model;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.time.LocalTime;

public record ReservationForm(
    @NotNull @Positive Long roomId,
    @NotNull @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date,
    @NotNull @DateTimeFormat(pattern = "HH:mm") LocalTime startTime,
    @NotNull @DateTimeFormat(pattern = "HH:mm") LocalTime endTime
) {}
