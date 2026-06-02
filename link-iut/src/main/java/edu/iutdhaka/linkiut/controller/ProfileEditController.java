package edu.iutdhaka.linkiut.controller;

import edu.iutdhaka.linkiut.model.AppUser;
import edu.iutdhaka.linkiut.model.UserProfile;
import edu.iutdhaka.linkiut.model.Project;
import edu.iutdhaka.linkiut.repository.ProfileRepository;
import edu.iutdhaka.linkiut.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Base64;

@Controller
public class ProfileEditController {

    private static final Logger log = LoggerFactory.getLogger(ProfileEditController.class);

    private final UserRepository userRepository;
    private final ProfileRepository profileRepository;

    public ProfileEditController(UserRepository userRepository, ProfileRepository profileRepository) {
        this.userRepository = userRepository;
        this.profileRepository = profileRepository;
    }

    /**
     * Show the profile edit form for the currently authenticated user.
     */
    @GetMapping("/profile/edit")
    public String showEditForm(Model model, @AuthenticationPrincipal UserDetails userDetails) {
        AppUser user = getCurrentUser(userDetails);
        UserProfile profile = profileRepository.findByUser_Id(user.getId())
                .orElseGet(() -> {
                    UserProfile p = new UserProfile();
                    p.setUser(user);
                    return profileRepository.save(p);
                });

        model.addAttribute("user", user);
        model.addAttribute("profile", profile);
        model.addAttribute("currentUser", user);
        return "profile/edit";
    }

    /**
     * Save profile changes including optional avatar upload.
     * Avatar is stored as a Base64 data URI in the user's avatarUrl field.
     */
    @PostMapping("/profile/edit")
    @Transactional
    public String saveProfile(@RequestParam String displayName,
                               @RequestParam(required = false) String headline,
                               @RequestParam(required = false) String bio,
                               @RequestParam(required = false) String department,
                               @RequestParam(required = false) String batch,
                               @RequestParam(required = false) String currentCompany,
                               @RequestParam(required = false) String location,
                               @RequestParam(required = false) String linkedinUrl,
                               @RequestParam(required = false) String[] projectName,
                               @RequestParam(required = false) String[] projectDesc,
                               @RequestParam(required = false) String[] projectRepoUrl,
                               @RequestParam(required = false) String[] projectTechStack,
                               @RequestParam(required = false) MultipartFile avatar,
                               @AuthenticationPrincipal UserDetails userDetails) {
        AppUser user = getCurrentUser(userDetails);

        // Update display name
        if (displayName != null && !displayName.trim().isEmpty()) {
            user.setDisplayName(displayName.trim());
        }

        // Handle avatar upload — convert to Base64 data URI
        if (avatar != null && !avatar.isEmpty()) {
            try {
                String contentType = avatar.getContentType();
                if (contentType != null && contentType.startsWith("image/")) {
                    byte[] bytes = avatar.getBytes();
                    String base64 = Base64.getEncoder().encodeToString(bytes);
                    String dataUri = "data:" + contentType + ";base64," + base64;
                    user.setAvatarUrl(dataUri);
                    log.info("Avatar uploaded for user {}: {}KB", user.getEmail(), bytes.length / 1024);
                }
            } catch (IOException e) {
                log.error("Failed to process avatar upload for {}: {}", user.getEmail(), e.getMessage());
            }
        }

        userRepository.save(user);

        // Update profile
        UserProfile profile = profileRepository.findByUser_Id(user.getId())
                .orElseGet(() -> {
                    UserProfile p = new UserProfile();
                    p.setUser(user);
                    return p;
                });

        profile.setHeadline(trimOrNull(headline));
        profile.setBio(trimOrNull(bio));
        profile.setDepartment(trimOrNull(department));
        profile.setBatch(trimOrNull(batch));
        profile.setCurrentCompany(trimOrNull(currentCompany));
        profile.setLocation(trimOrNull(location));
        profile.setLinkedinUrl(trimOrNull(linkedinUrl));

        // Update projects
        profile.getProjects().clear();
        if (projectName != null) {
            for (int i = 0; i < projectName.length; i++) {
                String pName = trimOrNull(projectName[i]);
                if (pName != null) {
                    Project p = new Project();
                    p.setName(pName);
                    p.setDescription(projectDesc != null && i < projectDesc.length ? trimOrNull(projectDesc[i]) : null);
                    p.setRepoUrl(projectRepoUrl != null && i < projectRepoUrl.length ? trimOrNull(projectRepoUrl[i]) : null);
                    p.setTechStack(projectTechStack != null && i < projectTechStack.length ? trimOrNull(projectTechStack[i]) : null);
                    p.setProfile(profile);
                    profile.getProjects().add(p);
                }
            }
        }

        profileRepository.save(profile);

        log.info("Profile updated for user: {}", user.getEmail());
        return "redirect:/profile/" + user.getId();
    }

    private AppUser getCurrentUser(UserDetails userDetails) {
        return userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new IllegalStateException("Logged-in user not found"));
    }

    private String trimOrNull(String value) {
        if (value == null || value.trim().isEmpty()) return null;
        return value.trim();
    }
}
