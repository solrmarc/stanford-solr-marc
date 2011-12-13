package org.solrmarc.testUtils;

import static org.junit.Assert.*;

import org.junit.*;
//import org.marc4j.marc.Record;

import java.io.*;
//import java.lang.reflect.InvocationTargetException;
import java.util.*;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.log4j.*;
import org.apache.solr.client.solrj.*;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.*;
import org.solrmarc.marc.MarcImporter;
import org.solrmarc.solr.*;
import org.solrmarc.tools.Utils;
import org.xml.sax.SAXException;

public abstract class IndexTest {
	
	protected static MarcImporter importer;
    protected static SolrProxy solrProxy;
	protected static SolrServer solrServer;
	protected static SolrJettyProcess solrJettyProcess = null;

	protected static String docIDfname = "id";

    static Logger logger = Logger.getLogger(IndexTest.class.getName());
	
    protected String testDataParentPath;
    protected String testConfigFname;
    protected String testSolrUrl;
    
    protected boolean useBinaryRequestHandler = Boolean.valueOf(System.getProperty("core.test.use_streaming_proxy"));
    protected boolean useStreamingProxy = Boolean.valueOf(System.getProperty("core.test.use_binary_request_handler"));
	protected static String testSolrLogLevel = System.getProperty("test.solr.log.level");
	protected static String testSolrMarcLogLevel = System.getProperty("test.solrmarc.log.level");


	/**
	 * Start a Jetty driven solr server running in a separate JVM at port jetty.test.port
	 */
	public static void startTestJetty()
	{
		String jettyTestPortStr;

		String testSolrHomeDir = System.getProperty("test.solr.path");
		if (testSolrHomeDir == null)
			fail("property test.solr.path must be defined for the tests to run");

		String jettyDir = System.getProperty("test.jetty.dir");
		if (jettyDir == null)
			fail("property test.jetty.dir must be defined for this test to run");

		jettyTestPortStr = System.getProperty("test.jetty.port");
		// Specify port 0 to select any available port
		if (jettyTestPortStr == null)
			jettyTestPortStr = "0";

		solrJettyProcess = new SolrJettyProcess(testSolrHomeDir, jettyDir, jettyTestPortStr);
		boolean serverIsUp = false;
		try
		{
			serverIsUp = solrJettyProcess.startProcessWaitUntilSolrIsReady();
		} 
		catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
			fail("Server did not become available");
		}
		assertTrue("Server did not become available", serverIsUp);
		// assertTrue("Server did not become available", solrJettyProcess.isServerRunning());

		// If you need to see the output of the solr server after the server is up and running, call
		// solrJettyProcess.outputReset() here to empty the buffer so the later output is visible in the Eclipse variable viewer
		// solrJettyProcess.outputReset();
		System.out.println("Server is up and running at " + jettyDir + ", port " + solrJettyProcess.getJettyPort());
	}

	/**
	 * stop the Jetty server if it is running
	 */
	public static void stopTestJetty() 
	{
	    if (solrJettyProcess != null && solrJettyProcess.isServerRunning())
	        solrJettyProcess.stopServer();
	}


	/**
     * initializes the properties used to create an index over http
     */
    protected void initVarsForHttpTestIndexing()
    {
        testDataParentPath = System.getProperty("test.data.path");
        if (testDataParentPath == null)
            fail("property test.data.path must be defined for the tests to run");
    
        testConfigFname = System.getProperty("test.config.file");
        if (testConfigFname == null)
            fail("property test.config.file must be defined for the tests to run");
        
        testSolrUrl = System.getProperty("test.solr.url");
        if (testSolrUrl == null)
            fail("property test.solr.url must be defined for the tests to run");
        System.setProperty("solr.hosturl", testSolrUrl);
    }
    
    public static void setTestLoggingLevels()
    {
    	setTestLoggingLevels(testSolrLogLevel, testSolrMarcLogLevel);
    }
    
