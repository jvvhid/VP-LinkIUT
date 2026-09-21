package edu.iutdhaka.linkiut.controller;

import edu.iutdhaka.linkiut.model.AppUser;
import edu.iutdhaka.linkiut.model.JobApplication;
import edu.iutdhaka.linkiut.model.JobPosting;
import edu.iutdhaka.linkiut.model.UserProfile;
import edu.iutdhaka.linkiut.repository.JobApplicationRepository;
import edu.iutdhaka.linkiut.repository.JobPostingRepository;
import edu.iutdhaka.linkiut.repository.ProfileRepository;
import edu.iutdhaka.linkiut.repository.UserRepository;
import edu.iutdhaka.linkiut.service.FileUploadService;
import edu.iutdhaka.linkiut.service.JobReadinessService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Controller
@RequestMapping("/jobs")
public class JobController {

    private final JobPostingRepository jobPostingRepository;
    private final JobApplicationRepository jobApplicationRepository;
    private final UserRepository userRepository;
    private final ProfileRepository profileRepository;
    private final FileUploadService fileUploadService;
    private final edu.iutdhaka.linkiut.service.NotificationService notificationService;
    private final JobReadinessService jobReadinessService;

    public JobController(JobPostingRepository jobPostingRepository,
                         JobApplicationRepository jobApplicationRepository,
                         UserRepository userRepository,
                         ProfileRepository profileRepository,
                         FileUploadService fileUploadService,
                         edu.iutdhaka.linkiut.service.NotificationService notificationService,
                         JobReadinessService jobReadinessService) {
        this.jobPostingRepository = jobPostingRepository;
        this.jobApplicationRepository = jobApplicationRepository;
        this.userRepository = userRepository;
        this.profileRepository = profileRepository;
        this.fileUploadService = fileUploadService;
        this.notificationService = notificationService;
        this.jobReadinessService = jobReadinessService;
    }

    @GetMapping
    public String jobsDashboard(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        AppUser currentUser = userRepository.findByEmail(userDetails.getUsername()).orElse(null);
        List<JobPosting> jobs = jobPostingRepository.findByActiveTrueOrderByCreatedAtDesc();
        
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("jobs", jobs);
        model.addAttribute("activePage", "jobs");
        return "jobs/index";
    }

    @GetMapping("/create")
    public String showCreateJobForm(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        AppUser currentUser = userRepository.findByEmail(userDetails.getUsername()).orElse(null);
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("jobTypes", JobPosting.JobType.values());
        model.addAttribute("activePage", "jobs");
        return "jobs/create";
    }

    @PostMapping("/create")
    public String createJob(@AuthenticationPrincipal UserDetails userDetails,
                            @RequestParam String title,
                            @RequestParam String company,
                            @RequestParam String location,
                            @RequestParam JobPosting.JobType jobType,
                            @RequestParam String description,
                            @RequestParam(required = false) String requirements,
                            @RequestParam(required = false) String requiredSkills,
                            @RequestParam(required = false) String salaryRange,
                            @RequestParam(required = false) String experienceLevel) {
        AppUser currentUser = userRepository.findByEmail(userDetails.getUsername()).orElseThrow();

        JobPosting job = new JobPosting();
        job.setTitle(title);
        job.setCompany(company);
        job.setLocation(location);
        job.setJobType(jobType);
        job.setDescription(description);
        job.setRequirements(requirements);
        job.setRequiredSkills(requiredSkills);
        job.setSalaryRange(salaryRange);
        job.setExperienceLevel(experienceLevel);
        job.setPostedBy(currentUser);

        jobPostingRepository.save(job);
        return "redirect:/jobs";
    }

