#! /bin/bash
# pullThenIndexSirsiIncr.sh
# Pull over the latest incremental update files from Sirsi, then do an
#  Remove deleted records from index and update index per marc records given
#  Naomi Dushay 2010-04-09

REMOTE_DATA_DIR=/s/Dataload/SearchworkIncrement/Output

LOCAL_DATA_DIR=/data/sirsi
LATEST_DATA_DIR=$LOCAL_DATA_DIR/latest/updates

# get filename date
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
SOLR_DATA_DIR=/data/solr/dataBuild
BLACKLIGHT_HOMEDIR=/home/blacklight
SOLRMARC_JAR=$BLACKLIGHT_HOMEDIR/solrmarc/dist/swSolrMarc.jar

REC_FNAME=$LATEST_DATA_DIR/$RECORDS_FNAME

DEL_ARG="-Dmarc.ids_to_delete="$LATEST_DATA_DIR/$DEL_KEYS_FNAME

# create log directory
LOG_DIR=$LATEST_DATA_DIR/logs
mkdir -p $LOG_DIR

# index the files
nohup java -Xmx4g -Xms4g -Dsolr.data.dir=$SOLR_DATA_DIR $DEL_ARG -Dsolr.optimize_at_end="true" -jar $SOLRMARC_JAR $REC_FNAME &>$LOG_DIR/$RECORDS_FNAME".txt"

exit 0
