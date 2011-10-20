package edu.stanford;

import static org.junit.Assert.*;

import java.io.File;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;
import org.solrmarc.tools.LoggerAppender4Testing;

public class MhldMappingTests extends AbstractStanfordTest 
{
	private String fldName = "mhld_display";
	private String testFilePath = testDataParentPath + File.separator;

@Before
	public void setup() 
	{
		mappingTestInit();
	}	

    /**
     * ensure all (non-skipped) 852s are output correctly
     */
@Test
    public void test852output() 
    {
    	String testDataFile = testFilePath + "mhldDisplay852only.mrc";
    	solrFldMapTest.assertSolrFldHasNumValues(testDataFile, "358041", fldName, 5);
    	solrFldMapTest.assertSolrFldValue(testDataFile, "358041", fldName, "GREEN -|- CURRENTPER -|- COUNTRY LIFE INTERNATIONAL. Latest yr. (or vol.) in CURRENT PERIODICALS; earlier in SAL -|-  -|- ");
    	solrFldMapTest.assertSolrFldValue(testDataFile, "358041", fldName, "SAL3 -|- STACKS -|-  -|-  -|- ");
    	solrFldMapTest.assertSolrFldValue(testDataFile, "358041", fldName, "SAL -|- STACKS -|-  -|-  -|- ");
    	solrFldMapTest.assertSolrFldValue(testDataFile, "358041", fldName, "GREEN -|- CURRENTPER -|- Latest yr. (or vol.) in CURRENT PERIODICALS; earlier in SAL -|-  -|- ");
    	solrFldMapTest.assertSolrFldValue(testDataFile, "358041", fldName, "GREEN -|- CURRENTPER -|- COUNTRY LIFE TRAVEL. Latest yr. (or vol.) in CURRENT PERIODICALS; earlier in SAL -|-  -|- ");
    }
    

    /**
     * per spec in email by Naomi Dushay on July 12, 2011, an MHLD is skipped
     *  if 852 sub z  says "All holdings transfered" 
     */
@Test
    public void testSkippedMhlds() 
    {
		String testDataFile = testFilePath + "mhldDisplay852only.mrc";
		solrFldMapTest.assertSolrFldHasNoValue(testDataFile, "3974376", fldName, "GREEN -|- STACKS -|-  -|-  -|- ");
		solrFldMapTest.assertSolrFldHasNumValues(testDataFile, "3974376", fldName, 0);
		solrFldMapTest.assertNoSolrFld(testDataFile, "3974376", fldName);
    }


    /**
     * ensure output with and without 86x have same number of separators
     */
@Test
    public void testNumberOfSeparators() 
    {
    	String testDataFile = testFilePath + "mhldDisplay852only.mrc";
    	// 852 alone without comment
    	String expectedResult = "SAL3 -|- STACKS -|-  -|-  -|- ";
    	solrFldMapTest.assertSolrFldValue(testDataFile, "358041", fldName, expectedResult);
    	assertNumSeparators(expectedResult);
    	
    	// 852 alone with comment
    	expectedResult = "GREEN -|- CURRENTPER -|- Latest yr. (or vol.) in CURRENT PERIODICALS; earlier in SAL -|-  -|- ";
    	solrFldMapTest.assertSolrFldValue(testDataFile, "358041", fldName, expectedResult);
    	assertNumSeparators(expectedResult);
    	
    	// 852 w 866
		testDataFile = testFilePath + "mhldDisplay868.mrc";
		expectedResult = "GREEN -|- CURRENTPER -|- keep 868 -|- v.194(2006)- -|- ";
    	solrFldMapTest.assertSolrFldValue(testDataFile, "keep868ind0", fldName, expectedResult);
    	assertNumSeparators(expectedResult);

    	// 852 w 868
    	expectedResult = "GREEN -|- CURRENTPER -|- keep 868 -|- Index: keep me (868) -|- ";
    	solrFldMapTest.assertSolrFldValue(testDataFile, "keep868ind0", fldName, expectedResult);
    	assertNumSeparators(expectedResult);
    	
    	// 852 w 867
		testDataFile = testFilePath + "mhldDisplay867.mrc";
		expectedResult = "GREEN -|- CURRENTPER -|- keep 867 -|- Supplement: keep me (867) -|- ";
    	solrFldMapTest.assertSolrFldValue(testDataFile, "keep867ind0", fldName, expectedResult);
    	assertNumSeparators(expectedResult);
    }


