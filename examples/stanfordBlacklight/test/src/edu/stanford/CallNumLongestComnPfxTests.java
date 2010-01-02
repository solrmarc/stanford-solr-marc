package edu.stanford;

import java.io.File;

import org.junit.*;

import edu.stanford.enumValues.CallNumberType;

/**
 * junit4 tests for Stanford University 
 *   correctly lop non-LC, non-Dewey call numbers according to longest common 
 *   prefix
 * @author Naomi Dushay
 */
public class CallNumLongestComnPfxTests extends AbstractStanfordBlacklightTest {

	private String fldName = "shelfkey";
	private String fileName = "callNumLongCommPfxTests.mrc";
    private String testFilePath = testDataParentPath + File.separator + fileName;

@Before
	public void setup() 
	{
		mappingTestInit();
	}	

	/**
	 * Pattern of 4 digits, then space, then 4 digits should lop properly
	 */
@Test
	public void test4then4() 
	{
		String id = "4then4";
		String unlop1 = "2451 7513 1954:NO.1-6";
		String unlop2 = "2451 7513 1957:NO.7-1959:NO.6";
		String unlop3 = "2451 7513 1961:NO.1,3-6";
		String unlop4 = "2451 7513 1954-1983 INDEX";
		String lopped = "2451 7513";
		assertExpectedLopping(id, unlop1, unlop2, lopped);
		assertExpectedLopping(id, unlop3, unlop4, lopped);
	}

	/**
	 * Pattern of 4 digits plus decimal, then space, then 4 digits should lop properly
	 */
@Test
	public void test4then4decimal() 
	{
		String id = "4then4decimal";
		String unlop1 = "4488.301 0300 2001 CD-ROM";
		String unlop2 = "4488.301 0300 1961";
		String unlop3 = "4488.301 0300 1950-1960";
		String lopped = "4488.301 0300";
		assertExpectedLopping(id, unlop1, unlop2, lopped);
		assertExpectedLopping(id, unlop1, unlop3, lopped);
	}

	/**
	 * Pattern of 4 digits plus decimal, then space, then 4 digits plus decimal
	 *  should lop properly
	 */
@Test
	public void test4then4decimalBoth() 
	{
		String id = "4then4decimalBoth";
		String unlop1 = "9698.3 4275.25 F V.1";
		String unlop2 = "9698.3 4275.25 F V.2";
// FIMXE:  lopping include F or not??
		String lopped = "9698.3 4275.25 F";
		assertExpectedLopping(id, unlop1, unlop2, lopped);
	}

	/**
	 * Pattern of 4 digits plus decimal, then space, then 4 digits plus decimal
	 *  should lop properly
	 */
@Test
	public void test2then5() 
	{
		String id = "2then5";
		String unlop1 = "71 15446 V.1";
		String unlop2 = "71 15446 V.2";
		String lopped = "71 15446";
		assertExpectedLopping(id, unlop1, unlop2, lopped);
	}

	/**
	 * Japanese in Process call number pattern should lop properly
	 */
@Test
	public void testJapaneseInProcess() 
	{
		String id = "japInProc";
		String unlop1 = "7885324-1001-1";
		String unlop2 = "7885324-1001-2";
		String lopped = "7885324-1001";
		assertExpectedLopping(id, unlop1, unlop2, lopped);
	}

	/**
	 * Deweyish (4 digits in class) call number pattern should lop properly
	 */
@Test
	public void testDeweyish() 
	{
		String id = "deweyish";
		String unlop1 = "8291.209 .A963 V.7 1975/1976";
		String unlop2 = "8291.209 .A963 V.16:NO.4 1994";
		String lopped = "8291.209 .A963";
		assertExpectedLopping(id, unlop1, unlop2, lopped);
	}
	
	/**
	 * map XM call number pattern should lop properly
	 */
@Test
	public void testMapXM() 
	{
		String id = "mapXM";
		String unlop1 = "XM98-1 NO.1";
		String unlop2 = "XM98-1 NO.2";
		String lopped = "XM98-1";
		assertExpectedLopping(id, unlop1, unlop2, lopped);
	}

	/**
	 * yugoslav serial call number pattern should lop properly
	 */
@Test
	public void testYugoSerial() 
	{
		String id = "yugoSerial";
		String unlop1 = "YUGOSLAV SERIAL 1996 V.37";
		String unlop2 = "YUGOSLAV SERIAL 1997-1998 V.38-39";
		String lopped = "YUGOSLAV SERIAL";
		assertExpectedLopping(id, unlop1, unlop2, lopped);
	}

