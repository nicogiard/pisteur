package utils;

import play.Play;

public final class Version {

    private Version() {
    }

    public static final String VERSION = Play.configuration.getProperty("application.version");
    public static final String BUILD_NUMBER = Play.configuration.getProperty("application.buildNumber");
    public static final String BUILD_DATE = Play.configuration.getProperty("application.buildDate");
}
