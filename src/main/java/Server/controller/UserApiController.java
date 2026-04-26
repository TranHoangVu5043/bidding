package Server.controller;
import Server.model.ApiResponse;
import Server.model.User;
import com.google.gson.Gson;
public class UserApiController {
    private final UserService userService = new UserService();
    private final Gson gson = new Gson();
    //Xu li dang nhap
    public String handlgLogin(String jsonRequest){
        try{
            //Giai ma JSON nhan tu Client
            User loginData=gson.fromJson(jsonRequest,User.class);
            //Goi service ktra Db
            String token=userService.login(loginData.getUsername(),loginData.getPassword());
            return gson.toJson(new ApiResponse(200,"Đăng nhập thành công !",token));
        }catch (Exception e){
            //Tra ve 401 neu sai mat khau
            return gson.toJson(new ApiResponse(401,"Lỗi: "+e.getMessage(),null));
        }
    }
    public String handleRegister(String jsonRequest) {
        try {
            User newUser = gson.fromJson(jsonRequest, User.class);
            userService.register(newUser.getUsername(), newUser.getPassword());
            return gson.toJson(new ApiResponse(201, "Đăng ký tài khoản thành công!", null));
        } catch (Exception e) {
            return gson.toJson(new ApiResponse(400, "Đăng ký thất bại: " + e.getMessage(), null));
        }
    }
}

