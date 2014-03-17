#! /bin/bash
# getBodoniNightlyAll.sh
# Pull over all the incremental update files from Sirsi bodoni
#  Naomi Dushay 2010-03-13

REMOTE_DATA_DIR=/s/SUL/Dataload/SearchWorksIncrement/Output

LOCAL_DATA_DIR=/data/sirsi
LATEST_DATA_DIR=$LOCAL_DATA_DIR/latest/updates

#  scp remote files preserving timestamps
sftp -o 'IdentityFile=~/.ssh/id_rsa' sirsi@bodoni:$REMOTE_DATA_DIR/* $LATEST_DATA_DIR/

exit 0
