package models;

import com.google.common.base.Strings;
import play.data.validation.Required;
import play.db.jpa.Model;
import play.libs.Codec;
import utils.IPUtils;

import javax.persistence.Entity;
import java.util.List;

@Entity
public class User extends Model {
    @Required
    public String username;

    @Required
    public String password;

    @Required
    public String email;

    public String ipAddress;

    public String clientName;

    public boolean isAdmin = false;

    public boolean isActive = false;


    public boolean checkPassword(String password) {
        return this.password.equals(Codec.hexSHA1(password));
    }

    public static Long countByLetter(String letter) {
        return User.count("username LIKE ?", letter + "%");
    }

    public static JPAQuery byLetter(String letter) {
        return User.find("username LIKE ? ORDER BY username", letter + "%");
    }

    public static List<User> findActiveUsers() {
        return User.find("isActive = true").fetch();
    }

    public static List<User> findInactiveUsers() {
        return User.find("isActive = false").fetch();
    }

    public void setPassword(String password) {
        if (!Strings.isNullOrEmpty(password)) {
            this.password = Codec.hexSHA1(password);
        }
    }

    public void setIpAddress(String ipAddress) {
        if (!Strings.isNullOrEmpty(ipAddress)) {
            this.ipAddress = ipAddress.toLowerCase();
        }
    }

    public static boolean isValidIPAddress(String ipAddress) {
        List<User> activeUsers = User.findActiveUsers();

        for (User activeUser : activeUsers) {
            String ip = activeUser.ipAddress;
            if (!IPUtils.isIPAddressFormat(activeUser.ipAddress)) {
                ip = IPUtils.resolveDynDNS(activeUser.ipAddress);
            }
            if (ip.equals(ipAddress)){
                return true;
            }
        }
        return false;
    }
}
