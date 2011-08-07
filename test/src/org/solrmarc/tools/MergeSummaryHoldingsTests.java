package org.solrmarc.tools;

import static org.junit.Assert.*;

import java.io.*;
import java.util.*;

import org.junit.*;
import org.marc4j.marc.*;
import org.marc4j.marc.impl.*;
import org.solrmarc.marc.RawRecordReader;
import org.solrmarc.testUtils.CommandLineUtils;

/**
 * Note that actual use of MergeSummaryHoldings is a call to main() from a 
 *  shell script, so these tests must use the CommandLineUtils
 * @author naomi
 *
 */
public class MergeSummaryHoldingsTests
{
    String testDir = "test";
    String testDataParentPath =  testDir + File.separator + "data";
    String smokeTestDir = testDataParentPath + File.separator + "smoketest";
    String testConfigFile = smokeTestDir + File.separator + "test_config.properties";

    String localDir = ".." + File.separator + ".." + File.separator + "examples" + File.separator + "stanfordBlacklight";
    String localTestDataParentPath = localDir + File.separator + testDataParentPath;

    String mrgMhldClassName = "org.solrmarc.tools.MergeSummaryHoldings";
    String mrcPrntrClassName = "org.solrmarc.marc.MarcPrinter";
    String mainMethodName = "main";
    
    

    @Before
    public void setUp()
    {
        if (!Boolean.parseBoolean(System.getProperty("test.solr.verbose")))
        {
            java.util.logging.Logger.getLogger("org.apache.solr").setLevel(java.util.logging.Level.SEVERE);
            Utils.setLog4jLogLevel(org.apache.log4j.Level.WARN);
        }
    }
        
    /**
     * code should end smoothly if it encounters no matches between bib and mhld
     */
@Test
    public void testNoMatches() 
    {
    	//bib46, mhld235
		String bibFilePath = localTestDataParentPath + File.separator + "mhldMergeBibs46.xml";
		String mhldFilePath = localTestDataParentPath + File.separator + "mhldMergeMhlds235.xml";

		// ensure no error message was printed
		ByteArrayOutputStream sysBAOS = new ByteArrayOutputStream();
		PrintStream sysMsgs = new PrintStream(sysBAOS);
		System.setErr(sysMsgs);
		System.setOut(sysMsgs);
	
		ByteArrayOutputStream mergedAsByteArrayOutStream = mergeBibAndMhldFiles(bibFilePath, mhldFilePath);
		// extract each record and determine it is present and there was no merge

		// ensure no error message was printed
		assertTrue("Output messages unexpectedly written: " + sysBAOS.toString(),  sysBAOS.size() == 0);
		
		fail("Implement me");
    }
    
    /**
     * code should smoothly continue if it encounters a bib with no MHLD
     */
//@Test
    public void testUnmatchedMHLD() 
    {
    	//bib46, mhld235
    	fail("Implement me");
    }
    
    /**
     * code should smoothly continue if it encounters a bib with no MHLD
     */
//@Test
    public void testUnmatchedBib() 
    {
    	fail("Implement me");
    }

// first record matching tests    
    /**
     * code should find a match when first bib matches first mhld
     */
//@Test
    public void testBothFirstRecsMatch() 
    {
    	// bib346, mhld345
    	fail("Implement me");
    }

    /**
     * code should find a match when first bib matches non-first mhld
     */
//@Test
    public void testFirstBibMatchesNonFirstMhld() 
    {
    	//bib346, mhld235
    	fail("Implement me");
    }

    /**
     * code should find a match when non-first bib matches first mhld
     */
//@Test
    public void testNonFirstBibMatchesFirstMhld() 
    {
    	//bib134, mhld345
    	fail("Implement me");
    }

// last record matching tests
    /**
     * code should find a match when last bib matches last mhld
     */
//@Test
    public void testBothLastRecsMatch() 
    {
    	//bib46, mhld236
    	fail("Implement me");
    }

    /**
     * code should find a match when last bib matches non-last mhld
     */
//@Test
    public void testLastBibMatchesNonLastMhld() 
    {
    	//bib134, mhld345
    	fail("Implement me");
    }

