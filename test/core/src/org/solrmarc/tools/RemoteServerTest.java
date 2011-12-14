package org.solrmarc.tools;

import static org.junit.Assert.*;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;

import org.junit.*;
import org.solrmarc.testUtils.*;
import org.xml.sax.SAXException;


public class RemoteServerTest extends IndexTest
{
//    static SolrJettyProcess solrJettyProcess = null; 
//    static int jettyProcessPort; 
//    static String testDataParentPath;
//    static String testConfigFile;
//    static String solrPath;

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
		initVarsForHttpTestIndexing();
        useBinaryRequestHandler = true;
        useStreamingProxy = false;
 		createFreshTestIxOverHTTP("mergeInput.mrc");

        // assert record was written as binary:  \\u001e is binary, #30 is non-binary
//        String resultVal = getFirstRawFieldValue("u3", "marc_display");
        String resultVal = getRawFieldByIDBob("u3", "marc_display");
System.err.println("DEBUG resultValue is " + resultVal);

		assertTrue("Remote non-streaming binary indexing doesn't get binary result", resultVal.contains("\\u001e"));
		assertTrue("Remote non-streaming binary indexing gets non-binary result", !resultVal.contains("#30"));
//		assertFalse("Record added using remote non-binary request handler unexpectedly contains #30;", resultVal.contains("#30;"));
	}
	
@Test	
	public void testSolrJNonStreamingNonBinary() 
			throws ParserConfigurationException, IOException, SAXException
	{
		initVarsForHttpTestIndexing();
        useBinaryRequestHandler = false;
        useStreamingProxy = false;
		createFreshTestIxOverHTTP("mergeInput.mrc");

        // assert record was written as non-binary:  \\u001e is binary, #30 is non-binary

//        String results = getRawFieldByIDBob(testSolrUrl, "u3", "marc_display");
//        assertTrue("Record added using remote non-binary request handler does contain \\u001e", !results.contains("\\u001e"));
//		assertTrue("Record added using remote non-binary request handler doesn't contain #30;", results.contains("#30;"));

        String resultVal = getFirstRawFieldValue("u3", "marc_display");
System.err.println("DEBUG resultValue is " + resultVal);
        assertTrue("Remote non-streaming non-binary indexing gets binary result", !resultVal.contains("\\u001e"));
		assertTrue("Remote non-streaming non-binary indexing doesn't get non-binary result", resultVal.contains("#30"));
	}

@Test	
	public void testSolrJStreamingBinary()
			throws ParserConfigurationException, IOException, SAXException
	{
		initVarsForHttpTestIndexing();
        useBinaryRequestHandler = true;
        useStreamingProxy = true;
		createFreshTestIxOverHTTP("mergeInput.mrc");

        // Check whether record was written as binary:  \\u001e is binary, #30 is non-binary
//        String results = getRawFieldByIDBob(testSolrUrl, "u3", "marc_display");
//        assertTrue("Record added using remote non-binary request handler doesn't contain \\u001e", results.contains("\\u001e"));
//        assertTrue("Record added using remote non-binary request handler does contain #30;", !results.contains("#30;"));

        // assert record was written as binary:  \\u001e is binary, #30 is non-binary
        String resultVal = getFirstRawFieldValue("u3", "marc_display");
System.err.println("DEBUG resultValue is " + resultVal);
        assertTrue("Remote streaming binary indexing doesn't get binary result", resultVal.contains("\\u001e"));
		assertTrue("Remote streaming binary indexing gets non-binary result", !resultVal.contains("#30"));
	}

@Test	
	public void testSolrJStreamingNonBinary()
			throws ParserConfigurationException, IOException, SAXException
	{
		initVarsForHttpTestIndexing();
        useBinaryRequestHandler = false;
        useStreamingProxy = true;
		createFreshTestIxOverHTTP("mergeInput.mrc");
        
        // Check whether record was not written as binary 
//        String results = getRawFieldByIDBob(testSolrUrl, "u3", "marc_display");
//        assertTrue("Record added using remote non-binary request handler does contain \\u001e", !results.contains("\\u001e"));
//        assertTrue("Record added using remote non-binary request handler doesn't contain #30;", results.contains("#30;"));
        
        // assert record was written as non-binary:  \\u001e is binary, #30 is non-binary
        String resultVal = getFirstRawFieldValue("u3", "marc_display");
System.err.println("DEBUG resultValue is " + resultVal);
        assertTrue("Remote streaming non-binary indexing gets binary result", !resultVal.contains("\\u001e"));
		assertTrue("Remote streaming non-binary indexing doesn't get non-binary result", resultVal.contains("#30"));
	}

    /**
     * unit test for index a number of records via the REMOTE http access methods.
     * then search for those records using the RemoteSolrSearcher class.
     */
