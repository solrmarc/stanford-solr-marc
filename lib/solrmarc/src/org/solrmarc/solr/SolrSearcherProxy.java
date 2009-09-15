package org.solrmarc.solr;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.Future;

//import org.apache.solr.schema.IndexSchema;
import org.apache.solr.search.*;
import org.apache.lucene.document.*;
import org.apache.lucene.search.*;
import org.apache.lucene.index.Term;

public class SolrSearcherProxy
{
    Object solrSearcher = null;
    
    public SolrSearcherProxy(SolrCoreProxy solrCoreProxy)
    {
//      refedSolrSearcher = solrCore.getSearcher();
//      solrSearcher = refedSolrSearcher.get();

        Object solrCore = solrCoreProxy.getCore();
        Object refedSolrSearcher;
        try
        {     
            Future waitSearcher[] = new Future[]{null};
            refedSolrSearcher = solrCore.getClass().getMethod("getSearcher", boolean.class, boolean.class, waitSearcher.getClass()).
                                                    invoke(solrCore, false, true, waitSearcher);
            solrSearcher = refedSolrSearcher.getClass().getMethod("get").invoke(refedSolrSearcher);
            if (waitSearcher[0] != null)
            {
                waitSearcher[0].get();
            }
        }
        catch (Exception e)
        {
            if (e instanceof java.lang.NoSuchMethodException)
            {
                Method methods[] = solrCore.getClass().getMethods();
                for (Method method : methods)
                {
                    if (method.getName().equals("getSearcher"))
                    {
                        System.err.print(method.getName() + "(");
                        Class<?> classes[] = method.getParameterTypes();
                        for (Class clazz : classes)
                        {
                            System.err.print(clazz.getName() + ", ");                            
                        }
                        System.err.println(")");
                    }
                }
            }
            e.printStackTrace();
        }
    }

    public void close()
    {
        try 
        {
            solrSearcher.getClass().getMethod("close").invoke(solrSearcher);
        }
        catch (Exception e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } 
        solrSearcher = null;
   }
    
    /**
     * get the user specified Solr doc ids for the DocSet returned when the 
     *  implied term query search is performed
     * @param field - the name of the field for the term query
     * @param value - the value of the field for the term query
     * @param docIdField - name of the field containing the user specified 
     *  unique key for the Solr document
     * @return array of Strings containing the unique keys of the Solr documents
     *  matching the query
     */
    public String[] getDocIdsFromSearch(String field, String value, String docIdField) throws IOException
    {
        String resultSet[] = null;
        try
        {
            Object docSet = getSolrDocSet(field, value);
            int totalSize = (Integer)docSet.getClass().getMethod("size").invoke(docSet);
            resultSet = new String[totalSize];
            //System. out.println("Searching for :" + field +" : "+ term+ "    Num found = " + totalSize);
            Object docIterator = docSet.getClass().getMethod("iterator").invoke(docSet);
            int i = 0;
            while (iteratorHasNext(docIterator))
            {
                int solrDocNum = iteratorGetNextSolrId(docIterator);
                resultSet[i++] = getDocIdFromSolrDocNum(solrDocNum, docIdField);
            }
        }
        catch (Exception e)
        {
            if (e instanceof InvocationTargetException)
            {
                Throwable cause = e.getCause();
                if (cause instanceof IOException) throw (IOException)cause;                
            }
            e.printStackTrace();
        }
        return(resultSet);
    }
    
