package Client.model;

public class Notification {
    private int id;
    private int userId;
    private String message;
    private boolean isRead;
    private String createdAt;

    public int getId() { return id; }
    public int getUserId() { return userId; }
    public String getMessage() { return message; }
    public boolean isRead() { return isRead; }
    public String getCreatedAt() { return createdAt; }
}
