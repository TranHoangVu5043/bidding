package Server.controller;

import Server.controller.responseObjects.ApiResponse;
import Server.model.Notification;
import Server.model.users.User;
import Server.networking.http.RequestWrapper;
import Server.networking.http.ResponseWrapper;
import Server.service.NotificationService;
import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.List;

public class NotificationApiController {

    private static final Logger log = LoggerFactory.getLogger(NotificationApiController.class);

    private final NotificationService notifService;
    private final Gson gson = new Gson();

    public NotificationApiController(NotificationService notifService) {
        this.notifService = notifService;
    }

    // GET /api/notifications
    public void getNotifications(RequestWrapper req, ResponseWrapper res) {
        try {
            User user = req.getUser();
            if (user == null) { res.error(401, "Unauthorized"); return; }
            List<Notification> list = notifService.getForUser(user.getId());
            res.sendJson(200, gson.toJson(new ApiResponse<>(200, "OK", list)));
        } catch (Exception e) {
            log.error("Unhandled exception", e);
            res.error(500, "Server error: " + e.getMessage());
        }
    }

    // POST /api/notifications/read-all
    public void markAllRead(RequestWrapper req, ResponseWrapper res) {
        try {
            User user = req.getUser();
            if (user == null) { res.error(401, "Unauthorized"); return; }
            notifService.markAllRead(user.getId());
            res.sendJson(200, gson.toJson(new ApiResponse<>(200, "OK", null)));
        } catch (Exception e) {
            log.error("Unhandled exception", e);
            res.error(500, "Server error: " + e.getMessage());
        }
    }
}
