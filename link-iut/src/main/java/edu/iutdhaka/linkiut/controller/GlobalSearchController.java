package edu.iutdhaka.linkiut.controller;

import edu.iutdhaka.linkiut.model.AppUser;
import edu.iutdhaka.linkiut.model.JobPosting;
import edu.iutdhaka.linkiut.model.Post;
import edu.iutdhaka.linkiut.model.UserProfile;
import edu.iutdhaka.linkiut.repository.JobPostingRepository;
import edu.iutdhaka.linkiut.repository.PostRepository;
import edu.iutdhaka.linkiut.repository.ProfileRepository;
import edu.iutdhaka.linkiut.repository.UserRepository;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequestMapping("/search")
public class GlobalSearchController {

    private final UserRepository userRepository;
    private final ProfileRepository profileRepository;
    private final PostRepository postRepository;
    private final JobPostingRepository jobPostingRepository;

    public GlobalSearchController(UserRepository userRepository,
                                  ProfileRepository profileRepository,
                                  PostRepository postRepository,
                                  JobPostingRepository jobPostingRepository) {
        this.userRepository = userRepository;
        this.profileRepository = profileRepository;
        this.postRepository = postRepository;
        this.jobPostingRepository = jobPostingRepository;
    }

    @GetMapping
    public String search(@RequestParam(name = "q", required = false) String query,
                         @AuthenticationPrincipal UserDetails userDetails,
                         Model model) {
        AppUser currentUser = userDetails != null ? userRepository.findByEmail(userDetails.getUsername()).orElse(null) : null;

        List<UserProfile> matchingProfiles = List.of();
        List<Post> matchingPosts = List.of();
        List<JobPosting> matchingJobs = List.of();

        if (query != null && !query.isBlank()) {
            String q = query.trim();
            matchingProfiles = profileRepository.findByUser_DisplayNameContainingIgnoreCaseOrHeadlineContainingIgnoreCaseOrDepartmentContainingIgnoreCase(q, q, q);
            matchingPosts = postRepository.findByContentContainingIgnoreCase(q);
            matchingJobs = jobPostingRepository.findByTitleContainingIgnoreCaseOrCompanyContainingIgnoreCaseOrDescriptionContainingIgnoreCase(q, q, q);
        }

        model.addAttribute("currentUser", currentUser);
        model.addAttribute("query", query);
        model.addAttribute("matchingProfiles", matchingProfiles);
        model.addAttribute("matchingPosts", matchingPosts);
        model.addAttribute("matchingJobs", matchingJobs);
        model.addAttribute("activePage", "search");
        return "search/index";
    }
}
