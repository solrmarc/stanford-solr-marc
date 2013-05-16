#! /bin/bash
# index_all_sirsi.sh
# Import all marc files from sirsi full dump into a Solr index  (SearchWorks flavor)
#
# updated for Naomi's FORK of solrmarc 2011-01-23
# Naomi Dushay 2008-10-12

# temporary! - take an argument for the date of the log subdirectory
# TODO: determine today's date and create log dir with today's date, with a suffix if necessary
#echo "   Usage: `basename $0` log_subdir(yyyy-mm-dd)"
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
mkdir $LOG_DIR

# index the files
#nohup java -Xmx8g -Xms8g -cp $CP -jar $SITE_JAR $RAW_DATA_DIR/uni_00000000_00499999.marc &>$LOG_DIR/log0000-0049.txt
#nohup java -Xmx8g -Xms8g -cp $CP -jar $SITE_JAR $RAW_DATA_DIR/uni_00500000_00999999.marc &>$LOG_DIR/log0050-0099.txt
#nohup java -Xmx8g -Xms8g -cp $CP -jar $SITE_JAR $RAW_DATA_DIR/uni_01000000_01499999.marc &>$LOG_DIR/log0100-0149.txt
#nohup java -Xmx8g -Xms8g -cp $CP -jar $SITE_JAR $RAW_DATA_DIR/uni_01500000_01999999.marc &>$LOG_DIR/log0150-0199.txt
#nohup java -Xmx8g -Xms8g -cp $CP -jar $SITE_JAR $RAW_DATA_DIR/uni_02000000_02499999.marc &>$LOG_DIR/log0200-0249.txt
#nohup java -Xmx8g -Xms8g -cp $CP -jar $SITE_JAR $RAW_DATA_DIR/uni_02500000_02999999.marc &>$LOG_DIR/log0250-0299.txt
#nohup java -Xmx8g -Xms8g -cp $CP -jar $SITE_JAR $RAW_DATA_DIR/uni_03000000_03499999.marc &>$LOG_DIR/log0300-0349.txt
nohup java -Xmx8g -Xms8g -cp $CP -jar $SITE_JAR $RAW_DATA_DIR/uni_03500000_03999999.marc &>$LOG_DIR/log0350-0399.txt
nohup java -Xmx8g -Xms8g -cp $CP -jar $SITE_JAR $RAW_DATA_DIR/uni_04000000_04499999.marc &>$LOG_DIR/log0400-0449.txt
nohup java -Xmx8g -Xms8g -cp $CP -jar $SITE_JAR $RAW_DATA_DIR/uni_04500000_04999999.marc &>$LOG_DIR/log0450-0499.txt
nohup java -Xmx8g -Xms8g -cp $CP -jar $SITE_JAR $RAW_DATA_DIR/uni_05000000_05499999.marc &>$LOG_DIR/log0500-0549.txt
nohup java -Xmx8g -Xms8g -cp $CP -jar $SITE_JAR $RAW_DATA_DIR/uni_05500000_05999999.marc &>$LOG_DIR/log0550-0599.txt
nohup java -Xmx8g -Xms8g -cp $CP -jar $SITE_JAR $RAW_DATA_DIR/uni_06000000_06499999.marc &>$LOG_DIR/log0600-0649.txt
nohup java -Xmx8g -Xms8g -Dsolr.commit_at_end="true" -cp $CP -jar $SITE_JAR $RAW_DATA_DIR/uni_06500000_06999999.marc &>$LOG_DIR/log0650-0699.txt
#nohup java -Xmx8g -Xms8g -cp $CP -jar $SITE_JAR $RAW_DATA_DIR/uni_07000000_07499999.marc &>$LOG_DIR/log0700-0749.txt
#nohup java -Xmx8g -Xms8g -cp $CP -jar $SITE_JAR $RAW_DATA_DIR/uni_07500000_07999999.marc &>$LOG_DIR/log0750-0799.txt
#nohup java -Xmx8g -Xms8g -cp $CP -jar $SITE_JAR $RAW_DATA_DIR/uni_08000000_08499999.marc &>$LOG_DIR/log0800-0849.txt
#nohup java -Xmx8g -Xms8g -cp $CP -jar $SITE_JAR $RAW_DATA_DIR/uni_08500000_08999999.marc &>$LOG_DIR/log0850-0899.txt
#nohup java -Xmx8g -Xms8g -cp $CP -jar $SITE_JAR $RAW_DATA_DIR/uni_09000000_09499999.marc &>$LOG_DIR/log0900-0949.txt
#nohup java -Xmx8g -Xms8g -cp $CP -jar $SITE_JAR $RAW_DATA_DIR/uni_09500000_09999999.marc &>$LOG_DIR/log0950-0999.txt
#nohup java -Xmx8g -Xms8g -Dsolr.commit_at_end="true" -cp $CP -jar $SITE_JAR $RAW_DATA_DIR/uni_10000000_10499999.marc &>$LOG_DIR/log1000-1049.txt

exit 0
