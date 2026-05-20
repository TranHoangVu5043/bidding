package Server.dto.requests;

public class UserRequestDTO {
    private String username;
    private String password;
    private String email;
    private String role;
    private String storeName;

    // Change-password fields
    private String oldPassword;
    private String newPassword;

    public String getUsername()    { return username; }
    public String getPassword()    { return password; }
    public String getEmail()       { return email; }
    public String getRole()        { return role != null ? role.toUpperCase() : "BIDDER"; }
    public String getStoreName()   { return storeName; }
    public String getOldPassword() { return oldPassword; }
    public String getNewPassword() { return newPassword; }
}
