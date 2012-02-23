package models;

import play.data.validation.Required;
import play.db.jpa.Model;

import javax.persistence.Entity;

@Entity
public class Tag extends Model {
    @Required
    public String name;

    public static Tag findByName(String tagName) {
        return Tag.find("byName", tagName).first();
    }
}
