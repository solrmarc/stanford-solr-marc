package edu.stanford;

import static edu.stanford.CallNumUtils.removeYearSuffix;
import static org.junit.Assert.assertEquals;

import org.junit.Test;

import edu.stanford.enumValues.CallNumberType;

/**
 * unit tests for edu.stanford.CallNumberUtils lopping methods
 * @author Naomi Dushay
 */
public class CallNumLoppingUnitTests extends AbstractStanfordBlacklightTest {

	/**
	 * test that T is not lopped when it shouldn't be
	 */
@Test
	public void testTLopping() {
		String callnum;
		
		callnum = "519 .D26ST 1965 V.1 TESTS";  // 5621053
		assertEquals("519 .D26ST", CallNumUtils.removeDeweySerialVolSuffix(callnum));
		assertEquals("519 .D26ST 1965", CallNumUtils.removeDeweyVolSuffix(callnum));
		callnum = "D 208.2:IT 1 R";
		assertEquals(callnum, CallNumUtils.removeNonLCDeweySerialVolSuffix(callnum, CallNumberType.SUDOC));
		assertEquals(callnum, CallNumUtils.removeNonLCDeweyVolSuffix(callnum, CallNumberType.SUDOC));
		callnum = "ST/GENEVA/LIB/SER.B/REF.";
		assertEquals("ST/GENEVA/LIB", CallNumUtils.removeNonLCDeweySerialVolSuffix(callnum, CallNumberType.OTHER));
		assertEquals("ST/GENEVA/LIB", CallNumUtils.removeNonLCDeweyVolSuffix(callnum, CallNumberType.OTHER));
		
		callnum = "519 .L18ST GRADE 1";
		assertEquals("519 .L18ST", CallNumUtils.removeDeweySerialVolSuffix(callnum));
		assertEquals("519 .L18ST", CallNumUtils.removeDeweyVolSuffix(callnum));

// NOTE:  this is addressed in situ with longest-common-prefix approach 
// for non-LC, non-Dewey call number lopping
//		callnum = "CALIF T900 .J6 V.1-2";
//		assertEquals("CALIF T900 .J6", CallNumUtils.removeNonLCDeweyVolSuffix(callnum, CallNumberType.OTHER));
//		assertEquals("CALIF T900 .J6", CallNumUtils.removeNonLCDeweySerialVolSuffix(callnum, CallNumberType.OTHER));
	}

	/**
	 * test that when more than one month is in the call number, that
	 *  the first month is used for lopping
	 */
@Test 
	public void testMultipleMonthLopping() {
		String callnum = "553.2805 .P117 NOV/DEC 2009";  // 7888686
		assertEquals("553.2805 .P117", CallNumUtils.removeDeweySerialVolSuffix(callnum));
		assertEquals("553.2805 .P117", CallNumUtils.removeDeweyVolSuffix(callnum));
	
		callnum = "553.2805 .P117 2009:SEPT./OCT";  // 7888686
		assertEquals("553.2805 .P117", CallNumUtils.removeDeweySerialVolSuffix(callnum));
		assertEquals("553.2805 .P117 2009", CallNumUtils.removeDeweyVolSuffix(callnum));
	}
	
