package org.solrmarc.tools;

import java.io.*;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.solr.client.solrj.SolrServerException;
import org.junit.*;
import org.solrmarc.testUtils.IndexTest;
import org.xml.sax.SAXException;


public class IndexSmokeTest extends IndexTest
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
//    	DEBUG        
//System.err.println("DEBUG: just started Jetty for IndexSmokeTest as beforeClass.");    	
//      try
//      {
//      Thread.sleep(1000 * 30); // do nothing for 30 seconds
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
    public static void stopJetty() 
    {
    	stopTestJetty();
    }
    
      
    /**
     * Creates index and asserts an expected doc is present.
     */
@Test
    public final void testForSmoke() 
    		throws ParserConfigurationException, IOException, SAXException, SolrServerException 
    {
    	initVarsForHttpTestIndexing();
        createFreshTestIxOverHTTP("double_007.xml");
    	assertDocPresent("ocm57136914 ");
    }

}
