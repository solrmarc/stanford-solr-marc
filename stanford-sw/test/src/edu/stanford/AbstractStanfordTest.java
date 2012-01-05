package edu.stanford;

import java.io.*;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;


import org.apache.solr.client.solrj.SolrServerException;
import org.junit.BeforeClass;
import org.solrmarc.testUtils.IndexTest;
import org.solrmarc.testUtils.SolrFieldMappingTest;

/**
 * Site Specific code used for testing the Stanford Blacklight index
 * @author Naomi Dushay
 */
public abstract class AbstractStanfordTest extends IndexTest {

// FIXME:  ensure log4j.properties is in bin	
	
// FIXME: is there a better way of allowing tests to run in eclipse without invoking ant?
//  can we read build.props?
	
	/** testDataParentPath is used for mapping tests - full path is needed */
    String testDataParentPath = null;
	
	/** SolrFieldMappingTest object to be used in specific tests */
	protected SolrFieldMappingTest solrFldMapTest = null;

    
	// hardcodings below are only used when the tests are invoked without the
	//  properties set
	// the properties ARE set when the tests are invoke via ant.
	{
        // !!! NOTE:  this one is local and must be modified for your installation !!!
        String solrPath = System.getProperty("solr.path");
        if (solrPath == null)
        {
//            solrPath = "home" + File.separator + "solrmarc" + File.separator
//                    + "jetty" + File.separator + "solr";
    		solrPath = "test" + File.separator + "solr";
            System.setProperty("solr.path", new File(solrPath).getAbsolutePath());
        }

        // used to find core translation_maps
        if (System.getProperty("solrmarc.path") == null)
            System.setProperty("solrmarc.path", new File("setup" + File.separator + "core").getAbsolutePath());
		
        String stanfordswSitePath = "stanford-sw";

		if (System.getProperty("solrmarc.site.path") == null)
            System.setProperty("solrmarc.site.path", new File(stanfordswSitePath).getAbsolutePath());
		
        
        String configPropDir = System.getProperty("test.config.dir");
        if (configPropDir == null)
            configPropDir = stanfordswSitePath;
        
        String configPropFile = System.getProperty("test.config.file");
		if (configPropFile == null) {
		    configPropFile = new File(configPropDir, "sw_config.properties").getAbsolutePath();
            System.setProperty("test.config.file", configPropFile);
		}
		
		testDataParentPath = System.getProperty("test.data.path");
        if (testDataParentPath == null)
        {
            testDataParentPath = System.getProperty("test.data.parent.path");
            if (testDataParentPath == null)
                testDataParentPath = stanfordswSitePath + File.separator + "test" + File.separator + "data";
            System.setProperty("test.data.path", new File(testDataParentPath).getAbsolutePath());
        }
		
		if (System.getProperty("solr.data.dir") == null)
			System.setProperty("solr.data.dir", new File(solrPath + File.separator + "data").getAbsolutePath());		
	}

@BeforeClass
	public static void setLoggingLevels() 
	{
		setTestLoggingLevels();
	}
	
	/**
	 * initialization for mapping tests
	 */
	public void mappingTestInit() 
	{
		docIDfname = "id";		
		String siteDir = "examples" + File.separator + "stanford";

		String anyTestFile = null;
		String testDataParentPath = System.getProperty("test.data.path");
		if (testDataParentPath != null)
			anyTestFile = new File(testDataParentPath, "pubDateTests.mrc").getAbsolutePath();
		else
        {
//            fail("property test.data.path must be defined for the tests to run");
    		String marcFileDir = siteDir + File.separator + 
    										"test" + File.separator +
    										"data" + File.separator;
    		anyTestFile = marcFileDir + "pubDateTests.mrc";
        }

		// these properties must be set or MarcHandler can't initialize properly
		System.setProperty("marc.source", "FILE");
		// needed to get through initialization; overridden in individual tests
		System.setProperty("marc.path", anyTestFile);
        String testConfigFname = getRequiredSystemProperty("test.config.file");
		solrFldMapTest = new SolrFieldMappingTest(testConfigFname, docIDfname);
	}


	/**
	 * creates a fresh index from the indicated test file, and initializes 
	 *  necessary variables
	 * @throws SolrServerException when can't delete all docs before writing new docs
	 */
	public void createFreshIx(String testDataFname) 
		throws ParserConfigurationException, IOException, SAXException, SolrServerException 
	{
		docIDfname = "id";

        String testDataParentPath = System.getProperty("test.data.path");
        String testConfigFname = getRequiredSystemProperty("test.config.file");

        String testJettyPortStr = System.getProperty("test.jetty.port");
        if (testJettyPortStr == null)
        {
        	testJettyPortStr = "8983";
        	System.setProperty("test.jetty.port", testJettyPortStr);
        }
        String testSolrUrl = "http://localhost:" + testJettyPortStr + "/solr";

		if (solrJettyProcess == null)
			startTestJetty();

// FIXME:  set up vars and use the single argument version?	
		createFreshTestIxOverHTTP(testConfigFname, testSolrUrl, useBinaryRequestHandler, useStreamingProxy, testDataParentPath, testDataFname);
	}
	
	/**
	 * updates an existing index from the indicated test file, and initializes 
	 *  necessary variables
	 */
	public void updateIx(String testDataFname) 
		throws ParserConfigurationException, IOException, SAXException 
	{
		testConfigFname = getRequiredSystemProperty("test.config.file");
		testDataParentPath = getRequiredSystemProperty("test.data.path");

        String testJettyPortStr = System.getProperty("test.jetty.port");
        if (testJettyPortStr == null)
        {
        	testJettyPortStr = "8983";
        	System.setProperty("test.jetty.port", testJettyPortStr);
        }
        String testSolrUrl = "http://localhost:" + testJettyPortStr + "/solr";

		if (solrJettyProcess == null)
			startTestJetty();

// FIXME:  set up vars and use the single argument version?		
        updateTestIxOverHTTP(testConfigFname, testSolrUrl, useBinaryRequestHandler, useStreamingProxy, testDataParentPath, testDataFname);
	}
	
	/**
	 * removes records from the index
	 *  @param deletedIdsFilename - name of file containing record ids to be deleted
	 */
	public void deleteIxDocs(String deletedIdsFilename) 
		throws ParserConfigurationException, IOException, SAXException 
	{
        String testConfigFname = getRequiredSystemProperty("test.config.file");
		String testDataParentPath = getRequiredSystemProperty("test.data.path");

        String testJettyPortStr = System.getProperty("test.jetty.port");
        if (testJettyPortStr == null)
        {
        	testJettyPortStr = "8983";
        	System.setProperty("test.jetty.port", testJettyPortStr);
        }
        String testSolrUrl = "http://localhost:" + testJettyPortStr + "/solr";

		// these properties must be set or MarcHandler can't initialize properly
		// needed to get through initialization; overridden in individual tests
		String anyTestFile = new File(testDataParentPath, "pubDateTests.mrc").getAbsolutePath();
		System.setProperty("marc.source", "FILE");
		System.setProperty("marc.path", anyTestFile);

		if (solrJettyProcess == null)
			startTestJetty();

		deleteRecordsFromTestIx(deletedIdsFilename, testSolrUrl, testConfigFname);
	}
	
}
