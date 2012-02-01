#! /bin/bash
# optimize index at localhost:8983
#
# Naomi Dushay 2011-11-22

curl localhost:8983/solr/update?commit=true -H 'Content-type:text/xml; charset=utf-8' --data-binary '<optimize waitSearcher="false" />'
