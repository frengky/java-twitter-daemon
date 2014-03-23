package com.frengky.twitter;
import java.io.FileInputStream;
import java.util.AbstractCollection;
import java.util.Iterator;
import java.util.Properties;

import org.apache.log4j.Logger;

import twitter4j.*;
import twitter4j.conf.ConfigurationBuilder;

public class Twitter {
	private static Logger log = Logger.getLogger(Twitter.class);
	
	protected void shutdown() {
		log.info("Shutting down...");
	}
	
	public static void main(String[] args) {
		log.info("Twitter Daemon v1.0 (frengky.lim@gmail.com)");
		
		String config = System.getProperty("config");
		String screenName = null;
		String consumerKey = null;
		String consumerSecret = null;
		String accessToken = null;
		String accessTokenSecret = null;
		
		log.info("Reading twitter configuration from " + config);
		try {
			Properties prop = new Properties();
			prop.load(new FileInputStream(config));
			
			screenName = prop.getProperty("twitter.screenName").trim();
			consumerKey = prop.getProperty("twitter.oauth.consumerKey").trim();
			consumerSecret = prop.getProperty("twitter.oauth.consumerSecret").trim();
			accessToken = prop.getProperty("twitter.oauth.accessToken").trim();
			accessTokenSecret = prop.getProperty("twitter.oauth.accessTokenSecret").trim();
			
			log.info("twitter.screenName: " + screenName);
			log.info("twitter.oauth.consumerKey: " + consumerKey);
			log.info("twitter.oauth.consumerSecret: " + consumerSecret);
			log.info("twitter.oauth.accessToken: " + accessToken);
			log.info("twitter.oauth.accessTokenSecret: " + accessTokenSecret);
			
		} catch(Exception e) {
			log.error("Error reading twitter configuration: " + e.toString());
		}

		if(screenName != null) {
			ConfigurationBuilder cb = new ConfigurationBuilder();
			cb.setDebugEnabled(true)
				.setOAuthConsumerKey(consumerKey)
				.setOAuthConsumerSecret(consumerSecret)
				.setOAuthAccessToken(accessToken)
				.setOAuthAccessTokenSecret(accessTokenSecret);
			
			TwitterStreamListener listener = new TwitterStreamListener();
			listener.setScreenName(screenName);
			listener.connectDatabase();
			
			log.info("Starting stream listener for @"+screenName+ " ...");
			TwitterStream twitterStream = new TwitterStreamFactory(cb.build()).getInstance();
	        twitterStream.addListener(listener);
	        twitterStream.user();			
		} else {
			log.error("Configuration incomplete, aborted.");
		}
	}
	
	public static String join
	  (AbstractCollection<String> s, String delimiter)
	  {
	    if (s == null || s.isEmpty()) return "";
	    Iterator<String> iter = s.iterator();
	    StringBuilder builder = new StringBuilder(iter.next());
	    while( iter.hasNext() ) {
	      builder.append(delimiter).append(iter.next());
	    }
	    return builder.toString();
	  }
}
