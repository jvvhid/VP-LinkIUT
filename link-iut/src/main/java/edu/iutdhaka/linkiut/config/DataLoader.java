package edu.iutdhaka.linkiut.config;

import edu.iutdhaka.linkiut.model.*;
import edu.iutdhaka.linkiut.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.jdbc.core.JdbcTemplate;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

@Component
public class DataLoader implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataLoader.class);

    private final UserRepository userRepository;
    private final ProfileRepository profileRepository;
    private final PasswordEncoder passwordEncoder;
    private final edu.iutdhaka.linkiut.repository.PostRepository postRepository;
    private final edu.iutdhaka.linkiut.repository.PostLikeRepository postLikeRepository;
    private final edu.iutdhaka.linkiut.repository.CommentRepository commentRepository;
    private final JdbcTemplate jdbcTemplate;

    public DataLoader(UserRepository userRepository,
                      ProfileRepository profileRepository,
                      PasswordEncoder passwordEncoder,
                      edu.iutdhaka.linkiut.repository.PostRepository postRepository,
                      edu.iutdhaka.linkiut.repository.PostLikeRepository postLikeRepository,
                      edu.iutdhaka.linkiut.repository.CommentRepository commentRepository,
                      JdbcTemplate jdbcTemplate) {
        this.userRepository = userRepository;
        this.profileRepository = profileRepository;
        this.passwordEncoder = passwordEncoder;
        this.postRepository = postRepository;
        this.postLikeRepository = postLikeRepository;
        this.commentRepository = commentRepository;
        this.jdbcTemplate = jdbcTemplate;
    }

    @org.springframework.beans.factory.annotation.Autowired
    @org.springframework.context.annotation.Lazy
    private DataLoader self;

    @Override
    public void run(String... args) {
        log.info("DataLoader: Updating existing users and seeding demo data...");

        try {
            // Fix schema constraints from old initiator/responder mapping
            jdbcTemplate.execute("ALTER TABLE chat_session MODIFY initiator_id BIGINT NULL");
            jdbcTemplate.execute("ALTER TABLE chat_session MODIFY responder_id BIGINT NULL");
            
            // Migrate legacy 1-on-1 chats to use the new participants join table
            jdbcTemplate.execute("INSERT IGNORE INTO chat_session_participants (chat_session_id, user_id) SELECT id, initiator_id FROM chat_session WHERE initiator_id IS NOT NULL");
            jdbcTemplate.execute("INSERT IGNORE INTO chat_session_participants (chat_session_id, user_id) SELECT id, responder_id FROM chat_session WHERE responder_id IS NOT NULL");
        } catch (Exception e) {
            log.warn("Could not alter chat_session table or migrate participants (might be fine if already migrated): " + e.getMessage());
        }

        self.seedData();
    }

    @Transactional
    public void seedData() {
        String encodedPassword = passwordEncoder.encode("123456");

        // 1. Update existing users password and student demo account
        List<AppUser> existingUsers = userRepository.findAll();
        for (AppUser user : existingUsers) {
            user.setPasswordHash(encodedPassword);
            
            // Update demo student account
            if ("student@iut.edu".equals(user.getEmail())) {
                user.setDisplayName("Jahidul Islam");
                
                profileRepository.findByUser_Id(user.getId()).ifPresent(profile -> {
                    profile.setHeadline("CSE Student | Developer");
                    profile.setBio("Passionate about tech and software development. IUT CSE batch 2023");
                    profile.setDepartment("CSE");
                    profile.setBatch("2023");
                    profile.setGithubUrl("https://github.com/jvvhid");
                    
                    // Clear existing projects and add actual github projects
                    profile.getProjects().clear();
                    
                    Project p1 = new Project();
                    p1.setProfile(profile);
                        p1.setName("LinkIUT");
                        p1.setDescription("Professional networking platform for IUT alumni and students.");
                        p1.setRepoUrl("https://github.com/jvvhid/LinkIUT");
                        profile.getProjects().add(p1);
                        
                        Project p2 = new Project();
                        p2.setProfile(profile);
                        p2.setName("Portfolio");
                        p2.setDescription("Portfolio Website");
                        p2.setRepoUrl("https://github.com/jvvhid/Portfolio");
                        profile.getProjects().add(p2);
                    profileRepository.save(profile);
                });
            }
            userRepository.save(user);
        }

        // 2. Seed 12 new profiles if not enough users
        if (userRepository.count() < 12) {
            seedProfiles(encodedPassword);
        }
        
        log.info("DataLoader: ✅ Finished data seeding and password updates.");
    }
    
    private void seedProfiles(String encodedPassword) {
        Random rand = new Random();
        
        // Data sets
        String[] names = {"Aminul Islam", "Sadia Afrin", "Tahsin Rahman", "Rifat Hossain", "Nusrat Jahan", "Shafiqul Alam", "Mehedi Hasan", "Sabrina Zaman", "Ashraful Haque", "Zarin Tasnim", "Farhan Ahmed", "Kamrul Hasan"};
        String[] emails = {"aminul", "sadia", "tahsin", "rifat", "nusrat", "shafiqul", "mehedi", "sabrina", "ashraful", "zarin", "farhan", "kamrul"};
        AppUser.Role[] roles = {AppUser.Role.ALUMNI, AppUser.Role.STUDENT, AppUser.Role.ALUMNI, AppUser.Role.STUDENT, AppUser.Role.ALUMNI, AppUser.Role.STUDENT, AppUser.Role.ALUMNI, AppUser.Role.STUDENT, AppUser.Role.ALUMNI, AppUser.Role.STUDENT, AppUser.Role.ALUMNI, AppUser.Role.STUDENT};
        String[] depts = {"CSE", "CSE", "EEE", "EEE", "MCE", "MCE", "CEE", "CEE", "SWE", "SWE", "BTM", "BTM"};
        String[] headlines = {"DevOps Engineer", "Frontend Enthusiast", "Power Systems Eng", "Robotics Learner", "Mechanical Engineer", "CAD Designer", "Civil Engineer", "Structural Intern", "Software Architect", "App Developer", "Product Manager", "Business Analyst"};
        String[] companies = {"BrainStation", "N/A", "EnergyPac", "N/A", "Walton", "N/A", "BSRM", "N/A", "Enosis", "N/A", "Pathao", "N/A"};
        String[] locations = {"Dhaka, BD", "Gazipur, BD", "Dhaka, BD", "Gazipur, BD", "Chittagong, BD", "Gazipur, BD", "Dhaka, BD", "Gazipur, BD", "Dhaka, BD", "Gazipur, BD", "Dhaka, BD", "Gazipur, BD"};
        
        AppUser[] newUsers = new AppUser[12];
        
        for (int i = 0; i < 12; i++) {
            AppUser u = new AppUser(emails[i] + "@iut.edu", encodedPassword, roles[i], names[i]);
            newUsers[i] = userRepository.save(u);
            
            UserProfile p = new UserProfile();
            p.setUser(u);
            p.setHeadline(headlines[i]);
            p.setBio("Passionate about tech. IUT " + depts[i] + " batch " + (roles[i] == AppUser.Role.ALUMNI ? "20" + (15 + rand.nextInt(5)) : "20" + (23 + rand.nextInt(3))));
            p.setDepartment(depts[i]);
            p.setBatch(roles[i] == AppUser.Role.ALUMNI ? "20" + (15 + rand.nextInt(5)) : "20" + (23 + rand.nextInt(3)));
            p.setCurrentCompany(companies[i]);
            p.setLocation(locations[i]);
            
            Experience exp = new Experience();
            exp.setProfile(p);
            exp.setTitle(headlines[i]);
            exp.setCompany(companies[i].equals("N/A") ? "Self-Employed" : companies[i]);
            exp.setStartDate(LocalDate.now().minusYears(rand.nextInt(3) + 1));
            p.getExperiences().add(exp);
            
            profileRepository.save(p);
        }
        
        // Seed Opportunities (as Posts)
        String[] oppTitles = {"Backend Dev Needed", "Summer Internship", "Thesis Partner", "Power Grid Design", "CAD Engineer", "Structural Analysis", "Looking for Co-founder", "Research Assistant", "UX Internship", "DevOps Engineer", "Business Strategy Role", "Data Analyst Intern"};
        String[] oppTypes = {"JOB", "INTERNSHIP", "THESIS_MATCHMAKING", "JOB", "JOB", "JOB", "MENTORSHIP", "INTERNSHIP", "INTERNSHIP", "JOB", "JOB", "INTERNSHIP"};
        
        for (int i = 0; i < 12; i++) {
            int userIdx = i % 12;
            if (roles[userIdx] == AppUser.Role.STUDENT && !oppTypes[i].equals("THESIS_MATCHMAKING")) {
                for(int j=0; j<12; j++) if(roles[j] == AppUser.Role.ALUMNI) { userIdx = j; break; }
            }
            Post opp = new Post();
            opp.setOpportunityTitle(oppTitles[i]);
            opp.setContent("We are looking for motivated IUTians for this " + oppTypes[i] + " opportunity. Apply soon!");
            opp.setOpportunityType(oppTypes[i]);
            opp.setOpportunity(true);
            opp.setAuthor(newUsers[userIdx]);
            opp.setCreatedAt(LocalDateTime.now().minusDays(rand.nextInt(10)).minusHours(rand.nextInt(24)));
            postRepository.save(opp);
        }
        
        // Seed Journey Posts
        AppUser student = null;
        for (AppUser u : newUsers) {
            if (u.getRole() == AppUser.Role.STUDENT) {
                student = u;
                break;
            }
        }
        if (student != null) {
            String techImages = "https://images.unsplash.com/photo-1518770660439-4636190af475?auto=format&fit=crop&w=800&q=80," +
                                "https://images.unsplash.com/photo-1550751827-4bd374c3f58b?auto=format&fit=crop&w=800&q=80," +
                                "https://images.unsplash.com/photo-1526374965328-7f61d4dc18c5?auto=format&fit=crop&w=800&q=80," +
                                "https://images.unsplash.com/photo-1451187580459-43490279c0fa?auto=format&fit=crop&w=800&q=80," +
                                "https://images.unsplash.com/photo-1531297172867-681b953460f4?auto=format&fit=crop&w=800&q=80";
            edu.iutdhaka.linkiut.model.Post techPost = new edu.iutdhaka.linkiut.model.Post(student, "Exploring the latest in tech! From AI circuits to robust servers, the future is incredibly exciting. Check out these amazing captures. 🚀👨‍💻");
            techPost.setImageUrl(techImages);
            techPost.setCreatedAt(LocalDateTime.now().minusHours(2));
            postRepository.save(techPost);
        }

        String[] postContents = {
            "Just deployed my first Spring Boot application to AWS! #milestone",
            "Looking for recommendations on books for learning System Design.",
            "Excited to announce I'll be joining TigerIT next month!",
            "Finally solved that nasty bug that kept me up all night. The feeling is unmatched.",
            "Attended a great seminar on AI in Healthcare today at campus.",
            "Who else is participating in the upcoming Hackathon?",
            "Can't believe it's been 5 years since graduation. Miss the campus life.",
            "Any IUTians in Stockholm want to catch up for coffee this weekend?",
            "Just published a new paper on Machine Learning. Link in comments!",
            "It's amazing how much you can learn just by reading source code.",
            "Learning about financial modeling and it's quite fascinating.",
            "Looking for collaborators for my new app idea!"
        };
        
        for (int i = 0; i < 12; i++) {
            edu.iutdhaka.linkiut.model.Post post = new edu.iutdhaka.linkiut.model.Post(newUsers[i], postContents[i]);
            post.setCreatedAt(LocalDateTime.now().minusDays(rand.nextInt(5)).minusHours(rand.nextInt(24)).minusMinutes(rand.nextInt(60)));
            postRepository.save(post);
        }
    }
}
