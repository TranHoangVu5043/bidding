package Server.service;

import Server.dao.NotificationDAO;
import Server.dao.users.UserDAO;
import Server.model.Notification;
import Server.model.users.User;
import java.util.List;

public class NotificationService {
    private final NotificationDAO dao;
    private final UserDAO         userDAO;

    public NotificationService(NotificationDAO dao, UserDAO userDAO) {
        this.dao     = dao;
        this.userDAO = userDAO;
    }

    /**
     * Sends a notification only if the recipient has auction notifications enabled.
     * All current notifications (outbid, win) are auction-type, so this single flag covers them.
     */
    public void send(int userId, String message) {
        User user = userDAO.findById(userId);
        if (user != null && !user.isNotifAuction()) return;  // user opted out
        dao.create(userId, message);
    }

    public List<Notification> getForUser(int userId) { return dao.findByUserId(userId); }
    public void markRead(int notifId, int userId) { dao.markRead(notifId, userId); }
    public void markAllRead(int userId) { dao.markAllRead(userId); }
}
