package edu.stanford;

import static org.junit.Assert.*;

import edu.stanford.AbstractStanfordBlacklightTest;

import org.junit.*;

/**
 * unit tests for edu.stanford.ItemUtils methods
 * @author Naomi Dushay
 */
public class ItemUtilsUnitTests {

	private static final String lcScheme = AbstractStanfordBlacklightTest.lcScheme;
	private static final String lcperScheme = AbstractStanfordBlacklightTest.lcperScheme;
	private static final String deweyScheme = AbstractStanfordBlacklightTest.deweyScheme;
	private static final String sudocScheme = AbstractStanfordBlacklightTest.sudocScheme;
	private static final String alphanumScheme = AbstractStanfordBlacklightTest.alphanumScheme;
	private static final String otherScheme = "ANYTHING_ELSE";	
	private static final boolean isSerial = true;

// TODO:  for matching shelfkey field value with item_display shelfkey value for nearby-on-shelf
//
//  note that ItemUtils lopping and shelfkey methods take info on 
//    callnum scheme and whether record is for serial, 	and calls appropriate
//    CallNumUtil method.   Basically, have single place to keep that logic.
	
// 1.  get ItemUtils working like (ItemDisplay) 
//  1a.  lopping 
//  2a.  shelfkeys
//
// 2.  understand differences between shelfkey field value and shelfkey value in item_display
//
// 3.  change ItemUtils lopping to work as desired
//  3a.  lopping  (scheme, serial sensitive)
//  3b.  shelfkey  (scheme sensitive)
//
// 4. use ItemUtils methods
//  4a. for item_display
//  4b. for shelfkeys
//
// 5. only lop once per item, then create shelfkey, reverse_shelfkey and vol sort in Item object 
//  5a.  should lopping happen in context of Item (Item needs to know if it's a serial for lopping),
//        and then set all the other values?
	
	/**
	 * test lopping of LC call numbers.  Serial and non-Serial flavor
	 */
@Test
	public void testLCLopping() {
		//		loppedCallnum = CallNumUtils.removeLCSerialVolSuffix(fullCallnum);
		//		loppedCallnum = CallNumUtils.removeLCVolSuffix(fullCallnum);
	
		// no lopping
		String callnum = "HE270 .I854";
		assertEquals(callnum, ItemUtils.getLoppedCallnum(callnum, lcScheme, isSerial));
		assertEquals(callnum, ItemUtils.getLoppedCallnum(callnum, lcScheme, !isSerial));

		// Vol piece only
		callnum = "HE 2708.I854 V.666";
		assertEquals("HE 2708.I854", ItemUtils.getLoppedCallnum(callnum, lcScheme, isSerial));
		assertEquals("HE 2708.I854", ItemUtils.getLoppedCallnum(callnum, lcScheme, !isSerial));
		callnum = "BM198.2 .H85 OCT 2006";
		assertEquals("BM198.2 .H85", ItemUtils.getLoppedCallnum(callnum, lcScheme, isSerial));
		assertEquals("BM198.2 .H85", ItemUtils.getLoppedCallnum(callnum, lcScheme, !isSerial));
		
		// year suffix - should be lopped for serial only
		callnum = "M270 .I854 1999";
		assertEquals("M270 .I854", ItemUtils.getLoppedCallnum(callnum, lcScheme, isSerial));
		assertEquals(callnum, ItemUtils.getLoppedCallnum(callnum, lcScheme, !isSerial));

		// vol then year
		callnum = "TX519 .D26 V.2 1966";  
		assertEquals("TX519 .D26", ItemUtils.getLoppedCallnum(callnum, lcScheme, isSerial));
		assertEquals("TX519 .D26", ItemUtils.getLoppedCallnum(callnum, lcScheme, !isSerial));
		
		// year then vol - year suffix should be lopped for serial only
		callnum = "TX519 .D26 1954 V.2";  
		assertEquals("TX519 .D26", ItemUtils.getLoppedCallnum(callnum, lcScheme, isSerial));
		assertEquals("TX519 .D26 1954", ItemUtils.getLoppedCallnum(callnum, lcScheme, !isSerial));		
	}
	

