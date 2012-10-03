package org.solrmarc.tools;

import java.util.*;

import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.client.solrj.*;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.util.ClientUtils;

/**
 * Utility methods for interacting with Solr and Solr objects
 * @author Naomi Dushay
 */
public class SolrUtils
{

	/**
	 * Given a Solr field name and value and the name of a requestHandler,
	 *  query Solr and return the matching Solr documents
	 * @param SolrServer - the initialized SolrJ SolrServer object to be used
	 *  to interact with the Solr index.
	 * @param solrFldName - the name of the field to be matched
	 * @param solrFldVal - the field value to be matched
	 * @param requestHandlerName - the name of the request handler to be used
	 * @return the matching Solr documents, as a SolrDocumentList
	 */
	public static SolrDocumentList getDocsFromFieldedQuery(SolrServer solrServer, String solrFldName, String solrFldVal, String requestHandlerName)
	{
	    SolrQuery query = new SolrQuery();
	    query.setQuery(solrFldName + ":" + solrFldVal);
	    query.setQueryType(requestHandlerName);
	    query.setFacet(false);
	    try
	    {
	        QueryResponse response = solrServer.query(query);
	        return response.getResults();
	    }
	    catch (SolrServerException e)
	    {
	        e.printStackTrace();
	    }

	    return null;
	}

	/**
	 * Do a fielded query using the requestHandler, return the matching Solr
	 * documents with all their (stored) fields
	 * @param SolrServer - the initialized SolrJ SolrServer object to be used to interact with the Solr index.
	 * @param solrFldName - the name of the field to be matched
	 * @param solrFldVal - the field value to be matched
	 * @param requestHandlerName - the name of the request handler to be used
	 * @return the matching Solr documents, as a SolrDocumentList
	 */
	public static SolrDocumentList getFullDocsFromFieldedQuery(SolrServer solrServer, String solrFldName, String solrFldVal, String requestHandlerName)
	{
	    SolrQuery query = new SolrQuery();
	    query.setQuery(solrFldName + ":" + solrFldVal);
	    query.setQueryType(requestHandlerName);
	    query.setFacet(false);
	    query.setParam("fl", "*");
	    try
	    {
	        QueryResponse response = solrServer.query(query);
	        return response.getResults();
	    }
	    catch (SolrServerException e)
	    {
	        e.printStackTrace();
	    }

	    return null;
	}

	/**
     * create a SolrInputDocument from a Map of Solr field names to values
     * @param fldNames2ValsMap keys are Solr field names, values are String or Collection objects containing values for Solr field
     * @return SolrInputDocument with fields indicated by fields2ValuesMap;
     *  return null if fields2ValuesMap is null.
     */
    public static SolrInputDocument createSolrInputDoc(Map<String, Object> fldNames2ValsMap)
    {
    	if (fldNames2ValsMap == null)
    		return null;
        SolrInputDocument solrInputDoc = new SolrInputDocument();
        for (String fldName : fldNames2ValsMap.keySet())
        {
            Object valObj = fldNames2ValsMap.get(fldName);
            if (valObj instanceof Collection<?>)
                for (Object singleValObj : (Collection<?>) valObj)
                    solrInputDoc.addField(fldName, singleValObj, 1.0f );
            else
                solrInputDoc.addField(fldName, valObj, 1.0f );
        }
        return solrInputDoc;
    }

    /**
     * return an XML representation of the SolrInputDocument object, as a String.
     * Do not include any default boost values in the XML.
     * @param solrInputDoc
     */
    public static String getXML(SolrInputDocument solrInputDoc)
    {
    	if (solrInputDoc == null || solrInputDoc.isEmpty())
    		return null;
    	return ClientUtils.toXML(solrInputDoc).replace(" boost=\"1.0\"", "");
    }


// FIXME: Should it be JSON?  Probably.

}
