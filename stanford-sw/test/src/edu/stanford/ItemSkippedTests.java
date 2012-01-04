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
public class ItemSkippedTests extends AbstractStanfordTest {
	
	static String fldName = "item_display";
	static String SEP = " -|- ";
	static String testDataFname = "itemSkippedTests.mrc";
	String testFilePath = testDataParentPath + File.separator + testDataFname;
	static boolean isSerial = true;
	
@Before
	public final void setup() 
	{
		mappingTestInit();
	}	
	
	/**
	 * test that a record with only skipped items does not get indexed.
	 */
@Test
	public void testAllSkipped() 
		throws ParserConfigurationException, SAXException, IOException 
	{
		String fldName = "id";
		createFreshIx(testDataFname);
		String id = "skipHomeLoc";
		assertZeroResults(fldName, id);
		id = "skipCurrLoc";
		assertZeroResults(fldName, id);
		id = "multSkip";
		assertZeroResults(fldName, id);
		id = "EdiRemove";
		assertZeroResults(fldName, id);
	}

	/**
	 * test that when there is a mix of skipped and non-skipped items, only
	 *  the latter are in the record.
	 */
@Test
	public void testSkipSome()
	{
		String id = "keepOne";
		String callnum = "KEEP";
		String shelfkey = edu.stanford.CallNumUtils.getShelfKey(callnum, CallNumberType.OTHER, id).toLowerCase();
		String reversekey = org.solrmarc.tools.CallNumUtils.getReverseShelfKey(shelfkey).toLowerCase();
		String volSort = edu.stanford.CallNumUtils.getVolumeSortCallnum(callnum, callnum, shelfkey, CallNumberType.OTHER, !isSerial, id);
		String fldVal = "KEEP -|- GREEN -|- STACKS" + SEP + SEP + "STKS" + SEP + callnum + SEP +
				shelfkey + SEP + reversekey + SEP + callnum + SEP + volSort;
	    solrFldMapTest.assertSolrFldValue(testFilePath, id, fldName, fldVal);
	    
	    id = "OnePlusEdiRemove";
	    solrFldMapTest.assertSolrFldValue(testFilePath, id, fldName, fldVal);
	    
	    callnum = "SKIP";
		shelfkey = edu.stanford.CallNumUtils.getShelfKey(callnum, CallNumberType.OTHER, id).toLowerCase();
		reversekey = org.solrmarc.tools.CallNumUtils.getReverseShelfKey(shelfkey).toLowerCase();
		volSort = edu.stanford.CallNumUtils.getVolumeSortCallnum(callnum, callnum, shelfkey, CallNumberType.OTHER, !isSerial, id);
		fldVal = "SKIP -|- GREEN -|- STACKS" + SEP + SEP + SEP + callnum + SEP +
				shelfkey + SEP + reversekey + SEP + callnum + SEP + volSort;
	    solrFldMapTest.assertSolrFldHasNoValue(testFilePath, id, fldName, fldVal);

	    id = "keepOne";
	    solrFldMapTest.assertSolrFldHasNoValue(testFilePath, id, fldName, fldVal);
	}


	/**
	 * when there are no items in the record, there should be an item_display
	 *  field with status ON-ORDER
	 */
@Test
	public void testNoItems()
	{
		String id = "NoItems";
		String barcode = "";
		String library = "";
		String callnum = "";
		String shelfkey = "";
		String reversekey = "";
		String volSort = "";
		String fldVal = barcode + SEP + library + SEP + "ON-ORDER" + SEP + "ON-ORDER" + SEP + SEP + callnum + SEP +
				shelfkey + SEP + reversekey + SEP + callnum + SEP + volSort;
	    solrFldMapTest.assertSolrFldValue(testFilePath, id, fldName, fldVal);
	}

}
