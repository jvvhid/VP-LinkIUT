package edu.iutdhaka.linkiut.controller;

import edu.iutdhaka.linkiut.model.AppUser;
import edu.iutdhaka.linkiut.model.Comment;
import edu.iutdhaka.linkiut.model.Post;
import edu.iutdhaka.linkiut.repository.UserRepository;
import edu.iutdhaka.linkiut.service.PostService;
import edu.iutdhaka.linkiut.service.FileUploadService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Controller
@RequestMapping("/posts")
public class PostController {

    private final PostService postService;
    private final UserRepository userRepository;
    private final FileUploadService fileUploadService;

    public PostController(PostService postService, UserRepository userRepository, FileUploadService fileUploadService) {
        this.postService = postService;
        this.userRepository = userRepository;
        this.fileUploadService = fileUploadService;
    }

    @PostMapping
    public String createPost(@RequestParam String content,
                             @RequestParam(required = false) java.util.List<MultipartFile> media,
                             @RequestParam(required = false) String pollQuestion,
                             @RequestParam(required = false) java.util.List<String> pollOptions,
                             Model model,
                             @AuthenticationPrincipal UserDetails userDetails) {
        AppUser currentUser = getCurrentUser(userDetails);
        
        java.util.List<String> uploadedUrls = new java.util.ArrayList<>();
        if (media != null && !media.isEmpty()) {
            for (MultipartFile file : media) {
                if (!file.isEmpty()) {
                    uploadedUrls.add(fileUploadService.storeFile(file));
                }
            }
        }
        
        String mediaUrl = uploadedUrls.isEmpty() ? null : String.join(",", uploadedUrls);
        
        Post post = postService.createPost(currentUser.getId(), content, mediaUrl, pollQuestion, pollOptions);
        model.addAttribute("post", post);
        model.addAttribute("currentUser", currentUser);
        return "feed/index :: post-card";
    }

    @DeleteMapping("/{id}")
    @ResponseBody
    public String deletePost(@PathVariable Long id, @AuthenticationPrincipal UserDetails userDetails) {
        AppUser currentUser = getCurrentUser(userDetails);
        postService.deletePost(id, currentUser.getId());
        return ""; // HTMX swap out
    }

    @PostMapping("/{id}/like")
    public String toggleLike(@PathVariable Long id, @AuthenticationPrincipal UserDetails userDetails, Model model) {
        AppUser currentUser = getCurrentUser(userDetails);
        postService.toggleLike(id, currentUser.getId());
        
        Post post = postService.getPostById(id);
        model.addAttribute("post", post);
        model.addAttribute("currentUser", currentUser);
        
        return "feed/fragments :: post-reactions";
    }

    @PostMapping("/{id}/dislike")
    public String toggleDislike(@PathVariable Long id, @AuthenticationPrincipal UserDetails userDetails, Model model) {
        AppUser currentUser = getCurrentUser(userDetails);
        postService.toggleDislike(id, currentUser.getId());
        
        Post post = postService.getPostById(id);
        model.addAttribute("post", post);
        model.addAttribute("currentUser", currentUser);
        
        return "feed/fragments :: post-reactions";
    }

    @PostMapping("/{id}/comment")
    public String addComment(@PathVariable Long id,
                             @RequestParam String content,
                             Model model,
                             @AuthenticationPrincipal UserDetails userDetails) {
        AppUser currentUser = getCurrentUser(userDetails);
        Comment comment = postService.addComment(id, currentUser.getId(), content);
        model.addAttribute("comment", comment);
        return "feed/fragments :: comment-item";
    }

    @GetMapping("/{id}/comments")
    public String getComments(@PathVariable Long id, Model model, @AuthenticationPrincipal UserDetails userDetails) {
        AppUser currentUser = getCurrentUser(userDetails);
        model.addAttribute("comments", postService.getComments(id));
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("postId", id);
        return "feed/fragments :: comments-list";
    }

    private AppUser getCurrentUser(UserDetails userDetails) {
        return userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new IllegalStateException("Logged-in user not found"));
    }
}
