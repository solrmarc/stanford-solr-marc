package edu.stanford;

import java.io.*;

import org.junit.*;
import static org.junit.Assert.*;

import edu.stanford.enumValues.CallNumberType;

/**
 * junit4 tests for Stanford University 
 *   cope properly with strange call numbers from Lane and Jackson
 * @author Naomi Dushay
 */
public class CallNumLaneJacksonTests extends AbstractStanfordBlacklightTest {

	private String fileName = "callNumLaneJackTests.mrc";
    private String testFilePath = testDataParentPath + File.separator + fileName;

@Before
	public final void setup() 
	{
		mappingTestInit();
	}	

	// non-LC call number from Jackson and Lane are common, because all of 
	//   the items are labeled LC.   Treat them as follows:
	// parsing error message should not be printed
	// search results - item_display  lopped callnum  should be full callnum
	// record view - item_display   full callnum, sort callnum  should be populated
	// availability - item_display   should have barcode as given
	// facets  - not assigned if non-LC
	// nearby  - not assigned if non-LC  (preferred_barcode, shelfkey, reversekey, item_display)
	
	/**
	 * no error messages should be printed if lane or jackson call number is
	 *   invalid, but error messages ARE printed if a diff library call number
	 *   is invalid LC
	 */
@Test
	public final void testNoErrorMessages() 
	{
		ByteArrayOutputStream sysBAOS = new ByteArrayOutputStream();
		PrintStream sysMsgs = new PrintStream(sysBAOS);
		System.setErr(sysMsgs);
		System.setOut(sysMsgs);
		
		String fldName = "preferred_barcode";
		solrFldMapTest.assertNoSolrFld(testFilePath, "letterY", fldName);
		assertTrue("Output messages unexpectedly written: " + sysBAOS.toString(),  sysBAOS.size() == 0);

		// reading in to last record, so there will now be system output messages
		solrFldMapTest.assertSolrFldValue(testFilePath, "greenInvalidLC", fldName, "94025");
		assertTrue("Output messages expected for Green invalid LC",  sysBAOS.size() != 0);
	}
	

	/**
	 * There should be no call number facet values when call number is 
	 *   invalid LC and library is Lane or Jackson
	 */
@Test
	public final void testFacetVals() 
	{
		String fldName = "callnum_top_facet";
	    solrFldMapTest.assertSolrFldValue(testFilePath, "LaneValidLC", fldName, "A - General Works");
	    solrFldMapTest.assertSolrFldValue(testFilePath, "LaneValidDewey", fldName, ItemUtils.DEWEY_TOP_FACET_VAL);
	    solrFldMapTest.assertSolrFldValue(testFilePath, "JacksonValidLC", fldName, "A - General Works");
	    solrFldMapTest.assertSolrFldValue(testFilePath, "JacksonValidDewey", fldName, ItemUtils.DEWEY_TOP_FACET_VAL);
		solrFldMapTest.assertNoSolrFld(testFilePath, "7811196", fldName);
		solrFldMapTest.assertNoSolrFld(testFilePath, "8238755", fldName);
		solrFldMapTest.assertNoSolrFld(testFilePath, "8373645", fldName);
		solrFldMapTest.assertNoSolrFld(testFilePath, "7575731", fldName);
		solrFldMapTest.assertNoSolrFld(testFilePath, "7672538", fldName);
		solrFldMapTest.assertNoSolrFld(testFilePath, "quote", fldName);
		solrFldMapTest.assertNoSolrFld(testFilePath, "paren", fldName);
		solrFldMapTest.assertNoSolrFld(testFilePath, "period", fldName);
		solrFldMapTest.assertNoSolrFld(testFilePath, "7603175", fldName);
		solrFldMapTest.assertNoSolrFld(testFilePath, "letterO", fldName);
		solrFldMapTest.assertNoSolrFld(testFilePath, "letterY", fldName);
		solrFldMapTest.assertNoSolrFld(testFilePath, "greenInvalidLC", fldName);
	}
	
	private static String LC_SKEY = "lc a   0027.000000 b0.360000";
	private static String DEWEY_SKEY = "dewey 666.00000000 t666";
	private static String OTHER_SKEY = "other y000210 .a000003f000006 001973";
	private static String LC_RSKEY = org.solrmarc.tools.CallNumUtils.getReverseShelfKey(LC_SKEY).toLowerCase();
	private static String DEWEY_RSKEY = org.solrmarc.tools.CallNumUtils.getReverseShelfKey(DEWEY_SKEY).toLowerCase();
	private static String OTHER_RSKEY = org.solrmarc.tools.CallNumUtils.getReverseShelfKey(OTHER_SKEY).toLowerCase();


