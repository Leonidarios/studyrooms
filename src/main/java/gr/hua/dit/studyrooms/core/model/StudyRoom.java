package gr.hua.dit.studyrooms.core.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalTime;

/**
 * StudyRoom entity.
 */
@Entity
@Table(
    name = "study_room",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_study_room_name", columnNames = "name")
    }
)
public class StudyRoom {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Column(name = "id")
    private Long id;

    @NotNull
    @NotBlank
    @Size(max = 100)
    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Size(max = 200)
    @Column(name = "location", length = 200)
    private String location;

    @NotNull
    @Min(1)
    @Max(100)
    @Column(name = "capacity", nullable = false)
    private Integer capacity;

    @NotNull
    @Column(name = "opening_time", nullable = false)
    private LocalTime openingTime;

    @NotNull
    @Column(name = "closing_time", nullable = false)
    private LocalTime closingTime;

    @Column(name = "is_open", nullable = false)
    private boolean open = true;

    public StudyRoom() {
    }

    public StudyRoom(String name, String location, Integer capacity, LocalTime openingTime, LocalTime closingTime) {
        this.name = name;
        this.location = location;
        this.capacity = capacity;
        this.openingTime = openingTime;
        this.closingTime = closingTime;
        this.open = true;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public Integer getCapacity() {
        return capacity;
    }

    public void setCapacity(Integer capacity) {
        this.capacity = capacity;
    }

    public LocalTime getOpeningTime() {
        return openingTime;
    }

    public void setOpeningTime(LocalTime openingTime) {
        this.openingTime = openingTime;
    }

    public LocalTime getClosingTime() {
        return closingTime;
    }

    public void setClosingTime(LocalTime closingTime) {
        this.closingTime = closingTime;
    }

    public boolean isOpen() {
        return open;
    }

    public void setOpen(boolean open) {
        this.open = open;
    }

    @Override
    public String toString() {
        return "StudyRoom{id=" + id + ", name='" + name + "', capacity=" + capacity
            + ", openingTime=" + openingTime + ", closingTime=" + closingTime + ", open=" + open + '}';
    }
}
