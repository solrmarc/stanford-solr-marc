package org.solrmarc.solr;

import java.io.IOException;
import java.util.*;

import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.common.SolrInputDocument;

public class SolrServerProxy implements SolrProxy
{
    SolrServer solrJSolrServer;
    public SolrServerProxy(SolrServer solrJSolrServer)
    {
        this.solrJSolrServer = solrJSolrServer;
    }
    
    public String addDoc(Map<String, Object> fieldsMap, boolean verbose, boolean addDocToIndex) throws IOException
    {
        SolrInputDocument inputDoc = new SolrInputDocument();
        Iterator<String> keys = fieldsMap.keySet().iterator();
        while (keys.hasNext())
        {
            String fldName = keys.next();
            Object fldValObject = fieldsMap.get(fldName);
            if (fldValObject instanceof Collection<?>)
            {
                Collection<?> collValObject = (Collection<?>)fldValObject;
                for (Object item : collValObject)
                {
                    inputDoc.addField(fldName, item, 1.0f );
                }
            }
            else if (fldValObject instanceof String)
            {
                inputDoc.addField(fldName, fldValObject, 1.0f );
            }
        }
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

    public void close()
    {
        // do nothing
    }

    public SolrServer getSolrServer()
    {
        return(solrJSolrServer);        
    }
    
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

    public boolean isSolrException(Exception e)
    {
        if (e.getCause() instanceof SolrServerException)
            return(true);
        return false;
    }

}
