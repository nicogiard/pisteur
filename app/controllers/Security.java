package controllers;

import models.User;
import org.apache.commons.lang.StringUtils;

public class Security extends Secure.Security {

    static boolean authenticate(String username, String password) {
        User user = User.find("username=?", username).first();
        if (user != null && user.checkPassword(password) && user.isActive) {
            return true;
        }
        return false;
    }

    public static User connectedUser() {
        return User.find("byUsername", connected()).first();
    }

    static void onAuthenticated() {
        String url = flash.get("url");
        if (StringUtils.isEmpty(url)) {
            redirect("Application.index");
        }
        redirect(url);
    }

    static void onDisconnected() {
        redirect("Secure.login");
    }
    
    static boolean check(String profile) {
        User user = User.find("byUsername", connected()).first();
        return user.isAdmin;
    }
}
