package controllers;

import com.google.common.collect.Lists;
import controllers.utils.Pager;
import models.Tag;
import models.Torrent;
import org.apache.commons.lang.StringUtils;
import play.Logger;
import play.data.validation.Error;
import play.data.validation.Required;
import play.data.validation.Valid;
import play.mvc.Before;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.With;
import utils.Twitter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Date;
import java.util.List;
import java.util.Map;

@With(Secure.class)
public class Application extends Controller {

    private static final Pager TORRENTS_PAGER = new Pager();

    @Before
    static void defaultData() {
        List<Tag> tags = Tag.findOrderByMostUsed();
        renderArgs.put("tags", tags);

        // Récupération de la pagination
        String pageParam = params.get("page");
        if (pageParam == null) {
            pageParam = "1";
        }
        TORRENTS_PAGER.setPage(Integer.valueOf(pageParam));
        session.put("torrentsPager.page", pageParam);
    }

    public static void index() {
        TORRENTS_PAGER.setElementCount(Torrent.count());
        List<Torrent> torrents = Torrent.find("ORDER BY creationDate DESC").fetch(TORRENTS_PAGER.getPage(), TORRENTS_PAGER.getPageSize());
        Pager pager = TORRENTS_PAGER;
        render(torrents, pager);
    }

    public static void tag(String tagName) {
        String activeTag = tagName;
        List<Torrent> torrents = null;
        if ("noTag".equals(activeTag)) {
            TORRENTS_PAGER.setElementCount(Torrent.countNotTagged());
            torrents = Torrent.findNotTagged(TORRENTS_PAGER.getPage(), TORRENTS_PAGER.getPageSize());
        } else {
            Tag tag = Tag.findByName(activeTag);
            notFoundIfNull(tag);
            TORRENTS_PAGER.setElementCount(Torrent.countTaggedWith(tag));
            torrents = Torrent.findTaggedWith(tag, TORRENTS_PAGER.getPage(), TORRENTS_PAGER.getPageSize());
        }
        Pager pager = TORRENTS_PAGER;
        renderTemplate("Application/index.html", pager, torrents, activeTag);
    }

    public static void search(String keywords) {
        TORRENTS_PAGER.setElementCount(Torrent.countSearch(keywords));
        int offset = TORRENTS_PAGER.getPage() > 1 ? (TORRENTS_PAGER.getPage() - 1) * TORRENTS_PAGER.getPageSize() : 0;
        List<Torrent> torrents = Torrent.search(keywords, offset, TORRENTS_PAGER.getPageSize());
        Pager pager = TORRENTS_PAGER;
        renderTemplate("Application/index.html", pager, keywords, torrents);
    }

    public static void create() {
        renderTemplate("Application/update.html");
    }

    public static void update(Long torrentId, String keywords, String activeTag) {
        Torrent torrent = Torrent.findById(torrentId);
        notFoundIfNull(torrent);
        render(torrent, keywords, activeTag);
    }

    public static void tags() {
        List<Tag> tags = Tag.findAll();

        StringBuilder sb = new StringBuilder();
        for (Tag tag : tags) {
            sb.append(tag.name).append('\n');
        }

        renderText(sb.toString());
    }

    public static void save(@Required @Valid Torrent torrent, File file, String tags, String keywords, String activeTag) {
        if (torrent.id == null) {
            validation.required(file);
        }
        validation.valid(file);
        if (validation.hasErrors()) {
            for (Map.Entry<String, List<Error>> stringListEntry : validation.errorsMap().entrySet()) {
                Logger.debug("%s : %s", stringListEntry.getKey(), stringListEntry.getValue());
            }
            flash.error("Veuillez corriger les erreurs");
            params.flash();
            validation.keep();
            if (torrent.id == null) {
                create();
            }
            update(torrent.id, keywords, activeTag);
        }

        if (torrent.id == null) {
            torrent.uploader = Security.connectedUser();
            torrent.creationDate = new Date();
            try {
                torrent.setFile(file);
            } catch (FileNotFoundException e) {
                Logger.error(e.getMessage());
            }
        } else {
            torrent.modificationDate = new Date();
        }
        torrent.tags = extractTags(tags);

        boolean tweetIt = false;
        if (torrent.id == null) {
            tweetIt = true;
        }

        torrent.save();

        if (tweetIt) {
            try {
                Twitter.init().setStatus("New : " + torrent.filename + " - http://bit.ly/xWYR4A");
            } catch (Exception e) {
                Logger.error(e.getMessage(), e);
            }
        }
        if (!StringUtils.isBlank(keywords)) {
            try {
                search(URLEncoder.encode(keywords, "UTF-8"));
            } catch (UnsupportedEncodingException e) {
                Logger.error(e.getMessage(), e);
            }
        } else if (!StringUtils.isBlank(activeTag)) {
            try {
                tag(URLEncoder.encode(activeTag, "UTF-8"));
            } catch (UnsupportedEncodingException e) {
                Logger.error(e.getMessage(), e);
            }
        } else {
            index();
        }
    }

    public static String uploadMultiple(File file) {
        Http.Response response = Http.Response.current();
        response.setHeader("Server", "Web Server");
        response.setHeader("Expires", "Mon, 26 Jul 1997 05:00:00 GMT");
        response.setHeader("Pragma", "no-cache");
        response.setHeader("Cache-Control", "no-store, no-cache, must-revalidate");

        Torrent torrent = new Torrent();
        try {
            torrent.uploader = Security.connectedUser();
            torrent.creationDate = new Date();
            torrent.setFile(file);
            torrent.filename = file.getName();
        } catch (FileNotFoundException e) {
            return "{\"jsonrpc\" : \"2.0\", \"error\" : {\"code\": 101, \"message\": \"Failed to open input stream.\"}, \"id\" : \"id\"}";
        }
        torrent.save();

        try {
            Twitter.init().setStatus("New : " + torrent.filename + " - http://bit.ly/xWYR4A");
        } catch (Exception e) {
            Logger.error(e.getMessage(), e);
        }

        return "{\"jsonrpc\" : \"2.0\", \"result\" : null, \"id\" : \"id\"}";
    }

    public static void delete(Long torrentId) {
        Torrent torrent = Torrent.findById(torrentId);
        notFoundIfNull(torrent);
        torrent.delete();
        flash.success("Le torrent a été supprimé avec succès");
        index();
    }

    public static void details(Long torrentId) {
        Torrent torrent = Torrent.findById(torrentId);
        notFoundIfNull(torrent);
        render(torrent);
    }

    public static void getTorrentFile(Long torrentId) {
        Torrent torrent = Torrent.findById(torrentId);
        notFoundIfNull(torrent);
        notFoundIfNull(torrent.getFile());
        File file = torrent.getFile();
        String filename = torrent.filename;
        if (!filename.endsWith(".torrent")) {
            filename += ".torrent";
        }
        renderBinary(file, filename);
    }

    private static List<Tag> extractTags(String tags) {
        List<Tag> listTags = Lists.newArrayList();
        if (StringUtils.isNotBlank(tags)) {
            String[] tabTags = tags.split(",");
            for (String stringTag : tabTags) {
                Tag tag = Tag.find("name=?", stringTag.trim()).first();
                if (tag == null) {
                    tag = new Tag();
                    tag.name = stringTag.trim();
                    tag.save();
                }
                listTags.add(tag);
            }
        }
        return listTags;
    }
}
