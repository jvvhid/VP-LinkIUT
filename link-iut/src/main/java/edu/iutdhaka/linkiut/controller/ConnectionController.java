package edu.iutdhaka.linkiut.controller;

import edu.iutdhaka.linkiut.model.AppUser;
import edu.iutdhaka.linkiut.repository.UserRepository;
import edu.iutdhaka.linkiut.service.ConnectionService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/connections")
public class ConnectionController {

    private final ConnectionService connectionService;
    private final UserRepository userRepository;

    public ConnectionController(ConnectionService connectionService, UserRepository userRepository) {
        this.connectionService = connectionService;
        this.userRepository = userRepository;
    }

    @PostMapping("/request/{userId}")
    public String requestConnection(@PathVariable Long userId, @AuthenticationPrincipal UserDetails userDetails, @RequestParam(defaultValue = "") String redirectUrl) {
        AppUser currentUser = getCurrentUser(userDetails);
        connectionService.sendRequest(currentUser.getId(), userId);
        return redirectUrl.isEmpty() ? "redirect:/profile/" + userId : "redirect:" + redirectUrl;
    }

    @PostMapping("/accept/{connectionId}")
    public String acceptConnection(@PathVariable Long connectionId, @AuthenticationPrincipal UserDetails userDetails, @RequestParam(defaultValue = "") String redirectUrl) {
        AppUser currentUser = getCurrentUser(userDetails);
        connectionService.acceptRequest(connectionId, currentUser.getId());
        return redirectUrl.isEmpty() ? "redirect:/network" : "redirect:" + redirectUrl;
    }

    @PostMapping("/reject/{connectionId}")
    public String rejectConnection(@PathVariable Long connectionId, @AuthenticationPrincipal UserDetails userDetails, @RequestParam(defaultValue = "") String redirectUrl) {
        AppUser currentUser = getCurrentUser(userDetails);
        connectionService.rejectRequest(connectionId, currentUser.getId());
        return redirectUrl.isEmpty() ? "redirect:/network" : "redirect:" + redirectUrl;
    }

    @PostMapping("/remove/{connectionId}")
    public String removeConnection(@PathVariable Long connectionId, @AuthenticationPrincipal UserDetails userDetails, @RequestParam(defaultValue = "") String redirectUrl) {
        AppUser currentUser = getCurrentUser(userDetails);
        connectionService.removeConnection(connectionId, currentUser.getId());
        return redirectUrl.isEmpty() ? "redirect:/network" : "redirect:" + redirectUrl;
    }

    @GetMapping
    public String listConnections(Model model, @AuthenticationPrincipal UserDetails userDetails) {
        AppUser currentUser = getCurrentUser(userDetails);
        model.addAttribute("pendingRequests", connectionService.getPendingRequests(currentUser.getId()));
        model.addAttribute("connections", connectionService.getConnections(currentUser.getId()));
        model.addAttribute("activePage", "network");
        addCurrentUser(model, userDetails);
        return "network/connections";
    }

    private AppUser getCurrentUser(UserDetails userDetails) {
        return userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new IllegalStateException("Logged-in user not found"));
    }

    private void addCurrentUser(Model model, UserDetails userDetails) {
        if (userDetails != null) {
            userRepository.findByEmail(userDetails.getUsername())
                    .ifPresent(u -> model.addAttribute("currentUser", u));
        }
    }
}
