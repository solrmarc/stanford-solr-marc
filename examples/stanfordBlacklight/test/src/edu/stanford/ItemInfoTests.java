package edu.stanford;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import javax.xml.parsers.ParserConfigurationException;

import org.junit.Before;
import org.junit.Test;
import org.xml.sax.SAXException;

import edu.stanford.enumValues.CallNumberType;

/**
 * junit4 tests for Stanford University's fields derived from item info in 
 * 999 other than call number (building_facet, access_facet, location, barcode,
 * etc.)
 * @author Naomi Dushay
 */
public class ItemInfoTests extends AbstractStanfordBlacklightTest {
	
@Before
	public final void setup() 
			throws ParserConfigurationException, IOException, SAXException 
	{
		mappingTestInit();
	}	

	/**
	 * Test building facet values.  Skipped building values are in a separate test
	 */
@Test
	public final void testBuildingFacet() 
			throws ParserConfigurationException, IOException, SAXException 
	{
		String fldName = "building_facet";
		createIxInitVars("buildingTests.mrc");
		assertFacetFieldProperties(fldName);
		assertFieldMultiValued(fldName);
		
	    assertSingleResult("229800", fldName, "\"Archive of Recorded Sound\"");
	    assertSingleResult("345228", fldName, "\"Art & Architecture\"");
	    assertSingleResult("460947", fldName, "\"Falconer (Biology)\"");
	    assertSingleResult("804724", fldName, "\"SAL Newark (Off-campus)\"");
	    assertSingleResult("919006", fldName, "\"Swain (Chemistry & Chem. Engineering)\"");
	    assertSingleResult("1147269", fldName, "\"Classics\"");
	    assertSingleResult("1505065", fldName, "\"Branner (Earth Sciences & Maps)\"");
	    assertSingleResult("1618836", fldName, "\"Cubberley (Education)\"");
	    assertSingleResult("1732616", fldName, "\"Math & Computer Science\"");
	    assertSingleResult("1849258", fldName, "Engineering");
	    assertSingleResult("2099904", fldName, "\"Jonsson (Government Documents)\"");
	    assertSingleResult("2678655", fldName, "\"Jackson (Business)\"");
	    assertSingleResult("3027805", fldName, "\"Miller (Hopkins Marine Station)\"");
	    assertSingleResult("3142611", fldName, "Physics");
	    assertSingleResult("4258089", fldName, "\"Special Collections & Archives\"");
	    assertSingleResult("4428936", fldName, "\"Tanner (Philosophy Dept.)\"");
	    assertSingleResult("4823592", fldName, "\"Crown (Law)\"");
	    assertSingleResult("5666387", fldName, "Music");
	    assertSingleResult("6676531", fldName, "\"East Asia\"");
	    assertSingleResult("2797607", fldName, "Meyer");
	
	    // hoover tests are a separate method below
	    
	    Set<String> docIds = new HashSet<String>();
	    docIds.add("1033119");
	    docIds.add("1261173");
	    docIds.add("2557826");
	    docIds.add("3941911");
	    docIds.add("4114632");
	    // checked out
	    docIds.add("575946");
	    // NOT  3277173  (withdrawn)
	    assertSearchResults(fldName, "\"Green (Humanities & Social Sciences)\"", docIds);
	
	    docIds.clear();
	    docIds.add("1033119");
	    docIds.add("1962398");
	    docIds.add("2328381");
	    docIds.add("2913114");
	    assertSearchResults(fldName, "\"Stanford Auxiliary Library (On-campus)\"", docIds);
	
	    docIds.clear();
	    docIds.add("690002");
	    docIds.add("2328381");
	    docIds.add("3941911");
	    docIds.add("7651581");
	    // education - withdrawn;  SAL3 STACKS
	    docIds.add("2214009");
	    assertSearchResults(fldName, "\"SAL3 (Off-campus)\"", docIds);
	
	    docIds.clear();
	    docIds.add("7370014");
	    // ask@lane
	    docIds.add("7233951");
	    assertSearchResults(fldName, "\"Lane (Medical)\"", docIds);
	}

	/**
	 * ensure that there are no building facet values for items that are in
	 *  buildings without translations in the library_map 
	 */
@Test
	public void testSkipBuildingFacet()
			throws ParserConfigurationException, IOException, SAXException 
	{
		String fldName = "building_facet";
		createIxInitVars("buildingTests.mrc");
		
		// APPLIEDPHY (Applied Physics Department is no longer a valid building)
//	    assertSingleResult("115472", fldName, "\"Applied Physics Department\"");  
	    assertZeroResults(fldName, "\"APPLIEDPHY\"");

	    // CPM not a valid building
//	    assertSingleResult("1391080", fldName, "\"GREEN - Current Periodicals & Microtext\""); 
	    assertZeroResults(fldName, "\"CPM\"");

	    // GRN-REF GREEN - Reference - Obsolete
//	    assertSingleResult("2442876", fldName, "\"GREEN - Reference\""); 
	    assertZeroResults(fldName, "\"GRN-REF\"");

	    // ILB Inter-Library Borrowing - Obsolete
//	    assertSingleResult("1111", fldName, "\"Inter-Library Borrowing\""); 
	    assertZeroResults(fldName, "\"ILB\"");
	    
	    // SPEC-DESK   GREEN (Humanities & Social Sciences)   not a valid building
//	    assertSingleResult("2222", fldName, "GREEN (Humanities & Social Sciences)");
	    assertZeroResults(fldName, "SPEC-DESK");

	    // SUL  Stanford University Libraries   not a valid building
//	    assertSingleResult("6493823", fldName, "Stanford University Libraries");
//	    assertSingleResult("7117119", fldName, "Stanford University Libraries");
	    assertZeroResults(fldName, "\"SUL\"");
	}

	/**
	 * ensure that the two hoover library codes have separate values for the 
	 *  building facet
	 */
@Test
	public void testHoover2Locs()
			throws ParserConfigurationException, IOException, SAXException 
	{
		String fldName = "building_facet";
		createIxInitVars("buildingTests.mrc");
		
	    assertSingleResult("3743949", fldName, "\"Hoover Library\"");
	    assertSingleResult("3400092", fldName, "\"Hoover Archives\"");
	}
	

	/**
	 * test if barcode_search field is populated correctly
	 */
@Test
	public final void testBarcodeSearch() 
			throws ParserConfigurationException, IOException, SAXException 
	{
		String fldName = "barcode_search";
		createIxInitVars("locationTests.mrc");
		assertTextFieldProperties(fldName);
		assertFieldOmitsNorms(fldName);
		assertFieldIndexed(fldName);
		assertFieldNotStored(fldName);
		assertFieldMultiValued(fldName);

		// single barcode in the record
		assertSingleResult("115472", fldName, "36105033811451");
		// multiple barcodes in the record
// these are shadowed locations, so they are skipped 2009-12-06
//		assertSingleResult("1033119", fldName, "36105037439663");
//		assertSingleResult("1033119", fldName, "36105001623284");
	}


