#! /bin/bash
# getBodoniHourly.sh
# Pull over the latest partial day update files from Sirsi bodoni
#  Naomi Dushay 2010-01-08

REMOTE_DATA_DIR=/s/SUL/Dataload/SearchworksPartday/Output

LOCAL_DATA_DIR=/data/sirsi
LATEST_DATA_DIR=$LOCAL_DATA_DIR/latest/updates

# get filename date
COUNTS_FNAME="dates_counts"
DEL_KEYS_FNAME="ckeys_delete.del"
RECORDS_FNAME="uni_partday.marc"
    
#  sftp remote files with today's datestamp to "latest/updates"
sftp -o 'IdentityFile=~/.ssh/id_rsa' sirsi@bodoni:$REMOTE_DATA_DIR/$COUNTS_FNAME $LOCAL_DATA_DIR
sftp -o 'IdentityFile=~/.ssh/id_rsa' sirsi@bodoni:$REMOTE_DATA_DIR/$DEL_KEYS_FNAME $LATEST_DATA_DIR/
sftp -o 'IdentityFile=~/.ssh/id_rsa' sirsi@bodoni:$REMOTE_DATA_DIR/$RECORDS_FNAME $LATEST_DATA_DIR/

exit 0
