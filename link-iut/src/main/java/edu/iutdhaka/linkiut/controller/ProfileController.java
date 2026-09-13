package edu.iutdhaka.linkiut.controller;

import edu.iutdhaka.linkiut.model.AppUser;
import edu.iutdhaka.linkiut.model.UserProfile;
import edu.iutdhaka.linkiut.repository.ProfileRepository;
import edu.iutdhaka.linkiut.repository.UserRepository;
import edu.iutdhaka.linkiut.repository.ProfileVisitRepository;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.transaction.annotation.Transactional;

@Controller
@RequestMapping("/profile")
public class ProfileController {

    private final ProfileRepository profileRepository;
    private final UserRepository userRepository;
    private final edu.iutdhaka.linkiut.service.PostService postService;
    private final edu.iutdhaka.linkiut.service.ConnectionService connectionService;
    private final edu.iutdhaka.linkiut.repository.EndorsementRepository endorsementRepository;
    private final ProfileVisitRepository profileVisitRepository;
    private final edu.iutdhaka.linkiut.service.FileUploadService fileUploadService;
    private final edu.iutdhaka.linkiut.repository.ConnectionRepository connectionRepository;

    public ProfileController(ProfileRepository profileRepository, UserRepository userRepository,
            edu.iutdhaka.linkiut.service.PostService postService,
            edu.iutdhaka.linkiut.service.ConnectionService connectionService,
            edu.iutdhaka.linkiut.repository.EndorsementRepository endorsementRepository,
            ProfileVisitRepository profileVisitRepository,
            edu.iutdhaka.linkiut.service.FileUploadService fileUploadService,
            edu.iutdhaka.linkiut.repository.ConnectionRepository connectionRepository) {
        this.profileRepository = profileRepository;
        this.userRepository = userRepository;
        this.postService = postService;
        this.connectionService = connectionService;
        this.endorsementRepository = endorsementRepository;
        this.profileVisitRepository = profileVisitRepository;
        this.fileUploadService = fileUploadService;
        this.connectionRepository = connectionRepository;
    }

    /**
     * View a user's profile with experiences and projects eagerly loaded.
     */
    @Transactional(readOnly = true)
    @GetMapping("/{userId}")
    public String viewProfile(@PathVariable Long userId, Model model,
            @AuthenticationPrincipal UserDetails userDetails) {
        UserProfile profile = profileRepository.findByUserIdWithDetails(userId)
                .orElseThrow(() -> new IllegalArgumentException("Profile not found for user " + userId));

        // Eagerly initialize lazy collections
        profile.getExperiences().size();
        profile.getProjects().size();

        model.addAttribute("profile", profile);
        model.addAttribute("profileUser", profile.getUser());
        model.addAttribute("opportunities", postService.getPostsByUser(userId).stream().filter(edu.iutdhaka.linkiut.model.Post::isOpportunity).toList());
        model.addAttribute("connectionCount", connectionService.getConnectionCount(userId));

        // Add current user for "Start Chat" button visibility
        if (userDetails != null) {
            userRepository.findByEmail(userDetails.getUsername())
                    .ifPresent(u -> {
                        model.addAttribute("currentUser", u);
                        model.addAttribute("isOwnProfile", u.getId().equals(userId));
                        if (!u.getId().equals(userId)) {
                            model.addAttribute("connectionStatus",
                                    connectionService.getConnectionStatus(u.getId(), userId));

                            // Get actual connection ID if needed for accept/reject/remove
                            connectionRepository.findConnectionBetween(u.getId(), userId).ifPresent(conn -> {
                                model.addAttribute("connectionId", conn.getId());
                                model.addAttribute("connectionRequesterId", conn.getRequester().getId());
                            });
                        }
                    });
        }

        java.util.Map<String, Long> endorsementCounts = new java.util.HashMap<>();
        for (Object[] row : endorsementRepository.countBySkillForProfile(profile.getId())) {
            endorsementCounts.put((String) row[0], (Long) row[1]);
        }
        model.addAttribute("endorsementCounts", endorsementCounts);

        // Handle Profile Views
        if (userDetails != null) {
            userRepository.findByEmail(userDetails.getUsername()).ifPresent(viewer -> {
                if (!viewer.getId().equals(userId)) {
                    if (!profileVisitRepository.existsByProfileIdAndViewerId(profile.getId(), viewer.getId())) {
                        profileVisitRepository.save(new edu.iutdhaka.linkiut.model.ProfileVisit(profile, viewer));
                    }
                }
            });
        }
        model.addAttribute("profileViews", profileVisitRepository.countByProfileId(profile.getId()));

        model.addAttribute("activePage", "profile");
        return "profile/view";
    }