    /**
     * ensure all (non-skipped) 866s are output correctly
     */
@Test
    public void test866output() 
    {
    	String testDataFile = testFilePath + "mhldDisplay86x.mrc";
    	solrFldMapTest.assertSolrFldValue(testDataFile, "358041", fldName, "GREEN -|- CURRENTPER -|- COUNTRY LIFE INTERNATIONAL. Latest yr. (or vol.) in CURRENT PERIODICALS; earlier in SAL -|- 2009- -|- ");
    	solrFldMapTest.assertSolrFldValue(testDataFile, "358041", fldName, "SAL3 -|- STACKS -|-  -|- v.151(1972)-v.152(1972) -|- ");
    	solrFldMapTest.assertSolrFldValue(testDataFile, "358041", fldName, "SAL -|- STACKS -|-  -|- 1953; v.143(1968)-v.144(1968),v.153(1973)-v.154(1973),v.164(1978),v.166(1979),v.175(1984),v.178(1985),v.182(1988)-v.183(1989),v.194(2000)- -|- ");
    	// 2nd indicator "0" and no 852 sub =
    	solrFldMapTest.assertSolrFldValue(testDataFile, "358725", fldName, "GREEN -|- CURRENTPER -|- Latest yr. (or vol.) in CURRENT PERIODICALS; earlier in STACKS -|- [18-38, 1922-42]; 39, 1943- -|- ");
    }
    
    /**
     * per email by Naomi Dushay on October 14, 2011, MHLD summary holdings are 
     *  NOT skipped: display 866 regardless of second indicator value or presence of 852 sub =
     * previously: 
     * per spec in email by Naomi Dushay on July 12, 2011, an MHLD summary holdings section
     *  is skipped if 866 has ind2 of 0 and 852 has a sub = 
     */
@Test
    public void testSkipped866() 
    {
		String testDataFile = testFilePath + "mhldDisplay86x.mrc";
		
		// skip if 2nd indicator '0'  and 852 sub '=' exists
		String valueStart = "GREEN -|- CURRENTPER -|- Latest yr. (or vol.) in CURRENT PERIODICALS; earlier in STACKS -|- ";
		assertNumSeparators(valueStart + " -|- ");
    	solrFldMapTest.assertSolrFldValue(testDataFile, "362573", fldName, valueStart + "V. 417 NO. 1A (JAN 2011) -|- ");
    	solrFldMapTest.assertSolrFldValue(testDataFile, "362573", fldName, valueStart + "V. 417 NO. 4A (FEB 2011) -|- ");
    	solrFldMapTest.assertSolrFldValue(testDataFile, "362573", fldName, valueStart + "V. 417 NO. 5A (FEB 2011) -|- ");
    	solrFldMapTest.assertSolrFldValue(testDataFile, "362573", fldName, valueStart + "V. 417 NO. 20A (JUN 2011) -|- ");
    	solrFldMapTest.assertSolrFldValue(testDataFile, "362573", fldName, valueStart + "V. 417 NO. 21A (JUN 2011) -|- ");
    	solrFldMapTest.assertSolrFldValue(testDataFile, "362573", fldName, valueStart + "V. 417 NO. 22A (JUN 2011) -|- ");
    	solrFldMapTest.assertSolrFldValue(testDataFile, "362573", fldName, valueStart + "V. 417 NO. 23A (JUN 2011) -|- "); 	
    }
    
    /**
     * ensure all (non-skipped) 867s are output correctly
     */
@Test
    public void test867output() 
    {
    	String testDataFile = testFilePath + "mhldDisplay867.mrc";
    	
    	// keep if 2nd indicator "0" and no 852 sub =
    	String valueStart = "GREEN -|- CURRENTPER -|- keep 867 -|- Supplement: keep me (867) -|- ";
		assertNumSeparators(valueStart);
    	solrFldMapTest.assertSolrFldValue(testDataFile, "keep867ind0", fldName, valueStart);
    	valueStart = "GREEN -|- STACKS -|- Supplement -|- Supplement: ";
		assertNumSeparators(valueStart + " -|- ");
    	solrFldMapTest.assertSolrFldValue(testDataFile, "multKeep867ind0", fldName, valueStart + "keep me 1 (867) -|- ");
    	solrFldMapTest.assertSolrFldValue(testDataFile, "multKeep867ind0", fldName, valueStart + "keep me 2 (867) -|- ");
    }
    
