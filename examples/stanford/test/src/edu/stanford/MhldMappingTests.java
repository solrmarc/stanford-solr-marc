package edu.stanford;

import java.io.File;

import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;
import org.solrmarc.tools.LoggerAppender4Testing;

public class MhldMappingTests extends AbstractStanfordTest 
{
	private String fldName = "mhld_display";
	private String testFilePath = testDataParentPath + File.separator;

@Before
	public final void setup() 
	{
		mappingTestInit();
	}	

    /**
     * per spec in email by Naomi Dushay on July 12, 2011, an MHLD is skipped
     *  if 852 sub z  says "All holdings transfered" 
     */
@Test
    public final void testSkippedMhlds() 
    {
		String testDataFile = testFilePath + "mhldDisplay852only.mrc";
    	solrFldMapTest.assertSolrFldHasNumValues(testDataFile, "362573", fldName, 3);
    	solrFldMapTest.assertSolrFldValue(testDataFile, "362573", fldName, "GREEN -|- MEDIA-MTXT -|- CDs and DVDs -|-  -|- ");
    	solrFldMapTest.assertSolrFldValue(testDataFile, "362573", fldName, "SAL3 -|- STACKS -|-  -|-  -|- ");
    	solrFldMapTest.assertSolrFldValue(testDataFile, "362573", fldName, "GREEN -|- CURRENTPER -|- Latest yr. (or vol.) in CURRENT PERIODICALS; earlier in STACKS -|-  -|- ");

		solrFldMapTest.assertSolrFldHasNoValue(testDataFile, "3974376", fldName, "GREEN -|- STACKS -|-  -|-  -|- ");
		solrFldMapTest.assertSolrFldHasNumValues(testDataFile, "3974376", fldName, 0);
		solrFldMapTest.assertNoSolrFld(testDataFile, "3974376", fldName);
    }


    /**
     * per spec in email by Naomi Dushay on July 12, 2011, an MHLD summary holdings section
     *  is skipped if 866 has ind2 of 0 and 852 has sub = 
     */
@Test
    public final void testSkipped866() 
    {
		String testDataFile = testFilePath + "mhldDisplay86x.mrc";
		
		// skip if 2nd indicator '0'  and 852 sub '=' exists
		String valueStart = "GREEN -|- CURRENTPER -|- Latest yr. (or vol.) in CURRENT PERIODICALS; earlier in STACKS -|-  -|- ";
    	solrFldMapTest.assertSolrFldHasNoValue(testDataFile, "362573", fldName, valueStart + "V. 417 NO. 1A (JAN 2011)");
    	solrFldMapTest.assertSolrFldHasNoValue(testDataFile, "362573", fldName, valueStart + "V. 417 NO. 4A (FEB 2011)");
    	solrFldMapTest.assertSolrFldHasNoValue(testDataFile, "362573", fldName, valueStart + "V. 417 NO. 5A (FEB 2011)");
    	solrFldMapTest.assertSolrFldHasNoValue(testDataFile, "362573", fldName, valueStart + "V. 417 NO. 20A (JUN 2011)");
    	solrFldMapTest.assertSolrFldHasNoValue(testDataFile, "362573", fldName, valueStart + "V. 417 NO. 21A (JUN 2011)");
    	solrFldMapTest.assertSolrFldHasNoValue(testDataFile, "362573", fldName, valueStart + "V. 417 NO. 22A (JUN 2011)");
    	solrFldMapTest.assertSolrFldHasNoValue(testDataFile, "362573", fldName, valueStart + "V. 417 NO. 23A (JUN 2011)"); 	
		
		// 2nd indicator "0" and no 852 sub =
    	solrFldMapTest.assertSolrFldValue(testDataFile, "358725", fldName, valueStart + "[18-38, 1922-42]; 39, 1943-");
    }
    

    /**
     * per spec in email by Naomi Dushay on July 12, 2011, if there are multiple
     *  866 with ind2 '0' and 852 sub '=' exists, then there should be an indexing error message
     */
@Test
    public final void testErrorMessageConditions() 
    {
    	String testDataFile = testFilePath + "mhldDisplay86x.mrc";
        LoggerAppender4Testing appender = new LoggerAppender4Testing();
    	MhldUtils.logger.addAppender(appender);
        try 
        {
            Logger.getLogger(MhldUtils.class).info("Test");
        	solrFldMapTest.assertSolrFldHasNoValue(testDataFile, "362573", fldName, "ignore");
            // message goes to logger ...
            appender.assertLogContains("Record 362573 has multiple 866 with ind2=0 and 852 sub=");
        }
        finally 
        {
        	MhldUtils.logger.removeAppender(appender);
        }
    }


	public void testCreateIx()
	{
		try
		{
			createIxInitVars("mhldDisplay86x.mrc");
			assertDocHasFieldValue("358725", fldName, "[18-38, 1922-42]; 39, 1943-");
			
		}
		catch (Exception e) {}
		
	}

}
