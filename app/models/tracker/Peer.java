package models.tracker;

import play.db.jpa.Model;
import play.mvc.Http;

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
    public int state;
    public Date updated;

    public Peer(Map<String, String> announceParams) {
        this.info_hash = announceParams.get("info_hash");
        this.peer_id = announceParams.get("peer_id");
        this.port = Integer.parseInt(announceParams.get("port"));
    }
    
    public static long countSeeder(String info_hash){
    	return Peer.count("info_hash = ? AND state = 1", info_hash);
    }
}
