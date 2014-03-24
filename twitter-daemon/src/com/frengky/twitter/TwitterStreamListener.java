package com.frengky.twitter;
import java.io.FileInputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Properties;
import org.apache.log4j.Logger;
import twitter4j.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;

public class TwitterStreamListener implements UserStreamListener {
	private static Logger log = Logger.getLogger(TwitterStreamListener.class);
	private Connection conn;
	private String dbTable;
	private String myScreenName;
	
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
			connString.append("?");
			connString.append("user=" + prop.getProperty("mysql.user"));
			connString.append("&");
			connString.append("password=" + prop.getProperty("mysql.password"));
			dbTable = prop.getProperty("mysql.table");
			
			Class.forName("com.mysql.jdbc.Driver").newInstance();
			conn = DriverManager.getConnection(connString.toString());
			
			log.info("@" + myScreenName + ": MYSQL " + connString.toString());
		} catch(Exception e) {
			log.error("@" + myScreenName + ": MYSQL EXCEPTION Error building database connection: " + e.toString());
		}
	}
	
	public void insertToDb(long user_id, String user_name, String screen_name, int rt_count, String tweet, Date tweeted) {
		PreparedStatement stmt = null;
		
		try {
			StringBuilder sql = new StringBuilder();
			sql.append("INSERT INTO ");
			sql.append(dbTable);
			sql.append("(user_id, user_name, screen_name, rt_count, tweet, tweeted, app_user, created)");
			sql.append(" VALUES(?,?,?,?,?,?,?,?)");
			
			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			Date created = new Date();
			
			stmt = conn.prepareStatement(sql.toString());
			stmt.setInt(1, (int)user_id);
			stmt.setString(2, user_name);
			stmt.setString(3, screen_name);
			stmt.setInt(4,  rt_count);
			stmt.setString(5, tweet);
			stmt.setString(6, dateFormat.format(tweeted));
			stmt.setString(7, myScreenName);
			stmt.setString(8, dateFormat.format(created));

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
    	
    	log.info("@"+myScreenName+": TWEET RECV [screenName:@"+screenName+"] [name:" + name + "] [id:" + userId + "]");
    	log.info("@"+myScreenName+":       TEXT " + statusText);
    	
    	status.getUserMentionEntities();
    	UserMentionEntity[] entities = status.getUserMentionEntities();        	
    	if(entities.length > 0) {
        	ArrayList<String> mentions = new ArrayList<String>();
        	for(UserMentionEntity entity : entities) {
        		if(entity.getScreenName() == myScreenName) {
        			isMentioned = true;
        		}
    			mentions.add("@" + entity.getScreenName());
    		}
        	log.info("@"+myScreenName+":       MENT " + TwitterUtil.join(mentions, ","));
    	}
    	
    	log.info("@"+myScreenName+":       RETW " + rtCount + " time(s)");
    	
    	if(isMentioned == true) {
    		log.info("@"+myScreenName+": TWEET RECV OK");
    		insertToDb(userId, name, screenName, rtCount, statusText, status.getCreatedAt());
    	} else {
    		log.info("@"+myScreenName+": TWEET RECV SKIP");
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
