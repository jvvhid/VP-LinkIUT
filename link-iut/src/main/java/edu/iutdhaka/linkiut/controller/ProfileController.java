package edu.iutdhaka.linkiut.controller;

import edu.iutdhaka.linkiut.model.AppUser;
import edu.iutdhaka.linkiut.model.UserProfile;
import edu.iutdhaka.linkiut.repository.ProfileRepository;
import edu.iutdhaka.linkiut.repository.UserRepository;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/profile")
public class ProfileController {

    private final ProfileRepository profileRepository;
    private final UserRepository userRepository;
    private final edu.iutdhaka.linkiut.service.OpportunityService opportunityService;

    public ProfileController(ProfileRepository profileRepository, UserRepository userRepository, edu.iutdhaka.linkiut.service.OpportunityService opportunityService) {
        this.profileRepository = profileRepository;
        this.userRepository = userRepository;
        this.opportunityService = opportunityService;
    }

    /**
     * View a user's profile with experiences and projects eagerly loaded.
     */
    @GetMapping("/{userId}")
    public String viewProfile(@PathVariable Long userId, Model model,
                               @AuthenticationPrincipal UserDetails userDetails) {
        UserProfile profile = profileRepository.findByUserIdWithDetails(userId)
                .orElseThrow(() -> new IllegalArgumentException("Profile not found for user " + userId));

        model.addAttribute("profile", profile);
        model.addAttribute("profileUser", profile.getUser());
        model.addAttribute("opportunities", opportunityService.getOpportunitiesByPosterId(userId));

        // Add current user for "Start Chat" button visibility
        if (userDetails != null) {
            userRepository.findByEmail(userDetails.getUsername())
                    .ifPresent(u -> {
                        model.addAttribute("currentUser", u);
                        model.addAttribute("isOwnProfile", u.getId().equals(userId));
                    });
        }

        return "profile/view";
    }

    @org.springframework.web.bind.annotation.PostMapping("/office-hours")
    @org.springframework.web.bind.annotation.ResponseBody
    public String updateOfficeHours(@org.springframework.web.bind.annotation.RequestParam String officeHours,
                                    @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) return "Error: Not logged in";
        
        AppUser currentUser = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new IllegalStateException("User not found"));
                
        if (currentUser.getRole() != AppUser.Role.ALUMNI) {
            return "Error: Only Alumni can set office hours.";
        }
                
        UserProfile profile = profileRepository.findByUserIdWithDetails(currentUser.getId())
                .orElseThrow(() -> new IllegalStateException("Profile not found"));
                
        profile.setOfficeHours(officeHours);
        profileRepository.save(profile);
        
        return officeHours;
    }
}
