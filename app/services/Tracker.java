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
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Tracker {

    public Map<String, String> announceParams = null;

    public boolean open_tracker = true;
    public int announce_interval = 1800;
    public int min_interval = 900;
    public int default_peers = 50;
    public int max_peers = 100;

    public boolean external_ip = true;
    public boolean force_compact = false;
    public boolean full_scrape = false;
    public int random_limit = 500;
    public int clean_idle_peers = 10;

    private int seedings = 0;

    public Tracker(Http.Request request) throws UnsupportedEncodingException {
        announceParams = getParams(request);

        if (announceParams.containsKey("left") && Long.valueOf(announceParams.get("left")) == 0) {
            seedings = 1;
        }
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
        if (!announceParams.containsKey("ip")) {
            String ipAddress = IPUtils.getIpFromRequest(request);
            Logger.debug("Tracker|getParams : ip : %s", ipAddress);
            announceParams.put("ip", ipAddress);
        }
        return announceParams;
    }

    public void validateParameters() throws AnnounceException {
        Logger.debug("Tracker|validateParameters : start verificating parameters...");

        // Si info_hash n'existe pas et si length() != 20
        if (!announceParams.containsKey("info_hash")) {
            throw new AnnounceException("info_hash is not found");
        } else {
            /*
            try {
                String hash = Utils.byteArrayToByteString(announceParams.get("info_hash").getBytes(Constants.BYTE_ENCODING));
                announceParams.put("info_hash", hash);
            } catch (UnsupportedEncodingException e) {
                throw new AnnounceException("info_hash is incorrect");
            }
            */
            if (announceParams.get("info_hash").length() != 20) {
                throw new AnnounceException("info_hash is incorrect");
            }
        }
        // Si peer_id n'existe pas et si length() != 20
        if (!announceParams.containsKey("peer_id")) {
            throw new AnnounceException("peer_id is not found");
        } else {
            /*
            try {
                String peerid = Utils.byteArrayToByteString(announceParams.get("peer_id").getBytes(Constants.BYTE_ENCODING));
                announceParams.put("peer_id", peerid);
            } catch (UnsupportedEncodingException e) {
                throw new AnnounceException("peer_id is incorrect");
            }
            */
            if (announceParams.get("peer_id").length() != 20) {
                throw new AnnounceException("peer_id is incorrect");
            }
        }

        // Si port n'existe pas et si port n'est pas numeric
        if (!announceParams.containsKey("port") || !NumberUtils.isNumber(announceParams.get("port"))) {
            throw new AnnounceException("client listening port is invalid");
        }
        // Si left n'existe pas et si left n'est pas numeric
        else if (!announceParams.containsKey("left") || !NumberUtils.isNumber(announceParams.get("left"))) {
            throw new AnnounceException("client data left field is invalid");
        }

        // TODO faire le reste des parameters

        // compact - optional

        // no_peer_id - optional

        // ip - optional
        // si non présente prendre la remote addr
        // sinon reponse = Tracker.error("'could not locate clients ip");
        else if (!announceParams.containsKey("ip")) {
            throw new AnnounceException("could not locate clients ip");
        }

        // numwant - optional
        // si non présente prendre numwant = default_peers
        // sinon si numwant > max_peers alors numwant = max_peers
        // sinon prendre numwant

        Logger.debug("Tracker|validateParameters : All is good!");
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

        // TODO : prendre en compte du num_want
        List<Peer> peers = Peer.find("info_hash=?", announceParams.get("info_hash")).fetch();

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

    public void event() throws UnknownUserException {
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
                if (User.isActive(announceParams.get("ip"))) {
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

    public void clean() {
        // TODO a faire
    }

    private void delete_peer() {
        String sql = "DELETE FROM `Peer` WHERE info_hash=? AND peer_id=?;";
        int result = JPA.em().createNativeQuery(sql).setParameter(1, announceParams.get("info_hash")).setParameter(2, announceParams.get("peer_id")).executeUpdate();
        Logger.debug("delete_peer : result=%s", result);
    }

    private void new_peer() {
        String sql = "INSERT INTO `Peer` (info_hash, peer_id, ip, port, state, updated) VALUES (?, ?, ?, ?, ?, ?);";
        int result = JPA.em().createNativeQuery(sql).setParameter(1, announceParams.get("info_hash")).setParameter(2, announceParams.get("peer_id")).setParameter(3, announceParams.get("ip")).setParameter(4, announceParams.get("port")).setParameter(5, seedings).setParameter(6, new Date()).executeUpdate();
        Logger.debug("new_peer : result=%s", result);
    }

    private void update_peer() {
        String sql = "UPDATE `Peer` SET ip=?, port=?, state=?, updated=? WHERE info_hash=? AND peer_id=?";
        int result = JPA.em().createNativeQuery(sql).setParameter(1, announceParams.get("ip")).setParameter(2, announceParams.get("port")).setParameter(3, seedings).setParameter(4, new Date()).setParameter(5, announceParams.get("info_hash")).setParameter(6, announceParams.get("peer_id")).executeUpdate();
        Logger.debug("update_peer : result=%s", result);
    }

    private void update_last_access() {
        String sql = "UPDATE `Peer` SET updated=? WHERE info_hash=? AND peer_id=?";
        int result = JPA.em().createNativeQuery(sql).setParameter(1, new Date()).setParameter(2, announceParams.get("info_hash")).setParameter(3, announceParams.get("peer_id")).executeUpdate();
        Logger.debug("update_last_access : result=%s", result);
    }
}
