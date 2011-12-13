package org.solrmarc.tools;

import static org.junit.Assert.fail;

import java.io.*;

import javax.xml.parsers.ParserConfigurationException;

import org.junit.*;
import org.solrmarc.testUtils.IndexTest;
import org.xml.sax.SAXException;


public class IndexSmokeTest extends IndexTest
{
    /**
     * Start a Jetty driven solr server running in a separate JVM at port jetty.test.port
     */
@BeforeClass
    public static void startJetty() 
    {    	
    	startTestJetty();
//    	DEBUG        
//System.err.println("DEBUG: just started Jetty for IndexSmokeTest as beforeClass.");    	
//      try
//      {
//      Thread.sleep(1000 * 60); // do nothing for 1000 miliseconds (1 second)
//      }
//      catch(InterruptedException e)
//      {
//      e.printStackTrace();
//      }
    }

    /**
     * Stop the Jetty server we spun up for testing
     */
@AfterClass
    public static void stopJetty() throws Exception 
    {
    	stopTestJetty();
    }
    
      
    /**
     * Creates index and asserts an expected doc is present.
     */
@Test
    public final void testForSmoke() throws ParserConfigurationException, IOException, SAXException 
    {
        createIxInitVars("double_007.xml");
    	this.assertDocPresent("ocm57136914 ");
    }

    /**
     * creates an index from the indicated test file, and initializes 
     *  necessary variables
     */
    private void createIxInitVars(String testDataFname) throws ParserConfigurationException, IOException, SAXException 
    {
        docIDfname = "id";
        
//    	String testDataPath = System.getProperty("test.data.path");
//        if (testDataPath == null)
//        {
//            testDataPath = "test" + File.separator + "core" + File.separator + "data";
//            System.setProperty("test.data.path", testDataPath);
//        }
//    	String testConfigFile = System.getProperty("test.config.file");
//        if (testConfigFile == null)
//        {
//        	testConfigFile = testDataPath + File.separator + "smoketest" + File.separator + "test_config.properties";
//            System.setProperty("test.config.file", testConfigFile);
//        }
//        String testSolrUrl = System.getProperty("test.solr.url");
//        if (testSolrUrl == null)
//        {
//        	testSolrUrl = "http://localhost:8984/solr";
//            System.setProperty("test.solr.url", testSolrUrl);
//        }    

        String testDataParentPath = System.getProperty("test.data.path");
        if (testDataParentPath == null)
            fail("property test.data.path must be defined for the tests to run");
    
        String testConfigFname = System.getProperty("test.config.file");
        if (testConfigFname == null)
            fail("property test.config.file must be defined for the tests to run");
        
        String testSolrUrl = System.getProperty("test.solr.url");
        if (testSolrUrl == null)
            fail("property test.solr.url must be defined for the tests to run");
        System.setProperty("solr.hosturl", testSolrUrl);
        boolean useBinaryRequestHandler = Boolean.valueOf(System.getProperty("core.test.use_streaming_proxy"));
        boolean useStreamingProxy= Boolean.valueOf(System.getProperty("core.test.use_binary_request_handler"));

//        createIxInitVarsDistSM2_3_1(testConfigFname, solrPath, solrDataDir, testDataParentPath, testDataFname);
        createFreshIxOverHTTP(testConfigFname, testSolrUrl, useBinaryRequestHandler, useStreamingProxy, testDataParentPath, testDataFname);
    }

}
