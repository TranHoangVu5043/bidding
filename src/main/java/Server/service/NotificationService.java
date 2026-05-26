package Server.service;

import Server.dao.NotificationDAO;
import Server.model.Notification;
import java.util.List;

public class NotificationService {
    private final NotificationDAO dao;
    public NotificationService(NotificationDAO dao) { this.dao = dao; }

    public void send(int userId, String message) { dao.create(userId, message); }
    public List<Notification> getForUser(int userId) { return dao.findByUserId(userId); }
    public void markRead(int notifId, int userId) { dao.markRead(notifId, userId); }
    public void markAllRead(int userId) { dao.markAllRead(userId); }
}