//@Test
    public void testRemoteIndexRecordBob()
    {
        // index a small set of records
        URL serverURL =  null;
        try
        {
            serverURL = new URL("http", "localhost", solrJettyProcess.getJettyPort(), "/solr");
        }
        catch (Exception e)
        {
        	e.getCause().printStackTrace();
            e.printStackTrace();
        }
        
        String urlStr = serverURL.toString();
        ByteArrayOutputStream out1 = new ByteArrayOutputStream();
        ByteArrayOutputStream err1 = new ByteArrayOutputStream();
        Map<String,String> addnlProps1 = new LinkedHashMap<String,String>();
        addnlProps1.put("solr.hosturl", urlStr);
        addnlProps1.put("solr.path", "REMOTE");
       // addnlProps1.put("marc.verbose", "true");
        addnlProps1.put("solrmarc.use_binary_request_handler", "true");
        addnlProps1.put("solrmarc.use_solr_server_proxy", "true");
        addnlProps1.put("solrmarc.use_streaming_proxy", "true");
        CommandLineUtils.runCommandLineUtil("org.solrmarc.marc.MarcImporter", "main", null, out1, err1, new String[]{testConfigFname, testDataParentPath+"/mergeInput.mrc"  }, addnlProps1);
        if (out1.toByteArray().length > 0) System.out.println("Importer results: "+ new String (out1.toByteArray()));
        if (err1.toByteArray().length > 0) System.out.println("Importer results: "+ new String (err1.toByteArray()));
        
        ByteArrayOutputStream out1a = new ByteArrayOutputStream();
        ByteArrayOutputStream err1a = new ByteArrayOutputStream();
        addnlProps1.put("solrmarc.use_binary_request_handler", "false");
        addnlProps1.put("solrmarc.use_streaming_proxy", "false");
        CommandLineUtils.runCommandLineUtil("org.solrmarc.marc.MarcImporter", "main", null, out1a, err1a, new String[]{testConfigFname, testDataParentPath+"/u2103.mrc"  }, addnlProps1);
        if (out1a.toByteArray().length > 0) System.out.println("Importer results: "+ new String (out1a.toByteArray()));
        if (err1a.toByteArray().length > 0) System.out.println("Importer results: "+ new String (err1a.toByteArray()));

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
        
        //   now test SolrUpdate  to commit the changes
        ByteArrayOutputStream out5 = new ByteArrayOutputStream();
        CommandLineUtils.runCommandLineUtil("org.solrmarc.tools.SolrUpdate", "main", null, out5, new String[]{"-v", urlStr+"/update"});
        
        // now delete all of the records in the index to make test order not matter
        //    first get the entire contents of index (don't try this at home)
        ByteArrayOutputStream out6 = new ByteArrayOutputStream();
        ByteArrayOutputStream err6 = new ByteArrayOutputStream();
        CommandLineUtils.runCommandLineUtil("org.solrmarc.solr.RemoteSolrSearcher", "main", null, out6, err6, new String[]{urlStr, "id:u*", "marc_xml_display"});
//        if (out6.toByteArray().length > 0) System.out.println("RemoteSolrSearcher results: "+ new String (out6.toByteArray()));
//        if (err6.toByteArray().length > 0) System.out.println("RemoteSolrSearcher results: "+ new String (err6.toByteArray()));
        
        //    next extract the ids from the returned records 
        ByteArrayInputStream in7 = new ByteArrayInputStream(out6.toByteArray());
        ByteArrayOutputStream out7 = new ByteArrayOutputStream();
        ByteArrayOutputStream err7 = new ByteArrayOutputStream();
        CommandLineUtils.runCommandLineUtil("org.solrmarc.marc.MarcPrinter", "main", in7, out7, err7, new String[]{testConfigFname, "print", "001"}, addnlProps1);
        String printidsResult = new String(out7.toByteArray());
        if (!printidsResult.matches("001 u1[\\r]?[\\n]001 u3[\\r]?[\\n]001 u4[\\r]?[\\n]001 u7[\\r]?[\\n]001 u8[\\r]?[\\n]001 u10[\\r]?[\\n]001 u2103[\\r]?[\\n]"))
        {
            System.out.println("Index should contain records u1, u3, u4, u7, u8, u10 and u2103, instead it has "+printidsResult.replaceAll("[\\r]?[\\n]", "").replaceFirst("001 ", "").replaceAll("001", ","));
        }
        assertTrue("Index should contain records u1, u3, u4, u7, u8, u10 and u2103, instead it has "+printidsResult.replaceAll("[\\r]?[\\n]", "").replaceFirst("001 ", "").replaceAll("001", ","),
                   (printidsResult.matches("001 u1[\\r]?[\\n]001 u3[\\r]?[\\n]001 u4[\\r]?[\\n]001 u7[\\r]?[\\n]001 u8[\\r]?[\\n]001 u10[\\r]?[\\n]001 u2103[\\r]?[\\n]")));                                                           
//        if (out7.toByteArray().length > 0) System.out.println("IDs to delete: "+ new String (out7.toByteArray()));
//        if (err7.toByteArray().length > 0) System.out.println("IDs to delete: "+ new String (err7.toByteArray()));

        //    now delete all of the records (but don't commit)
//        System.setProperty("marc.delete_record_id_mapper", "001 u?([0-9]*).*->u$1");
        ByteArrayInputStream in8 = new ByteArrayInputStream(out7.toByteArray());
        ByteArrayOutputStream out8 = new ByteArrayOutputStream();
        ByteArrayOutputStream err8 = new ByteArrayOutputStream();
        Map<String,String> addnlProps8 = new LinkedHashMap<String,String>();
        addnlProps8.put("marc.delete_record_id_mapper", "001 u?([0-9]*).*->u$1");
        addnlProps8.put("solr.hosturl", urlStr);
        addnlProps8.put("solr.path", "REMOTE");
        addnlProps8.put("marc.verbose", "true");
        CommandLineUtils.runCommandLineUtil("org.solrmarc.marc.MarcImporter", "main", in8, out8, err8, new String[]{testConfigFname, "DELETE_ONLY", "-nocommit"}, addnlProps8);
//        if (out8.toByteArray().length > 0) System.out.println("Importer results: "+ new String (out8.toByteArray()));
//        if (err8.toByteArray().length > 0) System.out.println("Importer results: "+ new String (err8.toByteArray()));

        //   then check that the index is NOT empty yet (because we didn't commit)
        ByteArrayOutputStream out9 = new ByteArrayOutputStream();
        ByteArrayOutputStream err9 = new ByteArrayOutputStream();
        CommandLineUtils.runCommandLineUtil("org.solrmarc.solr.RemoteSolrSearcher", "main", null, out9, err9, new String[]{urlStr, "id:u*", "marc_display"});

        CommandLineUtils.assertArrayEquals("record dump before and after delete but no commit ", out9.toByteArray(), out6.toByteArray()); 
        
        //   now test SolrUpdate  to commit the changes
        ByteArrayOutputStream out11 = new ByteArrayOutputStream();
        CommandLineUtils.runCommandLineUtil("org.solrmarc.tools.SolrUpdate", "main", null, out11, new String[]{"-v", urlStr+"/update"});

//        if (out11.toByteArray().length > 0) System.out.println("Final record is: "+ new String (out11.toByteArray()));
        assertTrue("Remote update was unsuccessful ", new String(out11.toByteArray()).contains("<int name=\"status\">0</int>"));

        //   lastly check that the index is NOW empty
        ByteArrayOutputStream out10 = new ByteArrayOutputStream();
        ByteArrayOutputStream err10 = new ByteArrayOutputStream();
        CommandLineUtils.runCommandLineUtil("org.solrmarc.solr.RemoteSolrSearcher", "main", null, out10, err10, new String[]{urlStr, "id:u*", "marc_xml_display"});
//        if (out10.toByteArray().length > 0) System.out.println("RemoteSolrSearcher results: "+ new String (out10.toByteArray()));
//        if (err10.toByteArray().length > 0) System.out.println("RemoteSolrSearcher results: "+ new String (err10.toByteArray()));

        System.out.println("Final check record size is: "+ out10.toByteArray().length);
//        if (out10.toByteArray().length > 0) System.out.println("Final record is: "+ new String (out10.toByteArray()));
        CommandLineUtils.assertArrayEquals("record dump via RemoteSolrSearcher, and empty record ", out10.toByteArray(), new byte[0]); 

        System.out.println("Test testRemoteIndexRecord is successful");
    }
    
}

