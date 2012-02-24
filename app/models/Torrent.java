package models;

import org.hibernate.Hibernate;
import org.hibernate.Session;
import org.hibernate.engine.jdbc.LobCreator;
import play.data.binding.As;
import play.data.validation.Required;
import play.db.jpa.JPA;
import play.db.jpa.Model;

import javax.persistence.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Blob;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;

@Entity
public class Torrent extends Model {

    @Required
    public String filename;

    private Blob file;

    @Column(columnDefinition = "TEXT")
    public String description;

    @ManyToMany
    public List<Tag> tags;

    @OneToOne
    public User uploader;

    @As(lang = {"*"}, value = {"dd/MM/yyyy HH:mm:ss"})
    public Date creationDate;

    @As(lang = {"*"}, value = {"dd/MM/yyyy HH:mm:ss"})
    public Date modificationDate;

    public Blob getFile() {
        return file;
    }

    public void setFile(File file) throws FileNotFoundException {
        this.file = getBlob(file);
    }

    public void deleteFile() {
        this.file = null;
    }

    private static Blob getBlob(File file) throws FileNotFoundException {
        FileInputStream fileInputStream = new FileInputStream(file);
        LobCreator lobCreator = Hibernate.getLobCreator((Session) JPA.em().getDelegate());
        return lobCreator.createBlob(fileInputStream, file.length());
    }

    public static List<Torrent> findTaggedWith(Tag tag, int page, int length) {
        return Torrent.find("SELECT t FROM Torrent t JOIN t.tags AS tag WHERE tag.name=?", tag.name).fetch(page, length);
    }

    public static long countTaggedWith(Tag tag) {
        return Torrent.count("FROM Torrent t JOIN t.tags AS tag WHERE tag.name=?", tag.name);
    }
}
