package edu.iutdhaka.linkiut.controller;

import edu.iutdhaka.linkiut.model.AppUser;
import edu.iutdhaka.linkiut.model.MentorshipSlot;
import edu.iutdhaka.linkiut.repository.MentorshipSlotRepository;
import edu.iutdhaka.linkiut.repository.UserRepository;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequestMapping("/mentorship")
public class MentorshipController {

    private final MentorshipSlotRepository mentorshipSlotRepository;
    private final UserRepository userRepository;

    public MentorshipController(MentorshipSlotRepository mentorshipSlotRepository, UserRepository userRepository) {
        this.mentorshipSlotRepository = mentorshipSlotRepository;
        this.userRepository = userRepository;
    }

    @GetMapping
    public String mentorshipDashboard(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        AppUser currentUser = userRepository.findByEmail(userDetails.getUsername()).orElse(null);
        List<MentorshipSlot> availableSlots = mentorshipSlotRepository.findByStatusOrderByStartTimeAsc(MentorshipSlot.SlotStatus.AVAILABLE);
        
        List<MentorshipSlot> myBookings = currentUser != null ? mentorshipSlotRepository.findByMentee_IdOrderByStartTimeAsc(currentUser.getId()) : List.of();
        List<MentorshipSlot> myOfferedSlots = currentUser != null ? mentorshipSlotRepository.findByMentor_IdOrderByStartTimeAsc(currentUser.getId()) : List.of();

        model.addAttribute("currentUser", currentUser);
        model.addAttribute("availableSlots", availableSlots);
        model.addAttribute("myBookings", myBookings);
        model.addAttribute("myOfferedSlots", myOfferedSlots);
        model.addAttribute("activePage", "mentorship");
        return "mentorship/index";
    }

    @PostMapping("/create")
    public String createSlot(@AuthenticationPrincipal UserDetails userDetails,
                             @RequestParam String topic,
                             @RequestParam String description,
                             @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
                             @RequestParam(defaultValue = "30") int durationMinutes,
                             @RequestParam(required = false) String meetingLink) {
        AppUser currentUser = userRepository.findByEmail(userDetails.getUsername()).orElseThrow();

        MentorshipSlot slot = new MentorshipSlot();
        slot.setMentor(currentUser);
        slot.setTopic(topic);
        slot.setDescription(description);
        slot.setStartTime(startTime);
        slot.setDurationMinutes(durationMinutes);
        slot.setMeetingLink(meetingLink);
        slot.setStatus(MentorshipSlot.SlotStatus.AVAILABLE);

        mentorshipSlotRepository.save(slot);
        return "redirect:/mentorship";
    }

    @PostMapping("/{id}/book")
    public String bookSlot(@PathVariable Long id,
                           @AuthenticationPrincipal UserDetails userDetails,
                           @RequestParam(required = false) String studentNote) {
        AppUser currentUser = userRepository.findByEmail(userDetails.getUsername()).orElseThrow();
        MentorshipSlot slot = mentorshipSlotRepository.findById(id).orElseThrow();

        if (slot.getStatus() == MentorshipSlot.SlotStatus.AVAILABLE && !slot.getMentor().getId().equals(currentUser.getId())) {
            slot.setMentee(currentUser);
            slot.setStudentNote(studentNote);
            slot.setStatus(MentorshipSlot.SlotStatus.BOOKED);
            mentorshipSlotRepository.save(slot);
        }

        return "redirect:/mentorship";
    }
}
