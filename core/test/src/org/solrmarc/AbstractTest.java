package org.solrmarc;

import java.io.*;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.solr.client.solrj.SolrServerException;
import org.junit.BeforeClass;
import org.solrmarc.testUtils.IndexTest;
import org.solrmarc.testUtils.SolrFieldMappingTest;
import org.xml.sax.SAXException;

/**
 * @author ndushay
 *
 */
/**
 * Instance variables and methods used for testing
 * @author Naomi Dushay
 */
public abstract class AbstractTest extends IndexTest
{

	/** testDataParentPath is used for mapping tests - full path is needed */
    protected String testDataParentPath = null;

	/** SolrFieldMappingTest object to be used in specific tests */
	protected SolrFieldMappingTest solrFldMapTest = null;

	// set up required properties when tests not invoked via Ant
	// hardcodings below are only used when the tests are invoked without the
	//  properties set (e.g. from eclipse)
	{
        String testJettyDir = System.getProperty("test.jetty.dir");
        if (testJettyDir == null)
        {
        	testJettyDir = "test" + File.separator + "jetty";
            System.setProperty("test.jetty.dir", testJettyDir);
        }

        String testSolrPath = System.getProperty("test.solr.path");
        if (testSolrPath == null)
            System.setProperty("test.solr.path", testJettyDir + File.separator + "solr");

        // used to find core translation_maps
        if (System.getProperty("solrmarc.path") == null)
            System.setProperty("solrmarc.path", new File("core").getAbsolutePath());
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

		// these properties must be set or MarcHandler can't initialize properly
		System.setProperty("marc.source", "FILE");
		// needed to get through initialization; overridden in individual tests
		System.setProperty("marc.path", testDataParentPath + File.separator + "pubDateTests.mrc");
        String testConfigFname = getRequiredSystemProperty("test.config.file");
        try
        {
    		solrFldMapTest = new SolrFieldMappingTest(testConfigFname, docIDfname);
        }
        catch (FileNotFoundException e)
        {
        	e.printStackTrace();
        	System.exit(666);
        }
	}


	/**
	 * creates a fresh index from the indicated test file, and initializes
	 *  necessary variables
	 * @throws SolrServerException when can't delete all docs before writing new docs
	 */
	public void createFreshIx(String testDataFname)
		throws ParserConfigurationException, IOException, SAXException, SolrServerException
	{
        String testSolrUrl = getLocalTestingSolrUrl();

		if (solrJettyProcess == null)
			startTestJetty();

		createFreshTestIxOverHTTP(testSolrUrl, testDataFname);
	}


	/**
	 * creates a fresh index from the indicated test file, and initializes
	 *  necessary variables
	 * @throws SolrServerException when can't delete all docs before writing new docs
	 */
	public void createFreshIx(String testDataFname, boolean useBinaryRequestHandler, boolean useStreamingProxy)
		throws ParserConfigurationException, IOException, SAXException, SolrServerException
	{
        String testSolrUrl = getLocalTestingSolrUrl();

		if (solrJettyProcess == null)
			startTestJetty();

		createFreshTestIxOverHTTP(testSolrUrl, testDataFname, useBinaryRequestHandler, useStreamingProxy);
	}


	/**
	 * updates an existing index from the indicated test file, and initializes
	 *  necessary variables
	 */
	public void updateIx(String testDataFname)
		throws ParserConfigurationException, IOException, SAXException
	{
        String testSolrUrl = getLocalTestingSolrUrl();

		if (solrJettyProcess == null)
			startTestJetty();

		updateTestIxOverHTTP(testSolrUrl, testDataFname);
	}


	private String getLocalTestingSolrUrl()
	{
		String testSolrUrl = System.getProperty("test.solr.url");
		if (testSolrUrl == null || testSolrUrl.length() < 7)
		{
			String testJettyPortStr = System.getProperty("test.jetty.port");
	        if (testJettyPortStr == null)
	        {
	        	testJettyPortStr = "8983";
	        	System.setProperty("test.jetty.port", testJettyPortStr);
	        }
	        testSolrUrl = "http://localhost:" + testJettyPortStr + "/solr";
		}

		return testSolrUrl;
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

        String testSolrUrl = getLocalTestingSolrUrl();

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
