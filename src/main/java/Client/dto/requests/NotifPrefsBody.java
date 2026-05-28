package Client.dto.requests;

public class NotifPrefsBody {
    public final boolean notifAuction;
    public final boolean notifEmail;

    public NotifPrefsBody(boolean notifAuction, boolean notifEmail) {
        this.notifAuction = notifAuction;
        this.notifEmail   = notifEmail;
    }
}
