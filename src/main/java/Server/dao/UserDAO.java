package Server.dao;

import Server.model.User;

import java.util.ArrayList;
import java.util.List;

public class UserDAO {
    private List<User> database=new ArrayList<>();
    public void save(User user){
        database.add(user);
        System.out.println(("DAO:Đã lưu người dùng: "+user.getUsername()));
    }
    public User findByUsername(String username) {
        return database.stream()
                .filter(u -> u.getUsername().equals(username))
                .findFirst()
                .orElse(null);
    }
}
