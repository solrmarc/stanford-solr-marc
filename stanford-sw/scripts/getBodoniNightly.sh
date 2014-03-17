#! /bin/bash
# getBodoniNightly.sh
# Pull the nightly update files from Sirsi bodoni
# defaults to most recent; can take a date arg in the form yymmdd
#  Naomi Dushay 2010-01-08

REMOTE_DATA_DIR=/s/SUL/Dataload/SearchWorksIncrement/Output

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
    
#  sftp remote files with datestamp to "latest/updates"
sftp -o 'IdentityFile=~/.ssh/id_rsa' sirsi@bodoni:$REMOTE_DATA_DIR/$COUNTS_FNAME $LOCAL_DATA_DIR
sftp -o 'IdentityFile=~/.ssh/id_rsa' sirsi@bodoni:$REMOTE_DATA_DIR/$DEL_KEYS_FNAME $LATEST_DATA_DIR/
sftp -o 'IdentityFile=~/.ssh/id_rsa' sirsi@bodoni:$REMOTE_DATA_DIR/$RECORDS_FNAME $LATEST_DATA_DIR/

exit 0
