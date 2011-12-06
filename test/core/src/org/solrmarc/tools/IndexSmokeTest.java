package org.solrmarc.tools;

import static org.junit.Assert.fail;

import java.io.*;

import org.junit.Test;
import org.marc4j.MarcStreamReader;
import org.marc4j.marc.Record;
import org.solrmarc.testUtils.IndexTest;


public class IndexSmokeTest extends IndexTest
{
    private final String testDataFname = "selectedRecs.mrc";
    
    /**
     * Test assignment of Book format
     *   includes monographic series
     */
@Test
    public final void testForSmoke() 
    {
        createIxInitVars(testDataFname);
        String testDataParentPath = System.getProperty("test.data.path");
        if (testDataParentPath == null)
            fail("property test.data.path must be defined for the tests to run");

        MarcStreamReader reader = null;
        try
        {
            reader = new MarcStreamReader(new FileInputStream(testDataParentPath + File.separator + testDataFname));
        }
        catch (FileNotFoundException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        while (reader != null && reader.hasNext())
        {
            Record rec = reader.next();
            String id = rec.getControlNumber();
            if (id != null)
            {
                assertDocPresent(id);
            }
        }
        System.out.println("Test testForSmoke is successful");
    }

    /**
     * creates an index from the indicated test file, and initializes 
     *  necessary variables
     */
    private void createIxInitVars(String testDataFname) 
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
//    	String solrPath = System.getProperty("solr.path");
//        if (solrPath == null)
//        {
//        	solrPath = testDataPath + File.separator + "smoketest" + File.separator + "solr";
//            System.setProperty("solr.path", testConfigFile);
//        }
//    	String solrDataDir = System.getProperty("solr.data.dir");
//
//        createIxInitVarsDistSM2_3_1(testConfigFile, solrPath, solrDataDir, testDataPath, testDataFname);

        
    
        String solrPath = System.getProperty("solr.path");
        String solrDataDir = System.getProperty("solr.data.dir");
        if (solrPath == null)
            fail("property solr.path must be defined for the tests to run");
    
        String testDataParentPath = System.getProperty("test.data.path");
        if (testDataParentPath == null)
            fail("property test.data.path must be defined for the tests to run");
    
        String testConfigFname = System.getProperty("test.config.file");
        if (testConfigFname == null)
            fail("property test.config.file must be defined for the tests to run");

        createIxInitVarsDistSM2_3_1(testConfigFname, solrPath, solrDataDir, testDataParentPath, testDataFname);
    
    //  createNewTestIndex(testDataParentPath + File.separator + testDataFname, configPropFile, solrPath, solrDataDir, solrmarcPath, siteSpecificPath);
    //  solrCore = getSolrCore(solrPath, solrDataDir);
    //  sis = getSolrIndexSearcher(solrCore);
    }
       


}
