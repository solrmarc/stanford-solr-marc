package edu.stanford;

import java.io.*;

import org.junit.*;

import edu.stanford.enumValues.CallNumberType;

/**
 * junit4 tests for Stanford University 
 *   cope properly with missing/lost items - display, discoverable  SW-234
 * @author Naomi Dushay
 */
public class ItemMissingTests extends AbstractStanfordBlacklightTest {

	private String fileName = "missingItems.mrc";
    private String testFilePath = testDataParentPath + File.separator + fileName;

@Before
	public final void setup() 
	{
		mappingTestInit();
	}	

	// search results - display:   item_display    lopped callnum
	// record view - display:   item_display   full callnum, sort callnum  should be populated
	// availability - display (based on current loc, not jenson?):
	// facets  - not assigned
	// shelflist  - not assigned  (preferred_barcode, shelfkey, reversekey, item_display)
	

	/**
	 * There should be no call number facet values when item is missing
	 */
@Test
	public final void testFacetVals() 
	{
		String fldName = "callnum_top_facet";
	    solrFldMapTest.assertSolrFldHasNoValue(testFilePath, "onlyMissing", fldName, CallNumUtils.DEWEY_TOP_FACET_VAL);
		solrFldMapTest.assertNoSolrFld(testFilePath, "onlyMissing", fldName);
	    solrFldMapTest.assertSolrFldValue(testFilePath, "missingDiff", fldName, "B - Philosophy, Psychology, Religion");
	    solrFldMapTest.assertSolrFldHasNoValue(testFilePath, "missingDiff", fldName, "A - General Works");
	    solrFldMapTest.assertSolrFldValue(testFilePath, "missingSame", fldName, "T - Technology");

	    solrFldMapTest.assertSolrFldHasNoValue(testFilePath, "onlyLost", fldName, CallNumUtils.DEWEY_TOP_FACET_VAL);
		solrFldMapTest.assertNoSolrFld(testFilePath, "onlyLost", fldName);
	    solrFldMapTest.assertSolrFldValue(testFilePath, "lostDiff", fldName, "B - Philosophy, Psychology, Religion");
	    solrFldMapTest.assertSolrFldHasNoValue(testFilePath, "lostDiff", fldName, "A - General Works");
	    solrFldMapTest.assertSolrFldValue(testFilePath, "lostSame", fldName, "T - Technology");	
	}
	
	private static String LCA_CNUM = "A1 .B2";
	private static String LCA_SKEY = CallNumUtils.getShelfKey(LCA_CNUM, CallNumberType.LC, null).toLowerCase();
	private static String LCB_CNUM = "B2 .C3";
	private static String LCB_SKEY = CallNumUtils.getShelfKey(LCB_CNUM, CallNumberType.LC, null).toLowerCase();
	private static String LCT_CNUM = "TR692 .P37";
	private static String LCT_CNUM_ELLIP = "TR692 .P37 ...";
	private static String LCT_SKEY = CallNumUtils.getShelfKey(LCT_CNUM, CallNumberType.LC, null).toLowerCase();
	private static String LCT_SKEY_ELLIP = CallNumUtils.getShelfKey(LCT_CNUM_ELLIP, CallNumberType.LC, null).toLowerCase();
	private static String DEWEY_CNUM = "363.2 .V349";
	private static String DEWEY_SKEY = CallNumUtils.getShelfKey(DEWEY_CNUM, CallNumberType.DEWEY, null).toLowerCase();
	
	private static String LCA_RSKEY = org.solrmarc.tools.CallNumUtils.getReverseShelfKey(LCA_SKEY).toLowerCase();
	private static String LCB_RSKEY = org.solrmarc.tools.CallNumUtils.getReverseShelfKey(LCB_SKEY).toLowerCase();
	private static String LCT_RSKEY = org.solrmarc.tools.CallNumUtils.getReverseShelfKey(LCT_SKEY).toLowerCase();
	private static String LCT_RSKEY_ELLIP = org.solrmarc.tools.CallNumUtils.getReverseShelfKey(LCT_SKEY_ELLIP).toLowerCase();
	private static String DEWEY_RSKEY = org.solrmarc.tools.CallNumUtils.getReverseShelfKey(DEWEY_SKEY).toLowerCase();


