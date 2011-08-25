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
    	solrFldMapTest.assertSolrFldValue(testDataFile, "362573", fldName, "GREEN -|- CURRENTPER -|- Latest yr. (or vol.) in CURRENT PERIODICALS; earlier in STACKS -|-  -|-  -|- ");

		solrFldMapTest.assertSolrFldHasNoValue(testDataFile, "3974376", fldName, "GREEN -|- STACKS -|-  -|-  -|- ");
		solrFldMapTest.assertSolrFldHasNumValues(testDataFile, "3974376", fldName, 0);
		solrFldMapTest.assertNoSolrFld(testDataFile, "3974376", fldName);
    }


    /**
     * per spec in email by Naomi Dushay on July 12, 2011, an MHLD summary holdings section
     *  is skipped if 866 has ind2 of 0 and 852 has a sub = 
     */
@Test
    public final void testSkipped866() 
    {
		String testDataFile = testFilePath + "mhldDisplay86x.mrc";
		
		// skip if 2nd indicator '0'  and 852 sub '=' exists
		String valueStart = "GREEN -|- CURRENTPER -|- Latest yr. (or vol.) in CURRENT PERIODICALS; earlier in STACKS -|-  -|-  -|- ";
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
     * per spec in email by Naomi Dushay on July 12, 2011, an MHLD summary holdings section
     *  is skipped if 867 has ind2 of 0 and 852 has a sub = 
     */
@Test
    public final void testSkipped867() 
    {
    	String testDataFile = testFilePath + "mhldDisplay867.mrc";
    	
    	// skip if 2nd indicator '0'  and 852 sub '=' exists
    	String valueStart = "GREEN -|- STACKS -|-  -|- Supplement -|-  -|- ";
    	solrFldMapTest.assertSolrFldHasNoValue(testDataFile, "skip867ind0", fldName, valueStart + "skip me (867)");
    	solrFldMapTest.assertSolrFldHasNoValue(testDataFile, "multSkip867ind0", fldName, valueStart + "skip me 1 (867)");
    	solrFldMapTest.assertSolrFldHasNoValue(testDataFile, "multSkip867ind0", fldName, valueStart + "skip me 2 (867)");
    	
    	// keep if 2nd indicator "0" and no 852 sub =
    	valueStart = "GREEN -|- CURRENTPER -|- keep 867 -|- Supplement -|-  -|- keep me (867)";
    	solrFldMapTest.assertSolrFldValue(testDataFile, "keep867ind0", fldName, valueStart);
    	valueStart = "GREEN -|- STACKS -|- Supplement -|- Supplement -|-  -|- ";
    	solrFldMapTest.assertSolrFldValue(testDataFile, "multKeep867ind0", fldName, valueStart + "keep me 1 (867)");
    	solrFldMapTest.assertSolrFldValue(testDataFile, "multKeep867ind0", fldName, valueStart + "keep me 2 (867)");
    }
    
    /**
     * per spec in email by Naomi Dushay on July 12, 2011, an MHLD summary holdings section
     *  is skipped if 868 has ind2 of 0 and 852 has a sub = 
     */
//@Test
    public final void testSkipped868() 
    {
    	String testDataFile = testFilePath + "mhldDisplay868.mrc";
    	
    	// skip if 2nd indicator '0'  and 852 sub '=' exists
    	String valueStart = "GREEN -|- CURRENTPER -|- skip 868 -|- Index -|-  -|- ";
    	solrFldMapTest.assertSolrFldHasNoValue(testDataFile, "skip868ind0", fldName, valueStart + "skip me (868)");
    	valueStart = "MUSIC -|- MUS-NOCIRC -|- -|- Index -|-  -|- ";
    	solrFldMapTest.assertSolrFldHasNoValue(testDataFile, "multSkip868ind0", fldName, valueStart + "skip me 1 (868)");
    	solrFldMapTest.assertSolrFldHasNoValue(testDataFile, "multSkip868ind0", fldName, valueStart + "skip me 2 (868)");
    	
    	// keep if 2nd indicator "0" and no 852 sub =
    	valueStart = "GREEN -|- CURRENTPER -|- keep 868 -|- Index -|-  -|- keep me (868)";
    	solrFldMapTest.assertSolrFldValue(testDataFile, "keep868ind0", fldName, valueStart);
    	valueStart = "MUSIC -|- MUS-NOCIRC -|- -|- Index -|-  -|- ";
    	solrFldMapTest.assertSolrFldValue(testDataFile, "multKeep868ind0", fldName, valueStart + "keep me 1 (868)");
    	solrFldMapTest.assertSolrFldValue(testDataFile, "multKeep868ind0", fldName, valueStart + "keep me 2 (868)");
    }


    /**
     * per spec in email by Naomi Dushay on July 12, 2011, if there are multiple
     *  866 with ind2 '0' and 852 sub '=' exists, then there should be an indexing error message
     */
@Test
    public final void test86xErrorMessageConditions() 
    {
    	String testDataFile = testFilePath + "mhldDisplay86x.mrc";
        LoggerAppender4Testing appender = new LoggerAppender4Testing();
    	MhldDisplayUtil.logger.addAppender(appender);
        try 
        {
            Logger.getLogger(MhldDisplayUtil.class).info("Test");
        	solrFldMapTest.assertSolrFldHasNoValue(testDataFile, "362573", fldName, "ignore");
            appender.assertLogContains("Record 362573 has multiple 866 with ind2=0 and an 852 sub=");
            
            testDataFile = testFilePath + "mhldDisplay867.mrc";
        	solrFldMapTest.assertSolrFldHasNoValue(testDataFile, "multSkip867ind0", fldName, "ignore");
            appender.assertLogContains("Record multSkip867ind0 has multiple 867 with ind2=0 and an 852 sub=");
        	
//            testDataFile = testFilePath + "mhldDisplay868.mrc";
//        	solrFldMapTest.assertSolrFldHasNoValue(testDataFile, "multSkip868ind0", fldName, "ignore");
//            appender.assertLogContains("Record multSkip868ind0 has multiple 868 with ind2=0 and an 852 sub=");

        }
        finally 
        {
        	MhldDisplayUtil.logger.removeAppender(appender);
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
