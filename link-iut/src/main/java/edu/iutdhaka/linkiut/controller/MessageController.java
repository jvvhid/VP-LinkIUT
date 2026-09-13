package edu.iutdhaka.linkiut.controller;

import edu.iutdhaka.linkiut.model.AppUser;
import edu.iutdhaka.linkiut.model.ChatSession;
import edu.iutdhaka.linkiut.model.Message;
import edu.iutdhaka.linkiut.repository.UserRepository;
import edu.iutdhaka.linkiut.service.FileUploadService;
import edu.iutdhaka.linkiut.service.MessageService;
import org.springframework.web.multipart.MultipartFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;

@Controller
@RequestMapping("/messages")
public class MessageController {

    private static final Logger log = LoggerFactory.getLogger(MessageController.class);

    private final MessageService messageService;
    private final UserRepository userRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final FileUploadService fileUploadService;

    public MessageController(MessageService messageService, UserRepository userRepository, 
                             SimpMessagingTemplate messagingTemplate, FileUploadService fileUploadService) {
        this.messageService = messageService;
        this.userRepository = userRepository;
        this.messagingTemplate = messagingTemplate;
        this.fileUploadService = fileUploadService;
    }

    /**
     * Chat list — all sessions for the current user (no chat selected).
     */
    @GetMapping
    public String chatList(Model model, @AuthenticationPrincipal UserDetails userDetails) {
        AppUser currentUser = getCurrentUser(userDetails);
        List<ChatSession> sessions = messageService.getUserSessions(currentUser.getId());
        model.addAttribute("chatSessions", sessions);
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("activeChat", null);
        model.addAttribute("chatMessages", Collections.emptyList());
        model.addAttribute("lastMessageId", 0L);
        model.addAttribute("activePage", "messages");
        model.addAttribute("allUsers", userRepository.findAll().stream()
            .filter(u -> !u.getId().equals(currentUser.getId())).toList());
        return "messages/chat";
    }

    /**
     * Open a specific chat session.
     */
    @GetMapping("/{sessionId}")
    public String chat(@PathVariable Long sessionId, Model model,
                       @AuthenticationPrincipal UserDetails userDetails) {
        try {
            AppUser currentUser = getCurrentUser(userDetails);
            ChatSession chatSession = messageService.getSession(sessionId);
            List<Message> messages = messageService.getChatHistory(sessionId);

            model.addAttribute("activeChat", chatSession);
            model.addAttribute("chatMessages", messages);
            model.addAttribute("currentUser", currentUser);
            model.addAttribute("chatSessions", messageService.getUserSessions(currentUser.getId()));
            model.addAttribute("lastMessageId", messages.isEmpty() ? 0L : messages.get(messages.size() - 1).getId());
            model.addAttribute("activePage", "messages");
            model.addAttribute("allUsers", userRepository.findAll().stream()
                .filter(u -> !u.getId().equals(currentUser.getId())).toList());
            return "messages/chat";
        } catch (Exception e) {
            log.error("Error opening chat {}: {}", sessionId, e.getMessage());
            return "redirect:/messages";
        }
    }

    /**
     * Start a new chat with a user (from profile page).
     */
    @PostMapping("/start/{responderId}")
    public String startChat(@PathVariable Long responderId,
                            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            AppUser currentUser = getCurrentUser(userDetails);
            ChatSession chatSession = messageService.getOrCreateSession(currentUser.getId(), responderId);
            return "redirect:/messages/" + chatSession.getId();
        } catch (Exception e) {
            log.error("Error starting chat with user {}: {}", responderId, e.getMessage());
            return "redirect:/messages";
        }
    }

    /**
     * Create a new group chat.
     */
    @PostMapping("/group/create")
    public String createGroupChat(@RequestParam String name,
                                  @RequestParam(required = false) MultipartFile coverPhoto,
                                  @RequestParam(required = false) List<Long> participants,
                                  @AuthenticationPrincipal UserDetails userDetails) {
        try {
            if (participants == null || participants.isEmpty()) {
                return "redirect:/messages?error=NoParticipantsSelected";
            }
            AppUser currentUser = getCurrentUser(userDetails);
            String coverUrl = null;
            if (coverPhoto != null && !coverPhoto.isEmpty()) {
                coverUrl = fileUploadService.storeFile(coverPhoto);
            }
            ChatSession groupSession = messageService.createGroupSession(name, coverUrl, currentUser.getId(), participants);
            return "redirect:/messages/" + groupSession.getId();
        } catch (Exception e) {
            log.error("Error creating group chat: {}", e.getMessage());
            return "redirect:/messages";
        }
    }

    /**
     * HTMX: send a message, return the new message bubble fragment.
     */
    @PostMapping("/{sessionId}/send")
    public String sendMessage(@PathVariable Long sessionId,
                               @RequestParam(required = false) String content,
                               @RequestParam(required = false) MultipartFile photo,
                               @RequestParam(required = false) MultipartFile video,
                               @RequestParam(required = false) MultipartFile audio,
                               Model model,
                               @AuthenticationPrincipal UserDetails userDetails) {
        AppUser currentUser = getCurrentUser(userDetails);
        try {
            String photoUrl = photo != null && !photo.isEmpty() ? fileUploadService.storeFile(photo) : null;
            String videoUrl = video != null && !video.isEmpty() ? fileUploadService.storeFile(video) : null;
            String audioUrl = audio != null && !audio.isEmpty() ? fileUploadService.storeFile(audio) : null;
            
            // Only send if there's actual content or media
            if ((content == null || content.trim().isEmpty()) && photoUrl == null && videoUrl == null && audioUrl == null) {
                return "messages/chat :: message-bubble"; // do nothing
            }

            Message msg = messageService.sendMessage(sessionId, currentUser.getId(), content, photoUrl, videoUrl, audioUrl);
            ChatSession chatSession = messageService.getSession(sessionId);
            
            // Broadcast over WebSocket to trigger background updates for other participants
            messagingTemplate.convertAndSend("/topic/chat/" + sessionId, "NEW_MESSAGE");
            
            model.addAttribute("msg", msg);
            model.addAttribute("currentUser", currentUser);
            model.addAttribute("activeChat", chatSession);
            return "messages/chat :: message-bubble";
        } catch (IllegalStateException e) {
            model.addAttribute("error", e.getMessage());
            return "messages/chat :: session-expired";
        }
    }

    /**
     * HTMX + WebSocket polling: return new messages since lastId and swap the loader.
     */
    @GetMapping("/{sessionId}/poll")
    public String pollMessages(@PathVariable Long sessionId,
                                @RequestParam(defaultValue = "0") Long lastId,
                                Model model,
                                @AuthenticationPrincipal UserDetails userDetails) {
        AppUser currentUser = getCurrentUser(userDetails);
        List<Message> newMessages = messageService.getNewMessages(sessionId, lastId);
        ChatSession chatSession = messageService.getSession(sessionId);

        model.addAttribute("chatMessages", newMessages);
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("activeChat", chatSession);
        model.addAttribute("lastMessageId", newMessages.isEmpty() ? lastId : newMessages.get(newMessages.size() - 1).getId());
        return "messages/chat :: new-messages-loader";
    }

    private AppUser getCurrentUser(UserDetails userDetails) {
        return userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new IllegalStateException("Logged-in user not found"));
    }
}
