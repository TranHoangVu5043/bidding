package Server.model;

public class ApiResponse {
    public int status;
    public String message;
    public Object data;

    public ApiResponse(int status, String message, Object data) {
        this.status = status;
        this.message = message;
        this.data = data;
    }
}
