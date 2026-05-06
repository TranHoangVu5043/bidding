package Server.model;

import java.io.Serializable;

public class ApiResponse<T> implements Serializable {
    private int status;      // Mã trạng thái (200: OK, 401: Unauthorized, 500: Error,...)
    private String message;  // Thông báo đi kèm (Ví dụ: "Đăng nhập thành công")
    private T data;          // Dữ liệu thực tế (Payload)

    // Constructor đầy đủ
    public ApiResponse(int status, String message, T data) {
        this.status = status;
        this.message = message;
        this.data = data;
    }

    // Getter và Setter (Bắt buộc để các thư viện JSON có thể đọc dữ liệu)
    public int getStatus() { return status; }
    public void setStatus(int status) { this.status = status; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public T getData() { return data; }
    public void setData(T data) { this.data = data; }
}