	/**
	 * test lopping of Non-LC, Non-Dewey call numbers.  Serial and non-Serial 
	 *  flavor
	 */
@Test
	public void testSerialYearLopping() {
		String callnum;
	
		// year only
		callnum = "M270 .I854 1999";
		assertEquals("M270 .I854", CallNumUtils.removeLCSerialVolSuffix(callnum));
		assertEquals(callnum, CallNumUtils.removeLCVolSuffix(callnum));
		callnum = "Z7511 .N33 1968-1971";
		assertEquals("Z7511 .N33", removeYearSuffix(callnum));
		
		callnum = "331.06931 .N566 2007";  // 7752489
		assertEquals("331.06931 .N566", CallNumUtils.removeDeweySerialVolSuffix(callnum));
		assertEquals(callnum, CallNumUtils.removeDeweyVolSuffix(callnum));
		callnum = "505 .N285 V.434:1-680 2005";
		assertEquals("505 .N285", CallNumUtils.removeDeweyVolSuffix(callnum));
		assertEquals("505 .N285", CallNumUtils.removeDeweySerialVolSuffix(callnum));
	
		callnum = "HE 2708.I854";  // bad LC - four digits in class
		assertEquals(callnum, CallNumUtils.removeNonLCDeweySerialVolSuffix(callnum, CallNumberType.OTHER));
		assertEquals(callnum, CallNumUtils.removeNonLCDeweyVolSuffix(callnum, CallNumberType.OTHER));
		
		callnum = "CALIF D210 .B34GE 2008";
		assertEquals("CALIF D210 .B34GE", CallNumUtils.removeNonLCDeweySerialVolSuffix(callnum, CallNumberType.OTHER));
		assertEquals(callnum, CallNumUtils.removeNonLCDeweyVolSuffix(callnum, CallNumberType.OTHER));
		callnum = "CALIF S405 .R4 2000";
		assertEquals("CALIF S405 .R4", CallNumUtils.removeNonLCDeweySerialVolSuffix(callnum, CallNumberType.OTHER));
		assertEquals(callnum, CallNumUtils.removeNonLCDeweyVolSuffix(callnum, CallNumberType.OTHER));
		callnum = "E 8.1: 2006";
		assertEquals("E 8.1", CallNumUtils.removeNonLCDeweySerialVolSuffix(callnum, CallNumberType.OTHER));
		assertEquals(callnum, CallNumUtils.removeNonLCDeweyVolSuffix(callnum, CallNumberType.OTHER));
		
		// vol then year
		callnum = "CALIF G255 .R4 NO.I-1B 1978"; // 425082
		assertEquals("CALIF G255 .R4", CallNumUtils.removeNonLCDeweySerialVolSuffix(callnum, CallNumberType.OTHER));
		assertEquals("CALIF G255 .R4", CallNumUtils.removeNonLCDeweyVolSuffix(callnum, CallNumberType.OTHER));
		
		// year then vol
		callnum = "TX519 .D26S 1954 V.2";  
		assertEquals("TX519 .D26S", CallNumUtils.removeLCSerialVolSuffix(callnum));		
		assertEquals("TX519 .D26S 1954", CallNumUtils.removeLCVolSuffix(callnum));		
		callnum = "CALIF G255 .R4 1978 OCT.23";
		assertEquals("CALIF G255 .R4", CallNumUtils.removeNonLCDeweySerialVolSuffix(callnum, CallNumberType.OTHER));
		assertEquals("CALIF G255 .R4 1978", CallNumUtils.removeNonLCDeweyVolSuffix(callnum, CallNumberType.OTHER));
		callnum = "CALIF G255 .R4 NO.I-1B 1978 OCT.23";
		assertEquals("CALIF G255 .R4 NO.I-1B", CallNumUtils.removeNonLCDeweySerialVolSuffix(callnum, CallNumberType.OTHER));
		assertEquals("CALIF G255 .R4 NO.I-1B 1978", CallNumUtils.removeNonLCDeweyVolSuffix(callnum, CallNumberType.OTHER));
		callnum = "QD1 .C59 1975:V.1-742";
		assertEquals("QD1 .C59", CallNumUtils.removeLCSerialVolSuffix(callnum));
		assertEquals("QD1 .C59 1975", CallNumUtils.removeLCVolSuffix(callnum));
	}

	/**
	 * test lopping for MFLIM call number
	 */
@Test
	public void testMfilmLopping()
	{
		String callnum = "MIFLM N.S. 16951";  // 8218359
		assertEquals(callnum, CallNumUtils.removeNonLCDeweySerialVolSuffix(callnum, CallNumberType.OTHER));
		assertEquals(callnum, CallNumUtils.removeNonLCDeweyVolSuffix(callnum, CallNumberType.OTHER));	
		callnum = "MFILM N.S. 14056 ITEM 32";  // 2949649
		assertEquals(callnum, CallNumUtils.removeNonLCDeweySerialVolSuffix(callnum, CallNumberType.OTHER));
		assertEquals(callnum, CallNumUtils.removeNonLCDeweyVolSuffix(callnum, CallNumberType.OTHER));	
		callnum = "MFILM N.S. 10300 REEL 154-156";  // 2457656
		assertEquals(callnum, CallNumUtils.removeNonLCDeweySerialVolSuffix(callnum, CallNumberType.OTHER));
		assertEquals(callnum, CallNumUtils.removeNonLCDeweyVolSuffix(callnum, CallNumberType.OTHER));	
// NOTE:  this is addressed in situ with longest-common-prefix approach 
// for non-LC, non-Dewey call number lopping
//		callnum = "MFILM N.S. 1350 REEL 230 NO. 3741";
//		assertEquals("MFILM N.S. 1350 REEL 230", CallNumUtils.removeNonLCDeweySerialVolSuffix(callnum, CallNumberType.OTHER));
//		assertEquals("MFILM N.S. 1350 REEL 230", CallNumUtils.removeNonLCDeweyVolSuffix(callnum, CallNumberType.OTHER));	
	}

