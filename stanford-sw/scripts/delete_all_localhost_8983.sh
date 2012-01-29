#! /bin/bash
# delete all records from the index
#
# Naomi Dushay 2011-11-22

curl localhost:8983/solr/update?commit=true -H 'Content-type:text/xml; charset=utf-8' --data-binary '<delete><query>*:*</query></delete>'
