package models.tracker;

import play.db.jpa.Model;

import javax.persistence.Entity;
import java.util.Date;
import java.util.Map;

@Entity
public class Peer extends Model {
    public String info_hash;
    public String peer_id;
    public String compact;
    public String ip;
    public int port;
    public Long downloaded;
    public Long uploaded;
    public Long byteLeft;
    public int state;
    public Date updated;

    public Peer() {
    }
    
    public static long countSeeder(String info_hash){
    	return Peer.count("info_hash = ? AND state = 1", info_hash);
    }
}
