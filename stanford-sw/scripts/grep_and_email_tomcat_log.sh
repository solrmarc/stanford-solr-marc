#! /bin/bash
grep 'ERROR\|WARN\|FATAL' /var/log/tomcat6/catalina.out | mail -s 'solr log warning, error and fatal messages' searchworks-reports@lists.stanford.edu, datacontrol@stanford.edu