	String SEP= " -|- ";
	boolean isSerial = true;

	
	/**
	 * test if item_display field is populated correctly, focusing on building/library
	 *  item_display contains:  (separator is " -|- ")
	 *    barcode -|- library(short version) -|- location -|- 
	 *     lopped call number (no volume/part info) -|- 
	 *     shelfkey (from lopped call num) -|- 
	 *     reverse_shelfkey (from lopped call num) -|- 
	 *     full callnum -|- callnum sortable for show view
	 */
 @Test
	public final void testItemDisplayBuildings()
			throws ParserConfigurationException, IOException, SAXException 
	{
		String fldName = "item_display";
		String testFilePath = testDataParentPath + File.separator + "buildingTests.mrc";
		
		// APPLIEDPHY ignored for building facet, but not here
		String id = "115472";
		String callnum = "HC241.25 .I4 D47";
		String shelfkey = edu.stanford.CallNumUtils.getShelfKey(callnum, CallNumberType.LC, id).toLowerCase();
		String reversekey = org.solrmarc.tools.CallNumUtils.getReverseShelfKey(shelfkey).toLowerCase();
		String volSort = edu.stanford.CallNumUtils.getVolumeSortCallnum(callnum, callnum, shelfkey, CallNumberType.LC, !isSerial, id);
		String fldVal = "36105033811451 -|- APPLIEDPHY -|- STACKS" + SEP + SEP + "STKS-MONO" + SEP +
				callnum + SEP + shelfkey + SEP + reversekey + SEP + callnum + SEP + volSort;
	    solrFldMapTest.assertSolrFldValue(testFilePath, id, fldName, fldVal);

		// ARS
		id = "229800";
		callnum = "HG6046 .V28 1986";
		shelfkey = edu.stanford.CallNumUtils.getShelfKey(callnum, CallNumberType.LC, id).toLowerCase();
		reversekey = org.solrmarc.tools.CallNumUtils.getReverseShelfKey(shelfkey).toLowerCase();
		volSort = edu.stanford.CallNumUtils.getVolumeSortCallnum(callnum, callnum, shelfkey, CallNumberType.LC, !isSerial, id);
		fldVal = "36105034181003 -|- ARS -|- STACKS -|- " + SEP + "STKS-MONO" + SEP +
				callnum + SEP + shelfkey + SEP + reversekey + SEP + callnum + SEP + volSort;
	    solrFldMapTest.assertSolrFldValue(testFilePath, id, fldName, fldVal);

	    // ART
		id = "345228";
		callnum = "D764.7 .K72 1990";
		shelfkey = edu.stanford.CallNumUtils.getShelfKey(callnum, CallNumberType.LC, id).toLowerCase();
		reversekey = org.solrmarc.tools.CallNumUtils.getReverseShelfKey(shelfkey).toLowerCase();
		volSort = edu.stanford.CallNumUtils.getVolumeSortCallnum(callnum, callnum, shelfkey, CallNumberType.LC, !isSerial, id);
		fldVal = "36105043140537 -|- ART -|- STACKS -|- " + SEP + "STKS-MONO" + SEP +
				callnum + SEP + shelfkey + SEP + reversekey + SEP + callnum + SEP + volSort;
	    solrFldMapTest.assertSolrFldValue(testFilePath, id, fldName, fldVal);
		
		// BIOLOGY 
		id = "460947";
		callnum = "E184.S75 R47A V.1 1980";
		String lopped = "E184.S75 R47A";
		shelfkey = edu.stanford.CallNumUtils.getShelfKey(lopped, CallNumberType.LC, id).toLowerCase();
		reversekey = org.solrmarc.tools.CallNumUtils.getReverseShelfKey(shelfkey).toLowerCase();
		volSort = edu.stanford.CallNumUtils.getVolumeSortCallnum(callnum, lopped, shelfkey, CallNumberType.LC, isSerial, id);
		fldVal = "36105007402873 -|- BIOLOGY -|- STACKS -|- " + SEP + "STKS-MONO" + SEP +
				lopped + SEP + shelfkey + SEP + reversekey + SEP + callnum + SEP + volSort;
	    solrFldMapTest.assertSolrFldValue(testFilePath, id, fldName, fldVal);
		
		// CHEMCHMENG 
		id = "919006";
		callnum = "PA3998 .H2 O5 1977";
		shelfkey = edu.stanford.CallNumUtils.getShelfKey(callnum, CallNumberType.LC, id).toLowerCase();
		reversekey = org.solrmarc.tools.CallNumUtils.getReverseShelfKey(shelfkey).toLowerCase();
		volSort = edu.stanford.CallNumUtils.getVolumeSortCallnum(callnum, callnum, shelfkey, CallNumberType.LC, !isSerial, id);
		fldVal = "36105036688153 -|- CHEMCHMENG -|- STACKS -|- " + SEP + "STKS-MONO" + SEP +
				callnum + SEP + shelfkey + SEP + reversekey + SEP + callnum + SEP + volSort;
	    solrFldMapTest.assertSolrFldValue(testFilePath, id, fldName, fldVal);

		// CLASSICS
		id = "1147269";
		callnum = "PR9184.6 .M3";
		shelfkey = edu.stanford.CallNumUtils.getShelfKey(callnum, CallNumberType.LC, id).toLowerCase();
		reversekey = org.solrmarc.tools.CallNumUtils.getReverseShelfKey(shelfkey).toLowerCase();
		volSort = edu.stanford.CallNumUtils.getVolumeSortCallnum(callnum, callnum, shelfkey, CallNumberType.LC, !isSerial, id);
		fldVal = "36105037871261 -|- CLASSICS -|- STACKS -|- " + SEP + "STKS-MONO" + SEP +
				callnum + SEP + shelfkey + SEP + reversekey + SEP + callnum + SEP + volSort;
	    solrFldMapTest.assertSolrFldValue(testFilePath, id, fldName, fldVal);

		// CPM 
		id = "1391080";
		callnum = "PQ6653.A646.V5";
		shelfkey = edu.stanford.CallNumUtils.getShelfKey(callnum, CallNumberType.LC, id).toLowerCase();
		reversekey = org.solrmarc.tools.CallNumUtils.getReverseShelfKey(shelfkey).toLowerCase();
		volSort = edu.stanford.CallNumUtils.getVolumeSortCallnum(callnum, callnum, shelfkey, CallNumberType.LC, !isSerial, id);
		fldVal = "36105038701285 -|- CPM -|- STACKS -|- " + SEP + "STKS-MONO" + SEP +
				callnum + SEP + shelfkey + SEP + reversekey + SEP + callnum + SEP + volSort;
	    solrFldMapTest.assertSolrFldValue(testFilePath, id, fldName, fldVal);

		// EARTH-SCI
		id = "1505065";
		callnum = "TD811.5 .G76 1983";
		shelfkey = edu.stanford.CallNumUtils.getShelfKey(callnum, CallNumberType.LC, id).toLowerCase();
		reversekey = org.solrmarc.tools.CallNumUtils.getReverseShelfKey(shelfkey).toLowerCase();
		volSort = edu.stanford.CallNumUtils.getVolumeSortCallnum(callnum, callnum, shelfkey, CallNumberType.LC, !isSerial, id);
		fldVal = "36105039395095 -|- EARTH-SCI -|- STACKS -|- " + SEP + "STKS-MONO" + SEP +
				callnum + SEP + shelfkey + SEP + reversekey + SEP + callnum + SEP + volSort;
	    solrFldMapTest.assertSolrFldValue(testFilePath, id, fldName, fldVal);

		// EAST-ASIA
		id = "6676531";
		callnum = "RD35 .H34 1982";
		shelfkey = edu.stanford.CallNumUtils.getShelfKey(callnum, CallNumberType.LC, id).toLowerCase();
		reversekey = org.solrmarc.tools.CallNumUtils.getReverseShelfKey(shelfkey).toLowerCase();
		volSort = edu.stanford.CallNumUtils.getVolumeSortCallnum(callnum, callnum, shelfkey, CallNumberType.LC, !isSerial, id);
		fldVal = "36105095758004 -|- EAST-ASIA -|- JAPANESE -|- " + SEP + "STKS-MONO" + SEP +
				callnum + SEP + shelfkey + SEP + reversekey + SEP + callnum + SEP + volSort;
	    solrFldMapTest.assertSolrFldValue(testFilePath, id, fldName, fldVal);

		// EDUCATION
		id = "1618836";
		callnum = "PQ6666.E7484 B4 1983";
		shelfkey = edu.stanford.CallNumUtils.getShelfKey(callnum, CallNumberType.LC, id).toLowerCase();
		reversekey = org.solrmarc.tools.CallNumUtils.getReverseShelfKey(shelfkey).toLowerCase();
		volSort = edu.stanford.CallNumUtils.getVolumeSortCallnum(callnum, callnum, shelfkey, CallNumberType.LC, !isSerial, id);
		fldVal = "36105040261765 -|- EDUCATION -|- STACKS -|- " + SEP + "STKS-MONO" + SEP +
				callnum + SEP + shelfkey + SEP + reversekey + SEP + callnum + SEP + volSort;
	    solrFldMapTest.assertSolrFldValue(testFilePath, id, fldName, fldVal);

		// ENG
		id = "1849258";
		callnum = "352.042 .C594 ED.2";
		shelfkey = edu.stanford.CallNumUtils.getShelfKey(callnum, CallNumberType.DEWEY, id).toLowerCase();
		reversekey = org.solrmarc.tools.CallNumUtils.getReverseShelfKey(shelfkey).toLowerCase();
		volSort = edu.stanford.CallNumUtils.getVolumeSortCallnum(callnum, callnum, shelfkey, CallNumberType.DEWEY, !isSerial, id);
		fldVal = "36105047516096 -|- ENG -|- STACKS -|- " + SEP + "STKS-MONO" + SEP +
				callnum + SEP + shelfkey + SEP + reversekey + SEP + callnum + SEP + volSort;
	    solrFldMapTest.assertSolrFldValue(testFilePath, id, fldName, fldVal);

		// GOV-DOCS
		id = "2099904";
		callnum = "DK43 .B63";
		shelfkey = edu.stanford.CallNumUtils.getShelfKey(callnum, CallNumberType.LC, id).toLowerCase();
		reversekey = org.solrmarc.tools.CallNumUtils.getReverseShelfKey(shelfkey).toLowerCase();
		volSort = edu.stanford.CallNumUtils.getVolumeSortCallnum(callnum, callnum, shelfkey, CallNumberType.LC, !isSerial, id);
		fldVal = "36105041442281 -|- GOV-DOCS -|- STACKS -|- " + SEP + "STKS-MONO" + SEP +
				callnum + SEP + shelfkey + SEP + reversekey + SEP + callnum + SEP + volSort;
	    solrFldMapTest.assertSolrFldValue(testFilePath, id, fldName, fldVal);

		// GREEN 
		id = "1261173";
		callnum = "MFILM N.S. 1350 REEL 230 NO. 3741";
		shelfkey = edu.stanford.CallNumUtils.getShelfKey(callnum, CallNumberType.OTHER, id).toLowerCase();
		reversekey = org.solrmarc.tools.CallNumUtils.getReverseShelfKey(shelfkey).toLowerCase();
		volSort = edu.stanford.CallNumUtils.getVolumeSortCallnum(callnum, callnum, shelfkey, CallNumberType.OTHER, !isSerial, id);
		fldVal = "001AFX2969 -|- GREEN -|- MEDIA-MTXT -|- " + SEP + "NH-MICR" + SEP +
					callnum + SEP + shelfkey + SEP + reversekey + SEP + callnum + SEP + volSort;
	    solrFldMapTest.assertSolrFldValue(testFilePath, id, fldName, fldVal);

		// GRN-REF
		id = "2442876";
		callnum = "PQ2678.I26 P54 1992";
		shelfkey = edu.stanford.CallNumUtils.getShelfKey(callnum, CallNumberType.LC, id).toLowerCase();
		reversekey = org.solrmarc.tools.CallNumUtils.getReverseShelfKey(shelfkey).toLowerCase();
		volSort = edu.stanford.CallNumUtils.getVolumeSortCallnum(callnum, callnum, shelfkey, CallNumberType.LC, !isSerial, id);
		fldVal = "36105043436257 -|- GRN-REF -|- STACKS -|- " + SEP + "STKS-MONO" + SEP +
					callnum + SEP + shelfkey + SEP + reversekey + SEP + callnum + SEP + volSort;
	    solrFldMapTest.assertSolrFldValue(testFilePath, id, fldName, fldVal);

		// HOOVER
		id = "3743949";
		callnum = "PQ6613 .A73 G44";
		shelfkey = edu.stanford.CallNumUtils.getShelfKey(callnum, CallNumberType.LC, id).toLowerCase();
		reversekey = org.solrmarc.tools.CallNumUtils.getReverseShelfKey(shelfkey).toLowerCase();
		volSort = edu.stanford.CallNumUtils.getVolumeSortCallnum(callnum, callnum, shelfkey, CallNumberType.LC, !isSerial, id);
		fldVal = "3743949-1001 -|- HOOVER -|- STACKS -|- " + SEP + "STKS" + SEP +
					callnum + SEP + shelfkey + SEP + reversekey + SEP + callnum + SEP + volSort;
	    solrFldMapTest.assertSolrFldValue(testFilePath, id, fldName, fldVal);

		// HOPKINS
		id = "3027805";
		callnum = "DG579 .A5 A5 1995";
		shelfkey = edu.stanford.CallNumUtils.getShelfKey(callnum, CallNumberType.LC, id).toLowerCase();
		reversekey = org.solrmarc.tools.CallNumUtils.getReverseShelfKey(shelfkey).toLowerCase();
		volSort = edu.stanford.CallNumUtils.getVolumeSortCallnum(callnum, callnum, shelfkey, CallNumberType.LC, !isSerial, id);
		fldVal = "36105016935392 -|- HOPKINS -|- STACKS -|- " + SEP + "STKS-MONO" + SEP +
					callnum + SEP + shelfkey + SEP + reversekey + SEP + callnum + SEP + volSort;
	    solrFldMapTest.assertSolrFldValue(testFilePath, id, fldName, fldVal);

		// HV-ARCHIVE
		id = "3400092";
		callnum = "DC34.5 .A78 L4 1996";
		shelfkey = edu.stanford.CallNumUtils.getShelfKey(callnum, CallNumberType.LC, id).toLowerCase();
		reversekey = org.solrmarc.tools.CallNumUtils.getReverseShelfKey(shelfkey).toLowerCase();
		volSort = edu.stanford.CallNumUtils.getVolumeSortCallnum(callnum, callnum, shelfkey, CallNumberType.LC, !isSerial, id);
		fldVal = "36105020376997 -|- HV-ARCHIVE -|- STACKS -|- " + SEP + "STKS-MONO" + SEP +
					callnum + SEP + shelfkey + SEP + reversekey + SEP + callnum + SEP + volSort;
	    solrFldMapTest.assertSolrFldValue(testFilePath, id, fldName, fldVal);

		// ILB
		id = "1111";
		callnum = "Z666 .P31 C6 1946";
		shelfkey = edu.stanford.CallNumUtils.getShelfKey(callnum, CallNumberType.LC, id).toLowerCase();
		reversekey = org.solrmarc.tools.CallNumUtils.getReverseShelfKey(shelfkey).toLowerCase();
		volSort = edu.stanford.CallNumUtils.getVolumeSortCallnum(callnum, callnum, shelfkey, CallNumberType.LC, !isSerial, id);
		fldVal = "36105129694373 -|- ILB -|- STACKS -|- " + SEP + "STKS-MONO" + SEP +
				callnum + SEP + shelfkey + SEP + reversekey + SEP + callnum + SEP + volSort;
	    solrFldMapTest.assertSolrFldValue(testFilePath, id, fldName, fldVal);

		// JACKSON
		id = "2678655";
		callnum = "GA 1.13:RCED-85-88";
		shelfkey = edu.stanford.CallNumUtils.getShelfKey(callnum, CallNumberType.SUDOC, id).toLowerCase();
		reversekey = org.solrmarc.tools.CallNumUtils.getReverseShelfKey(shelfkey).toLowerCase();
		volSort = edu.stanford.CallNumUtils.getVolumeSortCallnum(callnum, callnum, shelfkey, CallNumberType.SUDOC, !isSerial, id);
		fldVal = "001ANE5736 -|- JACKSON -|- STACKS -|- " + SEP + "GOVSTKS" + SEP +
				callnum + SEP + shelfkey + SEP + reversekey + SEP + callnum + SEP + volSort;
	    solrFldMapTest.assertSolrFldValue(testFilePath, id, fldName, fldVal);

		// LANE-MED
		id = "7233951";
		callnum = "X578 .S64 1851";
		shelfkey = edu.stanford.CallNumUtils.getShelfKey(callnum, CallNumberType.OTHER, id).toLowerCase();
		reversekey = org.solrmarc.tools.CallNumUtils.getReverseShelfKey(shelfkey).toLowerCase();
		volSort = edu.stanford.CallNumUtils.getVolumeSortCallnum(callnum, callnum, shelfkey, CallNumberType.OTHER, !isSerial, id);
		fldVal = "LL124341 -|- LANE-MED -|- ASK@LANE -|- " + SEP + "MEDICAL" + SEP +
				callnum + SEP + SEP + SEP + callnum + SEP + volSort;
	    solrFldMapTest.assertSolrFldValue(testFilePath, id, fldName, fldVal);

		// LAW
		id = "4823592";
		callnum = "Y 4.G 74/7:G 21/10";
		shelfkey = edu.stanford.CallNumUtils.getShelfKey(callnum, CallNumberType.SUDOC, id).toLowerCase();
		reversekey = org.solrmarc.tools.CallNumUtils.getReverseShelfKey(shelfkey).toLowerCase();
		volSort = edu.stanford.CallNumUtils.getVolumeSortCallnum(callnum, callnum, shelfkey, CallNumberType.SUDOC, !isSerial, id);
		fldVal = "36105063104488 -|- LAW -|- BASEMENT -|- " + SEP + "LAW-STKS" + SEP +
				callnum + SEP + shelfkey + SEP + reversekey + SEP + callnum + SEP + volSort;
	    solrFldMapTest.assertSolrFldValue(testFilePath, id, fldName, fldVal);

		// MATH-CS
		id = "1732616";
		callnum = "QA273 .C83 1962";
		shelfkey = edu.stanford.CallNumUtils.getShelfKey(callnum, CallNumberType.LC, id).toLowerCase();
		reversekey = org.solrmarc.tools.CallNumUtils.getReverseShelfKey(shelfkey).toLowerCase();
		volSort = edu.stanford.CallNumUtils.getVolumeSortCallnum(callnum, callnum, shelfkey, CallNumberType.LC, !isSerial, id);
		fldVal = "36105033142303 -|- MATH-CS -|- STACKS -|- " + SEP + "STKS" + SEP +
				callnum + SEP + shelfkey + SEP + reversekey + SEP + callnum + SEP + volSort;
	    solrFldMapTest.assertSolrFldValue(testFilePath, id, fldName, fldVal);

		// MEYER
		id = "2797607";
		callnum = "B781 .A33 I55 1993";
		shelfkey = edu.stanford.CallNumUtils.getShelfKey(callnum, CallNumberType.LC, id).toLowerCase();
		reversekey = org.solrmarc.tools.CallNumUtils.getReverseShelfKey(shelfkey).toLowerCase();
		volSort = edu.stanford.CallNumUtils.getVolumeSortCallnum(callnum, callnum, shelfkey, CallNumberType.LC, !isSerial, id);
		fldVal = "36105004381195 -|- MEYER -|- STACKS -|- " + SEP + "STKS-MONO" + SEP +
				callnum + SEP + shelfkey + SEP + reversekey + SEP + callnum + SEP + volSort;
	    solrFldMapTest.assertSolrFldValue(testFilePath, id, fldName, fldVal);

		// MUSIC
		id = "5666387";
		callnum = "ML410 .S54 I58 2000";
		shelfkey = edu.stanford.CallNumUtils.getShelfKey(callnum, CallNumberType.LC, id).toLowerCase();
		reversekey = org.solrmarc.tools.CallNumUtils.getReverseShelfKey(shelfkey).toLowerCase();
		volSort = edu.stanford.CallNumUtils.getVolumeSortCallnum(callnum, callnum, shelfkey, CallNumberType.LC, !isSerial, id);
		fldVal = "36105114964369 -|- MUSIC -|- STACKS -|- " + SEP + "STKS" + SEP +
				callnum + SEP + shelfkey + SEP + reversekey + SEP + callnum + SEP + volSort;
	    solrFldMapTest.assertSolrFldValue(testFilePath, id, fldName, fldVal);

		// PHYSICS
		id = "3142611";
		callnum = "PS3553 .L337 F76 1978";
		shelfkey = edu.stanford.CallNumUtils.getShelfKey(callnum, CallNumberType.LC, id).toLowerCase();
		reversekey = org.solrmarc.tools.CallNumUtils.getReverseShelfKey(shelfkey).toLowerCase();
		volSort = edu.stanford.CallNumUtils.getVolumeSortCallnum(callnum, callnum, shelfkey, CallNumberType.LC, !isSerial, id);
		fldVal = "36105017175519 -|- PHYSICS -|- STACKS -|- " + SEP + "STKS-MONO" + SEP +
				callnum + SEP + shelfkey + SEP + reversekey + SEP + callnum + SEP + volSort;
	    solrFldMapTest.assertSolrFldValue(testFilePath, id, fldName, fldVal);

		// SAL
		id = "2913114";
		callnum = "DS135 .P6 I65";
		shelfkey = edu.stanford.CallNumUtils.getShelfKey(callnum, CallNumberType.LC, id).toLowerCase();
		reversekey = org.solrmarc.tools.CallNumUtils.getReverseShelfKey(shelfkey).toLowerCase();
		volSort = edu.stanford.CallNumUtils.getVolumeSortCallnum(callnum, callnum, shelfkey, CallNumberType.LC, !isSerial, id);
		fldVal = "36105082973251 -|- SAL -|- SAL-PAGE -|- " + SEP + "STKS-MONO" + SEP +
				callnum + SEP + shelfkey + SEP + reversekey + SEP + callnum + SEP + volSort;
	    solrFldMapTest.assertSolrFldValue(testFilePath, id, fldName, fldVal);

		// SAL3
		id = "690002";
		callnum = "159.32 .W211";
		shelfkey = edu.stanford.CallNumUtils.getShelfKey(callnum, CallNumberType.DEWEY, id).toLowerCase();
		reversekey = org.solrmarc.tools.CallNumUtils.getReverseShelfKey(shelfkey).toLowerCase();
		volSort = edu.stanford.CallNumUtils.getVolumeSortCallnum(callnum, callnum, shelfkey, CallNumberType.DEWEY, !isSerial, id);
		fldVal = "36105046693508 -|- SAL3 -|- STACKS -|- " + SEP + "STKS-MONO" + SEP +
				callnum + SEP + shelfkey + SEP + reversekey + SEP + callnum + SEP + volSort;
	    solrFldMapTest.assertSolrFldValue(testFilePath, id, fldName, fldVal);

		// SAL-NEWARK
		id = "804724";
		callnum = "Z7164.T23.W45";
		shelfkey = edu.stanford.CallNumUtils.getShelfKey(callnum, CallNumberType.LC, id).toLowerCase();
		reversekey = org.solrmarc.tools.CallNumUtils.getReverseShelfKey(shelfkey).toLowerCase();
		volSort = edu.stanford.CallNumUtils.getVolumeSortCallnum(callnum, callnum, shelfkey, CallNumberType.LC, !isSerial, id);
		fldVal = "36105035887392 -|- SAL-NEWARK -|- STACKS -|- " + SEP + "STKS-MONO" + SEP +
				callnum + SEP + shelfkey + SEP + reversekey + SEP + callnum + SEP + volSort;
	    solrFldMapTest.assertSolrFldValue(testFilePath, id, fldName, fldVal);

		// SPEC-COLL
		id = "4258089";
		callnum = "NAS 1.26:205100";
		shelfkey = edu.stanford.CallNumUtils.getShelfKey(callnum, CallNumberType.SUDOC, id).toLowerCase();
		reversekey = org.solrmarc.tools.CallNumUtils.getReverseShelfKey(shelfkey).toLowerCase();
		volSort = edu.stanford.CallNumUtils.getVolumeSortCallnum(callnum, callnum, shelfkey, CallNumberType.SUDOC, !isSerial, id);
		fldVal = "4258089-1001 -|- SPEC-COLL -|- STACKS -|- " + SEP + "GOVSTKS" + SEP +
				callnum + SEP + shelfkey + SEP + reversekey + SEP + callnum + SEP + volSort;
	    solrFldMapTest.assertSolrFldValue(testFilePath, id, fldName, fldVal);

		// SPEC-DESK
		id = "2222";
		callnum = "S666 .P31 C6 1946";
		shelfkey = edu.stanford.CallNumUtils.getShelfKey(callnum, CallNumberType.LC, id).toLowerCase();
		reversekey = org.solrmarc.tools.CallNumUtils.getReverseShelfKey(shelfkey).toLowerCase();
		volSort = edu.stanford.CallNumUtils.getVolumeSortCallnum(callnum, callnum, shelfkey, CallNumberType.LC, !isSerial, id);
		fldVal = "36105129694373 -|- SPEC-DESK -|- STACKS -|- " + SEP + "STKS-MONO" + SEP +
				callnum + SEP + shelfkey + SEP + reversekey + SEP + callnum + SEP + volSort;
	    solrFldMapTest.assertSolrFldValue(testFilePath, id, fldName, fldVal);

		// TANNER
		id = "4428936";
		callnum = "PN1993.5 .I88 C5618 2000";
		shelfkey = edu.stanford.CallNumUtils.getShelfKey(callnum, CallNumberType.LC, id).toLowerCase();
		reversekey = org.solrmarc.tools.CallNumUtils.getReverseShelfKey(shelfkey).toLowerCase();
		volSort = edu.stanford.CallNumUtils.getVolumeSortCallnum(callnum, callnum, shelfkey, CallNumberType.LC, !isSerial, id);
		fldVal = "36105021909747 -|- TANNER -|- STACKS -|- " + SEP + "STKS-MONO" + SEP +
				callnum + SEP + shelfkey + SEP + reversekey + SEP + callnum + SEP + volSort;
	    solrFldMapTest.assertSolrFldValue(testFilePath, id, fldName, fldVal);

		// SUL
		id = "6493823";
		callnum = "F1356 .M464 2005";
		shelfkey = edu.stanford.CallNumUtils.getShelfKey(callnum, CallNumberType.LC, id).toLowerCase();
		reversekey = org.solrmarc.tools.CallNumUtils.getReverseShelfKey(shelfkey).toLowerCase();
		volSort = edu.stanford.CallNumUtils.getVolumeSortCallnum(callnum, callnum, shelfkey, CallNumberType.LC, !isSerial, id);
		fldVal = "36105122224160 -|- SUL -|- STACKS -|- " + SEP + "STKS-MONO" + SEP +
				callnum + SEP + shelfkey + SEP + reversekey + SEP + callnum + SEP + volSort;
	    solrFldMapTest.assertSolrFldValue(testFilePath, id, fldName, fldVal);

		// multiple items in single record, diff buildings
		id = "1033119";
		callnum = "BX4659.E85 W44";
		shelfkey = edu.stanford.CallNumUtils.getShelfKey(callnum, CallNumberType.LC, id).toLowerCase();
		reversekey = org.solrmarc.tools.CallNumUtils.getReverseShelfKey(shelfkey).toLowerCase();
		volSort = edu.stanford.CallNumUtils.getVolumeSortCallnum(callnum, callnum, shelfkey, CallNumberType.LC, !isSerial, id);
		fldVal = "36105037439663 -|- GREEN -|- STACKS -|- " + SEP + "STKS-MONO" + SEP +
				callnum + SEP + shelfkey + SEP + reversekey + SEP + callnum + SEP + volSort;
	    solrFldMapTest.assertSolrFldValue(testFilePath, id, fldName, fldVal);

		callnum = "BX4659 .E85 W44 1982";
		shelfkey = edu.stanford.CallNumUtils.getShelfKey(callnum, CallNumberType.LC, id).toLowerCase();
		reversekey = org.solrmarc.tools.CallNumUtils.getReverseShelfKey(shelfkey).toLowerCase();
		volSort = edu.stanford.CallNumUtils.getVolumeSortCallnum(callnum, callnum, shelfkey, CallNumberType.LC, !isSerial, id);
		fldVal = "36105001623284 -|- SAL -|- STACKS -|- " + SEP + "STKS-MONO" + SEP +
				callnum + SEP + shelfkey + SEP + reversekey + SEP + callnum + SEP + volSort;
	    solrFldMapTest.assertSolrFldValue(testFilePath, id, fldName, fldVal);
		
		//   same build, same loc, same callnum, one in another building
		id = "2328381";
		callnum = "PR3724.T3";
		shelfkey = edu.stanford.CallNumUtils.getShelfKey(callnum, CallNumberType.LC, id).toLowerCase();
		reversekey = org.solrmarc.tools.CallNumUtils.getReverseShelfKey(shelfkey).toLowerCase();
		volSort = edu.stanford.CallNumUtils.getVolumeSortCallnum(callnum, callnum, shelfkey, CallNumberType.LC, !isSerial, id);
		fldVal = "36105003934432 -|- SAL -|- STACKS -|- " + SEP + "STKS-MONO" + SEP +
				callnum + SEP + shelfkey + SEP + reversekey + SEP + callnum + SEP + volSort;
	    solrFldMapTest.assertSolrFldValue(testFilePath, id, fldName, fldVal);
		fldVal = "36105003934424 -|- SAL -|- STACKS -|- " + SEP + "STKS-MONO" + SEP +
				callnum + SEP + shelfkey + SEP + reversekey + SEP + callnum + SEP + volSort;
	    solrFldMapTest.assertSolrFldValue(testFilePath, id, fldName, fldVal);

		callnum = "827.5 .S97TG";
		shelfkey = edu.stanford.CallNumUtils.getShelfKey(callnum, CallNumberType.DEWEY, id).toLowerCase();
		reversekey = org.solrmarc.tools.CallNumUtils.getReverseShelfKey(shelfkey).toLowerCase();
		volSort = edu.stanford.CallNumUtils.getVolumeSortCallnum(callnum, callnum, shelfkey, CallNumberType.DEWEY, !isSerial, id);
		fldVal = "36105048104132 -|- SAL3 -|- STACKS -|- " + SEP + "STKS-MONO" + SEP +
				callnum + SEP + shelfkey + SEP + reversekey + SEP + callnum + SEP + volSort;
	    solrFldMapTest.assertSolrFldValue(testFilePath, id, fldName, fldVal);


		testFilePath = testDataParentPath + File.separator + "itemDisplayTests.mrc";
		
		// Lane example with actual values
		id = "6661112";
		callnum = "Z3871.Z8 V.22 1945";
		shelfkey = edu.stanford.CallNumUtils.getShelfKey(callnum, CallNumberType.LC, id).toLowerCase();;
		reversekey = org.solrmarc.tools.CallNumUtils.getReverseShelfKey(shelfkey).toLowerCase();
		volSort = edu.stanford.CallNumUtils.getVolumeSortCallnum(callnum, callnum, shelfkey, CallNumberType.LC, !isSerial, id);
		fldVal = "36105082101390 -|- LANE-MED -|- STACKS -|- " + SEP + "STKS-MONO" + SEP +
				callnum + SEP + shelfkey + SEP + reversekey + SEP + callnum + SEP + volSort;
	    solrFldMapTest.assertSolrFldValue(testFilePath, id, fldName, fldVal);
		
		// mult items same build, diff loc
		id = "2328381";
		callnum = "PR3724.T3";
		shelfkey = edu.stanford.CallNumUtils.getShelfKey(callnum, CallNumberType.LC, id).toLowerCase();
		reversekey = org.solrmarc.tools.CallNumUtils.getReverseShelfKey(shelfkey).toLowerCase();
		volSort = edu.stanford.CallNumUtils.getVolumeSortCallnum(callnum, callnum, shelfkey, CallNumberType.LC, !isSerial, id);
		fldVal = "36105003934432 -|- GREEN -|- STACKS -|- " + SEP + "STKS-MONO" + SEP +
				callnum + SEP + shelfkey + SEP + reversekey + SEP + callnum + SEP + volSort;
	    solrFldMapTest.assertSolrFldValue(testFilePath, id, fldName, fldVal);

		callnum = "PR3724.T3 A2";
		shelfkey = edu.stanford.CallNumUtils.getShelfKey(callnum, CallNumberType.LC, id).toLowerCase();
		reversekey = org.solrmarc.tools.CallNumUtils.getReverseShelfKey(shelfkey).toLowerCase();
		volSort = edu.stanford.CallNumUtils.getVolumeSortCallnum(callnum, callnum, shelfkey, CallNumberType.LC, !isSerial, id);
		fldVal = "36105003934424 -|- GREEN -|- BINDERY -|- " + SEP + "STKS-MONO" + SEP +
				callnum + SEP + shelfkey + SEP + reversekey + SEP + callnum + SEP + volSort;
	    solrFldMapTest.assertSolrFldValue(testFilePath, id, fldName, fldVal);

		callnum = "827.5 .S97TG";
		shelfkey = edu.stanford.CallNumUtils.getShelfKey(callnum, CallNumberType.DEWEY, id).toLowerCase();
		reversekey = org.solrmarc.tools.CallNumUtils.getReverseShelfKey(shelfkey).toLowerCase();
		volSort = edu.stanford.CallNumUtils.getVolumeSortCallnum(callnum, callnum, shelfkey, CallNumberType.DEWEY, !isSerial, id);
		fldVal = "36105048104132 -|- GRN-REF -|- STACKS -|- " + SEP + "STKS-MONO" + SEP +
				callnum + SEP + shelfkey + SEP + reversekey + SEP + callnum + SEP + volSort;
	    solrFldMapTest.assertSolrFldValue(testFilePath, id, fldName, fldVal);
	}


