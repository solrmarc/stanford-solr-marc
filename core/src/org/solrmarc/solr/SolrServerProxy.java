package org.solrmarc.solr;

import java.io.IOException;
import java.util.*;

import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.common.SolrInputDocument;
import org.solrmarc.tools.SolrObjectUtils;

/**
 * An implementation of SolrProxy utilizing a SolrServer from SolrJ.
 * @author Bob Haschart, with modifications by ndushay
 */
public class SolrServerProxy implements SolrProxy
{
    SolrServer solrJSolrServer;

    public SolrServerProxy(SolrServer solrJSolrServer)
    {
        this.solrJSolrServer = solrJSolrServer;
    }


    /**
     * given a map of field names and values, create a Document and add it to
     *  the index
     * @param fldNames2ValsMap keys are Solr field names, values are String or Collection objects containing values for Solr field
     * @return a string representation of the document when verbose is true or addDocToIndex is false
     */
    public String addDoc(Map<String, Object> fldNames2ValsMap, boolean verbose, boolean addDocToIndex) throws IOException
    {
        SolrInputDocument inputDoc = SolrObjectUtils.createSolrInputDoc(fldNames2ValsMap);
        if (addDocToIndex)
        {
            try
            {
				solrJSolrServer.add(inputDoc);
            }
            catch (SolrServerException e)
            {
                throw(new SolrRuntimeException("SolrServerException", e));
            }
        }

        if (verbose || !addDocToIndex)
            return inputDoc.toString().replaceAll("> ", "> \n");
        else
            return(null);
    }

    /**
     * close the solrCore
     */
    public void close()
    {
        // do nothing
    }

    public SolrServer getSolrServer()
    {
        return(solrJSolrServer);
    }

    /**
     * commit changes to the index
     */
    public void commit(boolean optimize) throws IOException
    {
        try
        {
            if (optimize)
                solrJSolrServer.optimize();
            else
                solrJSolrServer.commit();
        }
        catch (SolrServerException e)
        {
            throw(new SolrRuntimeException("SolrServerException", e));
        }
    }

    /**
     * delete doc from the index
     * @param id the unique identifier of the document to be deleted
     */
    public void delete(String id, boolean fromCommitted, boolean fromPending) throws IOException
    {
        try
        {
            solrJSolrServer.deleteById(id);
        }
        catch (SolrServerException e)
        {
            throw(new SolrRuntimeException("SolrServerException", e));
        }
    }

    /**
     * delete all docs from the index
     * Warning: be very sure you want to call this
     */
    public void deleteAllDocs() throws IOException
    {
        try
        {
            solrJSolrServer.deleteByQuery("*:*");
        }
        catch (SolrServerException e)
        {
            throw(new SolrRuntimeException("SolrServerException", e));
        }
    }

    /**
     * return true if exception is a SolrException
     */
    public boolean isSolrException(Exception e)
    {
        if (e.getCause() instanceof SolrServerException)
            return(true);
        return false;
    }

}
