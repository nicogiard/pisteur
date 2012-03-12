package models;

import java.util.List;

import play.data.validation.Required;
import play.db.jpa.Model;
import play.libs.Codec;

import javax.persistence.Entity;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;

@Entity
public class User extends Model {
    @Required
    public String username;

    @Required
    public String password;

    @Required
    public String email;

    @Required
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
    
    public static List<User> findUnactiveUsers(){
    	return User.find("isActive = false").fetch();    
    }    
    
    public void setPassword(String password) {
    	if(!Strings.isNullOrEmpty(password)){
    		this.password = Codec.hexSHA1(password);
    	}
    }
    public void setIpAddress(String ipAddress){
    	if(!Strings.isNullOrEmpty(ipAddress)){
    		this.ipAddress = ipAddress.toLowerCase();
    	}
    }
    
   public static boolean isActive(String ipAddress){
    	return User.count("ipAddress LIKE ? AND isActive = true", ipAddress) > 0;
    }
}
