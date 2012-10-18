#! /bin/bash
grep 'ERROR\|FATAL' /var/log/tomcat6/catalina.out | tac | mail -s 'solr log error and fatal messages' searchworks-reports@lists.stanford.edu, datacontrol@stanford.edu