	/**
	 * test lopping for MFLIM call number
	 */
@Test
	public void testZDVDLopping()
	{
		String callnum = "ZDVD 21237";  // 8220939
		assertEquals(callnum, CallNumUtils.removeNonLCDeweySerialVolSuffix(callnum, CallNumberType.OTHER));
		assertEquals(callnum, CallNumUtils.removeNonLCDeweyVolSuffix(callnum, CallNumberType.OTHER));
		callnum = "ZDVD 21145 DISC 1";  // 8325088
// NOTE:  this is addressed in situ with longest-common-prefix approach 
// for non-LC, non-Dewey call number lopping
//		assertEquals("ZDVD 21145", CallNumUtils.removeNonLCDeweySerialVolSuffix(callnum, CallNumberType.OTHER));
//		assertEquals("ZDVD 21145", CallNumUtils.removeNonLCDeweyVolSuffix(callnum, CallNumberType.OTHER));
	}
	
	/**
	 * test lopping for MFLIM call number
	 */
@Test
	public void testMusicRecordingLopping()
	{
// TODO:  should only remove DISC etc. suffix if it has a number (disk 1)	
		String callnum = "MCD 15528";  // 7785015
		assertEquals(callnum, CallNumUtils.removeNonLCDeweySerialVolSuffix(callnum, CallNumberType.OTHER));
		assertEquals(callnum, CallNumUtils.removeNonLCDeweyVolSuffix(callnum, CallNumberType.OTHER));
		callnum = "MCD 17393 DISC 1";  //8104760
// NOTE:  this is addressed in situ with longest-common-prefix approach 
// for non-LC, non-Dewey call number lopping
//		assertEquals("MCD 17393", CallNumUtils.removeNonLCDeweySerialVolSuffix(callnum, CallNumberType.OTHER));
//		assertEquals("MCD 17393", CallNumUtils.removeNonLCDeweyVolSuffix(callnum, CallNumberType.OTHER));
		assertEquals(callnum, CallNumUtils.removeNonLCDeweySerialVolSuffix(callnum, CallNumberType.OTHER));
		assertEquals(callnum, CallNumUtils.removeNonLCDeweyVolSuffix(callnum, CallNumberType.OTHER));	
		callnum = "MCD 17393 BOOKLET"; // 8104760
		assertEquals(callnum, CallNumUtils.removeNonLCDeweySerialVolSuffix(callnum, CallNumberType.OTHER));
		assertEquals(callnum, CallNumUtils.removeNonLCDeweyVolSuffix(callnum, CallNumberType.OTHER));
		callnum = "MCD 3361 1 BOOKLET";  // 312565
		assertEquals(callnum, CallNumUtils.removeNonLCDeweySerialVolSuffix(callnum, CallNumberType.OTHER));
		assertEquals(callnum, CallNumUtils.removeNonLCDeweyVolSuffix(callnum, CallNumberType.OTHER));

		callnum = "ACD 190";  // 313185
		assertEquals(callnum, CallNumUtils.removeNonLCDeweySerialVolSuffix(callnum, CallNumberType.OTHER));
		assertEquals(callnum, CallNumUtils.removeNonLCDeweyVolSuffix(callnum, CallNumberType.OTHER));
		callnum = "C 1047";  // 4359699
		assertEquals(callnum, CallNumUtils.removeNonLCDeweySerialVolSuffix(callnum, CallNumberType.OTHER));
		assertEquals(callnum, CallNumUtils.removeNonLCDeweyVolSuffix(callnum, CallNumberType.OTHER));
		callnum = "C 2061 (V.1)";  // 7925018
		assertEquals("C 2061", CallNumUtils.removeNonLCDeweySerialVolSuffix(callnum, CallNumberType.OTHER));
		assertEquals("C 2061", CallNumUtils.removeNonLCDeweyVolSuffix(callnum, CallNumberType.OTHER));
		callnum = "MD 6902";  // 300553
		assertEquals(callnum, CallNumUtils.removeNonLCDeweySerialVolSuffix(callnum, CallNumberType.OTHER));
		assertEquals(callnum, CallNumUtils.removeNonLCDeweyVolSuffix(callnum, CallNumberType.OTHER));
		callnum = "JVB 75830";  // 8106819
		assertEquals(callnum, CallNumUtils.removeNonLCDeweySerialVolSuffix(callnum, CallNumberType.OTHER));
		assertEquals(callnum, CallNumUtils.removeNonLCDeweyVolSuffix(callnum, CallNumberType.OTHER));
		callnum = "MDSC 1";  // 2779004
		assertEquals(callnum, CallNumUtils.removeNonLCDeweySerialVolSuffix(callnum, CallNumberType.OTHER));
		assertEquals(callnum, CallNumUtils.removeNonLCDeweyVolSuffix(callnum, CallNumberType.OTHER));		
		callnum = "MDS .V48 T78 P94";  // 300264 - alphanum
		assertEquals(callnum, CallNumUtils.removeNonLCDeweySerialVolSuffix(callnum, CallNumberType.OTHER));
		assertEquals(callnum, CallNumUtils.removeNonLCDeweyVolSuffix(callnum, CallNumberType.OTHER));
		callnum = "ZCD 23"; // 309475
		assertEquals(callnum, CallNumUtils.removeNonLCDeweySerialVolSuffix(callnum, CallNumberType.OTHER));
		assertEquals(callnum, CallNumUtils.removeNonLCDeweyVolSuffix(callnum, CallNumberType.OTHER));
		callnum = "ZCD 625";  // 312565
		assertEquals(callnum, CallNumUtils.removeNonLCDeweySerialVolSuffix(callnum, CallNumberType.OTHER));
		assertEquals(callnum, CallNumUtils.removeNonLCDeweyVolSuffix(callnum, CallNumberType.OTHER));
		callnum = "ZCD 625 TEXT";  // 312565
		assertEquals(callnum, CallNumUtils.removeNonLCDeweySerialVolSuffix(callnum, CallNumberType.OTHER));
		assertEquals(callnum, CallNumUtils.removeNonLCDeweyVolSuffix(callnum, CallNumberType.OTHER));
		callnum = "ZX1576 DISC";  // 4508
		assertEquals(callnum, CallNumUtils.removeNonLCDeweySerialVolSuffix(callnum, CallNumberType.OTHER));
		assertEquals(callnum, CallNumUtils.removeNonLCDeweyVolSuffix(callnum, CallNumberType.OTHER));
	}
	
