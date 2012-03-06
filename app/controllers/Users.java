package controllers;

import controllers.utils.Pager;
import models.Torrent;
import models.User;
import play.data.validation.Required;
import play.data.validation.Valid;
import play.db.jpa.JPA;
import play.mvc.Before;
import play.mvc.Controller;
import play.mvc.With;
import utils.IPUtils;

import java.util.List;

@With(Secure.class)
public class Users extends Controller {

    private static final Pager USERS_PAGER = new Pager();

    private static final Pager TORRENTS_PAGER = new Pager();

    @Before
    static void defaultData() {
        // Récupération de la pagination
        String pageParam = params.get("page");
        if (pageParam == null) {
            pageParam = "1";
        }
        USERS_PAGER.setPage(Integer.valueOf(pageParam));
        session.put("usersPager.page", pageParam);

        TORRENTS_PAGER.setPage(Integer.valueOf(pageParam));
        session.put("torrentsPager.page", pageParam);
    }

    public static void index() {
        USERS_PAGER.setElementCount(models.User.count());
        List<User> users = User.find("").fetch(USERS_PAGER.getPage(), USERS_PAGER.getPageSize());
        Pager pager = USERS_PAGER;
        render(users, pager);
    }

    public static void filter(String letter) {
        USERS_PAGER.setElementCount(models.User.countByLetter(letter));
        List<User> users = User.byLetter(letter).fetch(USERS_PAGER.getPage(), USERS_PAGER.getPageSize());
        Pager pager = USERS_PAGER;
        renderTemplate("Users/index.html", letter, users, pager);
    }

    @Check("isAdmin")
    public static void create() {
        String ipAdress = request.remoteAddress;
        renderTemplate("Users/update.html", ipAdress);
    }

    @Check("isAdmin")
    public static void update(Long userId) {
        User user = User.findById(userId);
        notFoundIfNull(user);
        String ipAddress = IPUtils.getIpFromRequest(request);
        render(user, ipAddress);
    }

    @Check("isAdmin")
    public static void save(@Required @Valid User user) {
        if (validation.hasErrors()) {
            flash.error("Veuillez corriger les erreurs");
            params.flash();
            validation.keep();
            if (user.id == null) {
                create();
            }
            update(user.id);
        }
        user.firstName = user.firstName.substring(0, 1).toUpperCase() + user.firstName.substring(1).toLowerCase();
        user.lastName = user.lastName.toUpperCase();

        user.save();
        flash.success("L'utilisateur a correctement été enregistré");
        index();
    }

    @Check("isAdmin")
    public static void activate(Long userId) {
        User user = User.findById(userId);
        notFoundIfNull(user);
        user.isActive = !user.isActive;
        user.save();
        index();
    }

    @Check("isAdmin")
    public static void delete(Long userId) {
        User user = User.findById(userId);
        notFoundIfNull(user);
        int result = JPA.em().createNativeQuery("DELETE FROM Torrent_Tag WHERE torrent_id IN (SELECT tota.torrent_id FROM Torrent_Tag tota INNER JOIN Torrent t ON tota.torrent_id = t.id WHERE t.uploader_id=?)").setParameter(1, user.id).executeUpdate();
        Torrent.delete("uploader=?", user);
        user.delete();
        flash.success("L'utilisateur a correctement été supprimé");
        index();
    }

    public static void details(Long userId) {
        User user = User.findById(userId);
        notFoundIfNull(user);
        TORRENTS_PAGER.setElementCount(Torrent.count("uploader=?", user));
        List<Torrent> torrents = Torrent.find("uploader=?", user).fetch(TORRENTS_PAGER.getPage(), TORRENTS_PAGER.getPageSize());
        Pager pager = TORRENTS_PAGER;
        render(user, torrents, pager);
    }
}
