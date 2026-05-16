package Client.model;

public class ActivityLog {
    private final String user;
    private final String activity;
    private final String time;
    private final String status;

    public ActivityLog(String user, String activity, String time, String status) {
        this.user = user;
        this.activity = activity;
        this.time = time;
        this.status = status;
    }

    public String getUser() { return user; }
    public String getActivity() { return activity; }
    public String getTime() { return time; }
    public String getStatus() { return status; }
}