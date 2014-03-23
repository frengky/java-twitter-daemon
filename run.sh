#!/bin/sh
. $(dirname $0)/env.sh

USER="$1"

if [ ! $USER ]; then
   echo "Usage: $0 <user>"
   exit 1
fi


CLASSPATH=$CLASSPATH:$APP_HOME/bin/twitter-daemon.jar

$JAVA \
    -Xms32m -Xmx128m \
    -Dconfig=$APP_HOME/config/$USER.twitter.properties \
    -Dlog4j.configuration=file://$APP_HOME/config/$USER.log.properties \
    -Ddbconfig=$APP_HOME/config/mysql.properties \
    -Djava.net.preferIPv4Stack=true \
    -Dfile.encoding=US-ASCII \
    -classpath $CLASSPATH \
    com.frengky.twitter.Twitter

#    -Dtwitter4j.debug=true \