	/**
	 * There should be no shelfkey values when item is missing 
	 *  (item will not be part of nearby-on-shelf scroll)
	 */
@Test
	public final void testNoShelfkey() 
	{
		String fldName = "shelfkey";
	    solrFldMapTest.assertSolrFldHasNoValue(testFilePath, "onlyMissing", fldName, DEWEY_SKEY);
		solrFldMapTest.assertNoSolrFld(testFilePath, "onlyMissing", fldName);
	    solrFldMapTest.assertSolrFldHasNoValue(testFilePath, "missingDiff", fldName, LCA_SKEY);
	    solrFldMapTest.assertSolrFldValue(testFilePath, "missingDiff", fldName, LCB_SKEY);
	    solrFldMapTest.assertSolrFldValue(testFilePath, "missingSame", fldName, LCT_SKEY_ELLIP);
	    solrFldMapTest.assertSolrFldHasNoValue(testFilePath, "missingSame", fldName, LCT_SKEY);

	    solrFldMapTest.assertSolrFldHasNoValue(testFilePath, "onlyLost", fldName, DEWEY_SKEY);
		solrFldMapTest.assertNoSolrFld(testFilePath, "onlyLost", fldName);
	    solrFldMapTest.assertSolrFldHasNoValue(testFilePath, "lostDiff", fldName, LCA_SKEY);
	    solrFldMapTest.assertSolrFldValue(testFilePath, "lostDiff", fldName, LCB_SKEY);
	    solrFldMapTest.assertSolrFldValue(testFilePath, "lostSame", fldName, LCT_SKEY_ELLIP);
	    solrFldMapTest.assertSolrFldHasNoValue(testFilePath, "lostSame", fldName, LCT_SKEY);
	}

	/**
	 * There should be no reverse shelfkey values when item is missing
	 *  (item will not be part of nearby-on-shelf scroll)
	 */
@Test
	public final void testNoReversekey() 
	{
		String fldName = "reverse_shelfkey";
		
	    solrFldMapTest.assertSolrFldHasNoValue(testFilePath, "onlyMissing", fldName, DEWEY_RSKEY);
		solrFldMapTest.assertNoSolrFld(testFilePath, "onlyMissing", fldName);
	    solrFldMapTest.assertSolrFldHasNoValue(testFilePath, "missingDiff", fldName, LCA_RSKEY);
	    solrFldMapTest.assertSolrFldValue(testFilePath, "missingDiff", fldName, LCB_RSKEY);
	    solrFldMapTest.assertSolrFldValue(testFilePath, "missingSame", fldName, LCT_RSKEY_ELLIP);
	    solrFldMapTest.assertSolrFldHasNoValue(testFilePath, "missingSame", fldName, LCT_RSKEY);

	    solrFldMapTest.assertSolrFldHasNoValue(testFilePath, "onlyLost", fldName, DEWEY_RSKEY);
		solrFldMapTest.assertNoSolrFld(testFilePath, "onlyLost", fldName);
	    solrFldMapTest.assertSolrFldHasNoValue(testFilePath, "lostDiff", fldName, LCA_RSKEY);
	    solrFldMapTest.assertSolrFldValue(testFilePath, "lostDiff", fldName, LCB_RSKEY);
	    solrFldMapTest.assertSolrFldValue(testFilePath, "lostSame", fldName, LCT_RSKEY_ELLIP);
	    solrFldMapTest.assertSolrFldHasNoValue(testFilePath, "lostSame", fldName, LCT_RSKEY);
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
		solrFldMapTest.assertNoSolrFld(testFilePath, "onlyMissing", fldName);
	    solrFldMapTest.assertSolrFldHasNoValue(testFilePath, "missingDiff", fldName, "111");
	    solrFldMapTest.assertSolrFldValue(testFilePath, "missingDiff", fldName, "222");
	    solrFldMapTest.assertSolrFldHasNoValue(testFilePath, "missingSame", fldName, "36105042256730");

	    solrFldMapTest.assertNoSolrFld(testFilePath, "onlyLost", fldName);
	    solrFldMapTest.assertSolrFldHasNoValue(testFilePath, "lostDiff", fldName, "111");
	    solrFldMapTest.assertSolrFldValue(testFilePath, "lostDiff", fldName, "222");
	    solrFldMapTest.assertSolrFldHasNoValue(testFilePath, "lostSame", fldName, "36105042256730");
	}
	

