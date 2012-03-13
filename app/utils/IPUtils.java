package utils;

import play.Logger;
import play.mvc.Http;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

    public static boolean isIPAddressFormat(String ipAddressOrHostname){
        Pattern pattern = Pattern.compile("[0-9]*\\.[0-9]*\\.[0-9]*\\.[0-9]");
        Matcher matcher = pattern. matcher(ipAddressOrHostname);
        return matcher.find();
    }

    public static String resolveDynDNS(String dyndns) {
        try {
            InetAddress address = InetAddress.getByName(dyndns);
            return address.getHostAddress();
        } catch (UnknownHostException e) {
            Logger.error("IPUtils|resolveDynDNS : une erreur est survenue", e);
        }
        return "";
    }
}