	/**
	 * test if item_display field is populated correctly, focusing on locations
	 *  item_display contains:  (separator is " -|- ")
	 *    barcode -|- library(short version) -|- location -|- 
	 *     lopped call number (no volume/part info) -|- 
	 *     shelfkey (from lopped call num) -|- 
	 *     reverse_shelfkey (from lopped call num) -|- 
	 *     full callnum -|- callnum sortable for show view
	 */
 @Test
	public final void testItemDisplayLocations()
			throws ParserConfigurationException, IOException, SAXException 
	{
		String fldName = "item_display";
		String testFilePath = testDataParentPath + File.separator + "buildingTests.mrc";
		
		// STACKS
		String id = "229800";
		String callnum = "HG6046 .V28 1986";
		String shelfkey = edu.stanford.CallNumUtils.getShelfKey(callnum, CallNumberType.LC, id).toLowerCase();
		String reversekey = org.solrmarc.tools.CallNumUtils.getReverseShelfKey(shelfkey).toLowerCase();
		String volSort = edu.stanford.CallNumUtils.getVolumeSortCallnum(callnum, callnum, shelfkey, CallNumberType.LC, !isSerial, id);
		String fldVal = "36105034181003 -|- ARS -|- STACKS" + SEP + SEP + "STKS-MONO" + SEP +
				callnum + SEP + shelfkey + SEP + reversekey + SEP + callnum + SEP + volSort;
	    solrFldMapTest.assertSolrFldValue(testFilePath, id, fldName, fldVal);

	    id = "3941911";
		callnum = "PS3557 .O5829 K3 1998";
		shelfkey = edu.stanford.CallNumUtils.getShelfKey(callnum, CallNumberType.LC, id).toLowerCase();
		reversekey = org.solrmarc.tools.CallNumUtils.getReverseShelfKey(shelfkey).toLowerCase();
		volSort = edu.stanford.CallNumUtils.getVolumeSortCallnum(callnum, callnum, shelfkey, CallNumberType.LC, !isSerial, id);
		fldVal = "36105019748495 -|- SAL3 -|- STACKS" + SEP + SEP + "STKS-MONO" +  SEP +
				callnum + SEP + shelfkey + SEP + reversekey + SEP + callnum + SEP + volSort;
	    solrFldMapTest.assertSolrFldValue(testFilePath, id, fldName, fldVal);
	    fldVal = "36105025373064 -|- GREEN -|- BENDER" + SEP + SEP + "NONCIRC" + SEP +
				callnum + SEP + shelfkey + SEP + reversekey + SEP + callnum + SEP + volSort;
	    solrFldMapTest.assertSolrFldValue(testFilePath, id, fldName, fldVal);
		
		id = "6676531";
		callnum = "RD35 .H34 1982";
		shelfkey = edu.stanford.CallNumUtils.getShelfKey(callnum, CallNumberType.LC, id).toLowerCase();
		reversekey = org.solrmarc.tools.CallNumUtils.getReverseShelfKey(shelfkey).toLowerCase();
		volSort = edu.stanford.CallNumUtils.getVolumeSortCallnum(callnum, callnum, shelfkey, CallNumberType.LC, !isSerial, id);
		fldVal = "36105095758004 -|- EAST-ASIA -|- JAPANESE -|- " +  SEP + "STKS-MONO" + SEP +
			callnum + SEP + shelfkey + SEP + reversekey + SEP + callnum + SEP + volSort;
	    solrFldMapTest.assertSolrFldValue(testFilePath, id, fldName, fldVal);

		id = "1261173";
		callnum = "MFILM N.S. 1350 REEL 230 NO. 3741";
		String lopped = CallNumUtils.removeNonLCDeweyVolSuffix(callnum, CallNumberType.OTHER);
		shelfkey = edu.stanford.CallNumUtils.getShelfKey(lopped, CallNumberType.OTHER, id).toLowerCase();
		reversekey = org.solrmarc.tools.CallNumUtils.getReverseShelfKey(shelfkey).toLowerCase();
		volSort = edu.stanford.CallNumUtils.getVolumeSortCallnum(callnum, lopped, shelfkey, CallNumberType.OTHER, !isSerial, id);
		fldVal = "001AFX2969 -|- GREEN -|- MEDIA-MTXT -|- " + SEP + "NH-MICR" + SEP +
				lopped + SEP + shelfkey + SEP + reversekey + SEP + callnum + SEP + volSort;
	    solrFldMapTest.assertSolrFldValue(testFilePath, id, fldName, fldVal);

		// inquire
		id = "7233951";
		callnum = "X578 .S64 1851";
		shelfkey = edu.stanford.CallNumUtils.getShelfKey(callnum, CallNumberType.OTHER, id).toLowerCase();
		reversekey = org.solrmarc.tools.CallNumUtils.getReverseShelfKey(shelfkey).toLowerCase();
		volSort = edu.stanford.CallNumUtils.getVolumeSortCallnum(callnum, callnum, shelfkey, CallNumberType.OTHER, !isSerial, id);
		fldVal = "LL124341 -|- LANE-MED -|- ASK@LANE -|- " + SEP + "MEDICAL" + SEP +
			callnum + SEP + SEP + SEP + callnum + SEP + volSort;
	    solrFldMapTest.assertSolrFldValue(testFilePath, id, fldName, fldVal);

		// in transit
		id = "1962398";
		callnum = "Z3871.Z8";
		shelfkey = edu.stanford.CallNumUtils.getShelfKey(callnum, CallNumberType.LC, id).toLowerCase();
		reversekey = org.solrmarc.tools.CallNumUtils.getReverseShelfKey(shelfkey).toLowerCase();
		volSort = edu.stanford.CallNumUtils.getVolumeSortCallnum(callnum, callnum, shelfkey, CallNumberType.LC, !isSerial, id);
		fldVal = "36105082101390 -|- SAL -|- SAL-PAGE -|- " + SEP + "STKS-MONO" + SEP +
				callnum + SEP + shelfkey + SEP + reversekey + SEP + callnum + SEP + volSort;
	    solrFldMapTest.assertSolrFldValue(testFilePath, id, fldName, fldVal);
	    
		id = "2913114";
		callnum = "DS135 .P6 I65";
		shelfkey = edu.stanford.CallNumUtils.getShelfKey(callnum, CallNumberType.LC, id).toLowerCase();
		reversekey = org.solrmarc.tools.CallNumUtils.getReverseShelfKey(shelfkey).toLowerCase();
		volSort = edu.stanford.CallNumUtils.getVolumeSortCallnum(callnum, callnum, shelfkey, CallNumberType.LC, !isSerial, id);
		fldVal = "36105082973251 -|- SAL -|- SAL-PAGE -|- " + SEP + "STKS-MONO" + SEP +
				callnum + SEP + shelfkey + SEP + reversekey + SEP + callnum + SEP + volSort;
	    solrFldMapTest.assertSolrFldValue(testFilePath, id, fldName, fldVal);

		// in process
		id = "7651581";
		callnum = "PQ9661 .P31 C6 1946";
		shelfkey = edu.stanford.CallNumUtils.getShelfKey(callnum, CallNumberType.LC, id).toLowerCase();
		reversekey = org.solrmarc.tools.CallNumUtils.getReverseShelfKey(shelfkey).toLowerCase();
		volSort = edu.stanford.CallNumUtils.getVolumeSortCallnum(callnum, callnum, shelfkey, CallNumberType.LC, !isSerial, id);
		fldVal = "36105129694373 -|- SAL3 -|- INPROCESS -|- " + SEP + "STKS-MONO" + SEP +
				callnum + SEP + shelfkey + SEP + reversekey + SEP + callnum + SEP + volSort;
	    solrFldMapTest.assertSolrFldValue(testFilePath, id, fldName, fldVal);
		
		// gov docs
		id = "2557826";
		callnum = "E 1.28:COO-4274-1";
		shelfkey = edu.stanford.CallNumUtils.getShelfKey(callnum, CallNumberType.SUDOC, id).toLowerCase();
		reversekey = org.solrmarc.tools.CallNumUtils.getReverseShelfKey(shelfkey).toLowerCase();
		volSort = edu.stanford.CallNumUtils.getVolumeSortCallnum(callnum, callnum, shelfkey, CallNumberType.SUDOC, !isSerial, id);
		fldVal = "001AMR5851 -|- GREEN -|- FED-DOCS -|- " + SEP + "GOVSTKS" + SEP +
				callnum + SEP + shelfkey + SEP + reversekey + SEP + callnum + SEP + volSort;
	    solrFldMapTest.assertSolrFldValue(testFilePath, id, fldName, fldVal);
	    
		id = "4114632";
		callnum = "ITC 1.15/3:";
		shelfkey = edu.stanford.CallNumUtils.getShelfKey(callnum, CallNumberType.SUDOC, id).toLowerCase();
		reversekey = org.solrmarc.tools.CallNumUtils.getReverseShelfKey(shelfkey).toLowerCase();
		volSort = edu.stanford.CallNumUtils.getVolumeSortCallnum(callnum, lopped, shelfkey, CallNumberType.SUDOC, !isSerial, id);
		fldVal = "4114632-1001 -|- GREEN -|- FED-DOCS -|- " + SEP + "GOVSTKS" + SEP +
				callnum + SEP + shelfkey + SEP + reversekey + SEP + callnum + SEP + volSort;
	    solrFldMapTest.assertSolrFldValue(testFilePath, id, fldName, fldVal);

		// one withdrawn location, one valid 
		id = "2214009";
		callnum = "370.1 .S655";
		shelfkey = edu.stanford.CallNumUtils.getShelfKey(callnum, CallNumberType.DEWEY, id).toLowerCase();
		reversekey = org.solrmarc.tools.CallNumUtils.getReverseShelfKey(shelfkey).toLowerCase();
		volSort = edu.stanford.CallNumUtils.getVolumeSortCallnum(callnum, callnum, shelfkey, CallNumberType.DEWEY, !isSerial, id);
		fldVal = "36105033336798 -|- EDUCATION -|- WITHDRAWN -|- " + SEP + SEP +
				callnum + SEP + shelfkey + SEP + reversekey + SEP + callnum + SEP + volSort;
	    solrFldMapTest.assertSolrFldHasNoValue(testFilePath, id, fldName, fldVal);
	    fldVal = "36105033336780 -|- SAL3 -|- STACKS -|- " + SEP + "STKS-MONO" + SEP +
				callnum + SEP + shelfkey + SEP + reversekey + SEP + callnum + SEP + volSort;
	    solrFldMapTest.assertSolrFldValue(testFilePath, id, fldName, fldVal);

		// online locations:  ELECTR-LOC  INTERNET  ONLINE-TXT RESV-URL
//		assertDocHasNoField("7117119", fldName);  
// TODO:  will have item_display field for online items when callnum is included in facets, nearby-on-shelf
		fldVal = "7117119-1001 -|- Online -|- Online -|- " + SEP + SEP +
				"" + SEP + "" + SEP + "" + SEP + "" + SEP + "";
	    solrFldMapTest.assertSolrFldHasNoValue(testFilePath, id, fldName, fldVal);
		

		testFilePath = testDataParentPath + File.separator + "itemDisplayTests.mrc";
		
		// on order locations
		id = "460947";
		callnum = "E184.S75 R47A V.1 1980";
		lopped = CallNumUtils.removeLCVolSuffix(callnum);
		shelfkey = edu.stanford.CallNumUtils.getShelfKey(lopped, CallNumberType.LC, id).toLowerCase();
		reversekey = org.solrmarc.tools.CallNumUtils.getReverseShelfKey(shelfkey).toLowerCase();
		volSort = edu.stanford.CallNumUtils.getVolumeSortCallnum(callnum, lopped, shelfkey, CallNumberType.LC, isSerial, id);
		fldVal = "36105007402873 -|- GREEN -|- ON-ORDER -|- " + SEP + "STKS-MONO" + SEP +
				lopped + SEP + shelfkey + SEP + reversekey + SEP + callnum + SEP + volSort;
	    solrFldMapTest.assertSolrFldValue(testFilePath, id, fldName, fldVal);
		// reserve locations
		id = "690002";
		callnum = "159.32 .W211";
		shelfkey = edu.stanford.CallNumUtils.getShelfKey(callnum, CallNumberType.DEWEY, id).toLowerCase();
		reversekey = org.solrmarc.tools.CallNumUtils.getReverseShelfKey(shelfkey).toLowerCase();
		volSort = edu.stanford.CallNumUtils.getVolumeSortCallnum(callnum, callnum, shelfkey, CallNumberType.DEWEY, !isSerial, id);
		fldVal = "36105046693508 -|- EARTH-SCI -|- BRAN-RESV -|- " + SEP + "STKS-MONO" + SEP +
				callnum + SEP + shelfkey + SEP + reversekey + SEP + callnum + SEP + volSort;
	    solrFldMapTest.assertSolrFldValue(testFilePath, id, fldName, fldVal);
		
		
		// mult items same build, diff loc
		id = "2328381";
		callnum = "PR3724.T3";
		shelfkey = edu.stanford.CallNumUtils.getShelfKey(callnum, CallNumberType.LC, id).toLowerCase();
		reversekey = org.solrmarc.tools.CallNumUtils.getReverseShelfKey(shelfkey).toLowerCase();
		volSort = edu.stanford.CallNumUtils.getVolumeSortCallnum(callnum, callnum, shelfkey, CallNumberType.LC, !isSerial, id);
		fldVal = "36105003934432 -|- GREEN -|- STACKS -|- " + SEP + "STKS-MONO" + SEP +
				callnum + SEP + shelfkey + SEP + reversekey + SEP + callnum + SEP + volSort;
	    solrFldMapTest.assertSolrFldValue(testFilePath, id, fldName, fldVal);
		callnum = "PR3724.T3 A2";
		shelfkey = edu.stanford.CallNumUtils.getShelfKey(callnum, CallNumberType.LC, id).toLowerCase();
		reversekey = org.solrmarc.tools.CallNumUtils.getReverseShelfKey(shelfkey).toLowerCase();
		volSort = edu.stanford.CallNumUtils.getVolumeSortCallnum(callnum, callnum, shelfkey, CallNumberType.LC, !isSerial, id);
		fldVal = "36105003934424 -|- GREEN -|- BINDERY -|- " + SEP + "STKS-MONO" + SEP +
				callnum + SEP + shelfkey + SEP + reversekey + SEP + callnum + SEP + volSort;
	    solrFldMapTest.assertSolrFldValue(testFilePath, id, fldName, fldVal);
		callnum = "827.5 .S97TG";
		shelfkey = edu.stanford.CallNumUtils.getShelfKey(callnum, CallNumberType.DEWEY, id).toLowerCase();
		reversekey = org.solrmarc.tools.CallNumUtils.getReverseShelfKey(shelfkey).toLowerCase();
		volSort = edu.stanford.CallNumUtils.getVolumeSortCallnum(callnum, callnum, shelfkey, CallNumberType.DEWEY, !isSerial, id);
		fldVal = "36105048104132 -|- GRN-REF -|- STACKS -|- " + SEP + "STKS-MONO" + SEP +
				callnum + SEP + shelfkey + SEP + reversekey + SEP + callnum + SEP + volSort;
	    solrFldMapTest.assertSolrFldValue(testFilePath, id, fldName, fldVal);

		
		// multiple items for single bib with same library / location, diff callnum
		id = "666";
		callnum = "PR3724.T3";
		shelfkey = edu.stanford.CallNumUtils.getShelfKey(callnum, CallNumberType.LC, id).toLowerCase();
		reversekey = org.solrmarc.tools.CallNumUtils.getReverseShelfKey(shelfkey).toLowerCase();
		volSort = edu.stanford.CallNumUtils.getVolumeSortCallnum(callnum, callnum, shelfkey, CallNumberType.LC, !isSerial, id);
		fldVal = "36105003934432 -|- GREEN -|- STACKS -|- " + SEP + "STKS-MONO" + SEP +
				callnum + SEP + shelfkey + SEP + reversekey + SEP + callnum + SEP + volSort;
	    solrFldMapTest.assertSolrFldValue(testFilePath, id, fldName, fldVal);
		callnum = "PR3724.T3 A2 V.1";
		lopped = CallNumUtils.removeLCVolSuffix(callnum);
		shelfkey = edu.stanford.CallNumUtils.getShelfKey(lopped, CallNumberType.LC, id).toLowerCase();
		reversekey = org.solrmarc.tools.CallNumUtils.getReverseShelfKey(shelfkey).toLowerCase();
		volSort = edu.stanford.CallNumUtils.getVolumeSortCallnum(callnum, lopped, shelfkey, CallNumberType.LC, !isSerial, id);
		fldVal = "36105003934424 -|- GREEN -|- STACKS -|- " + SEP + "STKS-MONO" + SEP +
				lopped + SEP + shelfkey + SEP + reversekey + SEP + callnum + SEP + volSort;
	    solrFldMapTest.assertSolrFldValue(testFilePath, id, fldName, fldVal);
		callnum = "PR3724.T3 A2 V.2";
		lopped = CallNumUtils.removeLCVolSuffix(callnum);
		shelfkey = edu.stanford.CallNumUtils.getShelfKey(lopped, CallNumberType.LC, id).toLowerCase();
		reversekey = org.solrmarc.tools.CallNumUtils.getReverseShelfKey(shelfkey).toLowerCase();
		volSort = edu.stanford.CallNumUtils.getVolumeSortCallnum(callnum, lopped, shelfkey, CallNumberType.LC, !isSerial, id);
		fldVal = "36105048104132 -|- GREEN -|- STACKS -|- " + SEP + "STKS-MONO" + SEP +
				lopped + SEP + shelfkey + SEP + reversekey + SEP + callnum + SEP + volSort;
	    solrFldMapTest.assertSolrFldValue(testFilePath, id, fldName, fldVal);
	}