    /**
     * per email by Naomi Dushay on October 14, 2011, MHLD summary holdings are 
     *  NOT skipped: display 867 regardless of second indicator value or presence of 852 sub =
     * previously: 
     * per spec in email by Naomi Dushay on July 12, 2011, an MHLD summary holdings section
     *  is skipped if 867 has ind2 of 0 and 852 has a sub = 
     */
@Test
    public void testSkipped867() 
    {
    	String testDataFile = testFilePath + "mhldDisplay867.mrc";
    	
    	// skip if 2nd indicator '0'  and 852 sub '=' exists
    	String valueStart = "GREEN -|- STACKS -|-  -|- Supplement: ";
		assertNumSeparators(valueStart + " -|- ");
    	solrFldMapTest.assertSolrFldValue(testDataFile, "skip867ind0", fldName, valueStart + "skip me (867) -|- ");
    	solrFldMapTest.assertSolrFldValue(testDataFile, "multSkip867ind0", fldName, valueStart + "skip me 1 (867) -|- ");
    	solrFldMapTest.assertSolrFldValue(testDataFile, "multSkip867ind0", fldName, valueStart + "skip me 2 (867) -|- ");
    }
    
    /**
     * ensure all (non-skipped) 867s are output correctly
     */
@Test
    public void test868output() 
    {
		String testDataFile = testFilePath + "mhldDisplay868.mrc";
	
    	// keep if 2nd indicator "0" and no 852 sub =
    	String valueStart = "GREEN -|- CURRENTPER -|- keep 868 -|- Index: keep me (868) -|- ";
		assertNumSeparators(valueStart);
    	solrFldMapTest.assertSolrFldValue(testDataFile, "keep868ind0", fldName, valueStart);
    	valueStart = "MUSIC -|- MUS-NOCIRC -|-  -|- Index: ";
		assertNumSeparators(valueStart + " -|- ");
    	solrFldMapTest.assertSolrFldValue(testDataFile, "multKeep868ind0", fldName, valueStart + "keep me 1 (868) -|- ");
    	solrFldMapTest.assertSolrFldValue(testDataFile, "multKeep868ind0", fldName, valueStart + "keep me 2 (868) -|- ");
    	
    	testDataFile = testFilePath + "mhldDisplay86x.mrc";
    	solrFldMapTest.assertSolrFldValue(testDataFile, "484112", fldName, valueStart + "annee.188(1999) -|- ");
    	solrFldMapTest.assertSolrFldValue(testDataFile, "484112", fldName, valueStart + "MICROFICHE (MAY/DEC 2000) -|- ");
    }
    
    /**
     * per email by Naomi Dushay on October 14, 2011, MHLD summary holdings are 
     *  NOT skipped: display 868 regardless of second indicator value or presence of 852 sub =
     * previously: 
     * per spec in email by Naomi Dushay on July 12, 2011, an MHLD summary holdings section
     *  is skipped if 868 has ind2 of 0 and 852 has a sub = 
     */
@Test
    public void testSkipped868() 
    {
    	String testDataFile = testFilePath + "mhldDisplay868.mrc";
    	
    	// skip if 2nd indicator '0'  and 852 sub '=' exists
    	String valueStart = "GREEN -|- CURRENTPER -|- skip 868 -|- Index: ";
		assertNumSeparators(valueStart + " -|- ");
    	solrFldMapTest.assertSolrFldValue(testDataFile, "skip868ind0", fldName, valueStart + "skip me (868) -|- ");
    	valueStart = "MUSIC -|- MUS-NOCIRC -|-  -|- Index: ";
		assertNumSeparators(valueStart + " -|- ");
    	solrFldMapTest.assertSolrFldValue(testDataFile, "multSkip868ind0", fldName, valueStart + "skip me 1 (868) -|- ");
    	solrFldMapTest.assertSolrFldValue(testDataFile, "multSkip868ind0", fldName, valueStart + "skip me 2 (868) -|- ");
    }


