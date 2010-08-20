package edu.stanford;

import java.io.File;
import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import org.junit.Before;
import org.junit.Test;
import org.xml.sax.SAXException;

import edu.stanford.enumValues.CallNumberType;

/**
 * unit tests for edu.stanford.Item methods
 * @author Naomi Dushay
 */
public class ItemObjectTests extends AbstractStanfordBlacklightTest {
	
	static String fldName = "item_display";
	static String SEP = " -|- ";
	String testFilePath = testDataParentPath + File.separator + "itemObjectTests.mrc";
	static boolean isSerial = true;
	
@Before
	public final void setup() 
	{
		mappingTestInit();
	}	
	
	/**
	 * if call number is actually lopped, it should have ellipsis on end
	 */
@Test
	public void testLoppedCallnumEllipsis()
	{
		// labelled LC but it's Dewey 
		String id = "lopped";
		String callnum = "F12 .B6 V.27";
		String lopped = "F12 .B6 ...";
		//  it's not LC
		String shelfkey = edu.stanford.CallNumUtils.getShelfKey(lopped, CallNumberType.LC, id).toLowerCase();
		String reversekey = org.solrmarc.tools.CallNumUtils.getReverseShelfKey(shelfkey).toLowerCase();
		String volSort = edu.stanford.CallNumUtils.getVolumeSortCallnum(callnum, lopped, shelfkey, CallNumberType.LC, !isSerial, id);
		String fldVal = "001 -|- GREEN -|- STACKS" + SEP + SEP + SEP + lopped + SEP +
				shelfkey + SEP + reversekey + SEP + callnum + SEP + volSort;
	    solrFldMapTest.assertSolrFldValue(testFilePath, id, fldName, fldVal);	
	}

	/**
	 * test that scheme for call number that is supposed to be LC, but isn't
	 *  valid LC, is changed to DEWEY or other
	 */
@Test
	public void testInvalidLCCallnum() 
	{
		// labelled LC but it's Dewey 
		String id = "deweyAsLC";
		String callnum = "111.11 .A5";
		//  it's not LC
		String shelfkey = edu.stanford.CallNumUtils.getShelfKey(callnum, CallNumberType.LC, id).toLowerCase();
		String reversekey = org.solrmarc.tools.CallNumUtils.getReverseShelfKey(shelfkey).toLowerCase();
		String volSort = edu.stanford.CallNumUtils.getVolumeSortCallnum(callnum, callnum, shelfkey, CallNumberType.LC, !isSerial, id);
		String fldVal = "111 -|- GREEN -|- STACKS" + SEP + SEP + SEP + callnum + SEP +
				shelfkey + SEP + reversekey + SEP + callnum + SEP + volSort;
	    solrFldMapTest.assertSolrFldHasNoValue(testFilePath, id, fldName, fldVal);
	    //  it is Dewey
		shelfkey = edu.stanford.CallNumUtils.getShelfKey(callnum, CallNumberType.DEWEY, id).toLowerCase();
		reversekey = org.solrmarc.tools.CallNumUtils.getReverseShelfKey(shelfkey).toLowerCase();
		volSort = edu.stanford.CallNumUtils.getVolumeSortCallnum(callnum, callnum, shelfkey, CallNumberType.DEWEY, !isSerial, id);
		fldVal = "111 -|- GREEN -|- STACKS" + SEP + SEP + SEP + callnum + SEP +
				shelfkey + SEP + reversekey + SEP + callnum + SEP + volSort;
	    solrFldMapTest.assertSolrFldValue(testFilePath, id, fldName, fldVal);

		// when not LC or Dewey, bad LC becomes other
		id = "badLC";
		callnum = "BAD";
		// it's not LC
		shelfkey = edu.stanford.CallNumUtils.getShelfKey(callnum, CallNumberType.LC, id).toLowerCase();
		reversekey = org.solrmarc.tools.CallNumUtils.getReverseShelfKey(shelfkey).toLowerCase();
		volSort = edu.stanford.CallNumUtils.getVolumeSortCallnum(callnum, callnum, shelfkey, CallNumberType.LC, !isSerial, id);
		fldVal = "222 -|- GREEN -|- STACKS" + SEP + SEP + SEP + callnum + SEP +
				shelfkey + SEP + reversekey + SEP + callnum + SEP + volSort;
	    solrFldMapTest.assertSolrFldHasNoValue(testFilePath, id, fldName, fldVal);
	    //  it is other
		shelfkey = edu.stanford.CallNumUtils.getShelfKey(callnum, CallNumberType.OTHER, id).toLowerCase();
		reversekey = org.solrmarc.tools.CallNumUtils.getReverseShelfKey(shelfkey).toLowerCase();
		volSort = edu.stanford.CallNumUtils.getVolumeSortCallnum(callnum, callnum, shelfkey, CallNumberType.OTHER, !isSerial, id);
		fldVal = "222 -|- GREEN -|- STACKS" + SEP + SEP + SEP + callnum + SEP +
				shelfkey + SEP + reversekey + SEP + callnum + SEP + volSort;
	    solrFldMapTest.assertSolrFldValue(testFilePath, id, fldName, fldVal);
	}
	
