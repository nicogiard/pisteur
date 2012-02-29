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
import play.mvc.With;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Date;
import java.util.List;
import java.util.Map;

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
    
    public static void search(String keywords){
    	TORRENTS_PAGER.setElementCount(Torrent.countSearch(keywords));    	
        List<Torrent> torrents = Torrent.search(keywords, TORRENTS_PAGER.getPage(), TORRENTS_PAGER.getPageSize());        
        Pager pager = TORRENTS_PAGER;
        renderTemplate("Application/index.html", pager, keywords, torrents);
    }
    
    public static void create() {
        renderTemplate("Application/update.html");
    }

    public static void update(Long torrentId) {
        Torrent torrent = Torrent.findById(torrentId);
        notFoundIfNull(torrent);
        render(torrent);
    }

    public static void save(@Required @Valid Torrent torrent, File file, String tags) {
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
            update(torrent.id);
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
        torrent.save();
        index();
    }


    public static void delete(Long id) {
        Torrent torrent = Torrent.findById(id);
        notFoundIfNull(torrent);
        torrent.delete();
        flash.success("Le torrent a été supprimé avec succès");
        index();
    }

    public static void getTorrentFile(Long torrentId) {
        Torrent torrent = Torrent.findById(torrentId);
        notFoundIfNull(torrent);
        notFoundIfNull(torrent.getFile());
        File file = torrent.getFile();
        renderBinary(file, torrent.filename);
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
