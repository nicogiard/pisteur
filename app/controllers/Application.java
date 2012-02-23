package controllers;

import controllers.utils.Pager;
import models.Tag;
import models.Torrent;
import play.data.validation.Required;
import play.data.validation.Valid;
import play.mvc.Before;
import play.mvc.Controller;
import play.mvc.With;

import java.util.Date;
import java.util.List;

@With(Secure.class)
public class Application extends Controller {

    private static final Pager TORRENTS_PAGER = new Pager();

    @Before
    static void defaultData() {
        List<Tag> tags = Tag.findAll();
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
        List<Torrent> torrents = Torrent.find("").fetch(TORRENTS_PAGER.getPage(), TORRENTS_PAGER.getPageSize());
        Pager pager = TORRENTS_PAGER;
        render(torrents, pager);
    }

    public static void tag(String tagName) {
        Tag activeTag = Tag.findByName(tagName);
        notFoundIfNull(activeTag);
        TORRENTS_PAGER.setElementCount(Torrent.countTaggedWith(activeTag));
        List<Torrent> torrents = Torrent.findTaggedWith(activeTag, TORRENTS_PAGER.getPage(), TORRENTS_PAGER.getPageSize());
        Pager pager = TORRENTS_PAGER;
        renderTemplate("Application/index.html", pager, torrents, activeTag);
    }

    public static void create() {
        renderTemplate("Application/update.html");
    }

    public static void update(Long torrentId) {
        Torrent torrent = Torrent.findById(torrentId);
        notFoundIfNull(torrent);
        render(torrent);
    }

    public static void save(@Required @Valid Torrent torrent) {
        if (validation.hasErrors()) {
            flash.error("Veuillez corriger les erreurs");
            params.flash();
            validation.keep();
            if (torrent.id == null) {
                create();
            }
            update(torrent.id);
        }

        if (torrent.id == null) {
            torrent.uploader = Security.connectedUser();
            torrent.creationDate = new Date();
        } else {
            torrent.modificationDate = new Date();
        }
        // torrent.save();

        index();
    }
}
