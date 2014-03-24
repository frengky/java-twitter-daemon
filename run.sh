#!/bin/sh
. $(dirname $0)/env.sh

CLASSPATH=$CLASSPATH:$APP_HOME/bin/twitter-daemon.jar

$JAVA \
    -Xms32m -Xmx128m \
    -Dconfigdir=$APP_HOME/config.d \
    -Dlog4j.configuration=file://$APP_HOME/config/log4j.properties \
    -Ddbconfig=$APP_HOME/config/mysql.properties \
    -Djava.net.preferIPv4Stack=true \
    -Dfile.encoding=US-ASCII \
    -classpath $CLASSPATH \
    com.frengky.twitter.Twitter

#    -Dtwitter4j.debug=true \
