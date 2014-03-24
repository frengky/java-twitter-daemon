package com.frengky.twitter;
import java.io.FileInputStream;
import java.util.Properties;
import org.apache.log4j.Logger;
import twitter4j.TwitterStream;
import twitter4j.TwitterStreamFactory;
import twitter4j.conf.ConfigurationBuilder;

public class TwitterClient implements Runnable {
	private static Logger log = Logger.getLogger(TwitterClient.class);
	private String screenName = null;
	private String configPath = null;
	private String consumerKey = null;
	private String consumerSecret = null;
	private String accessToken = null;
	private String accessTokenSecret = null;
	
	public TwitterClient(String config) {
		setConfigPath(config);
	}
	
	public void setConfigPath(String config) {
		configPath = config;
		
		try {
			Properties prop = new Properties();
			prop.load(new FileInputStream(configPath));
			
			screenName = prop.getProperty("twitter.screenName").trim();
			consumerKey = prop.getProperty("twitter.oauth.consumerKey").trim();
			consumerSecret = prop.getProperty("twitter.oauth.consumerSecret").trim();
			accessToken = prop.getProperty("twitter.oauth.accessToken").trim();
			accessTokenSecret = prop.getProperty("twitter.oauth.accessTokenSecret").trim();
			
			log.info("Initializing twitter client @" + screenName);
			log.info("@"+screenName+": CONFIG " + configPath);
			log.info("@"+screenName+":       twitter.screenName: " + screenName);
			log.info("@"+screenName+":       twitter.oauth.consumerKey: " + consumerKey);
			log.info("@"+screenName+":       twitter.oauth.consumerSecret: " + consumerSecret);
			log.info("@"+screenName+":       twitter.oauth.accessToken: " + accessToken);
			log.info("@"+screenName+":       twitter.oauth.accessTokenSecret: " + accessTokenSecret);
			
		} catch(Exception e) {
			log.error("@"+screenName+": EXCEPTION Error reading twitter configuration: " + e.toString());
		}		
	}
	
	public void run() {
		log.info("Starting twitter client @" + screenName);
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
			
			log.info("@"+screenName+": Attaching stream listener ...");
			TwitterStream twitterStream = new TwitterStreamFactory(cb.build()).getInstance();
	        twitterStream.addListener(listener);
	        twitterStream.user();			
		} else {
			log.error("@"+screenName+": EXCEPTION configuration incomplete, aborted.");
		}
	}
}