	/**
	 * test vol lopping of month suffix
	 */
@Test
	public void testMonthSuffix() 
	{
		// LC
		String callnum = "BM198.2 .H85 OCT 2006";
		assertEquals("BM198.2 .H85", CallNumUtils.removeLCSerialVolSuffix(callnum));
		assertEquals("BM198.2 .H85", CallNumUtils.removeLCVolSuffix(callnum));
		callnum = "BM198.2 .H85 NOV 2006";
		assertEquals("BM198.2 .H85", CallNumUtils.removeLCSerialVolSuffix(callnum));
		assertEquals("BM198.2 .H85", CallNumUtils.removeLCVolSuffix(callnum));
		callnum = "BM198.2 .H85 DEC 2006";
		assertEquals("BM198.2 .H85", CallNumUtils.removeLCSerialVolSuffix(callnum));
		assertEquals("BM198.2 .H85", CallNumUtils.removeLCVolSuffix(callnum));
		callnum = "BM198.2 .H85 JAN 2007";
		assertEquals("BM198.2 .H85", CallNumUtils.removeLCSerialVolSuffix(callnum));
		assertEquals("BM198.2 .H85", CallNumUtils.removeLCVolSuffix(callnum));
		callnum = "BM198.2 .H85 FEB 2007";
		assertEquals("BM198.2 .H85", CallNumUtils.removeLCSerialVolSuffix(callnum));
		assertEquals("BM198.2 .H85", CallNumUtils.removeLCVolSuffix(callnum));
		callnum = "BM198.2 .H85 MAR 2007";
		assertEquals("BM198.2 .H85", CallNumUtils.removeLCSerialVolSuffix(callnum));
		assertEquals("BM198.2 .H85", CallNumUtils.removeLCVolSuffix(callnum));
		callnum = "BM198.2 .H85 APR 2007";
		assertEquals("BM198.2 .H85", CallNumUtils.removeLCSerialVolSuffix(callnum));
		assertEquals("BM198.2 .H85", CallNumUtils.removeLCVolSuffix(callnum));
		callnum = "BM198.2 .H85 MAY 2007";
		assertEquals("BM198.2 .H85", CallNumUtils.removeLCSerialVolSuffix(callnum));
		assertEquals("BM198.2 .H85", CallNumUtils.removeLCVolSuffix(callnum));
		callnum = "BM198.2 .H85 JUN 2007";
		assertEquals("BM198.2 .H85", CallNumUtils.removeLCSerialVolSuffix(callnum));
		assertEquals("BM198.2 .H85", CallNumUtils.removeLCVolSuffix(callnum));
		callnum = "BM198.2 .H85 JUL 2007";
		assertEquals("BM198.2 .H85", CallNumUtils.removeLCSerialVolSuffix(callnum));
		assertEquals("BM198.2 .H85", CallNumUtils.removeLCVolSuffix(callnum));
		callnum = "BM198.2 .H85 AUG 2007";
		assertEquals("BM198.2 .H85", CallNumUtils.removeLCSerialVolSuffix(callnum));
		assertEquals("BM198.2 .H85", CallNumUtils.removeLCVolSuffix(callnum));
		callnum = "BM198.2 .H85 SEP 2007";
		assertEquals("BM198.2 .H85", CallNumUtils.removeLCSerialVolSuffix(callnum));
		assertEquals("BM198.2 .H85", CallNumUtils.removeLCVolSuffix(callnum));
	    
		// SEPT
		callnum = "BM198.2 .H85 SEPT 2007";
		assertEquals("BM198.2 .H85", CallNumUtils.removeLCSerialVolSuffix(callnum));
		assertEquals("BM198.2 .H85", CallNumUtils.removeLCVolSuffix(callnum));
		
		// Dewey months not working?
		callnum = "553.2805 .P117 JAN";
		assertEquals("553.2805 .P117", CallNumUtils.removeDeweySerialVolSuffix(callnum));
		assertEquals("553.2805 .P117", CallNumUtils.removeDeweyVolSuffix(callnum));
		callnum = "553.2805 .P117 SEPT";  // 7888686
		assertEquals("553.2805 .P117", CallNumUtils.removeDeweySerialVolSuffix(callnum));
		assertEquals("553.2805 .P117", CallNumUtils.removeDeweyVolSuffix(callnum));
	}
	
