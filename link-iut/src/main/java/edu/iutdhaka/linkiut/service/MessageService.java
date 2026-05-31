package edu.iutdhaka.linkiut.service;

import edu.iutdhaka.linkiut.model.AppUser;
import edu.iutdhaka.linkiut.model.ChatSession;
import edu.iutdhaka.linkiut.model.Message;
import edu.iutdhaka.linkiut.repository.ChatSessionRepository;
import edu.iutdhaka.linkiut.repository.MessageRepository;
import edu.iutdhaka.linkiut.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class MessageService {

    private final ChatSessionRepository chatSessionRepository;
    private final MessageRepository messageRepository;
    private final UserRepository userRepository;

    public MessageService(ChatSessionRepository chatSessionRepository,
                          MessageRepository messageRepository,
                          UserRepository userRepository) {
        this.chatSessionRepository = chatSessionRepository;
        this.messageRepository = messageRepository;
        this.userRepository = userRepository;
    }

    /**
     * Finds an active session between two users, or creates a new one
     * with slaDeadline = now() + 48 hours.
     */
    @Transactional
    public ChatSession getOrCreateSession(Long initiatorId, Long responderId) {
        return chatSessionRepository.findActiveSessionBetween(initiatorId, responderId)
                .orElseGet(() -> {
                    AppUser initiator = userRepository.findById(initiatorId)
                            .orElseThrow(() -> new IllegalArgumentException("Initiator not found"));
                    AppUser responder = userRepository.findById(responderId)
                            .orElseThrow(() -> new IllegalArgumentException("Responder not found"));
                    ChatSession session = new ChatSession(initiator, responder);
                    return chatSessionRepository.save(session);
                });
    }

    /**
     * Sends a message in an active session.
     * Rejects if session is EXPIRED or CLOSED.
     */
    @Transactional
    public Message sendMessage(Long sessionId, Long senderId, String content) {
        ChatSession session = chatSessionRepository.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("Session not found"));

        if (session.isExpired() || session.getStatus() != ChatSession.Status.ACTIVE) {
            throw new IllegalStateException("Cannot send message: session is " + session.getStatus());
        }

        AppUser sender = userRepository.findById(senderId)
                .orElseThrow(() -> new IllegalArgumentException("Sender not found"));

        Message message = new Message(session, sender, content);
        return messageRepository.save(message);
    }

    /**
     * Returns the full chat history for a session with senders pre-loaded.
     */
    public List<Message> getChatHistory(Long sessionId) {
        return messageRepository.findByChatSessionIdWithSender(sessionId);
    }

    /**
     * Returns messages newer than `afterId` for HTMX polling.
     */
    public List<Message> getNewMessages(Long sessionId, Long afterId) {
        return messageRepository.findNewMessages(sessionId, afterId);
    }

    /**
     * Returns a session by ID.
     */
    public ChatSession getSession(Long sessionId) {
        return chatSessionRepository.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("Session not found"));
    }

    /**
     * Lists all sessions for a user.
     */
    public List<ChatSession> getUserSessions(Long userId) {
        return chatSessionRepository.findAllByUser(userId);
    }
}
