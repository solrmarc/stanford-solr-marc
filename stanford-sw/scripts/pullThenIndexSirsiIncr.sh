#! /bin/bash 
# pullThenIndexSirsiIncr.sh
# Pull over the latest incremental update files from Sirsi, then
#  Remove deleted records (per file of ids) from index and update index (with marc records in file)
#
# updated for Naomi's FORK of solrmarc 2011-01-23
# Naomi Dushay 2010-04-09

REMOTE_DATA_DIR=/s/Dataload/SearchworkIncrement/Output

LOCAL_DATA_DIR=/data/sirsi
LATEST_DATA_DIR=$LOCAL_DATA_DIR/latest/updates

# get filename date, either from command line or default to today's date
if [ $1 ] ; then
  COUNTS_FNAME=$1"_dates_counts"
  DEL_KEYS_FNAME=$1"_ckeys_delete.del"
  RECORDS_FNAME=$1"_uni_increment.marc"
else
  TODAY=`eval date +%y%m%d`
  COUNTS_FNAME=$TODAY"_dates_counts"
  DEL_KEYS_FNAME=$TODAY"_ckeys_delete.del"
  RECORDS_FNAME=$TODAY"_uni_increment.marc"
fi

#  sftp remote files with today's datestamp to "latest/updates"

sftp -o 'IdentityFile=~/.ssh/id_rsa' apache@jenson:$REMOTE_DATA_DIR/$COUNTS_FNAME $LOCAL_DATA_DIR
sftp -o 'IdentityFile=~/.ssh/id_rsa' apache@jenson:$REMOTE_DATA_DIR/$DEL_KEYS_FNAME $LATEST_DATA_DIR/
sftp -o 'IdentityFile=~/.ssh/id_rsa' apache@jenson:$REMOTE_DATA_DIR/$RECORDS_FNAME $LATEST_DATA_DIR/


#########
#  make the changes to the Solr index
#########

JAVA_HOME=/usr/lib/jvm/java

# set up the classpath
HOMEDIR=/home/blacklight
SOLRMARC_BASEDIR=$HOMEDIR/solrmarc-sw
DIST_DIR=$SOLRMARC_BASEDIR/dist
SITE_JAR=$DIST_DIR/StanfordSearchWorksSolrMarc.jar
CP=$SITE_JAR:$DIST_DIR:$DIST_DIR/lib

# create log directory
LOG_DIR=$LATEST_DATA_DIR/logs
mkdir -p $LOG_DIR
LOG_FILE=$LOG_DIR/$RECORDS_FNAME".txt"

REC_FNAME=$LATEST_DATA_DIR/$RECORDS_FNAME
DEL_ARG="-Dmarc.ids_to_delete="$LATEST_DATA_DIR/$DEL_KEYS_FNAME

# index the files
nohup java -Xmx4g -Xms4g $DEL_ARG -Dsolr.commit_at_end="true" -cp $CP -jar $SITE_JAR $REC_FNAME &>$LOG_FILE
# email the results
mail -s 'pullThenIndexSirsiIncr.sh output' searchworks-reports@lists.stanford.edu, datacontrol@stanford.edu < $LOG_FILE
# email the solr log messages 
$SOLRMARC_BASEDIR/stanford-sw/scripts/grep_and_email_tomcat_log.sh

# include latest course reserves data
JRUBY_OPTS="--1.9"
export JRUBY_OPTS
(source /usr/local/rvm/scripts/rvm && cd /home/blacklight/crez-sw-ingest && source ./.rvmrc && ./bin/index_latest_no_email.sh -s prod)

exit 0