    /**
     * get the Solr doc numbers for the DocSet returned when the a term query 
     *  search is performed
     * @param field - the name of the field for the term query
     * @param value - the value of the field for the term query
     * @return array of int containing the Solr doc numbers
     */
    public int[] getDocSet(String field, String value) throws IOException
    {
        int resultSet[] = null;
        try
        {
            Object docSet = getSolrDocSet(field, value);
            int totalSize = (Integer)docSet.getClass().getMethod("size").invoke(docSet);
            resultSet = new int[totalSize];
            //System. out.println("Searching for :" + field +" : "+ term+ "    Num found = " + totalSize);
            Object docIterator = docSet.getClass().getMethod("iterator").invoke(docSet);
            int i = 0;
            while (iteratorHasNext(docIterator))
            {
                resultSet[i++] = iteratorGetNextSolrId(docIterator);
            }
        }
        catch (Exception e)
        {
            if (e instanceof InvocationTargetException)
            {
                Throwable cause = e.getCause();
                if (cause instanceof IOException) throw (IOException)cause;                
            }
            e.printStackTrace();
        }
        return(resultSet);
    }
    
    /**
	 * Given an index field name and value, return a list of Lucene Document
	 *  ids that match the term query sent to the index, sorted (by Lucene) by 
	 *  the indicated field (ASCENDING)
	 * @param fld - the name of the field to be searched in the lucene index
	 * @param value - the string to be searched in the given field
	 * @param sortfld - name of the field by which results should be sorted
	 * @return an array of int containing the sorted document ids
     */
    public int[] getAscSortDocNums(String field, String value, String sortfld) throws IOException
    {
// TODO: do this w java reflection instead    	
    	Sort sort = new Sort(sortfld);
        DocList docList = getSortedSolrDocList(field, value, sort);
        return getIdsFromDocList(docList);
    }
    
    /**
	 * Given an index field name and value, return a list of Lucene Document
	 *  ids that match the term query sent to the index, sorted (by Lucene) by 
	 *  the indicated field (DESCENDING)
	 * @param fld - the name of the field to be searched in the lucene index
	 * @param value - the string to be searched in the given field
	 * @param sortfld - name of the field by which results should be sorted
	 * @return an array of int containing the sorted document ids
     */
    public int[] getDescSortDocNums(String field, String value, String sortfld) throws IOException
    {
// TODO: do this w java reflection instead    	
		Sort sort = new Sort(sortfld, true); // reverse sort!
		DocList docList = getSortedSolrDocList(field, value, sort);
        return getIdsFromDocList(docList);
    }
    
    /**
	 * Given an index field name and value, and a Sort object, return 
	 * a DocList matching the implied term query and lucene sort (lucene is
	 * doing the sort, not this method)
	 * @param fld - the name of the field to be searched in the lucene index
	 * @param value - the string to be searched in the given field
	 * @param sort - org.apache.lucene.search.Sort object
	 * @return DocList of the matching sorted documents
     */
    private DocList getSortedSolrDocList(String field, String value, Sort sort) 
    	throws IOException
	{
// TODO: do this w java reflection instead    	
		SolrIndexSearcher sis = (SolrIndexSearcher) solrSearcher;
		TermQuery termQuery = new TermQuery(new Term(field, value));
		DocSet ds = sis.getDocSet(termQuery);
		return sis.getDocList(termQuery, ds, sort, 0, ds.size());
	}
	
    /**
     * Given an (ordered) Solr docList, return the document ids as an array of
     * int, preserving order
     */
    private int[] getIdsFromDocList(DocList docList)
    	throws IOException
    {
        int[] resultSet = new int[docList.size()];

        DocIterator docIterator = docList.iterator();
        int i = 0;
        while (docIterator.hasNext())
        {
            resultSet[i++] = docIterator.nextDoc();
        }

        return resultSet;    
    }
    
    /**
	 * Given an index field name and value, return the number of documents that
	 *  match the implied term query
	 * @param field - the name of the field to be searched in the lucene index
	 * @param value - the string to be searched in the given field
     */
    public int getNumberOfHits(String field, String value) throws IOException
    {
        try
        {
            Object docSet = getSolrDocSet(field, value);
            if (docSet != null) 
            	return (Integer)docSet.getClass().getMethod("size").invoke(docSet);
        }
        catch (Exception e)
        {
            if (e instanceof InvocationTargetException)
            {
                Throwable cause = e.getCause();
                if (cause instanceof IOException) throw (IOException)cause;                
            }
            e.printStackTrace();
        }
        return(-1);
    }

