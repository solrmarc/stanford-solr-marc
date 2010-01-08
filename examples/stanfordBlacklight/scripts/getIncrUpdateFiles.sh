#! /bin/bash
# getIncrUpdateFiles.sh
# Pull over the latest incremental update files from Sirsi.  
#  Naomi Dushay 2010-01-08

REMOTE_DATA_DIR=/s/Dataload/SearchworkIncrement/Output

LOCAL_DATA_DIR=/home/blacklight/data/unicorn
LATEST_DATA_DIR=$LOCAL_DATA_DIR/latest/updates

# check if dump is on unicorn box

#  sftp remote files with today's datestamp to "latest/updates"
DATE=`eval date +%Y%m%d`
COUNTS_FNAME=$DATE_dates_counts
DEL_KEYS_FNAME=$DATE_ckeys_delete.del
RECORDS_FNAME=$DATE_uni_increment.marc

sftp -o 'IdentityFile=~/.ssh/id_rsa' apache@jenson:$REMOTE_DATA_DIR/$COUNTS_FNAME $LOCAL_DATA_DIR
sftp -o 'IdentityFile=~/.ssh/id_rsa' apache@jenson:$REMOTE_DATA_DIR/$DEL_KEYS_FNAME $LATEST_DATA_DIR/
sftp -o 'IdentityFile=~/.ssh/id_rsa' apache@jenson:$REMOTE_DATA_DIR/$RECORDS_FNAME $LATEST_DATA_DIR/

exit 0