// FIXME:  move this to Utils, and also look for logging levels in config.properties    
    
	/**
	 * default settings:  solr:  WARNING;  solrmarc: WARN
	 * Solr uses java.util.logging; level settings for solr logging: 
	 *    OFF, SEVERE, WARNING, INFO, FINE, FINER, FINEST, ALL
	 * SolrMarc uses log4j logging; level settings for solrmarc logging:
	 *    OFF, FATAL, WARN, INFO, DEBUG, ALL
	 */
	public static void setTestLoggingLevels(String solrLogLevel, String solrmarcLogLevel)
	{
        java.util.logging.Level solrLevel = java.util.logging.Level.WARNING;

        if (solrLogLevel != null)
        {
            if (solrLogLevel.equals("OFF"))     solrLevel = java.util.logging.Level.OFF;
            if (solrLogLevel.equals("SEVERE"))  solrLevel = java.util.logging.Level.SEVERE;
            if (solrLogLevel.equals("WARNING")) solrLevel = java.util.logging.Level.WARNING;
            if (solrLogLevel.equals("INFO"))    solrLevel = java.util.logging.Level.INFO;
            if (solrLogLevel.equals("FINE"))    solrLevel = java.util.logging.Level.FINE;
            if (solrLogLevel.equals("FINER"))   solrLevel = java.util.logging.Level.FINER;
            if (solrLogLevel.equals("FINEST"))  solrLevel = java.util.logging.Level.FINEST;
            if (solrLogLevel.equals("ALL"))     solrLevel = java.util.logging.Level.ALL;
        }
        java.util.logging.Logger.getLogger("org.apache.solr").setLevel(solrLevel);

        org.apache.log4j.Level solrmarcLevel = org.apache.log4j.Level.WARN;
        if (solrmarcLogLevel != null)
        {
            if (solrmarcLogLevel.equals("OFF"))     solrmarcLevel = Level.OFF;
            if (solrmarcLogLevel.equals("FATAL"))   solrmarcLevel = Level.FATAL;
            if (solrmarcLogLevel.equals("WARN"))    solrmarcLevel = Level.WARN;
            if (solrmarcLogLevel.equals("INFO"))    solrmarcLevel = Level.INFO;
            if (solrmarcLogLevel.equals("DEBUG"))   solrmarcLevel = Level.DEBUG;
            if (solrmarcLogLevel.equals("ALL"))     solrmarcLevel = Level.ALL;
        }
        Utils.setLog4jLogLevel(solrmarcLevel);
	}


    /**
     * Creates a pristine Solr index from the indicated test file of marc records, and initializes 
     *  necessary variables.  Uses a bunch of class instance variables
	 * @param marcTestDataFname - file of marc records to be indexed.  should end in ".mrc" "marc" or ".xml"
     */
    protected void createFreshTestIxOverHTTP(String marcTestDataFname)
    		throws ParserConfigurationException, IOException, SAXException 
    {
        createFreshTestIxOverHTTP(testConfigFname, testSolrUrl, useBinaryRequestHandler, useStreamingProxy, testDataParentPath, marcTestDataFname);
    }

    
    /**
	 * Create a pristine Solr index from the marc file.
	 * @param confPropFilename - name of config.properties file
	 * @param testSolrUrl - url for test solr instances, as a string
	 * @param useBinaryRequestHandler - true to use the binary request handler
	 * @param useStreamingProxy - true to use streaming proxy (multiple records added at a time)
	 * @param testDataParentPath - directory containing the test data file
	 * @param marcTestDataFname - file of marc records to be indexed.  should end in ".mrc" "marc" or ".xml"
	 */
	public void createFreshTestIxOverHTTP(String configPropFilename, String testSolrUrl, 
											boolean useBinaryRequestHandler, boolean useStreamingProxy, 
	        								String testDataParentPath, String marcTestDataFname) 
	        		throws ParserConfigurationException, IOException, SAXException 
	{
	    solrProxy = SolrCoreLoader.loadRemoteSolrServer(testSolrUrl + "/update", useBinaryRequestHandler, useStreamingProxy);
	    logger.debug("just set solrProxy to remote solr server at " + testSolrUrl + " - " + solrProxy.toString());
		solrProxy.deleteAllDocs();
	    logger.debug("just deleted all docs known to the solrProxy");
	
		importer = new MarcImporter(solrProxy);
	    importer.init(new String[] {configPropFilename, testDataParentPath + File.separator + marcTestDataFname});        	
		int numImported = importer.importRecords();
	    
	    solrProxy.commit(false);  // don't optimize
	    
	    solrServer = ((SolrServerProxy)solrProxy).getSolrServer();
	    logger.debug("just set solrServer to " + solrServer.toString());
	}

	
    /**
     * Updates the Solr index from the indicated test file of marc records, and initializes 
     *  necessary variables.  Uses a bunch of class instance variables
	 * @param marcTestDataFname - file of marc records to be indexed.  should end in ".mrc" "marc" or ".xml"
     */
    protected void updateTestIxOverHTTP(String marcTestDataFname)
    		throws ParserConfigurationException, IOException, SAXException 
    {
    	updateTestIxOverHTTP(testConfigFname, testSolrUrl, useBinaryRequestHandler, useStreamingProxy, testDataParentPath, marcTestDataFname);
    }

	
    /**
	 * Updates the Solr index from the marc file.
	 * @param confPropFilename - name of config.properties file
	 * @param testSolrUrl - url for test solr instances, as a string
	 * @param useBinaryRequestHandler - true to use the binary request handler
	 * @param useStreamingProxy - true to use streaming proxy (multiple records added at a time)
	 * @param testDataParentPath - directory containing the test data file
	 * @param marcTestDataFname - file of marc records to be indexed.  should end in ".mrc" "marc" or ".xml"
	 */
	public void updateTestIxOverHTTP(String configPropFilename, String testSolrUrl, 
										boolean useBinaryRequestHandler, boolean useStreamingProxy, 
										String testDataParentPath, String marcTestDataFname)
			throws ParserConfigurationException, IOException, SAXException 
	{
	    logger.debug("solrProxy for " + testSolrUrl + " starting as - " + (solrProxy == null ? "null" : solrProxy.toString()));
		if (solrProxy == null)
		{
		    solrProxy = SolrCoreLoader.loadRemoteSolrServer(testSolrUrl + "/update", useBinaryRequestHandler, useStreamingProxy);
		    logger.debug("just set solrProxy to remote solr server at " + testSolrUrl + " - " + solrProxy.toString());
		    solrServer = ((SolrServerProxy)solrProxy).getSolrServer();
		    logger.debug("just set solrServer to " + solrServer.toString());
		}
		
		if (importer == null)
			importer = new MarcImporter(solrProxy);

	    importer.init(new String[] {configPropFilename, testDataParentPath + File.separator + marcTestDataFname});        	
		int numImported = importer.importRecords();
	    solrProxy.commit(false);  // don't optimize
	}

	
	




	/**
     * Given the paths to a marc file to be indexed, the solr directory, and
     *  the path for the solr index, create the index from the marc file.
     * @param confPropFilename - name of config.properties file
     * @param solrPath - the directory holding the solr instance (think conf files)
     * @param solrDataDir - the data directory to hold the index
     * @param testDataParentPath - directory containing the test data file
     * @param testDataFname - file of marc records to be indexed.  should end in ".mrc" "marc" or ".xml"
     * @deprecated
     */
	public void createIxInitVarsOld(String configPropFilename, String solrPath, String solrDataDir, 
	                             String testDataParentPath, String testDataFname) 
			                     throws ParserConfigurationException, IOException, SAXException 
	{
		setSolrSysProperties(solrPath, solrDataDir);

		// delete old index files
        logger.debug("System.getProperty(\"os.name\") : "+System.getProperty("os.name"));
        if (!System.getProperty("os.name").toLowerCase().contains("win"))
        {
            logger.info("Calling Delete Dir Contents");
            Utils.deleteDirContents(System.getProperty("solr.data.dir"));
        }
        else
        {
            logger.info("Calling Delete All Docs");
            importer.getSolrProxy().deleteAllDocs();
        }
		setupMarcImporter(configPropFilename, testDataParentPath + File.separator + testDataFname);
		int numImported = importer.importRecords();       
		importer.finish();
 
        solrProxy = importer.getSolrProxy();
        solrProxy.commit(false);
        solrServer = ((SolrServerProxy)solrProxy).getSolrServer();
	}
	
    /**
     * Set the appropriate system properties for Solr processing
     * @param solrPath - the directory holding the solr instance (think solr/conf files)
     * @param solrDataDir - the data directory to hold the index
     * @deprecated
     */
	private void setSolrSysProperties(String solrPath, String solrDataDir) 
	{
        if (solrPath != null)  
        {
            System.setProperty("solr.path", solrPath);
            if (solrDataDir == null)
                solrDataDir = solrPath + File.separator + "data";

            System.setProperty("solr.data.dir", solrDataDir);
        }
        if (!Boolean.parseBoolean(System.getProperty("test.solr.verbose")))
        {
            java.util.logging.Logger.getLogger("org.apache.solr").setLevel(java.util.logging.Level.SEVERE);
            Utils.setLog4jLogLevel(org.apache.log4j.Level.WARN);
        }
        // from DistSMCreateIxInitVars ... which only creates the smoketest
//        if (!Boolean.parseBoolean(System.getProperty("test.solr.verbose")))
//        {
//            addnlProps.put("solr.log.level", "OFF");
//            addnlProps.put("solrmarc.log.level", "OFF");
//        }
	}


    /**
     * Given the paths to a marc file to be indexed, the solr directory, and
     *  the path for the solr index, instantiate the MarcImporter object 
     * @param confPropFilename - name of config.properties file (must include ".properties" on the end)
     * @param argFileName - the name of a file to be processed by the
     *   MarcImporter;  should end in  "marc" or ".mrc" or ".xml" or ".del", 
     *    or be null (or the string "NONE") if there is no such file.  (All this per MarcHandler constructor)
     * @deprecated
     */
	private void setupMarcImporter(String configPropFilename, String argFileName) 
    	throws ParserConfigurationException, IOException, SAXException 
    {
        if (argFileName == null)
        	argFileName = "NONE";
        
        importer = new MarcImporter();
        if (configPropFilename != null) 
            importer.init(new String[] {configPropFilename, argFileName});        	
        else  
       	    importer.init(new String[] {argFileName});	
 	}
	
    /**
     * Given the paths to a marc file to be indexed, the solr directory, and
     *  the path for the solr index, create the index from the marc file.
     * @param confPropFilename - name of config.properties file
     * @param solrPath - the directory holding the solr instance (think conf files)
     * @param solrDataDir - the data directory to hold the index
     * @param testDataParentPath - directory containing the test data file
     * @param testDataFname - file of marc records to be indexed.  should end in ".mrc" "marc" or ".xml"
     */
	public void updateIx(String configPropFilename, String solrPath, String solrDataDir, 
	                             String testDataParentPath, String testDataFname) 
			                     throws ParserConfigurationException, IOException, SAXException 
	{
		setSolrSysProperties(solrPath, solrDataDir);
		setupMarcImporter(configPropFilename, testDataParentPath + File.separator + testDataFname);
		int numImported = importer.importRecords();
// FIXME:  Naomi doesn't think this will work for remote server debugging
		importer.finish();
 
        solrProxy = (SolrProxy)importer.getSolrProxy();
        solrProxy.commit(false);
        solrServer = ((SolrServerProxy)solrProxy).getSolrServer();
	}

    /**
     * Given the paths to a marc file to be indexed, the solr directory, and
     *  the path for the solr index, delete the records from the index.
     * @param confPropFilename - name of config.properties file
     * @param solrPath - the directory holding the solr instance (think conf files)
     * @param solrDataDir - the data directory to hold the index
	 *  @param deletedIdsFilename - file containing record ids to be deleted (including parent path)
	 *  @deprecated
     */
	public void deleteRecordsFromIx(String configPropFilename, String solrPath, String solrDataDir, String deletedIdsFilename) 
			                     throws ParserConfigurationException, IOException, SAXException 
	{
		setSolrSysProperties(solrPath, solrDataDir);
        if (deletedIdsFilename != null)
        	System.setProperty("marc.ids_to_delete", deletedIdsFilename);
		setupMarcImporter(configPropFilename, deletedIdsFilename);    
        
        int numDeleted = importer.deleteRecords();       
// FIXME:  Naomi doesn't think this will work for remote server debugging
        importer.finish();
 
        solrProxy = importer.getSolrProxy();
        solrProxy.commit(false);
        solrServer = ((SolrServerProxy)solrProxy).getSolrServer();
	}
	
	
    /**
     * Given the paths to a marc file to be indexed, the solr directory, and
     *  the path for the solr index, create the index from the marc file.
     * @param confPropFilename - name of config.properties file
     * @param solrPath - the directory holding the solr instance (think conf files)
     * @param solrDataDir - the data directory to hold the index
     * @param testDataParentPath - directory containing the test data file
     * @param testDataFname - file of marc records to be indexed.  should end in ".mrc" "marc" or ".xml"
     * @deprecated
     */
	public void createIxInitVarsDistSM2_3_1(String configPropFilename, String solrPath, String solrDataDir, 
	                             String testDataParentPath, String testDataFname) 
	{
        //System.err.println("test.solr.verbose = " + System.getProperty("test.solr.verbose"));
//      if (!Boolean.parseBoolean(System.getProperty("test.solr.verbose")))
//      {
//          java.util.logging.Logger.getLogger("org.apache.solr").setLevel(java.util.logging.Level.SEVERE);
//          Utils.setLog4jLogLevel(org.apache.log4j.Level.WARN);
//      }
//      addnlProps = new LinkedHashMap<String, String>();
//      backupProps = new LinkedHashMap<String, String>();
//      allOrigProps = new LinkedHashMap<String, String>();
//      CommandLineUtils.checkpointProps(allOrigProps);
//
//      if (solrPath != null)  
//      {
//          addnlProps.put("solr.path", solrPath);
////          if (solrDataDir == null)
////          {
////              solrDataDir = solrPath + File.separator + "data";
////          }
////          addnlProps.put("solr.data.dir", solrDataDir);
//      }
      logger.debug("System.getProperty(\"os.name\") : "+System.getProperty("os.name"));
//      if (!System.getProperty("os.name").toLowerCase().contains("win"))
//      {
//          //   comment out these two lines since if the solr data dir is set the same as the solr home, the conf directory would be deleted as well.
//          //   for that matter, if the solr data dir is accidently pointed at a valued directory, that directory, and all of its children, would be wiped out.  
////           logger.info("Calling Delete Dir Contents");
////           deleteDirContents(solrDataDir);
//      }
      // index a small set of records (actually one record)
      ByteArrayOutputStream out1 = new ByteArrayOutputStream();
      ByteArrayOutputStream err1 = new ByteArrayOutputStream();
      Map<String,String> addnlProps = new LinkedHashMap<String,String>();
      addnlProps.put("solr.path", solrPath);
      if (solrDataDir != null)
      {
          addnlProps.put("solr.data.dir", solrDataDir);
      }
      if (!Boolean.parseBoolean(System.getProperty("test.solr.verbose")))
      {
          addnlProps.put("solr.log.level", "OFF");
          addnlProps.put("solrmarc.log.level", "OFF");
      }

      CommandLineUtils.runCommandLineUtil("org.solrmarc.marc.MarcImporter", "main", null, out1, err1, new String[]{configPropFilename, testDataParentPath + File.separator + testDataFname }, addnlProps);
      solrProxy = SolrCoreLoader.loadEmbeddedCore(solrPath, solrDataDir, null, false, logger);
      solrServer = ((SolrServerProxy)solrProxy).getSolrServer();
      
//      CommandLineUtils.addProps(addnlProps, backupProps);
//      importer = new MarcImporter();
//      if (configPropFilename != null)
//      {
//          importer.init(new String[]{configPropFilename, testDataParentPath + File.separator + testDataFname});
//      }
//      else 
//      {
//          importer.init(new String[]{testDataParentPath + File.separator + testDataFname});
//      }
//      if (System.getProperty("os.name").toLowerCase().contains("win"))
//      {
//          logger.info("Calling Delete All Docs");
//          importer.getSolrProxy().deleteAllDocs();
//      }
//      
//      int numImported = importer.importRecords();       
//      importer.finish();
//      
//      solrProxy = (SolrCoreProxy)importer.getSolrProxy();
//      solrCoreProxy.commit(false);
//      searcherProxy = new SolrSearcherProxy(solrCoreProxy);
	}
	
		
	/**
	 * close and set solrProxy to null
	 */
