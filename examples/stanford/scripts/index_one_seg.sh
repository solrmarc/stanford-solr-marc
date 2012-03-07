#! /bin/bash
# index_incr_sirsi.sh
# Remove deleted records from index and update index per marc records given
#  Naomi Dushay 2010-01-08

#SOLR_DATA_DIR=/data/solr/dataBuild
SOLR_DATA_DIR=/data/solr
RAW_DATA_DIR=/data/sirsi/latest/updates

DEL_ARG="-Dmarc.ids_to_delete="$RAW_DATA_DIR/$DEL_KEYS_FNAME

JAVA_HOME=/usr/lib/jvm/java

# set up the classpath
BLACKLIGHT_HOMEDIR=/home/blacklight
SOLRMARC_BASEDIR=$BLACKLIGHT_HOMEDIR/solrmarc-sw
DIST_DIR=$SOLRMARC_BASEDIR/dist
SITE_JAR=$DIST_DIR/StanfordIndexer.jar
CORE_JAR=$DIST_DIR/SolrMarc.jar
CP=$SITE_JAR:$CORE_JAR:$DIST_DIR

# create log directory
LOG_PARENT_DIR=$RAW_DATA_DIR/logs
LOG_DIR=$LOG_PARENT_DIR
mkdir -p $LOG_DIR

# index the files
#nohup java -Xmx4g -Xms4g -Dsolr.data.dir=$SOLR_DATA_DIR $DEL_ARG -cp $CP -jar $CORE_JAR $REC_FNAME &>$LOG_DIR/$RECORDS_FNAME".txt"

nohup java -Xmx12g -Xms12g -Dsolr.data.dir=$SOLR_DATA_DIR -cp $CP -jar $CORE_JAR $RAW_DATA_DIR/uni_04500000_04999999.marc &>$LOG_DIR/111111_uni_04500000_04999999.txt


exit 0
