package edu.iutdhaka.linkiut.controller;

import edu.iutdhaka.linkiut.model.AppUser;
import edu.iutdhaka.linkiut.model.Post;
import edu.iutdhaka.linkiut.model.PostReaction;
import edu.iutdhaka.linkiut.repository.PostReactionRepository;
import edu.iutdhaka.linkiut.repository.PostRepository;
import edu.iutdhaka.linkiut.repository.UserRepository;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@Controller
@RequestMapping("/reactions")
public class ReactionController {

    private final PostReactionRepository postReactionRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;

    public ReactionController(PostReactionRepository postReactionRepository,
                               PostRepository postRepository,
                               UserRepository userRepository) {
        this.postReactionRepository = postReactionRepository;
        this.postRepository = postRepository;
        this.userRepository = userRepository;
    }

    @PostMapping("/post/{postId}")
    public String reactToPost(@PathVariable Long postId,
                              @AuthenticationPrincipal UserDetails userDetails,
                              @RequestParam PostReaction.ReactionType type) {
        AppUser currentUser = userRepository.findByEmail(userDetails.getUsername()).orElseThrow();
        Post post = postRepository.findById(postId).orElseThrow();

        Optional<PostReaction> existingOpt = postReactionRepository.findByPost_IdAndUser_Id(postId, currentUser.getId());
        if (existingOpt.isPresent()) {
            PostReaction existing = existingOpt.get();
            if (existing.getReactionType() == type) {
                // Remove reaction if clicked twice
                postReactionRepository.delete(existing);
            } else {
                existing.setReactionType(type);
                postReactionRepository.save(existing);
            }
        } else {
            PostReaction reaction = new PostReaction();
            reaction.setPost(post);
            reaction.setUser(currentUser);
            reaction.setReactionType(type);
            postReactionRepository.save(reaction);
        }

        return "redirect:/feed";
    }

    @PostMapping("/post/{postId}/repost")
    public String repost(@PathVariable Long postId,
                         @AuthenticationPrincipal UserDetails userDetails,
                         @RequestParam(required = false) String commentary) {
        AppUser currentUser = userRepository.findByEmail(userDetails.getUsername()).orElseThrow();
        Post original = postRepository.findById(postId).orElseThrow();

        Post repost = new Post();
        repost.setAuthor(currentUser);
        repost.setContent(commentary != null && !commentary.isBlank() ? commentary : "Reposted");
        repost.setOriginalPost(original);

        postRepository.save(repost);
        return "redirect:/feed";
    }
}
