package services;

import exception.tracker.AnnounceException;
import exception.tracker.UnknownUserException;
import models.User;
import models.tracker.Peer;
import org.apache.commons.lang.math.NumberUtils;
import play.Logger;
import play.db.jpa.JPA;
import play.mvc.Http;
import utils.Constants;
import utils.IPUtils;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Tracker {

    public Map<String, String> announceParams = null;

    public boolean open_tracker = true;
    public static int announce_interval = 1800;
    public int min_interval = 900;
    public int default_peers = 50;
    public int max_peers = 100;

    public boolean external_ip = true;
    public boolean force_compact = false;
    public boolean full_scrape = false;
    public int random_limit = 500;
    public int clean_idle_peers = 10;

    private int numwant = 0;
    private int seedings = 0;

    public Tracker(Http.Request request) throws UnsupportedEncodingException {
        announceParams = getParams(request);
    }

    public static String error(String message) {
        StringBuilder sb = new StringBuilder();
        sb.append("d14:failure reason").append(message.length()).append(":").append(message).append("e");
        return sb.toString();
    }

    public static Map<String, String> getParams(Http.Request request) throws UnsupportedEncodingException {
        Logger.debug("Tracker|getParams : querystring = %s", request.querystring);
        Map<String, String> announceParams = new HashMap<String, String>();
        String[] keyValues = request.querystring.split("&");
        for (String keyValue : keyValues) {
            int i = keyValue.indexOf('=');
            String key = null;
            String value = null;
            if (i > 0) {
                key = keyValue.substring(0, i);
                value = keyValue.substring(i + 1);
            } else {
                key = keyValue;
            }

            if (value != null) {
                value = URLDecoder.decode(value, Constants.BYTE_ENCODING);
                announceParams.put(key, value);
                Logger.debug("Tracker|getParams : %s : %s", key, value);
            }
        }

        // prendre quoi qu'il arrive l'ip de la request
        String ipAddress = IPUtils.getIpFromRequest(request);
        Logger.debug("Tracker|getParams : ip : %s", ipAddress);
        announceParams.put("ip", ipAddress);

        return announceParams;
    }

    public void validateAnnounceParameters() throws AnnounceException {
        Logger.debug("Tracker|validateAnnounceParameters : start verificating announce parameters...");

        // Si info_hash n'existe pas et si length() != 20
        if (!announceParams.containsKey("info_hash")) {
            throw new AnnounceException("info_hash is not found");
        } else {
            if (announceParams.get("info_hash").length() != 20) {
                throw new AnnounceException("info_hash is incorrect");
            }
        }
        // Si peer_id n'existe pas et si length() != 20
        if (!announceParams.containsKey("peer_id")) {
            throw new AnnounceException("peer_id is not found");
        } else {
            if (announceParams.get("peer_id").length() != 20) {
                throw new AnnounceException("peer_id is incorrect");
            }
        }

        // Si port n'existe pas et si port n'est pas numeric
        if (!announceParams.containsKey("port") || !NumberUtils.isNumber(announceParams.get("port"))) {
            throw new AnnounceException("client listening port is invalid");
        }
        // Si uploaded n'existe pas et si uploaded n'est pas numeric
        if (!announceParams.containsKey("uploaded") || !NumberUtils.isNumber(announceParams.get("uploaded"))) {
            throw new AnnounceException("client data uploaded field is invalid");
        }
        // Si downloaded n'existe pas et si downloaded n'est pas numeric
        if (!announceParams.containsKey("downloaded") || !NumberUtils.isNumber(announceParams.get("downloaded"))) {
            throw new AnnounceException("client data downloaded field is invalid");
        }
        // Si left n'existe pas et si left n'est pas numeric
        if (!announceParams.containsKey("left") || !NumberUtils.isNumber(announceParams.get("left"))) {
            throw new AnnounceException("client data left field is invalid");
        } else {
            if (Long.valueOf(announceParams.get("left")) == 0) {
                seedings = 1;
            }
        }

        // TODO faire le reste des parameters

        // compact - optional

        // no_peer_id - optional

        // si l'ip n'existe pas
        if (!announceParams.containsKey("ip")) {
            throw new AnnounceException("could not locate clients ip");
        }

        // numwant - optional
        // si non présente prendre numwant = default_peers
        // sinon si numwant > max_peers alors numwant = max_peers
        // sinon prendre numwant
        if (!announceParams.containsKey("numwant") || !NumberUtils.isNumber(announceParams.get("numwant"))) {
            numwant = default_peers;
        } else {
            numwant = Integer.valueOf(announceParams.get("numwant"));
            if (numwant > max_peers) {
                numwant = max_peers;
            }
        }

        Logger.debug("Tracker|validateAnnounceParameters : All is good!");
    }

    public String peers() {
        Logger.debug("Tracker|peers : start encoding");
        // fetch peer total
        String sqlCount = "SELECT COUNT(*) FROM `Peer` WHERE info_hash=?";
        String sql = "SELECT compact, peer_id, ip, port FROM `Peer` WHERE info_hash=?;";
        String sqlWithNumWantLessRandomLimit = "SELECT compact, peer_id, ip, port FROM `Peer` WHERE info_hash=? ORDER BY RAND() LIMIT ?;";
        String sqlWithNumWant = "SELECT compact, peer_id, ip, port FROM `Peer` WHERE info_hash='' LIMIT ? OFFSET ;";

        // response start
        String response = "d8:intervali" + announce_interval + "e12:min intervali" + min_interval + "e5:peers";

        List<Peer> peers = Peer.find("info_hash=?", announceParams.get("info_hash")).fetch(numwant);

        boolean compact = false;
        if (compact) {
            // TODO compact response
            // compact announce start
            // compact announce end
        } else {
            // dictionary announce start
            // list start
            response = response + "l";

            for (Peer peer : peers) {
                if (!announceParams.containsKey("no_peer_id")) {
                    // include peer_id
                    response = response + "d2:ip" + peer.ip.length() + ":" + peer.ip + "7:peer id20:" + peer.peer_id + "4:porti" + peer.port + "ee";
                } else {
                    // omit peer_id
                    response = response + "d2:ip" + peer.ip.length() + ":" + peer.ip + "4:porti" + peer.port + "ee";
                }
            }
            // list end
            response = response + "e";
            // dictionary announce end
        }
        // response end
        response = response + "e";

        Logger.debug("Tracker|peers : response : %s", response);

        return response;
    }

    public void event() throws UnknownUserException, UnknownHostException {
        String event = "none";
        if (announceParams.containsKey("event")) {
            event = announceParams.get("event");
        }

        Peer peer = Peer.find("info_hash=? AND peer_id=?", announceParams.get("info_hash"), announceParams.get("peer_id")).first();

        Logger.debug("Tracker|event : event = %s", event);
        if ("stopped".equals(event)) { // client gracefully exited
            delete_peer();
        } else {
            if ("completed".equals(event)) { // client completed download            	
                seedings = 1;
            }
            if (peer == null) {
                if (User.isValidIPAddress(announceParams.get("ip"))) {
                    new_peer();
                } else {
                    Logger.debug("Tracker|event : utilisateur(ip: %s) non autorisé", announceParams.get("ip"));
                    throw new UnknownUserException("Vous n'êtes pas autorisé à utiliser ce trackeur");
                }
            } else if (peer.ip != announceParams.get("ip") || peer.port != Integer.valueOf(announceParams.get("port")) || peer.state != seedings) {
                update_peer();
            } else {
                update_last_access();
            }
        }
    }

    public void validateScrapeParameters() throws AnnounceException {
        Logger.debug("Tracker|validateScrapeParameters : start verificating scrape parameters...");

        Logger.debug("Tracker|validateScrapeParameters : All is good!");
    }

    public String stats() {
        String sql = "SELECT SUM(state=1), SUM(state=0), COUNT(DISTINCT info_hash) FROM Peers";

//        StringBuilder reponse = new StringBuilder();
//        if (announceParams.containsKey("xml")) {
//            //        header('Content-Type: text/xml');
//            reponse.append("<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>");
//            reponse.append("<tracker version=\"").append(Version.VERSION).append("\">");
//            reponse.append("<peers>").append(number_format($stats[0] + $stats[1])).append("<peers>");
//            reponse.append("<seeders>").append(number_format($stats[0])).append("</seeders>");
//            reponse.append("<leechers>").append(number_format($stats[1])).append("</leechers>");
//            reponse.append("<torrents>").append(number_format($stats[2])).append("</torrents></tracker>");
//        } else if (announceParams.containsKey("json")) {
//            //        header('Content-Type: text/javascript');
//            //        echo '{"tracker":{"version":"$Id$",' .
//            //        '"peers": "' . number_format($stats[0] + $stats[1]) . '",' .
//            //        '"seeders":"' . number_format($stats[0]) . '",' .
//            //        '"leechers":"' . number_format($stats[1]) . '",' .
//            //        '"torrents":"' . number_format($stats[2]) . '"}}';
//        } else {
//            //        echo '<!doctype html><html><head><meta http-equiv="content-type" content="text/html; charset=UTF-8">' .
//            //        '<title>PeerTracker: $Id$</title>' .
//            //        '<body><pre>' . number_format($stats[0] + $stats[1]) .
//            //        ' peers (' . number_format($stats[0]) . ' seeders + ' . number_format($stats[1]) .
//            //        ' leechers) in ' . number_format($stats[2]) . ' torrents</pre></body></html>';
//        }
        return "";
    }

    public String scrape() {
        return "";
    }

    private void delete_peer() {
        String sql = "DELETE FROM `Peer` WHERE info_hash=? AND peer_id=?;";
        int result = JPA.em().createNativeQuery(sql).setParameter(1, announceParams.get("info_hash")).setParameter(2, announceParams.get("peer_id")).executeUpdate();
        Logger.debug("delete_peer : result=%s", result);
    }

    private void new_peer() {
        Peer peer = new Peer();
        peer.info_hash = announceParams.get("info_hash");
        peer.peer_id = announceParams.get("peer_id");
        peer.ip = announceParams.get("ip");
        peer.port = Integer.valueOf(announceParams.get("port"));
        peer.state = seedings;
        peer.downloaded = Long.valueOf(announceParams.get("downloaded"));
        peer.uploaded = Long.valueOf(announceParams.get("uploaded"));
        peer.byteLeft = Long.valueOf(announceParams.get("left"));
        peer.updated = new Date();
        peer.save();
        Logger.debug("Tracker|new_peer : sauvegarde d'un nouveau peer [%s]", peer.id);
    }

    private void update_peer() {
        Peer peer = Peer.find("info_hash=? AND peer_id=?", announceParams.get("info_hash"), announceParams.get("peer_id")).first();
        if (peer != null) {
            peer.ip = announceParams.get("ip");
            peer.port = Integer.valueOf(announceParams.get("port"));
            peer.state = seedings;
            peer.downloaded = Long.valueOf(announceParams.get("downloaded"));
            peer.uploaded = Long.valueOf(announceParams.get("uploaded"));
            peer.byteLeft = Long.valueOf(announceParams.get("left"));
            peer.updated = new Date();
            peer.save();

            Logger.debug("Tracker|update_peer : update du peer [%s]", peer.id);
        } else {
            Logger.error("Tracker|update_peer : peer not found : info_hash[%s] peer_id[%s]", announceParams.get("info_hash"), announceParams.get("peer_id"));
        }
    }

    private void update_last_access() {
        Peer peer = Peer.find("info_hash=? AND peer_id=?", announceParams.get("info_hash"), announceParams.get("peer_id")).first();
        if (peer != null) {
            peer.updated = new Date();
            peer.save();

            Logger.debug("Tracker|update_last_access : update du peer [%s]", peer.id);
        } else {
            Logger.error("Tracker|update_last_access : peer not found : info_hash[%s] peer_id[%s]", announceParams.get("info_hash"), announceParams.get("peer_id"));
        }
    }
}
