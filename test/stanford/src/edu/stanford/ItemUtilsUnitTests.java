package edu.stanford;

import static org.junit.Assert.*;

import java.io.File;

import edu.stanford.AbstractStanfordTest;
import edu.stanford.enumValues.CallNumberType;

import org.junit.*;

/**
 * unit tests for edu.stanford.ItemUtils methods
 * @author Naomi Dushay
 */
public class ItemUtilsUnitTests extends AbstractStanfordTest {

	private static final boolean isSerial = true;

	/**
	 * test lopping of LC call numbers.  Serial and non-Serial flavor
	 */
@Test
	public void testLCLopping() {
		// no lopping
		String callnum = "HE270 .I854";
		assertEquals(callnum, CallNumUtils.getLoppedCallnum(callnum, CallNumberType.LC, isSerial));
		assertEquals(callnum, CallNumUtils.getLoppedCallnum(callnum, CallNumberType.LC, !isSerial));

		// Vol piece only
		callnum = "HE 2708.I854 V.666";
		assertEquals("HE 2708.I854", CallNumUtils.getLoppedCallnum(callnum, CallNumberType.LC, isSerial));
		assertEquals("HE 2708.I854", CallNumUtils.getLoppedCallnum(callnum, CallNumberType.LC, !isSerial));
		callnum = "BM198.2 .H85 OCT 2006";
		assertEquals("BM198.2 .H85", CallNumUtils.getLoppedCallnum(callnum, CallNumberType.LC, isSerial));
		assertEquals("BM198.2 .H85", CallNumUtils.getLoppedCallnum(callnum, CallNumberType.LC, !isSerial));
		
		// year suffix - should be lopped for serial only
		callnum = "M270 .I854 1999";
		assertEquals("M270 .I854", CallNumUtils.getLoppedCallnum(callnum, CallNumberType.LC, isSerial));
		assertEquals(callnum, CallNumUtils.getLoppedCallnum(callnum, CallNumberType.LC, !isSerial));

		// vol then year
		callnum = "TX519 .D26 V.2 1966";  
		assertEquals("TX519 .D26", CallNumUtils.getLoppedCallnum(callnum, CallNumberType.LC, isSerial));
		assertEquals("TX519 .D26", CallNumUtils.getLoppedCallnum(callnum, CallNumberType.LC, !isSerial));
		
		// year then vol - year suffix should be lopped for serial only
		callnum = "TX519 .D26 1954 V.2";  
		assertEquals("TX519 .D26", CallNumUtils.getLoppedCallnum(callnum, CallNumberType.LC, isSerial));
		assertEquals("TX519 .D26 1954", CallNumUtils.getLoppedCallnum(callnum, CallNumberType.LC, !isSerial));		
	}
	

