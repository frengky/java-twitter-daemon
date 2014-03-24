#!/bin/sh

##
## Configure application directories
##
APP_HOME=$(pwd)
APP_CONFIG=$APP_HOME/config
APP_CONFIG_DIR=$APP_HOME/config.d
##
## Configure Java environment
##
JAVA_HOME=/home/frengky/App/java-6-oracle-x86
JAVA=$JAVA_HOME/bin/java
CLASSPATH=$APP_HOME/lib/'*':$APP_HOME/bin/twitter-daemon.jar
export JAVA_HOME=$JAVA_HOME

##
## Run the application
##
cd $APP_HOME

$JAVA \
    -Xms32m -Xmx128m \
    -Dconfigdir=$APP_CONFIG_DIR \
    -Dlog4j.configuration=file://$APP_CONFIG/log4j.properties \
    -Ddbconfig=$APP_CONFIG/mysql.properties \
    -Djava.net.preferIPv4Stack=true \
    -Dfile.encoding=US-ASCII \
    -classpath $CLASSPATH \
    com.frengky.twitter.Twitter
