package edu.stanford;

import java.io.File;
import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import org.junit.Before;
import org.junit.Test;
import org.xml.sax.SAXException;

import edu.stanford.enumValues.CallNumberType;

/**
 * tests for records that have so many items that the sirsi dump splits them
 *  into multiple records to avoid a 5000 character limit for each record. Each
 *  of the records has the same bib portion except for the 999s
 * @author naomi
 *
 */
public class ItemsSplitTests extends AbstractStanfordBlacklightTest {

	static String fldName = "item_display";
	static String SEP = " -|- ";
	String testFilePath = testDataParentPath + File.separator + "splitItemsTest.mrc";
	static boolean isSerial = true;
	
@Before
	public final void setup() 
			throws ParserConfigurationException, IOException, SAXException 
	{
		mappingTestInit();
	}	
	
	/**
	 * test when the first record in the file is split into multiple records.
	 */
@Test
	public void testFirstRecordSplit()
	{
		solrFldMapTest.assertSolrFldValue(testFilePath, "notSplit1", "id", "notSplit1");
		solrFldMapTest.assertSolrFldValue(testFilePath, "notSplit2", "id", "notSplit2");
	
		String id = "split1";
		solrFldMapTest.assertSolrFldHasNumValues(testFilePath, id, fldName, 5);

		String lopped = "A1 .B2 ...";
		String shelfkey = edu.stanford.CallNumUtils.getShelfKey(lopped, CallNumberType.LC, id).toLowerCase();
		String reversekey = org.solrmarc.tools.CallNumUtils.getReverseShelfKey(shelfkey).toLowerCase();

		String barcode = "101";
		String callnum = "A1 .B2 V.1";		
		String volSort = edu.stanford.CallNumUtils.getVolumeSortCallnum(callnum, lopped, shelfkey, CallNumberType.LC, isSerial, id);
		String fldVal = barcode + SEP + "GREEN -|- STACKS" + SEP + SEP + SEP + lopped + SEP +
				shelfkey + SEP + reversekey + SEP + callnum + SEP + volSort;		
	    solrFldMapTest.assertSolrFldValue(testFilePath, id, fldName, fldVal.trim());

		barcode = "102";
		callnum = "A1 .B2 V.2";		
		volSort = edu.stanford.CallNumUtils.getVolumeSortCallnum(callnum, lopped, shelfkey, CallNumberType.LC, isSerial, id);
		fldVal = barcode + SEP + "GREEN -|- STACKS" + SEP + SEP + SEP + lopped + SEP +
				shelfkey + SEP + reversekey + SEP + callnum + SEP + volSort;		
	    solrFldMapTest.assertSolrFldValue(testFilePath, id, fldName, fldVal);
	    
		barcode = "103";
		callnum = "A1 .B2 V.3";		
		volSort = edu.stanford.CallNumUtils.getVolumeSortCallnum(callnum, lopped, shelfkey, CallNumberType.LC, isSerial, id);
		fldVal = barcode + SEP + "GREEN -|- STACKS" + SEP + SEP + SEP + lopped + SEP +
				shelfkey + SEP + reversekey + SEP + callnum + SEP + volSort;		
	    solrFldMapTest.assertSolrFldValue(testFilePath, id, fldName, fldVal);

		barcode = "104";
		callnum = "A1 .B2 V.4";		
		volSort = edu.stanford.CallNumUtils.getVolumeSortCallnum(callnum, lopped, shelfkey, CallNumberType.LC, isSerial, id);
		fldVal = barcode + SEP + "GREEN -|- STACKS" + SEP + SEP + SEP + lopped + SEP +
				shelfkey + SEP + reversekey + SEP + callnum + SEP + volSort;		
	    solrFldMapTest.assertSolrFldValue(testFilePath, id, fldName, fldVal);

		barcode = "105";
		callnum = "A1 .B2 V.5";		
		volSort = edu.stanford.CallNumUtils.getVolumeSortCallnum(callnum, lopped, shelfkey, CallNumberType.LC, isSerial, id);
		fldVal = barcode + SEP + "GREEN -|- STACKS" + SEP + SEP + SEP + lopped + SEP +
				shelfkey + SEP + reversekey + SEP + callnum + SEP + volSort;		
	    solrFldMapTest.assertSolrFldValue(testFilePath, id, fldName, fldVal);	    
	}