	/**
	 * There should be no shelfkey values when call number is 
	 *   invalid LC and library is Lane or Jackson (item will not be part of
	 *   nearby-on-shelf scroll)
	 */
@Test
	public final void testNoShelfkey() 
	{
		String fldName = "shelfkey";
	    solrFldMapTest.assertSolrFldValue(testFilePath, "LaneValidLC", fldName, LC_SKEY);
	    solrFldMapTest.assertSolrFldValue(testFilePath, "LaneValidDewey", fldName, DEWEY_SKEY);
	    solrFldMapTest.assertSolrFldValue(testFilePath, "JacksonValidLC", fldName, LC_SKEY);
	    solrFldMapTest.assertSolrFldValue(testFilePath, "JacksonValidDewey", fldName, DEWEY_SKEY);
		solrFldMapTest.assertNoSolrFld(testFilePath, "7811196", fldName);
		solrFldMapTest.assertNoSolrFld(testFilePath, "8238755", fldName);
		solrFldMapTest.assertNoSolrFld(testFilePath, "8373645", fldName);
		solrFldMapTest.assertNoSolrFld(testFilePath, "7575731", fldName);
		solrFldMapTest.assertNoSolrFld(testFilePath, "7672538", fldName);
		solrFldMapTest.assertNoSolrFld(testFilePath, "quote", fldName);
		solrFldMapTest.assertNoSolrFld(testFilePath, "paren", fldName);
		solrFldMapTest.assertNoSolrFld(testFilePath, "period", fldName);
		solrFldMapTest.assertNoSolrFld(testFilePath, "7603175", fldName);
		solrFldMapTest.assertNoSolrFld(testFilePath, "letterO", fldName);
		solrFldMapTest.assertNoSolrFld(testFilePath, "letterY", fldName);
		solrFldMapTest.assertSolrFldValue(testFilePath, "greenInvalidLC", fldName, OTHER_SKEY);
	}

	/**
	 * There should be no reverse shelfkey values when call number is 
	 *   invalid LC and library is Lane or Jackson (item will not be part of
	 *   nearby-on-shelf scroll)
	 */
@Test
	public final void testNoReversekey() 
	{
		String fldName = "reverse_shelfkey";
	    solrFldMapTest.assertSolrFldValue(testFilePath, "LaneValidLC", fldName, LC_RSKEY);
	    solrFldMapTest.assertSolrFldValue(testFilePath, "LaneValidDewey", fldName, DEWEY_RSKEY);
	    solrFldMapTest.assertSolrFldValue(testFilePath, "JacksonValidLC", fldName, LC_RSKEY);
	    solrFldMapTest.assertSolrFldValue(testFilePath, "JacksonValidDewey", fldName, DEWEY_RSKEY);
		solrFldMapTest.assertNoSolrFld(testFilePath, "7811196", fldName);
		solrFldMapTest.assertNoSolrFld(testFilePath, "8238755", fldName);
		solrFldMapTest.assertNoSolrFld(testFilePath, "8373645", fldName);
		solrFldMapTest.assertNoSolrFld(testFilePath, "7575731", fldName);
		solrFldMapTest.assertNoSolrFld(testFilePath, "7672538", fldName);
		solrFldMapTest.assertNoSolrFld(testFilePath, "quote", fldName);
		solrFldMapTest.assertNoSolrFld(testFilePath, "paren", fldName);
		solrFldMapTest.assertNoSolrFld(testFilePath, "period", fldName);
		solrFldMapTest.assertNoSolrFld(testFilePath, "7603175", fldName);
		solrFldMapTest.assertNoSolrFld(testFilePath, "letterO", fldName);
		solrFldMapTest.assertNoSolrFld(testFilePath, "letterY", fldName);
		solrFldMapTest.assertSolrFldValue(testFilePath, "greenInvalidLC", fldName, OTHER_RSKEY);
	}