    /**
     * code should find a match when non-last bib matches last mhld
     */
//@Test
    public void testNonLastBibMatchesLastMhld() 
    {
    	//bib46, mhld34
    	fail("Implement me");
    }

    
    /**
     * need to ensure all the MHLD data is included, not just the first record
     */
//@Test
    public void testMultMHLDsWithSameID() 
    {
    	//bib134, multMhlds1    	
    	fail("Implement me");
    }
    
    /**
     * the MHLD fields should only be merged into ONE of the bibs, if the bibs will be combined?
     * Or it's probably ok if they are in each bib, as they should be removed from the bib after processing?
     */
//@Test
    public void testMultBibsWithSameID() 
    {
    	// multBibs4, mhld 34
    	fail("Implement me");
    }
    
    /**
     * need to ensure all the MHLD data is included, not just the first record
     */
    public void testMultBothWithSameID() 
    {
    	
    }
    
    
    
    /**
     * the bib record should only get the fields specified
     */
    public void testFieldsToMerge() 
    {
    }

    /**
     * the bib record should not get any MHLD fields that aren't indicated for the merge
     */
    public void testFieldsNotToMerge() 
    {
    }
    
    /**
     * if the MHLD has more than one instance of a field, all instances should be put in the bib record
     */
    public void testMultOccurFieldsToMerge() 
    {
    }
    
    
    /**
     * if the bib rec has existing MHLD fields (not from another MHLD record?) then it should
     * remove them before adding the MHLD fields
     */
//@Test
    public void testCrashingBibFieldsRemoved() 
    {
    	//bibWmhldFlds, completeMhld
		fail("implement me");
    }
    


String mergedSummaryHoldingsOutput[] = {
        "LEADER 02429nas a2200481 a 4500",
        "001 u335",
        "003 SIRSI",
        "008 840508c19799999gw fu p       0uuub0ger d",
        "035   $a(Sirsi) o10701458",
        "035   $a(OCoLC)10701458",
        "040   $aVA@$cVA@",
        "049   $aVAS@",
        "090   $aAP30$b.T75$mVAS@$qALDERMAN",
        "245 00$aTumult.",
        "246 13$aZeitschrift für Verkehrswissenschaft",
        "260   $aBerlin :$bMerve Verlag,$c1979-",
        "300   $av. :$bill. ;$c24 cm.",
        "310   $aSemiannual",
        "362 0 $a1-",
        "500   $aTitle from cover; imprint varies.",
        "599   $a2$b(YR.) 2008 NO. 34;$b(YR.) 2008 NO. 33;$bNR. 32 2007;",
        "596   $a2",
        "515   $aNone published 1980-1981.",
        "852   $bALDERMAN$cALD-STKS$xpat#169090$x2x$xbind 4N=2 or 3yrs$xex.:  Nr. 15-18 1988-93$xindex ?$xuse copyright year for dating$zCURRENT ISSUES HELD IN THE PERIODICALS ROOM $x5071",
        "853 2 $82$anr.",
        "853 2 $83$anr.$i(year)$j(unit)",
        "853 2 $84$a(yr.)$bno.$u2$vc",
        "866  0$81$aNr.1-28  (1979-2004)$zIn stacks",
        "863  1$83.6$a29$i2005$j.",
        "863  1$83.7$a30$i2005$j.",
        "863  1$83.8$a31$i2006$j.",
        "863  1$83.9$a32$i2007",
        "863  1$84.1$a2008$b33",
        "863  1$84.2$a2008$b34",
        "999   $aAP30 .T75 Nr.7-10 1983-87$wLCPER$c1$iX001614137$d5/9/2008$lALD-STKS$mALDERMAN$n2$q3$rY$sY$tBOUND-JRNL$u6/28/1996$xH-NOTIS",
        "999   $aAP30 .T75 Nr.1-3 1979-82$wLCPER$c1$iX000769605$d4/8/2009$lALD-STKS$mALDERMAN$q2$rY$sY$tBOUND-JRNL$u6/28/1996$xH-NOTIS",
        "999   $aAP30 .T75 Nr.4-6 1982-83$wLCPER$c1$iX000764174$d5/21/2002$lALD-STKS$mALDERMAN$q5$rY$sY$tBOUND-JRNL$u6/28/1996$xH-NOTIS",
        "999   $aAP30 .T75 Nr.11-14 1988-90$wLCPER$c1$iX002128357$d1/27/2010$lALD-STKS$mALDERMAN$n1$q1$rY$sY$tBOUND-JRNL$u6/28/1996$xH-NOTIS",
        "999   $aAP30 .T75 Nr.15-18 1991-93$wLCPER$c1$iX002509913$d11/11/1994$lALD-STKS$mALDERMAN$n1$rY$sY$tBOUND-JRNL$u6/28/1996$xH-NOTIS",
        "999   $aAP30 .T75 Periodical order-001$wLCPER$c1$i335-6001$d1/11/1999$lALD-STKS$mALDERMAN$rY$sY$tBOUND-JRNL$u12/18/1996",
        "999   $aAP30 .T75 Nr.19-22 1994-96$wLCPER$c1$iX006060933$d7/23/1998$e5/26/1998$lALD-STKS$mALDERMAN$n1$rY$sY$tBOUND-JRNL$u5/26/1998$xADD",
        "999   $aAP30 .T75 Nr.25-28 2001-2004$wLCPER$c1$iX030047292$d2/12/2007$e1/23/2007$lALD-STKS$mALDERMAN$q1$rY$sY$tBOUND-JRNL$u1/22/2007$xADD",
        "999   $aAP30 .T75 Nr.23-24 1998-1999$wLCPER$c1$iX006166304$d4/5/2007$e3/13/2007$lALD-STKS$mALDERMAN$rY$sY$tBOUND-JRNL$u3/12/2007$xADD",
        };

String mergedSummaryHoldingsOutputNoUmlaut[] = {
        "LEADER 02429nas a2200481 a 4500",
        "001 u335",
        "003 SIRSI",
        "008 840508c19799999gw fu p       0uuub0ger d",
        "035   $a(Sirsi) o10701458",
        "035   $a(OCoLC)10701458",
        "040   $aVA@$cVA@",
        "049   $aVAS@",
        "090   $aAP30$b.T75$mVAS@$qALDERMAN",
        "245 00$aTumult.",
        "246 13$aZeitschrift fèur Verkehrswissenschaft",
        "260   $aBerlin :$bMerve Verlag,$c1979-",
        "300   $av. :$bill. ;$c24 cm.",
        "310   $aSemiannual",
        "362 0 $a1-",
        "500   $aTitle from cover; imprint varies.",
        "599   $a2$b(YR.) 2008 NO. 34;$b(YR.) 2008 NO. 33;$bNR. 32 2007;",
        "596   $a2",
        "515   $aNone published 1980-1981.",
        "852   $bALDERMAN$cALD-STKS$xpat#169090$x2x$xbind 4N=2 or 3yrs$xex.:  Nr. 15-18 1988-93$xindex ?$xuse copyright year for dating$zCURRENT ISSUES HELD IN THE PERIODICALS ROOM $x5071",
        "853 2 $82$anr.",
        "853 2 $83$anr.$i(year)$j(unit)",
        "853 2 $84$a(yr.)$bno.$u2$vc",
        "866  0$81$aNr.1-28  (1979-2004)$zIn stacks",
        "863  1$83.6$a29$i2005$j.",
        "863  1$83.7$a30$i2005$j.",
        "863  1$83.8$a31$i2006$j.",
        "863  1$83.9$a32$i2007",
        "863  1$84.1$a2008$b33",
        "863  1$84.2$a2008$b34",
        "999   $aAP30 .T75 Nr.7-10 1983-87$wLCPER$c1$iX001614137$d5/9/2008$lALD-STKS$mALDERMAN$n2$q3$rY$sY$tBOUND-JRNL$u6/28/1996$xH-NOTIS",
        "999   $aAP30 .T75 Nr.1-3 1979-82$wLCPER$c1$iX000769605$d4/8/2009$lALD-STKS$mALDERMAN$q2$rY$sY$tBOUND-JRNL$u6/28/1996$xH-NOTIS",
        "999   $aAP30 .T75 Nr.4-6 1982-83$wLCPER$c1$iX000764174$d5/21/2002$lALD-STKS$mALDERMAN$q5$rY$sY$tBOUND-JRNL$u6/28/1996$xH-NOTIS",
        "999   $aAP30 .T75 Nr.11-14 1988-90$wLCPER$c1$iX002128357$d1/27/2010$lALD-STKS$mALDERMAN$n1$q1$rY$sY$tBOUND-JRNL$u6/28/1996$xH-NOTIS",
        "999   $aAP30 .T75 Nr.15-18 1991-93$wLCPER$c1$iX002509913$d11/11/1994$lALD-STKS$mALDERMAN$n1$rY$sY$tBOUND-JRNL$u6/28/1996$xH-NOTIS",
        "999   $aAP30 .T75 Periodical order-001$wLCPER$c1$i335-6001$d1/11/1999$lALD-STKS$mALDERMAN$rY$sY$tBOUND-JRNL$u12/18/1996",
        "999   $aAP30 .T75 Nr.19-22 1994-96$wLCPER$c1$iX006060933$d7/23/1998$e5/26/1998$lALD-STKS$mALDERMAN$n1$rY$sY$tBOUND-JRNL$u5/26/1998$xADD",
        "999   $aAP30 .T75 Nr.25-28 2001-2004$wLCPER$c1$iX030047292$d2/12/2007$e1/23/2007$lALD-STKS$mALDERMAN$q1$rY$sY$tBOUND-JRNL$u1/22/2007$xADD",
        "999   $aAP30 .T75 Nr.23-24 1998-1999$wLCPER$c1$iX006166304$d4/5/2007$e3/13/2007$lALD-STKS$mALDERMAN$rY$sY$tBOUND-JRNL$u3/12/2007$xADD",
        };

