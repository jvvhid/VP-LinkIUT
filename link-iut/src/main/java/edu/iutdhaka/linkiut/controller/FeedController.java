package edu.iutdhaka.linkiut.controller;

import edu.iutdhaka.linkiut.model.AppUser;
import edu.iutdhaka.linkiut.model.Opportunity;
import edu.iutdhaka.linkiut.repository.UserRepository;
import edu.iutdhaka.linkiut.service.OpportunityService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class FeedController {

    private final OpportunityService opportunityService;
    private final UserRepository userRepository;

    public FeedController(OpportunityService opportunityService, UserRepository userRepository) {
        this.opportunityService = opportunityService;
        this.userRepository = userRepository;
    }

    /**
     * Main feed page — renders all opportunities.
     */
    @GetMapping("/")
    public String index(Model model, @AuthenticationPrincipal UserDetails userDetails) {
        model.addAttribute("opportunities", opportunityService.getAllOpportunities());
        model.addAttribute("types", Opportunity.OpportunityType.values());
        addCurrentUser(model, userDetails);
        return "feed/index";
    }

    /**
     * HTMX partial: returns just the opportunity list fragment for live updates or search.
     */
    @GetMapping("/opportunities/list")
    public String opportunityList(@RequestParam(required = false, name = "q") String query,
                                  Model model, @AuthenticationPrincipal UserDetails userDetails) {
        if (query != null && !query.trim().isEmpty()) {
            model.addAttribute("opportunities", opportunityService.searchOpportunities(query));
        } else {
            model.addAttribute("opportunities", opportunityService.getAllOpportunities());
        }
        addCurrentUser(model, userDetails);
        return "feed/index :: opportunity-list";
    }

    @PostMapping("/opportunities")
    public String createOpportunity(@RequestParam String title,
            @RequestParam String description,
            @RequestParam Opportunity.OpportunityType type,
            Model model,
            @AuthenticationPrincipal UserDetails userDetails) {
        AppUser currentUser = getCurrentUser(userDetails);
        Opportunity opp = opportunityService.createOpportunity(title, description, type, currentUser);
        model.addAttribute("opp", opp);
        addCurrentUser(model, userDetails);
        return "feed/index :: opportunity-card";
    }

    /**
     * HTMX: delete an opportunity
     */
    @DeleteMapping("/opportunities/{id}")
    @ResponseBody
    public String deleteOpportunity(@PathVariable Long id, @AuthenticationPrincipal UserDetails userDetails) {
        AppUser currentUser = getCurrentUser(userDetails);
        opportunityService.deleteOpportunity(id, currentUser.getId());
        return ""; // Return empty string to swap outerHTML and remove the element
    }

    // ── Helpers ──────────────────────────────────────────────
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
