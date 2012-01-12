package edu.stanford;

import java.io.*;

import org.junit.*;

import edu.stanford.enumValues.CallNumberType;

/**
 * junit4 tests for Stanford University 
 *   cope properly with items with call number "NO CALL NUMBER" - display, discoverable  SW-231
 * @author Naomi Dushay
 */
public class ItemNoCallNumberTests extends AbstractStanfordTest 
{
	private String fileName = "itemNoCallNumber.mrc";
    private String testFilePath = testDataParentPath + File.separator + fileName;

@Before
	public final void setup() 
	{
		mappingTestInit();
	}	

	//  If there is no call number in any 999 for the record 
  	//   look for one in the 050, 090, 086 (see below)

	// search results - do not display:   item_display    lopped callnum
	// record view - do not display:   item_display   full callnum, sort callnum 
	// availability - display (don't call jenson)
	// facets  - assign if there is a call number in the bib record
	// shelflist  - if call number, include
	//   (preferred_barcode, shelfkey, reversekey, item_display  shelfkey, reversekey)
    //   NOTE:  display call number as starting point, but display the link text in the list
    //
	
	/**
	 * "NO CALL NUMBER" is a skipped call number;  there are resources that
	 *   only have items with this call number, especially from Lane and
	 *   Jackson (and especially online items from Lane and Jackson)
	 */
@Test
	public void testNoCallNumber()
	{
		String id = "NCN";
		String id050 = "NCN050";
		String fldName = "callnum_top_facet";
		solrFldMapTest.assertNoSolrFld(testFilePath, id, fldName);
		solrFldMapTest.assertSolrFldValue(testFilePath, id050, fldName, "A - General Works");

		fldName = "preferred_barcode";
		solrFldMapTest.assertNoSolrFld(testFilePath, id, fldName);
		solrFldMapTest.assertSolrFldValue(testFilePath, id050, fldName, "LL271310");
		
		fldName = "shelfkey";
		solrFldMapTest.assertNoSolrFld(testFilePath, id, fldName);
		String callnum = "A1 .B2";
	    String skey = CallNumberType.LC.getPrefix() + org.solrmarc.tools.CallNumUtils.getLCShelfkey(callnum, null).toLowerCase();
		solrFldMapTest.assertSolrFldValue(testFilePath, id050, fldName, skey);

		fldName = "item_display";
		String sep = ItemUtils.SEP;
		String uncallnum = "NO CALL NUMBER";
// TODO: should  these items have "NO CALL NUMBER" callnum in lopped, full and volsort?  SW-231
//  or should these sections be empty?
		String firstPart = "LL271310" + sep + "LANE-MED" + sep + "ASK@LANE" + sep + sep + sep;	
	    String fldVal = firstPart + uncallnum + sep + sep + sep + uncallnum + sep;
	    solrFldMapTest.assertSolrFldHasNoValue(testFilePath, id, fldName, fldVal);
	   	fldVal = firstPart + sep + sep + sep + sep;
	    solrFldMapTest.assertSolrFldValue(testFilePath, id, fldName, fldVal);
	    
		String rkey = org.solrmarc.tools.CallNumUtils.getReverseShelfKey(skey).toLowerCase();
	    fldVal = firstPart + uncallnum + sep + skey + sep + rkey + uncallnum + sep;
		solrFldMapTest.assertSolrFldHasNoValue(testFilePath, id050, fldName, fldVal);
	    fldVal = firstPart + sep + skey + sep + rkey + sep + sep;
		solrFldMapTest.assertSolrFldValue(testFilePath, id050, fldName, fldVal);
	}
}