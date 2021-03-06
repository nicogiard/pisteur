package utils;

import play.Logger;
import play.Play;
import play.libs.OAuth;
import play.libs.WS;

import java.net.URLEncoder;

public class Twitter {

    private String username = "pisteurNAS";
    private String token = "520216648-Ddb7eVejDNSYaUKRCZpz1yVGUM5wUWdnGgAjJMsG";
    private String secret = "hJvAI7ZCEqySqmT1U7OdIFaEGIk9Ks5sEe3FgNhHA";

    public static Twitter instance;

    private static final OAuth.ServiceInfo TWITTER = new OAuth.ServiceInfo(
            "http://twitter.com/oauth/request_token",
            "http://twitter.com/oauth/access_token",
            "http://twitter.com/oauth/authorize",
            "gjovZSZw9nlg7MMwRIUQ",
            "GEtcK2zAkGcGhTqPItYCxJuMVc74LopODpIppOYAf8"
    );

    private Twitter() {
    }

    public static Twitter init() {
        if (instance == null) {
            instance = new Twitter();
        }
        return instance;
    }

    public String setStatus(String status) throws Exception {
        Boolean twitterActive = Boolean.parseBoolean(Play.configuration.getProperty("application.twitter.active", "false"));
        if (twitterActive) {
            String url = "http://twitter.com/statuses/update.json?status=" + URLEncoder.encode(status, "utf-8");
            String response = WS.url(url).oauth(TWITTER, token, secret).post().getString();
            Logger.debug("Twitter|setStatus : response = %s", response);
            return response;
        }
        return "";
    }
}