//@After
	public static void closeSolrProxy()
	{
	    // avoid "already closed" exception
	    logger.debug("IndexTest closing solrProxy and setting to null");
        if (solrProxy != null)
        {
            logger.info("Closing solrProxy");
            solrProxy.close();
            solrProxy = null;
        }
	}
	
    /**
     * assert there is a single doc in the index with the value indicated
     * @param docId - the identifier of the SOLR/Lucene document
     * @param fldname - the field to be searched
     * @param fldVal - field value to be found
     */
    public final void assertSingleResult(String docId, String fldName, String fldVal) 
            throws ParserConfigurationException, SAXException, IOException 
    {
        SolrDocumentList sdl = getDocList(fldName, fldVal);
        if (sdl.size() == 1) 
        {
            SolrDocument doc = sdl.get(0);
            Object field = doc.getFieldValue(docIDfname);
            if (field.toString().equals(docId))
            {
                return;
            }
            fail("There is a single doc with " + fldName + " of " + fldVal + " but it is not doc \"" + docId + "\"");
        }
        if (sdl.size() == 0) 
        {
            fail("There is no doc with " + fldName + " of " + fldVal);
        }
        if (sdl.size() > 1) 
        {
            fail("There is more than 1 doc with " + fldName + " of " + fldVal);
        }    
    }

    public final void assertZeroResults(String fldName, String fldVal) 
    {
        assertResultSize(fldName, fldVal, 0);
    }
    
	/**
	 * Get the Lucene document with the given id from the solr index at the
	 *  solrDataDir
	 * @param doc_id - the unique id of the lucene document in the index
	 * @return the Lucene document matching the given id
	 */
	public final SolrDocument getDocument(String doc_id)
	{
	    SolrDocumentList sdl = getDocList(docIDfname, doc_id);
        for (SolrDocument doc : sdl)
        {
            return(doc);
        }
	    return(null);
	}
	    

    /**
     * Get the List of Solr Documents with the given value for the given field 
     *  
     * @param doc_id - the unique id of the lucene document in the index
     * @return the Lucene document matching the given id
     */
	public final SolrDocumentList getDocList(String field, String value)
	{
	    SolrQuery query = new SolrQuery(field+":"+value);
	    query.setQueryType("standard");
	    query.setFacet(false);
	    try {
	        QueryResponse response = solrServer.query(query); 
	        return(response.getResults());
	    }
	    catch (SolrServerException e)
	    {
	    	e.getCause().printStackTrace();
//	    	e.printStackTrace();
	    }
	    return(new SolrDocumentList());
	}
	
	/**
	 * asserts that the document is present in the index
	 */
	public final void assertDocPresent(String doc_id)
	{
        SolrDocumentList sdl = getDocList(docIDfname, doc_id);
        assertTrue("Found no document with id \"" + doc_id + "\"", sdl.size() == 1);
	}

	/**
	 * asserts that the document is NOT present in the index
	 */
	public final void assertDocNotPresent(String doc_id)
	{
        SolrDocumentList sdl = getDocList(docIDfname, doc_id);
        assertTrue("Found no document with id \"" + doc_id + "\"", sdl.size() == 0);
	}


	public final void assertDocHasFieldValue(String doc_id, String fldName, String fldVal)
	{
		// TODO: repeatable field vs. not ...
		//  TODO: check for single occurrence of field value, even for repeatable field
		SolrDocumentList sdl = getDocList(docIDfname, doc_id);
		if (sdl.size() > 0) 
		{
		    SolrDocument doc = sdl.get(0);
		    Collection<Object> fields = doc.getFieldValues(fldName);
		    for (Object field : fields)
		    {
		        if (field.toString().equals(fldVal))
		        {
		            // found field with desired value
		            return;
		        }
		    }
	        fail("Field " + fldName + " did not contain value \"" + fldVal + "\" in doc " + doc_id);
		}
        fail("Document " + doc_id + " was not found");
	}

	public final void assertDocHasNoFieldValue(String doc_id, String fldName, String fldVal)
	{
		// TODO: repeatable field vs. not ...
		// TODO: check for single occurrence of field value, even for repeatable field
        SolrDocumentList sdl = getDocList(docIDfname, doc_id);
        if (sdl.size() > 0) 
        {
            SolrDocument doc = sdl.get(0);
            Collection<Object> fields = doc.getFieldValues(fldName);
            for (Object field : fields)
            {
                if (field.toString().equals(fldVal))
                {
                    fail("Field " + fldName + " contained value \"" + fldVal + "\" in doc " + doc_id);
                }
            }
            return;
        }
        fail("Document " + doc_id + " was not found");
	}

