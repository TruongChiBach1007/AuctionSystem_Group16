package com.auction.dao;

import com.auction.model.users.User;
import java.util.List;

public interface IUserDAO {
    User findByUsername(String username);
    void addUser(User user);
    List<User> getAllUsers();
    // Các hàm khác như updatePassword, deleteUser...
}
