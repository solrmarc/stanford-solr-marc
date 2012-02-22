package org.solrmarc.tools;

import java.util.*;

import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.client.solrj.util.ClientUtils;

/**
 * Utility methods for manipulating Solr objects
 * @author Naomi Dushay
 */
public class SolrObjectUtils
{

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
