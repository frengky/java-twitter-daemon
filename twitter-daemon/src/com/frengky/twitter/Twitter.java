package com.frengky.twitter;
import java.io.File;

public class Twitter {
	public static void main(String[] args) {
		final TwitterClient twitter = new TwitterClient();
		
		Runtime.getRuntime().addShutdownHook(new Thread() {
			public void run() {
				twitter.dispose();
			}
		});
		
		String configDir = System.getProperty("configdir");
		
		File dir = new File(configDir);
		File[] files = dir.listFiles();
		
		String ext = null;
		String config = null;
		for(int i=0; i<files.length; i++) {
			config = files[i].getAbsolutePath();
			
			ext = getExtension(new File(config));
			if(ext != null && ext.equals("properties")) {
				twitter.dispatch(config);
			}
		}
	}
	
	public static String getExtension(File f) {
	    String ext = null;
	    String s = f.getName();
	    int i = s.lastIndexOf('.');

	    if (i > 0 &&  i < s.length() - 1) {
	        ext = s.substring(i+1).toLowerCase();
	    }
	    return ext;
	}	
}