	/**
	 * test if item_display field is populated correctly when location is online
	 *   ?? callnums in search results?  in record view?
	 *   callnum facets?   nearby-on-shelf?
	 */
@Test
	public final void testItemDisplayOnlineLocs() 
			throws ParserConfigurationException, IOException, SAXException
	{
		String fldName = "item_display";
		String testFilePath = testDataParentPath + File.separator + "locationTests.mrc";
	
		// online locations do not appear as items in the search results, but
		//   they do appear in nearby on shelf

		// ELECTR-LOC
		String id = "115472";
		String callnum = "HC241.25 .I4 D47";
		String shelfkey = edu.stanford.CallNumUtils.getShelfKey(callnum, CallNumberType.LC, id).toLowerCase();
		String reversekey = org.solrmarc.tools.CallNumUtils.getReverseShelfKey(shelfkey).toLowerCase();
		String volSort = edu.stanford.CallNumUtils.getVolumeSortCallnum(callnum, callnum, shelfkey, CallNumberType.LC, !isSerial, id);
		String fldVal = "36105033811451 -|- ELECTR-LOC" + SEP + SEP + SEP + SEP +
				callnum + SEP + shelfkey + SEP + reversekey + SEP + callnum + SEP + volSort;
	    solrFldMapTest.assertSolrFldHasNoValue(testFilePath, id, fldName, fldVal);
		
		// INTERNET
		id = "229800";
		callnum = "HG6046 .V28 1986";
		shelfkey = edu.stanford.CallNumUtils.getShelfKey(callnum, CallNumberType.LC, id).toLowerCase();
		reversekey = org.solrmarc.tools.CallNumUtils.getReverseShelfKey(shelfkey).toLowerCase();
		volSort = edu.stanford.CallNumUtils.getVolumeSortCallnum(callnum, callnum, shelfkey, CallNumberType.LC, !isSerial, id);
		fldVal = "36105034181003 -|- INTERNET" + SEP + SEP + SEP + SEP +
				callnum + SEP + shelfkey + SEP + reversekey + SEP + callnum + SEP + volSort;
		solrFldMapTest.assertSolrFldHasNoValue(testFilePath, id, fldName, fldVal);
		
		// ONLINE-TXT
		id = "460947";
		callnum = "E184.S75 R47A V.1 1980";
		String lopped = CallNumUtils.removeLCVolSuffix(callnum);
		shelfkey = edu.stanford.CallNumUtils.getShelfKey(lopped, CallNumberType.LC, id).toLowerCase();
		reversekey = org.solrmarc.tools.CallNumUtils.getReverseShelfKey(shelfkey).toLowerCase();
		volSort = edu.stanford.CallNumUtils.getVolumeSortCallnum(callnum, lopped, shelfkey, CallNumberType.LC, isSerial, id);
		fldVal = "36105007402873 -|- ONLINE-TXT" + SEP + SEP + SEP + SEP +
				lopped + SEP + shelfkey + SEP + reversekey + SEP + callnum + SEP + volSort;
	    solrFldMapTest.assertSolrFldHasNoValue(testFilePath, id, fldName, fldVal);
		
		// RESV-URL is no longer skipped
		id = "690002";
		callnum = "159.32 .W211";
		shelfkey = edu.stanford.CallNumUtils.getShelfKey(callnum, CallNumberType.DEWEY, id).toLowerCase();
		reversekey = org.solrmarc.tools.CallNumUtils.getReverseShelfKey(shelfkey).toLowerCase();
		volSort = edu.stanford.CallNumUtils.getVolumeSortCallnum(callnum, callnum, shelfkey, CallNumberType.DEWEY, !isSerial, id);
		fldVal = "36105046693508 -|- RESV-URL" + SEP + SEP + SEP + SEP +
				callnum + SEP + shelfkey + SEP + reversekey + SEP + callnum + SEP + volSort;
	    solrFldMapTest.assertSolrFldHasNoValue(testFilePath, id, fldName, fldVal);
		
		// SUL library  INTERNET callnum
		testFilePath = testDataParentPath + File.separator + "callNumberTests.mrc";
		id = "7117119";
		callnum = "";
		shelfkey = "";
		reversekey = "";
		volSort = "";
		fldVal = "7117119-1001 -|- INTERNET" + SEP + SEP + SEP + SEP +
				callnum + SEP + shelfkey + SEP + reversekey + SEP + callnum + SEP + volSort;
	    solrFldMapTest.assertSolrFldHasNoValue(testFilePath, id, fldName, fldVal);
	}

	
// display home location
	/**
	 * test if item_display field is populated correctly when there is a current
	 *  location that should be ignored in favor of the home location
	 */
@Test
	public final void testItemDisplayIgnoreCurrentLocs() 
			throws ParserConfigurationException, IOException, SAXException
	{
		String fldName = "item_display";
		String testFilePath = testDataParentPath + File.separator + "buildingTests.mrc";
		
		// these locations should only appear as "current" locations, and they
		//   should be ignored in favor of "home" locations.  The status of the
		//   item (e.g. checked out) will be displayed elsewhere.

		// CHECKEDOUT as current location, STACKS as home location
		String id = "575946";
		String callnum = "CB3 .A6 SUPPL. V.31";
		String lopped = "CB3 .A6 SUPPL.";
		String shelfkey = edu.stanford.CallNumUtils.getShelfKey(lopped, CallNumberType.LC, id).toLowerCase();
		String reversekey = org.solrmarc.tools.CallNumUtils.getReverseShelfKey(shelfkey).toLowerCase();
		String volSort = edu.stanford.CallNumUtils.getVolumeSortCallnum(callnum, lopped, shelfkey, CallNumberType.LC, !isSerial, id);
		String fldVal = "36105035087092 -|- GREEN -|- STACKS -|- CHECKEDOUT -|- STKS-MONO -|- " +
				lopped + SEP + shelfkey + SEP + reversekey + SEP + callnum + SEP + volSort;
	    solrFldMapTest.assertSolrFldValue(testFilePath, id, fldName, fldVal);
		
		// WITHDRAWN as current location implies item is skipped
//		assertDocHasNoField("3277173", fldName);
//	    fldVal = "something";
//	    solrFldMapTest.assertSolrFldHasNoValue(testFilePath, "3277173", fldName, fldVal);
	}