	/**
	 * test lopping of Dewey call numbers.  Serial and non-Serial flavor
	 */
@Test
	public void testDeweyLopping() {	
		// no lopping
		String callnum = "553.2805 .P117";
		assertEquals(callnum, CallNumUtils.getLoppedCallnum(callnum, CallNumberType.DEWEY, isSerial));
		assertEquals(callnum, CallNumUtils.getLoppedCallnum(callnum, CallNumberType.DEWEY, !isSerial));

		// Vol piece only
		callnum = "553.2805 .P117 NOV/DEC 2009";  // 7888686
		assertEquals("553.2805 .P117", CallNumUtils.getLoppedCallnum(callnum, CallNumberType.DEWEY, isSerial));
		assertEquals("553.2805 .P117", CallNumUtils.getLoppedCallnum(callnum, CallNumberType.DEWEY, !isSerial));
		callnum = "621.38406 .B865 F V.5:NO.3-6 2007/2008";  // 6913279
		assertEquals("621.38406 .B865 F", CallNumUtils.getLoppedCallnum(callnum, CallNumberType.DEWEY, isSerial));
		assertEquals("621.38406 .B865 F", CallNumUtils.getLoppedCallnum(callnum, CallNumberType.DEWEY, !isSerial));
				
		// year only
		callnum = "331.06931 .N566 2007";  // 7752489
		assertEquals("331.06931 .N566", CallNumUtils.getLoppedCallnum(callnum, CallNumberType.DEWEY, isSerial));
		assertEquals(callnum, CallNumUtils.getLoppedCallnum(callnum, CallNumberType.DEWEY, !isSerial));
		
		// vol then year
		callnum = "505 .N285B V.241-245 1973";
		assertEquals("505 .N285B", CallNumUtils.getLoppedCallnum(callnum, CallNumberType.DEWEY, isSerial));
		assertEquals("505 .N285B", CallNumUtils.getLoppedCallnum(callnum, CallNumberType.DEWEY, !isSerial));
		callnum = "505 .N285 V.458:543--1212 2009";
		assertEquals("505 .N285", CallNumUtils.getLoppedCallnum(callnum, CallNumberType.DEWEY, isSerial));
		assertEquals("505 .N285", CallNumUtils.getLoppedCallnum(callnum, CallNumberType.DEWEY, !isSerial));

		// year then vol
		callnum = "553.2805 .P117 2009 SEP";  // 7888686
		assertEquals("553.2805 .P117", CallNumUtils.getLoppedCallnum(callnum, CallNumberType.DEWEY, isSerial));
		assertEquals("553.2805 .P117 2009", CallNumUtils.getLoppedCallnum(callnum, CallNumberType.DEWEY, !isSerial));
	}
	
