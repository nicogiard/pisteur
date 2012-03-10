package models;

import play.data.validation.Required;
import play.db.jpa.JPA;
import play.db.jpa.Model;

import javax.persistence.Entity;
import java.util.List;

@Entity
public class Tag extends Model {
    @Required
    public String name;

    public static Tag findByName(String tagName) {
        return Tag.find("byName", tagName).first();
    }

    public static List<Tag> findOrderByMostUsed() {
        return (List<Tag>) JPA.em().createNativeQuery("SELECT tag.id, tag.name, COUNT(*) AS nombre FROM `Tag` tag INNER JOIN `Torrent_Tag` tt ON tag.id=tt.tags_id GROUP BY tt.tags_id ORDER BY nombre DESC", Tag.class).getResultList();
    }
}