    public Object getDocSetIterator(String field, String term) throws IOException
    {
        try
        {
            Object docSet = getSolrDocSet(field, term);
//            int totalSize = (Integer)docSet.getClass().getMethod("size").invoke(docSet);
            //System. out.println("Searching for :" + field +" : "+ term+ "    Num found = " + totalSize);
            Object docIterator = docSet.getClass().getMethod("iterator").invoke(docSet);
            return(docIterator);
        }
        catch (Exception e)
        {
            if (e instanceof InvocationTargetException)
            {
                Throwable cause = e.getCause();
                if (cause instanceof IOException) throw (IOException)cause;                
            }
            e.printStackTrace();
        }
        return(null);
    }
    
    private Object getSolrDocSet(String field, String value) 
    	throws IllegalArgumentException, SecurityException, IllegalAccessException, InvocationTargetException, NoSuchMethodException, ClassNotFoundException, InstantiationException 
    {
        Object schema = solrSearcher.getClass().getMethod("getSchema").invoke(solrSearcher);
        Class queryClass = Class.forName("org.apache.lucene.search.Query");
        Class parser = Class.forName("org.apache.solr.search.QueryParsing");
        Object query = parser.getMethod("parseQuery", String.class, String.class, schema.getClass())
                             .invoke(null, value, field, schema);

        return solrSearcher.getClass().getMethod("getDocSet", queryClass).invoke(solrSearcher, query);
    }
    
    private Object getSortedSolrDocList(String field, String value, String sortfld) 
    	throws IllegalArgumentException, SecurityException, IllegalAccessException, InvocationTargetException, NoSuchMethodException, ClassNotFoundException, InstantiationException
    {
/* from Bob
        Object schema = solrSearcher.getClass().getMethod("getSchema").invoke(solrSearcher);
        Class sortClass = Class.forName("org.apache.lucene.search.Sort");
        Object sort = sortClass.getConstructor(String.class).newInstance(sortfld);
        Class parser = Class.forName("org.apache.solr.search.QueryParsing");
        Object query = parser.getMethod("parseQuery", String.class, String.class, schema.getClass())
                             .invoke(null, term, field, schema);
        Class queryClass = Class.forName("org.apache.lucene.search.Query");
        Object docSet = solrSearcher.getClass().getMethod("getDocSet", queryClass).invoke(solrSearcher, query);
        int size = (Integer)docSet.getClass().getMethod("size").invoke(docSet);
        Object docList = solrSearcher.getClass().getMethod("getDocList", queryClass, queryClass, sortClass, Integer.class, Integer.class).
                                      invoke(query, null, sort, 0, size);
*/    
    
    
    
        Class termClass = Class.forName("org.apache.lucene.index.Term");
        Object searchTerm = termClass.getConstructor(String.class, String.class).newInstance(field, value);
        Object nullSearchTerm = termClass.getConstructor(String.class).newInstance(field);
        Class termQueryClass = Class.forName("org.apache.lucene.search.TermQuery");
        Object termQuery = termQueryClass.getConstructor(termClass).newInstance(searchTerm);
        Object nullTermQuery = termQueryClass.getConstructor(termClass).newInstance(nullSearchTerm);

        Class sortClass = Class.forName("org.apache.lucene.search.Sort");
        Object sort = sortClass.getConstructor(String.class).newInstance(sortfld);

        Class queryClass = Class.forName("org.apache.lucene.search.Query");
        
        Class docSetClass = Class.forName("org.apache.solr.search.DocSet");
        Object docSet = solrSearcher.getClass().getMethod("getDocSet", queryClass).invoke(solrSearcher, termQuery);
        int size = ((Integer) docSet.getClass().getMethod("size").invoke(docSet)).intValue();
        
        Object docList = solrSearcher.getClass().getMethod("getDocList", queryClass, queryClass, sortClass, int.class, int.class).
        								invoke(queryClass.cast(termQuery), queryClass.cast(nullTermQuery), sort, 0, size);
//										invoke(termQuery, nullTermQuery, sort, 0, size);
        return(docList);
    }
    
