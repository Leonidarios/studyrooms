package gr.hua.dit.studyrooms.web.ui;

import gr.hua.dit.studyrooms.core.exception.BusinessRuleException;
import gr.hua.dit.studyrooms.core.service.ReservationService;
import gr.hua.dit.studyrooms.core.service.StudyRoomService;
import gr.hua.dit.studyrooms.core.service.model.StudyRoomRequest;
import gr.hua.dit.studyrooms.core.service.model.StudyRoomView;
import gr.hua.dit.studyrooms.web.ui.model.StudyRoomForm;

import jakarta.validation.Valid;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatusCode;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;

/**
 * UI controller for managing study rooms.
 */
@Controller
@RequestMapping("/rooms")
public class StudyRoomController {

    private final StudyRoomService studyRoomService;
    private final ReservationService reservationService;

    public StudyRoomController(final StudyRoomService studyRoomService,
                               final ReservationService reservationService) {
        if (studyRoomService == null) throw new NullPointerException();
        if (reservationService == null) throw new NullPointerException();
        this.studyRoomService = studyRoomService;
        this.reservationService = reservationService;
    }

    private static StudyRoomRequest toRequest(final StudyRoomForm form) {
        return new StudyRoomRequest(form.name(), form.location(), form.capacity(), form.openingTime(), form.closingTime());
    }

    @GetMapping("")
    public String list(final Model model) {
        model.addAttribute("rooms", this.studyRoomService.getAllRooms());
        return "rooms";
    }

    @GetMapping("/{roomId}")
    public String detail(
        @PathVariable final long roomId,
        @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date,
        final Model model
    ) {
        final StudyRoomView room = this.studyRoomService.getRoom(roomId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatusCode.valueOf(404), "Room not found"));
        if (date == null) date = LocalDate.now();
        model.addAttribute("room", room);
        model.addAttribute("date", date);
        model.addAttribute("slots", this.reservationService.getAvailability(roomId, date));
        return "room";
    }

    @PreAuthorize("hasRole('STAFF')")
    @GetMapping("/new")
    public String showCreateForm(final Model model) {
        model.addAttribute("form", new StudyRoomForm("", "", 4, null, null));
        model.addAttribute("isEdit", false);
        return "room_form";
    }

    @PreAuthorize("hasRole('STAFF')")
    @PostMapping("/new")
    public String handleCreateForm(
        @ModelAttribute("form") @Valid final StudyRoomForm form,
        final BindingResult bindingResult,
        final Model model
    ) {
        model.addAttribute("isEdit", false);
        if (bindingResult.hasErrors()) {
            return "room_form";
        }
        try {
            final StudyRoomView room = this.studyRoomService.createRoom(toRequest(form));
            return "redirect:/rooms/" + room.id();
        } catch (BusinessRuleException e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "room_form";
        }
    }

    @PreAuthorize("hasRole('STAFF')")
    @GetMapping("/{roomId}/edit")
    public String showEditForm(@PathVariable final long roomId, final Model model) {
        final StudyRoomView room = this.studyRoomService.getRoom(roomId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatusCode.valueOf(404), "Room not found"));
        model.addAttribute("form", new StudyRoomForm(
            room.name(), room.location(), room.capacity(), room.openingTime(), room.closingTime()));
        model.addAttribute("isEdit", true);
        model.addAttribute("roomId", roomId);
        return "room_form";
    }

    @PreAuthorize("hasRole('STAFF')")
    @PostMapping("/{roomId}/edit")
    public String handleEditForm(
        @PathVariable final long roomId,
        @ModelAttribute("form") @Valid final StudyRoomForm form,
        final BindingResult bindingResult,
        final Model model
    ) {
        model.addAttribute("isEdit", true);
        model.addAttribute("roomId", roomId);
        if (bindingResult.hasErrors()) {
            return "room_form";
        }
        try {
            this.studyRoomService.updateRoom(roomId, toRequest(form));
            return "redirect:/rooms/" + roomId;
        } catch (BusinessRuleException e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "room_form";
        }
    }

    @PreAuthorize("hasRole('STAFF')")
    @PostMapping("/{roomId}/close")
    public String close(@PathVariable final long roomId,
                        @RequestParam(required = false) final String reason,
                        final RedirectAttributes redirectAttributes) {
        try {
            this.studyRoomService.closeRoom(roomId, reason);
            redirectAttributes.addFlashAttribute("message", "Room closed. Future reservations were cancelled and students notified.");
        } catch (BusinessRuleException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/rooms/" + roomId;
    }

    @PreAuthorize("hasRole('STAFF')")
    @PostMapping("/{roomId}/reopen")
    public String reopen(@PathVariable final long roomId, final RedirectAttributes redirectAttributes) {
        this.studyRoomService.reopenRoom(roomId);
        redirectAttributes.addFlashAttribute("message", "Room is open again.");
        return "redirect:/rooms/" + roomId;
    }
}