	/**
	 * test vol lopping when call number has BOX suffix
	 */
@Test
	public void testBoxSuffix() 
	{
		String callnum = "M1522 BOX 1";
		assertEquals("M1522", CallNumUtils.removeNonLCDeweyVolSuffix(callnum, CallNumberType.OTHER));
		assertEquals("M1522", CallNumUtils.removeNonLCDeweySerialVolSuffix(callnum, CallNumberType.OTHER));
	}
	
	/**
	 * test vol lopping when call number has carton suffix
	 */
@Test
	public void testCartonSuffix() 
	{
		String callnum = "M1479 CARTON 1";
		assertEquals("M1479", CallNumUtils.removeNonLCDeweyVolSuffix(callnum, CallNumberType.OTHER));
		assertEquals("M1479", CallNumUtils.removeNonLCDeweySerialVolSuffix(callnum, CallNumberType.OTHER));
	}
	
	/**
	 * test vol lopping when call number has flat box suffix
	 */
@Test
	public void testFlatBoxSuffix() 
	{
		String callnum = "M1522 FLAT BOX 17";
		assertEquals("M1522", CallNumUtils.removeNonLCDeweyVolSuffix(callnum, CallNumberType.OTHER));
		assertEquals("M1522", CallNumUtils.removeNonLCDeweySerialVolSuffix(callnum, CallNumberType.OTHER));
	}
	
	/**
	 * test vol lopping when call number has half box suffix
	 */
@Test
	public void testHalfBoxSuffix() 
	{
		String callnum = "M1522 HALF BOX 1";
		assertEquals("M1522", CallNumUtils.removeNonLCDeweyVolSuffix(callnum, CallNumberType.OTHER));
		assertEquals("M1522", CallNumUtils.removeNonLCDeweySerialVolSuffix(callnum, CallNumberType.OTHER));
	}
	
