package edu.stanford;

import java.io.*;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.solr.client.solrj.SolrServerException;
import org.junit.*;
import org.xml.sax.SAXException;

import edu.stanford.enumValues.CallNumberType;

/**
 * tests for records that have so many items that the sirsi dump splits them
 *  into multiple records to avoid a 5000 character limit for each record. Each
 *  of the records has the same bib portion except for the 999s
 * @author naomi
 *
 */
public class ItemsSplitTests extends AbstractStanfordTest 
{
	static String fldName = "item_display";
	static String SEP = " -|- ";
	String testFilePath = testDataParentPath + File.separator + "splitItemsTest.mrc";
	static boolean isSerial = true;
	
@Before
	public final void setup() 
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

	/**
	 * assert all items are present and not garbled (via sampling)
	 *    (turns out problem is in the marc directory ... the ruby marc gem
	 *    can read these records correctly if it uses the "forgiving" reader.
	 *    but the REAL solution is to switch to marcxml.
	 */
//@Test
	public void testAllItemsPresent()
			throws ParserConfigurationException, IOException, SAXException, SolrServerException
	{
		String barcodeFldName = "barcode_search";

		// 3195846 is a giant record for "Science"
		createFreshIx("3195846.mrc");
//		createIxInitVars("100817_uni_increment.marc");
		assertDocPresent("3195846");
				
		// first barcode
		assertSingleResult("3195846", barcodeFldName, "36105016962404");
		// last "good" 999 in UI
		assertSingleResult("3195846", barcodeFldName, "36105019891873");
		// first "bad" 999 in UI
		assertSingleResult("3195846", barcodeFldName, "36105018074463");
		// last barcode
		assertSingleResult("3195846", barcodeFldName, "36105123124971");


		// 4332640 is a giant record for Buckminster Fuller 
		createFreshIx("4332640.mrc");
//		createIxInitVars("100813_uni_increment.marc");
		assertDocPresent("4332640");

		// first barcode
		assertSingleResult("4332640", barcodeFldName, "36105116017505");
		// last "good" 999 in UI
		assertSingleResult("4332640", barcodeFldName, "36105116028791");
		// first "bad" 999 in UI
		assertSingleResult("4332640", barcodeFldName, "36105116028809");
		// last barcode
		assertSingleResult("4332640", barcodeFldName, "36105115865367");
	

		// 7621542 is a moderately large record for Southern Pacific Railroad Group 2
		createFreshIx("7621542.mrc");
		assertDocPresent("7621542");

		// first barcode
		assertSingleResult("7621542", barcodeFldName, "36105115582079");
		// last "good" 999 in UI
		assertSingleResult("7621542", barcodeFldName, "36105116127981");
		// first "bad" 999 in UI
		assertSingleResult("7621542", barcodeFldName, "36105116127999");
		// last barcode
		assertSingleResult("7621542", barcodeFldName, "36105115641370"); 
	}



}
