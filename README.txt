This is a FORK of the SolrMarc project;  it was streamlined for the single
purpose of indexing Stanford Marc data for the SearchWorks application
(a Blacklight application).

SolrMarc is a utility that reads in MaRC records from a file, extracts 
information from various fields as specified in an indexing configuration 
script, and adds that information to a specified Apache Solr index.

SolrMarc provides a rich set of techniques for mapping the tags, fields, and 
subfields contained in the MARC record to the fields you wish to include in your
Solr documents, but it also allows the creation of custom indexing functions if 
you cannot achieve what you require using the predefined mapping techniques.

Aside from inline comments, the best place to find documentation is on the 
non-forked SolrMarc project pages at

http://code.google.com/p/solrmarc/w/list
