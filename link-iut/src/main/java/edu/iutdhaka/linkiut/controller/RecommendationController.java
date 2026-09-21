package edu.iutdhaka.linkiut.controller;

import edu.iutdhaka.linkiut.model.AppUser;
import edu.iutdhaka.linkiut.model.Recommendation;
import edu.iutdhaka.linkiut.repository.RecommendationRepository;
import edu.iutdhaka.linkiut.repository.UserRepository;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/recommendations")
public class RecommendationController {

    private final RecommendationRepository recommendationRepository;
    private final UserRepository userRepository;

    public RecommendationController(RecommendationRepository recommendationRepository, UserRepository userRepository) {
        this.recommendationRepository = recommendationRepository;
        this.userRepository = userRepository;
    }

    @PostMapping("/give/{receiverId}")
    public String giveRecommendation(@PathVariable Long receiverId,
                                     @AuthenticationPrincipal UserDetails userDetails,
                                     @RequestParam String relationship,
                                     @RequestParam String content) {
        AppUser author = userRepository.findByEmail(userDetails.getUsername()).orElseThrow();
        AppUser receiver = userRepository.findById(receiverId).orElseThrow();

        if (author.getId().equals(receiverId)) {
            return "redirect:/profile/" + receiverId + "?error=self_recommendation";
        }

        Recommendation rec = new Recommendation();
        rec.setAuthor(author);
        rec.setReceiver(receiver);
        rec.setRelationship(relationship);
        rec.setContent(content);
        rec.setApproved(false); // Requires receiver approval

        recommendationRepository.save(rec);
        return "redirect:/profile/" + receiverId + "?success=recommendation_submitted";
    }

    @PostMapping("/{id}/approve")
    public String approveRecommendation(@PathVariable Long id, @AuthenticationPrincipal UserDetails userDetails) {
        AppUser currentUser = userRepository.findByEmail(userDetails.getUsername()).orElseThrow();
        Recommendation rec = recommendationRepository.findById(id).orElseThrow();

        if (rec.getReceiver().getId().equals(currentUser.getId())) {
            rec.setApproved(true);
            recommendationRepository.save(rec);
        }

        return "redirect:/profile/" + currentUser.getId();
    }

    @PostMapping("/{id}/delete")
    public String deleteRecommendation(@PathVariable Long id, @AuthenticationPrincipal UserDetails userDetails) {
        AppUser currentUser = userRepository.findByEmail(userDetails.getUsername()).orElseThrow();
        Recommendation rec = recommendationRepository.findById(id).orElseThrow();

        if (rec.getReceiver().getId().equals(currentUser.getId()) || rec.getAuthor().getId().equals(currentUser.getId())) {
            recommendationRepository.delete(rec);
        }

        return "redirect:/profile/" + currentUser.getId();
    }
}
