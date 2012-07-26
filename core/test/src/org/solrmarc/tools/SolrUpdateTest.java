package org.solrmarc.tools;

import java.io.*;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.solr.client.solrj.SolrServerException;
import org.junit.*;
import org.solrmarc.testUtils.IndexTest;
import org.xml.sax.SAXException;


/**
 *
 * @author Naomi Dushay
 */
public class SolrUpdateTest extends IndexTest
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
//System.err.println("DEBUG: just started Jetty for SolrUpdateTest as beforeClass.");
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
    public static void stopJetty() throws Exception
    {
    	stopTestJetty();
    }


	/**
	 * test the SolrUpdate class which is used to send a commit message to Solr
	 *  NOTE:  Naomi thinks SolrUpdate class can be replaced with a simple
	 *    call to solrProxy.commit()
	 *
	 * when is SolrUpdate used?  only when MarcImporter.main() is called.
	 * MarcImporter is the class executed in the SolrMarc.jar
	 *  MarcImporter.main() calls
	 *   MarcImporter.handleAll() which calls
	 *    MarcImporter.signalServer  which calls, under certain circumstances,
	 *    org.solrmarc.tools.SolrUpdate.sendCommitToSolr()
	 */
@Test
    public void testSolrUpdate()
    		throws IOException, ParserConfigurationException, SAXException, SolrServerException
    {
		closeSolrProxy();  // need to reset the solrProxy to get the right request handling
	    useBinaryRequestHandler = true;
	    useStreamingProxy = false;
		createFreshTestIxOverHTTPNoCommit("u2103.mrc");

		assertDocNotPresent("u2103");

		SolrUpdate.sendCommitToSolr(testSolrUrl + "/update");
		assertDocPresent("u2103");
    }


}
