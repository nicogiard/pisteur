package utils;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.io.Files;

import exception.utils.TorrentParserException;

public final class TorrentParser {
	
	
	private String announceURL;
    private String comment;
    private String createdBy;
    private long creationDate;
    private String encoding;
    private String saveAs;
    private int pieceLength;
    private String infosHash;

    public String getInfosHash() {
		return infosHash;
	}

	public void setInfosHash(String infosHash) {
		this.infosHash = infosHash;
	}

	/* In case of multiple files torrent, saveAs is the name of a directory
     * and name contains the path of the file to be saved in this directory
     */
    private List<String> name = new ArrayList<String>();
    private List<Long> length = new ArrayList<Long>();

    private long total_length;
	
	public String getAnnounceURL() {
		return announceURL;
	}

	public void setAnnounceURL(String announceURL) {
		this.announceURL = announceURL;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public String getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
	}

	public long getCreationDate() {
		return creationDate;
	}

	public void setCreationDate(long creationDate) {
		this.creationDate = creationDate;
	}

	public String getEncoding() {
		return encoding;
	}

	public void setEncoding(String encoding) {
		this.encoding = encoding;
	}

	public String getSaveAs() {
		return saveAs;
	}

	public void setSaveAs(String saveAs) {
		this.saveAs = saveAs;
	}

	public int getPieceLength() {
		return pieceLength;
	}

	public void setPieceLength(int pieceLength) {
		this.pieceLength = pieceLength;
	}

	public List<String> getName() {
		return name;
	}

	public void setName(ArrayList name) {
		this.name = name;
	}

	public List<Long> getLength() {
		return length;
	}

	public void setLength(ArrayList length) {
		this.length = length;
	}

	public long getTotal_length() {
		return total_length;
	}

	public void setTotal_length(long total_length) {
		this.total_length = total_length;
	}

	private byte[] torrentBytes = null;
	private int position = 0;
	
	public TorrentParser(File torrentFile) throws TorrentParserException{
		InputStreamReader reader = null;
		try {
			FileInputStream is = new FileInputStream(torrentFile);
			this.torrentBytes = new byte[(int)torrentFile.length()]; 
			is.read(torrentBytes);
		} catch (FileNotFoundException e) {
			throw new TorrentParserException("File not found");
		} catch (IOException e) {
			throw new TorrentParserException("unable to read the torrent file");
		} 
		finally {		    
		    if (reader != null) try { reader.close(); } catch (IOException ignore) {}
		}
	}
	
	public void parse() throws TorrentParserException{
		if(torrentBytes != null && torrentBytes.length > 0){
			position = 0;

			Map metaInfo = (HashMap<String, Object>)this.bdecode();
			
			if(metaInfo.containsKey("announce")){
	            this.announceURL = (String)metaInfo.get("announce");
			}
	        if(metaInfo.containsKey("comment")){
	            this.comment = (String)metaInfo.get("comment");
	        }
	        if(metaInfo.containsKey("created by")){
	            this.createdBy = (String)metaInfo.get("created by");
	        }
	        if(metaInfo.containsKey("creation date")){
	            this.creationDate = (Long)metaInfo.get("creation date");
	        }
	        if(metaInfo.containsKey("encoding")){
	            this.encoding = (String)metaInfo.get("encoding");
	        }

	        //Store the info field data
	        if(metaInfo.containsKey("info")){
	            Map info = (Map) metaInfo.get("info");	            	
	            //TODO hash du dictionnaire info
	            this.infosHash = Utils.byteArrayToURLString(Utils.hash(BEncode.encode(Utils.getJSON(info)).getBytes(Constants.DEFAULT_CHARSET)));
	            if (info.containsKey("name")){
	                this.saveAs = (String)info.get("name");
	            }

	            if (info.containsKey("files")) {
	                List multFiles = (List) info.get("files");
	                this.total_length = 0;
	                for (int i = 0; i < multFiles.size(); i++) {
	                    this.length.add((Long) ((Map) multFiles.get(i)).
	                                             get("length"));
	                    this.total_length += (Long) ((Map) multFiles.get(i)).
	                                                  get("length");

	                    List path = (List) ((Map) multFiles.get(i)).get(
	                            "path");
	                    String filePath = "";
	                    for (int j = 0; j < path.size(); j++) {
	                        filePath += new String((byte[]) path.get(j));
	                    }
	                    this.name.add(filePath);
	                }
	            } else {
	                this.length.add((Long) info.get("length"));
	                this.total_length = (Long) info.get("length");
	                this.name.add((String)info.get("name"));
	            }
	        }
		}
	}
	