    @GetMapping("/{id}")
    public String jobDetails(@PathVariable Long id, @AuthenticationPrincipal UserDetails userDetails, Model model) {
        AppUser currentUser = userRepository.findByEmail(userDetails.getUsername()).orElse(null);
        JobPosting job = jobPostingRepository.findById(id).orElseThrow();

        boolean alreadyApplied = currentUser != null && jobApplicationRepository.existsByJobPosting_IdAndApplicant_Id(id, currentUser.getId());

        // AI Readiness Score
        if (currentUser != null) {
            UserProfile profile = profileRepository.findByUserIdWithDetails(currentUser.getId()).orElse(null);
            JobReadinessService.ReadinessResult readiness = jobReadinessService.analyzeReadiness(profile, job);
            model.addAttribute("readiness", readiness);
        }

        model.addAttribute("currentUser", currentUser);
        model.addAttribute("job", job);
        model.addAttribute("alreadyApplied", alreadyApplied);
        model.addAttribute("activePage", "jobs");
        return "jobs/details";
    }

    @PostMapping("/{id}/apply")
    public String applyForJob(@PathVariable Long id,
                              @AuthenticationPrincipal UserDetails userDetails,
                              @RequestParam(required = false) String coverNote,
                              @RequestParam(value = "resumeFile", required = false) MultipartFile resumeFile) {
        AppUser currentUser = userRepository.findByEmail(userDetails.getUsername()).orElseThrow();
        JobPosting job = jobPostingRepository.findById(id).orElseThrow();

        if (jobApplicationRepository.existsByJobPosting_IdAndApplicant_Id(id, currentUser.getId())) {
            return "redirect:/jobs/" + id + "?error=already_applied";
        }

        JobApplication application = new JobApplication();
        application.setJobPosting(job);
        application.setApplicant(currentUser);
        application.setCoverNote(coverNote);

        if (resumeFile != null && !resumeFile.isEmpty()) {
            String uploadedPath = fileUploadService.storeFile(resumeFile);
            application.setResumeUrl(uploadedPath);
        }

        jobApplicationRepository.save(application);

        // Notify job poster
        notificationService.createNotification(
                job.getPostedBy(),
                currentUser,
                "JOB_APPLICATION",
                currentUser.getDisplayName() + " applied for your job: " + job.getTitle(),
                "/jobs/posted/" + job.getId() + "/applicants"
        );

        return "redirect:/jobs/" + id + "?success=applied";
    }

    @GetMapping("/my-applications")
    public String myApplications(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        AppUser currentUser = userRepository.findByEmail(userDetails.getUsername()).orElseThrow();
        List<JobApplication> applications = jobApplicationRepository.findByApplicant_IdOrderByAppliedAtDesc(currentUser.getId());

        model.addAttribute("currentUser", currentUser);
        model.addAttribute("applications", applications);
        model.addAttribute("activePage", "jobs");
        return "jobs/my-applications";
    }

    @GetMapping("/posted/{id}/applicants")
    public String viewApplicants(@PathVariable Long id, @AuthenticationPrincipal UserDetails userDetails, Model model) {
        AppUser currentUser = userRepository.findByEmail(userDetails.getUsername()).orElseThrow();
        JobPosting job = jobPostingRepository.findById(id).orElseThrow();

        if (!job.getPostedBy().getId().equals(currentUser.getId())) {
            return "redirect:/jobs";
        }

        List<JobApplication> applications = jobApplicationRepository.findByJobPosting_IdOrderByAppliedAtDesc(id);

        model.addAttribute("currentUser", currentUser);
        model.addAttribute("job", job);
        model.addAttribute("applications", applications);
        model.addAttribute("statuses", JobApplication.ApplicationStatus.values());
        model.addAttribute("activePage", "jobs");
        return "jobs/applicants";
    }

    @PostMapping("/applications/{appId}/status")
    public String updateApplicationStatus(@PathVariable Long appId,
                                          @AuthenticationPrincipal UserDetails userDetails,
                                          @RequestParam JobApplication.ApplicationStatus status) {
        JobApplication application = jobApplicationRepository.findById(appId).orElseThrow();
        AppUser currentUser = userRepository.findByEmail(userDetails.getUsername()).orElseThrow();

        if (!application.getJobPosting().getPostedBy().getId().equals(currentUser.getId())) {
            return "redirect:/jobs";
        }

        application.setStatus(status);
        jobApplicationRepository.save(application);
        return "redirect:/jobs/posted/" + application.getJobPosting().getId() + "/applicants";
    }
}