	/**
	 * test when a middle record in the file is split into multiple records.
	 */
@Test
	public void testMiddleRecordSplit()
	{
		String id = "split2";
		solrFldMapTest.assertSolrFldHasNumValues(testFilePath, id, fldName, 5);
		
		String lopped = "A3 .B4 ...";
		String shelfkey = edu.stanford.CallNumUtils.getShelfKey(lopped, CallNumberType.LC, id).toLowerCase();
		String reversekey = org.solrmarc.tools.CallNumUtils.getReverseShelfKey(shelfkey).toLowerCase();
	
		String barcode = "201";
		String callnum = "A3 .B4 V.1";		
		String volSort = edu.stanford.CallNumUtils.getVolumeSortCallnum(callnum, lopped, shelfkey, CallNumberType.LC, isSerial, id);
		String fldVal = barcode + SEP + "GREEN -|- STACKS" + SEP + SEP + SEP + lopped + SEP +
				shelfkey + SEP + reversekey + SEP + callnum + SEP + volSort;		
		solrFldMapTest.assertSolrFldValue(testFilePath, id, fldName, fldVal);
		
		barcode = "202";
		callnum = "A3 .B4 V.2";		
		volSort = edu.stanford.CallNumUtils.getVolumeSortCallnum(callnum, lopped, shelfkey, CallNumberType.LC, isSerial, id);
		fldVal = barcode + SEP + "GREEN -|- STACKS" + SEP + SEP + SEP + lopped + SEP +
				shelfkey + SEP + reversekey + SEP + callnum + SEP + volSort;		
		solrFldMapTest.assertSolrFldValue(testFilePath, id, fldName, fldVal);
		
		barcode = "203";
		callnum = "A3 .B4 V.3";		
		volSort = edu.stanford.CallNumUtils.getVolumeSortCallnum(callnum, lopped, shelfkey, CallNumberType.LC, isSerial, id);
		fldVal = barcode + SEP + "GREEN -|- STACKS" + SEP + SEP + SEP + lopped + SEP +
				shelfkey + SEP + reversekey + SEP + callnum + SEP + volSort;		
		solrFldMapTest.assertSolrFldValue(testFilePath, id, fldName, fldVal);
		
		barcode = "204";
		callnum = "A3 .B4 V.4";		
		volSort = edu.stanford.CallNumUtils.getVolumeSortCallnum(callnum, lopped, shelfkey, CallNumberType.LC, isSerial, id);
		fldVal = barcode + SEP + "GREEN -|- STACKS" + SEP + SEP + SEP + lopped + SEP +
				shelfkey + SEP + reversekey + SEP + callnum + SEP + volSort;		
		solrFldMapTest.assertSolrFldValue(testFilePath, id, fldName, fldVal);
		
		barcode = "205";
		callnum = "A3 .B4 V.5";		
		volSort = edu.stanford.CallNumUtils.getVolumeSortCallnum(callnum, lopped, shelfkey, CallNumberType.LC, isSerial, id);
		fldVal = barcode + SEP + "GREEN -|- STACKS" + SEP + SEP + SEP + lopped + SEP +
				shelfkey + SEP + reversekey + SEP + callnum + SEP + volSort;		
		solrFldMapTest.assertSolrFldValue(testFilePath, id, fldName, fldVal);
	}

	/**
	 * test when the last record in the file is split into multiple records.
	 */
@Test
	public void testLastRecordSplit()
	{
		String id = "split3";
		solrFldMapTest.assertSolrFldHasNumValues(testFilePath, id, fldName, 5);
		
		String lopped = "A5 .B6 ...";
		String shelfkey = edu.stanford.CallNumUtils.getShelfKey(lopped, CallNumberType.LC, id).toLowerCase();
		String reversekey = org.solrmarc.tools.CallNumUtils.getReverseShelfKey(shelfkey).toLowerCase();

		String barcode = "301";
		String callnum = "A5 .B6 V.1";
		String volSort = edu.stanford.CallNumUtils.getVolumeSortCallnum(callnum, lopped, shelfkey, CallNumberType.LC, !isSerial, id);
		String fldVal = barcode + SEP + "GREEN -|- STACKS" + SEP + SEP + SEP + lopped + SEP +
				shelfkey + SEP + reversekey + SEP + callnum + SEP + volSort;
		solrFldMapTest.assertSolrFldValue(testFilePath, id, fldName, fldVal);

		barcode = "302";
		callnum = "A5 .B6 V.2";
		volSort = edu.stanford.CallNumUtils.getVolumeSortCallnum(callnum, lopped, shelfkey, CallNumberType.LC, !isSerial, id);
		fldVal = barcode + SEP + "GREEN -|- STACKS" + SEP + SEP + SEP + lopped + SEP +
				shelfkey + SEP + reversekey + SEP + callnum + SEP + volSort;
		solrFldMapTest.assertSolrFldValue(testFilePath, id, fldName, fldVal);

		barcode = "303";
		callnum = "A5 .B6 V.3";
		volSort = edu.stanford.CallNumUtils.getVolumeSortCallnum(callnum, lopped, shelfkey, CallNumberType.LC, !isSerial, id);
		fldVal = barcode + SEP + "GREEN -|- STACKS" + SEP + SEP + SEP + lopped + SEP +
				shelfkey + SEP + reversekey + SEP + callnum + SEP + volSort;
		solrFldMapTest.assertSolrFldValue(testFilePath, id, fldName, fldVal);

		barcode = "304";
		callnum = "A5 .B6 V.4";
		volSort = edu.stanford.CallNumUtils.getVolumeSortCallnum(callnum, lopped, shelfkey, CallNumberType.LC, !isSerial, id);
		fldVal = barcode + SEP + "GREEN -|- STACKS" + SEP + SEP + SEP + lopped + SEP +
				shelfkey + SEP + reversekey + SEP + callnum + SEP + volSort;
		solrFldMapTest.assertSolrFldValue(testFilePath, id, fldName, fldVal);

		barcode = "305";
		callnum = "A5 .B6 V.5";
		volSort = edu.stanford.CallNumUtils.getVolumeSortCallnum(callnum, lopped, shelfkey, CallNumberType.LC, !isSerial, id);
		fldVal = barcode + SEP + "GREEN -|- STACKS" + SEP + SEP + SEP + lopped + SEP +
				shelfkey + SEP + reversekey + SEP + callnum + SEP + volSort;
		solrFldMapTest.assertSolrFldValue(testFilePath, id, fldName, fldVal);
	}

	/**
	 * assert the non-split records are present.
	 */
@Test
	public void testNonSplitRecords()
	{
		solrFldMapTest.assertSolrFldValue(testFilePath, "notSplit1", "id", "notSplit1");
		solrFldMapTest.assertSolrFldValue(testFilePath, "notSplit2", "id", "notSplit2");
	}

}
