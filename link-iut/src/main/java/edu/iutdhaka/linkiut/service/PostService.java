package edu.iutdhaka.linkiut.service;

import edu.iutdhaka.linkiut.model.AppUser;
import edu.iutdhaka.linkiut.model.Comment;
import edu.iutdhaka.linkiut.model.Post;
import edu.iutdhaka.linkiut.model.PostLike;
import edu.iutdhaka.linkiut.repository.CommentRepository;
import edu.iutdhaka.linkiut.repository.PostLikeRepository;
import edu.iutdhaka.linkiut.repository.PostRepository;
import edu.iutdhaka.linkiut.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class PostService {

    private final PostRepository postRepository;
    private final PostLikeRepository postLikeRepository;
    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    public PostService(PostRepository postRepository, PostLikeRepository postLikeRepository, CommentRepository commentRepository, UserRepository userRepository, NotificationService notificationService) {
        this.postRepository = postRepository;
        this.postLikeRepository = postLikeRepository;
        this.commentRepository = commentRepository;
        this.userRepository = userRepository;
        this.notificationService = notificationService;
    }

    public Post createPost(Long authorId, String content, String imageUrl, String pollQuestion, java.util.List<String> pollOptions) {
        AppUser author = userRepository.findById(authorId)
                .orElseThrow(() -> new IllegalArgumentException("Author not found"));
        
        Post tempPost = postRepository.save(new Post(author, "")); // get ID for url
        String parsedContent = parseAndNotifyMentions(content, author, "/feed#" + tempPost.getId());
        tempPost.setContent(parsedContent);
        
        if (imageUrl != null && !imageUrl.isBlank()) {
            tempPost.setImageUrl(imageUrl);
        }
        
        if (pollQuestion != null && !pollQuestion.isBlank() && pollOptions != null && pollOptions.size() >= 2) {
            edu.iutdhaka.linkiut.model.Poll poll = new edu.iutdhaka.linkiut.model.Poll(pollQuestion);
            for (String opt : pollOptions) {
                if (opt != null && !opt.isBlank()) {
                    poll.addOption(new edu.iutdhaka.linkiut.model.PollOption(opt));
                }
            }
            if (poll.getOptions().size() >= 2) {
                tempPost.setPoll(poll);
            }
        }
        
        return postRepository.save(tempPost);
    }

    public Post createOpportunity(Long authorId, String title, String description, String type) {
        AppUser author = userRepository.findById(authorId)
                .orElseThrow(() -> new IllegalArgumentException("Author not found"));
        
        Post tempPost = postRepository.save(new Post(author, ""));
        String parsedDescription = parseAndNotifyMentions(description, author, "/feed#" + tempPost.getId());
        
        tempPost.setContent(parsedDescription);
        tempPost.setOpportunity(true);
        tempPost.setOpportunityTitle(title);
        tempPost.setOpportunityType(type);
        
        return postRepository.save(tempPost);
    }

    public void deletePost(Long postId, Long userId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("Post not found"));
        
        if (!post.getAuthor().getId().equals(userId)) {
            throw new IllegalStateException("Not authorized to delete this post");
        }
        
        postRepository.delete(post);
    }

    public List<Post> getAllPosts() {
        return postRepository.findAllWithAuthor();
    }

    public Post getPostById(Long id) {
        return postRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Post not found"));
    }

    public List<Post> getPostsByUser(Long userId) {
        return postRepository.findByAuthorId(userId);
    }

    @Transactional
    public void toggleLike(Long postId, Long userId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("Post not found"));
        
        AppUser user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        
        Optional<PostLike> existingLikeOpt = post.getLikes().stream().filter(l -> l.getUser().getId().equals(userId)).findFirst();
        
        if (existingLikeOpt.isPresent()) {
            PostLike existingLike = existingLikeOpt.get();
            if (existingLike.isDislike()) {
                existingLike.setDislike(false); // Switch to like
                notificationService.createNotification(post.getAuthor(), user, "LIKE", user.getDisplayName() + " liked your post.", "/feed#" + postId);
            } else {
                post.getLikes().remove(existingLike); // Unlike
            }
        } else {
            PostLike newLike = new PostLike(post, user, false);
            post.getLikes().add(newLike);
            notificationService.createNotification(post.getAuthor(), user, "LIKE", user.getDisplayName() + " liked your post.", "/feed#" + postId);
        }
        postRepository.save(post);
    }

    @Transactional
    public void toggleDislike(Long postId, Long userId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("Post not found"));
        
        AppUser user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
                
        Optional<PostLike> existingLikeOpt = post.getLikes().stream().filter(l -> l.getUser().getId().equals(userId)).findFirst();
        
        if (existingLikeOpt.isPresent()) {
            PostLike existingLike = existingLikeOpt.get();
            if (!existingLike.isDislike()) {
                existingLike.setDislike(true); // Switch to dislike
            } else {
                post.getLikes().remove(existingLike); // Undislike
            }
        } else {
            PostLike newLike = new PostLike(post, user, true);
            post.getLikes().add(newLike);
        }
        postRepository.save(post);
    }

    public Comment addComment(Long postId, Long authorId, String content) {
        if (content == null || content.trim().isEmpty()) {
            throw new IllegalArgumentException("Comment content cannot be empty");
        }
        
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("Post not found"));
        AppUser author = userRepository.findById(authorId)
                .orElseThrow(() -> new IllegalArgumentException("Author not found"));
                
        String parsedContent = parseAndNotifyMentions(content, author, "/feed#" + postId);
        
        Comment comment = new Comment(post, author, parsedContent);
        Comment savedComment = commentRepository.save(comment);
        
        if (!post.getAuthor().getId().equals(author.getId())) {
            notificationService.createNotification(post.getAuthor(), author, "COMMENT", author.getDisplayName() + " commented on your post.", "/feed#" + postId);
        }
        
        return savedComment;
    }

    private String parseAndNotifyMentions(String rawContent, AppUser author, String targetUrl) {
        if (rawContent == null || rawContent.isBlank()) return rawContent;
        String safeContent = org.springframework.web.util.HtmlUtils.htmlEscape(rawContent);
        
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("@([a-zA-Z0-9_.-]+)");
        java.util.regex.Matcher matcher = pattern.matcher(safeContent);
        StringBuffer sb = new StringBuffer();
        
        while (matcher.find()) {
            String handle = matcher.group(1);
            String emailPrefix = handle + "@";
            java.util.List<AppUser> possibleUsers = userRepository.findByEmailStartingWith(emailPrefix);
            if (!possibleUsers.isEmpty()) {
                AppUser mentioned = possibleUsers.get(0); // Take the first match
                if (!mentioned.getId().equals(author.getId())) {
                    notificationService.createNotification(mentioned, author, "MENTION", 
                        author.getDisplayName() + " mentioned you in a post.", targetUrl);
                }
                String link = String.format("<a href=\"/profile/%d\" class=\"text-tertiary font-bold hover:underline\">@%s</a>", mentioned.getId(), handle);
                matcher.appendReplacement(sb, link);
            } else {
                matcher.appendReplacement(sb, "@" + handle);
            }
        }
        matcher.appendTail(sb);
        
        // Convert basic newlines to <br> for HTML rendering since we use th:utext now
        String result = sb.toString().trim();
        return result.replace("\r\n", "<br>").replace("\n", "<br>");
    }

    public List<Comment> getComments(Long postId) {
        return commentRepository.findByPostIdWithAuthor(postId);
    }
}