	/**
	 * test if item_display field is populated correctly when location implies
	 *  item is shelved by title (SHELBYTITL  SHELBYSER  STORBYTITL)
	 */
@Test
 	public final void testItemDisplayShelbyLocs()
 			throws ParserConfigurationException, IOException, SAXException 
 	{
		String fldName = "item_display";
	    String testFilePath = testDataParentPath + File.separator + "callNumberLCSortTests.mrc";

		// callnum for all three is  PQ9661 .P31 C6 VOL 1 1946"
		
		// SHELBYTITL
		String id = "1111";
		String callnum = "Shelved by title";
		String shelfkey = callnum.toLowerCase();
		String reversekey = org.solrmarc.tools.CallNumUtils.getReverseShelfKey(shelfkey).toLowerCase();
		String show_view_callnum = callnum + " VOL 1 1946";
		String volSort = edu.stanford.CallNumUtils.getVolumeSortCallnum(show_view_callnum, callnum, shelfkey, CallNumberType.LC, isSerial, id);
		String fldVal = "36105129694373 -|- CHEMCHMENG -|- SHELBYTITL" + SEP + SEP + "STKS-MONO" + SEP +
				callnum + SEP + shelfkey + SEP + reversekey + SEP + show_view_callnum + SEP + volSort;
	    solrFldMapTest.assertSolrFldValue(testFilePath, id, fldName, fldVal);
		
		// STORBYTITL
	    fldVal = "36105129694375 -|- CHEMCHMENG -|- STORBYTITL" + SEP + SEP + "STKS-MONO" + SEP + 
				callnum + SEP + shelfkey + SEP + reversekey + SEP + show_view_callnum + SEP + volSort;
	    solrFldMapTest.assertSolrFldValue(testFilePath, "3311", fldName, fldVal);

		// SHELBYSER
		id = "2211";
		callnum = "Shelved by Series title";
		shelfkey = callnum.toLowerCase();
		reversekey = org.solrmarc.tools.CallNumUtils.getReverseShelfKey(shelfkey).toLowerCase();
		show_view_callnum = callnum + " VOL 1 1946";
		volSort = edu.stanford.CallNumUtils.getVolumeSortCallnum(show_view_callnum, callnum, shelfkey, CallNumberType.OTHER, isSerial, id);
		fldVal = "36105129694374 -|- CHEMCHMENG -|- SHELBYSER" + SEP + SEP + "STKS-MONO" + SEP +
				callnum + SEP + shelfkey + SEP + reversekey + SEP + show_view_callnum + SEP + volSort;
	    solrFldMapTest.assertSolrFldValue(testFilePath, id, fldName, fldVal);
 	}
 	
 	
	/**
	 * test if item_display field is missing when the location shouldn't be
	 *  displayed
	 */
@Test
 	public final void testItemDisplaySkipLocs()
 			throws ParserConfigurationException, IOException, SAXException 
 	{
		String fldName = "item_display";
		createIxInitVars("locationTests.mrc");

		// DISCARD-NS
		assertZeroResults("id", "345228");
		// WITHDRAWN
		assertZeroResults("id", "575946");
		// FED-DOCS-S  (shadow)
		assertZeroResults("id", "804724");
		// CDPSHADOW and TECHSHADOW
		assertZeroResults("id", "1033119");
		// LOST
		assertZeroResults("id", "1505065");
		
		// INPROCESS - keep it
	    String testFilePath = testDataParentPath + File.separator + "locationTests.mrc";
		String id = "7651581";
		String callnum = "PQ9661 .P31 C6 1946";
		String shelfkey = edu.stanford.CallNumUtils.getShelfKey(callnum, CallNumberType.LC, id).toLowerCase();
		String reversekey = org.solrmarc.tools.CallNumUtils.getReverseShelfKey(shelfkey).toLowerCase();
		String volSort = edu.stanford.CallNumUtils.getVolumeSortCallnum(callnum, callnum, shelfkey, CallNumberType.LC, !isSerial, id);
		String fldVal = "36105129694373 -|- SAL3 -|- INPROCESS" + SEP + SEP + "STKS-MONO" + SEP +
				callnum + SEP + shelfkey + SEP + reversekey + SEP + callnum + SEP + volSort;
	    solrFldMapTest.assertSolrFldValue(testFilePath, id, fldName, fldVal);
 	}