	/**
	 * This is Bob's original test, re-written only to allow it to execute as
	 * a normal junit test within Eclipse.
	 */
@Test
	public void origTestOfRewritingMHLDtoSameBib() 
	        throws IOException
	{
	    String mhldRecFileName = testDataParentPath + File.separator + "summaryHld_1-1000.mrc";
	    String bibRecFileName = testDataParentPath + File.separator + "u335.mrc";
	
	    InputStream inStr = null;
	    ByteArrayOutputStream resultMrcOutStream = new ByteArrayOutputStream();
	    String[] mergeMhldArgs = new String[]{"-s", mhldRecFileName, bibRecFileName };
	
	    // call the code for mhldfile summaryHld_1-1000.mrc  and bibfile u335.mrc
	    CommandLineUtils.runCommandLineUtil(mrgMhldClassName, mainMethodName, inStr, resultMrcOutStream, mergeMhldArgs);
	
	    assertMarcRecsEqual(mergedSummaryHoldingsOutput, resultMrcOutStream);
	    
	    // Now merge record again to test the deleting of existing summary holdings info
	    ByteArrayInputStream mergedMarcBibRecAsInStream = new ByteArrayInputStream(resultMrcOutStream.toByteArray());
	    resultMrcOutStream.close();
	    resultMrcOutStream = new ByteArrayOutputStream();
	    //  do the merge by piping the bib record in to the merge class
	    CommandLineUtils.runCommandLineUtil(mrgMhldClassName, mainMethodName, mergedMarcBibRecAsInStream, resultMrcOutStream, new String[]{"-s", mhldRecFileName } );
	    
	    assertMarcRecsEqual(mergedSummaryHoldingsOutput, resultMrcOutStream);
	}


