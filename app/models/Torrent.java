package models;

import org.hibernate.Hibernate;
import org.hibernate.Session;
import org.hibernate.engine.jdbc.LobCreator;
import play.data.binding.As;
import play.data.validation.Required;
import play.db.jpa.JPA;
import play.db.jpa.Model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ManyToMany;
import javax.persistence.OneToOne;
import java.io.*;
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

    public File getFile() {
        if (file != null) {
            File tempFile = new File("tempFile");
            InputStream is = null;
            try {
                is = file.getBinaryStream();
                OutputStream os = new FileOutputStream(tempFile);
                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = is.read(buffer)) != -1) {
                    os.write(buffer, 0, bytesRead);
                }
                is.close();
                os.close();
            } catch (SQLException e) {
                e.printStackTrace();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return tempFile;
        }
        return null;
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
