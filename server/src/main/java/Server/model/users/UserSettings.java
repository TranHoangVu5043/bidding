package Server.model.users;

/**
 * Holds a user's notification preferences, backed by the {@code user_settings} table.
 */
public class UserSettings {

    private final int userId;
    private boolean notifAuction;
    private boolean notifEmail;

    public UserSettings(int userId, boolean notifAuction, boolean notifEmail) {
        this.userId       = userId;
        this.notifAuction = notifAuction;
        this.notifEmail   = notifEmail;
    }

    public int getUserId() { return userId; }

    public boolean isNotifAuction() { return notifAuction; }
    public void    setNotifAuction(boolean notifAuction) { this.notifAuction = notifAuction; }

    public boolean isNotifEmail() { return notifEmail; }
    public void    setNotifEmail(boolean notifEmail) { this.notifEmail = notifEmail; }

    @Override
    public String toString() {
        return "UserSettings{userId=" + userId
                + ", notifAuction=" + notifAuction
                + ", notifEmail=" + notifEmail + "}";
    }
}
