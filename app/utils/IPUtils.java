package utils;

import play.Logger;
import play.mvc.Http;

public final class IPUtils {

    private IPUtils(){
    }

    public static String getIpFromRequest(Http.Request request) {
        String ipAddress = request.remoteAddress;

        Http.Header forwarded = request.headers.get("x-forwarded-for");
        if (forwarded != null) {
            ipAddress = forwarded.value();
            Logger.debug("IPUtils|getIpFromRequest : forwarded : %s", ipAddress);
        } else {
            Logger.debug("IPUtils|getIpFromRequest : not forwarded : %s", request.remoteAddress);
        }
        return ipAddress;
    }
}