	 private Object bdecode() throws TorrentParserException{
		 char currentChar = (char)torrentBytes[position];
		 if(currentChar == 'i'){
			 position ++;
			 return this.decodeInteger();
		 }
		 else if(currentChar == 'd'){
			 position ++;
			 return this.decodeDictionary();
		 }
		 else if(currentChar == 'l'){
			 position ++;
			 return this.decodeList();
		 }
		 else if(Character.isDigit(currentChar)){
			 return this.decodeString();
		 }
		 else{
			return null; 
		 }
	 }
	
	private Map<String, Object> decodeDictionary() throws TorrentParserException{
		HashMap<String, Object> dictionary = Maps.newHashMap();
        while (position <= torrentBytes.length) {        	
        	char currentChar = (char)torrentBytes[position];
            if (currentChar == 'e') {
                break;
            }
            String key = new String(this.decodeString());
            Object value = this.bdecode();
            dictionary.put(key, value);
        }
        position ++;
        return dictionary;		
	}
	
	private List decodeList() throws TorrentParserException{
		ArrayList<Object> liste = Lists.newArrayList();
		
        while ((char)torrentBytes[position] != 'e' && position < torrentBytes.length) {
            liste.add(this.bdecode());
        }
        position ++;
        return liste;
	}
	
	private String decodeString() throws TorrentParserException{
		 StringBuilder sb = new StringBuilder();
		try{
			int colonPosition = position;
			while((colonPosition < torrentBytes.length && (char)torrentBytes[colonPosition] != ':')){
				colonPosition ++;
			}	       
	        for(int i=position; i < colonPosition; i++){
	        	sb.append((char)torrentBytes[i]);
	        }
	        
	        int length = Integer.parseInt(sb.toString());
	        	        
	        if (length == 0) {
	        	this.position = colonPosition + length + 1;
	            return null;
	        } else {
	        	this.position = colonPosition + length + 1;
	
	    		byte[] tempArray = new byte[length];
	    		int tempArrayIndex = 0;
	        	for(int i=colonPosition + 1; i < colonPosition + 1 + length; i++){
	            	tempArray[tempArrayIndex++] = torrentBytes[i];
	            }
	        	return new String(tempArray, Constants.DEFAULT_CHARSET);
	        }
		}
        catch (NumberFormatException e) {
			throw new TorrentParserException("bad length ("+ sb.toString() +") for string at position : " + position );
		}
	}
	
	private Long decodeInteger() throws TorrentParserException{
		StringBuilder sb = new StringBuilder();
		int ePosition = position;
		try{
			while(ePosition < torrentBytes.length && (char)torrentBytes[ePosition] != 'e'){
				ePosition ++;
			}
			
			for(int i=position; i < ePosition; i++){
	        	sb.append((char)torrentBytes[i]);
	        }
	        long valeur = Long.parseLong(sb.toString());
	        position = ePosition + 1;
	        return valeur;
		}
		catch(NumberFormatException e){
			throw new TorrentParserException("Wrong value for integer ("+ sb.toString() +") at position : " + position );
		}
	}
	
	public static void main(String args[]){
		try {
			TorrentParser parser = new TorrentParser(new File("./test/2012 Francais.avi.avi.torrent"));
			parser.parse();
			System.out.println("announce url : " + parser.announceURL);
			System.out.println("comment : " + parser.comment);
			System.out.println("createdBy : " + parser.createdBy);
			System.out.println("creationDate : " + parser.creationDate);
			System.out.println("encoding : " + parser.encoding);
			System.out.println("pieceLength : " + parser.pieceLength);
			System.out.println("saveAs : " + parser.saveAs);
			System.out.println("total_length : " + parser.total_length);
			System.out.println("name : " + parser.name);
			System.out.println("infoHash : " + parser.infosHash);
		} catch (TorrentParserException e) {
			e.printStackTrace();
		}
	}
}