	/**
	 * There should be no preferred item barcode value when call number is 
	 *   invalid LC and library is Lane or Jackson.  (This means there is no
	 *   preferred item for nearby-on-shelf)
	 */
@Test
	public final void testNoPreferredItemBarcode() 
	{
		String fldName = "preferred_barcode";
	    solrFldMapTest.assertSolrFldValue(testFilePath, "LaneValidLC", fldName, "LL111");
	    solrFldMapTest.assertSolrFldValue(testFilePath, "LaneValidDewey", fldName, "LL666");
	    solrFldMapTest.assertSolrFldValue(testFilePath, "JacksonValidLC", fldName, "JJ111");
	    solrFldMapTest.assertSolrFldValue(testFilePath, "JacksonValidDewey", fldName, "JJ666");
		solrFldMapTest.assertNoSolrFld(testFilePath, "7811196", fldName);
		solrFldMapTest.assertNoSolrFld(testFilePath, "8238755", fldName);
		solrFldMapTest.assertNoSolrFld(testFilePath, "8373645", fldName);
		solrFldMapTest.assertNoSolrFld(testFilePath, "7575731", fldName);
		solrFldMapTest.assertNoSolrFld(testFilePath, "7672538", fldName);
		solrFldMapTest.assertNoSolrFld(testFilePath, "quote", fldName);
		solrFldMapTest.assertNoSolrFld(testFilePath, "paren", fldName);
		solrFldMapTest.assertNoSolrFld(testFilePath, "period", fldName);
		solrFldMapTest.assertNoSolrFld(testFilePath, "7603175", fldName);
		solrFldMapTest.assertNoSolrFld(testFilePath, "letterO", fldName);
		solrFldMapTest.assertNoSolrFld(testFilePath, "letterY", fldName);
		solrFldMapTest.assertSolrFldValue(testFilePath, "greenInvalidLC", fldName, "94025");
	}
	