    /**
     * per email by Naomi Dushay on October 14, 2011, MHLD summary holdings are 
     *  NOT skipped: display 868 regardless of second indicator value or presence of 852 sub =
     * previously: 
     * per spec in email by Naomi Dushay on July 12, 2011, if there are multiple
     *  866 with ind2 '0' and 852 sub '=' exists, then there should be an indexing error message
     */
//@Test
//    public void test86xErrorMessageConditions() 
//    {
//    	String testDataFile = testFilePath + "mhldDisplay86x.mrc";
//        LoggerAppender4Testing appender = new LoggerAppender4Testing();
//    	MhldDisplayUtil.logger.addAppender(appender);
//        try 
//        {
//            Logger.getLogger(MhldDisplayUtil.class).info("Test");
//        	solrFldMapTest.assertSolrFldHasNoValue(testDataFile, "362573", fldName, "ignore");
//            appender.assertLogContains("Record 362573 has multiple 866 with ind2=0 and an 852 sub=");
//            
//            testDataFile = testFilePath + "mhldDisplay867.mrc";
//        	solrFldMapTest.assertSolrFldHasNoValue(testDataFile, "multSkip867ind0", fldName, "ignore");
//            appender.assertLogContains("Record multSkip867ind0 has multiple 867 with ind2=0 and an 852 sub=");
//        	
//            testDataFile = testFilePath + "mhldDisplay868.mrc";
//        	solrFldMapTest.assertSolrFldHasNoValue(testDataFile, "multSkip868ind0", fldName, "ignore");
//            appender.assertLogContains("Record multSkip868ind0 has multiple 868 with ind2=0 and an 852 sub=");
//
//        }
//        finally 
//        {
//        	MhldDisplayUtil.logger.removeAppender(appender);
//        }
//    }

    /**
     * 852 subfield 3 should be included in the comment
     */
@Test
    public void test852sub3() 
    {
    	String testDataFile = testFilePath + "mhldDisplay852sub3.mrc";
    	String valueStrB4 = "GREEN -|- STACKS -|- ";
    	String valueStrAfter = " -|-  -|- ";
		assertNumSeparators(valueStrB4 + valueStrAfter);
    	
    	solrFldMapTest.assertSolrFldValue(testDataFile, "852zNo3", fldName, valueStrB4 + "sub z" + valueStrAfter);
    	solrFldMapTest.assertSolrFldValue(testDataFile, "852-3noZ", fldName, valueStrB4 + "sub 3" + valueStrAfter);
    	solrFldMapTest.assertSolrFldValue(testDataFile, "852zAnd3", fldName, valueStrB4 + "sub 3 sub z" + valueStrAfter);
    }
    
//-------- Latest received tests ----------------

    /**
     * if the 866 field is open (ends with a hyphen), then use the most recent
     *   863, formatted per matching 853
     */
@Test
    public void testLatestReceived()
    {
    	String testDataFile = testFilePath + "mhldDisplay.mrc";
    	// 852 has sub =  and no 866
    	String libLoc = "GREEN -|- CURRENTPER -|- ";
    	String result = libLoc + "COUNTRY LIFE INTERNATIONAL. Latest yr. (or vol.) in CURRENT PERIODICALS; earlier in SAL -|- 2009- -|- 2011 Summer";
    	assertNumSeparators(result);
    	solrFldMapTest.assertSolrFldValue(testDataFile, "358041", fldName, result);
    }
    
// FIXME:  need tests for different formats (ugh) -- do it with true unit tests (no xml records)
    
    
    /**
     * there should be no "Latest Received" portion when the 866 is closed
     *   (doesn't end with a hyphen)
     */
@Test
    public void testClosedHoldings() 
    {
    	String testDataFile = testFilePath + "mhldDisplay.mrc";
    
    	// 866 doesn't end with hyphen, and there are 863 - do not include 863 as Latest Received
    	String resultNoLatestRecd = "MUSIC -|- MUS-NOCIRC -|-  -|- v.188(1999) -|- ";
    	String result = resultNoLatestRecd + "annee 188 no.14 Dec 17, 1999";
    	assertNumSeparators(result);
    	solrFldMapTest.assertSolrFldHasNoValue(testDataFile, "484112", fldName, result);
    	// it SHOULD have a value without the Latest Received portion
    	assertNumSeparators(resultNoLatestRecd);
    	solrFldMapTest.assertSolrFldValue(testDataFile, "484112", fldName, resultNoLatestRecd);

// FIXME:    	
//    	fail("add tests closed holdings occurring first, last, middle ...");
    }

