package gr.hua.dit.studyrooms.web.ui;

import gr.hua.dit.studyrooms.core.exception.BusinessRuleException;
import gr.hua.dit.studyrooms.core.exception.NotFoundException;
import gr.hua.dit.studyrooms.core.model.PersonType;
import gr.hua.dit.studyrooms.core.security.CurrentUser;
import gr.hua.dit.studyrooms.core.security.CurrentUserProvider;
import gr.hua.dit.studyrooms.core.service.ReservationService;
import gr.hua.dit.studyrooms.core.service.StudyRoomService;
import gr.hua.dit.studyrooms.core.service.model.CreateReservationRequest;
import gr.hua.dit.studyrooms.core.service.model.ReservationView;
import gr.hua.dit.studyrooms.core.service.model.StudyRoomView;
import gr.hua.dit.studyrooms.web.ui.model.ReservationForm;

import jakarta.validation.Valid;

import org.springframework.format.annotation.DateTimeFormat;
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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.List;

/**
 * UI controller for managing reservations.
 */
@Controller
@RequestMapping("/reservations")
public class ReservationController {

    private final CurrentUserProvider currentUserProvider;
    private final ReservationService reservationService;
    private final StudyRoomService studyRoomService;

    public ReservationController(final CurrentUserProvider currentUserProvider,
                                 final ReservationService reservationService,
                                 final StudyRoomService studyRoomService) {
        if (currentUserProvider == null) throw new NullPointerException();
        if (reservationService == null) throw new NullPointerException();
        if (studyRoomService == null) throw new NullPointerException();
        this.currentUserProvider = currentUserProvider;
        this.reservationService = reservationService;
        this.studyRoomService = studyRoomService;
    }

    private List<StudyRoomView> openRooms() {
        return this.studyRoomService.getAllRooms().stream().filter(StudyRoomView::open).toList();
    }

    @PreAuthorize("hasAnyRole('STUDENT', 'STAFF')")
    @GetMapping("")
    public String list(
        @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") final LocalDate date,
        final Model model
    ) {
        final CurrentUser me = this.currentUserProvider.requireCurrentUser();
        final List<ReservationView> reservations;
        if (me.type() == PersonType.STAFF) {
            reservations = this.reservationService.getAllReservations(date);
        } else {
            reservations = this.reservationService.getReservationsOfStudent(me.id());
        }
        model.addAttribute("reservations", reservations);
        model.addAttribute("date", date);
        return "reservations";
    }

    @PreAuthorize("hasRole('STUDENT')")
    @GetMapping("/new")
    public String showCreateForm(
        @RequestParam(required = false) final Long roomId,
        @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") final LocalDate date,
        final Model model
    ) {
        model.addAttribute("form", new ReservationForm(roomId, date == null ? LocalDate.now() : date, null, null));
        model.addAttribute("rooms", this.openRooms());
        return "new_reservation";
    }

    @PreAuthorize("hasRole('STUDENT')")
    @PostMapping("/new")
    public String handleCreateForm(
        @ModelAttribute("form") @Valid final ReservationForm form,
        final BindingResult bindingResult,
        final Model model,
        final RedirectAttributes redirectAttributes
    ) {
        model.addAttribute("rooms", this.openRooms());
        if (bindingResult.hasErrors()) {
            return "new_reservation";
        }
        final CreateReservationRequest request = new CreateReservationRequest(
            this.currentUserProvider.requiredStudentId(),
            form.roomId(),
            form.date(),
            form.startTime(),
            form.endTime()
        );
        try {
            final ReservationView reservation = this.reservationService.createReservation(request);
            redirectAttributes.addFlashAttribute("message",
                "Reservation #" + reservation.id() + " confirmed. An SMS notification was requested.");
            return "redirect:/reservations";
        } catch (BusinessRuleException | NotFoundException e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "new_reservation";
        }
    }

    @PreAuthorize("hasAnyRole('STUDENT', 'STAFF')")
    @PostMapping("/{reservationId}/cancel")
    public String cancel(
        @PathVariable final long reservationId,
        @RequestParam(required = false) final String reason,
        final RedirectAttributes redirectAttributes
    ) {
        final CurrentUser me = this.currentUserProvider.requireCurrentUser();
        final Long actingStudentId = me.type() == PersonType.STUDENT ? me.id() : null;
        try {
            this.reservationService.cancelReservation(reservationId, actingStudentId, reason);
            redirectAttributes.addFlashAttribute("message", "Reservation #" + reservationId + " cancelled.");
        } catch (BusinessRuleException | NotFoundException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/reservations";
    }
}
