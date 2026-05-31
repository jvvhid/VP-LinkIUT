package edu.iutdhaka.linkiut.service;

import edu.iutdhaka.linkiut.repository.ChatSessionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 48-Hour SLA Timer Scheduler.
 * <p>
 * Runs every 60 seconds, scans for ACTIVE chat sessions whose
 * {@code slaDeadline} has passed, and bulk-updates their status to EXPIRED.
 * <p>
 * SLA Flow:
 * <ol>
 *   <li>Student initiates chat → ChatSession created, slaDeadline = now() + 48h</li>
 *   <li>This scheduler polls every 60s for ACTIVE sessions past deadline</li>
 *   <li>Expired sessions → status = EXPIRED, no new messages allowed</li>
 *   <li>Alumni can respond any time before expiry to keep the conversation going</li>
 * </ol>
 */
@Component
public class ChatTimerScheduler {

    private static final Logger log = LoggerFactory.getLogger(ChatTimerScheduler.class);

    private final ChatSessionRepository chatSessionRepository;

    public ChatTimerScheduler(ChatSessionRepository chatSessionRepository) {
        this.chatSessionRepository = chatSessionRepository;
    }

    /**
     * Executes every 60 seconds (fixedRate = 60_000 ms).
     * Uses a single bulk UPDATE query for efficiency — no entity loading needed.
     */
    @Scheduled(fixedRate = 60_000)
    @Transactional
    public void expireOverdueSessions() {
        LocalDateTime now = LocalDateTime.now();
        int expiredCount = chatSessionRepository.expireOverdueSessions(now);

        if (expiredCount > 0) {
            log.info("SLA Timer: expired {} chat session(s) past their 48-hour deadline.", expiredCount);
        } else {
            log.debug("SLA Timer: no sessions to expire at {}.", now);
        }
    }
}
