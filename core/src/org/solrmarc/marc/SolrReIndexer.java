package org.solrmarc.marc;

import java.io.*;
import java.util.*;

import org.marc4j.*;
import org.marc4j.marc.Record;
import org.solrmarc.solr.SolrProxy;
import org.solrmarc.solr.SolrServerProxy;
import org.solrmarc.tools.*;

import org.apache.log4j.Logger;
import org.apache.solr.client.solrj.*;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.*;


/**
 * Reindex marc records stored in a Solr index
 * @author Robert Haschart
 * @version $Id$
 *
 */
public class SolrReIndexer extends MarcImporter
{
    private String queryForRecordsToUpdate;
    protected String solrFieldContainingEncodedMarcRecord;
    protected boolean doUpdate = true;
    protected MarcWriter output = null;
    protected SolrServer solrServer= null;
    private boolean getIdsOnly = false;

    // Initialize logging category
    static Logger logger = Logger.getLogger(SolrReIndexer.class.getName());

    public SolrReIndexer()
    {
    }


    /**
     * Constructs an instance using given SolrProxy
     */
    public SolrReIndexer(SolrProxy solrProxy)
    {
    	this();
    	this.solrProxy = solrProxy;
    }


    @Override
    public int handleAll()
    {
        verbose = false;
        output = new MarcStreamWriter(System.out, "UTF8", true);
        if (solrFieldContainingEncodedMarcRecord == null)
        	solrFieldContainingEncodedMarcRecord = "marc_display";
        if (getIdsOnly)
            outputAllMatchingIds(queryForRecordsToUpdate);
        else
            outputAllMatchingDocs(queryForRecordsToUpdate);
        output.close();
        return 0;
    }

    @Override
    protected void loadLocalProperties()
    {
//      configProps.setProperty("solrmarc.use_binary_request_handler", "false");
//        configProps.setProperty("solrmarc.use_streaming_proxy", "false");
        super.loadLocalProperties();
        solrFieldContainingEncodedMarcRecord = PropertiesUtils.getProperty(configProps, "solr.fieldname");
        queryForRecordsToUpdate = PropertiesUtils.getProperty(configProps, "solr.query");
        String up = PropertiesUtils.getProperty(configProps, "solr.do_update");
        doUpdate = (up == null) ? true : Boolean.parseBoolean(up);
    }

    @Override
    protected void processAdditionalArgs()
    {
        int argOffset = 0;
        if (addnlArgs.length > 0 && addnlArgs[0].equals("-id"))
        {
            getIdsOnly = true;
            argOffset = 1;
        }
        if (queryForRecordsToUpdate == null && addnlArgs.length > argOffset)
            queryForRecordsToUpdate = addnlArgs[argOffset];
        if (solrFieldContainingEncodedMarcRecord == null && addnlArgs.length > argOffset+1)
            solrFieldContainingEncodedMarcRecord = addnlArgs[argOffset+1];
        solrServer = ((SolrServerProxy)solrProxy).getSolrServer();
    }


    /**
	 * Retrieve a single document from the solr index, given the implied fielded
	 *  search (which assumes a request handler named "standard" that uses
	 *  the Lucene QueryParser), then get the marc record stored in the indicated
	 *  Solr field, then re-index the marc record and return the SolrInputDocument
	 *  created (new SolrInputDocument is not written to the index.)
	 * @param solrFldName field name for Solr query (fielded search)
	 * @param solrFldVal  field value for Solr query (fielded search)
     * @param marcRecFldName the name of the Solr field containing the full Marc Record
     * @return a populated SolrInputDocument that has not been written to the Solr Index
     */
    public SolrInputDocument getSolrInputDoc(String solrFldName, String solrFldVal, String marcRecFldName)
    {
    	SolrDocument solrDoc = getSingleSolrDocumentFromSolr(solrFldName, solrFldVal);
    	Record marcRecObj = getMarcRecObjFromSolrDoc(solrDoc, marcRecFldName);
    	return getSolrInputDocFromMarcRec(marcRecObj);
    }


    /**
	 * Retrieve a single document from the solr index, given the implied fielded
	 *  search (which assumes a request handler named "standard" that uses
	 *  the Lucene QueryParser
	 * @param solrFldName field name for Solr query (fielded search)
	 * @param solrFldVal  field value for Solr query (fielded search)
	 * @return the single matching SolrDocument
	 */
    public SolrDocument getSingleSolrDocumentFromSolr(String solrFldName, String solrFldVal)
    {
	    SolrDocumentList sdl = SolrUtils.getDocsFromFieldedQuery(solrServer, solrFldName, solrFldVal, "standard");
	    if (sdl == null || sdl.size() != 1)
	    {
            logger.warn("Didn't find single Solr document with value " + solrFldVal + " in field " + solrFldName);
	    	return null;
	    }
	    else
	    	return sdl.get(0);
    }