	/**
	 * test vol lopping when call number has half carton suffix
	 */
@Test
	public void testHalfCartonSuffix() 
	{
		String callnum = "M1522 HALF CARTON 1";
		assertEquals("M1522", CallNumUtils.removeNonLCDeweyVolSuffix(callnum, CallNumberType.OTHER));
		assertEquals("M1522", CallNumUtils.removeNonLCDeweySerialVolSuffix(callnum, CallNumberType.OTHER));
	}
	
	/**
	 * test vol lopping when call number has carton suffix
	 */
@Test
	public void testIndexSuffix() 
	{
		String callnum = "ML1 .I614 INDEX 1969-1986";
		assertEquals("ML1 .I614", CallNumUtils.removeNonLCDeweyVolSuffix(callnum, CallNumberType.OTHER));
		assertEquals("ML1 .I614", CallNumUtils.removeNonLCDeweySerialVolSuffix(callnum, CallNumberType.OTHER));
	}
	
	/**
	 * test vol lopping when call number has large map folder suffix
	 */
@Test
	public void testLargeMapFolderSuffix() 
	{
		String callnum = "M1522 LARGE MAP FOLDER 26";
		assertEquals("M1522", CallNumUtils.removeNonLCDeweyVolSuffix(callnum, CallNumberType.OTHER));
		assertEquals("M1522", CallNumUtils.removeNonLCDeweySerialVolSuffix(callnum, CallNumberType.OTHER));
	}
	
	/**
	 * test vol lopping when call number has large folder suffix
	 */
@Test
	public void testLargeFolderSuffix() 
	{
		String callnum = "M1522 LARGE FOLDER 26";
		assertEquals("M1522", CallNumUtils.removeNonLCDeweyVolSuffix(callnum, CallNumberType.OTHER));
		assertEquals("M1522", CallNumUtils.removeNonLCDeweySerialVolSuffix(callnum, CallNumberType.OTHER));
	}
	
	/**
	 * test vol lopping when call number has map folder suffix
	 */
@Test
	public void testMapFolderSuffix() 
	{
		String callnum = "M1522 MAP FOLDER 26";
		assertEquals("M1522", CallNumUtils.removeNonLCDeweyVolSuffix(callnum, CallNumberType.OTHER));
		assertEquals("M1522", CallNumUtils.removeNonLCDeweySerialVolSuffix(callnum, CallNumberType.OTHER));
	}
		
	/**
	 * test vol lopping when call number has mfilm reel suffix
	 */
@Test
	public void testMfilmReelSuffix() 
	{
		String callnum = "CD3031 .A35 T-60 MFILM REEL 3";
		assertEquals("CD3031 .A35 T-60", CallNumUtils.removeNonLCDeweyVolSuffix(callnum, CallNumberType.OTHER));
		assertEquals("CD3031 .A35 T-60", CallNumUtils.removeNonLCDeweySerialVolSuffix(callnum, CallNumberType.OTHER));
	}
	
	/**
	 * test vol lopping when call number has os box suffix
	 */
@Test
	public void testOSBoxSuffix() 
	{
		String callnum = "M1522 OS BOX 26";
		assertEquals("M1522", CallNumUtils.removeNonLCDeweyVolSuffix(callnum, CallNumberType.OTHER));
		assertEquals("M1522", CallNumUtils.removeNonLCDeweySerialVolSuffix(callnum, CallNumberType.OTHER));
	}
	
	/**
	 * test vol lopping when call number has os folder suffix
	 */
@Test
	public void testOSFolderSuffix() 
	{
		String callnum = "M1522 OS FOLDER 26";
		assertEquals("M1522", CallNumUtils.removeNonLCDeweyVolSuffix(callnum, CallNumberType.OTHER));
		assertEquals("M1522", CallNumUtils.removeNonLCDeweySerialVolSuffix(callnum, CallNumberType.OTHER));
	}
	
	/**
	 * test vol lopping when call number has small map folder suffix
	 */
@Test
	public void testSmallMapFolderSuffix() 
	{
		String callnum = "M1522 SMALL MAP FOLDER 26";
		assertEquals("M1522", CallNumUtils.removeNonLCDeweyVolSuffix(callnum, CallNumberType.OTHER));
		assertEquals("M1522", CallNumUtils.removeNonLCDeweySerialVolSuffix(callnum, CallNumberType.OTHER));
	}
	
