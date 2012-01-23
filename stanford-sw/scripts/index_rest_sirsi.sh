#! /bin/bash
# index_rest_sirsi.sh
# Script to finish full reindex if we stopped processing for some reason
#  (all "gotcha" lines commented out)
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

# create fresh dist files
#ant -buildfile $SOLRMARC_BASEDIR/build.xml dist_site

# set up the classpath
DIST_DIR=$SOLRMARC_BASEDIR/dist
SITE_JAR=$DIST_DIR/StanfordSearchWorksSolrMarc.jar
CP=$SITE_JAR:$DIST_DIR:$DIST_DIR/lib

# create log directory
LOG_PARENT_DIR=$RAW_DATA_DIR/logs
LOG_DIR=$LOG_PARENT_DIR/$LOG_SUBDIR
mkdir $LOG_DIR

# index the files
#nohup java -Xmx16g -Xms16g -cp $CP -jar $SITE_JAR $RAW_DATA_DIR/uni_00000000_00499999.marc &>$LOG_DIR/log000-049.txt
#nohup java -Xmx16g -Xms16g -cp $CP -jar $SITE_JAR $RAW_DATA_DIR/uni_00500000_00999999.marc &>$LOG_DIR/log050-099.txt
#nohup java -Xmx16g -Xms16g -cp $CP -jar $SITE_JAR $RAW_DATA_DIR/uni_01000000_01499999.marc &>$LOG_DIR/log100-149.txt
#nohup java -Xmx16g -Xms16g -cp $CP -jar $SITE_JAR $RAW_DATA_DIR/uni_01500000_01999999.marc &>$LOG_DIR/log150-199.txt
#nohup java -Xmx16g -Xms16g -cp $CP -jar $SITE_JAR $RAW_DATA_DIR/uni_02000000_02499999.marc &>$LOG_DIR/log200-249.txt
#nohup java -Xmx16g -Xms16g -cp $CP -jar $SITE_JAR $RAW_DATA_DIR/uni_02500000_02999999.marc &>$LOG_DIR/log250-299.txt
#nohup java -Xmx16g -Xms16g -cp $CP -jar $SITE_JAR $RAW_DATA_DIR/uni_03000000_03499999.marc &>$LOG_DIR/log300-349.txt
#nohup java -Xmx16g -Xms16g -cp $CP -jar $SITE_JAR $RAW_DATA_DIR/uni_03500000_03999999.marc &>$LOG_DIR/log350-399.txt
#nohup java -Xmx16g -Xms16g -cp $CP -jar $SITE_JAR $RAW_DATA_DIR/uni_04000000_04499999.marc &>$LOG_DIR/log400-449.txt
#nohup java -Xmx16g -Xms16g -cp $CP -jar $SITE_JAR $RAW_DATA_DIR/uni_04500000_04999999.marc &>$LOG_DIR/log450-499.txt
#nohup java -Xmx16g -Xms16g -cp $CP -jar $SITE_JAR $RAW_DATA_DIR/uni_05000000_05499999.marc &>$LOG_DIR/log500-549.txt
#nohup java -Xmx16g -Xms16g -cp $CP -jar $SITE_JAR $RAW_DATA_DIR/uni_05500000_05999999.marc &>$LOG_DIR/log550-599.txt
#nohup java -Xmx16g -Xms16g -cp $CP -jar $SITE_JAR $RAW_DATA_DIR/uni_06000000_06499999.marc &>$LOG_DIR/log600-649.txt
#nohup java -Xmx16g -Xms16g -cp $CP -jar $SITE_JAR $RAW_DATA_DIR/uni_06500000_06999999.marc &>$LOG_DIR/log650-699.txt
#nohup java -Xmx16g -Xms16g -cp $CP -jar $SITE_JAR $RAW_DATA_DIR/uni_07000000_07499999.marc &>$LOG_DIR/log700-749.txt
#nohup java -Xmx16g -Xms16g -cp $CP -jar $SITE_JAR $RAW_DATA_DIR/uni_07500000_07999999.marc &>$LOG_DIR/log750-799.txt
#nohup java -Xmx16g -Xms16g -cp $CP -jar $SITE_JAR $RAW_DATA_DIR/uni_08000000_08499999.marc &>$LOG_DIR/log800-849.txt
#nohup java -Xmx16g -Xms16g -cp $CP -jar $SITE_JAR $RAW_DATA_DIR/uni_08500000_08999999.marc &>$LOG_DIR/log850-899.txt
#nohup java -Xmx16g -Xms16g -cp $CP -jar $SITE_JAR $RAW_DATA_DIR/uni_09000000_09499999.marc &>$LOG_DIR/log900-949.txt
nohup java -Xmx16g -Xms16g -Dsolr.optimize_at_end="true" -cp $CP -jar $SITE_JAR $RAW_DATA_DIR/uni_09000000_09499999.marc &>$LOG_DIR/log900-949.txt

# include Image Gallery images
curl http://sw-solr-gen:8983/solr/update?commit=true -H 'Content-type:text/xml; charset=utf-8' --data-binary @/data/image_gallery/reid-dennis/ReidDennisSolrDocs_20110722_0001.xml
curl http://sw-solr-gen:8983/solr/update?commit=true -H 'Content-type:text/xml; charset=utf-8' --data-binary @/data/image_gallery/kolb/KolbSolrDocs_20110722_0001.xml
curl http://sw-solr-gen:8983/solr/update?commit=true -H 'Content-type:text/xml; charset=utf-8' --data-binary @/data/image_gallery/kolb/KolbSolrDocs_20110722_0002.xml

echo "!!! RUN SEARCHWORKS TESTS before putting index into production !!!"

exit 0
