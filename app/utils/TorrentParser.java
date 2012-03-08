package utils;

import java.io.File;
import java.io.IOException;
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

public final class TorrentParser {
	
	
	//Name of the torrent
	private String name = null;
	
	//Filename of the torrent
	private String fileName = null;

	// Comment
	private String comment = null;

	//date de creation
	private Date creationDate = null;

	// liste des fichiers contenu dans le torrent
	private List<String> files = null;

	//taille du torrent
	private long size = 0;

	//Signature of the software which created the torrent
	private String created_by = null;
	
	//string tracker (the tracker the torrent has been received from)
	private String announce = null;

	//Liste des tracker pour ce torrent
	private List<String> announces = null;

	//Source
	private String source = null;

	//source length
	private long sourceLength = 0;
	

	//Current position of the string
	private int position = 0;
	
	//Current Char
	private char currentChar;
	
    //Torrent is marked as 'private'
	private boolean isPrivate = false;
	
	private String torrentString = null;
	
	public TorrentParser(File torrentFile) throws IOException{
		torrentString = Files.toString(torrentFile, Constants.DEFAULT_CHARSET);
	}
	
	public Map<String, Object> parse(){
		if(!Strings.isNullOrEmpty(torrentString)){
			position = 0;
			//retourne
			Map info = (HashMap<String, Object>)this.bdecode();
			return info;
		}
		return null;
	}
	
	 private Object bdecode(){
		 currentChar = torrentString.charAt(position);
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
	
	private Map<String, Object> decodeDictionary(){
		HashMap<String, Object> dictionary = Maps.newHashMap();
        while (position <= torrentString.length()) {        	
        	currentChar = torrentString.charAt(position);
            if (currentChar == 'e') {
                break;
            }
            String key = this.decodeString();            
            Object value = this.bdecode();
            dictionary.put(key, value);
        }
        position ++;
        return dictionary;		
	}
	
	private List decodeList(){
		ArrayList<Object> liste = Lists.newArrayList();
		
        while (torrentString.charAt(position) != 'e') {
            liste.add(this.bdecode());
        }
        position ++;
        return liste;
	}
	
	private String decodeString(){
		 // Check for bad leading zero
        if (torrentString.charAt(position) == '0' && torrentString.charAt(position + 1) != ':'){
        	
        }
        // Find position of colon
        // Supress error message if colon is not found which may be caused by a corrupted or wrong encoded string
        int colonPosition = torrentString.indexOf(':', position);  
        if ( colonPosition == -1) {
            
        }
        // Get length of string
        
        int length = Integer.parseInt(torrentString.substring(position, colonPosition));        
        if (length + colonPosition + 1 > torrentString.length()) {
        	
        }
        // Get string
        if (length == 0) {
        	this.position = colonPosition + length + 1;
            return "";
        } else {
        	this.position = colonPosition + length + 1;
        	return torrentString.substring(colonPosition + 1, colonPosition + 1 + length);
        }
	}
	
	private Long decodeInteger(){
		int ePosition = torrentString.indexOf('e', position);
        long valeur = Long.parseLong(torrentString.substring(position, ePosition));
        position = ePosition + 1;
        return valeur;
	}
	
	public static void main(String args[]){
		try {
			TorrentParser parser = new TorrentParser(new File("./test/GroovyinActionSecondEdition.pdf.torrent"));
			parser.parse();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