//	public final int getSingleDocNum(String fldName, String fldVal)
//			throws ParserConfigurationException, SAXException, IOException 
//	{
//        SolrDocumentList sdl = getDocList(fldName, fldVal);
//		if (sdl.size() != 1)
//		{
//		    fail("The index does not have a single document containing field " 
//					+ fldName + " with value of \""+ fldVal +"\"");
//		}
//        Object id = sdl.get(0).getFieldValue(fldName);
//        return id.toString();
//	}

	@SuppressWarnings("unchecked")
	public final void assertDocHasNoField(String doc_id, String fldName) 
	{
        SolrDocumentList sdl = getDocList(docIDfname, doc_id);
        if (sdl.size() > 0) 
        {
            SolrDocument doc = sdl.get(0);
            Collection<Object> fields = doc.getFieldValues(fldName);
            if (fields == null || fields.size() == 0) 
            {
                // Document has no field by that name.  yay.
                return;
            }
            fail("Field " + fldName + " found in doc \"" + doc_id + "\"");
        }
        fail("Document " + doc_id + " was not found");
	}

	/**
	 * Do a search for the implied term query and assert the search results
	 *  have docIds that are an exact match of the set of docIds passed in
	 * @param fldName - name of the field to be searched
	 * @param fldVal - value of the field to be searched
	 * @param docIds - Set of doc ids expected to be in the results
	 */
	public final void assertSearchResults(String fldName, String fldVal, Set<String> docIds) 
	{
        SolrDocumentList sdl = getDocList(fldName, fldVal);
        
	    assertTrue("Expected " + docIds.size() + " documents for " + fldName + " search \"" 
                   + fldVal + "\" but got " + sdl.size(), docIds.size() == sdl.size());
        
		String msg = fldName + " search \"" + fldVal + "\": ";
		for (SolrDocument doc : sdl)
		{
		    assertDocInSet(doc, docIds, msg);
		}
	}
    
	public final void assertDocInSet(SolrDocument doc, Set<String> docIds, String msgPrefix) 
    {
        String id = doc.getFieldValue(docIDfname).toString();
	    if (docIds.contains(id))
        {
            return;
        }
        fail(msgPrefix + "doc \"" + id + "\" missing from list");
    }

	public final void assertFieldValues(String fldName, String fldVal, Set<String> docIds) 
	{
		for (String docId : docIds)
			assertDocHasFieldValue(docId, fldName, fldVal); 
	}

	/**
	 * ensure that the value(s) for the two fields in the document are the 
	 *  same
	 * @param docId - the id of the document
	 * @param fldName1 - the first field to match
	 * @param fldName2 - the second field to match
	 */
	public final void assertFieldValuesEqual(String docId, String fldName1, String fldName2)
			throws ParserConfigurationException, SAXException, IOException 
	{
//		int solrDocNum = getSingleDocNum(docIDfname, docId);
//		DocumentProxy doc = getSearcherProxy().getDocumentProxyBySolrDocNum(solrDocNum);
		SolrDocument doc = getDocument(docId);
//		String[] fld1Vals = doc.getValues(fldName1);
//		int numValsFld1 = fld1Vals.length;
		Collection<Object> fldObjColl = doc.getFieldValues(fldName1);
		int numValsFld1 = fldObjColl.size();
		String[] fld1Vals = fldObjColl.toArray(new String[numValsFld1]);
//		String[] fld2Vals = doc.getValues(fldName2);
//		int numValsFld2 = fld2Vals.length;
		fldObjColl = doc.getFieldValues(fldName1);
		int numValsFld2 = fldObjColl.size();
		String[] fld2Vals = fldObjColl.toArray(new String[numValsFld2]);
		String errmsg ="fields " + fldName1 + ", " + fldName2 + " have different numbers of values";
		assertEquals(errmsg, numValsFld1, numValsFld2);
		
		errmsg = "In doc " + docId + ", field " + fldName1 + " has value not in " + fldName2 + ": ";
		List<String> fld1ValList = Arrays.asList(fld1Vals);
		List<String> fld2ValList = Arrays.asList(fld2Vals);
		for (String val : fld1ValList)
		{
			if (!fld2ValList.contains(val))
				fail(errmsg + val);
		}
	}

	
	/**
	 * get all the documents matching the implied term search and check for
	 *  expected number of results
     * @param fldName - the field to be searched
     * @param fldVal - field value to be found
	 * @param numExp the number of documents expected
	 * @return List of the Documents returned from the search
	 */
	public final void assertResultSize(String fldName, String fldVal, int numExp) 
	{
        SolrDocumentList sdl = getDocList(fldName, fldVal);
        int num = sdl.size();
		assertTrue("Expected " + numExp + " documents for " + fldName + " search \"" 
				+ fldVal + "\" but got " + num, num == numExp);
	}

