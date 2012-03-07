package controllers;

import exception.tracker.AnnounceException;
import exception.tracker.UnknownUserException;
import play.Logger;
import play.mvc.Controller;
import services.Tracker;
import utils.Constants;
import utils.Utils;

import java.io.UnsupportedEncodingException;
import java.net.UnknownHostException;

public class Announce extends Controller {

    public static void index() {
        Logger.debug("Announce|index >>>>>>>>>>> new announce request");
        String reponse = "";
        try {
            Tracker tracker = new Tracker(request);

            tracker.validateParameters();

            reponse = tracker.peers();

            tracker.event();

            if (Logger.isDebugEnabled()) {
                String info_hash = tracker.announceParams.get("info_hash");
                String peer_id = tracker.announceParams.get("peer_id");
                String ip = tracker.announceParams.get("ip");

                String decoded_info_hash = Utils.byteArrayToByteString(info_hash.getBytes(Constants.BYTE_ENCODING));

                Logger.debug("info_hash : %s (%s) | %s ", info_hash, info_hash.length(), decoded_info_hash);
                Logger.debug("peer_id : %s (%s)", peer_id, peer_id.length());
                Logger.debug("ip : %s", ip);
            }
        } catch (UnsupportedEncodingException e) {
            Logger.error("Announce|index : An error occured : %s", e.getMessage());
            reponse = Tracker.error(e.getMessage());
        } catch (AnnounceException e) {
            Logger.error("Announce|index : An error occured : %s", e.getMessage());
            reponse = Tracker.error(e.getMessage());
        } catch (UnknownUserException e) {
        	Logger.warn("Announce|index : An error occured : %s", e.getMessage());
            reponse = Tracker.error(e.getMessage());
		} catch (UnknownHostException e) {
			Logger.error("Announce|index : An error occured : %s", e.getMessage());
            reponse = Tracker.error(e.getMessage());
		}
        renderText(reponse);
    }
}
