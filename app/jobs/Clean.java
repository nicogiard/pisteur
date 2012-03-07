package jobs;

import models.tracker.Peer;
import org.joda.time.DateTime;
import play.Logger;
import play.jobs.Every;
import play.jobs.Job;
import play.jobs.OnApplicationStart;
import services.Tracker;

import java.util.Date;
import java.util.List;

// Toutes les announce_interval * 2
// 1800 * 2 = 3600s = 60m = 1h
@OnApplicationStart
@Every("3600s")
public class Clean extends Job {

    public void doJob() {
        DateTime now = new DateTime();
        Logger.info("Clean|doJob : now = %s", now.toString("dd/MM/yyyy hh:mm:ss"));

        DateTime thresoldDate = now.plusSeconds(-(Tracker.announce_interval * 2));
        Logger.info("Clean|doJob : thresoldDate : %s", thresoldDate.toString("dd/MM/yyyy hh:mm:ss"));

        List<Peer> oldPeers = Peer.find("updated < ?", thresoldDate.toDate()).fetch();
        if(oldPeers.size() > 0){
            Logger.info("Clean|doJob : remove old %s peers", oldPeers.size());
            for (Peer oldPeer : oldPeers) {
                oldPeer.delete();
            }
        }
        else{
            Logger.info("Clean|doJob : no peer to remove");
        }
    }
}