	/**
	 * test lopping of Non-LC, Non-Dewey call numbers.  Serial and non-Serial 
	 *  flavor
	 */
@Test
	public void testOtherLopping() {
		// no lopping
		String callnum = "HE 20.6209/8:";
		assertEquals(callnum, CallNumUtils.getLoppedCallnum(callnum, CallNumberType.SUDOC, isSerial));
		assertEquals(callnum, CallNumUtils.getLoppedCallnum(callnum, CallNumberType.SUDOC, !isSerial));
		callnum = "Y 4.AG 8/1:108-16";
		assertEquals(callnum, CallNumUtils.getLoppedCallnum(callnum, CallNumberType.SUDOC, isSerial));
		assertEquals(callnum, CallNumUtils.getLoppedCallnum(callnum, CallNumberType.SUDOC, !isSerial));
		callnum = "GA 1.13:RCED-85-88";
		assertEquals(callnum, CallNumUtils.getLoppedCallnum(callnum, CallNumberType.SUDOC, isSerial));
		assertEquals(callnum, CallNumUtils.getLoppedCallnum(callnum, CallNumberType.SUDOC, !isSerial));
		callnum = "D 208.2:IT 1 R";
		assertEquals(callnum, CallNumUtils.getLoppedCallnum(callnum, CallNumberType.SUDOC, isSerial));
		assertEquals(callnum, CallNumUtils.getLoppedCallnum(callnum, CallNumberType.SUDOC, !isSerial));
		callnum = "M1621 .Y";  // 287900
		assertEquals(callnum, CallNumUtils.getLoppedCallnum(callnum, CallNumberType.OTHER, isSerial));
		assertEquals(callnum, CallNumUtils.getLoppedCallnum(callnum, CallNumberType.OTHER, !isSerial));
		callnum = "SUSEL-69048";
		assertEquals(callnum, CallNumUtils.getLoppedCallnum(callnum, CallNumberType.OTHER, isSerial));
		assertEquals(callnum, CallNumUtils.getLoppedCallnum(callnum, CallNumberType.OTHER, !isSerial));
		
		// vol piece only
		callnum = "DPI/SER.Z/3/2008";
		assertEquals("DPI/SER.Z/3", CallNumUtils.getLoppedCallnum(callnum, CallNumberType.SUDOC, isSerial));
		assertEquals(callnum, CallNumUtils.getLoppedCallnum(callnum, CallNumberType.SUDOC, !isSerial));
		callnum = "ST/GENEVA/LIB/SER.B/REF.";
		assertEquals("ST/GENEVA/LIB", CallNumUtils.getLoppedCallnum(callnum, CallNumberType.OTHER, isSerial));
		assertEquals("ST/GENEVA/LIB", CallNumUtils.getLoppedCallnum(callnum, CallNumberType.OTHER, !isSerial));
		callnum = "TD 5.9:V.6/986";
		assertEquals("TD 5.9", CallNumUtils.getLoppedCallnum(callnum, CallNumberType.SUDOC, isSerial));
		assertEquals("TD 5.9", CallNumUtils.getLoppedCallnum(callnum, CallNumberType.SUDOC, !isSerial));
		callnum = "M1522 BOX 1";
		assertEquals("M1522", CallNumUtils.getLoppedCallnum(callnum, CallNumberType.OTHER, isSerial));
		assertEquals("M1522", CallNumUtils.getLoppedCallnum(callnum, CallNumberType.OTHER, !isSerial));
		callnum = "MFILM N.S. 1350 REEL 230 NO. 3741";
// NOTE:  actually lopped by longest common prefix, so ok.
//		assertEquals("MFILM N.S. 1350 REEL 230", CallNumUtils.getLoppedCallnum(callnum, CallNumberType.OTHER, isSerial));
//		assertEquals("MFILM N.S. 1350 REEL 230", CallNumUtils.getLoppedCallnum(callnum, CallNumberType.OTHER, !isSerial));						
		callnum = "CALIF L1080 .J67 V.1-12:NO.1";
		assertEquals("CALIF L1080 .J67", CallNumUtils.getLoppedCallnum(callnum, CallNumberType.OTHER, isSerial));		
		assertEquals("CALIF L1080 .J67", CallNumUtils.getLoppedCallnum(callnum, CallNumberType.OTHER, !isSerial));
		// dewey cutter invalid: starts 2 letters - treated as non-dewey		
		callnum = "888.4 .JF78A V.5";
		assertEquals("888.4 .JF78A", CallNumUtils.getLoppedCallnum(callnum, CallNumberType.OTHER, isSerial));
		assertEquals("888.4 .JF78A", CallNumUtils.getLoppedCallnum(callnum, CallNumberType.OTHER, !isSerial));
		
		// year only
		callnum = "CALIF D210 .B34GE 2008";
		assertEquals("CALIF D210 .B34GE", CallNumUtils.getLoppedCallnum(callnum, CallNumberType.OTHER, isSerial));
		assertEquals(callnum, CallNumUtils.getLoppedCallnum(callnum, CallNumberType.LC, !isSerial));
		callnum = "CALIF S405 .R4 2000";
		assertEquals("CALIF S405 .R4", CallNumUtils.getLoppedCallnum(callnum, CallNumberType.OTHER, isSerial));
		assertEquals(callnum, CallNumUtils.getLoppedCallnum(callnum, CallNumberType.LC, !isSerial));
		callnum = "E 8.1: 2006";
		assertEquals("E 8.1", CallNumUtils.getLoppedCallnum(callnum, CallNumberType.OTHER, isSerial));
		assertEquals(callnum, CallNumUtils.getLoppedCallnum(callnum, CallNumberType.OTHER, !isSerial));
		
		// vol then year
		callnum = "CALIF G255 .R4 NO.I-1B 1978"; // 425082
		assertEquals("CALIF G255 .R4", CallNumUtils.getLoppedCallnum(callnum, CallNumberType.OTHER, isSerial));
		assertEquals("CALIF G255 .R4", CallNumUtils.getLoppedCallnum(callnum, CallNumberType.OTHER, !isSerial));
		
		// year then vol
		callnum = "CALIF G255 .R4 1978 OCT.23";
		assertEquals("CALIF G255 .R4", CallNumUtils.getLoppedCallnum(callnum, CallNumberType.OTHER, isSerial));
		assertEquals("CALIF G255 .R4 1978", CallNumUtils.getLoppedCallnum(callnum, CallNumberType.OTHER, !isSerial));
		callnum = "CALIF G255 .R4 NO.I-1B 1978 OCT.23";
		assertEquals("CALIF G255 .R4 NO.I-1B", CallNumUtils.getLoppedCallnum(callnum, CallNumberType.OTHER, isSerial));
		assertEquals("CALIF G255 .R4 NO.I-1B 1978", CallNumUtils.getLoppedCallnum(callnum, CallNumberType.OTHER, !isSerial));
	}
	
// TODO: does this test belong elsewhere?
	/**
	 * test that lopping shelve-by-title call numbers is correct
	 */
@Test
	public void testLoppingShelbyTitleCallnum()
	{
		boolean isSerial = true;

		// vol only
		String callnum = "V.35-37 1984-1986";  // 497457   LCPER
		assertEquals("V.35-37", CallNumUtils.getLoppedCallnum(callnum, CallNumberType.LC, isSerial));
		
		// LC
		callnum = "QA276 .J86 V.27 NOS.4-6 2006";  // 491239  LCPER
		assertEquals("QA276 .J86", CallNumUtils.getLoppedCallnum(callnum, CallNumberType.LC, isSerial));
		
// TODO: Dewey shelve-by-title tests
	}

