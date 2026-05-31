package edu.iutdhaka.linkiut.controller;

import edu.iutdhaka.linkiut.model.AppUser;
import edu.iutdhaka.linkiut.model.UserProfile;
import edu.iutdhaka.linkiut.repository.ProfileRepository;
import edu.iutdhaka.linkiut.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class RegistrationController {

    private static final Logger log = LoggerFactory.getLogger(RegistrationController.class);

    private final UserRepository userRepository;
    private final ProfileRepository profileRepository;
    private final PasswordEncoder passwordEncoder;

    public RegistrationController(UserRepository userRepository,
                                   ProfileRepository profileRepository,
                                   PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.profileRepository = profileRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        model.addAttribute("roles", AppUser.Role.values());
        return "register";
    }

    @PostMapping("/register")
    @Transactional
    public String registerUser(@RequestParam String displayName,
                                @RequestParam String email,
                                @RequestParam String password,
                                @RequestParam String confirmPassword,
                                @RequestParam String role,
                                @RequestParam(required = false, defaultValue = "") String department,
                                @RequestParam(required = false, defaultValue = "") String batch,
                                @RequestParam(required = false, defaultValue = "") String headline,
                                Model model) {
        try {
            // Validation
            if (displayName == null || displayName.trim().isEmpty()) {
                return showError(model, "Display name is required.");
            }

            if (email == null || email.trim().isEmpty()) {
                return showError(model, "Email is required.");
            }

            if (password.length() < 6) {
                return showError(model, "Password must be at least 6 characters.");
            }

            if (!password.equals(confirmPassword)) {
                return showError(model, "Passwords do not match.");
            }

            if (userRepository.existsByEmail(email.trim())) {
                return showError(model, "An account with this email already exists.");
            }

            // Parse role safely
            AppUser.Role userRole;
            try {
                userRole = AppUser.Role.valueOf(role.toUpperCase());
            } catch (IllegalArgumentException e) {
                return showError(model, "Invalid role selected.");
            }

            // Create user
            AppUser user = new AppUser(
                    email.trim(),
                    passwordEncoder.encode(password),
                    userRole,
                    displayName.trim()
            );
            user = userRepository.save(user);

            // Create profile
            UserProfile profile = new UserProfile();
            profile.setUser(user);
            if (department != null && !department.isBlank()) {
                profile.setDepartment(department.trim());
            }
            if (batch != null && !batch.isBlank()) {
                profile.setBatch(batch.trim());
            }
            if (headline != null && !headline.isBlank()) {
                profile.setHeadline(headline.trim());
            }
            profileRepository.save(profile);

            log.info("New user registered: {} ({})", email, userRole);
            return "redirect:/login?registered";

        } catch (Exception e) {
            log.error("Registration failed for {}: {}", email, e.getMessage(), e);
            return showError(model, "Registration failed. Please try again.");
        }
    }

    private String showError(Model model, String message) {
        model.addAttribute("error", message);
        model.addAttribute("roles", AppUser.Role.values());
        return "register";
    }
}
