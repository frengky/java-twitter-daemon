package com.frengky.twitter;

import java.io.FileInputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.Properties;

import org.apache.log4j.Logger;

import twitter4j.*;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;

public class TwitterStreamListener implements UserStreamListener {
	private static Logger log = Logger.getLogger(TwitterStreamListener.class);
	private static Logger tweetLog = Logger.getLogger(Tweet.class);
	
	private Connection conn;
	private String dbTable;
	private String myScreenName;
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
	
	public TwitterStreamListener() {
		log.info("Filters: " + radio.length + " screen name(s)");
	}
	
	public void setScreenName(String screenName) {
		myScreenName = screenName;
	}
	
	public void connectDatabase() {
		StringBuilder connString = new StringBuilder();
		connString.append("jdbc:mysql://");
		
		String myDbConfig = System.getProperty("dbconfig");
		log.info("@" + myScreenName + ": MYSQL " + myDbConfig);
		try {
			Properties prop = new Properties();
			prop.load(new FileInputStream(myDbConfig));
			connString.append(prop.getProperty("mysql.host"));
			connString.append("/");
			connString.append(prop.getProperty("mysql.database"));
			connString.append("?useUnicode=true&characterEncoding=UTF-8");
			dbTable = prop.getProperty("mysql.table");
			
			Class.forName("com.mysql.jdbc.Driver").newInstance();
			conn = DriverManager.getConnection(connString.toString(), prop.getProperty("mysql.user"), prop.getProperty("mysql.password"));
			
			log.info("@" + myScreenName + ": MYSQL " + connString.toString());
		} catch(Exception e) {
			log.error("@" + myScreenName + ": MYSQL EXCEPTION Error building database connection: " + e.toString());
		}
	}
	
	private boolean isMyScreenName(String screenName) {
		if(myScreenName.toLowerCase(Locale.ENGLISH).equals(screenName.toLowerCase(Locale.ENGLISH))) {
			return true;
		}
		return false;
	}
	
	private boolean isRadioScreenName(String screenName) {
		boolean found = false;
		for(String radioScreenName : radio) {
			if(radioScreenName.toLowerCase(Locale.ENGLISH).equals(screenName.toLowerCase(Locale.ENGLISH))) {
				found = true;
			}
		}
		return found;
	}
	
	public void insertToDb(long user_id, String user_name, String screen_name, String profile_image, String radio_name, String mention, String tweet, Date tweeted) {
		PreparedStatement stmt = null;
		try {
			StringBuilder sql = new StringBuilder();
			sql.append("INSERT INTO ");
			sql.append(dbTable);
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
			stmt.setString(6, myScreenName); // timeline
			stmt.setString(7, radio_name); // published
			stmt.setString(8, profile_image); // profile_image
			stmt.setString(9, "Yes"); // published
			stmt.setInt(10, 0); // deleted
			stmt.setString(11, dateFormat.format(tweeted)); //tweeted
			stmt.setString(12, dateFormat.format(created)); // created
			stmt.setString(13, dateFormat.format(created)); // modified

			int affected = stmt.executeUpdate();
			
			log.info("@" + myScreenName + ": MYSQL INSERT: " + affected + " affected row(s)");
			if(!stmt.isClosed()) {
				stmt.close();
			}
		} catch(Exception e) {
			log.error("@" + myScreenName + ": MYSQL EXCEPTION: " + e.getMessage());
		} finally {
			if(stmt != null) {
				stmt = null;
			}
		}
	}
	
    @Override
    public void onStatus(Status status) {
    	User user = status.getUser();
    	
    	String name = user.getName();
    	String screenName = user.getScreenName();
    	long userId = user.getId();
    	String statusText = status.getText();
    	int rtCount = status.getRetweetCount();
    	boolean isMentioned = false;
    	boolean isRadioMentioned = false;
    	String radioName = "";
    	String mentionList = "";
    	String profileImage = user.getOriginalProfileImageURL();
    	
    	log.info("@"+myScreenName+": TWEET TEXT " + statusText);
    	log.info("@"+myScreenName+":       FROM @"+screenName+" [name:" + name + "] [id:" + userId + "] [rt:" + rtCount + "]");
    	
    	status.getUserMentionEntities();
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
        	mentionList = TwitterUtil.join(mentions, " ");
        	log.info("@"+myScreenName+":       MENT " + mentionList);
    	}
    	
    	StringBuilder logStr = new StringBuilder();
    	logStr.append("@"+myScreenName+":       ");
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
    		tweetLog.info("@"+ myScreenName + ": @" + screenName + ": " + statusText);
    		insertToDb(userId, name, screenName, profileImage, radioName, mentionList, statusText, status.getCreatedAt());
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
    	log.warn("@"+myScreenName+": TRACK LIMIT " + numberOfLimitedStatuses);
    }

    @Override
    public void onScrubGeo(long userId, long upToStatusId) {
    }

    @Override
    public void onStallWarning(StallWarning warning) {
    	log.warn("@"+myScreenName+": STALL " + warning.getPercentFull() + "% (" + warning.getMessage() + ")");
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
        log.warn("@"+myScreenName+": EXCEPTION "  + ex.getMessage());
    }
}
