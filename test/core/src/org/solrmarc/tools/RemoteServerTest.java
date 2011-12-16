package org.solrmarc.tools;

import static org.junit.Assert.*;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import org.junit.*;
import org.solrmarc.testUtils.IndexTest;
import org.xml.sax.SAXException;

/**
 * 
 * @author Naomi Dushay, based on code by Bob Haschart
 */
public class RemoteServerTest extends IndexTest
{

    /**
     * Start a Jetty driven solr server running in a separate JVM at port jetty.test.port
     *  and set the logging levels
     */
@BeforeClass
    public static void setupTestClass() 
    {
		setTestLoggingLevels();
    	startTestJetty();
    }
    
    
    /**
     * Stop the Jetty server we spun up for testing
     */
@AfterClass
    public static void stopJetty()
    {
    	stopTestJetty();
    	closeSolrProxy();
    }
    

// TODO: redundant with IndexSmokeTest class
	/**
	 * smoke test for indexing via HTTP
	 */
@Test
	public void testRemoteIndexRecord() 
			throws ParserConfigurationException, IOException, SAXException
	{
		initVarsForHttpTestIndexing();
        createFreshTestIxOverHTTP("double_007.mrc");
    	assertDocPresent("ocm57136914 ");
	}

    /**
     * testing update of existing index via HTTP
     */
@Test
	public void testRemoteUpdateIndex()
			throws ParserConfigurationException, IOException, SAXException
	{
		initVarsForHttpTestIndexing();
		createFreshTestIxOverHTTP("mergeInput.mrc");
		updateTestIxOverHTTP("u2103.mrc");
		
		assertDocPresent("u2103");
    	assertDocPresent("u3"); // from mergeInput.mrc
	}
	

// TODO:  rewrite this to something decent.
	public void testIndexFormats()
			throws ParserConfigurationException, IOException, SAXException
	{
		initVarsForHttpTestIndexing();
		createFreshTestIxOverHTTP("mergeInput.mrc");
		updateTestIxOverHTTP("u2103.mrc");

		// get record u3 raw from test data file

		// get record u3 raw from index
		// compare results
		
        // retrieve record u3 from the index as XML
		// compare results

        // retrieve record u3 from the index as JSON
		// compare results

		// YUCK:  should call the java class as a java class, not from command line
/*		
        // retrieve record u3 from the index
        ByteArrayOutputStream out2 = new ByteArrayOutputStream();
        ByteArrayOutputStream err2 = new ByteArrayOutputStream();
        CommandLineUtils.runCommandLineUtil("org.solrmarc.solr.RemoteSolrSearcher", "main", null, out2, err2, new String[]{urlStr, "id:u3", "marc_display"});
        
        // retrieve record u3 from the original input file
        ByteArrayOutputStream out3 = new ByteArrayOutputStream();
        CommandLineUtils.runCommandLineUtil("org.solrmarc.marc.RawRecordReader", "main", null, out3, new String[]{testDataParentPath+"/mergeInput.mrc", "u3" });
        
        // compare the results
        CommandLineUtils.assertArrayEquals("record via GetFromSolr(raw), and record via GetRecord ", out2.toByteArray(), out3.toByteArray());
        
        // retrieve record u3 from the index as XML
        ByteArrayOutputStream out4 = new ByteArrayOutputStream();
        ByteArrayOutputStream err4 = new ByteArrayOutputStream();
        CommandLineUtils.runCommandLineUtil("org.solrmarc.solr.RemoteSolrSearcher", "main", null, out4, err4, new String[]{urlStr, "id:u3", "marc_xml_display"});
        
        // compare the results
        CommandLineUtils.assertArrayEquals("record via GetFromSolr(XML), and record via GetRecord ", out3.toByteArray(), out4.toByteArray());
        
        // retrieve record u3 from the index as JSON
        ByteArrayOutputStream out4a = new ByteArrayOutputStream();
        ByteArrayOutputStream err4a = new ByteArrayOutputStream();
        CommandLineUtils.runCommandLineUtil("org.solrmarc.solr.RemoteSolrSearcher", "main", null, out4a, err4a, new String[]{urlStr, "id:u3", "marc_json_display"});
        
        // compare the results
        CommandLineUtils.assertArrayEquals("record via GetFromSolr(JSON), and record via GetRecord ", out3.toByteArray(), out4a.toByteArray());
        //System.out.println("Test testRemoteIndexRecord is successful");
*/		
		
		fail("testIndexFormats not yet implemented");
	}

@Test	
	public void testSolrJNonStreamingBinary()
			throws ParserConfigurationException, IOException, SAXException
	{
		closeSolrProxy();  // need to reset the solrProxy to get the right request handling
		initVarsForHttpTestIndexing();
        useBinaryRequestHandler = true;
        useStreamingProxy = false;
 		createFreshTestIxOverHTTP("mergeInput.mrc");

        // assert record was written as binary:  \\u001e is binary, #30 is non-binary
        String resultVal = getFldValPreserveBinary("u3", "marc_display");
		assertTrue("Remote non-streaming binary indexing doesn't get binary result", resultVal.contains("\\u001e"));
		assertTrue("Remote non-streaming binary indexing gets non-binary result", !resultVal.contains("#30"));
	}
	
@Test	
	public void testSolrJNonStreamingNonBinary() 
			throws ParserConfigurationException, IOException, SAXException
	{
		closeSolrProxy();  // need to reset the solrProxy to get the right request handling
		initVarsForHttpTestIndexing();
        useBinaryRequestHandler = false;
        useStreamingProxy = false;
		createFreshTestIxOverHTTP("mergeInput.mrc");

        // assert record was written as non-binary:  \\u001e is binary, #30 is non-binary
		String resultVal = getFldValPreserveBinary("u3", "marc_display");
        assertTrue("Remote non-streaming non-binary indexing gets binary result", !resultVal.contains("\\u001e"));
		assertTrue("Remote non-streaming non-binary indexing doesn't get non-binary result", resultVal.contains("#30"));
	}

@Test	
	public void testSolrJStreamingBinary()
			throws ParserConfigurationException, IOException, SAXException
	{
		closeSolrProxy();  // need to reset the solrProxy to get the right request handling
		initVarsForHttpTestIndexing();
        useBinaryRequestHandler = true;
        useStreamingProxy = true;
		createFreshTestIxOverHTTP("mergeInput.mrc");

        // assert record was written as binary:  \\u001e is binary, #30 is non-binary
		String resultVal = getFldValPreserveBinary("u3", "marc_display");
        assertTrue("Remote streaming binary indexing doesn't get binary result", resultVal.contains("\\u001e"));
		assertTrue("Remote streaming binary indexing gets non-binary result", !resultVal.contains("#30"));
	}

@Test	
	public void testSolrJStreamingNonBinary()
			throws ParserConfigurationException, IOException, SAXException
	{
		closeSolrProxy();  // need to reset the solrProxy to get the right request handling
		initVarsForHttpTestIndexing();
        useBinaryRequestHandler = false;
        useStreamingProxy = true;
		createFreshTestIxOverHTTP("mergeInput.mrc");
        
        // assert record was written as non-binary:  \\u001e is binary, #30 is non-binary
		String resultVal = getFldValPreserveBinary("u3", "marc_display");
        assertTrue("Remote streaming non-binary indexing gets binary result", !resultVal.contains("\\u001e"));
		assertTrue("Remote streaming non-binary indexing doesn't get non-binary result", resultVal.contains("#30"));
	}
    
}

