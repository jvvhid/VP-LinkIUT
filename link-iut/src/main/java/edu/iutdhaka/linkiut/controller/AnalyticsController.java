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
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/analytics")
public class AnalyticsController {

    private final ProfileRepository profileRepository;
    private final UserRepository userRepository;

    public AnalyticsController(ProfileRepository profileRepository, UserRepository userRepository) {
        this.profileRepository = profileRepository;
        this.userRepository = userRepository;
    }

    @GetMapping
    public String showAnalytics(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        AppUser currentUser = userDetails != null ? userRepository.findByEmail(userDetails.getUsername()).orElse(null) : null;
        List<UserProfile> profiles = profileRepository.findAll();

        Map<String, Long> deptDistribution = profiles.stream()
                .filter(p -> p.getDepartment() != null && !p.getDepartment().isBlank())
                .collect(Collectors.groupingBy(UserProfile::getDepartment, Collectors.counting()));

        Map<String, Long> topCompanies = profiles.stream()
                .filter(p -> p.getCurrentCompany() != null && !p.getCurrentCompany().isBlank() && !"N/A".equalsIgnoreCase(p.getCurrentCompany()))
                .collect(Collectors.groupingBy(UserProfile::getCurrentCompany, Collectors.counting()));

        long totalStudents = profiles.stream().filter(p -> p.getUser() != null && p.getUser().getRole() == AppUser.Role.STUDENT).count();
        long totalAlumni = profiles.stream().filter(p -> p.getUser() != null && p.getUser().getRole() == AppUser.Role.ALUMNI).count();

        model.addAttribute("currentUser", currentUser);
        model.addAttribute("deptDistribution", deptDistribution);
        model.addAttribute("topCompanies", topCompanies);
        model.addAttribute("totalStudents", totalStudents);
        model.addAttribute("totalAlumni", totalAlumni);
        model.addAttribute("activePage", "analytics");
        return "analytics/index";
    }
}