    /**
     * Retrieve the marc information from the Solr document
     * @param solrDoc SolrDocument from the index
     * @param marcRecFldName the name of the Solr field containing the full Marc Record
     * @return marc4j Record
     */
    public Record getMarcRecObjFromSolrDoc(SolrDocument solrDoc, String marcRecFldName)
    {
        String field = null;
        field = solrDoc.getFirstValue(marcRecFldName).toString();
        if (field == null || field.length() == 0)
        {
            //System.err.println("field: "+ solrFieldContainingEncodedMarcRecord + " not found in solr document");
            logger.warn("field: "+ marcRecFldName + " not found in solr document");
            return(null);
        }

        String marcRecordStr = field;
        if (marcRecordStr.startsWith("<?xml version"))
        	return (MarcReadingUtils.getRecordFromXMLString(marcRecordStr, !doUpdate));
        else if (marcRecordStr.startsWith("{"))
        	return MarcReadingUtils.getRecordFromJSONString(marcRecordStr, verbose);
        else
            return MarcReadingUtils.getRecordFromBinaryMarc(marcRecordStr, verbose);
    }


    /**
     * Retrieve the marc information from the Solr document
     * @param solrDoc SolrDocument from the index
     * @return marc4j Record
     */
    public Record getMarcRecObjFromSolrDoc(SolrDocument solrDoc)
    {
    	return getMarcRecObjFromSolrDoc(solrDoc, solrFieldContainingEncodedMarcRecord);
    }


    /**
     * Given a marc4j Record object, run it through indexing code to create a
     *  SolrInputDocument ... but don't write the document to the index
     */
    public SolrInputDocument getSolrInputDocFromMarcRec(Record marcRecord)
    {
    	if (marcRecord == null)
    		return null;

        Map<String, Object> fldNames2ValsMap = indexer.createFldNames2ValsMap(marcRecord, errors);
        return SolrUtils.createSolrInputDoc(fldNames2ValsMap);
    }


