package utils;

import play.Logger;
import play.libs.OAuth;
import play.libs.WS;

import java.net.URLEncoder;

public class Twitter {

    private String username = "pisteurNAS";
    private String token = "520216648-fTEy2CSiboeezJ9aNmkp6ki4wJpQNmloUdzeCJsf";
    private String secret = "TTgIa1EDeaeehiYwDJrtR62qZlvyG5DA9luBzScr4Ic";

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
        String url = "http://twitter.com/statuses/update.json?status=" + URLEncoder.encode(status, "utf-8");
        String response = WS.url(url).oauth(TWITTER, token, secret).post().getString();
        return response;
    }
}
