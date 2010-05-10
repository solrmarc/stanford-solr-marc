#! /bin/bash
# start_solr.sh
#  Start up jetty to run solr 
#    with enough memory and correct data directory
#  Naomi Dushay 2009-03-29

BLACKLIGHT_HOMEDIR=/home/blacklight
SOLR_DATA_DIR=/data/solr

JAVA_HOME=/usr/lib/jvm/java

#   see memorymanagement_whitepaper.pdf  from Sun
# gc general options of interest:  -XX:NewRatio=n
# gc algorithm options
#   parallel gc options:  –XX:ParallelGCThreads=n
#   parallel gc algorithms:   -XX:+UseParallelGC   and maybe   -XX:+UseParallelOldGC
#   another parallel gc algorithm:    -XX:+UseConcMarkSweepGC   and maybe   –XX:+CMSIncrementalMode  or  -Xincgc
#
#JAVA_OPTS="-server -Xmx12g -Xms12g -d64 -XX:+AggressiveOpts -XX:+UseParallelGC -XX:NewRatio=5"
#JAVA_OPTS="-server -Xmx12g -Xms12g -d64 -XX:+AggressiveOpts -XX:+UseConcMarkSweepGC -XX:NewRatio=3"
JAVA_OPTS="-server -Xmx15g -Xms15g -d64 -XX:+AggressiveOpts -XX:+UseConcMarkSweepGC -XX:ParallelGCThreads=4 -XX:NewRatio=3"

#LOG_OPTS=-Djava.util.logging.config.file=$BLACKLIGHT_HOMEDIR/jetty/solr/logging.properties

NOW=`eval date +"%F_%T"`

SYSMSG_FNAME=sysmsgs_started_$NOW.txt

#nohup $JAVA_HOME/bin/java $JAVA_OPTS $LOG_OPTS -Dsolr.data.dir=$SOLR_DATA_DIR -jar start.jar &>logs/$SYSMSG_FNAME
nohup $JAVA_HOME/bin/java $JAVA_OPTS -Dsolr.data.dir=$SOLR_DATA_DIR -jar start.jar &>logs/$SYSMSG_FNAME