	/**
	 * test if item_display field is populated correctly when location is to
	 *  be left "as is"  (no translation in map, but don't skip)
	 */
// FIXME:  this needs to be changed to a location that is still "as is"
//@Test
	public final void testAsIsLocations()
			throws ParserConfigurationException, IOException, SAXException 
	{
		String fldName = "item_display";
	    String testFilePath = testDataParentPath + File.separator + "mediaLocTests.mrc";
	
		String id = "7652182";
		String callnum = "G70.212 .A73934 2008";
		String shelfkey = edu.stanford.CallNumUtils.getShelfKey(callnum, CallNumberType.LC, id).toLowerCase();
		String reversekey = org.solrmarc.tools.CallNumUtils.getReverseShelfKey(shelfkey).toLowerCase();
		String volSort = edu.stanford.CallNumUtils.getVolumeSortCallnum(callnum, callnum, shelfkey, CallNumberType.LC, !isSerial, id);
		String fldVal = "36105130436541 -|- EARTH-SCI -|- PERM-RES -|- " + SEP + SEP +
				callnum + SEP + shelfkey + SEP + reversekey + SEP + callnum + SEP + volSort;
	    solrFldMapTest.assertSolrFldValue(testFilePath, id, fldName, fldVal);
		fldVal = "36105130436848 -|- EARTH-SCI -|- REFERENCE -|- " + SEP + SEP +
				callnum + SEP + shelfkey + SEP + reversekey + SEP + callnum + SEP + volSort;
	    solrFldMapTest.assertSolrFldValue(testFilePath, id, fldName, fldVal);
		fldVal = "36105130437192 -|- EARTH-SCI -|- MEDIA -|- " + SEP + SEP +
				callnum + SEP + shelfkey + SEP + reversekey + SEP + callnum + SEP + volSort;
	    solrFldMapTest.assertSolrFldValue(testFilePath, id, fldName, fldVal);
	}

 	 	
	/**
	 * test if item_display field is populated correctly, focused on lopped callnums
	 *  item_display contains:  (separator is " -|- ")
	 *    barcode -|- library(short version) -|- location -|- 
	 *     lopped call number (no volume/part info) -|- 
	 *     shelfkey (from lopped call num) -|- 
	 *     reverse_shelfkey (from lopped call num) -|- 
	 *     full callnum -|- callnum sortable for show view
	 */
 @Test
	public final void testItemDisplayLoppedCallnums()
			throws ParserConfigurationException, IOException, SAXException 
	{
		String fldName = "item_display";
	    String testFilePath = testDataParentPath + File.separator + "buildingTests.mrc";

		// LC
		String id = "460947";
		String callnum = "E184.S75 R47A V.1 1980";
		String lopped = "E184.S75 R47A";
		String shelfkey = edu.stanford.CallNumUtils.getShelfKey(lopped, CallNumberType.LC, id).toLowerCase();
		String reversekey = org.solrmarc.tools.CallNumUtils.getReverseShelfKey(shelfkey).toLowerCase();
		String volSort = edu.stanford.CallNumUtils.getVolumeSortCallnum(callnum, lopped, shelfkey, CallNumberType.LC, isSerial, id);
		String fldVal = "36105007402873 -|- BIOLOGY -|- STACKS -|- " + SEP + "STKS-MONO" + SEP +
				lopped + SEP + shelfkey + SEP + reversekey + SEP + callnum + SEP + volSort;
	    solrFldMapTest.assertSolrFldValue(testFilePath, id, fldName, fldVal);

		id = "575946";
		callnum = "CB3 .A6 SUPPL. V.31";
// FIXME:  it finds V.31, so it doesn't look for SUPPL. preceding it.
		lopped = "CB3 .A6 SUPPL.";
		shelfkey = edu.stanford.CallNumUtils.getShelfKey(lopped, CallNumberType.LC, id).toLowerCase();
		reversekey = org.solrmarc.tools.CallNumUtils.getReverseShelfKey(shelfkey).toLowerCase();
		volSort = edu.stanford.CallNumUtils.getVolumeSortCallnum(callnum, lopped, shelfkey, CallNumberType.LC, !isSerial, id);
		fldVal = "36105035087092 -|- GREEN -|- STACKS -|- CHECKEDOUT" + SEP + "STKS-MONO" + SEP +
				lopped + SEP + shelfkey + SEP + reversekey + SEP + callnum + SEP + volSort;
	    solrFldMapTest.assertSolrFldValue(testFilePath, id, fldName, fldVal);

		// DEWEY (no vol)
		id = "690002";
		callnum = "159.32 .W211";
		shelfkey = edu.stanford.CallNumUtils.getShelfKey(callnum, CallNumberType.DEWEY, id).toLowerCase();
		reversekey = org.solrmarc.tools.CallNumUtils.getReverseShelfKey(shelfkey).toLowerCase();
		volSort = edu.stanford.CallNumUtils.getVolumeSortCallnum(callnum, callnum, shelfkey, CallNumberType.DEWEY, !isSerial, id);
		fldVal = "36105046693508 -|- SAL3 -|- STACKS -|- " + SEP + "STKS-MONO" + SEP +
				callnum + SEP + shelfkey + SEP + reversekey + SEP + callnum + SEP + volSort;
	    solrFldMapTest.assertSolrFldValue(testFilePath, id, fldName, fldVal);
		
		// SUDOC (no vol)
		id = "2557826";
		callnum = "E 1.28:COO-4274-1";
		shelfkey = edu.stanford.CallNumUtils.getShelfKey(callnum, CallNumberType.SUDOC, id).toLowerCase();
		reversekey = org.solrmarc.tools.CallNumUtils.getReverseShelfKey(shelfkey).toLowerCase();
		volSort = edu.stanford.CallNumUtils.getVolumeSortCallnum(callnum, callnum, shelfkey, CallNumberType.SUDOC, !isSerial, id);
		fldVal = "001AMR5851 -|- GREEN -|- FED-DOCS -|- " + SEP + "GOVSTKS" + SEP +
				callnum + SEP + shelfkey + SEP + reversekey + SEP + callnum + SEP + volSort;
	    solrFldMapTest.assertSolrFldValue(testFilePath, id, fldName, fldVal);
					

	    testFilePath = testDataParentPath + File.separator + "itemDisplayTests.mrc";

		// LCPER 
		id = "460947";
		callnum = "E184.S75 R47A V.1 1980";
		lopped = "E184.S75 R47A";
		shelfkey = edu.stanford.CallNumUtils.getShelfKey(lopped, CallNumberType.LC, id).toLowerCase();
		reversekey = org.solrmarc.tools.CallNumUtils.getReverseShelfKey(shelfkey).toLowerCase();
		volSort = edu.stanford.CallNumUtils.getVolumeSortCallnum(callnum, lopped, shelfkey, CallNumberType.LC, isSerial, id);
		fldVal = "36105007402873 -|- GREEN -|- ON-ORDER -|- " + SEP + "STKS-MONO" + SEP +
				lopped + SEP + shelfkey + SEP + reversekey + SEP + callnum + SEP + volSort;
	    solrFldMapTest.assertSolrFldValue(testFilePath, id, fldName, fldVal);
		// DEWEYPER (no vol)
		id = "446688";
		callnum = "666.27 .F22";
		shelfkey = edu.stanford.CallNumUtils.getShelfKey(callnum, CallNumberType.DEWEY, id).toLowerCase();
		reversekey = org.solrmarc.tools.CallNumUtils.getReverseShelfKey(shelfkey).toLowerCase();
		volSort = edu.stanford.CallNumUtils.getVolumeSortCallnum(callnum, callnum, shelfkey, CallNumberType.DEWEY, !isSerial, id);
		fldVal = "36105007402873 -|- GREEN -|- STACKS -|- " + SEP + "STKS-MONO" + SEP +
				callnum + SEP + shelfkey + SEP + reversekey + SEP + callnum + SEP + volSort;
	    solrFldMapTest.assertSolrFldValue(testFilePath, id, fldName, fldVal);
		// ALPHANUM-SUSEL (no vol)
		id = "4578538";
		callnum = "SUSEL-69048";
		shelfkey = edu.stanford.CallNumUtils.getShelfKey(callnum, CallNumberType.OTHER, id).toLowerCase();
		reversekey = org.solrmarc.tools.CallNumUtils.getReverseShelfKey(shelfkey).toLowerCase();
		volSort = edu.stanford.CallNumUtils.getVolumeSortCallnum(callnum, callnum, shelfkey, CallNumberType.OTHER, !isSerial, id);
		fldVal = "36105046377987 -|- SAL3 -|- STACKS -|- " + SEP + "STKS-MONO" + SEP +
				callnum + SEP + shelfkey + SEP + reversekey + SEP + callnum + SEP + volSort;
	    solrFldMapTest.assertSolrFldValue(testFilePath, id, fldName, fldVal);
		// ALPHANUM - MFILM ... which is no longer lopped 12-03-09
		id = "1261173";
		callnum = "MFILM N.S. 1350 REEL 230 NO. 3741";
		shelfkey = edu.stanford.CallNumUtils.getShelfKey(callnum, CallNumberType.OTHER, id).toLowerCase();
		reversekey = org.solrmarc.tools.CallNumUtils.getReverseShelfKey(shelfkey).toLowerCase();
		volSort = edu.stanford.CallNumUtils.getVolumeSortCallnum(callnum, lopped, shelfkey, CallNumberType.OTHER, !isSerial, id);
		fldVal = "001AFX2969 -|- GREEN -|- MEDIA-MTXT -|- " + SEP + "NH-MICR" + SEP +
				callnum + SEP + shelfkey + SEP + reversekey + SEP + callnum + SEP + volSort;
	    solrFldMapTest.assertSolrFldValue(testFilePath, id, fldName, fldVal);
		// ALPHANUM - MCD
		id = "1234673";
		callnum = "MCD Brendel Plays Beethoven's Eroica variations";
		shelfkey = edu.stanford.CallNumUtils.getShelfKey(callnum, CallNumberType.OTHER, id).toLowerCase();
		reversekey = org.solrmarc.tools.CallNumUtils.getReverseShelfKey(shelfkey).toLowerCase();
		volSort = edu.stanford.CallNumUtils.getVolumeSortCallnum(callnum, callnum, shelfkey, CallNumberType.OTHER, !isSerial, id);
		fldVal = "001AFX2969 -|- GREEN -|- MEDIA-MTXT -|- " + SEP + "NH-MICR" + SEP +
				callnum + SEP + shelfkey + SEP + reversekey + SEP + callnum + SEP + volSort;
	    solrFldMapTest.assertSolrFldValue(testFilePath, id, fldName, fldVal);
		
		// multiple items with same call number
		id = "3941911";
		callnum = "PS3557 .O5829 K3 1998";
		shelfkey = edu.stanford.CallNumUtils.getShelfKey(callnum, CallNumberType.LC, id).toLowerCase();
		reversekey = org.solrmarc.tools.CallNumUtils.getReverseShelfKey(shelfkey).toLowerCase();
		volSort = edu.stanford.CallNumUtils.getVolumeSortCallnum(callnum, callnum, shelfkey, CallNumberType.LC, !isSerial, id);
		fldVal = "36105025373064 -|- GREEN -|- BENDER -|- " + SEP + "NONCIRC" + SEP +
				callnum + SEP + shelfkey + SEP + reversekey + SEP + callnum + SEP + volSort;
	    solrFldMapTest.assertSolrFldValue(testFilePath, id, fldName, fldVal);
		fldVal = "36105019748495 -|- GREEN -|- BENDER -|- " + SEP + "STKS-MONO" + SEP +
				callnum + SEP + shelfkey + SEP + reversekey + SEP + callnum + SEP + volSort;
	    solrFldMapTest.assertSolrFldValue(testFilePath, id, fldName, fldVal);

		// multiple items with same call number due to vol lopping
		id = "111";
		callnum = "PR3724.T3 A2 V.12";
		lopped = "PR3724.T3 A2";
		shelfkey = edu.stanford.CallNumUtils.getShelfKey(lopped, CallNumberType.LC, id).toLowerCase();
		reversekey = org.solrmarc.tools.CallNumUtils.getReverseShelfKey(shelfkey).toLowerCase();
		volSort = edu.stanford.CallNumUtils.getVolumeSortCallnum(callnum, lopped, shelfkey, CallNumberType.LC, !isSerial, id);
		fldVal = "36105003934432 -|- GREEN -|- STACKS -|- " + SEP + "STKS-MONO" + SEP +
				lopped + SEP + shelfkey + SEP + reversekey + SEP + callnum + SEP + volSort;
	    solrFldMapTest.assertSolrFldValue(testFilePath, id, fldName, fldVal);
		callnum = "PR3724.T3 A2 V.1";
		volSort = edu.stanford.CallNumUtils.getVolumeSortCallnum(callnum, lopped, shelfkey, CallNumberType.LC, !isSerial, id);
		fldVal = "36105003934424 -|- GREEN -|- STACKS -|- " + SEP + "STKS-MONO" + SEP +
				lopped + SEP + shelfkey + SEP + reversekey + SEP + callnum + SEP + volSort;
	    solrFldMapTest.assertSolrFldValue(testFilePath, id, fldName, fldVal);
		callnum = "PR3724.T3 A2 V.2";
		volSort = edu.stanford.CallNumUtils.getVolumeSortCallnum(callnum, lopped, shelfkey, CallNumberType.LC, !isSerial, id);
		fldVal = "36105048104132 -|- GREEN -|- STACKS -|- " + SEP + "STKS-MONO" + SEP +
				lopped + SEP + shelfkey + SEP + reversekey + SEP + callnum + SEP + volSort;
	    solrFldMapTest.assertSolrFldValue(testFilePath, id, fldName, fldVal);
		
		// multiple items with same call number due to mult buildings
		id = "222";
		callnum = "PR3724.T3 V2";
		shelfkey = edu.stanford.CallNumUtils.getShelfKey(callnum, CallNumberType.LC, id).toLowerCase();
		reversekey = org.solrmarc.tools.CallNumUtils.getReverseShelfKey(shelfkey).toLowerCase();
		volSort = edu.stanford.CallNumUtils.getVolumeSortCallnum(callnum, callnum, shelfkey, CallNumberType.LC, !isSerial, id);
		fldVal = "36105003934432 -|- GREEN -|- STACKS -|- " + SEP + "STKS-MONO" + SEP +
				callnum + SEP + shelfkey + SEP + reversekey + SEP + callnum + SEP + volSort;
	    solrFldMapTest.assertSolrFldValue(testFilePath, id, fldName, fldVal);
		fldVal = "36105003934424 -|- SAL -|- STACKS -|- " + SEP + "STKS-MONO" + SEP +
				callnum + SEP + shelfkey + SEP + reversekey + SEP + callnum + SEP + volSort;
	    solrFldMapTest.assertSolrFldValue(testFilePath, id, fldName, fldVal);

		// invalid LC call number
		id = "4823592";
		callnum = "Y 4.G 74/7:G 21/10";
		lopped = CallNumUtils.removeLCVolSuffix(callnum);
		shelfkey = edu.stanford.CallNumUtils.getShelfKey(lopped, CallNumberType.OTHER, "4823592").toLowerCase();
		reversekey = org.solrmarc.tools.CallNumUtils.getReverseShelfKey(shelfkey).toLowerCase();
		volSort = edu.stanford.CallNumUtils.getVolumeSortCallnum(callnum, lopped, shelfkey, CallNumberType.OTHER, !isSerial, id);
		fldVal = "36105063104488 -|- LAW -|- BASEMENT -|- " + SEP + "LAW-STKS" + SEP +
				lopped + SEP + shelfkey + SEP + reversekey + SEP + callnum + SEP + volSort;
	    solrFldMapTest.assertSolrFldValue(testFilePath, id, fldName, fldVal);
	}
 