    public boolean iteratorHasNext(Object docSetIterator)
    {
        boolean result = false;
        try
        {
            result = (Boolean)(docSetIterator.getClass().getInterfaces()[0].getMethod("hasNext").invoke(docSetIterator));
        }
        catch (Exception e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return(result);
    }
    
    public int iteratorGetNextSolrId(Object docSetIterator) throws IOException
    {
        int docNo = -1;
        try
        {
            docNo = (Integer)(docSetIterator.getClass().getInterfaces()[0].getMethod("next").invoke(docSetIterator));
        }
        catch (Exception e)
        {
            if (e instanceof InvocationTargetException)
            {
                Throwable cause = e.getCause();
                if (cause instanceof IOException) throw (IOException)cause;                
            }
            e.printStackTrace();
        }
        return(docNo);
    }
    
    public DocumentProxy iteratorGetNextDoc(Object docSetIterator) throws IOException
    {
        Object docProxyObj = null;
        try
        {
            int docNo = (Integer)(docSetIterator.getClass().getInterfaces()[0].getMethod("next").invoke(docSetIterator));
            docProxyObj = solrSearcher.getClass().getMethod("doc", int.class).invoke(solrSearcher, docNo);
        }
        catch (Exception e)
        {
            if (e instanceof InvocationTargetException)
            {
                Throwable cause = e.getCause();
                if (cause instanceof IOException) throw (IOException)cause;                
            }
            e.printStackTrace();
        }
        return(new DocumentProxy(docProxyObj));
    }
    
    /**
     * given a Solr document number, return the id of the Solr document, as 
     *  stored in the docIdField
     * @param docNo - Solr document number as an int
     * @param docIdField - name of the field containing the user specified 
     *  unique key for the Solr document
     * @return the value of the docIdField in the Solr document, as a String
     */
    public String getDocIdFromSolrDocNum(int docNo, String docIdField) throws IOException
    {
        String id = null;
        try
        {
// TODO: do this w java reflection        	
    		SolrIndexSearcher sis = (SolrIndexSearcher) solrSearcher;
    		Document doc = sis.doc(docNo);
			Field f = doc.getField(docIdField);
			if (f != null) 
				id = f.stringValue();
/*			
            Object document = solrSearcher.getClass().getMethod("doc", int.class).invoke(solrSearcher, docNo);
            Object field = document.getClass().getMethod("getField", String.class).invoke(document, docIdField);
            if (field != null)
              id = field.getClass().getMethod("stringValue").invoke(field).toString();
*/
        }
        catch (Exception e)
        {
            if (e instanceof InvocationTargetException)
            {
                Throwable cause = e.getCause();
                if (cause instanceof IOException) throw (IOException)cause;                
            }
            e.printStackTrace();
        }
        return(id);
    }
    
    public DocumentProxy getDocumentBySolrDocNum(int docNo) throws IOException
    {
        Object docProxyObj = null;
        try
        {
            docProxyObj = solrSearcher.getClass().getMethod("doc", int.class).invoke(solrSearcher, docNo);
        }
        catch (Exception e)
        {
            if (e instanceof InvocationTargetException)
            {
                Throwable cause = e.getCause();
                if (cause instanceof IOException) throw (IOException)cause;                
            }
            e.printStackTrace();
        }
        return(new DocumentProxy(docProxyObj));
    }
    

// TODO: do this with reflection (see getDocumentBySolrDocNum() )    
    public Document getDocBySolrDocNum(int docNo)
    	throws IOException
    {
    	SolrIndexSearcher sis = (SolrIndexSearcher) solrSearcher;
    	return sis.doc(docNo);
    }
    
}