	/**
	 * item_display field should have:
	 *   barcode, library, locations, etc as in 999
	 *   lopped callnum as full callnum
	 *   shelfkey and reversekey parts empty (no nearby-on-shelf)
	 *   full callnum, sort callnum populated
	 */
@Test
	public final void testItemDisplay() 
	{
		String fldName = "item_display";
		String sep = " -|- ";
		String barcode = "LL111";
		String LANE = "LANE-MED";
		String JACKSON = "JACKSON";
		String ASK_LANE = "ASK@LANE";
		String ASK_JACK = "ASK@GSB";
		String callnum = "A27 .B36";
		String fldVal = barcode + sep + LANE + sep + ASK_LANE + sep + sep + sep +
						callnum + sep + LC_SKEY + sep + LC_RSKEY + sep + callnum + sep + LC_SKEY;
	    solrFldMapTest.assertSolrFldValue(testFilePath, "LaneValidLC", fldName, fldVal);
		fldVal = "JJ111" + sep + JACKSON + sep + ASK_JACK + sep + sep + sep +
					callnum + sep + LC_SKEY + sep + LC_RSKEY + sep + callnum + sep + LC_SKEY;
	    solrFldMapTest.assertSolrFldValue(testFilePath, "JacksonValidLC", fldName, fldVal);
	    callnum = "666 .T666";
		fldVal = "JJ666" + sep + JACKSON + sep + ASK_JACK + sep + sep + sep +
					callnum + sep + DEWEY_SKEY + sep + DEWEY_RSKEY + sep + callnum + sep + DEWEY_SKEY;
	    solrFldMapTest.assertSolrFldValue(testFilePath, "JacksonValidDewey", fldName, fldVal);
		fldVal = "LL666" + sep + LANE + sep + ASK_LANE + sep + sep + sep +
					callnum + sep + DEWEY_SKEY + sep + DEWEY_RSKEY + sep + callnum + sep + DEWEY_SKEY;
	    solrFldMapTest.assertSolrFldValue(testFilePath, "LaneValidDewey", fldName, fldVal);
	    
	    String id = "7811196";
		solrFldMapTest.assertSolrFldValue(testFilePath, id, fldName, 
				getJackInvalidLCItemDispVal(id, "JJ181806", "KHREHBIEL/MALHOTRA/MONIN/SAUMITRA"));
		
		id = "8238755";
		solrFldMapTest.assertSolrFldValue(testFilePath, id, fldName, 
				getJackInvalidLCItemDispVal(id, "JJ182661", "ARCHIVES N&P 090606"));

		id = "8373645";
		solrFldMapTest.assertSolrFldValue(testFilePath, id, fldName, 
				getLaneInvalidLCItemDispVal(id, "LL289799", "3781 2009 T"));

		id = "7575731";
		// NOTE:  trailing periods removed in LC normalization
		solrFldMapTest.assertSolrFldValue(testFilePath, id, fldName, 
				getJackInvalidLCItemDispVal(id, "JL28537S", "1ST AMERICAN BANCORP, INC"));
		solrFldMapTest.assertSolrFldValue(testFilePath, id, fldName, 
				getJackInvalidLCItemDispVal(id, "JL11613S", "202 DATA SYSTEMS, INC"));

		id = "7672538";
		solrFldMapTest.assertSolrFldValue(testFilePath, id, fldName, 
				getLaneInvalidLCItemDispVal(id, "LL238400", "6.4C-CZ[BC]"));
		solrFldMapTest.assertSolrFldValue(testFilePath, id, fldName, 
				getLaneInvalidLCItemDispVal(id, "LL229390", "8.99"));
		
		id = "quote";
		solrFldMapTest.assertSolrFldValue(testFilePath, id, fldName, 
				getJackInvalidLCItemDispVal(id, "JL36924S", "\"NEW BEGINNING\" INVESTMENT RESERVE FUND,"));
		solrFldMapTest.assertSolrFldValue(testFilePath, id, fldName, 
				getJackInvalidLCItemDispVal(id, "JL41534S", "\"21\" BRANDS, INCORPORATED"));

		id = "paren";
		solrFldMapTest.assertSolrFldValue(testFilePath, id, fldName, 
				// note:  trailing period removed in normalization
				getJackInvalidLCItemDispVal(id, "JL8237S", "(THE) NWNL COMPANIES, INC"));

		id = "period";
		solrFldMapTest.assertSolrFldValue(testFilePath, id, fldName, 
				getLaneInvalidLCItemDispVal(id, "LL90670", ".W42 1996"));
		solrFldMapTest.assertSolrFldValue(testFilePath, id, fldName, 
				getLaneInvalidLCItemDispVal(id, "LL89612", ".G59"));

		id = "7603175";
		solrFldMapTest.assertSolrFldValue(testFilePath, id, fldName, 
				getLaneInvalidLCItemDispVal(id, "LL205659", "???"));

		id = "letterO";
		solrFldMapTest.assertSolrFldValue(testFilePath, id, fldName, 
				getJackInvalidLCItemDispVal(id, "JJ175237", "O'REILLY"));
		solrFldMapTest.assertSolrFldValue(testFilePath, id, fldName,
				getJackInvalidLCItemDispVal(id, "JJ183188", "ONLINE RESOURCE"));

		id = "letterY";
		solrFldMapTest.assertSolrFldValue(testFilePath, "letterY", fldName, 
				getLaneInvalidLCItemDispVal(id, "LL41857", "Y210 .A3F6 1973"));

		barcode = "94025";
		callnum = "Y210 .A3F6 1973";
		fldVal = barcode + sep + "GREEN" + sep + "STACKS" + sep + sep + sep +
					callnum + sep + OTHER_SKEY + sep + OTHER_RSKEY + sep + callnum + sep + OTHER_SKEY;
		solrFldMapTest.assertSolrFldValue(testFilePath, "greenInvalidLC", fldName, fldVal);
	}


	/**
	 * @return item_display value for given id, barcode and callnum, assuming
	 *   there should be no shelfkey or reversekey in the result, and the 
	 *   lopped and full call numbers are the same
	 */
	private String getLaneInvalidLCItemDispVal(String id, String barcode, String callnum) 
	{
		String SEP = " -|- ";
		String LANE = "LANE-MED";
		String ASK_LANE = "ASK@LANE";
	    String shelfkey = edu.stanford.CallNumUtils.getShelfKey(callnum, CallNumberType.OTHER, id).toLowerCase();
		return barcode + SEP + LANE + SEP + ASK_LANE + SEP + SEP + SEP +
				callnum + SEP + SEP + SEP + callnum + SEP + shelfkey;
	}
	
	/**
	 * @return item_display value for given id, barcode and callnum, assuming
	 *   there should be no shelfkey or reversekey in the result, and the 
	 *   lopped and full call numbers are the same
	 */
	private String getJackInvalidLCItemDispVal(String id, String barcode, String callnum) 
	{
		String SEP = " -|- ";
		String JACKSON = "JACKSON";
		String ASK_JACK = "ASK@GSB";
	    String shelfkey = edu.stanford.CallNumUtils.getShelfKey(callnum, CallNumberType.OTHER, id).toLowerCase();
		return barcode + SEP + JACKSON + SEP + ASK_JACK + SEP + SEP + SEP +
				callnum + SEP + SEP + SEP + callnum + SEP + shelfkey;
	}

}