	/**
	 * test if item_display field is populated correctly, focused on forward sorting callnums
	 *  item_display contains:  (separator is " -|- ")
	 *    barcode -|- library(short version) -|- location -|- 
	 *     lopped call number (no volume/part info) -|- 
	 *     shelfkey (from lopped call num) -|- 
	 *     reverse_shelfkey (from lopped call num) -|- 
	 *     full callnum -|- callnum sortable for show view
	 */
@Test
	public final void testItemDisplayShelfkey()
			throws ParserConfigurationException, IOException, SAXException 
	{
		String fldName = "item_display";
	    String testFilePath = testDataParentPath + File.separator + "buildingTests.mrc";

		// are we getting the shelfkey for the lopped call number?
		String id = "460947";
		String callnum = "E184.S75 R47A V.1 1980";
		String lopped = "E184.S75 R47A";
		String shelfkey = edu.stanford.CallNumUtils.getShelfKey(lopped, CallNumberType.LC, id).toLowerCase();
		String reversekey = org.solrmarc.tools.CallNumUtils.getReverseShelfKey(shelfkey).toLowerCase();
		String volSort = edu.stanford.CallNumUtils.getVolumeSortCallnum(callnum, lopped, shelfkey, CallNumberType.LC, isSerial, id);
		String fldVal = "36105007402873 -|- BIOLOGY -|- STACKS -|- " + SEP + "STKS-MONO" + SEP +
				lopped + SEP + shelfkey + SEP + reversekey + SEP + callnum + SEP + volSort;
	    solrFldMapTest.assertSolrFldValue(testFilePath, id, fldName, fldVal);
	}

