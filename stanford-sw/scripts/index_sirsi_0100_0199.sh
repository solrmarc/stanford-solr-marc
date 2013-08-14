#! /bin/bash
# index_all_sirsi.sh
# Import all marc files from sirsi full dump into a Solr index  (SearchWorks flavor)
#
# updated for Naomi's FORK of solrmarc 2011-01-23
# Naomi Dushay 2008-10-12

# take an argument for the name of the log subdirectory
LOG_SUBDIR=$1

HOMEDIR=/home/blacklight
SOLRMARC_BASEDIR=$HOMEDIR/solrmarc-sw

RAW_DATA_DIR=/data/sirsi/latest

JAVA_HOME=/usr/lib/jvm/java

# set up the classpath
DIST_DIR=$SOLRMARC_BASEDIR/dist
SITE_JAR=$DIST_DIR/StanfordSearchWorksSolrMarc.jar
CP=$SITE_JAR:$DIST_DIR:$DIST_DIR/lib

# create log directory
LOG_PARENT_DIR=$RAW_DATA_DIR/logs
LOG_DIR=$LOG_PARENT_DIR/$LOG_SUBDIR
mkdir -p $LOG_DIR

# index the files
nohup java -Xmx1g -Xms256m -cp $CP -jar $SITE_JAR $RAW_DATA_DIR/uni_01000000_01499999.marc &>$LOG_DIR/log0100-0149.txt
nohup java -Xmx1g -Xms256m -cp $CP -jar $SITE_JAR $RAW_DATA_DIR/uni_01500000_01999999.marc &>$LOG_DIR/log0150-0199.txt
#nohup java -Xmx1g -Xms256m -Dsolr.commit_at_end="true" -cp $CP -jar $SITE_JAR $RAW_DATA_DIR/uni_10000000_10499999.marc &>$LOG_DIR/log1000-1049.txt

exit 0
