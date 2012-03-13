package models;

import javax.persistence.Entity;

import play.db.jpa.Model;

@Entity
public class AttachedFile extends Model {

	public String name;
	
	public String size;
	
}