	/**
	 * test vol lopping when call number has small folder suffix
	 */
@Test
	public void testSmallFolderSuffix() 
	{
		String callnum = "M1522 SMALL FOLDER 26";
		assertEquals("M1522", CallNumUtils.removeNonLCDeweyVolSuffix(callnum, CallNumberType.OTHER));
		assertEquals("M1522", CallNumUtils.removeNonLCDeweySerialVolSuffix(callnum, CallNumberType.OTHER));
	}
	
	/**
	 * test vol lopping when call number has tube suffix
	 */
@Test
	public void testTubeSuffix() 
	{
		String callnum = "M1522 TUBE 26";
		assertEquals("M1522", CallNumUtils.removeNonLCDeweyVolSuffix(callnum, CallNumberType.OTHER));
		assertEquals("M1522", CallNumUtils.removeNonLCDeweySerialVolSuffix(callnum, CallNumberType.OTHER));
	}
	
	/**
	 * test vol lopping when call number has Series Box Suffix
	 */
@Test
	public void testSeriesBoxSuffix() 
	{
		String callnum = "SC 165 SERIES 5 BOX 1";
		assertEquals("SC 165", CallNumUtils.removeNonLCDeweyVolSuffix(callnum, CallNumberType.OTHER));
		assertEquals("SC 165", CallNumUtils.removeNonLCDeweySerialVolSuffix(callnum, CallNumberType.OTHER));
		
		callnum = "M1090 SERIES 24 BOX 1";
		assertEquals("M1090", CallNumUtils.removeNonLCDeweyVolSuffix(callnum, CallNumberType.OTHER));
		assertEquals("M1090", CallNumUtils.removeNonLCDeweySerialVolSuffix(callnum, CallNumberType.OTHER));
	}
	
	/**
	 * test vol lopping when call number has series half suffix
	 */
@Test
	public void testSeriesHalfBoxSuffix() 
	{
		String callnum = "M1090 SERIES 16 HALF BOX 1.1";
		assertEquals("M1090", CallNumUtils.removeNonLCDeweyVolSuffix(callnum, CallNumberType.OTHER));
		assertEquals("M1090", CallNumUtils.removeNonLCDeweySerialVolSuffix(callnum, CallNumberType.OTHER));
	
		callnum = "M1090 SERIES 6 HALF BOX 39B";
		assertEquals("M1090", CallNumUtils.removeNonLCDeweyVolSuffix(callnum, CallNumberType.OTHER));
		assertEquals("M1090", CallNumUtils.removeNonLCDeweySerialVolSuffix(callnum, CallNumberType.OTHER));
	}
	
	/**
	 * test vol lopping when call number has series os folder suffix
	 */
@Test
	public void testSeriesOSFolderSuffix() 
	{
		String callnum = "M1090 SERIES 16 OS FOLDER 276.3";
		assertEquals("M1090", CallNumUtils.removeNonLCDeweyVolSuffix(callnum, CallNumberType.OTHER));
		assertEquals("M1090", CallNumUtils.removeNonLCDeweySerialVolSuffix(callnum, CallNumberType.OTHER));
	}
	
	/**
	 * test vol lopping when call number has series small folder suffix
	 */
@Test
	public void testSeriesSmallFolderSuffix() 
	{
		String callnum = "M1090 SERIES 16 SMALL FOLDER 72.06";
		assertEquals("M1090", CallNumUtils.removeNonLCDeweyVolSuffix(callnum, CallNumberType.OTHER));
		assertEquals("M1090", CallNumUtils.removeNonLCDeweySerialVolSuffix(callnum, CallNumberType.OTHER));
	}
	
	/**
	 * test vol lopping when call number has series small map folder suffix
	 */
@Test
	public void testSeriesSmallMapFolderSuffix() 
	{
		String callnum = "M1090 SERIES 16 SMALL MAP FOLDER 72.02";
		assertEquals("M1090", CallNumUtils.removeNonLCDeweyVolSuffix(callnum, CallNumberType.OTHER));
		assertEquals("M1090", CallNumUtils.removeNonLCDeweySerialVolSuffix(callnum, CallNumberType.OTHER));
	}
	
	/**
	 * test vol lopping when call number has series large map folder suffix
	 */
@Test
	public void testSeriesLargMapFolderSuffix() 
	{
		String callnum = "M1090 SERIES 16 LARGE MAP FOLDER 276.5";
		assertEquals("M1090", CallNumUtils.removeNonLCDeweyVolSuffix(callnum, CallNumberType.OTHER));
		assertEquals("M1090", CallNumUtils.removeNonLCDeweySerialVolSuffix(callnum, CallNumberType.OTHER));
	}
	