	/**
	 * test lopping of Dewey call numbers.  Serial and non-Serial flavor
	 */
@Test
	public void testDeweyLopping() {		
		//		loppedCallnum = CallNumUtils.removeDeweySerialVolSuffix(fullCallnum);
		//		loppedCallnum = CallNumUtils.removeDeweyVolSuffix(fullCallnum);
		
		// no lopping
		String callnum = "553.2805 .P117";
		assertEquals(callnum, ItemUtils.getLoppedCallnum(callnum, deweyScheme, isSerial));
		assertEquals(callnum, ItemUtils.getLoppedCallnum(callnum, deweyScheme, !isSerial));

		// Vol piece only
		callnum = "553.2805 .P117 NOV/DEC 2009";  // 7888686
		assertEquals("553.2805 .P117", ItemUtils.getLoppedCallnum(callnum, deweyScheme, isSerial));
		assertEquals("553.2805 .P117", ItemUtils.getLoppedCallnum(callnum, deweyScheme, !isSerial));
		callnum = "621.38406 .B865 F V.5:NO.3-6 2007/2008";  // 6913279
		assertEquals("621.38406 .B865 F", ItemUtils.getLoppedCallnum(callnum, deweyScheme, isSerial));
		assertEquals("621.38406 .B865 F", ItemUtils.getLoppedCallnum(callnum, deweyScheme, !isSerial));
				
		// year only
		callnum = "331.06931 .N566 2007";  // 7752489
		assertEquals("331.06931 .N566", ItemUtils.getLoppedCallnum(callnum, deweyScheme, isSerial));
		assertEquals(callnum, ItemUtils.getLoppedCallnum(callnum, deweyScheme, !isSerial));
		
		// vol then year
		callnum = "505 .N285B V.241-245 1973";
		assertEquals("505 .N285B", ItemUtils.getLoppedCallnum(callnum, deweyScheme, isSerial));
		assertEquals("505 .N285B", ItemUtils.getLoppedCallnum(callnum, deweyScheme, !isSerial));
		callnum = "505 .N285 V.458:543--1212 2009";
		assertEquals("505 .N285", ItemUtils.getLoppedCallnum(callnum, deweyScheme, isSerial));
		assertEquals("505 .N285", ItemUtils.getLoppedCallnum(callnum, deweyScheme, !isSerial));

		// year then vol
		callnum = "553.2805 .P117 2009 SEP";  // 7888686
		assertEquals("553.2805 .P117", ItemUtils.getLoppedCallnum(callnum, deweyScheme, isSerial));
		assertEquals("553.2805 .P117 2009", ItemUtils.getLoppedCallnum(callnum, deweyScheme, !isSerial));
	}
	
