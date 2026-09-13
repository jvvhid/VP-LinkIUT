package edu.iutdhaka.linkiut.controller;

import edu.iutdhaka.linkiut.model.AppUser;
import edu.iutdhaka.linkiut.model.Post;
import edu.iutdhaka.linkiut.repository.UserRepository;
import edu.iutdhaka.linkiut.service.PostService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.ArrayList;

@Controller
public class FeedController {

    private final UserRepository userRepository;
    private final PostService postService;

    public FeedController(UserRepository userRepository, PostService postService) {
        this.userRepository = userRepository;
        this.postService = postService;
    }

    /**
     * Main feed page — renders all posts and opportunities.
     */
    @Transactional(readOnly = true)
    @GetMapping("/feed")
    public String index(@RequestParam(required = false, defaultValue = "ALL") String tab,
                        Model model, @AuthenticationPrincipal UserDetails userDetails) {
        
        List<Post> posts = postService.getAllPosts();
        List<FeedItem> feedItems = new ArrayList<>();
        
        for (Post post : posts) {
            // Eagerly initialize lazy collections to prevent LazyInitializationException in Thymeleaf
            post.getLikes().size();
            post.getComments().size();
            
            if (post.isOpportunity() && ("ALL".equalsIgnoreCase(tab) || "OPPORTUNITIES".equalsIgnoreCase(tab))) {
                feedItems.add(new FeedItem("POST", post, post.getCreatedAt()));
            } else if (!post.isOpportunity() && ("ALL".equalsIgnoreCase(tab) || "JOURNEY".equalsIgnoreCase(tab))) {
                feedItems.add(new FeedItem("POST", post, post.getCreatedAt()));
            }
        }
        
        feedItems.sort((a, b) -> b.createdAt.compareTo(a.createdAt));
        
        model.addAttribute("feedItems", feedItems);
        model.addAttribute("currentTab", tab.toUpperCase());
        model.addAttribute("types", new String[]{"JOB", "INTERNSHIP", "MENTORSHIP", "THESIS_MATCHMAKING"}); // Hardcoded for now
        
        // Count opportunities
        long oppCount = posts.stream().filter(Post::isOpportunity).count();
        model.addAttribute("opportunityCount", oppCount);
        
        model.addAttribute("userCount", userRepository.count());
        model.addAttribute("activePage", "feed");
        addCurrentUser(model, userDetails);
        return "feed/index";
    }

    public static class FeedItem {
        public String type;
        public Object item;
        public java.time.LocalDateTime createdAt;

        public FeedItem(String type, Object item, java.time.LocalDateTime createdAt) {
            this.type = type;
            this.item = item;
            this.createdAt = createdAt;
        }
        
        public String getType() { return type; }
        public Object getItem() { return item; }
        public java.time.LocalDateTime getCreatedAt() { return createdAt; }
    }

    /**
     * HTMX partial: returns just the feed list fragment for live updates or search.
     */
    @Transactional(readOnly = true)
    @GetMapping("/opportunities/list")
    public String feedList(@RequestParam(required = false, name = "q") String query,
                           @RequestParam(required = false, defaultValue = "ALL") String tab,
                           Model model, @AuthenticationPrincipal UserDetails userDetails) {
        
        List<Post> posts = postService.getAllPosts();
        List<FeedItem> feedItems = new ArrayList<>();
        
        for (Post post : posts) {
            // Eagerly initialize lazy collections to prevent LazyInitializationException in Thymeleaf
            post.getLikes().size();
            post.getComments().size();
            
            boolean matchesSearch = true;
            if (query != null && !query.trim().isEmpty() && post.isOpportunity()) {
                String q = query.toLowerCase();
                matchesSearch = (post.getOpportunityTitle() != null && post.getOpportunityTitle().toLowerCase().contains(q)) ||
                                (post.getContent() != null && post.getContent().toLowerCase().contains(q));
            }
            
            if (matchesSearch) {
                if (post.isOpportunity() && ("ALL".equalsIgnoreCase(tab) || "OPPORTUNITIES".equalsIgnoreCase(tab))) {
                    feedItems.add(new FeedItem("POST", post, post.getCreatedAt()));
                } else if (!post.isOpportunity() && (query == null || query.trim().isEmpty()) && ("ALL".equalsIgnoreCase(tab) || "JOURNEY".equalsIgnoreCase(tab))) {
                    feedItems.add(new FeedItem("POST", post, post.getCreatedAt()));
                }
            }
        }
        
        feedItems.sort((a, b) -> b.createdAt.compareTo(a.createdAt));
        
        model.addAttribute("feedItems", feedItems);
        model.addAttribute("currentTab", tab.toUpperCase());
        addCurrentUser(model, userDetails);
        return "feed/index :: opportunity-list";
    }

    @PostMapping("/opportunities")
    public String createOpportunity(@RequestParam String title,
            @RequestParam String description,
            @RequestParam String type,
            Model model,
            @AuthenticationPrincipal UserDetails userDetails) {
        AppUser currentUser = getCurrentUser(userDetails);
        Post post = postService.createOpportunity(currentUser.getId(), title, description, type);
        model.addAttribute("post", post);
        addCurrentUser(model, userDetails);
        return "feed/index :: post-card";
    }

    // Note: Opportunities are now Posts, so deleting uses the /posts/{id} endpoint from PostController
    // The old /opportunities/{id} endpoint can be removed or redirected

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
