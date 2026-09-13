package edu.iutdhaka.linkiut.controller;

import edu.iutdhaka.linkiut.model.AppUser;
import edu.iutdhaka.linkiut.repository.UserRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
public class MentionController {

    private final UserRepository userRepository;

    public MentionController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping("/search-mentions")
    public List<Map<String, String>> searchMentions(@RequestParam(name = "q", defaultValue = "") String query) {
        // Find users matching displayName or email
        List<AppUser> users = userRepository.findAll().stream()
                .filter(u -> u.getDisplayName().toLowerCase().contains(query.toLowerCase()) || 
                             u.getEmail().toLowerCase().contains(query.toLowerCase()))
                .limit(10)
                .collect(Collectors.toList());

        return users.stream().map(u -> {
            String handle = u.getEmail().substring(0, u.getEmail().indexOf('@'));
            return Map.of(
                    "displayName", u.getDisplayName(),
                    "handle", handle,
                    "avatarUrl", u.getAvatarUrl() != null ? u.getAvatarUrl() : "",
                    "initials", u.getInitials()
            );
        }).collect(Collectors.toList());
    }
}