    @org.springframework.web.bind.annotation.PostMapping("/{userId}/endorse")
    @org.springframework.web.bind.annotation.ResponseBody
    public String endorseSkill(@PathVariable Long userId,
            @org.springframework.web.bind.annotation.RequestParam String skill,
            @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null)
            return "redirect:/login";
        AppUser endorser = userRepository.findByEmail(userDetails.getUsername()).orElseThrow();
        if (endorser.getId().equals(userId))
            return "<span class='text-error'>Cannot endorse self</span>";

        UserProfile profile = profileRepository.findByUserIdWithDetails(userId).orElseThrow();

        if (!endorsementRepository.existsByProfileIdAndEndorserIdAndSkill(profile.getId(), endorser.getId(), skill)) {
            endorsementRepository.save(new edu.iutdhaka.linkiut.model.Endorsement(profile, endorser, skill));
        }

        long count = endorsementRepository.countByProfileIdAndSkill(profile.getId(), skill);
        return String.valueOf(count);
    }

    @org.springframework.web.bind.annotation.PostMapping("/office-hours")
    @org.springframework.web.bind.annotation.ResponseBody
    public String updateOfficeHours(@org.springframework.web.bind.annotation.RequestParam String officeHours,
            @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null)
            return "Error: Not logged in";

        AppUser currentUser = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new IllegalStateException("User not found"));

        if (currentUser.getRole() != AppUser.Role.ALUMNI) {
            return "Error: Only Alumni can set office hours.";
        }

        UserProfile profile = profileRepository.findByUserIdWithDetails(currentUser.getId())
                .orElseThrow(() -> new IllegalStateException("Profile not found"));

        profile.setOfficeHours(officeHours);
        profileRepository.save(profile);

        return officeHours;
    }

    @GetMapping("/edit")
    public String editProfile(Model model, @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null)
            return "redirect:/login";
        AppUser currentUser = userRepository.findByEmail(userDetails.getUsername()).orElseThrow();
        UserProfile profile = profileRepository.findByUserIdWithDetails(currentUser.getId()).orElse(new UserProfile());
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("profile", profile);
        model.addAttribute("activePage", "profile");
        return "profile/edit";
    }

    @org.springframework.web.bind.annotation.PostMapping("/edit")
    public String saveProfile(@org.springframework.web.bind.annotation.RequestParam String displayName,
            @org.springframework.web.bind.annotation.RequestParam(required = false) String headline,
            @org.springframework.web.bind.annotation.RequestParam(required = false) String bio,
            @org.springframework.web.bind.annotation.RequestParam(required = false) String department,
            @org.springframework.web.bind.annotation.RequestParam(required = false) String batch,
            @org.springframework.web.bind.annotation.RequestParam(required = false) String hostel,
            @org.springframework.web.bind.annotation.RequestParam(required = false) String location,
            @org.springframework.web.bind.annotation.RequestParam(required = false) String linkedinUrl,
            @org.springframework.web.bind.annotation.RequestParam(required = false) String githubUrl,
            @org.springframework.web.bind.annotation.RequestParam(required = false) String skills,
            @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null)
            return "redirect:/login";
        AppUser currentUser = userRepository.findByEmail(userDetails.getUsername()).orElseThrow();

        currentUser.setDisplayName(displayName);
        userRepository.save(currentUser);

        UserProfile profile = profileRepository.findByUserIdWithDetails(currentUser.getId()).orElse(new UserProfile());
        profile.setUser(currentUser);
        profile.setHeadline(headline);
        profile.setBio(bio);
        profile.setDepartment(department);
        profile.setBatch(batch);
        profile.setHostel(hostel);
        profile.setLocation(location);
        profile.setLinkedinUrl(linkedinUrl);
        profile.setGithubUrl(githubUrl);
        profile.setSkills(skills);
        profileRepository.save(profile);

        return "redirect:/profile/" + currentUser.getId();
    }

    @org.springframework.web.bind.annotation.PostMapping("/avatar")
    public String uploadAvatar(@org.springframework.web.bind.annotation.RequestParam("avatar") org.springframework.web.multipart.MultipartFile avatar,
                               @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) return "redirect:/login";
        AppUser currentUser = userRepository.findByEmail(userDetails.getUsername()).orElseThrow();
        
        if (!avatar.isEmpty()) {
            String avatarUrl = fileUploadService.storeFile(avatar);
            currentUser.setAvatarUrl(avatarUrl);
            userRepository.save(currentUser);
        }
        
        return "redirect:/profile/edit";
    }

    @org.springframework.web.bind.annotation.PostMapping("/avatar/remove")
    public String removeAvatar(@AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) return "redirect:/login";
        AppUser currentUser = userRepository.findByEmail(userDetails.getUsername()).orElseThrow();
        
        if (currentUser.getAvatarUrl() != null) {
            currentUser.setAvatarUrl(null);
            userRepository.save(currentUser);
        }
        
        return "redirect:/profile/edit";
    }

    @org.springframework.web.bind.annotation.PostMapping("/experience")
    public String addExperience(edu.iutdhaka.linkiut.model.Experience experience, Model model,
            @AuthenticationPrincipal UserDetails userDetails) {
        AppUser currentUser = userRepository.findByEmail(userDetails.getUsername()).orElseThrow();
        UserProfile profile = profileRepository.findByUserIdWithDetails(currentUser.getId()).orElseThrow();

        experience.setProfile(profile);
        profile.getExperiences().add(experience);
        profileRepository.save(profile);

        // Return the fragment of the newly added experience. Since profile is saved,
        // the last one has the ID.
        model.addAttribute("exp", profile.getExperiences().get(profile.getExperiences().size() - 1));
        return "profile/edit :: experience-item";
    }

    @org.springframework.web.bind.annotation.DeleteMapping("/experience/{id}")
    @org.springframework.web.bind.annotation.ResponseBody
    public String deleteExperience(@PathVariable Long id, @AuthenticationPrincipal UserDetails userDetails) {
        AppUser currentUser = userRepository.findByEmail(userDetails.getUsername()).orElseThrow();
        UserProfile profile = profileRepository.findByUserIdWithDetails(currentUser.getId()).orElseThrow();

        profile.getExperiences().removeIf(e -> e.getId().equals(id));
        profileRepository.save(profile);

        return ""; // return empty string to remove the element in HTMX
    }

    @org.springframework.web.bind.annotation.PostMapping("/project")
    public String addProject(edu.iutdhaka.linkiut.model.Project project, Model model,
            @AuthenticationPrincipal UserDetails userDetails) {
        AppUser currentUser = userRepository.findByEmail(userDetails.getUsername()).orElseThrow();
        UserProfile profile = profileRepository.findByUserIdWithDetails(currentUser.getId()).orElseThrow();

        project.setProfile(profile);
        profile.getProjects().add(project);
        profileRepository.save(profile);

        model.addAttribute("proj", profile.getProjects().get(profile.getProjects().size() - 1));
        return "profile/edit :: project-item";
    }

    @org.springframework.web.bind.annotation.DeleteMapping("/project/{id}")
    @org.springframework.web.bind.annotation.ResponseBody
    public String deleteProject(@PathVariable Long id, @AuthenticationPrincipal UserDetails userDetails) {
        AppUser currentUser = userRepository.findByEmail(userDetails.getUsername()).orElseThrow();
        UserProfile profile = profileRepository.findByUserIdWithDetails(currentUser.getId()).orElseThrow();

        profile.getProjects().removeIf(p -> p.getId().equals(id));
        profileRepository.save(profile);

        return "";
    }
}
