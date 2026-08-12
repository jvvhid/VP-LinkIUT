package edu.iutdhaka.linkiut.controller;

import edu.iutdhaka.linkiut.model.UserProfile;
import edu.iutdhaka.linkiut.repository.ProfileRepository;
import edu.iutdhaka.linkiut.repository.UserRepository;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
public class NetworkController {

    private final ProfileRepository profileRepository;
    private final UserRepository userRepository;

    public NetworkController(ProfileRepository profileRepository, UserRepository userRepository) {
        this.profileRepository = profileRepository;
        this.userRepository = userRepository;
    }

    @GetMapping("/network")
    public String index(Model model, @AuthenticationPrincipal UserDetails userDetails) {
        List<UserProfile> profiles = profileRepository.searchNetwork(null, null, null, null);
        model.addAttribute("profiles", profiles);
        addCurrentUser(model, userDetails);
        return "network/index";
    }

    @GetMapping("/network/search")
    public String search(@RequestParam(required = false) String department,
                         @RequestParam(required = false) String batch,
                         @RequestParam(required = false) String hostel,
                         @RequestParam(required = false) String name,
                         Model model, @AuthenticationPrincipal UserDetails userDetails) {
                         
        List<UserProfile> profiles = profileRepository.searchNetwork(department, batch, hostel, name);
        model.addAttribute("profiles", profiles);
        return "network/index :: profile-grid";
    }

    private void addCurrentUser(Model model, UserDetails userDetails) {
        if (userDetails != null) {
            userRepository.findByEmail(userDetails.getUsername())
                    .ifPresent(u -> model.addAttribute("currentUser", u));
        }
    }
}
