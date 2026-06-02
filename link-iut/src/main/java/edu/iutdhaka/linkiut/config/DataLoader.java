package edu.iutdhaka.linkiut.config;

import edu.iutdhaka.linkiut.model.*;
import edu.iutdhaka.linkiut.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Seeds demo data on startup using the real PasswordEncoder bean,
 * so passwords are guaranteed to match at login time.
 * Only runs when the database is empty (idempotent).
 */
@Component
public class DataLoader implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataLoader.class);

    private final UserRepository userRepository;
    private final ProfileRepository profileRepository;
    private final OpportunityRepository opportunityRepository;
    private final PasswordEncoder passwordEncoder;

    public DataLoader(UserRepository userRepository,
                      ProfileRepository profileRepository,
                      OpportunityRepository opportunityRepository,
                      PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.profileRepository = profileRepository;
        this.opportunityRepository = opportunityRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (userRepository.count() > 0) {
            log.info("DataLoader: users already exist, skipping seed.");
            return;
        }

        log.info("DataLoader: seeding demo data...");

        // Encode "password" with BCrypt — this is the REAL hash
        String encodedPassword = passwordEncoder.encode("password");

        // ── Alumni user ──────────────────────────────────────────
        AppUser alumni = new AppUser("alumni@iut.edu", encodedPassword, AppUser.Role.ALUMNI, "Farhan Rahman");
        alumni.setAvatarUrl(generateSvgAvatar("FR", "#3377ff", "#1440e1"));
        alumni = userRepository.save(alumni);

        UserProfile alumniProfile = new UserProfile();
        alumniProfile.setUser(alumni);
        alumniProfile.setHeadline("Senior SWE @ Google");
        alumniProfile.setBio("IUT CSE '18 alumnus passionate about distributed systems and mentoring.");
        alumniProfile.setDepartment("CSE");
        alumniProfile.setBatch("2018");
        alumniProfile.setCurrentCompany("Google");
        alumniProfile.setLocation("Mountain View, CA");
        alumniProfile.setLinkedinUrl("https://linkedin.com/in/farhanr");

        Experience exp1 = new Experience();
        exp1.setProfile(alumniProfile);
        exp1.setTitle("Senior Software Engineer");
        exp1.setCompany("Google");
        exp1.setStartDate(LocalDate.of(2022, 6, 1));
        exp1.setDescription("Building large-scale data infrastructure on Google Cloud.");
        alumniProfile.getExperiences().add(exp1);

        Experience exp2 = new Experience();
        exp2.setProfile(alumniProfile);
        exp2.setTitle("Software Engineer");
        exp2.setCompany("Pathao");
        exp2.setStartDate(LocalDate.of(2019, 1, 15));
        exp2.setEndDate(LocalDate.of(2022, 5, 30));
        exp2.setDescription("Led the ride-sharing matching algorithm team.");
        alumniProfile.getExperiences().add(exp2);

        Project proj1 = new Project();
        proj1.setProfile(alumniProfile);
        proj1.setName("DistCache");
        proj1.setDescription("Distributed caching layer for microservices");
        proj1.setTechStack("Java, gRPC, Redis");
        proj1.setRepoUrl("https://github.com/farhanr/distcache");
        alumniProfile.getProjects().add(proj1);

        profileRepository.save(alumniProfile);

        // ── Student user ─────────────────────────────────────────
        AppUser student = new AppUser("student@iut.edu", encodedPassword, AppUser.Role.STUDENT, "Nadia Hossain");
        student.setAvatarUrl(generateSvgAvatar("NH", "#2dd4bf", "#0d9488"));
        student = userRepository.save(student);

        UserProfile studentProfile = new UserProfile();
        studentProfile.setUser(student);
        studentProfile.setHeadline("Aspiring ML Engineer");
        studentProfile.setBio("Final-year CSE student exploring NLP, computer vision, and open source.");
        studentProfile.setDepartment("CSE");
        studentProfile.setBatch("2024");
        studentProfile.setLocation("Dhaka, BD");
        studentProfile.setLinkedinUrl("https://linkedin.com/in/nadiah");

        Experience exp3 = new Experience();
        exp3.setProfile(studentProfile);
        exp3.setTitle("ML Intern");
        exp3.setCompany("Grameenphone");
        exp3.setStartDate(LocalDate.of(2024, 6, 1));
        exp3.setEndDate(LocalDate.of(2024, 8, 31));
        exp3.setDescription("Developed a churn-prediction pipeline with XGBoost.");
        studentProfile.getExperiences().add(exp3);

        Project proj2 = new Project();
        proj2.setProfile(studentProfile);
        proj2.setName("BanglaOCR");
        proj2.setDescription("Optical character recognition for Bangla script");
        proj2.setTechStack("Python, PyTorch, OpenCV");
        proj2.setRepoUrl("https://github.com/nadiah/banglaocr");
        studentProfile.getProjects().add(proj2);

        profileRepository.save(studentProfile);

        // ── Opportunities ────────────────────────────────────────
        Opportunity opp1 = new Opportunity();
        opp1.setTitle("Backend Engineer Intern – Google");
        opp1.setDescription("Join the Cloud Storage team for a 12-week summer internship. Work on petabyte-scale systems.");
        opp1.setType(Opportunity.OpportunityType.INTERNSHIP);
        opp1.setPostedBy(alumni);
        opp1.setExpiresAt(LocalDateTime.now().plusDays(30));
        opportunityRepository.save(opp1);

        Opportunity opp2 = new Opportunity();
        opp2.setTitle("Research Collaboration on Systems");
        opp2.setDescription("Looking for a student researcher for a paper on distributed systems design.");
        opp2.setType(Opportunity.OpportunityType.RESEARCH_CONTRIBUTION);
        opp2.setPostedBy(alumni);
        opp2.setExpiresAt(LocalDateTime.now().plusDays(60));
        opportunityRepository.save(opp2);

        Opportunity opp3 = new Opportunity();
        opp3.setTitle("Junior Full-Stack Developer – Pathao");
        opp3.setDescription("Pathao is hiring! Spring Boot + React. Remote-friendly.");
        opp3.setType(Opportunity.OpportunityType.JOB);
        opp3.setPostedBy(alumni);
        opp3.setExpiresAt(LocalDateTime.now().plusDays(45));
        opportunityRepository.save(opp3);

        log.info("DataLoader: ✅ seeded 2 users, 3 experiences, 2 projects, 3 opportunities.");
    }

    /**
     * Generates a simple SVG avatar as a data URI.
     * Creates a gradient circle with centered initials text.
     */
    private String generateSvgAvatar(String initials, String color1, String color2) {
        String svg = "<svg xmlns='http://www.w3.org/2000/svg' width='200' height='200' viewBox='0 0 200 200'>"
                + "<defs><linearGradient id='g' x1='0%' y1='0%' x2='100%' y2='100%'>"
                + "<stop offset='0%' stop-color='" + color1 + "'/>"
                + "<stop offset='100%' stop-color='" + color2 + "'/>"
                + "</linearGradient></defs>"
                + "<rect width='200' height='200' fill='url(#g)' rx='20'/>"
                + "<text x='100' y='115' font-family='Inter,sans-serif' font-size='72' font-weight='700' "
                + "fill='white' text-anchor='middle'>" + initials + "</text>"
                + "</svg>";
        String base64 = java.util.Base64.getEncoder().encodeToString(svg.getBytes());
        return "data:image/svg+xml;base64," + base64;
    }
}
