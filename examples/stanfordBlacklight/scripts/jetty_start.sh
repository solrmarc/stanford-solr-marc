#! /bin/bash
# start_solr.sh
#  Start up jetty to run solr 
#    with enough memory and correct data directory
#  Naomi Dushay 2009-03-29

BLACKLIGHT_HOMEDIR=/home/blacklight
SOLR_DATA_DIR=/data/solr

JAVA_HOME=/usr/lib/jvm/java

JAVA_OPTS="-server -Xmx12g -Xms12g -d64 -XX:+UseParallelGC -XX:+AggressiveOpts -XX:NewRatio=5"

TODAY=`eval date +%F`
HOUR=`eval date +%T`

#LOG_OPTS=-Djava.util.logging.config.file=$BLACKLIGHT_HOMEDIR/jetty/solr/logging.properties

#nohup $JAVA_HOME/bin/java $JAVA_OPTS $LOG_OPTS -Dsolr.data.dir=$SOLR_DATA_DIR -jar start.jar &>logs/sysmsgs_started_$TODAY_$HOUR.txt
nohup $JAVA_HOME/bin/java $JAVA_OPTS -Dsolr.data.dir=$SOLR_DATA_DIR -jar start.jar &>logs/sysmsgs_started_$TODAY_$HOUR.txt
