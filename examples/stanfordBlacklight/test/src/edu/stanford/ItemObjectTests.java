package edu.stanford;

import java.io.File;
import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import org.junit.Before;
import org.junit.Test;
import org.xml.sax.SAXException;

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
			throws ParserConfigurationException, IOException, SAXException 
	{
		mappingTestInit();
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
		String shelfkey = edu.stanford.CallNumUtils.getShelfKey(callnum, lcScheme, id).toLowerCase();
		String reversekey = org.solrmarc.tools.CallNumUtils.getReverseShelfKey(shelfkey).toLowerCase();
		String volSort = edu.stanford.CallNumUtils.getVolumeSortCallnum(callnum, callnum, shelfkey, lcScheme, !isSerial, id);
		String fldVal = "111 -|- GREEN -|- STACKS" + SEP + SEP + SEP + callnum + SEP +
				shelfkey + SEP + reversekey + SEP + callnum + SEP + volSort;
	    solrFldMapTest.assertSolrFldHasNoValue(testFilePath, id, fldName, fldVal);
	    //  it is Dewey
	    shelfkey = edu.stanford.CallNumUtils.getShelfKey(callnum, deweyScheme, id).toLowerCase();
		reversekey = org.solrmarc.tools.CallNumUtils.getReverseShelfKey(shelfkey).toLowerCase();
		volSort = edu.stanford.CallNumUtils.getVolumeSortCallnum(callnum, callnum, shelfkey, deweyScheme, !isSerial, id);
		fldVal = "111 -|- GREEN -|- STACKS" + SEP + SEP + SEP + callnum + SEP +
				shelfkey + SEP + reversekey + SEP + callnum + SEP + volSort;
	    solrFldMapTest.assertSolrFldValue(testFilePath, id, fldName, fldVal);

		// when not LC or Dewey, bad LC becomes other
		id = "badLC";
		callnum = "BAD";
		// it's not LC
		shelfkey = edu.stanford.CallNumUtils.getShelfKey(callnum, lcScheme, id).toLowerCase();
		reversekey = org.solrmarc.tools.CallNumUtils.getReverseShelfKey(shelfkey).toLowerCase();
		volSort = edu.stanford.CallNumUtils.getVolumeSortCallnum(callnum, callnum, shelfkey, lcScheme, !isSerial, id);
		fldVal = "222 -|- GREEN -|- STACKS" + SEP + SEP + SEP + callnum + SEP +
				shelfkey + SEP + reversekey + SEP + callnum + SEP + volSort;
	    solrFldMapTest.assertSolrFldHasNoValue(testFilePath, id, fldName, fldVal);
	    //  it is other
		shelfkey = edu.stanford.CallNumUtils.getShelfKey(callnum, otherScheme, id).toLowerCase();
		reversekey = org.solrmarc.tools.CallNumUtils.getReverseShelfKey(shelfkey).toLowerCase();
		volSort = edu.stanford.CallNumUtils.getVolumeSortCallnum(callnum, callnum, shelfkey, otherScheme, !isSerial, id);
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
		String shelfkey = edu.stanford.CallNumUtils.getShelfKey(callnum, deweyScheme, id).toLowerCase();
		String reversekey = org.solrmarc.tools.CallNumUtils.getReverseShelfKey(shelfkey).toLowerCase();
		String volSort = edu.stanford.CallNumUtils.getVolumeSortCallnum(callnum, callnum, shelfkey, deweyScheme, !isSerial, id);
		String fldVal = "333 -|- GREEN -|- STACKS" + SEP + SEP + SEP + callnum + SEP +
				shelfkey + SEP + reversekey + SEP + callnum + SEP + volSort;
	    solrFldMapTest.assertSolrFldHasNoValue(testFilePath, id, fldName, fldVal);
	    //  it is Dewey
	    shelfkey = edu.stanford.CallNumUtils.getShelfKey(callnum, otherScheme, id).toLowerCase();
		reversekey = org.solrmarc.tools.CallNumUtils.getReverseShelfKey(shelfkey).toLowerCase();
		volSort = edu.stanford.CallNumUtils.getVolumeSortCallnum(callnum, callnum, shelfkey, otherScheme, !isSerial, id);
		fldVal = "333 -|- GREEN -|- STACKS" + SEP + SEP + SEP + callnum + SEP +
				shelfkey + SEP + reversekey + SEP + callnum + SEP + volSort;
	    solrFldMapTest.assertSolrFldValue(testFilePath, id, fldName, fldVal);	
	}
	
	
	static String INET_LOC = "INTERNET";
	static String INET_CALLNUM = "INTERNET RESOURCE";

	/**
	 * when homeLoc is INTERNET, item is online
	 */