	/**
	 * for missing items, item_display field should have:
	 *   barcode, library, locations, etc as in 999
	 *   lopped callnum 
	 *   shelfkey and reversekey parts empty (no nearby-on-shelf)
	 *   full callnum, sort callnum populated
	 */
@Test
	public final void testItemDisplay() 
	{
		String fldName = "item_display";
		String sep = " -|- ";
		String sal = "SAL";
		String green = "GREEN";
		String stacks = "STACKS";
		String missing = "MISSING";

		String id = "onlyMissing";
		String barcode = "36105002467384";
		String firstPart = barcode + sep + sal + sep + stacks + sep + missing + sep + sep + DEWEY_CNUM + sep;
		String fldVal = firstPart + DEWEY_SKEY + sep + DEWEY_RSKEY + sep + DEWEY_CNUM + sep + DEWEY_SKEY;
	    solrFldMapTest.assertSolrFldHasNoValue(testFilePath, id, fldName, fldVal);
		fldVal = firstPart + sep + sep + DEWEY_CNUM + sep + DEWEY_SKEY;
		solrFldMapTest.assertSolrFldValue(testFilePath, id, fldName, fldVal);

		id = "missingDiff";
	    barcode = "111"; // missing - no shelfkey pieces
	    firstPart = barcode + sep + sal + sep + stacks + sep + missing + sep + sep + LCA_CNUM + sep;
		fldVal = firstPart + LCA_SKEY + sep + LCA_RSKEY + sep + LCA_CNUM + sep + LCA_SKEY;
	    solrFldMapTest.assertSolrFldHasNoValue(testFilePath, id, fldName, fldVal);
		fldVal = firstPart + sep + sep + LCA_CNUM + sep + LCA_SKEY;
		solrFldMapTest.assertSolrFldValue(testFilePath, id, fldName, fldVal);	    	
	    barcode = "222";  // not missing - yes shelfkey pieces
	    firstPart = barcode + sep + sal + sep + stacks + sep + sep + sep + LCB_CNUM + sep;
		fldVal = firstPart + LCB_SKEY + sep + LCB_RSKEY + sep + LCB_CNUM + sep + LCB_SKEY;
	    solrFldMapTest.assertSolrFldValue(testFilePath, id, fldName, fldVal);
		fldVal = firstPart + sep + sep + LCB_CNUM + sep + LCB_SKEY;
		solrFldMapTest.assertSolrFldHasNoValue(testFilePath, id, fldName, fldVal);

		id = "missingSame";
	    barcode = "36105042256730"; // missing - no shelfkey pieces
	    firstPart = barcode + sep + green + sep + stacks + sep + missing + sep + sep + LCT_CNUM + sep;
		fldVal = firstPart + LCT_SKEY + sep + LCT_RSKEY + sep + LCT_CNUM + sep + LCT_SKEY;
	    solrFldMapTest.assertSolrFldHasNoValue(testFilePath, id, fldName, fldVal);
		fldVal = firstPart + sep + sep + LCT_CNUM + sep + LCT_SKEY;
		solrFldMapTest.assertSolrFldValue(testFilePath, id, fldName, fldVal);   	
	    barcode = "36105000549084";  // not missing - yes shelfkey pieces
	    firstPart = barcode + sep + sal + sep + stacks + sep + sep + sep + LCT_CNUM_ELLIP + sep;
	    String fullCallnum = "TR692 .P37 V.3 1978";
	    String volSort = CallNumUtils.getVolumeSortCallnum(fullCallnum, LCT_CNUM_ELLIP, LCT_SKEY_ELLIP, CallNumberType.LC, true, id);
		fldVal = firstPart + LCT_SKEY_ELLIP + sep + LCT_RSKEY_ELLIP + sep + fullCallnum + sep + volSort;
	    solrFldMapTest.assertSolrFldValue(testFilePath, id, fldName, fldVal);
		fldVal = firstPart + sep + sep + fullCallnum + sep + volSort;
		solrFldMapTest.assertSolrFldHasNoValue(testFilePath, id, fldName, fldVal);
	    barcode = "36105000549068";  // not missing - yes shelfkey pieces
	    fullCallnum = "TR692 .P37 V.1 1973";
	    firstPart = barcode + sep + sal + sep + stacks + sep + sep + sep + LCT_CNUM_ELLIP + sep;
	    volSort = CallNumUtils.getVolumeSortCallnum(fullCallnum, LCT_CNUM_ELLIP, LCT_SKEY_ELLIP, CallNumberType.LC, true, id);
		fldVal = firstPart + LCT_SKEY_ELLIP + sep + LCT_RSKEY_ELLIP + sep + fullCallnum + sep + volSort;
	    solrFldMapTest.assertSolrFldValue(testFilePath, id, fldName, fldVal);
		fldVal = firstPart + sep + sep + fullCallnum + sep + volSort;
		solrFldMapTest.assertSolrFldHasNoValue(testFilePath, id, fldName, fldVal);

		String lost = "LOST-ASSUM";
		id = "onlyLost";
	    barcode = "36105002467384"; // lost - no shelfkey pieces
	    firstPart = barcode + sep + sal + sep + stacks + sep + lost + sep + sep + DEWEY_CNUM + sep;
		fldVal = firstPart + DEWEY_SKEY + sep + DEWEY_RSKEY + sep + DEWEY_CNUM + sep + DEWEY_SKEY;
	    solrFldMapTest.assertSolrFldHasNoValue(testFilePath, id, fldName, fldVal);
		fldVal = firstPart + sep + sep + DEWEY_CNUM + sep + DEWEY_SKEY;
		solrFldMapTest.assertSolrFldValue(testFilePath, id, fldName, fldVal);	    	

		id = "lostDiff";
	    barcode = "111"; // lost - no shelfkey pieces
	    firstPart = barcode + sep + sal + sep + stacks + sep + lost + sep + sep + LCA_CNUM + sep;
		fldVal = firstPart + LCA_SKEY + sep + LCA_RSKEY + sep + LCA_CNUM + sep + LCA_SKEY;
	    solrFldMapTest.assertSolrFldHasNoValue(testFilePath, id, fldName, fldVal);
		fldVal = firstPart + sep + sep + LCA_CNUM + sep + LCA_SKEY;
		solrFldMapTest.assertSolrFldValue(testFilePath, id, fldName, fldVal);	    	
	    barcode = "222";  // not lost - yes shelfkey pieces
	    firstPart = barcode + sep + sal + sep + stacks + sep + sep + sep + LCB_CNUM + sep;
		fldVal = firstPart + LCB_SKEY + sep + LCB_RSKEY + sep + LCB_CNUM + sep + LCB_SKEY;
	    solrFldMapTest.assertSolrFldValue(testFilePath, id, fldName, fldVal);
		fldVal = firstPart + sep + sep + LCB_CNUM + sep + LCB_SKEY;
		solrFldMapTest.assertSolrFldHasNoValue(testFilePath, id, fldName, fldVal);

		id = "lostSame";
	    barcode = "36105042256730"; // lost - no shelfkey pieces
	    firstPart = barcode + sep + green + sep + stacks + sep + lost + sep + sep + LCT_CNUM + sep;
		fldVal = firstPart + LCT_SKEY + sep + LCT_RSKEY + sep + LCT_CNUM + sep + LCT_SKEY;
	    solrFldMapTest.assertSolrFldHasNoValue(testFilePath, id, fldName, fldVal);
		fldVal = firstPart + sep + sep + LCT_CNUM + sep + LCT_SKEY;
		solrFldMapTest.assertSolrFldValue(testFilePath, id, fldName, fldVal);   	
	    barcode = "36105000549084";  // not lost - yes shelfkey pieces
	    firstPart = barcode + sep + sal + sep + stacks + sep + sep + sep + LCT_CNUM_ELLIP + sep;
	    fullCallnum = "TR692 .P37 V.3 1978";
	    volSort = CallNumUtils.getVolumeSortCallnum(fullCallnum, LCT_CNUM_ELLIP, LCT_SKEY_ELLIP, CallNumberType.LC, true, id);
		fldVal = firstPart + LCT_SKEY_ELLIP + sep + LCT_RSKEY_ELLIP + sep + fullCallnum + sep + volSort;
	    solrFldMapTest.assertSolrFldValue(testFilePath, id, fldName, fldVal);
		fldVal = firstPart + sep + sep + fullCallnum + sep + volSort;
		solrFldMapTest.assertSolrFldHasNoValue(testFilePath, id, fldName, fldVal);
	    barcode = "36105000549068";  // not lost - yes shelfkey pieces
	    fullCallnum = "TR692 .P37 V.1 1973";
	    firstPart = barcode + sep + sal + sep + stacks + sep + sep + sep + LCT_CNUM_ELLIP + sep;
	    volSort = CallNumUtils.getVolumeSortCallnum(fullCallnum, LCT_CNUM_ELLIP, LCT_SKEY_ELLIP, CallNumberType.LC, true, id);
		fldVal = firstPart + LCT_SKEY_ELLIP + sep + LCT_RSKEY_ELLIP + sep + fullCallnum + sep + volSort;
	    solrFldMapTest.assertSolrFldValue(testFilePath, id, fldName, fldVal);
		fldVal = firstPart + sep + sep + fullCallnum + sep + volSort;
		solrFldMapTest.assertSolrFldHasNoValue(testFilePath, id, fldName, fldVal);
	}

}