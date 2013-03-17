#! /bin/bash

# revise this so there is only one commit at the end of all.
/home/blacklight/solrmarc-sw/stanford-sw/scripts/pullThenIndexSirsiIncr.sh 130309
/home/blacklight/solrmarc-sw/stanford-sw/scripts/pullThenIndexSirsiIncr.sh 130310
/home/blacklight/solrmarc-sw/stanford-sw/scripts/pullThenIndexSirsiIncr.sh 130311
/home/blacklight/solrmarc-sw/stanford-sw/scripts/pullThenIndexSirsiIncr.sh 130312
/home/blacklight/solrmarc-sw/stanford-sw/scripts/pullThenIndexSirsiIncr.sh 130313
/home/blacklight/solrmarc-sw/stanford-sw/scripts/pullThenIndexSirsiIncr.sh 130314
/home/blacklight/solrmarc-sw/stanford-sw/scripts/pullThenIndexSirsiIncr.sh 130315
/home/blacklight/solrmarc-sw/stanford-sw/scripts/pullThenIndexSirsiIncr.sh 130316
/home/blacklight/solrmarc-sw/stanford-sw/scripts/pullThenIndexSirsiIncr.sh 130317
#/home/blacklight/solrmarc-sw/stanford-sw/scripts/pullThenIndexSirsiIncrOpt.sh 120217

# include latest course reserves data IFF it's not done with above scripts
JRUBY_OPTS="--1.9"
export JRUBY_OPTS
LANG="en_US.UTF-8"
export LANG

#(source /usr/local/rvm/scripts/rvm && cd /home/blacklight/crez-sw-ingest && source ./.rvmrc && ./bin/index_latest_no_email.sh -s prod )

echo "!!! RUN SEARCHWORKS TESTS before putting index into production !!!"
echo "!!! CHGRP before putting index into production !!!"
