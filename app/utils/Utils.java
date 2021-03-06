/*
 * Java Bittorrent API as its name indicates is a JAVA API that implements the Bittorrent Protocol
 * This project contains two packages:
 * 1. jBittorrentAPI is the "client" part, i.e. it implements all classes needed to publish
 *    files, share them and download them.
 *    This package also contains example classes on how a developer could create new applications.
 * 2. trackerBT is the "tracker" part, i.e. it implements a all classes needed to run
 *    a Bittorrent tracker that coordinates peers exchanges. *
 *
 * Copyright (C) 2007 Baptiste Dubuis, Artificial Intelligence Laboratory, EPFL
 *
 * This file is part of jbittorrentapi-v1.0.zip
 *
 * Java Bittorrent API is free software and a free user study set-up;
 * you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * Java Bittorrent API is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Java Bittorrent API; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 * @version 1.0
 * @author Baptiste Dubuis
 * To contact the author:
 * email: baptiste.dubuis@gmail.com
 *
 * More information about Java Bittorrent API:
 *    http://sourceforge.net/projects/bitext/
 */

package utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Iterator;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * A set of utility methods used by several classes
 *
 * @author Bat
 */
public class Utils {

	private static final long TERAOCTET = (long) Math.pow(2, 40);  
	private static final long GIGAOCTET = (long) Math.pow(2, 30);
	private static final long MEGAOCTET = (long) Math.pow(2, 20);
	private static final long KILOOCTET = (long) Math.pow(2, 10);
    /*
     * Convert a byte array into a URL encoded String
     */
    public static String byteArrayToURLString(byte in[]) {
        byte ch = 0x00;
        int i = 0;
        if (in == null || in.length <= 0) {
            return null;
        }

        String pseudo[] = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "A", "B", "C", "D", "E", "F"};
        StringBuffer out = new StringBuffer(in.length * 2);

        while (i < in.length) {
            // First check to see if we need ASCII or HEX
            if ((in[i] >= '0' && in[i] <= '9')
                    || (in[i] >= 'a' && in[i] <= 'z')
                    || (in[i] >= 'A' && in[i] <= 'Z') || in[i] == '$'
                    || in[i] == '-' || in[i] == '_' || in[i] == '.'
                    || in[i] == '+' || in[i] == '!') {
                out.append((char) in[i]);
                i++;
            } else {
                out.append('%');
                ch = (byte) (in[i] & 0xF0); // Strip off high nibble
                ch = (byte) (ch >>> 4); // shift the bits down
                ch = (byte) (ch & 0x0F); // must do this is high order bit is
                // on!
                out.append(pseudo[(int) ch]); // convert the nibble to a
                // String Character
                ch = (byte) (in[i] & 0x0F); // Strip off low nibble
                out.append(pseudo[(int) ch]); // convert the nibble to a
                // String Character
                i++;
            }
        }

        String rslt = new String(out);

        return rslt;
    }

    public static String byteStringToByteArray(String s) {
        String ret = "";
        for (int i = 0; i < s.length(); i += 2)
            ret += "%" + (char) s.charAt(i) + (char) s.charAt(i + 1);
        return ret;
    }

    /**
     * Convert a byte[] array to readable string format. This makes the "hex"
     * readable!
     *
     * @param in byte[] buffer to convert to string format
     * @return result String buffer in String format
     * @author Jeff Boyle
     */
    // Taken from http://www.devx.com/tips/Tip/13540
    public static String byteArrayToByteString(byte in[]) {
        byte ch = 0x00;
        int i = 0;
        if (in == null || in.length <= 0) {
            return null;
        }

        String pseudo[] = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "A", "B", "C", "D", "E", "F"};
        StringBuffer out = new StringBuffer(in.length * 2);

        while (i < in.length) {
            ch = (byte) (in[i] & 0xF0); // Strip off high nibble
            ch = (byte) (ch >>> 4); // shift the bits down
            ch = (byte) (ch & 0x0F); // must do this is high order bit is on!
            out.append(pseudo[(int) ch]); // convert the nibble to a String
            // Character
            ch = (byte) (in[i] & 0x0F); // Strip off low nibble
            out.append(pseudo[(int) ch]); // convert the nibble to a String
            // Character
            i++;
        }

        String rslt = new String(out);

        return rslt;
    }
    
    public static String byteSizeToStringSize(long size){
    	long taille;
    	long reste;
    	String decime;

    	if (size >= Utils.TERAOCTET) {
    		taille = (long)Math.floor(size/Utils.TERAOCTET);
    		reste = size%Utils.TERAOCTET;
    		if(reste > 10 * Utils.GIGAOCTET){
    			if(reste > 100 * Utils.GIGAOCTET){
    				return taille + "," + Math.round(reste/(100*Utils.GIGAOCTET))+"To";
    			}    			
    			return taille + ",0" +  Math.round(reste/(10*Utils.GIGAOCTET))+"To";
    		}
    		return taille+"To";
    	}

    	if(size >= Utils.GIGAOCTET){
    		taille = (long)Math.floor(size/Utils.GIGAOCTET);
    		reste = size%Utils.GIGAOCTET;
    		if(reste > 10 * Utils.MEGAOCTET){
    			if(reste > 100 * Utils.MEGAOCTET){
    				return taille + "," + Math.round(reste/(100*Utils.MEGAOCTET))+"Go";
    			}    			
    			return taille + ",0" +  Math.round(reste/(10*Utils.MEGAOCTET))+"Go";
    		}
    		return taille+"Go";
    	}
    	
    	if(size >= Utils.MEGAOCTET ){
    		taille = (long)Math.floor(size/Utils.MEGAOCTET);
    		reste = size%Utils.MEGAOCTET;
    		if(reste >= 10 * Utils.KILOOCTET){
    			if(reste >= 100 * Utils.KILOOCTET){
    				return taille + "," + Math.round(reste/(100*Utils.KILOOCTET))+"Mo";
    			}    			
    			return taille + ",0" +  Math.round(reste/(10*Utils.KILOOCTET))+"Mo";
    		}
    		return taille+"Mo";
    	}
    	if(size >= Utils.KILOOCTET){
    		taille = (long)Math.floor(size/Utils.KILOOCTET);
    		return taille+"Ko";
    	}
    	return size + "Octets";
    	
    }
    
    /**
     * Compute the SHA1 hash of the array in parameter
     *
     * @param hashThis The array to be hashed
     * @return byte[] The SHA1 hash
     */
    public static byte[] hash(byte[] hashThis) {
        try {
            byte[] hash = new byte[20];
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            hash = md.digest(hashThis);
            return hash;
        } catch (NoSuchAlgorithmException nsae) {
            System.err.println("SHA-1 algorithm is not available...");
            System.exit(2);
        }
        return null;
    }
    
    public static JsonObject getJSON(Map map) {
    	Gson gson = new Gson();
    	String jsonString = gson.toJson(map);
    	JsonParser parser= new JsonParser();
    	JsonElement jsonElement = parser.parse(jsonString);
    	return jsonElement.getAsJsonObject();
	}
}
