package edu.stanford;

import static org.junit.Assert.*;

import java.io.*;
import java.util.*;

import org.junit.*;
import org.marc4j.marc.Record;
import org.solrmarc.marc.RawRecordReader;
import org.solrmarc.testUtils.*;
import org.solrmarc.tools.*;

import edu.stanford.marcUtils.MergeMhldFldsIntoBibsReader;

/**
 * Note that actual use of MergeMhldFldsIntoBibs is generally a call to main()
 *  but most of these tests use mergeMhldsIntoBibRecordsAsMap() for convenience
 * @author Naomi Dushay
 *
 */
public class MergeMhldFldsIntoBibsReaderTests
{
    static String coreTestDataParentPath =  "core" + File.separator + "test" + File.separator + "data";
    static String localTestDataParentPath = "stanford-sw" + File.separator + "test" + File.separator + "data";

    static String MERGE_MHLD_CLASS_NAME = "edu.stanford.marcUtils.MergeMhldFldsIntoBibsReader";
    static String MARC_PRINTER_CLASS_NAME = "org.solrmarc.marc.MarcPrinter";
    static String MAIN_METHOD_NAME = "main";

    // for vetting results - no point in loading these constants for each test
    static Map<String, Record> ALL_MERGED_BIB_RESULTS = new HashMap<String, Record>();
    static Map<String, Record> ALL_UNMERGED_BIBS = new HashMap<String, Record>();
    static
    {
		String bibFilePath = localTestDataParentPath + File.separator + "mhldMergeBibs1346.mrc";
		try {
			RawRecordReader rawRecRdr = new RawRecordReader(new FileInputStream(new File(bibFilePath)));
	        while (rawRecRdr.hasNext())
	        {
	        	RawRecord rawRec = rawRecRdr.next();
	        	Record rec = rawRec.getAsRecord(true, false, "999", "MARC8");
	        	String id = MarcUtils.getControlFieldData(rec, "001");
	        	ALL_UNMERGED_BIBS.put(id, rec);
	        }
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		bibFilePath = localTestDataParentPath + File.separator + "mhldMerged1346.mrc";
		try {
			RawRecordReader rawRecRdr = new RawRecordReader(new FileInputStream(new File(bibFilePath)));
	        while (rawRecRdr.hasNext())
	        {
	        	RawRecord rawRec = rawRecRdr.next();
	        	Record rec = rawRec.getAsRecord(true, false, "999", "MARC8");
	        	String id = MarcUtils.getControlFieldData(rec, "001");
	        	ALL_MERGED_BIB_RESULTS.put(id, rec);
	        }
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
    }


@Before
    public void setUp()
    {
		String testSolrLogLevel = System.getProperty("test.solr.log.level");
		String testSolrmarcLogLevel = System.getProperty("test.solrmarc.log.level");
		Utils.setLoggingLevels(testSolrLogLevel, testSolrmarcLogLevel);
    }

    /**
     * code should output the unchanged bib records if no mhlds match
     */
@Test
    public void testNoMatches()
    		throws IOException
    {
		// bib46, mhld235
		String bibFilePath = localTestDataParentPath + File.separator + "mhldMergeBibs46.mrc";
		String mhldFilePath = localTestDataParentPath + File.separator + "mhldMergeMhlds235.mrc";
	    Map<String, Record> mergedRecs = mergeRecordsAsMap(bibFilePath, mhldFilePath);
	    Set<String> mergedRecIds = mergedRecs.keySet();
	    assertEquals(2, mergedRecIds.size());

	    // result bibs should match the bib input because there was no merge
	    String id = "a4";
       	RecordTestingUtils.assertEquals(ALL_UNMERGED_BIBS.get(id), mergedRecs.get(id));
	    id = "a6";
       	RecordTestingUtils.assertEquals(ALL_UNMERGED_BIBS.get(id), mergedRecs.get(id));
    }

	/**
	 * code should end smoothly if it encounters no matches between bib and mhld
	 */
@Test
	public void testNoOutputMessagesWhenNoMatches()
			throws IOException
	{
		// bib46, mhld235
		String commandLinePathPrefix = ".." + File.separator + ".." + File.separator;
		String bibFilePath =  commandLinePathPrefix + localTestDataParentPath + File.separator + "mhldMergeBibs46.mrc";
		String mhldFilePath =  commandLinePathPrefix + localTestDataParentPath + File.separator + "mhldMergeMhlds235.mrc";

		// ensure no error message was printed
		ByteArrayOutputStream sysBAOS = TestingUtil.getSysMsgsBAOS();
		ByteArrayOutputStream mergedRecordsAsByteArrayOutStream = mergeAsBAOutputStream(bibFilePath, mhldFilePath);
		assertTrue("Output messages unexpectedly written: " + sysBAOS.toString(),  sysBAOS.size() == 0);
	}


// first record in file tests ----------
    /**
     * code should find a match when first bib matches first mhld
     */
@Test
	public void testBothFirstRecsMatch()
    		throws IOException
    {
    	// bib346, mhld34
		String bibFilePath = localTestDataParentPath + File.separator + "mhldMergeBibs346.mrc";
		String mhldFilePath = localTestDataParentPath + File.separator + "mhldMergeMhlds34.mrc";
	    Map<String, Record> mergedRecs = mergeRecordsAsMap(bibFilePath, mhldFilePath);

	    // there should be 3 results
	    Set<String> mergedRecIds = mergedRecs.keySet();
	    assertEquals(3, mergedRecIds.size());

	    // result bibs 3, 4 should have the mhld fields
	    String id = "a3";
       	RecordTestingUtils.assertEqualsIgnoreLeader(ALL_MERGED_BIB_RESULTS.get(id), mergedRecs.get(id));
       	id = "a4";
       	RecordTestingUtils.assertEqualsIgnoreLeader(ALL_MERGED_BIB_RESULTS.get(id), mergedRecs.get(id));
       	// result bib 6 should not be changed
	    id = "a6";
       	RecordTestingUtils.assertEquals(ALL_UNMERGED_BIBS.get(id), mergedRecs.get(id));
    }

    /**
     * code should find a match when first bib matches non-first mhld
     */
@Test
    public void testFirstBibMatchesNonFirstMhld()
			throws IOException
    {
    	//bib346, mhld235
		String bibFilePath = localTestDataParentPath + File.separator + "mhldMergeBibs346.mrc";
		String mhldFilePath = localTestDataParentPath + File.separator + "mhldMergeMhlds235.mrc";
	    Map<String, Record> mergedRecs = mergeRecordsAsMap(bibFilePath, mhldFilePath);

	    // there should be 3 results
	    Set<String> mergedRecIds = mergedRecs.keySet();
	    assertEquals(3, mergedRecIds.size());

	    // result bib 3 only should have the mhld fields
	    String id = "a3";
	   	RecordTestingUtils.assertEqualsIgnoreLeader(ALL_MERGED_BIB_RESULTS.get(id), mergedRecs.get(id));
	   	// result bibs 4 and 6 should not be changed
	   	id = "a4";
	   	RecordTestingUtils.assertEqualsIgnoreLeader(ALL_UNMERGED_BIBS.get(id), mergedRecs.get(id));
	    id = "a6";
	   	RecordTestingUtils.assertEquals(ALL_UNMERGED_BIBS.get(id), mergedRecs.get(id));
    }

    /**
     * code should find a match when non-first bib matches first mhld
     */
@Test
    public void testNonFirstBibMatchesFirstMhld()
    		throws IOException
    {
    	//bib134, mhld345
		String bibFilePath = localTestDataParentPath + File.separator + "mhldMergeBibs134.mrc";
		String mhldFilePath = localTestDataParentPath + File.separator + "mhldMergeMhlds345.mrc";
	    Map<String, Record> mergedRecs = mergeRecordsAsMap(bibFilePath, mhldFilePath);

	    // there should be 3 results
	    Set<String> mergedRecIds = mergedRecs.keySet();
	    assertEquals(3, mergedRecIds.size());

	    // result bibs 3 and 4 only should have the mhld fields
	    String id = "a1";
	   	RecordTestingUtils.assertEqualsIgnoreLeader(ALL_UNMERGED_BIBS.get(id), mergedRecs.get(id));
	    id = "a3";
	   	RecordTestingUtils.assertEqualsIgnoreLeader(ALL_MERGED_BIB_RESULTS.get(id), mergedRecs.get(id));
	   	id = "a4";
	   	RecordTestingUtils.assertEqualsIgnoreLeader(ALL_MERGED_BIB_RESULTS.get(id), mergedRecs.get(id));
    }

// last record in file tests ------------
    /**
     * code should find a match when last bib matches last mhld
     */
@Test
    public void testBothLastRecsMatch()
			throws IOException
    {
    	//bib46, mhld236
		String bibFilePath = localTestDataParentPath + File.separator + "mhldMergeBibs46.mrc";
		String mhldFilePath = localTestDataParentPath + File.separator + "mhldMergeMhlds236.mrc";
	    Map<String, Record> mergedRecs = mergeRecordsAsMap(bibFilePath, mhldFilePath);

	    // there should be 2 results
	    Set<String> mergedRecIds = mergedRecs.keySet();
	    assertEquals(2, mergedRecIds.size());

	    // result bib 6 only should have the mhld fields
	    String id = "a4";
	   	RecordTestingUtils.assertEqualsIgnoreLeader(ALL_UNMERGED_BIBS.get(id), mergedRecs.get(id));
	    id = "a6";
	   	RecordTestingUtils.assertEqualsIgnoreLeader(ALL_MERGED_BIB_RESULTS.get(id), mergedRecs.get(id));
    }

    /**
     * code should find a match when last bib matches non-last mhld
     */
@Test
    public void testLastBibMatchesNonLastMhld()
		throws IOException
    {
		//bib134, mhld345
		String bibFilePath = localTestDataParentPath + File.separator + "mhldMergeBibs134.mrc";
		String mhldFilePath = localTestDataParentPath + File.separator + "mhldMergeMhlds345.mrc";
	    Map<String, Record> mergedRecs = mergeRecordsAsMap(bibFilePath, mhldFilePath);

	    // there should be 3 results
	    Set<String> mergedRecIds = mergedRecs.keySet();
	    assertEquals(3, mergedRecIds.size());

	    // result bibs 3 and 4 only should have the mhld fields
	    String id = "a1";
	   	RecordTestingUtils.assertEqualsIgnoreLeader(ALL_UNMERGED_BIBS.get(id), mergedRecs.get(id));
	    id = "a3";
	   	RecordTestingUtils.assertEqualsIgnoreLeader(ALL_MERGED_BIB_RESULTS.get(id), mergedRecs.get(id));
	   	id = "a4";
	   	RecordTestingUtils.assertEqualsIgnoreLeader(ALL_MERGED_BIB_RESULTS.get(id), mergedRecs.get(id));
    }

    /**
     * code should find a match when non-last bib matches last mhld
     */
@Test
    public void testNonLastBibMatchesLastMhld()
			throws IOException
	{
    	//bib46, mhld34
		String bibFilePath = localTestDataParentPath + File.separator + "mhldMergeBibs46.mrc";
		String mhldFilePath = localTestDataParentPath + File.separator + "mhldMergeMhlds34.mrc";
	    Map<String, Record> mergedRecs = mergeRecordsAsMap(bibFilePath, mhldFilePath);

	    // there should be 2 results
	    Set<String> mergedRecIds = mergedRecs.keySet();
	    assertEquals(2, mergedRecIds.size());

	    // result bib 6 only should have the mhld fields
	    String id = "a4";
	   	RecordTestingUtils.assertEqualsIgnoreLeader(ALL_MERGED_BIB_RESULTS.get(id), mergedRecs.get(id));
	    id = "a6";
	   	RecordTestingUtils.assertEqualsIgnoreLeader(ALL_UNMERGED_BIBS.get(id), mergedRecs.get(id));
    }


    /**
     * need to ensure all the MHLD data is included, not just the first record
     */
@Test
    public void testMultMHLDsWithSameID()
			throws IOException
    {
    	//bib134, multMhlds1
		String bibFilePath = localTestDataParentPath + File.separator + "mhldMergeBibs134.mrc";
		String mhldFilePath = localTestDataParentPath + File.separator + "mhldMergeMhlds1Mult.mrc";
	    Map<String, Record> mergedRecs = mergeRecordsAsMap(bibFilePath, mhldFilePath);

	    Record mergedRec = mergedRecs.get("a1");
	    assertEquals("Wrong number of 852s ", 3, mergedRec.getVariableFields("852").size());
	    Set<String> expectedVals = new HashSet<String>();
	    expectedVals.add("Location1");
	    expectedVals.add("Location2");
	    RecordTestingUtils.assertSubfieldHasExpectedValues(mergedRec, "852", 'b', expectedVals);

	    assertEquals("Wrong number of 853s ", 3, mergedRec.getVariableFields("853").size());
	    expectedVals.clear();
	    expectedVals.add("(month)");
	    expectedVals.add("(season)");
	    RecordTestingUtils.assertSubfieldHasExpectedValues(mergedRec, "853", 'b', expectedVals);

	    assertEquals("Wrong number of 863s ", 2, mergedRec.getVariableFields("863").size());
	    expectedVals.clear();
	    expectedVals.add("1.1");
	    expectedVals.add("2.1");
	    RecordTestingUtils.assertSubfieldHasExpectedValues(mergedRec, "863", '8', expectedVals);

	    assertEquals("Wrong number of 866s", 1, mergedRec.getVariableFields("866").size());
    }

    /**
     * the MHLD fields should only be merged into ONE of the bibs, if the bibs will be combined?
     * Or it's probably ok if they are in each bib, as they should be removed from the bib after processing?
     */
@Test
    public void testMultBibsWithSameID()
    		throws IOException
    {
    	// multBibs4, mhld 34
    	String bibFilePath = localTestDataParentPath + File.separator + "mhldMergeBibs4Mult.mrc";
		String mhldFilePath = localTestDataParentPath + File.separator + "mhldMergeMhlds34.mrc";
	    Map<String, Record> mergedRecs = mergeRecordsAsMap(bibFilePath, mhldFilePath);

	    Record mergedRec = mergedRecs.get("a4");
	    assertEquals("Wrong number of 852 ", 1, mergedRec.getVariableFields("852").size());

	    assertEquals("Wrong number of 999s ", 5, mergedRec.getVariableFields("999").size());
    }

    /**
     * need to ensure all the MHLD data is included, not just the first record
     */
@Test
	public void testMultBothWithSameID()
    		throws IOException
    {
    	String bibFilePath = localTestDataParentPath + File.separator + "mhldMergeBibs4Mult.mrc";
		String mhldFilePath = localTestDataParentPath + File.separator + "mhldMergeMhlds4Mult.mrc";
	    Map<String, Record> mergedRecs = mergeRecordsAsMap(bibFilePath, mhldFilePath);

	    Record mergedRec = mergedRecs.get("a4");
	    // bib flds merged in
	    assertEquals("Wrong number of 999s ", 5, mergedRec.getVariableFields("999").size());

	    // mhld flds merged in
	    assertEquals("Wrong number of 852s ", 3, mergedRec.getVariableFields("852").size());
	    Set<String> expectedVals = new HashSet<String>();
	    expectedVals.add("Location1");
	    expectedVals.add("Location2");
	    RecordTestingUtils.assertSubfieldHasExpectedValues(mergedRec, "852", 'b', expectedVals);

	    assertEquals("Wrong number of 853s ", 3, mergedRec.getVariableFields("853").size());
	    expectedVals.clear();
	    expectedVals.add("(month)");
	    expectedVals.add("(season)");
	    RecordTestingUtils.assertSubfieldHasExpectedValues(mergedRec, "853", 'b', expectedVals);

	    assertEquals("Wrong number of 863s ", 2, mergedRec.getVariableFields("863").size());
	    expectedVals.clear();
	    expectedVals.add("1.1");
	    expectedVals.add("2.1");
	    RecordTestingUtils.assertSubfieldHasExpectedValues(mergedRec, "863", '8', expectedVals);

	    assertEquals("Wrong number of 866s", 1, mergedRec.getVariableFields("866").size());
    }



    /**
     * the bib record should get all mhld fields specified
     */
@Test
    public void testFieldsToMerge()
			throws IOException
    {
		// mhldMergeBibWmhldFlds, mhldMergeMhldAllFlds
		String bibFilePath = localTestDataParentPath + File.separator + "mhldMergeBibWmhldFlds.mrc";
		String mhldFilePath = localTestDataParentPath + File.separator + "mhldMergeMhldAllFlds.mrc";
	    Map<String, Record> mergedRecs = mergeRecordsAsMap(bibFilePath, mhldFilePath);

	    Record mergedRec = mergedRecs.get("aallMhldFlds");

	    // mhld flds merged in
	    assertEquals("Wrong number of 852s ", 1, mergedRec.getVariableFields("852").size());
	    Set<String> expectedVals = new HashSet<String>();
	    expectedVals.add("mhldLoc");
	    RecordTestingUtils.assertSubfieldHasExpectedValues(mergedRec, "852", 'b', expectedVals);

	    assertEquals("Wrong number of 853s ", 1, mergedRec.getVariableFields("853").size());
	    expectedVals.clear();
	    expectedVals.add("mhld853a");
	    RecordTestingUtils.assertSubfieldHasExpectedValues(mergedRec, "853", 'a', expectedVals);

	    assertEquals("Wrong number of 863s ", 1, mergedRec.getVariableFields("863").size());
	    expectedVals.clear();
	    expectedVals.add("mhld863a");
	    RecordTestingUtils.assertSubfieldHasExpectedValues(mergedRec, "863", 'a', expectedVals);

	    assertEquals("Wrong number of 866s", 3, mergedRec.getVariableFields("866").size());
	    expectedVals.clear();
	    expectedVals.add("V. 417 NO. 1A (JAN 2011)");
	    expectedVals.add("mhld866a ind1 blank ind2 0");
	    expectedVals.add("2009-");
	    expectedVals.add("mhld866a ind1 3 ind2 1");
	    expectedVals.add("mhld866a ind1 4 ind2 1");
	    RecordTestingUtils.assertSubfieldHasExpectedValues(mergedRec, "866", 'a', expectedVals);

	    assertEquals("Wrong number of 867s ", 1, mergedRec.getVariableFields("867").size());
	    expectedVals.clear();
	    expectedVals.add("mhld867a ind1 blank ind2 0");
	    RecordTestingUtils.assertSubfieldHasExpectedValues(mergedRec, "867", 'a', expectedVals);

	    assertEquals("Wrong number of 868s ", 3, mergedRec.getVariableFields("868").size());
	    expectedVals.clear();
	    expectedVals.add("mhld868a ind1 blank ind2 0");
	    expectedVals.add("mhld868a ind1 3 ind2 1");
	    expectedVals.add("1957-1993, 1995-1998");
	    expectedVals.add("mhld868a ind1 4 ind2 1");
	    RecordTestingUtils.assertSubfieldHasExpectedValues(mergedRec, "868", 'a', expectedVals);
    }

    /**
     * the bib record should not get any MHLD fields that aren't indicated for the merge
     */
@Test
    public void testFieldsNotToMerge()
    		throws IOException
    {
    	// mhldMergeBibWmhldFlds, mhldMergeMhldAllFlds
    	String bibFilePath = localTestDataParentPath + File.separator + "mhldMergeBibWmhldFlds.mrc";
		String mhldFilePath = localTestDataParentPath + File.separator + "mhldMergeMhldAllFlds.mrc";
	    Map<String, Record> mergedRecs = mergeRecordsAsMap(bibFilePath, mhldFilePath);

	    Record mergedRec = mergedRecs.get("aallMhldFlds");

	    // 901 should not be merged
	    assertEquals("Wrong number of 901s ", 1, mergedRec.getVariableFields("901").size());
	    Set<String> expectedVals = new HashSet<String>();
	    expectedVals.add("bib901a");
	    RecordTestingUtils.assertSubfieldHasExpectedValues(mergedRec, "901", 'a', expectedVals);
	    Set<String> unexpectedVals = new HashSet<String>();
	    unexpectedVals.add("mhld901a");
	    RecordTestingUtils.assertSubfieldDoesNotHaveValues(mergedRec, "901", 'a', unexpectedVals);
    }

    /**
     * if the MHLD has more than one instance of a field, all instances should be put in the bib record
     */
@Test
    public void testMultOccurFieldsToMerge()
    		throws IOException
    {
    	testFieldsToMerge();
    }


    /**
     * if the bib rec has existing fields included in the mhldFldsToMerge list,
     *  then those bib fields should be removed before adding the MHLD fields
     */
@Test
    public void testCrashingBibFieldsRemoved()
    		throws IOException
    {
    	// mhldMergeBibWmhldFlds, mhldMergeMhldAllFlds
    	String bibFilePath = localTestDataParentPath + File.separator + "mhldMergeBibWmhldFlds.mrc";
		String mhldFilePath = localTestDataParentPath + File.separator + "mhldMergeMhldAllFlds.mrc";
	    Map<String, Record> mergedRecs = mergeRecordsAsMap(bibFilePath, mhldFilePath);

	    Record mergedRec = mergedRecs.get("aallMhldFlds");

	    Set<String> unexpectedVals = new HashSet<String>();
	    unexpectedVals.add("bibLoc");
	    RecordTestingUtils.assertSubfieldDoesNotHaveValues(mergedRec, "852", 'b', unexpectedVals);

	    unexpectedVals.clear();
	    unexpectedVals.add("bib853a");
	    RecordTestingUtils.assertSubfieldDoesNotHaveValues(mergedRec, "853", 'a', unexpectedVals);

	    unexpectedVals.clear();
	    unexpectedVals.add("bib863a");
	    RecordTestingUtils.assertSubfieldDoesNotHaveValues(mergedRec, "863", 'a', unexpectedVals);

	    unexpectedVals.clear();
	    unexpectedVals.add("bib866a");
	    RecordTestingUtils.assertSubfieldDoesNotHaveValues(mergedRec, "866", 'a', unexpectedVals);

	    unexpectedVals.clear();
	    unexpectedVals.add("bib867a");
	    unexpectedVals.add("bib867a2");
	    RecordTestingUtils.assertSubfieldDoesNotHaveValues(mergedRec, "867", 'a', unexpectedVals);

	    unexpectedVals.clear();
	    unexpectedVals.add("bib868a");
	    RecordTestingUtils.assertSubfieldDoesNotHaveValues(mergedRec, "868", 'a', unexpectedVals);
    }


// Tests for very basic functionality of code, including Bob's original test (with some modifications to run as a more typical junit test)

String MERGED_BIB335_OUTPUT[] = {
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

String MERGED_BIB335_OUTPUT_NO_UMLAUT[] = {
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
	 * test methods that return Map of ids to Records and no sysout stuff
	 */
@Test
	public void testGettingOutputAsMapOfRecords()
	        throws IOException
	{
	    String mhldRecFileName = coreTestDataParentPath + File.separator + "summaryHld_1-1000.mrc";
	    String bibRecFileName = coreTestDataParentPath + File.separator + "u335.mrc";

	    Map<String, Record> mergedRecs = mergeRecordsAsMap(bibRecFileName, mhldRecFileName);

	    junit.framework.Assert.assertEquals("results should have 1 record", 1, mergedRecs.size());
	    String expId = "u335";
	    assertTrue("Record with id " + expId + " should be in results", mergedRecs.containsKey(expId));

	    Record resultRec = mergedRecs.get(expId);
	    RecordTestingUtils.assertEqualsIgnoreLeader(MERGED_BIB335_OUTPUT_NO_UMLAUT, resultRec);
	}


	/**
	 * Test mergeMhldRecsIntoBibRecsAsStdOut method (distinct from Bob's old way)
	 */
//@Test
	public void testMergeToStdOut()
	        throws IOException
	{
	    String mhldRecFileName = coreTestDataParentPath + File.separator + "summaryHld_1-1000.mrc";
	    String bibRecFileName = coreTestDataParentPath + File.separator + "u335.mrc";

		ByteArrayOutputStream sysBAOS = TestingUtil.getSysMsgsBAOS();
		MergeMhldFldsIntoBibsReader.mergeMhldRecsIntoBibRecsAsStdOut(bibRecFileName, mhldRecFileName);
		RecordTestingUtils.assertMarcRecsEqual(MERGED_BIB335_OUTPUT, sysBAOS);
	}


// supporting methods for testing ----------------------------------------------

	/**
     * @param bibRecsFileName name of the file containing Bib records, relative to the localTestDataParentPath
     * @param mhldRecsFileName name of the file containing MHLD records, relative to the localTestDataParentPath
     * @return the resulting merged bib file as a ByteArrayOutputStream
     */
    private ByteArrayOutputStream mergeAsBAOutputStream(String bibRecsFileName, String mhldRecsFileName)
    {
        String fullBibRecsFileName = localTestDataParentPath + File.separator + bibRecsFileName;
        String fullMhldRecsFileName = localTestDataParentPath + File.separator + mhldRecsFileName;

        InputStream inStr = null;
		ByteArrayOutputStream resultMrcOutStream = TestingUtil.getSysMsgsBAOS();
        String[] mergeMhldArgs = new String[]{"-s", fullMhldRecsFileName, fullBibRecsFileName };

        // call the MergeMhldFldsIntoBibs code from the command line
        CommandLineUtils.runCommandLineUtil(MERGE_MHLD_CLASS_NAME, MAIN_METHOD_NAME, inStr, resultMrcOutStream, mergeMhldArgs);
        return resultMrcOutStream;
    }

	/**
	 * basically for testing
	 * for each bib record in the bib rec file
	 *  look for a corresponding mhld record.  If a match is found,
	 *    1) remove any existing fields in the bib record that duplicate the mhld fields to be merged into the bib record
	 *    2) merge the mhld fields into the bib record
	 * then add the bib record (whether it had a match or not) to the List of records
	 * @param bibRecsFileName - the name of the file containing MARC Bibliographic records
	 * @param mhldRecsFileName - the name of the file containing MARC MHLD records
	 * @return Map of ids -> Record objects for the bib records, which will include mhld fields if a match was found
	 */
	public static Map<String, Record> mergeRecordsAsMap(String bibRecsFileName, String mhldRecsFileName)
		throws IOException
	{
		Map<String, Record> results = new HashMap<String, Record>();

		boolean permissive = true;
	    boolean toUtf8 = false;
	    String defaultEncoding = "MARC8";
	    MergeMhldFldsIntoBibsReader merger = new MergeMhldFldsIntoBibsReader(bibRecsFileName, permissive, toUtf8, defaultEncoding,
	                                                           mhldRecsFileName, MergeMhldFldsIntoBibsReader.DEFAULT_MHLD_FLDS_TO_MERGE);

	    while (merger.hasNext())
	    {
	    	Record bibRecWithPossChanges = merger.next();
	    	results.put(MarcUtils.getControlFieldData(bibRecWithPossChanges, "001"), bibRecWithPossChanges);
	    }
	    return results;
	}

}