	/**
	 * if there is no 866, then 
	 *  if the 852 has a sub = , then display the most recent 863
	 */
@Test
	public void testNo866()
	{
    	String testDataFile = testFilePath + "mhldDisplay.mrc";

    	// 852 has sub =  and no 866:  use most recent 863
    	String libLoc = "GREEN -|- CURRENTPER -|- ";
    	String result = libLoc + "COUNTRY LIFE TRAVEL. Latest yr. (or vol.) in CURRENT PERIODICALS; earlier in SAL -|-  -|- 2010/2011 Winter";
    	assertNumSeparators(result);
    	solrFldMapTest.assertSolrFldValue(testDataFile, "358041", fldName, result);
    	
    	// 852 has no sub =  and no 866:  do not use latest 863
    	String resultNoLatestRecd = libLoc + "Latest yr. (or vol.) in CURRENT PERIODICALS; earlier in SAL3 -|-  -|- ";
    	result = resultNoLatestRecd + "v.20 no.9 (2011:March 18)";
    	assertNumSeparators(result);
    	solrFldMapTest.assertSolrFldHasNoValue(testDataFile, "2416921", fldName, result);
    	// it SHOULD have a result from the 852 (with no Latest Received or Library Has)
    	assertNumSeparators(resultNoLatestRecd);
    	solrFldMapTest.assertSolrFldValue(testDataFile, "2416921", fldName, resultNoLatestRecd);
	}


    
    /**
     * test the expected values for 358041
     */
@Test
    public void test358041()
    {
    	String testDataFile = testFilePath + "mhldDisplay.mrc";
    
    	Set<String> resultSet = new HashSet<String>();
    	
    	// 852 has sub =  and no 866:  use most recent 863
    	String greenCurrentper = "GREEN -|- CURRENTPER -|- ";
    	String result = greenCurrentper + "COUNTRY LIFE INTERNATIONAL. Latest yr. (or vol.) in CURRENT PERIODICALS; earlier in SAL -|- 2009- -|- 2011 Summer";
    	resultSet.add(result);
    	
    	result = "SAL3 -|- STACKS -|-  -|- v.151(1972)-v.152(1972) -|- ";
    	resultSet.add(result);
    	
    	result = "SAL -|- STACKS -|-  -|- 1953; v.143(1968)-v.144(1968),v.153(1973)-v.154(1973),v.164(1978),v.166(1979),v.175(1984),v.178(1985),v.182(1988)-v.183(1989),v.194(2000)- -|- ";
    	resultSet.add(result);

    	// 867 ind 0  previous 852 has sub =  - now used per email by Naomi Dushay on October 14, 2011 	
    	result = greenCurrentper + "Latest yr. (or vol.) in CURRENT PERIODICALS; earlier in SAL -|- Supplement: COUNTRY LIFE ABROAD (WIN 2001), (JUL 14, 2005) -|- ";
    	resultSet.add(result);

    	result = greenCurrentper + "COUNTRY LIFE TRAVEL. Latest yr. (or vol.) in CURRENT PERIODICALS; earlier in SAL -|-  -|- 2010/2011 Winter";
    	resultSet.add(result);
    	
    	solrFldMapTest.assertSolrFldHasNumValues(testDataFile, "358041", fldName, 5);
    	for (String expected : resultSet)
    	{
    		assertNumSeparators(result);
        	solrFldMapTest.assertSolrFldValue(testDataFile, "358041", fldName, expected);
    	}
    }
    

    /**
     * test the expected values for a record with easier text strings
     */
@Test
    public void testEasyTextStr()
    {
    	String testDataFile = testFilePath + "mhldDisplayEasy.mrc";
    
    	Set<String> resultSet = new HashSet<String>();
    	
    	// 852 has sub =  and no 866:  use most recent 863
    	String lib1loc1 = "lib1 -|- loc1 -|- ";
    	String result = lib1loc1 + "comment1 -|- 866a1open- -|- 2011 Summer";
    	resultSet.add(result);
    	
    	result = "lib2 -|- loc2 -|-  -|- 866a2 -|- ";
    	resultSet.add(result);
    	
    	result = "lib3 -|- loc3 -|-  -|- 866a3open- -|- ";
    	resultSet.add(result);

    	result = lib1loc1 + "comment4 -|- Supplement: 867a -|- ";
    	resultSet.add(result);
    	
    	
    	result = lib1loc1 + "comment5 -|-  -|- 2010/2011 Winter";
    	resultSet.add(result);
    	
    	solrFldMapTest.assertSolrFldHasNumValues(testDataFile, "358041", fldName, 5);
    	
    	for (String expected : resultSet)
    	{
    		assertNumSeparators(result);
        	solrFldMapTest.assertSolrFldValue(testDataFile, "358041", fldName, expected);
    	}
    }


	/**
	 * assert that the expected mhld_display value has the correct number of separators
	 */
    private void assertNumSeparators(String mhld_displayValue) 
    {
       Pattern p = Pattern.compile(" -\\|- ");
       Matcher m = p.matcher(mhld_displayValue); 
       int count = 0;
       while(m.find()) {
           count++;
       }
       assertEquals("Got wrong number of separators for mhld_display field: ",  4, count);
    }

	
}
