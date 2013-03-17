#! /bin/bash
grep 'ERROR\|FATAL' /var/log/tomcat6/catalina.out | tac > /data/sirsi/latest/updates/logs/tmp.txt
mail -s 'solr log error and fatal messages' searchworks-reports@lists.stanford.edu, datacontrol@stanford.edu < /data/sirsi/latest/updates/logs/tmp.txt
