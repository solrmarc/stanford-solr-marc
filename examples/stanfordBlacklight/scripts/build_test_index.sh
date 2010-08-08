#! /bin/bash
# build_test_index.sh
# Import a single marc file into a Solr index  (Stanford Blacklight)
#  Naomi Dushay 2008-10-12

BLACKLIGHT_HOMEDIR=/home/blacklight
SOLRMARC_BASEDIR=$BLACKLIGHT_HOMEDIR/solrmarc

RAW_DATA_DIR=$SOLRMARC_BASEDIR/examples/stanfordBlacklight/test/data

JAVA_HOME=/usr/lib/jvm/java

# TODO: determine today's date and create log dir with today's date, with a suffix if necessary

# temporary! - take an argument for the name of the log subdirectory
LOG_SUBDIR=$1

# create new dist files
rm -rf $SOLRMARC_BASEDIR/local_build
ant -buildfile $SOLRMARC_BASEDIR/build.xml -Dexample.configuration=stanfordBlacklight -Dinput.continue.processing.with.not.uptodate=c dist

# set up the classpath
DIST_DIR=$SOLRMARC_BASEDIR/dist
SITE_JAR=$DIST_DIR/StanfordIndexer.jar
CORE_JAR=$DIST_DIR/SolrMarc.jar
CP=$SITE_JAR:$CORE_JAR:$DIST_DIR

# get index directory ready 
SOLR_DATA_DIR=$SOLRMARC_BASEDIR/local_build/test/solr/data
mv $SOLR_DATA_DIR/index $SOLR_DATA_DIR/index_b4_$LOG_SUBDIR
# no longer using spellcheck as of 2010-01 (?)
#mv $SOLR_DATA_DIR/spellchecker $SOLR_DATA_DIR/spellchecker_b4_$LOG_SUBDIR
#mv $SOLR_DATA_DIR/spellcheckerFile $SOLR_DATA_DIR/spellcheckerFile_b4_$LOG_SUBDIR
#mv $SOLR_DATA_DIR/spellcheckerJaroWin $SOLR_DATA_DIR/spellcheckerJaroWin_b4_$LOG_SUBDIR

# create log directory
LOG_PARENT_DIR=$SOLRMARC_BASEDIR/local_build/test/solr/logs
LOG_DIR=$LOG_PARENT_DIR/$LOG_SUBDIR
mkdir -p $LOG_DIR

# index the file
#java -Xmx1g -Xms1g -Dsolr.data.dir=$SOLR_DATA_DIR -cp $CP -jar $CORE_JAR $RAW_DATA_DIR/physicalTests.mrc &>$LOG_DIR/log.txt
java -Xmx1g -Xms1g -Dsolr.data.dir=$SOLR_DATA_DIR -cp $CP -jar $CORE_JAR $RAW_DATA_DIR/formatTests.mrc &>$LOG_DIR/log.txt
#java -Xmx1g -Xms1g -Dsolr.data.dir=$SOLR_DATA_DIR -cp $CP -Dsolr.optimize_at_end="true" -jar $CORE_JAR $RAW_DATA_DIR/physicalTests.mrc &>$LOG_DIR/log.txt

exit 0