	/**
	 * manuscript collection call number pattern should lop properly
	 */
@Test
	public void testManuscriptCollection() 
	{
		String id = "manuColl";
		String unlop1 = "M1162";
		String unlop2 = "M1162 ACCN 2000-260 BOX 1";
		String unlop3 = "M1162 ACCN 2000-260 BOX 2";
// FIMXE:  lop all to M1162  or lop 2 to M1162 ACCN 2000-260 ?
		String lopped = "M1162";
//		assertExpectedLopping(id, unlop1, unlop2, lopped);
//		String lopped = "M1162 ACCN 2000-260";
		assertExpectedLopping(id, unlop2, unlop3, lopped);
	}


	/**
	 * Sudoc call number pattern should lop properly
	 */
@Test
	public void testSudoc() 
	{
		String id = "sudoc";
		String unlop1 = "C 13.58:";
		String unlop1Shelfkey = CallNumUtils.getShelfKey(unlop1, CallNumberType.SUDOC, id).toLowerCase();
		String unlop2 = "C 13.58:6616";
		String unlop2Shelfkey = CallNumUtils.getShelfKey(unlop2, CallNumberType.SUDOC, id).toLowerCase();
		String unlop3 = "C 13.58:6628";
		String unlop3Shelfkey = CallNumUtils.getShelfKey(unlop3, CallNumberType.SUDOC, id).toLowerCase();
		String lopped = "C 13.58";
		String loppedShelfkey = CallNumUtils.getShelfKey(lopped, CallNumberType.SUDOC, id).toLowerCase();
	
	    solrFldMapTest.assertSolrFldValue(testFilePath, id, fldName, loppedShelfkey);
	    solrFldMapTest.assertSolrFldHasNoValue(testFilePath, id, fldName, unlop1Shelfkey);
	    solrFldMapTest.assertSolrFldHasNoValue(testFilePath, id, fldName, unlop2Shelfkey);
	    solrFldMapTest.assertSolrFldHasNoValue(testFilePath, id, fldName, unlop3Shelfkey);
	}

	/**
	 * MCD call number pattern should lop properly
	 */
@Test
	public void testMCD() 
	{
		String id = "MCD";
		String unlop1 = "MCD 17393 DISC 1";
		String unlop2 = "MCD 17393 DISC 2";
		String lopped = "MCD 17393";
		assertExpectedLopping(id, unlop1, unlop2, lopped);
	}

	/**
	 * MCD call number pattern should lop properly
	 */
	@Test
	public void testMCDdontLop() 
	{
		String id = "MCDdontLop";
		String unlop1 = "MCDI 141 (V.87:2)";
		String unlop2 = "MCDI 142 (V.87:3)";
		String lopped12 = "MCDI";
		String unlop3 = "MCD 10313 (V.91:3)";
		String unlop4 = "MCD 10945 (V.91:5)";
		String lopped34 = "MCD";
		
		String unlop1Shelfkey = CallNumUtils.getShelfKey(unlop1, CallNumberType.OTHER, id).toLowerCase();
		String unlop2Shelfkey = CallNumUtils.getShelfKey(unlop2, CallNumberType.OTHER, id).toLowerCase();
		String unlop3Shelfkey = CallNumUtils.getShelfKey(unlop3, CallNumberType.OTHER, id).toLowerCase();
		String unlop4Shelfkey = CallNumUtils.getShelfKey(unlop4, CallNumberType.OTHER, id).toLowerCase();
		String lopped12Shelfkey = CallNumUtils.getShelfKey(lopped12, CallNumberType.OTHER, id).toLowerCase();
		String lopped34Shelfkey = CallNumUtils.getShelfKey(lopped34, CallNumberType.OTHER, id).toLowerCase();
	
	    solrFldMapTest.assertSolrFldValue(testFilePath, id, fldName, unlop1Shelfkey);
	    solrFldMapTest.assertSolrFldValue(testFilePath, id, fldName, unlop2Shelfkey);
	    solrFldMapTest.assertSolrFldValue(testFilePath, id, fldName, unlop3Shelfkey);
	    solrFldMapTest.assertSolrFldValue(testFilePath, id, fldName, unlop4Shelfkey);
	    solrFldMapTest.assertSolrFldHasNoValue(testFilePath, id, fldName, lopped12Shelfkey);
	    solrFldMapTest.assertSolrFldHasNoValue(testFilePath, id, fldName, lopped34Shelfkey);
	}

	/**
	 * MDVD call number pattern should lop properly
	 */
@Test
	public void testMDVD() 
	{
		String id = "MDVD";
		String unlop1 = "MDVD 703 (V.12)";
		String unlop2 = "MDVD 703 (V.13)";
		String lopped = "MDVD 703";
		assertExpectedLopping(id, unlop1, unlop2, lopped);
	}

