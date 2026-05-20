package Client.networking;

import Client.model.user.User;

public class SessionManager {

    private static String token;
    private static User   currentUser;

    public static void setToken(String token) {
        SessionManager.token = token;
    }

    public static String getToken() {
        return token;
    }

    public static void setCurrentUser(User user) {
        SessionManager.currentUser = user;
    }

    public static User getCurrentUser() {
        return currentUser;
    }

    public static boolean isLoggedIn() {
        return token != null;
    }

    public static void clear() {
        token       = null;
        currentUser = null;
    }
}