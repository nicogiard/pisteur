package utils;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
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

public final class TorrentParser {
	
	
	 public String announceURL;
    public String comment;
    public String createdBy;
    public long creationDate;
    public String encoding;
    public String saveAs;
    public int pieceLength;

    /* In case of multiple files torrent, saveAs is the name of a directory
     * and name contains the path of the file to be saved in this directory
     */
    public ArrayList name = new ArrayList();
    public ArrayList length = new ArrayList();

    public byte[] info_hash_as_binary;
    public String info_hash_as_hex;
    public String info_hash_as_url;
    public long total_length;

    public ArrayList piece_hash_values_as_binary;
    public ArrayList piece_hash_values_as_hex;
    public ArrayList piece_hash_values_as_url;
	
	private byte[] torrentBytes = null;
	private int position = 0;
	
	public TorrentParser(File torrentFile) throws IOException{
		InputStreamReader reader = null;
		 StringBuilder sb = new StringBuilder();
		try {
			FileInputStream is = new FileInputStream(torrentFile);
			this.torrentBytes = new byte[(int)torrentFile.length()]; 
			is.read(torrentBytes);
		    
		} finally {
		    // Always close resources in finally!
		    if (reader != null) try { reader.close(); } catch (IOException ignore) {}
		}
	}
	
	public void parse(){
		if(torrentBytes != null){
			position = 0;

			Map metaInfo = (HashMap<String, Object>)this.bdecode();
			
			if(metaInfo.containsKey("announce")){
	            this.announceURL = new String((byte[]) metaInfo.get("announce"));
			}
	        if(metaInfo.containsKey("comment")){
	            this.comment = new String((byte[]) metaInfo.get("comment"));
	        }
	        if(metaInfo.containsKey("created by")){
	            this.createdBy = new String((byte[]) metaInfo.get("created by"));
	        }
	        if(metaInfo.containsKey("creation date")){
	            this.creationDate = (Long)metaInfo.get("creation date");
	        }
	        if(metaInfo.containsKey("encoding")){
	            this.encoding = new String((byte[]) metaInfo.get("encoding"));
	        }

	        //Store the info field data
	        if(metaInfo.containsKey("info")){
	            Map info = (Map) metaInfo.get("info");	            	
	            	//TODO hash du champ info	           
	            if (info.containsKey("name")){
	                this.saveAs = new String((byte[]) info.get("name"));
	            }
	            if (info.containsKey("piece length")){
	                this.pieceLength = ((Long) info.get("piece length")).intValue();
	            }

	            if (info.containsKey("pieces")) {
	                byte[] piecesHash2 = (byte[]) info.get("pieces");

	                for (int i = 0; i < piecesHash2.length / 20; i++) {
	                    //TODO faire le hash
	                }
	            }

	            if (info.containsKey("files")) {
	                List multFiles = (List) info.get("files");
	                this.total_length = 0;
	                for (int i = 0; i < multFiles.size(); i++) {
	                    this.length.add(((Long) ((Map) multFiles.get(i)).
	                                             get("length")).intValue());
	                    this.total_length += ((Long) ((Map) multFiles.get(i)).
	                                                  get("length")).intValue();

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
	                this.name.add(new String((byte[]) info.get("name")));
	            }
	        }
		}
	}
	
	 private Object bdecode(){
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
	
	private Map<String, Object> decodeDictionary(){
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
	
	private List decodeList(){
		ArrayList<Object> liste = Lists.newArrayList();
		
        while ((char)torrentBytes[position] != 'e') {
            liste.add(this.bdecode());
        }
        position ++;
        return liste;
	}
	
	private byte[] decodeString(){		
		int colonPosition = position;
		while(colonPosition < torrentBytes.length && (char)torrentBytes[colonPosition] != ':'){
			colonPosition ++;
		}
        StringBuilder sb = new StringBuilder();
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
        	return tempArray;
        }
	}
	
	private Long decodeInteger(){
		int ePosition = position;
		while(ePosition < torrentBytes.length && (char)torrentBytes[ePosition] != 'e'){
			ePosition ++;
		}
		StringBuilder sb = new StringBuilder();
		for(int i=position; i < ePosition; i++){
        	sb.append((char)torrentBytes[i]);
        }
        long valeur = Long.parseLong(sb.toString());
        position = ePosition + 1;
        return valeur;
	}
	
	public static void main(String args[]){
		try {
			TorrentParser parser = new TorrentParser(new File("./test/GroovyinActionSecondEdition.pdf.torrent"));
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
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