	/**
	 * test if item_display field is populated correctly, focused on backward sorting callnums
	 *  item_display contains:  (separator is " -|- ")
	 *    barcode -|- library(short version) -|- location -|- 
	 *     lopped call number (no volume/part info) -|- 
	 *     shelfkey (from lopped call num) -|- 
	 *     reverse_shelfkey (from lopped call num) -|- 
	 *     full callnum -|- callnum sortable for show view
	 */
@Test
	public final void testItemDisplayReverseShelfkey()
			throws ParserConfigurationException, IOException, SAXException 
	{
		String fldName = "item_display";
	    String testFilePath = testDataParentPath + File.separator + "buildingTests.mrc";

		// are we getting the reverse shelfkey for the lopped call number?
		String id = "460947";
		String callnum = "E184.S75 R47A V.1 1980";
		String lopped = "E184.S75 R47A";
		String shelfkey = edu.stanford.CallNumUtils.getShelfKey(lopped, CallNumberType.LC, id).toLowerCase();
		String reversekey = org.solrmarc.tools.CallNumUtils.getReverseShelfKey(shelfkey).toLowerCase();
		String volSort = edu.stanford.CallNumUtils.getVolumeSortCallnum(callnum, lopped, shelfkey, CallNumberType.LC, isSerial, id);
		String fldVal = "36105007402873 -|- BIOLOGY -|- STACKS -|- " + SEP + "STKS-MONO" + SEP +
				lopped + SEP + shelfkey + SEP + reversekey + SEP + callnum + SEP + volSort;
	    solrFldMapTest.assertSolrFldValue(testFilePath, id, fldName, fldVal);
	}

	/**
	 * test if item_display field is populated correctly, focused on full call numbers
	 *  item_display contains:  (separator is " -|- ")
	 *    barcode -|- library(short version) -|- location -|- 
	 *     lopped call number (no volume/part info) -|- 
	 *     shelfkey (from lopped call num) -|- 
	 *     reverse_shelfkey (from lopped call num) -|- 
	 *     full callnum -|- callnum sortable for show view
	 */
@Test
	public final void testItemDisplayFullCallnum()
			throws ParserConfigurationException, IOException, SAXException 
	{
		String fldName = "item_display";
	    String testFilePath = testDataParentPath + File.separator + "buildingTests.mrc";
		
		// are we getting the full call number as expected
		String id = "460947";
		String callnum = "E184.S75 R47A V.1 1980";
		String lopped = CallNumUtils.removeLCVolSuffix(callnum);
		String shelfkey = edu.stanford.CallNumUtils.getShelfKey(lopped, CallNumberType.LC, id).toLowerCase();
		String reversekey = org.solrmarc.tools.CallNumUtils.getReverseShelfKey(shelfkey).toLowerCase();
		String volSort = edu.stanford.CallNumUtils.getVolumeSortCallnum(callnum, lopped, shelfkey, CallNumberType.LC, isSerial, id);
		String fldVal = "36105007402873 -|- BIOLOGY -|- STACKS -|- " + SEP + "STKS-MONO" + SEP +
				lopped + SEP + shelfkey + SEP + reversekey + SEP + callnum + SEP + volSort;
	    solrFldMapTest.assertSolrFldValue(testFilePath, id, fldName, fldVal);
	}

	/**
	 * test if item_display field is populated correctly, focused on sorting call numbers for show view
	 *  item_display contains:  (separator is " -|- ")
	 *    barcode -|- library(short version) -|- location -|- 
	 *     lopped call number (no volume/part info) -|- 
	 *     shelfkey (from lopped call num) -|- 
	 *     reverse_shelfkey (from lopped call num) -|- 
	 *     full callnum -|- callnum sortable for show view
	 */
@Test
	public final void testItemDisplayCallnumVolumeSort()
			throws ParserConfigurationException, IOException, SAXException 
	{
		String fldName = "item_display";
	    String testFilePath = testDataParentPath + File.separator + "buildingTests.mrc";
		
		// are we getting the volume sortable call number we expect?
		String id = "460947";
		String callnum = "E184.S75 R47A V.1 1980";
		String lopped = CallNumUtils.removeLCVolSuffix(callnum);
		String shelfkey = edu.stanford.CallNumUtils.getShelfKey(lopped, CallNumberType.LC, id).toLowerCase();
		String reversekey = org.solrmarc.tools.CallNumUtils.getReverseShelfKey(shelfkey).toLowerCase();
		String volSort = edu.stanford.CallNumUtils.getVolumeSortCallnum(callnum, lopped, shelfkey, CallNumberType.LC, isSerial, id);
		String fldVal = "36105007402873 -|- BIOLOGY -|- STACKS -|- " + SEP + "STKS-MONO" + SEP +
				lopped + SEP + shelfkey + SEP + reversekey + SEP + callnum + SEP + volSort;
	    solrFldMapTest.assertSolrFldValue(testFilePath, id, fldName, fldVal);
	}


	/**
	 * test if shelfkey field data (for searching) matches shelfkey in 
	 * item_display field
	 */
@Test
	public void testShelfkeyMatchesItemDisp()
			throws ParserConfigurationException, IOException, SAXException 
	{
	    String testFilePath = testDataParentPath + File.separator + "shelfkeyMatchItemDispTests.mrc";
		
		// shelfkey should be same in item_display and in shelfkey fields
	    String id = "5788269";
	    String callnum = "CALIF A125 .A34 2002";
	    String lopped = ItemUtils.getLoppedCallnum(callnum, CallNumberType.OTHER, isSerial);
	    String shelfkey = edu.stanford.CallNumUtils.getShelfKey(lopped, CallNumberType.OTHER, id).toLowerCase();
	    String reversekey = org.solrmarc.tools.CallNumUtils.getReverseShelfKey(shelfkey).toLowerCase();
	    String volSort = edu.stanford.CallNumUtils.getVolumeSortCallnum(callnum, lopped, shelfkey, CallNumberType.LC, isSerial, id);
	    String fldVal = "36105122888543 -|- GREEN -|- CALIF-DOCS" + SEP + SEP + "GOVSTKS" + SEP +
				lopped + SEP + shelfkey + SEP + reversekey + SEP + callnum + SEP + volSort;
	    solrFldMapTest.assertSolrFldValue(testFilePath, id, "item_display", fldVal);
	    solrFldMapTest.assertSolrFldValue(testFilePath, id, "shelfkey", shelfkey);
	    
	    id = "409752";
		callnum = "CALIF A125 .B9 V.17 1977:NO.3";
		lopped = ItemUtils.getLoppedCallnum(callnum, CallNumberType.OTHER, isSerial);
		shelfkey = edu.stanford.CallNumUtils.getShelfKey(lopped, CallNumberType.OTHER, id).toLowerCase();
		reversekey = org.solrmarc.tools.CallNumUtils.getReverseShelfKey(shelfkey).toLowerCase();
		volSort = edu.stanford.CallNumUtils.getVolumeSortCallnum(callnum, lopped, shelfkey, CallNumberType.LC, isSerial, id);
		fldVal = "36105127370745 -|- GREEN -|- CALIF-DOCS" + SEP + SEP + "GOVSTKS" + SEP +
				lopped + SEP + shelfkey + SEP + reversekey + SEP + callnum + SEP + volSort;
	    solrFldMapTest.assertSolrFldValue(testFilePath, id, "item_display", fldVal);
	    solrFldMapTest.assertSolrFldValue(testFilePath, id, "shelfkey", shelfkey);
		callnum = "CALIF A125 .B9 V.7-15 1966-1977:NO.1";
		lopped = ItemUtils.getLoppedCallnum(callnum, CallNumberType.OTHER, isSerial);
		shelfkey = edu.stanford.CallNumUtils.getShelfKey(lopped, CallNumberType.OTHER, id).toLowerCase();
		reversekey = org.solrmarc.tools.CallNumUtils.getReverseShelfKey(shelfkey).toLowerCase();
		volSort = edu.stanford.CallNumUtils.getVolumeSortCallnum(callnum, lopped, shelfkey, CallNumberType.LC, isSerial, id);
		fldVal = "36105127370737 -|- GREEN -|- CALIF-DOCS -|- " + SEP + "GOVSTKS" + SEP +
				lopped + SEP + shelfkey + SEP + reversekey + SEP + callnum + SEP + volSort;
	    solrFldMapTest.assertSolrFldValue(testFilePath, id, "item_display", fldVal);
	    solrFldMapTest.assertSolrFldValue(testFilePath, id, "shelfkey", shelfkey);

	    id = "373245";
		callnum = "553.2805 .P187 V.1-2 1916-1918";
		lopped = ItemUtils.getLoppedCallnum(callnum, CallNumberType.DEWEY, isSerial);
		shelfkey = edu.stanford.CallNumUtils.getShelfKey(lopped, CallNumberType.DEWEY, id).toLowerCase();
		reversekey = org.solrmarc.tools.CallNumUtils.getReverseShelfKey(shelfkey).toLowerCase();
		volSort = edu.stanford.CallNumUtils.getVolumeSortCallnum(callnum, lopped, shelfkey, CallNumberType.LC, isSerial, id);
		fldVal = "36105027549075 -|- SAL3 -|- STACKS -|- " + SEP + "STKS-PERI" + SEP +
				lopped + SEP + shelfkey + SEP + reversekey + SEP + callnum + SEP + volSort;
	    solrFldMapTest.assertSolrFldValue(testFilePath, id, "item_display", fldVal);
	    solrFldMapTest.assertSolrFldValue(testFilePath, id, "shelfkey", shelfkey);
	    
	    id = "373759";
		callnum = "553.2805 .P494 V.11 1924:JAN.-JUNE";
		lopped = ItemUtils.getLoppedCallnum(callnum, CallNumberType.DEWEY, isSerial);
		shelfkey = edu.stanford.CallNumUtils.getShelfKey(lopped, CallNumberType.DEWEY, id).toLowerCase();
		reversekey = org.solrmarc.tools.CallNumUtils.getReverseShelfKey(shelfkey).toLowerCase();
		volSort = edu.stanford.CallNumUtils.getVolumeSortCallnum(callnum, lopped, shelfkey, CallNumberType.LC, isSerial, id);
		fldVal = "36105027313985 -|- SAL3 -|- STACKS -|- " + SEP + "STKS-PERI" + SEP +
				lopped + SEP + shelfkey + SEP + reversekey + SEP + callnum + SEP + volSort;
	    solrFldMapTest.assertSolrFldValue(testFilePath, id, "item_display", fldVal);
	    solrFldMapTest.assertSolrFldValue(testFilePath, id, "shelfkey", shelfkey);
	}


	/**
	 * test preferred_barcode field is created correctly in index
	 */
@Test
	public void testPreferredBarcodeInIx() 
			throws ParserConfigurationException, IOException, SAXException
	{
		// single test to make sure this field is created properly
		createIxInitVars("itemPreferredTests.mrc");
		String fldName = "preferred_barcode";
		assertStringFieldProperties(fldName);
		assertFieldStored(fldName);
		assertFieldNotIndexed(fldName);
		assertFieldNotMultiValued(fldName);
		assertDocHasFieldValue("multLC", fldName, "12");
	}


	/**
	 * Assert that multiple copies of an item each have a separate field
	 */
//@Test
	public final void testMultipleCopies() 
			throws ParserConfigurationException, IOException, SAXException 
	{
		String fldName = "item_display";
		String fileName = "multipleCopies.mrc";
		createIxInitVars(fileName);
		mappingTestInit();
	    String testFilePath = testDataParentPath + File.separator + fileName;

		String id = "1";
	    String callnum = "PR3724.T2";
		String lopped = CallNumUtils.removeLCVolSuffix(callnum);
		String shelfkey = edu.stanford.CallNumUtils.getShelfKey(lopped, CallNumberType.LC, id).toLowerCase();
		String reversekey = org.solrmarc.tools.CallNumUtils.getReverseShelfKey(shelfkey).toLowerCase();
		String volSort = edu.stanford.CallNumUtils.getVolumeSortCallnum(callnum, lopped, shelfkey, CallNumberType.LC, isSerial, id);
		String rest = " -|- SAL3 -|- STACKS -|- " + SEP + SEP +
				lopped + SEP + shelfkey + SEP + reversekey + SEP + callnum + SEP + volSort;
	    String item1 = "36105003934432" + rest;
	    String item2 = "36105003934424" + rest;

	    solrFldMapTest.assertSolrFldValue(testFilePath, id, fldName, item1);
	    solrFldMapTest.assertSolrFldValue(testFilePath, id, fldName, item2);
	    
	    assertDocHasFieldValue(id, fldName, item1);
	    assertDocHasFieldValue(id, fldName, item2);
	}
}
