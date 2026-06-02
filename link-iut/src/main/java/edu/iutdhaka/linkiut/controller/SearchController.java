package edu.iutdhaka.linkiut.controller;

import edu.iutdhaka.linkiut.model.AppUser;
import edu.iutdhaka.linkiut.repository.UserRepository;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Collections;
import java.util.List;

@Controller
public class SearchController {

    private final UserRepository userRepository;

    public SearchController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Search/Discover page — search users by name, email, or find all users.
     */
    @GetMapping("/search")
    public String search(@RequestParam(required = false, defaultValue = "") String q,
                         Model model,
                         @AuthenticationPrincipal UserDetails userDetails) {
        List<AppUser> results;
        if (q == null || q.trim().isEmpty()) {
            results = userRepository.findAll();
        } else {
            results = userRepository.searchByKeyword(q.trim());
        }

        model.addAttribute("query", q);
        model.addAttribute("results", results);

        // Add current user for navbar
        if (userDetails != null) {
            userRepository.findByEmail(userDetails.getUsername())
                    .ifPresent(u -> model.addAttribute("currentUser", u));
        }

        return "feed/search";
    }
}
