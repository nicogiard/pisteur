package models;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;

import exception.utils.TorrentParserException;

import org.hibernate.Hibernate;
import org.hibernate.Session;
import org.hibernate.engine.jdbc.LobCreator;
import play.Play;
import play.data.binding.As;
import play.data.validation.Required;
import play.db.jpa.JPA;
import play.db.jpa.Model;
import play.modules.search.Field;
import play.modules.search.Indexed;
import play.modules.search.Query;
import play.modules.search.Search;
import utils.TorrentParser;
import utils.Utils;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ManyToMany;
import javax.persistence.OneToOne;
import java.io.*;
import java.sql.Blob;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;

@Indexed
@Entity
public class Torrent extends Model {

    @Field
    @Required
    public String filename;

    private Blob file;

    @Field
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

    public String info_hash;
    
    public String totalSize;

    public File getFile() {
        if (file != null) {
            File tempFile = new File(Play.configuration.getProperty("temp") + File.separator + "tempFile");
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
                
                // TODO : faudrait gérer un peu mieux les exceptions ici
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
    	try {
    		this.file = getBlob(file);
			this.getInfoFromTorrentFile(file);
		} catch (TorrentParserException e) {
			e.printStackTrace();
		}
        
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
        return Torrent.find("SELECT t FROM Torrent t JOIN t.tags AS tag WHERE tag.name=? ORDER BY creationDate DESC", tag.name).fetch(page, length);
    }

    public static long countTaggedWith(Tag tag) {
        return Torrent.count("FROM Torrent t JOIN t.tags AS tag WHERE tag.name=?", tag.name);
    }

    public static List<Torrent> findNotTagged(int page, int length) {
        int pageLimit = page;
        if (pageLimit < 1) {
            pageLimit = 1;
        }
        int startLimit = (pageLimit - 1) * length;
        return (List<Torrent>) JPA.em().createNativeQuery("SELECT t.id, t.filename, t.description, t.file, t.uploader_id, t.creationDate, t.modificationDate, t.info_hash, t.totalSize FROM Torrent t LEFT OUTER JOIN Torrent_Tag AS tt ON t.id = tt.torrent_id WHERE tt.torrent_id IS NULL ORDER BY t.creationDate DESC LIMIT ? , ?", Torrent.class).setParameter(1, startLimit).setParameter(2, length).getResultList();
    }

    public static long countNotTagged() {
        long count = JPA.em().createNativeQuery("SELECT COUNT(t.id) FROM Torrent t LEFT OUTER JOIN Torrent_Tag AS tt ON t.id = tt.torrent_id WHERE tt.torrent_id IS NULL").getFirstResult();
        return count;
    }

    public static List<Torrent> search(String keywords, int offset, int length) {
        List<Torrent> torrents = Lists.newArrayList();
        if (!Strings.isNullOrEmpty(keywords)) {
            String trimKeywords = keywords.trim().toLowerCase();
            String queryString = "filename:" + trimKeywords + " OR description:" + trimKeywords;
            Query query = Search.search(queryString, Torrent.class);
            torrents = query.page(offset, length).fetch();
        }
        return torrents;
    }

    public static long countSearch(String keywords) {
        long count = 0;
        if (!Strings.isNullOrEmpty(keywords)) {
            String trimKeywords = keywords.trim();
            String queryString = "filename:" + trimKeywords + " OR description:" + trimKeywords;
            Query query = Search.search(queryString, Torrent.class);
            count = query.count();
        }
        return count;
    }
    
    private void getInfoFromTorrentFile(File torrentFile) throws TorrentParserException{
    	TorrentParser parser = new TorrentParser(torrentFile);
    	parser.parse();
    	this.totalSize = Utils.byteMultipleSize(parser.getTotal_length());
    }
    
}
