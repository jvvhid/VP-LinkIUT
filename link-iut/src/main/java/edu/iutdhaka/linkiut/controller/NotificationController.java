package edu.iutdhaka.linkiut.controller;

import edu.iutdhaka.linkiut.model.AppUser;
import edu.iutdhaka.linkiut.model.Notification;
import edu.iutdhaka.linkiut.repository.UserRepository;
import edu.iutdhaka.linkiut.service.NotificationService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
@RequestMapping("/notifications")
public class NotificationController {

    private final NotificationService notificationService;
    private final UserRepository userRepository;

    public NotificationController(NotificationService notificationService, UserRepository userRepository) {
        this.notificationService = notificationService;
        this.userRepository = userRepository;
    }

    @GetMapping
    public String viewNotifications(Model model, @AuthenticationPrincipal UserDetails userDetails) {
        AppUser currentUser = getCurrentUser(userDetails);
        List<Notification> notifications = notificationService.getUserNotifications(currentUser.getId());
        
        // Mark all as read when viewing the page
        notificationService.markAllAsRead(currentUser.getId());
        
        model.addAttribute("notifications", notifications);
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("activePage", "notifications");
        
        return "notifications/index";
    }

    @GetMapping("/unread-count")
    @ResponseBody
    public String getUnreadCount(@AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) return "0";
        AppUser currentUser = getCurrentUser(userDetails);
        long count = notificationService.getUnreadCount(currentUser.getId());
        return String.valueOf(count);
    }

    private AppUser getCurrentUser(UserDetails userDetails) {
        return userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new IllegalStateException("Logged-in user not found"));
    }
}
