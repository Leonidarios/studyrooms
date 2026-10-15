package gr.hua.dit.studyrooms.core.service.model;

import java.time.LocalTime;

/**
 * Occupancy of a room for one hourly slot of a day.
 */
public record SlotAvailability(
    LocalTime start,
    LocalTime end,
    int booked,
    int capacity
) {

    public int free() {
        return Math.max(0, this.capacity - this.booked);
    }

    public boolean full() {
        return this.booked >= this.capacity;
    }
}
