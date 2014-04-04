#!/bin/sh

##
## Configure application directories
##
APP_HOME=$(pwd)
APP_JAR=$APP_HOME/bin/twitter-daemon.jar
APP_CONFIG=$APP_HOME/config
APP_CONFIG_DIR=$APP_HOME/config.d
APP_CONFIG_LOG4J=$APP_CONFIG/log4j.properties

##
## Configure Java environment
##
export JAVA_HOME=/home/frengky/App/java-7-oracle-x86_64

##
## Run the application
##
cd $APP_HOME

$JAVA_HOME/bin/java \
    -Xms32m -Xmx128m \
    -Dlog4j.configuration=file://$APP_CONFIG_LOG4J \
    -Djava.net.preferIPv4Stack=true \
    -Dfile.encoding=UTF-8 \
    -Ddbconfig=$APP_CONFIG/mysql.properties \
    -Dconfigdir=$APP_CONFIG_DIR \
    -jar $APP_JAR