// FIXME:  uncomment when we start having item_display for online resources
//@Test
	public void testHomeLocInternet() 
	{
		// home location INTERNET
		String id = "homeLocInternet";
		String callnum = "IGNORED";
	    String shelfkey = "";
		String reversekey = "";
		String volSort = "";
		//  it's not left alone
		String fldVal = "444 -|- GREEN" + SEP + INET_LOC + SEP + SEP + SEP + callnum + SEP +
				shelfkey + SEP + reversekey + SEP + callnum + SEP + volSort;
	    solrFldMapTest.assertSolrFldHasNoValue(testFilePath, id, fldName, fldVal);
	    //  curr loc added, callnum changed
		fldVal = "444 -|- GREEN" + SEP + INET_LOC + SEP + INET_LOC + SEP + SEP + INET_CALLNUM + SEP +
				shelfkey + SEP + reversekey + SEP + INET_CALLNUM + SEP + volSort;
	    solrFldMapTest.assertSolrFldValue(testFilePath, id, fldName, fldVal);	
	}


	/**
	 * when currLoc is INTERNET, item is online
	 */
@Test
	public void testCurrLocInternet() 
	{
		String id = "currLocInternet";
		String callnum = "IGNORED";
		String shelfkey = "";
		String reversekey = "";
		String volSort = "";
		//  it's not left alone
		String fldVal = "555 -|- GREEN" + SEP + INET_LOC + SEP + INET_LOC + SEP + SEP + callnum + SEP +
				shelfkey + SEP + reversekey + SEP + callnum + SEP + volSort;
	    solrFldMapTest.assertSolrFldHasNoValue(testFilePath, id, fldName, fldVal);
	    //  callnum changed
		fldVal = "555 -|- GREEN" + SEP + INET_LOC + SEP + INET_LOC + SEP + "SUL" + SEP + INET_CALLNUM + SEP +
				shelfkey + SEP + reversekey + SEP + INET_CALLNUM + SEP + volSort;
		solrFldMapTest.assertSolrFldValue(testFilePath, id, fldName, fldVal);	
	}
	
	/**
	 * when callnum is 'INTERNET RESOURCE', item is online
	 */
//@Test
	public void testCallnumInternet() 
	{
		// call number INTERENT RESOURCE
		String id = "internetCallnum";
		String callnum = INET_CALLNUM;
		String shelfkey = "";
		String reversekey = "";
		String volSort = "";
		//  it's not left alone
		String fldVal = "666 -|- GREEN -|- STACKS" + SEP + SEP + SEP + callnum + SEP +
				shelfkey + SEP + reversekey + SEP + callnum + SEP + volSort;
	    solrFldMapTest.assertSolrFldHasNoValue(testFilePath, id, fldName, fldVal);
	    //  home loc changed, curr loc changed
		fldVal = "666 -|- GREEN" + SEP + INET_LOC + SEP + INET_LOC + SEP + SEP + callnum + SEP +
				shelfkey + SEP + reversekey + SEP + callnum + SEP + volSort;
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
		String shelfkey = edu.stanford.CallNumUtils.getShelfKey(callnum, otherScheme, id).toLowerCase();
		String reversekey = org.solrmarc.tools.CallNumUtils.getReverseShelfKey(shelfkey).toLowerCase();
		String volSort = edu.stanford.CallNumUtils.getVolumeSortCallnum(callnum, callnum, shelfkey, otherScheme, !isSerial, id);
		String fldVal = "777 -|- SUL -|- INPROCESS" + SEP + SEP + SEP + callnum + SEP +
				shelfkey + SEP + reversekey + SEP + callnum + SEP + volSort;
	    solrFldMapTest.assertSolrFldHasNoValue(testFilePath, id, fldName, fldVal);
	}

}
