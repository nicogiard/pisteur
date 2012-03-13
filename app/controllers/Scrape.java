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

public class Scrape extends Controller {

    public static void index() {
        Logger.debug("Scrape|index >>>>>>>>>>> new announce request");
        String reponse = "";
//        try {
//            Tracker tracker = new Tracker(request);
//
//            tracker.validateScrapeParameters();
//
//            // Si un parametre stats existe
//            reponse = tracker.stats();
//
//            // Sinon
//            reponse = tracker.scrape();
//
//
//        } catch (UnsupportedEncodingException e) {
//            Logger.error("Announce|index : An error occured : %s", e.getMessage());
//            reponse = Tracker.error(e.getMessage());
//        } catch (AnnounceException e) {
//            Logger.error("Announce|index : An error occured : %s", e.getMessage());
//            reponse = Tracker.error(e.getMessage());
//        }
        renderText(reponse);
    }
}
