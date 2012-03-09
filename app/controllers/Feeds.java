package controllers;

import com.google.common.collect.Maps;
import com.sun.syndication.feed.synd.*;
import com.sun.syndication.io.FeedException;
import com.sun.syndication.io.SyndFeedOutput;
import models.Torrent;
import play.Logger;
import play.Play;
import play.i18n.Messages;
import play.mvc.Controller;
import play.mvc.Router;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class Feeds extends Controller {

    public static void rss() {
        SyndFeed feed = generateNewsRSS();

        StringWriter writer = new StringWriter();
        SyndFeedOutput out = new SyndFeedOutput();
        try {
            out.output(feed, writer);
        } catch (IOException e) {
            Logger.error(Messages.get("rss.error.io") + e.getMessage());
        } catch (FeedException e) {
            Logger.error(Messages.get("rss.error.serialization", e.getMessage()));
        }

        response.contentType = "application/rss+xml";
        response.encoding = "UTF-8";
        renderXml(writer.toString());
    }

    private static SyndFeed generateNewsRSS() {
        SyndFeed feed = new SyndFeedImpl();
        feed.setTitle("RSS");
        feed.setFeedType("rss_2.0");
        feed.setAuthor("pisteur");
        feed.setCopyright("©2012 all rights reserved");
        feed.setDescription("RSS feed for all new torrents");
        feed.setLink(Router.getFullUrl("Feeds.rss"));
        feed.setPublishedDate(new Date());

        List<Torrent> torrents = Torrent.find("").fetch(15);
        List entries = new ArrayList();
        for (Torrent torrent : torrents) {
            SyndEntry item = new SyndEntryImpl();
            if (torrent.modificationDate != null) {
                item.setPublishedDate(torrent.modificationDate);
            }
            if (torrent.filename != null) {
                item.setTitle(torrent.filename);
            }
            SyndContent content = new SyndContentImpl();
            content.setType("text/plain");
            if (torrent.description != null) {
                content.setValue(torrent.description);
            }
            item.setDescription(content);
            Map<String, Object> params = Maps.newHashMap();
            params.put("torrentId", torrent.id);
            item.setLink(Router.getFullUrl("Application.getTorrentFile", params));
            entries.add(item);
        }
        feed.setEntries(entries);
        return feed;
    }
}
