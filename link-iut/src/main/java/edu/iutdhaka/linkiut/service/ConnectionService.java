package edu.iutdhaka.linkiut.service;

import edu.iutdhaka.linkiut.model.AppUser;
import edu.iutdhaka.linkiut.model.Connection;
import edu.iutdhaka.linkiut.repository.ConnectionRepository;
import edu.iutdhaka.linkiut.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ConnectionService {

    private final ConnectionRepository connectionRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    public ConnectionService(ConnectionRepository connectionRepository, UserRepository userRepository, NotificationService notificationService) {
        this.connectionRepository = connectionRepository;
        this.userRepository = userRepository;
        this.notificationService = notificationService;
    }

    public Connection sendRequest(Long requesterId, Long receiverId) {
        if (requesterId.equals(receiverId)) {
            throw new IllegalArgumentException("Cannot send connection request to yourself");
        }

        Optional<Connection> existing = connectionRepository.findConnectionBetween(requesterId, receiverId);
        if (existing.isPresent()) {
            throw new IllegalStateException("Connection or request already exists");
        }

        AppUser requester = userRepository.findById(requesterId).orElseThrow();
        AppUser receiver = userRepository.findById(receiverId).orElseThrow();

        Connection connection = new Connection(requester, receiver, Connection.Status.PENDING);
        Connection savedConnection = connectionRepository.save(connection);
        
        notificationService.createNotification(receiver, requester, "CONNECTION_REQUEST", requester.getDisplayName() + " sent you a connection request.", "/network");
        
        return savedConnection;
    }

    public Connection acceptRequest(Long connectionId, Long receiverId) {
        Connection connection = connectionRepository.findById(connectionId).orElseThrow();
        
        if (!connection.getReceiver().getId().equals(receiverId)) {
            throw new IllegalStateException("Not authorized to accept this request");
        }
        
        connection.setStatus(Connection.Status.ACCEPTED);
        Connection savedConnection = connectionRepository.save(connection);
        
        notificationService.createNotification(connection.getRequester(), connection.getReceiver(), "CONNECTION_ACCEPT", connection.getReceiver().getDisplayName() + " accepted your connection request.", "/profile/" + connection.getReceiver().getId());
        
        return savedConnection;
    }

    public void rejectRequest(Long connectionId, Long receiverId) {
        Connection connection = connectionRepository.findById(connectionId).orElseThrow();
        
        if (!connection.getReceiver().getId().equals(receiverId)) {
            throw new IllegalStateException("Not authorized to reject this request");
        }
        
        connectionRepository.delete(connection);
    }

    public void removeConnection(Long connectionId, Long userId) {
        Connection connection = connectionRepository.findById(connectionId).orElseThrow();
        
        if (!connection.getRequester().getId().equals(userId) && !connection.getReceiver().getId().equals(userId)) {
            throw new IllegalStateException("Not authorized to remove this connection");
        }
        
        connectionRepository.delete(connection);
    }

    public List<Connection> getPendingRequests(Long userId) {
        return connectionRepository.findPendingForUser(userId);
    }

    public List<Connection> getConnections(Long userId) {
        return connectionRepository.findAcceptedConnections(userId);
    }

    public long getConnectionCount(Long userId) {
        return connectionRepository.countAcceptedConnections(userId);
    }

    public String getConnectionStatus(Long currentUserId, Long targetUserId) {
        if (currentUserId.equals(targetUserId)) {
            return "SELF";
        }
        
        Optional<Connection> connection = connectionRepository.findConnectionBetween(currentUserId, targetUserId);
        
        if (connection.isEmpty()) {
            return null;
        }
        
        Connection conn = connection.get();
        if (conn.getStatus() == Connection.Status.ACCEPTED) {
            return "CONNECTED";
        }
        
        if (conn.getRequester().getId().equals(currentUserId)) {
            return "PENDING_SENT";
        } else {
            return "PENDING_RECEIVED";
        }
    }
}