	/**
	 * test that scheme for call number that is supposed to be Dewey, but isn't
	 *  valid, is changed to other
	 */
@Test
	public void testInvalidDeweyCallnum() 
	{
		String id = "badDewey";
		String callnum = "1234.5 .D6";
		//  it's not Dewey
		String shelfkey = edu.stanford.CallNumUtils.getShelfKey(callnum, CallNumberType.DEWEY, id).toLowerCase();
		String reversekey = org.solrmarc.tools.CallNumUtils.getReverseShelfKey(shelfkey).toLowerCase();
		String volSort = edu.stanford.CallNumUtils.getVolumeSortCallnum(callnum, callnum, shelfkey, CallNumberType.DEWEY, !isSerial, id);
		String fldVal = "333 -|- GREEN -|- STACKS" + SEP + SEP + SEP + callnum + SEP +
				shelfkey + SEP + reversekey + SEP + callnum + SEP + volSort;
	    solrFldMapTest.assertSolrFldHasNoValue(testFilePath, id, fldName, fldVal);
		// so it's other
		shelfkey = edu.stanford.CallNumUtils.getShelfKey(callnum, CallNumberType.OTHER, id).toLowerCase();
		reversekey = org.solrmarc.tools.CallNumUtils.getReverseShelfKey(shelfkey).toLowerCase();
		volSort = edu.stanford.CallNumUtils.getVolumeSortCallnum(callnum, callnum, shelfkey, CallNumberType.OTHER, !isSerial, id);
		fldVal = "333 -|- GREEN -|- STACKS" + SEP + SEP + SEP + callnum + SEP +
				shelfkey + SEP + reversekey + SEP + callnum + SEP + volSort;
	    solrFldMapTest.assertSolrFldValue(testFilePath, id, fldName, fldVal);	
	}

	/**
	 * when homeLoc is INTERNET, item is online
	 */
@Test
	public void testHomeLocInternet() 
	{
		// home location INTERNET
		String id = "homeLocInternet";
	    String shelfkey = "";
		String reversekey = "";
		String volSort = "";
		//  it's not left alone
		String fldVal = "444 -|- GREEN" + SEP + Item.ELOC + SEP + SEP + "SUL" + SEP + Item.ECALLNUM + SEP +
				shelfkey + SEP + reversekey + SEP + Item.ECALLNUM + SEP + volSort;
	    solrFldMapTest.assertSolrFldHasNoValue(testFilePath, id, fldName, fldVal);
	    //  no lopped or full call number included
		fldVal = "444 -|- GREEN" + SEP + Item.ELOC + SEP + SEP + "SUL" + SEP + SEP +
				shelfkey + SEP + reversekey + SEP + SEP;
	    solrFldMapTest.assertSolrFldValue(testFilePath, id, fldName, fldVal);	
	}


	/**
	 * when currLoc is INTERNET, item is online
	 */
@Test
	public void testCurrLocInternet() 
	{
		String id = "currLocInternet";
		String shelfkey = "";
		String reversekey = "";
		String volSort = "";
		//  it's not left alone
		String fldVal = "555 -|- GREEN" + SEP + Item.ELOC + SEP + Item.ELOC + SEP + "SUL" + SEP + Item.ECALLNUM + SEP +
				shelfkey + SEP + reversekey + SEP + Item.ECALLNUM + SEP + volSort;
	    solrFldMapTest.assertSolrFldHasNoValue(testFilePath, id, fldName, fldVal);
	    //  lopped and full callnum dropped
		fldVal = "555 -|- GREEN" + SEP + "STACKS" + SEP + Item.ELOC + SEP + "SUL" + SEP + SEP +
				shelfkey + SEP + reversekey + SEP + SEP;
		solrFldMapTest.assertSolrFldValue(testFilePath, id, fldName, fldVal);	
	}
	
	/**
	 * when callnum is 'INTERNET RESOURCE', item is online
	 */
@Test
	public void testCallnumInternet() 
	{
		// call number INTERNET RESOURCE, but GREEN STACKS, not SUL
		String id = "internetCallnum";
		String shelfkey = "";
		String reversekey = "";
		String volSort = "";
		//  it's not left alone
		String fldVal = "666 -|- GREEN -|- STACKS" + SEP + SEP + "SUL"  + SEP + Item.ECALLNUM + SEP +
				shelfkey + SEP + reversekey + SEP + Item.ECALLNUM + SEP + volSort;
	    solrFldMapTest.assertSolrFldHasNoValue(testFilePath, id, fldName, fldVal);
	    //  lopped and full callnum dropped 
	    fldVal = "666 -|- GREEN" + SEP + "STACKS" + SEP + SEP + "SUL" + SEP + SEP +
				shelfkey + SEP + reversekey + SEP + SEP;
	    solrFldMapTest.assertSolrFldValue(testFilePath, id, fldName, fldVal);	
	}
	

	/**
	 * test that when item type is EDI-REMOVE, item is skipped
	 */
@Test
	public void testSkipEdiRemoveItems()
	{
		String id = "ediremove";
		String callnum = "427331959";
		String shelfkey = edu.stanford.CallNumUtils.getShelfKey(callnum, CallNumberType.OTHER, id).toLowerCase();
		String reversekey = org.solrmarc.tools.CallNumUtils.getReverseShelfKey(shelfkey).toLowerCase();
		String volSort = edu.stanford.CallNumUtils.getVolumeSortCallnum(callnum, callnum, shelfkey, CallNumberType.OTHER, !isSerial, id);
		String fldVal = "777 -|- SUL -|- INPROCESS" + SEP + SEP + SEP + callnum + SEP +
				shelfkey + SEP + reversekey + SEP + callnum + SEP + volSort;
	    solrFldMapTest.assertSolrFldHasNoValue(testFilePath, id, fldName, fldVal);
	}

}
