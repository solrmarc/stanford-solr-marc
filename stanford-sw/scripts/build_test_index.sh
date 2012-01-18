#! /bin/bash
# build_test_index.sh
# Import a single marc file into a test Solr index  (Stanford SearchWorks)
#  updated 2012-01-16
#  Naomi Dushay 2008-10-12

# temporary! - take an argument for the name of the log subdirectory
LOG_SUBDIR=$1

HOMEDIR=/Users/ndushay/searchworks
SOLRMARC_BASEDIR=$HOMEDIR/solrmarc-sw

RAW_DATA_DIR=$SOLRMARC_BASEDIR/stanford-sw/test/data

JAVA_HOME=/usr/lib/jvm/java

# create new dist files
#ant -buildfile $SOLRMARC_BASEDIR/build.xml dist_site

# set up the classpath
DIST_DIR=$SOLRMARC_BASEDIR/dist
SITE_JAR=$DIST_DIR/StanfordSearchWorksSolrMarc.jar
#CORE_JAR=$DIST_DIR/SolrMarc.jar
CP=$SITE_JAR:$DIST_DIR:$DIST_DIR/lib

# create log directory
LOG_PARENT_DIR=$SOLRMARC_BASEDIR/test/logs
LOG_DIR=$LOG_PARENT_DIR/$LOG_SUBDIR
mkdir -p $LOG_DIR

# index the file
java -Xmx1g -Xms1g  -cp $CP -jar $SITE_JAR $RAW_DATA_DIR/physicalTests.mrc &>$LOG_DIR/log.txt
#java -Xmx1g -Xms1g -Dsolr.data.dir=$SOLR_DATA_DIR -cp $CP -Dsolr.optimize_at_end="true" -jar $CORE_JAR $RAW_DATA_DIR/physicalTests.mrc &>$LOG_DIR/log.txt

exit 0
