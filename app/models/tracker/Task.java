package models.tracker;

import play.db.jpa.Model;

import javax.persistence.Entity;

@Entity
public class Task extends Model {
    public String name;
    public int value;
}
