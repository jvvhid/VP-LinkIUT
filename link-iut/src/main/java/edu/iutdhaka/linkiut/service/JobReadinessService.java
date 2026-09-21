package edu.iutdhaka.linkiut.service;

import edu.iutdhaka.linkiut.model.JobPosting;
import edu.iutdhaka.linkiut.model.UserProfile;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class JobReadinessService {

    /**
     * AI-powered Job Readiness Analysis.
     * Compares user's profile skills against job requirements and description
     * to generate a readiness score with detailed skill gap analysis.
     */
    public ReadinessResult analyzeReadiness(UserProfile profile, JobPosting job) {
        // Gather user skills (normalized to lowercase)
        Set<String> userSkills = new LinkedHashSet<>();
        if (profile != null && profile.getSkills() != null && !profile.getSkills().isBlank()) {
            for (String s : profile.getSkillList()) {
                userSkills.add(s.trim().toLowerCase());
            }
        }

        // Gather job required skills
        Set<String> jobSkills = new LinkedHashSet<>();
        if (job.getRequiredSkills() != null && !job.getRequiredSkills().isBlank()) {
            for (String s : job.getRequiredSkillList()) {
                jobSkills.add(s.trim().toLowerCase());
            }
        }

        // Also extract keywords from job description + requirements using NLP-lite approach
        Set<String> extractedKeywords = extractTechKeywords(
                (job.getDescription() != null ? job.getDescription() : "") + " " +
                (job.getRequirements() != null ? job.getRequirements() : "")
        );

        // Merge explicit required skills with extracted keywords (explicit take priority)
        Set<String> allJobSkills = new LinkedHashSet<>(jobSkills);
        allJobSkills.addAll(extractedKeywords);

        if (allJobSkills.isEmpty()) {
            // No skills to compare — return neutral result
            return new ReadinessResult(0, List.of(), List.of(), List.of(), "No specific skills listed for this job.");
        }

        // Match skills (fuzzy matching)
        List<String> matched = new ArrayList<>();
        List<String> missing = new ArrayList<>();

        for (String jobSkill : allJobSkills) {
            if (fuzzyContains(userSkills, jobSkill)) {
                matched.add(capitalize(jobSkill));
            } else {
                missing.add(capitalize(jobSkill));
            }
        }

        int total = matched.size() + missing.size();
        int score = (int) Math.round((double) matched.size() / total * 100);

        // Generate recommendations
        List<String> recommendations = generateRecommendations(score, missing, job);

        String summary = generateSummary(score, matched.size(), total);

        return new ReadinessResult(score, matched, missing, recommendations, summary);
    }

    /**
     * Fuzzy matching: checks if the user has a skill that matches or contains the job skill.
     * E.g. user has "spring boot" and job wants "spring" → match.
     */
    private boolean fuzzyContains(Set<String> userSkills, String jobSkill) {
        for (String userSkill : userSkills) {
            if (userSkill.equals(jobSkill)) return true;
            if (userSkill.contains(jobSkill) || jobSkill.contains(userSkill)) return true;
            // Handle common abbreviations
            if (levenshteinDistance(userSkill, jobSkill) <= 2) return true;
        }
        return false;
    }

    private int levenshteinDistance(String a, String b) {
        int[][] dp = new int[a.length() + 1][b.length() + 1];
        for (int i = 0; i <= a.length(); i++) dp[i][0] = i;
        for (int j = 0; j <= b.length(); j++) dp[0][j] = j;
        for (int i = 1; i <= a.length(); i++) {
            for (int j = 1; j <= b.length(); j++) {
                int cost = a.charAt(i - 1) == b.charAt(j - 1) ? 0 : 1;
                dp[i][j] = Math.min(Math.min(dp[i - 1][j] + 1, dp[i][j - 1] + 1), dp[i - 1][j - 1] + cost);
            }
        }
        return dp[a.length()][b.length()];
    }

    /**
     * Extracts common tech skills from job text using keyword dictionary.
     */
    private Set<String> extractTechKeywords(String text) {
        String lowerText = text.toLowerCase();
        Set<String> found = new LinkedHashSet<>();

        String[] techKeywords = {
            "java", "python", "javascript", "typescript", "c++", "c#", "go", "rust", "kotlin", "swift",
            "html", "css", "react", "angular", "vue", "node.js", "express", "django", "flask",
            "spring", "spring boot", "hibernate", "jpa", "microservices",
            "sql", "mysql", "postgresql", "mongodb", "redis", "elasticsearch",
            "aws", "azure", "gcp", "docker", "kubernetes", "terraform", "ci/cd",
            "git", "linux", "rest api", "graphql", "grpc",
            "machine learning", "deep learning", "nlp", "computer vision", "tensorflow", "pytorch",
            "data science", "data analysis", "pandas", "numpy", "tableau", "power bi",
            "agile", "scrum", "jira", "figma", "photoshop",
            "communication", "leadership", "teamwork", "problem solving", "critical thinking"
        };

        for (String keyword : techKeywords) {
            if (lowerText.contains(keyword)) {
                found.add(keyword);
            }
        }
        return found;
    }

    private List<String> generateRecommendations(int score, List<String> missing, JobPosting job) {
        List<String> recs = new ArrayList<>();

        if (score >= 80) {
            recs.add("You're a strong match! Highlight your relevant experience in your cover note.");
        } else if (score >= 50) {
            recs.add("You have a solid foundation. Consider upskilling in the missing areas to strengthen your application.");
        } else if (score >= 20) {
            recs.add("This role requires significant skill development. Focus on building core competencies first.");
        } else {
            recs.add("Consider exploring roles that better match your current skill set while learning these technologies.");
        }

        if (!missing.isEmpty() && missing.size() <= 5) {
            recs.add("Priority skills to learn: " + String.join(", ", missing.subList(0, Math.min(3, missing.size()))));
        }

        if (score < 100 && score >= 50) {
            recs.add("Tip: Add any unlisted skills to your profile to improve your readiness score.");
        }

        return recs;
    }

    private String generateSummary(int score, int matched, int total) {
        if (score >= 85) return "Excellent match — you meet " + matched + "/" + total + " required skills!";
        if (score >= 65) return "Good match — you meet " + matched + "/" + total + " required skills.";
        if (score >= 40) return "Partial match — you meet " + matched + "/" + total + " skills. Room for growth.";
        return "Early stage — you meet " + matched + "/" + total + " skills. Keep learning!";
    }

    private String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return s.substring(0, 1).toUpperCase() + s.substring(1);
    }

    /**
     * Result DTO for the readiness analysis.
     */
    public record ReadinessResult(
        int score,
        List<String> matchedSkills,
        List<String> missingSkills,
        List<String> recommendations,
        String summary
    ) {}
}