	/**
	 * test just getting map of ids to Records and no sysout stuff
	 */
@Test
	public void testGettingOutputAsMapOfRecords() 
	        throws IOException
	{
	    String mhldRecFileName = testDataParentPath + File.separator + "summaryHld_1-1000.mrc";
	    String bibRecFileName = testDataParentPath + File.separator + "u335.mrc";
	
	    Map<String, Record> mergedRecs = MergeSummaryHoldings.mergeMhldsIntoBibRecordsAsMap(bibRecFileName, mhldRecFileName);
	    junit.framework.Assert.assertEquals("results should have 1 record", 1, mergedRecs.size());
	    String expId = "335";
	    assertTrue("Record with id " + expId + " should be in results", mergedRecs.containsKey(expId));
	    
	    Record resultRec = mergedRecs.get(expId);
		assertEqualsIgnoreLeader(mergedSummaryHoldingsOutputNoUmlaut, resultRec);		
	}


	/**
	 * Test if using Naomi's approach with next() works as well as weird way of duplicating code
	 */
@Test
	public void testMergeToStdOut2() 
	        throws IOException
	{
	    String mhldRecFileName = testDataParentPath + File.separator + "summaryHld_1-1000.mrc";
	    String bibRecFileName = testDataParentPath + File.separator + "u335.mrc";
	
		ByteArrayOutputStream sysBAOS = new ByteArrayOutputStream();
		PrintStream sysMsgs = new PrintStream(sysBAOS);
		System.setOut(sysMsgs);

		MergeSummaryHoldings.mergeMhldRecsIntoBibRecsAsStdOut2(bibRecFileName, mhldRecFileName);
	
	    assertMarcRecsEqual(mergedSummaryHoldingsOutput, sysBAOS);
	}