	/**
	 * test choice of preferred item is correct.  It should be
	 *   1.  the barcode for the item with the longest LC call number.
	 *   2.  if no LC call numbers, the item with the longest Dewey call number.
	 *   3.  if no LC and no Dewey, the item with the longest Sudoc call number.
	 *   4.  otherwise, the longest call number.
	 */
@Test
	public void testPreferredItemBarcode()
	{
		mappingTestInit();
		String fldName = "preferred_barcode";
	    String testFilePath = testDataParentPath + File.separator + "itemPreferredTests.mrc";
	    solrFldMapTest.assertSolrFldValue(testFilePath, "oneLC", fldName, "11");
	    solrFldMapTest.assertSolrFldValue(testFilePath, "oneDewey", fldName, "22");
	    solrFldMapTest.assertSolrFldValue(testFilePath, "oneSudoc", fldName, "33");
	    solrFldMapTest.assertSolrFldValue(testFilePath, "oneOther", fldName, "44");
	    solrFldMapTest.assertNoSolrFld(testFilePath, "noItems", fldName);
	    solrFldMapTest.assertSolrFldValue(testFilePath, "badLC", fldName, "55");
	    solrFldMapTest.assertSolrFldValue(testFilePath, "badDewey", fldName, "66");
	    solrFldMapTest.assertNoSolrFld(testFilePath, "onlineItem", fldName);
	    solrFldMapTest.assertNoSolrFld(testFilePath, "ignoredCallnum", fldName);
	    solrFldMapTest.assertNoSolrFld(testFilePath, "shelbyLoc", fldName);
	    solrFldMapTest.assertSolrFldValue(testFilePath, "oneEach", fldName, "11");
	    solrFldMapTest.assertSolrFldValue(testFilePath, "noLC", fldName, "22");
	    solrFldMapTest.assertSolrFldValue(testFilePath, "noLCnoDewey", fldName, "33");
	    solrFldMapTest.assertSolrFldValue(testFilePath, "multLC", fldName, "12");
	    solrFldMapTest.assertSolrFldValue(testFilePath, "multLC2", fldName, "11");
	    solrFldMapTest.assertSolrFldValue(testFilePath, "multDewey", fldName, "24");
	    solrFldMapTest.assertSolrFldValue(testFilePath, "multSudoc", fldName, "34");
	    solrFldMapTest.assertSolrFldValue(testFilePath, "multOther", fldName, "45");
	}

}