//	/**
//	 * get the ids of all the documents matching the implied term search
//     * @param fldName - the field to be searched
//     * @param fldVal - field value to be found
//	 */
//	public final String[] getDocIDList(String fldName, String fldVal)
//	        throws ParserConfigurationException, SAXException, IOException 
//	{
//	    return searcherProxy.getDocIdsFromSearch(fldName, fldVal, docIDfname);
//	}
//	
	/**
	 * Given an index field name and value, return a list of Lucene Documents
	 *  that match the term query sent to the index
	 * @param fld - the name of the field to be searched in the lucene index
	 * @param value - the string to be searched in the given field
	 * @return a list of Lucene Documents
	 */
	public final SolrDocumentList getAllMatchingDocs(String fld, String value) 
	{
		return getDocList(fld, value);
	}


	/**
	 * return the number of docs that match the implied term query
	 * @param fld - the name of the field to be searched in the lucene index
	 * @param value - the string to be searched in the given field
	 */
	public int getNumMatchingDocs(String fld, String value)
	{
        SolrDocumentList sdl = getDocList(fld, value);
        return(sdl.size());
	}
	
	/**
	 * Given an index field name and value, return a list of Documents
	 *  that match the term query sent to the index, sorted in ascending
	 *  order per the sort fld
	 * @param fld - the name of the field to be searched in the lucene index
	 * @param value - the string to be searched in the given field
	 * @param sortfld - name of the field by which results should be sorted
	 *   (ascending)
	 * @return a sorted list of DocumentProxy objects
	 */
	public final SolrDocumentList getAscSortDocs(String fld, String value, String sortfld) 
	{
        SolrQuery query = new SolrQuery(fld+":"+value);
        query.setQueryType("standard");
        query.setFacet(false);
        query.setSortField(sortfld, SolrQuery.ORDER.asc);
        try {
            QueryResponse response = solrServer.query(query); 
            return(response.getResults());
        }
        catch (SolrServerException e)
        {
        }
        return(new SolrDocumentList());
	}
	
	
	/**
	 * Given an index field name and value, return a list of Documents
	 *  that match the term query sent to the index, sorted in descending
	 *  order per the sort fld
	 * @param fld - the name of the field to be searched in the lucene index
	 * @param value - the string to be searched in the given field
	 * @param sortfld - name of the field by which results should be sorted
	 *   (descending)
	 * @return a sorted list of DocumentProxy objects
	 */
	public final SolrDocumentList getDescSortDocs(String fld, String value, String sortfld) 
	{
        SolrQuery query = new SolrQuery(fld+":"+value);
        query.setQueryType("standard");
        query.setFacet(false);
        query.setSortField(sortfld, SolrQuery.ORDER.desc);
        try {
            QueryResponse response = solrServer.query(query); 
            return(response.getResults());
        }
        catch (SolrServerException e)
        {
        }
        return(new SolrDocumentList());
	}

	
	/**
	 * given an array of Solr document numbers as int, return a List of
	 * DocumentProxy objects corresponding to the Solr doc nums.  Order is
	 * maintained.
	 */
