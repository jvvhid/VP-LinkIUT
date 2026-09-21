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

import java.util.List;

@Controller
@RequestMapping("/hubs")
public class HubsController {

    private final ProfileRepository profileRepository;
    private final UserRepository userRepository;

    public HubsController(ProfileRepository profileRepository, UserRepository userRepository) {
        this.profileRepository = profileRepository;
        this.userRepository = userRepository;
    }

    @GetMapping
    public String hubsOverview(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        AppUser currentUser = userDetails != null ? userRepository.findByEmail(userDetails.getUsername()).orElse(null) : null;

        List<String> departments = List.of("CSE", "EEE", "MCE", "CEE", "SWE", "BTM");
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("departments", departments);
        model.addAttribute("activePage", "hubs");
        return "hubs/index";
    }

    @GetMapping("/dept/{deptCode}")
    public String departmentHub(@PathVariable String deptCode, @AuthenticationPrincipal UserDetails userDetails, Model model) {
        AppUser currentUser = userDetails != null ? userRepository.findByEmail(userDetails.getUsername()).orElse(null) : null;
        List<UserProfile> members = profileRepository.findByDepartmentIgnoreCase(deptCode);

        model.addAttribute("currentUser", currentUser);
        model.addAttribute("deptCode", deptCode.toUpperCase());
        model.addAttribute("members", members);
        model.addAttribute("activePage", "hubs");
        return "hubs/department";
    }
}
