package com.frengky.twitter;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.AbstractCollection;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.Locale;
import java.util.Properties;

import javax.sql.DataSource;

import twitter4j.DirectMessage;
import twitter4j.StallWarning;
import twitter4j.Status;
import twitter4j.StatusDeletionNotice;
import twitter4j.TwitterStream;
import twitter4j.TwitterStreamFactory;
import twitter4j.User;
import twitter4j.UserList;
import twitter4j.UserMentionEntity;
import twitter4j.UserStreamListener;
import twitter4j.conf.ConfigurationBuilder;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TwitterClient {
	private static Logger log = LoggerFactory.getLogger(TwitterClient.class);
	private static Logger tweetLog = LoggerFactory.getLogger(Tweet.class);
	private DataSource dataSource = null;
	
	public TwitterClient() {
		log.info("Twitter Daemon v1.1 (frengky.lim@gmail.com)");
		
		String configFile = System.getProperty("dbconfig");
		HikariConfig config = new HikariConfig(configFile);
		
		config.setMaximumPoolSize(20);		
		config.setConnectionTestQuery("SELECT 1");
		
		dataSource = new HikariDataSource(config);		
	}
	
	public void dispose() {
		((HikariDataSource)dataSource).shutdown();
	}
	
	public void dispatch(final String configFile) {
		(new Thread(new Runnable() {
			public void run() {

				String screenName = null;
				String consumerKey = null;
				String consumerSecret = null;
				String accessToken = null;
				String accessTokenSecret = null;
				
				try {
					Properties prop = new Properties();
					prop.load(new FileInputStream(configFile));
					
					screenName = prop.getProperty("twitter.screenName").trim();
					consumerKey = prop.getProperty("twitter.oauth.consumerKey").trim();
					consumerSecret = prop.getProperty("twitter.oauth.consumerSecret").trim();
					accessToken = prop.getProperty("twitter.oauth.accessToken").trim();
					accessTokenSecret = prop.getProperty("twitter.oauth.accessTokenSecret").trim();
				} catch(FileNotFoundException e) {
					log.error("Configuration file not found: " + e.getMessage());
				} catch(IOException e) {
					log.error(e.getMessage());
				} catch(Exception e) {
					log.error(e.getMessage());
				}
				
				if(accessToken == null || accessToken.equals("")) {
					log.error("Configuration file incomplete, cannot continue");
					return;
				}
				
				log.info("Starting twitter client @" + screenName);
				
				ConfigurationBuilder cb = new ConfigurationBuilder();
				cb.setDebugEnabled(true)
					.setOAuthConsumerKey(consumerKey)
					.setOAuthConsumerSecret(consumerSecret)
					.setOAuthAccessToken(accessToken)
					.setOAuthAccessTokenSecret(accessTokenSecret);
				
				final TwitterStream twitterStream = new TwitterStreamFactory(cb.build()).getInstance();
				twitterStream.addListener(new TwitterStreamListener(screenName));
				
				Runtime.getRuntime().addShutdownHook(new Thread() {
					public void run() {
						twitterStream.clearListeners();
						twitterStream.shutdown();
					}
				});
				
		        twitterStream.user();
		        
			}
		})).start();
	}
	
	public String join(AbstractCollection<String> s, String delimiter) {
	    if (s == null || s.isEmpty()) return "";
	    Iterator<String> iter = s.iterator();
	    StringBuilder builder = new StringBuilder(iter.next());
	    while( iter.hasNext() ) {
	      builder.append(delimiter).append(iter.next());
	    }
	    return builder.toString();
	 }	    
	
	private class Tweet {
	}
	
	private class TwitterStreamListener implements UserStreamListener {
		private String screenName;
		private String[] radio = new String[] {
			"101Jakfm",
			"987Genfm",
			"Prambors",
			"ardanradio",
			"GajahmadaFM",
			"GeronimoFM",
			"solo_radio",
			"istarafm",
			"ElfaraFM",
			"KissFmMedan",
			"MomeafmPLM",
			"FlamboyantFM",
			"radio_nuansa",
			"Persada924FM",
			"JRadio917FM",
			"radiovenusmks",
			"CDBSFMBALI",
			"ClassyFM",
			"kita876fm",
			"trendyfm",
			"onix887fm"
		};
		
		public TwitterStreamListener(String screenName) {
			this.screenName = screenName;
		}	
		
		private boolean isMyScreenName(String sName) {
			if(screenName.toLowerCase(Locale.ENGLISH).equals(sName.toLowerCase(Locale.ENGLISH))) {
				return true;
			}
			return false;
		}
		
		private boolean isRadioScreenName(String sName) {
			boolean found = false;
			for(String radioScreenName : radio) {
				if(radioScreenName.toLowerCase(Locale.ENGLISH).equals(sName.toLowerCase(Locale.ENGLISH))) {
					found = true;
				}
			}
			return found;
		}
		
		private void insertToDb(long user_id, String user_name, String screen_name, String profile_image, String radio_name, String mention, String tweet, Date tweeted) {
			Connection conn = null;
			PreparedStatement stmt = null;
			
			try {
				conn = dataSource.getConnection();
				
				StringBuilder sql = new StringBuilder();
				sql.append("INSERT INTO feeds");
				sql.append("(user_id, user_name, screen_name, mention, tweet, timeline, radio_name, profile_image, published, deleted, tweeted, created, modified)");
				sql.append(" VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?)");
				
				SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				Date created = new Date();
				
				stmt = conn.prepareStatement(sql.toString());
				stmt.setLong(1, user_id); // user_id
				stmt.setString(2, user_name); // user_name
				stmt.setString(3, screen_name); // screen_name
				stmt.setString(4, mention); // mention
				stmt.setString(5, tweet); // tweet
				stmt.setString(6, screenName); // timeline
				stmt.setString(7, radio_name); // published
				stmt.setString(8, profile_image); // profile_image
				stmt.setString(9, "Yes"); // published
				stmt.setInt(10, 0); // deleted
				stmt.setString(11, dateFormat.format(tweeted)); //tweeted
				stmt.setString(12, dateFormat.format(created)); // created
				stmt.setString(13, dateFormat.format(created)); // modified

				int affected = stmt.executeUpdate();
				
				log.info("@" + screenName + ": MYSQL INSERT: " + affected + " affected row(s)");
			} catch(SQLException e) {
				log.error("@" + screenName + ": SQLEXCEPTION: " + e.getMessage());
			} catch(Exception e) {
				log.error("@" + screenName + ": EXCEPTION: " + e.getMessage());
			} finally {
				try { if(stmt != null) { stmt.close(); } } catch(SQLException e) {}
				try { if(conn != null) { conn.close(); } } catch(SQLException e) {}
			}
		}
		
	    @Override
	    public void onStatus(Status status) {
	    	User user = status.getUser();

	    	boolean isMentioned = false;
	    	boolean isRadioMentioned = false;
	    	String radioName = "";
	    	String mentionList = "";
	    	
	    	log.info("@"+screenName+": TWEET TEXT " + status.getText());
	    	log.info("@"+screenName+":       FROM @" + user.getScreenName() + " [name:" + user.getName() + "] [id:" + user.getId() + "] [rt:" + status.getRetweetCount() + "]");
	    	
	    	UserMentionEntity[] entities = status.getUserMentionEntities();
	    	if(entities.length > 0) {
	        	ArrayList<String> mentions = new ArrayList<String>();
	        	for(UserMentionEntity entity : entities) {
	        		if(isMyScreenName(entity.getScreenName())) {
	        			isMentioned = true;
	        		}
	        		if(isRadioScreenName(entity.getScreenName())) {
	        			radioName = entity.getScreenName();
	        			isRadioMentioned = true;
	        		}
	    			mentions.add("@" + entity.getScreenName());
	    		}
	        	mentionList = join(mentions, " ");
	        	log.info("@"+screenName+":       MENT " + mentionList);
	    	}
	    	
	    	StringBuilder logStr = new StringBuilder();
	    	logStr.append("@"+screenName+":       ");
	    	if(isMentioned) {
	    		logStr.append("ME YES");
	    	} else {
	    		logStr.append("ME NO ");
	    	}
	    	if(isRadioMentioned) {
	    		logStr.append(",  RADIO YES");
	    	} else {
	    		logStr.append(",  RADIO NO ");
	    	}
	    	
	    	if(isMentioned == true && isRadioMentioned == true) {
	    		logStr.append(",  SAVE YES");
	    		log.info(logStr.toString());
	    		tweetLog.info("@"+ screenName + ": @" + user.getScreenName() + ": " + status.getText());
	    		insertToDb(user.getId(), user.getName(), user.getScreenName(), user.getOriginalProfileImageURL(), radioName, mentionList, status.getText(), status.getCreatedAt());
	    	} else {
	    		logStr.append(",  SAVE NO");
	    		log.info(logStr.toString());
	    	}
	    }
	    
	    @Override
	    public void onDeletionNotice(StatusDeletionNotice statusDeletionNotice) {
	    }

	    @Override
	    public void onDeletionNotice(long directMessageId, long userId) {
	    }

	    @Override
	    public void onTrackLimitationNotice(int numberOfLimitedStatuses) {
	    	log.warn("@"+screenName+": TRACK LIMIT " + numberOfLimitedStatuses);
	    }

	    @Override
	    public void onScrubGeo(long userId, long upToStatusId) {
	    }

	    @Override
	    public void onStallWarning(StallWarning warning) {
	    	log.warn("@"+screenName+": STALL " + warning.getPercentFull() + "% (" + warning.getMessage() + ")");
	    }

	    @Override
	    public void onFriendList(long[] friendIds) {
	    }

	    @Override
	    public void onFavorite(User source, User target, Status favoritedStatus) {
	    }

	    @Override
	    public void onUnfavorite(User source, User target, Status unfavoritedStatus) {
	    }

	    @Override
	    public void onFollow(User source, User followedUser) {
	    }

	    @Override
	    public void onUnfollow(User source, User followedUser) {
	    }

	    @Override
	    public void onDirectMessage(DirectMessage directMessage) {
	    }

	    @Override
	    public void onUserListMemberAddition(User addedMember, User listOwner, UserList list) {
	    }

	    @Override
	    public void onUserListMemberDeletion(User deletedMember, User listOwner, UserList list) {
	    }

	    @Override
	    public void onUserListSubscription(User subscriber, User listOwner, UserList list) {
	    }

	    @Override
	    public void onUserListUnsubscription(User subscriber, User listOwner, UserList list) {
	    }

	    @Override
	    public void onUserListCreation(User listOwner, UserList list) {
	    }

	    @Override
	    public void onUserListUpdate(User listOwner, UserList list) {
	    }

	    @Override
	    public void onUserListDeletion(User listOwner, UserList list) {
	    }

	    @Override
	    public void onUserProfileUpdate(User updatedUser) {
	    }

	    @Override
	    public void onBlock(User source, User blockedUser) {
	    }

	    @Override
	    public void onUnblock(User source, User unblockedUser) {
	    }

	    @Override
	    public void onException(Exception ex) {
	        // ex.printStackTrace();
	        log.warn("@"+screenName+": EXCEPTION "  + ex.getMessage());
	    }	    
	}
}