//	private List<DocumentProxy> getDocProxiesFromDocNums(int[] solrDocNums) 
//		throws IOException 
//	{
//        List<DocumentProxy> docProxyList = new ArrayList<DocumentProxy>();
//        for (int solrDocNum : solrDocNums)
//            docProxyList.add( getSearcherProxy().getDocumentProxyBySolrDocNum(solrDocNum) );
//        return docProxyList;
//	}
	
    

//	public final void assertDocInList(String[] docIdList, String doc_id, String msgPrefix) 
//			throws ParserConfigurationException, SAXException, IOException 
//	{
//		for (String id : docIdList)
//		{
//		    if (id.equals(doc_id))  return;
//		}
//		fail(msgPrefix + "doc \"" + doc_id + "\" missing from list");
//	}
	
	
    public static void deleteAllRecordsEmbedded(String testConfigFile, String solrPath )
    {
        byte[] listOfRecordsToDelete = getListOfAllRecordIdsEmbedded(testConfigFile, solrPath, false);
        
        ByteArrayInputStream in = new ByteArrayInputStream(listOfRecordsToDelete);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ByteArrayOutputStream err = new ByteArrayOutputStream();
        Map<String,String> addnlProps3 = new LinkedHashMap<String,String>();
//        addnlProps3.put("marc.delete_record_id_mapper", "001 [ ]*([A-Za-z0-9]*).*->$1");
        addnlProps3.put("solr.path", solrPath);
        CommandLineUtils.runCommandLineUtil("org.solrmarc.marc.MarcImporter", "main", in, out, err, new String[]{testConfigFile, "DELETE_ONLY"}, addnlProps3);
    }
    
    public static byte[] getListOfAllRecordIdsEmbedded(String testConfigFile, String solrPath, boolean show)
    {
        // dump the entire contents of index (don't try this at home)
        ByteArrayOutputStream out1 = new ByteArrayOutputStream();
        ByteArrayOutputStream err1 = new ByteArrayOutputStream();
        Map<String,String> addnlProps1 = new LinkedHashMap<String,String>();
        addnlProps1.put("solr.path", solrPath);
        CommandLineUtils.runCommandLineUtil("org.solrmarc.marc.SolrReIndexer", "main", null, out1, err1, new String[]{testConfigFile, "-id", "*:*", "marc_display"}, addnlProps1);
                
        // now show the list all of the records
        if (show)
        {
            try
            {
                System.out.println("testConfigFile= "+ testConfigFile + "    solrPath="+solrPath);
                System.out.println(out1.toString("UTF8"));
            }
            catch (UnsupportedEncodingException e)
            {
            }
        }
        return(out1.toByteArray());
    }

	
}