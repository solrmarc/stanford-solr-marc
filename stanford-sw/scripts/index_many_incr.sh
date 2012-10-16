#! /bin/bash

# revise this so there is only one commit at the end of all.
/home/blacklight/solrmarc-sw/stanford-sw/scripts/pullThenIndexSirsiIncr.sh 120720
/home/blacklight/solrmarc-sw/stanford-sw/scripts/pullThenIndexSirsiIncr.sh 120721
/home/blacklight/solrmarc-sw/stanford-sw/scripts/pullThenIndexSirsiIncr.sh 120722
/home/blacklight/solrmarc-sw/stanford-sw/scripts/pullThenIndexSirsiIncr.sh 120723
/home/blacklight/solrmarc-sw/stanford-sw/scripts/pullThenIndexSirsiIncr.sh 120724
/home/blacklight/solrmarc-sw/stanford-sw/scripts/pullThenIndexSirsiIncr.sh 120725
/home/blacklight/solrmarc-sw/stanford-sw/scripts/pullThenIndexSirsiIncr.sh 120726
#/home/blacklight/solrmarc-sw/stanford-sw/scripts/pullThenIndexSirsiIncrOpt.sh 120217

# include latest course reserves data IFF it's not done with above scripts
JRUBY_OPTS="--1.9"
export JRUBY_OPTS
#( cd /home/blacklight/crez-sw-ingest && source ./.rvmrc && ./bin/index_latest_no_email.sh -s prod )

echo "!!! RUN SEARCHWORKS TESTS before putting index into production !!!"
echo "!!! CHGRP before putting index into production !!!"
