package Server.model;

public class Notification {
    private int id;
    private int userId;
    private String message;
    private boolean isRead;
    private String createdAt;

    public Notification() {}
    public Notification(int id, int userId, String message, boolean isRead, String createdAt) {
        this.id = id; this.userId = userId; this.message = message;
        this.isRead = isRead; this.createdAt = createdAt;
    }
    public int getId() { return id; }
    public int getUserId() { return userId; }
    public String getMessage() { return message; }
    public boolean isRead() { return isRead; }
    public String getCreatedAt() { return createdAt; }
    public void setId(int id) { this.id = id; }
    public void setUserId(int userId) { this.userId = userId; }
    public void setMessage(String message) { this.message = message; }
    public void setRead(boolean read) { isRead = read; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
}