	/**
	 * ZDVD call number pattern should lop properly
	 */
@Test
	public void testZDVD() 
	{
		String id = "ZDVD";
		String unlop1 = "ZDVD 20921 DISC 1";
		String unlop2 = "ZDVD 20921 DISC 2";
		String lopped = "ZDVD 20921";
		assertExpectedLopping(id, unlop1, unlop2, lopped);
	}

	/**
	 * Hoover mfilm call number pattern should lop properly
	 */
@Test
	public void testMultCallnumGroups() 
	{
		String id = "mfilmHoover1";
		String unlop1 = "N343 MFILM 1886 JUL-DEC.";
		String unlop2 = "N343 MFILM 1891 JAN-JUN.";
		String lopped = "N343 MFILM";
		assertExpectedLopping(id, unlop1, unlop2, lopped);
		id = "mfilmHoover2";
		unlop1 = "N413 1947:SEP. MFILM";
		unlop2 = "N413 1947:OCT. MFILM";
		lopped = "N413 1947";
		assertExpectedLopping(id, unlop1, unlop2, lopped);
	}
	
	/**
	 * mfilm n.s. call number pattern should lop properly
	 */
@Test
	public void testMfilmNS() 
	{
		String id = "mfilmNS";
		String unlop1 = "MFILM N.S. 56 V.65 1981";
		String unlop2 = "MFILM N.S. 56 V.53 1969";
		String lopped = "MFILM N.S. 56";
		assertExpectedLopping(id, unlop1, unlop2, lopped);
	}
	
	/**
	 * mislabeled LC call number pattern should lop properly
	 */
@Test
	public void testBadLC() 
	{
		String id = "letterY";
		String unlop1 = "Y210 .A3F6 1973";
		String unlop2 = "Y210 .A3F6 1978";
		String lopped = "Y210 .A3F6";
		assertExpectedLopping(id, unlop1, unlop2, lopped);
	}
	
	/**
	 * Lane mislabeled LC call number pattern should not lop, and should not
	 *   be included in shelfkey at all.
	 */
	@Test
	public void testLaneBadLC() 
	{
		String id = "laneBadLC";
		String unlop1 = "Y210 .A3F6 VOL. 1";
		String unlop2 = "Y210 .A3F6 VOL. 2";
		String lopped = "Y210 .A3F6";
		//  no shelfkey for bad LC from Lane or Jackson
	    solrFldMapTest.assertNoSolrFld(testFilePath, id, fldName);
	    
	    //  need to look in item display for lopping;
		fldName = "item_display";
		String sep = " -|- ";
		String barcode = "LL41857";
		String lib = "LANE-MED";
		String loc = "ASK@LANE";
		String volSort = edu.stanford.CallNumUtils.getShelfKey(unlop1, CallNumberType.OTHER, id).toLowerCase();
		String fldVal = barcode + sep + lib + sep + loc + sep + sep + sep +
						lopped + sep + sep + sep + unlop1 + sep + volSort;
	    solrFldMapTest.assertSolrFldValue(testFilePath, id, fldName, fldVal);
	    barcode = "LL41858";
		volSort = edu.stanford.CallNumUtils.getShelfKey(unlop2, CallNumberType.OTHER, id).toLowerCase();
		fldVal = barcode + sep + lib + sep + loc + sep + sep + sep +
					lopped + sep + sep + sep + unlop2 + sep + volSort;
	    solrFldMapTest.assertSolrFldValue(testFilePath, id, fldName, fldVal);
	}
	
	
//----   assert helper methods

	/**
	 * assert that the given record (from the file in the class testFilePath)
	 *  has a properly lopped call number in the shelf key
	 * @param id - the id of the record (from the file in testFilePath)
	 * @param unlop1 - an unlopped call number
	 * @param unlop2 - another unlopped call number
	 * @param lopped - the lopped call number expected.
	 */
	private void assertExpectedLopping(String id, String unlop1, String unlop2, String lopped) 
	{
		String unlop1Shelfkey = CallNumUtils.getShelfKey(unlop1, CallNumberType.OTHER, id).toLowerCase();
		String unlop2Shelfkey = CallNumUtils.getShelfKey(unlop2, CallNumberType.OTHER, id).toLowerCase();
		String loppedShelfkey = CallNumUtils.getShelfKey(lopped, CallNumberType.OTHER, id).toLowerCase();
	
	    solrFldMapTest.assertSolrFldValue(testFilePath, id, fldName, loppedShelfkey);
	    solrFldMapTest.assertSolrFldHasNoValue(testFilePath, id, fldName, unlop1Shelfkey);
	    solrFldMapTest.assertSolrFldHasNoValue(testFilePath, id, fldName, unlop2Shelfkey);
	}
	

}