	/**
	 * test lopping of Non-LC, Non-Dewey call numbers.  Serial and non-Serial 
	 *  flavor
	 */
@Test
	public void testOtherLopping() {
		//		loppedCallnum = CallNumUtils.removeNonLCDeweySerialVolSuffix(fullCallnum, callnumScheme);
		//		loppedCallnum = CallNumUtils.removeNonLCDeweyVolSuffix(fullCallnum, callnumScheme);
		
		// no lopping
		String callnum = "HE 20.6209/8:";
		assertEquals(callnum, ItemUtils.getLoppedCallnum(callnum, sudocScheme, isSerial));
		assertEquals(callnum, ItemUtils.getLoppedCallnum(callnum, sudocScheme, !isSerial));
		callnum = "Y 4.AG 8/1:108-16";
		assertEquals(callnum, ItemUtils.getLoppedCallnum(callnum, sudocScheme, isSerial));
		assertEquals(callnum, ItemUtils.getLoppedCallnum(callnum, sudocScheme, !isSerial));
		callnum = "GA 1.13:RCED-85-88";
		assertEquals(callnum, ItemUtils.getLoppedCallnum(callnum, sudocScheme, isSerial));
		assertEquals(callnum, ItemUtils.getLoppedCallnum(callnum, sudocScheme, !isSerial));
		callnum = "D 208.2:IT 1 R";
		assertEquals(callnum, ItemUtils.getLoppedCallnum(callnum, sudocScheme, isSerial));
		assertEquals(callnum, ItemUtils.getLoppedCallnum(callnum, sudocScheme, !isSerial));
		callnum = "M1621 .Y";  // 287900
		assertEquals(callnum, ItemUtils.getLoppedCallnum(callnum, alphanumScheme, isSerial));
		assertEquals(callnum, ItemUtils.getLoppedCallnum(callnum, alphanumScheme, !isSerial));
		callnum = "SUSEL-69048";
		assertEquals(callnum, ItemUtils.getLoppedCallnum(callnum, alphanumScheme, isSerial));
		assertEquals(callnum, ItemUtils.getLoppedCallnum(callnum, alphanumScheme, !isSerial));
		
		// vol piece only
		callnum = "DPI/SER.Z/3/2008";
		assertEquals("DPI/SER.Z/3", ItemUtils.getLoppedCallnum(callnum, sudocScheme, isSerial));
		assertEquals(callnum, ItemUtils.getLoppedCallnum(callnum, sudocScheme, !isSerial));
		callnum = "ST/GENEVA/LIB/SER.B/REF.";
		assertEquals("ST/GENEVA/LIB", ItemUtils.getLoppedCallnum(callnum, alphanumScheme, isSerial));
		assertEquals("ST/GENEVA/LIB", ItemUtils.getLoppedCallnum(callnum, alphanumScheme, !isSerial));
		callnum = "TD 5.9:V.6/986";
		assertEquals("TD 5.9", ItemUtils.getLoppedCallnum(callnum, sudocScheme, isSerial));
		assertEquals("TD 5.9", ItemUtils.getLoppedCallnum(callnum, sudocScheme, !isSerial));
		callnum = "M1522 BOX 1";
		assertEquals("M1522", ItemUtils.getLoppedCallnum(callnum, alphanumScheme, isSerial));
		assertEquals("M1522", ItemUtils.getLoppedCallnum(callnum, alphanumScheme, !isSerial));
		callnum = "MFILM N.S. 1350 REEL 230 NO. 3741";
// FIXME:  needs lopping?  MFLIM is no longer lopped  2009-12-03
//		assertEquals("MFILM N.S. 1350 REEL 230", ItemUtils.getLoppedCallnum(callnum, alphanumScheme, isSerial));
//		assertEquals("MFILM N.S. 1350 REEL 230", ItemUtils.getLoppedCallnum(callnum, alphanumScheme, !isSerial));						
		callnum = "CALIF L1080 .J67 V.1-12:NO.1";
		assertEquals("CALIF L1080 .J67", ItemUtils.getLoppedCallnum(callnum, alphanumScheme, isSerial));		
		assertEquals("CALIF L1080 .J67", ItemUtils.getLoppedCallnum(callnum, alphanumScheme, !isSerial));
		// dewey cutter invalid: starts 2 letters - treated as non-dewey		
		callnum = "888.4 .JF78A V.5";
		assertEquals("888.4 .JF78A", ItemUtils.getLoppedCallnum(callnum, otherScheme, isSerial));
		assertEquals("888.4 .JF78A", ItemUtils.getLoppedCallnum(callnum, otherScheme, !isSerial));
		
		// year only
		callnum = "CALIF D210 .B34GE 2008";
		assertEquals("CALIF D210 .B34GE", ItemUtils.getLoppedCallnum(callnum, alphanumScheme, isSerial));
		assertEquals(callnum, ItemUtils.getLoppedCallnum(callnum, lcScheme, !isSerial));
		callnum = "CALIF S405 .R4 2000";
		assertEquals("CALIF S405 .R4", ItemUtils.getLoppedCallnum(callnum, alphanumScheme, isSerial));
		assertEquals(callnum, ItemUtils.getLoppedCallnum(callnum, lcScheme, !isSerial));
		callnum = "E 8.1: 2006";
		assertEquals("E 8.1", ItemUtils.getLoppedCallnum(callnum, alphanumScheme, isSerial));
		assertEquals(callnum, ItemUtils.getLoppedCallnum(callnum, alphanumScheme, !isSerial));
		
		// vol then year
		callnum = "CALIF G255 .R4 NO.I-1B 1978"; // 425082
		assertEquals("CALIF G255 .R4", ItemUtils.getLoppedCallnum(callnum, alphanumScheme, isSerial));
		assertEquals("CALIF G255 .R4", ItemUtils.getLoppedCallnum(callnum, alphanumScheme, !isSerial));
		
		// year then vol
		callnum = "CALIF G255 .R4 1978 OCT.23";
		assertEquals("CALIF G255 .R4", ItemUtils.getLoppedCallnum(callnum, alphanumScheme, isSerial));
		assertEquals("CALIF G255 .R4 1978", ItemUtils.getLoppedCallnum(callnum, alphanumScheme, !isSerial));
		callnum = "CALIF G255 .R4 NO.I-1B 1978 OCT.23";
		assertEquals("CALIF G255 .R4 NO.I-1B", ItemUtils.getLoppedCallnum(callnum, alphanumScheme, isSerial));
		assertEquals("CALIF G255 .R4 NO.I-1B 1978", ItemUtils.getLoppedCallnum(callnum, alphanumScheme, !isSerial));
	}
	
// TODO: this test belongs elsewhere?
	/**
	 * test that lopping shelve-by-title call numbers
	 */
@Test
	public void testLoppingShelbyTitleCallnum()
	{
		boolean isSerial = true;

		// vol only
		String callnum = "V.35-37 1984-1986";  // 497457   LCPER
		assertEquals("V.35-37", ItemUtils.getLoppedCallnum(callnum, lcperScheme, isSerial));
		
		// LC
		callnum = "QA276 .J86 V.27 NOS.4-6 2006";  // 491239  LCPER
		assertEquals("QA276 .J86", ItemUtils.getLoppedCallnum(callnum, lcperScheme, isSerial));
		
// TODO: Dewey shelve-by-title tests
	}

}
