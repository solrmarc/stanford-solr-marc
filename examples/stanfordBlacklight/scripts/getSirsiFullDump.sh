#! /bin/bash
# getSirsiFullDump.sh
# Pull over the latest full dump from Sirsi
#  Naomi Dushay 2008-10-12

COUNTS_FNAME=files_counts
REMOTE_DATA_DIR=/s/Dataload/VufindDump/Output

LOCAL_DATA_DIR=/home/blacklight/data/unicorn
LATEST_DATA_DIR=$LOCAL_DATA_DIR/latest
PREVIOUS_DATA_DIR=$LOCAL_DATA_DIR/previous

# check if dump is on unicorn box

# grab remote files_counts
sftp -o 'IdentityFile=~/.ssh/id_rsa' apache@jenson:$REMOTE_DATA_DIR/$COUNTS_FNAME $LOCAL_DATA_DIR


# ***** TODO:  THIS DIFF CONDITIONAL DOESN'T WORK!!!!  ******
 
# check if new file counts match latest local file counts
#if [`diff $LOCAL_DATA_DIR/$COUNTS_FNAME $LATEST_DATA_DIR/$COUNTS_FNAME` -eq 0]
`diff -q $LOCAL_DATA_DIR/$COUNTS_FNAME $LATEST_DATA_DIR/$COUNTS_FNAME` >/dev/null 2>&1
if [ $? -eq 0]
then 
  echo " Local Unicorn dump is up to date"
  exit 0
fi
# if different, it is new

# rm old data from "previous"
rm $PREVIOUS_DATA_DIR/*

#  mv "latest" data to "previous"
mv $LATEST_DATA_DIR/logs/* $PREVIOUS_DATA_DIR/logs
mv $LATEST_DATA_DIR/* $PREVIOUS_DATA_DIR

# temporary! - take an argument for the date of the directory
#EXPECTED_ARGS = 1;
#if [$# -ne $EXPECTED_ARGS]
#then
#  echo "   Usage: `basename $0` yyyy-mm-dd"
#  exit 65
#fi

#DUMP_DATE=$1
#LOCAL_DATA_DIR=/home/blacklight/data/unicorn_$DUMP_DATE
#mkdir $LOCAL_DATA_DIR


#  scp remote files to "latest" preserving timestamps
sftp -o 'IdentityFile=~/.ssh/id_rsa' apache@jenson:$REMOTE_DATA_DIR/* $LATEST_DATA_DIR/

exit 0
