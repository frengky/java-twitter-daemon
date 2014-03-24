package com.frengky.twitter;
import java.io.File;

import org.apache.log4j.Logger;

public class Twitter {
	private static Logger log = Logger.getLogger(Twitter.class);
	
	protected void shutdown() {
		log.info("Shutting down...");
	}
	
	public static void main(String[] args) {
		log.info("Twitter Daemon v1.0 (frengky.lim@gmail.com)");
		
		String configDir = System.getProperty("configdir");
		log.info("Reading configuration files from directory " + configDir);
		
		File dir = new File(configDir);
		File[] files = dir.listFiles();
		
		String config = null;
		for(int i=0; i<files.length; i++) {
			config = files[i].getAbsolutePath();
			(new Thread(new TwitterClient(config))).start();
		}
	}
}
