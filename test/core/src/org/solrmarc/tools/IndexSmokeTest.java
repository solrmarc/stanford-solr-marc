package org.solrmarc.tools;

import java.io.*;

import javax.xml.parsers.ParserConfigurationException;

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
    public final void testForSmoke() 
    		throws ParserConfigurationException, IOException, SAXException 
    {
        initVarsForHttpTestIndexing();
        createFreshTestIxOverHTTP("double_007.xml");
    	assertDocPresent("ocm57136914 ");
    }

}
