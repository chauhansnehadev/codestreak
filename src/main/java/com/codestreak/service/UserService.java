package com.codestreak.service;

import com.codestreak.dao.UserDao;
import com.codestreak.model.User;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.SQLException;
import java.util.Optional;

public class UserService {

    private final UserDao userDao = new UserDao();

    public User register(String username, String email, String password, String fullName) throws Exception {
        if (username == null || username.trim().length() < 3) {
            throw new IllegalArgumentException("Username must be at least 3 characters long.");
        }
        if (email == null || !email.contains("@")) {
            throw new IllegalArgumentException("A valid email address is required.");
        }
        if (password == null || password.length() < 6) {
            throw new IllegalArgumentException("Password must be at least 6 characters long.");
        }

        username = username.trim();
        email = email.trim().toLowerCase();

        if (userDao.findByUsername(username).isPresent()) {
            throw new IllegalArgumentException("Username '" + username + "' is already taken.");
        }
        if (userDao.findByEmail(email).isPresent()) {
            throw new IllegalArgumentException("Email '" + email + "' is already registered.");
        }

        String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt(10));
        User newUser = new User();
        newUser.setUsername(username);
        newUser.setEmail(email);
        newUser.setPasswordHash(hashedPassword);
        newUser.setFullName(fullName != null ? fullName.trim() : username);
        newUser.setTargetProblemsPerDay(3);

        return userDao.create(newUser);
    }

    public Optional<User> authenticate(String usernameOrEmail, String password) throws SQLException {
        if (usernameOrEmail == null || password == null) {
            return Optional.empty();
        }

        Optional<User> userOpt;
        if (usernameOrEmail.contains("@")) {
            userOpt = userDao.findByEmail(usernameOrEmail.trim().toLowerCase());
        } else {
            userOpt = userDao.findByUsername(usernameOrEmail.trim());
        }

        if (userOpt.isPresent()) {
            User user = userOpt.get();
            if (BCrypt.checkpw(password, user.getPasswordHash())) {
                return Optional.of(user);
            }
        }
        return Optional.empty();
    }

    public Optional<User> getUserById(int id) throws SQLException {
        return userDao.findById(id);
    }

    public boolean updateProfile(User user) throws SQLException {
        return userDao.updateProfile(user);
    }

    public boolean changePassword(int userId, String oldPassword, String newPassword) throws Exception {
        if (newPassword == null || newPassword.length() < 6) {
            throw new IllegalArgumentException("New password must be at least 6 characters.");
        }
        Optional<User> userOpt = userDao.findById(userId);
        if (userOpt.isEmpty()) {
            return false;
        }
        User user = userOpt.get();
        if (!BCrypt.checkpw(oldPassword, user.getPasswordHash())) {
            throw new IllegalArgumentException("Incorrect current password.");
        }

        String newHash = BCrypt.hashpw(newPassword, BCrypt.gensalt(10));
        return userDao.updatePassword(userId, newHash);
    }
}