	/**
	 * test vol lopping when call number is california gov doc
	 */
@Test
	public void testCalifGovDocSuffix() 
	{
		String callnum = "CALIF L1080 .J67 V.1-12:NO.1";
		assertEquals("CALIF L1080 .J67", CallNumUtils.removeNonLCDeweyVolSuffix(callnum, CallNumberType.OTHER));
		assertEquals("CALIF L1080 .J67", CallNumUtils.removeNonLCDeweySerialVolSuffix(callnum, CallNumberType.OTHER));

		// T is serial char; this is addressed in situ with longest-common-prefix approach 
		// for non-LC, non-Dewey call number lopping
//		callnum = "CALIF T900 .J6 V.1-2";
//		assertEquals("CALIF T900 .J6", CallNumUtils.removeNonLCDeweyVolSuffix(callnum, CallNumberType.OTHER));
//		assertEquals("CALIF T900 .J6", CallNumUtils.removeNonLCDeweySerialVolSuffix(callnum, CallNumberType.OTHER));
	}
	
	/**
	 * test vol lopping when Dewey call number has cutter ending in letter(s)
	 */
@Test
	public void testCutterEndsLetDeweySuffix() 
	{
		String callnum = "505 .N285B V.241-245 1973";
		assertEquals("505 .N285B", CallNumUtils.removeDeweyVolSuffix(callnum));
		assertEquals("505 .N285B", CallNumUtils.removeDeweySerialVolSuffix(callnum));
	
		// dewey cutter invalid: starts 2 letters - treated as non-dewey		
		callnum = "888.4 .JF78A V.5";
		assertEquals("888.4 .JF78A", CallNumUtils.removeNonLCDeweyVolSuffix(callnum, CallNumberType.OTHER));
		assertEquals("888.4 .JF78A", CallNumUtils.removeNonLCDeweySerialVolSuffix(callnum, CallNumberType.OTHER));
	}
	
	/**
	 * test vol lopping when LC call number has colon in vol suffix
	 */
@Test
	public void testColonLCSuffix() 
	{
		String callnum = "Q1 .N2 V.434:NO.7031 2005:MAR.17";
		assertEquals("Q1 .N2", CallNumUtils.removeLCVolSuffix(callnum));
		assertEquals("Q1 .N2", CallNumUtils.removeLCSerialVolSuffix(callnum));
	
		callnum = "Q1 .N2 V.421-426 2003:INDEX";
		assertEquals("Q1 .N2", CallNumUtils.removeLCVolSuffix(callnum));
		assertEquals("Q1 .N2", CallNumUtils.removeLCSerialVolSuffix(callnum));
	
		callnum = "Q1 .N2 V.171 1953:JAN.-MAR.";
		assertEquals("Q1 .N2", CallNumUtils.removeLCVolSuffix(callnum));
		assertEquals("Q1 .N2", CallNumUtils.removeLCSerialVolSuffix(callnum));
	
		callnum = "Q1 .S34 V.293:5527-5535 2001:JUL.-AUG";
		assertEquals("Q1 .S34", CallNumUtils.removeLCVolSuffix(callnum));
		assertEquals("Q1 .S34", CallNumUtils.removeLCSerialVolSuffix(callnum));
	}
	
	/**
	 * test vol lopping when Dewey call number has colon in vol suffix 
	 */
@Test
	public void testColonDeweySuffix() 
	{
		String callnum = "505 .N285 V.434:1-680";
		assertEquals("505 .N285", CallNumUtils.removeDeweyVolSuffix(callnum));
		assertEquals("505 .N285", CallNumUtils.removeDeweySerialVolSuffix(callnum));
		
		callnum = "505 .N285 V.458:543--1212";
		assertEquals("505 .N285", CallNumUtils.removeDeweyVolSuffix(callnum));
		assertEquals("505 .N285", CallNumUtils.removeDeweySerialVolSuffix(callnum));
	}

	/**
	 * test that lopping is correct for thesis call numbers
	 */
@Test
	public void testLoppingThesisCallnum()
	{
		boolean isSerial = true;
	
		String callnum = "3781 2009 Z";
		assertEquals(callnum, ItemUtils.getLoppedCallnum(callnum, CallNumberType.THESIS, !isSerial));
	}

}
