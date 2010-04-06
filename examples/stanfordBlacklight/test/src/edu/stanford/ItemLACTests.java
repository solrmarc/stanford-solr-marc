package edu.stanford;

import java.io.*;

import org.junit.*;
import org.solrmarc.tools.CallNumUtils;

import edu.stanford.enumValues.CallNumberType;

/**
 * junit4 tests for Stanford University 
 *   cope properly with items with current location LAC  SW-314
 * @author Naomi Dushay
 */
public class ItemLACTests extends AbstractStanfordBlacklightTest {

	private String fileName = "itemLACcurrentLoc.mrc";
    private String testFilePath = testDataParentPath + File.separator + fileName;

@Before
	public final void setup() 
	{
		mappingTestInit();
	}	

	/**
	 * SW-314
	 * LAC found in a current location (999k) implies that we should just pass 
	 * LAC to UI. (UI code will translate LAC to "Out for vendor cataloging" 
	 * etc.)
	 * LAC locations are associated with XX call numbers, so the XX 
	 * call number code need to accommodate this case. 
	 */
@Test
	public void currLocLACXXCallnum()
	{
		String id = "LACcurrXX";
		String fldName = "item_display";
		String sep = ItemUtils.SEP;
		String firstPart = "1" + sep + "GREEN" + sep + "STACKS" + sep;	
	    String lastPart = sep + sep + sep + sep + sep + sep;
	    String fldVal = firstPart + "ON-ORDER" + lastPart;
	    solrFldMapTest.assertSolrFldHasNoValue(testFilePath, id, fldName, fldVal);
	    fldVal = firstPart + "LAC" + lastPart;
	    solrFldMapTest.assertSolrFldValue(testFilePath, id, fldName, fldVal);
	}


	/**
	 * if LAC is in a home location, pass it on thru.
	 * SW-314
	 * LAC found in a home location (999k) implies that we should just pass 
	 * LAC to UI. (UI code will translate LAC to "Out for vendor cataloging" 
	 * etc.)
	 * LAC locations are (can be?) associated with XX call numbers, so the XX 
	 * call number code need to accommodate this case. 
	 */
@Test
	public void homeLocLACXXCallnum()
	{
		String id = "LAChomeXX";
		String fldName = "item_display";
		String sep = ItemUtils.SEP;
		String firstPart = "2" + sep + "GREEN" + sep;	
	    String lastPart = sep + sep + sep + sep + sep + sep + sep;
	    String fldVal = firstPart + "ON-ORDER" + lastPart;
	    solrFldMapTest.assertSolrFldHasNoValue(testFilePath, id, fldName, fldVal);
	    fldVal = firstPart + "LAC" + lastPart;
	    solrFldMapTest.assertSolrFldValue(testFilePath, id, fldName, fldVal);
	
	}
	
	/**
	 * ensure LAC current location with a regular call number passes thru.
	 */
@Test
	public void currLocLACLC()
	{
	    String id = "LACcurrLC";
		String fldName = "item_display";
		String sep = ItemUtils.SEP;
	    String callnum = "A1 .B2";
	    String skey = CallNumberType.LC.getPrefix() + org.solrmarc.tools.CallNumUtils.getLCShelfkey(callnum, null).toLowerCase();
	    String rskey = CallNumUtils.getReverseShelfKey(skey).toLowerCase();
		String firstPart = "3" + sep + "GREEN" + sep + "STACKS" + sep;	
	    String lastPart = sep + sep + callnum + sep + skey + sep + rskey + sep + callnum + sep + skey;
	    String fldVal = firstPart + "ON-ORDER" + lastPart;
	    solrFldMapTest.assertSolrFldHasNoValue(testFilePath, id, fldName, fldVal);
	    fldVal = firstPart + "LAC" + lastPart;
	    solrFldMapTest.assertSolrFldValue(testFilePath, id, fldName, fldVal);
	}
	
	/**
	 * test LAC current location with an XX call number in a real record.
	 */
@Test
	public void actualLACExample()
	{
	    String id = "6792210";
		String fldName = "item_display";
		String sep = ItemUtils.SEP;
		String firstPart = "36105123571122" + sep + "GREEN" + sep + "STACKS" + sep;	
	    String lastPart = sep + sep + sep + sep + sep + sep;
	    String fldVal = firstPart + "ON-ORDER" + lastPart;
	    solrFldMapTest.assertSolrFldHasNoValue(testFilePath, id, fldName, fldVal);
	    fldVal = firstPart + "LAC" + lastPart;
	    solrFldMapTest.assertSolrFldValue(testFilePath, id, fldName, fldVal);
	}

}