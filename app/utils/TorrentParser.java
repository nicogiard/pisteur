package utils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.google.common.base.Strings;
import com.google.common.io.Files;

public final class TorrentParser {
	
	private TorrentParser(){		
	}
	
	public static Map<String, String> getInfo(File torrentFile) throws IOException{
		//StringBuilder info = new StringBuilder();
		List<String> lines = Files.readLines(torrentFile, Constants.DEFAULT_CHARSET);
		for (String line : lines) {
			if(info.length() > 0){
				info.append(line);
			}
			if(line.indexOf(BEncode.encode("info")) > 0){
				info.append(line.substring(line.indexOf(BEncode.encode("info"))));
			}
		}
		return Strings.emptyToNull(info.toString());
	}
	
	public static String getInfo(File torrentFile){
		
	}
	
	public 
}
