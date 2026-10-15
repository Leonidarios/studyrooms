package gr.hua.dit.studyrooms.web.ui.model;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalTime;

public record StudyRoomForm(
    @NotNull @NotBlank @Size(max = 100) String name,
    @Size(max = 200) String location,
    @NotNull @Min(1) @Max(100) Integer capacity,
    @NotNull @DateTimeFormat(pattern = "HH:mm") LocalTime openingTime,
    @NotNull @DateTimeFormat(pattern = "HH:mm") LocalTime closingTime
) {}
