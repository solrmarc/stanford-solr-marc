#! /bin/bash
# index_all_sirsi.sh
# Import all marc files from sirsi full dump into a Solr index  (Stanford Blacklight flavor)
#  Naomi Dushay 2008-10-12

BLACKLIGHT_HOMEDIR=/home/blacklight
SOLRMARC_BASEDIR=$BLACKLIGHT_HOMEDIR/solrmarc

RAW_DATA_DIR=/data/sirsi/latest

JAVA_HOME=/usr/lib/jvm/java

# TODO: determine today's date and create log dir with today's date, with a suffix if necessary

# temporary! - take an argument for the date of the log subdirectory
 echo "   Usage: `basename $0` log_subdir(yyyy-mm-dd)"
LOG_SUBDIR=$1

# create new dist jar and other files
rm -rf $SOLRMARC_BASEDIR/local_build
ant -buildfile $SOLRMARC_BASEDIR/build.xml -Dexample.configuration=stanfordBlacklight -Dinput.continue.processing.with.not.uptodate=c dist

# set up the classpath
DIST_DIR=$SOLRMARC_BASEDIR/dist
SITE_JAR=$DIST_DIR/StanfordIndexer.jar
CORE_JAR=$DIST_DIR/SolrMarc.jar
CP=$SITE_JAR:$CORE_JAR:$DIST_DIR

# get index directories ready
SOLR_DATA_DIR=/data/solr/dataBuild
mv $SOLR_DATA_DIR/index $SOLR_DATA_DIR/index_b4_$LOG_SUBDIR
# no longer using spellcheck as of 2010-01 (?)
#mv $SOLR_DATA_DIR/spellchecker $SOLR_DATA_DIR/spellchecker_b4_$LOG_SUBDIR
#mv $SOLR_DATA_DIR/spellcheckerFile $SOLR_DATA_DIR/spellcheckerFile_b4_$LOG_SUBDIR
#mv $SOLR_DATA_DIR/spellcheckerJaroWin $SOLR_DATA_DIR/spellcheckerJaroWin_b4_$LOG_SUBDIR

# create log directory
LOG_PARENT_DIR=$RAW_DATA_DIR/logs
mkdir -p $LOG_PARENT_DIR
LOG_DIR=$LOG_PARENT_DIR/$LOG_SUBDIR
mkdir $LOG_DIR

# index the files
nohup java -Xmx16g -Xms16g -Dsolr.data.dir=$SOLR_DATA_DIR -cp $CP -jar $CORE_JAR $RAW_DATA_DIR/uni_00000000_00499999.marc &>$LOG_DIR/log000-049.txt
nohup java -Xmx16g -Xms16g -Dsolr.data.dir=$SOLR_DATA_DIR -cp $CP -jar $CORE_JAR $RAW_DATA_DIR/uni_00500000_00999999.marc &>$LOG_DIR/log050-099.txt
nohup java -Xmx16g -Xms16g -Dsolr.data.dir=$SOLR_DATA_DIR -cp $CP -jar $CORE_JAR $RAW_DATA_DIR/uni_01000000_01499999.marc &>$LOG_DIR/log100-149.txt
nohup java -Xmx16g -Xms16g -Dsolr.data.dir=$SOLR_DATA_DIR -cp $CP -jar $CORE_JAR $RAW_DATA_DIR/uni_01500000_01999999.marc &>$LOG_DIR/log150-199.txt
nohup java -Xmx16g -Xms16g -Dsolr.data.dir=$SOLR_DATA_DIR -cp $CP -jar $CORE_JAR $RAW_DATA_DIR/uni_02000000_02499999.marc &>$LOG_DIR/log200-249.txt
nohup java -Xmx16g -Xms16g -Dsolr.data.dir=$SOLR_DATA_DIR -cp $CP -jar $CORE_JAR $RAW_DATA_DIR/uni_02500000_02999999.marc &>$LOG_DIR/log250-299.txt
nohup java -Xmx16g -Xms16g -Dsolr.data.dir=$SOLR_DATA_DIR -cp $CP -jar $CORE_JAR $RAW_DATA_DIR/uni_03000000_03499999.marc &>$LOG_DIR/log300-349.txt
nohup java -Xmx16g -Xms16g -Dsolr.data.dir=$SOLR_DATA_DIR -cp $CP -jar $CORE_JAR $RAW_DATA_DIR/uni_03500000_03999999.marc &>$LOG_DIR/log350-399.txt
nohup java -Xmx16g -Xms16g -Dsolr.data.dir=$SOLR_DATA_DIR -cp $CP -jar $CORE_JAR $RAW_DATA_DIR/uni_04000000_04499999.marc &>$LOG_DIR/log400-449.txt
nohup java -Xmx16g -Xms16g -Dsolr.data.dir=$SOLR_DATA_DIR -cp $CP -jar $CORE_JAR $RAW_DATA_DIR/uni_04500000_04999999.marc &>$LOG_DIR/log450-499.txt
nohup java -Xmx16g -Xms16g -Dsolr.data.dir=$SOLR_DATA_DIR -cp $CP -jar $CORE_JAR $RAW_DATA_DIR/uni_05000000_05499999.marc &>$LOG_DIR/log500-549.txt
nohup java -Xmx16g -Xms16g -Dsolr.data.dir=$SOLR_DATA_DIR -cp $CP -jar $CORE_JAR $RAW_DATA_DIR/uni_05500000_05999999.marc &>$LOG_DIR/log550-599.txt
nohup java -Xmx16g -Xms16g -Dsolr.data.dir=$SOLR_DATA_DIR -cp $CP -jar $CORE_JAR $RAW_DATA_DIR/uni_06000000_06499999.marc &>$LOG_DIR/log600-649.txt
nohup java -Xmx16g -Xms16g -Dsolr.data.dir=$SOLR_DATA_DIR -cp $CP -jar $CORE_JAR $RAW_DATA_DIR/uni_06500000_06999999.marc &>$LOG_DIR/log650-699.txt
nohup java -Xmx16g -Xms16g -Dsolr.data.dir=$SOLR_DATA_DIR -cp $CP -jar $CORE_JAR $RAW_DATA_DIR/uni_07000000_07499999.marc &>$LOG_DIR/log700-749.txt
nohup java -Xmx16g -Xms16g -Dsolr.data.dir=$SOLR_DATA_DIR -cp $CP -jar $CORE_JAR $RAW_DATA_DIR/uni_07500000_07999999.marc &>$LOG_DIR/log750-799.txt
nohup java -Xmx16g -Xms16g -Dsolr.data.dir=$SOLR_DATA_DIR -cp $CP -jar $CORE_JAR $RAW_DATA_DIR/uni_08000000_08499999.marc &>$LOG_DIR/log800-849.txt
nohup java -Xmx16g -Xms16g -Dsolr.data.dir=$SOLR_DATA_DIR -cp $CP -jar $CORE_JAR $RAW_DATA_DIR/uni_08500000_08999999.marc &>$LOG_DIR/log850-899.txt
nohup java -Xmx16g -Xms16g -Dsolr.data.dir=$SOLR_DATA_DIR -Dsolr.optimize_at_end="true" -cp $CP -jar $CORE_JAR $RAW_DATA_DIR/uni_09000000_09499999.marc &>$LOG_DIR/log900-949.txt

exit 0
