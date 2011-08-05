package org.solrmarc.tools;

import static org.junit.Assert.*;

import java.io.*;

import org.junit.*;
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
    String mrgMhldClassName = "org.solrmarc.tools.MergeSummaryHoldings";
    String mrcPrntrClassName = "org.solrmarc.marc.MarcPrinter";
    String mainMethodName = "main";
    // "Usage: MergeSummaryHoldings [-v] [-a] -s marcMhldFile.mrc  marcBibsFile.mrc";
    
    

    @Before
    public void setUp()
    {
        if (!Boolean.parseBoolean(System.getProperty("test.solr.verbose")))
        {
            java.util.logging.Logger.getLogger("org.apache.solr").setLevel(java.util.logging.Level.SEVERE);
            Utils.setLog4jLogLevel(org.apache.log4j.Level.WARN);
        }
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

    
    /**
     * code should smoothly continue if it encounters a bib with no MHLD
     */
    @Test
    public void testRewritingMHLDtoSameBib() 
            throws IOException
    {
        String mhldRecFileName = testDataParentPath + File.separator + "summaryHld_1-1000.mrc";
        String bibRecFileName = testDataParentPath + File.separator + "u335.mrc";

        InputStream inStr = null;
        ByteArrayOutputStream resultMrcOutStream = new ByteArrayOutputStream();
        String[] mergeMhldArgs = new String[]{"-s", mhldRecFileName, bibRecFileName };

        // call the code for mhldfile summaryHld_1-1000.mrc  and bibfile u335.mrc
        CommandLineUtils.runCommandLineUtil(mrgMhldClassName, mainMethodName, inStr, resultMrcOutStream, mergeMhldArgs);

        // take the resulting MARC bib rec with the MHLD fields and get it in a format for comparison with fixture data
        ByteArrayInputStream mergedMarcBibRecAsInStream = new ByteArrayInputStream(resultMrcOutStream.toByteArray());
        
        ByteArrayOutputStream marcPrinterOutputOfMergedBibRec = new ByteArrayOutputStream();
        ByteArrayOutputStream errorStream = new ByteArrayOutputStream();
        String[] marcPrintArgs = new String[]{testConfigFile, "print"};
        CommandLineUtils.runCommandLineUtil(mrcPrntrClassName, mainMethodName, mergedMarcBibRecAsInStream, marcPrinterOutputOfMergedBibRec, errorStream, marcPrintArgs); 

        // did the resulting merged record contain the expected output?
        assertMarcRecsEqual(mergedSummaryHoldingsOutput, new ByteArrayInputStream(marcPrinterOutputOfMergedBibRec.toByteArray()));

        
        // Now merge record again to test the deleting of existing summary holdings info
        resultMrcOutStream.close();
        resultMrcOutStream = new ByteArrayOutputStream();
        //  do the merge by piping the bib record in to the merge class
        CommandLineUtils.runCommandLineUtil(mrgMhldClassName, mainMethodName, mergedMarcBibRecAsInStream, resultMrcOutStream, new String[]{"-s", mhldRecFileName } );
        
        mergedMarcBibRecAsInStream.close();
        mergedMarcBibRecAsInStream = new ByteArrayInputStream(resultMrcOutStream.toByteArray());
        marcPrinterOutputOfMergedBibRec.close();
        marcPrinterOutputOfMergedBibRec = new ByteArrayOutputStream();
        CommandLineUtils.runCommandLineUtil(mrcPrntrClassName, mainMethodName, mergedMarcBibRecAsInStream, marcPrinterOutputOfMergedBibRec, marcPrintArgs); 

        assertMarcRecsEqual(mergedSummaryHoldingsOutput, new ByteArrayInputStream(marcPrinterOutputOfMergedBibRec.toByteArray()));
    }

    
    /**
     * code should smoothly continue if it encounters a bib with no MHLD
     */
    public void testNoMatchingMHLD() 
    {
        
    }
    
    /**
     * code should output an error message when it encounters an MHLD with no bib?
     */
    public void testNoMatchingBib() 
    {
    }
    
    /**
     * need to ensure all the MHLD data is included, not just the first record
     */
    public void testMultMHLDsWithSameID() 
    {
    }
    
    /**
     * the MHLD fields should only be merged into ONE of the bibs, if the bibs will be combined?
     * Or it's probably ok if they are in each bib, as they should be removed from the bib after processing?
     */
    public void testMultBibsWithSameID() 
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
    public void testFielsNotToMerge() 
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
    public void testCrashingBibFieldsRemoved() 
    {
    }
    
    
    
    /**
     * Given an expected marc record as an Array of strings corresponding to 
     *  the lines in a 
     * @param expectedAsLines
     * @param actualAsInputStream
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
                    assertEquals("output line ["+ actualLine + "]  doesn't match expected [" + expectedLine + "]", expectedLine, actualLine);
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
        

    
}