	/**
	 * Given an expected marc record as an Array of strings corresponding to 
	 *  the lines in the output of MarcPrinter and 
	 * given the actual marc record as a ByteArrayOutputStream,
	 *  assert they are equal
	 */
	private void assertMarcRecsEqual(String[] expectedAsLines, ByteArrayOutputStream actualAsBAIS)
	{
	    // convert actual record into an array of strings from MarcPrinter output
	    ByteArrayInputStream mergedMarcBibRecAsInStream = new ByteArrayInputStream(actualAsBAIS.toByteArray());
	    ByteArrayOutputStream marcPrinterOutputOfMergedBibRec = new ByteArrayOutputStream();
	    ByteArrayOutputStream errorStream = new ByteArrayOutputStream();
	    String[] marcPrintArgs = new String[]{testConfigFile, "print"};
	    CommandLineUtils.runCommandLineUtil(mrcPrntrClassName, mainMethodName, mergedMarcBibRecAsInStream, marcPrinterOutputOfMergedBibRec, errorStream, marcPrintArgs); 

	    // did the resulting merged record contain the expected output?
	    assertMarcRecsEqual(expectedAsLines, new ByteArrayInputStream(marcPrinterOutputOfMergedBibRec.toByteArray()));
	}
    
    
    /**
     * Given an expected marc record as an Array of strings corresponding to 
     *  the lines in the output of MarcPrinter and given the actual marc record as an InputStream,
     *  assert they are equal
     */
    private void assertMarcRecsEqual(String[] expectedAsLines, InputStream actualAsInputStream) 
    {
        BufferedReader actualAsBuffRdr = null;
        try
        {
            actualAsBuffRdr = new BufferedReader(new InputStreamReader(actualAsInputStream, "UTF-8"));
        }
        catch (UnsupportedEncodingException e)
        {
            e.printStackTrace();
            fail("couldn't read record to be tested from InputStream");
        }

        int numExpectedLines = expectedAsLines.length;

        try
        {
            int lineCnt = 0;
            String actualLine = null;
            while ((actualLine = actualAsBuffRdr.readLine()) != null)
            {
                if (actualLine.length() == 0) 
                {
                	// do nothing;
                }
                else if (numExpectedLines > 0 && lineCnt < numExpectedLines) 
                {
                    if (actualLine.equals("Flushing results...") || actualLine.equals("Flushing results done") || actualLine.startsWith("Cobertura:"))
                        continue;   // skip this line and don't even count it.  I don't know where these "Flushing Results..." lines are coming from.

                    String expectedLine = expectedAsLines[lineCnt];
                    junit.framework.Assert.assertEquals("output line ["+ actualLine + "]  doesn't match expected [" + expectedLine + "]", expectedLine, actualLine);
                }
                lineCnt++;
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
            fail("couldn't compare records");
        }
    }
        

    /**
     * 
     * @param bibRecsFileName name of the file containing Bib records, relative to the testDataParentPath
     * @param mhldRecsFileName name of the file containing MHLD records, relative to the testDataParentPath
     * @return the resulting merged file as a ByteArrayOutputStream
     */
    private ByteArrayOutputStream mergeBibAndMhldFiles(String bibRecsFileName, String mhldRecsFileName) 
    {
        String fullBibRecsFileName = testDataParentPath + File.separator + bibRecsFileName;
        String fullMhldRecsFileName = testDataParentPath + File.separator + mhldRecsFileName;

        InputStream inStr = null;
        ByteArrayOutputStream resultMrcOutStream = new ByteArrayOutputStream();
        String[] mergeMhldArgs = new String[]{"-s", fullMhldRecsFileName, fullBibRecsFileName };

        // call the MergeSummaryHoldings code from the command line
        CommandLineUtils.runCommandLineUtil(mrgMhldClassName, mainMethodName, inStr, resultMrcOutStream, mergeMhldArgs);
        return resultMrcOutStream;
    }

    
// FIXME: don't understand why the leader is not matching ...    
    /**
     * compare two marc records;  the expected result is represented as
     *  an array of strings.  The leaders don't match; not sure why or if it
     *  matters.
     * @param expected
     * @param actual
     */
    private void assertEqualsIgnoreLeader(String[] expected, Record actual) 
    {
    	String actualAsStr = actual.toString();
     	// removing leader is removing "LEADER " and the 24 char leader and the newline
    	String actualAsStrWithoutLdr = actualAsStr.substring(32);

     	StringBuffer buf = new StringBuffer();
    	for (int i = 1; i < expected.length; i++) {
    		buf.append(expected[i] + "\n");
    	}
    	
    	junit.framework.Assert.assertEquals("Records weren't equal", buf.toString(), actualAsStrWithoutLdr);
/*
    	buf = new StringBuffer();
    	for (int i = 0; i < expected.length; i++) {
    		buf.append(expected[i] + "\n");
    	}
    	junit.framework.Assert.assertEquals("Records weren't equal", buf.toString(), actualStr);
*/  
    }

    
    /**
     * assert two RawRecord objects are equal by comparing them as byte[]
     */
    private static void assertEquals(RawRecord expected, RawRecord actual)
    {
    	String actualId = actual.getRecordId();
    	String expectedId = expected.getRecordId();
        assertTrue("Record ids didn't match: " + actualId + " " + expectedId, actualId.equals(expectedId));

    	byte expBytes[] = expected.getRecordBytes();
        byte actualBytes[] = actual.getRecordBytes();
        assertTrue("Records " + expectedId + " didn't match", java.util.Arrays.equals(expBytes, actualBytes));
    }

    /**
     * assert two RawRecord objects are not equal by comparing them as byte[]
     */
    private static void assertNotEqual(RawRecord expected, RawRecord actual)
    {
    	String actualId = actual.getRecordId();
    	String expectedId = expected.getRecordId();
        if ( !actualId.equals(expected.getRecordId()) )
        	return;

    	byte expBytes[] = expected.getRecordBytes();
        byte actualBytes[] = actual.getRecordBytes();
        assertFalse("Records " + expectedId + " matched", java.util.Arrays.equals(expBytes, actualBytes));
    }
 
    /**
     * vetted!
     * assert two Record objects are equal by comparing them as strings
     */
    private static void assertEquals(Record expected, Record actual)
    {
    	String actualId = actual.getControlNumber();
    	String errmsg = "Record " + actualId + " wasn't as expected";
        if ( actualId.equals(expected.getControlNumber()) )
        	assertTrue(errmsg, expected.toString().equals(actual.toString()) );
        else
        	fail(errmsg);
    }
    
    /**
     * vetted!
     * assert two Record objects aren't equal by comparing them as strings
     */
    private static void assertNotEqual(Record expected, Record actual)
    {
    	String actualId = actual.getControlNumber();
        if ( !actualId.equals(expected.getControlNumber()) )
        	return;

        assertFalse("Records unexpectedly the same: " + actualId, expected.toString().equals(actual.toString()) );
    }
    
    /**
     * compare two marc records as XML strings
     * FIXME:  this is going to be messy with whitespace ...
     * @param expectedAsXMLStr
     * @param actual
     */
    private void assertEqualsAsXml(String expectedAsXMLStr, RawRecord actual) 
    {
    	String actualAsXMLStr = convertRawRecordToXml(actual);
    	String actualId = actual.getRecordId();
        assertTrue("Record id " + actualId + " wasn't as expected", expectedAsXMLStr.equals(actualAsXMLStr) );
    }


    /**
     * given a RawRecord object, convert it to XML and return the XML as a string
     */
    private String convertRawRecordToXml(RawRecord rawRec) 
    {
        ByteArrayInputStream recAsInStream = new ByteArrayInputStream(rawRec.getRecordBytes());
        ByteArrayOutputStream resultAsOutStream = new ByteArrayOutputStream();
    	String className = "org.marc4j.util.MarcXmlDriver";
    	CommandLineUtils.runCommandLineUtil(className, "main", recAsInStream, resultAsOutStream, new String[]{});
    	return resultAsOutStream.toString();
    }
    
    /**
     * given a file of records as a ByteArrayOutputStream and a record id,
     *  look for that record.  If it is found, return it as a RawRecord object,
     *  otherwise, return null
     */
    private RawRecord extractRecord(ByteArrayOutputStream recsFileAsBAOutStream, String recId)
    {
        ByteArrayInputStream fileAsInputStream = new ByteArrayInputStream(recsFileAsBAOutStream.toByteArray());
    	RawRecordReader fileRawRecReader = new RawRecordReader(fileAsInputStream);    	
        while (fileRawRecReader.hasNext())
        {
            RawRecord rawRec = fileRawRecReader.next();
            if (recId == rawRec.getRecordId())
            	return rawRec;
        }
        return null;
    }
    
@Test
    public void testRawRecordAssertEqualsAndNot()
  		throws IOException
  	{
  	    String bibRecFileName = testDataParentPath + File.separator + "u335.mrc";

  	    RawRecordReader bibsRawRecRdr = new RawRecordReader(new FileInputStream(new File(bibRecFileName)));
  	    if (bibsRawRecRdr.hasNext()) {
  	    	
  	        RawRecord rawRec1 = bibsRawRecRdr.next();
  	        assertEquals(rawRec1, rawRec1);
  	        
  	        RawRecordReader bibsRawRecRdr2 = new RawRecordReader(new FileInputStream(new File(bibRecFileName)));
  	        if (bibsRawRecRdr2.hasNext()) {
  	  	        RawRecord rawRec2 = bibsRawRecRdr2.next();
  	  	        Record rec2 = rawRec2.getAsRecord(true, false, "999", "MARC8");
  	  	        DataField dataFld = new DataFieldImpl("333", ' ', ' ');
  	  	        rec2.addVariableField(dataFld);
  	  	        
  	  	        

// TODO: changing rec2 didn't affect rawrec  	  	        
  	  	        
  	  	        assertNotEqual(rawRec1,rawRec2);
  	        }
  	  	    else
  	            fail("shouldn't get here");
  	    }
  	    else
            fail("shouldn't get here");
  	}


	/**
	 * ensure that the assertEquals and assertNotEqual methods work for 
	 *  Record objects
	 */
@Test  	
  	public void testRecordAssertEqualsAndNot()
  	  		throws IOException
  	{
  	    String bibRecFileName = testDataParentPath + File.separator + "u335.mrc";

  	    RawRecordReader bibsRawRecRdr = new RawRecordReader(new FileInputStream(new File(bibRecFileName)));
  	    if (bibsRawRecRdr.hasNext()) 
  	    {
  	        RawRecord rawRec1 = bibsRawRecRdr.next();
  	        Record rec1 = rawRec1.getAsRecord(true, false, "999", "MARC8");

  	        assertEquals(rec1, rec1);
  	        
  	        RawRecordReader bibsRawRecRdr2 = new RawRecordReader(new FileInputStream(new File(bibRecFileName)));
  	        if (bibsRawRecRdr2.hasNext())
  	        {
  	  	        RawRecord rawRec2 = bibsRawRecRdr2.next();
  	  	        Record rec2 = rawRec2.getAsRecord(true, false, "999", "MARC8");
  	  	        DataField dataFld = new DataFieldImpl("333", ' ', ' ');
  	  	        rec2.addVariableField(dataFld);

  	  	        assertNotEqual(rec1, rec2);
  	        }
  	  	    else
  	            fail("shouldn't get here");
  	    }
  	    else
            fail("shouldn't get here");
  	}


@Test  	
	public void testRecordXMLAssertEquals()
	  		throws IOException
	{
	    String bibRecFileName = testDataParentPath + File.separator + "u335.mrc";

	    RawRecordReader bibsRawRecRdr = new RawRecordReader(new FileInputStream(new File(bibRecFileName)));
	    if (bibsRawRecRdr.hasNext())
	    {
		    RawRecord rawRec1 = bibsRawRecRdr.next();
		    String rec1AsXml = convertRawRecordToXml(rawRec1);
		    assertEqualsAsXml(rec1AsXml, rawRec1);

		    String notRec1AsXml = rec1AsXml.replace('a', '2');
		    assertEqualsAsXml(notRec1AsXml, rawRec1);
	        fail("shouldn't get here");
	    }
	    else
	    	fail("shouldn't get here");
	}

    
}