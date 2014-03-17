#! /bin/bash
/home/blacklight/solrmarc-sw/stanford-sw/scripts/indexNightlyNoEmailNoCrezNoCommit 140318
/home/blacklight/solrmarc-sw/stanford-sw/scripts/indexNightlyNoEmailNoCrezNoCommit 140319
# last one should do commit before crez processing
/home/blacklight/solrmarc-sw/stanford-sw/scripts/indexNightlyNoEmailNoCrez 140320

# include latest course reserves data IFF it's not done with above scripts
JRUBY_OPTS="--1.9"
export JRUBY_OPTS
LANG="en_US.UTF-8"
export LANG

(source /usr/local/rvm/scripts/rvm && cd /home/blacklight/crez-sw-ingest && source ./.rvmrc && ./bin/index_latest_no_email.sh -s prod )

echo "!!! RUN GDOR, SEARCHWORKS TESTS before putting index into production !!!"
echo "!!! CHGRP before putting index into production !!!"
