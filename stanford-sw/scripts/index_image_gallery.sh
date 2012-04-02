#! /bin/bash
# add image gallery records to the index
#
# Naomi Dushay 2011-11-22

#curl http://sw-solr-gen:8983/solr/update?commit=true -H 'Content-type:text/xml; charset=utf-8' --data-binary @filename
curl http://sw-solr-gen:8983/solr/update -H 'Content-type:text/xml; charset=utf-8' --data-binary @/data/image_gallery/reid-dennis/ReidDennisSolrDocs_20110722_0001.xml
curl http://sw-solr-gen:8983/solr/update -H 'Content-type:text/xml; charset=utf-8' --data-binary @/data/image_gallery/kolb/KolbSolrDocs_20110722_0001.xml
curl http://sw-solr-gen:8983/solr/update?commit=true -H 'Content-type:text/xml; charset=utf-8' --data-binary @/data/image_gallery/kolb/KolbSolrDocs_20110722_0002.xml
