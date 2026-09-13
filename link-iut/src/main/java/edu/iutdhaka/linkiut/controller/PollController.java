package edu.iutdhaka.linkiut.controller;

import edu.iutdhaka.linkiut.model.AppUser;
import edu.iutdhaka.linkiut.model.Poll;
import edu.iutdhaka.linkiut.model.PollOption;
import edu.iutdhaka.linkiut.model.PollVote;
import edu.iutdhaka.linkiut.repository.PollOptionRepository;
import edu.iutdhaka.linkiut.repository.PollRepository;
import edu.iutdhaka.linkiut.repository.PollVoteRepository;
import edu.iutdhaka.linkiut.repository.UserRepository;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.transaction.annotation.Transactional;

@Controller
@RequestMapping("/polls")
public class PollController {

    private final PollRepository pollRepository;
    private final PollOptionRepository pollOptionRepository;
    private final PollVoteRepository pollVoteRepository;
    private final UserRepository userRepository;

    public PollController(PollRepository pollRepository, PollOptionRepository pollOptionRepository, PollVoteRepository pollVoteRepository, UserRepository userRepository) {
        this.pollRepository = pollRepository;
        this.pollOptionRepository = pollOptionRepository;
        this.pollVoteRepository = pollVoteRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    @PostMapping("/{pollId}/vote")
    public String vote(@PathVariable Long pollId, @RequestParam Long optionId, Model model, @AuthenticationPrincipal UserDetails userDetails) {
        AppUser currentUser = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Poll poll = pollRepository.findById(pollId)
                .orElseThrow(() -> new IllegalArgumentException("Poll not found"));

        if (pollVoteRepository.existsByPollIdAndUserId(pollId, currentUser.getId())) {
            PollVote existingVote = pollVoteRepository.findByPollIdAndUserId(pollId, currentUser.getId())
                    .orElseThrow(() -> new IllegalArgumentException("Vote not found"));
            
            if (!existingVote.getPollOption().getId().equals(optionId)) {
                // Switch vote
                PollOption oldOption = existingVote.getPollOption();
                oldOption.decrementVote();
                pollOptionRepository.save(oldOption);

                PollOption newOption = pollOptionRepository.findById(optionId)
                        .orElseThrow(() -> new IllegalArgumentException("Option not found"));
                newOption.incrementVote();
                pollOptionRepository.save(newOption);
                
                existingVote.setPollOption(newOption);
                pollVoteRepository.save(existingVote);
            }
        } else {
            // New vote
            PollOption option = pollOptionRepository.findById(optionId)
                    .orElseThrow(() -> new IllegalArgumentException("Option not found"));
            option.incrementVote();
            pollOptionRepository.save(option);
            pollVoteRepository.save(new PollVote(poll, option, currentUser));
        }

        model.addAttribute("poll", poll);
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("hasVoted", true);
        model.addAttribute("userVoteOptionId", optionId);
        
        return "feed/fragments :: poll-results";
    }
}
