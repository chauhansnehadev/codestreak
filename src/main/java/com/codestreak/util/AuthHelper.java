package com.codestreak.util;

import com.codestreak.dao.UserDao;
import com.codestreak.model.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

public class AuthHelper {

    private static final UserDao userDao = new UserDao();
    private static User defaultDemoUser = null;

    /**
     * Retrieves the current user from session if available,
     * or seamlessly falls back to the default demo user (no authentication required).
     */
    public static User getEffectiveUser(HttpServletRequest req) {
        HttpSession session = req.getSession(false);
        if (session != null && session.getAttribute("currentUser") != null) {
            return (User) session.getAttribute("currentUser");
        }

        if (defaultDemoUser == null) {
            try {
                defaultDemoUser = userDao.findById(1).orElseGet(() -> {
                    User u = new User();
                    u.setId(1);
                    u.setUsername("demouser");
                    u.setFullName("Demo Coder");
                    u.setEmail("demo@codestreak.dev");
                    return u;
                });
            } catch (Exception e) {
                User u = new User();
                u.setId(1);
                u.setUsername("demouser");
                u.setFullName("Demo Coder");
                return u;
            }
        }
        return defaultDemoUser;
    }
}