    /**
     * Get marc records from the index using passed query and write ids to System.out
     * @param queryForRecordsToUpdate
     */
    public void outputAllMatchingIds(String queryForRecordsToUpdate)
    {
        String queryparts[] = queryForRecordsToUpdate.split(":");
        if (queryparts.length != 2)
        {
            logger.error("Error:  query must be of the form    field:term");
            System.out.println("Error: query must be of the form    field:term  " + queryForRecordsToUpdate);
            return;
        }

        SolrQuery query = new SolrQuery();
        query.setQuery(queryForRecordsToUpdate);
        query.setQueryType("standard");
        query.setFacet(false);
        query.setRows(1000);
        query.setFields("id");
        int totalHits = -1;
        int totalProcessed = 0;
        try
        {
            do {
                query.setStart(totalProcessed);
                QueryResponse response = solrServer.query(query);
                SolrDocumentList sdl = response.getResults();
                if (totalHits == -1)
                	totalHits = (int)sdl.getNumFound();
                for (SolrDocument doc : sdl)
                {
                    String id = doc.getFieldValue("id").toString();
                    totalProcessed++;
                    if (output != null && id != null)
                    {
                        System.out.println(id);
                        System.out.flush();
                    }
                }
            } while (totalProcessed < totalHits);
        }
        catch (SolrServerException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**
     * Get marc records from the index using passed query and write marc records to System.out
     * @param queryForRecordsToUpdate
     */
    public void outputAllMatchingDocs(String queryForRecordsToUpdate)
    {
        String queryparts[] = queryForRecordsToUpdate.split(":");
        if (queryparts.length != 2)
        {
            //System.err.println("Error query must be of the form    field:term");
            logger.error("Error query must be of the form    field:term");
            System.out.println("Error: query must be of the form    field:term  " + queryForRecordsToUpdate);
            return;
        }

        // grab them 1000 at a time
        SolrQuery query = new SolrQuery();
        query.setQuery(queryForRecordsToUpdate);
        query.setQueryType("standard");
        query.setFacet(false);
        query.setRows(1000);
        int totalHits = -1;
        int totalProcessed = 0;
        try
        {
            do {
                query.setStart(totalProcessed);
                QueryResponse response = solrServer.query(query);
                SolrDocumentList sdl = response.getResults();
                if (totalHits == -1)
                	totalHits = (int)sdl.getNumFound();
                for (SolrDocument doc : sdl)
                {
                    totalProcessed++;
                    Record record = getMarcRecObjFromSolrDoc(doc);
                    if (output != null && record != null)
                    {
                        output.write(record);
                        System.out.flush();
                    }
                }
            } while (totalProcessed < totalHits);
        }
        catch (SolrServerException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**
	 * Retrieve docs from the solr index and re-index them by extracting
	 *  the full marc stored in the document, running it through the indexer
	 *  and then writing the result to solr.
	 * @param solrFldName field name for Solr query (fielded search)
	 * @param solrFldVal  field value for Solr query (fielded search)
	 * @param update set to true to update the record
	 * @return Map of the solr fields
	 */
	public Map<String, Object> retrieveAndReIndexDocs(String solrFldName, String solrFldVal, boolean update)
	{
	    SolrDocumentList sdl = SolrUtils.getDocsFromFieldedQuery(solrServer, solrFldName, solrFldVal, "standard");
        for (SolrDocument solrDoc : sdl)
        {
            Record marcRec = getMarcRecObjFromSolrDoc(solrDoc);
            if (marcRec != null)
            {
                Map<String, Object> fldNames2ValsMap = indexer.createFldNames2ValsMap(marcRec, errors);
                if (update && fldNames2ValsMap != null && fldNames2ValsMap.size() != 0)
                    updateSolrIndex(fldNames2ValsMap);
                else
                    return(fldNames2ValsMap);
            }
        }

	    return(null);
	}


	/**
     * Given the fldNames2ValsMap, turn it into a Solr Document and write the
     *  document to the index
     * @param fldNames2ValsMap the Map of solr fields (keys are field names, values are an Object containing the values (a Set or a String))
     */
    public void updateSolrIndex(Map<String, Object> fldNames2ValsMap)
    {
        try
        {
            String docStr = solrProxy.addDoc(fldNames2ValsMap, verbose, true);
            if (verbose)
                logger.info(docStr);

        }
        catch (IOException ioe)
        {
            logger.error("Couldn't add document: " + ioe.getMessage());
        }
    }

// ---- static methods that could move elsewhere ---

    /**
     * Add extra information from a Solr Document to a map
     * @param solrDoc Solr Document to pull information from
     * @param fldNames2ValsMap the Map to add information to
     * @param solrFieldName name of field (value) to retrieve from Solr document
     */
    protected static void addSolrDocValueToMap(SolrDocument solrDoc, Map<String, Object> fldNames2ValsMap, String solrFieldName)
    {
        Collection<Object> solrDocFldVals = solrDoc.getFieldValues(solrFieldName);
        if (solrDocFldVals != null && solrDocFldVals.size() > 0)
        {
            for (Object fieldValObj : solrDocFldVals)
            {
                String fieldVal = fieldValObj.toString();
                addValToFldNames2ValsMap(fldNames2ValsMap, solrFieldName, fieldVal);
            }
        }
    }

    /**
     * Add the passed value to the given field in the fldNames2ValsMap; if
     *  there is an existing value, the new value will be added.
     * @param fldNames2ValsMap the Map of solr fields (keys are field names, values are an Object containing the values (a Set or a String))
     * @param fldName the name of the field we are adding
     * @param value the value of the field we are adding
     */
    protected static void addValToFldNames2ValsMap(Map<String, Object> fldNames2ValsMap, String fldName, String value)
    {
        if (fldNames2ValsMap.containsKey(fldName))
        {
            Object prevValue = fldNames2ValsMap.get(fldName);
            if (prevValue instanceof String)
            {
                if (!prevValue.equals(value))
                {
                    Set<String> result = new LinkedHashSet<String>();
                    result.add((String) prevValue);
                    result.add((String) value);
                    fldNames2ValsMap.put(fldName, result);
                }
            }
            else if (prevValue instanceof Collection)
            {
                Iterator<String> valIter = ((Collection) prevValue).iterator();
                boolean addit = true;
                while (valIter.hasNext())
                {
                    String collVal = valIter.next();
                    if (collVal.equals(value))
                    	addit = false;
                }
                if (addit)
                {
                    ((Collection) prevValue).add(value);
                    fldNames2ValsMap.put(fldName, prevValue);
                }
            }
        }
        else
            fldNames2ValsMap.put(fldName, value);
    }


    /*
     * @param args
     */
    public static void main(String[] args)
    {
        String newArgs[] = new String[args.length + 1];
        System.arraycopy(args, 0, newArgs, 1, args.length);
        newArgs[0] = "NONE";

        SolrReIndexer reIndexer = new SolrReIndexer();
        try
        {
        	reIndexer.init(newArgs);
        }
        catch (FileNotFoundException e)
        {
        	e.printStackTrace();
        	System.exit(666);
        }

        reIndexer.handleAll();

        reIndexer.finish();
//        System.clearProperty("marc.path");
//        System.clearProperty("marc.source");
        System.exit(0);

    }

}
