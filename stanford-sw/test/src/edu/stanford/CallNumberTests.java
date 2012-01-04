package edu.stanford;

import static org.junit.Assert.*;
import static edu.stanford.CallNumUtils.getVolumeSortCallnum;

import java.io.File;
import java.io.IOException;
import java.util.*;

import javax.xml.parsers.ParserConfigurationException;

import org.junit.*;
import org.solrmarc.tools.CallNumUtils;
import org.xml.sax.SAXException;

import edu.stanford.enumValues.Access;
import edu.stanford.enumValues.CallNumberType;

/**
 * junit4 tests for Stanford University call number fields for blacklight index
 * @author Naomi Dushay
 */
public class CallNumberTests extends AbstractStanfordTest {

	private final String govDocStr = "Government Document";
	private final boolean isSerial = true;
	private final String ignoredId = "ignored";
	private String fileName = "callNumberTests.mrc";
    private String testFilePath = testDataParentPath + File.separator + fileName;

@Before
	public final void setup() 
	{
		mappingTestInit();
	}	


	/**
	 * callnum_top_facet, for dewey, should be DEWEY_TOP_FACET_VAL
	 */
@Test
	public final void testFacetsInIx() 
			throws IOException, ParserConfigurationException, SAXException 
	{
		String fldName = "callnum_top_facet";
		createFreshIx(fileName);
						
		assertSingleResult("1033119", fldName, "\"B - Philosophy, Psychology, Religion\"");

		// skipped values should not be found
		// bad start chars for LC
		assertZeroResults(fldName, "I*"); // IN PROCESS 
		assertZeroResults(fldName, "W*"); // WITHDRAWN
		// only N call number in test data is "NO CALL NUMBER"
		assertZeroResults(fldName, "N*");
		assertZeroResults(fldName, "X*");

		Set<String> docIds = new HashSet<String>();
		docIds.add("690002");
		docIds.add("2328381");
		docIds.add("2214009");
		docIds.add("1849258");
		docIds.add("1");
		docIds.add("11");
		docIds.add("2");
		docIds.add("22");
		docIds.add("3");
		docIds.add("31");
		docIds.add("DeweyVol");
		assertSearchResults(fldName, "\"" + edu.stanford.CallNumUtils.DEWEY_TOP_FACET_VAL + "\"", docIds);
		assertSearchResults(fldName, "\"Dewey Classification\"", docIds);
		
		fldName = "lc_alpha_facet";
		assertZeroResults(fldName, "NO*");  // "NO CALL NUMBER"
		assertZeroResults(fldName, "IN*");  // "IN PROCESS"
		assertZeroResults(fldName, "X*");   // X call nums (including XX)
		assertZeroResults(fldName, "WI*");  // "WITHDRAWN"

		fldName = "lc_b4cutter_facet";
		assertZeroResults(fldName, "NO CALL NUMBER");
		assertZeroResults(fldName, "IN PROCESS");
		assertZeroResults(fldName, "X*"); // X call nums (including XX)
		assertZeroResults(fldName, "WITHDRAWN");
		assertZeroResults(fldName, "110978984448763");
				
		fldName = "dewey_1digit_facet";
		docIds.clear();
		docIds.add("2214009");
		docIds.add("1849258");
		assertSearchResults(fldName, "\"300s - Social Sciences\"", docIds);
		fldName = "dewey_2digit_facet";
		fldName = "dewey_b4cutter_facet";
		assertZeroResults(fldName, "WITHDRAWN");
		
		
		fldName = "callnum_search";
		assertSingleResult("690002", fldName, "\"159.32 .W211\""); 
		//  skipped values
		assertZeroResults(fldName, "\"NO CALL NUMBER\"");
		assertZeroResults(fldName, "\"IN PROCESS\"");
		assertZeroResults(fldName, "\"INTERNET RESOURCE\""); 
		assertZeroResults(fldName, "\"" + govDocStr + "\""); 
	}

	/**
	 * callnum_top_facet, for LC, contains the first letter of an LC call number
	 *  along with a user friendly description of the broad topic indicated by
	 *  the letter. Dewey and GovDoc values are tested in separate methods.
	 */
@Test
	public final void testTopFacetLC() 
	{
		String fldName = "callnum_top_facet";
		
		// single char LC classification
		solrFldMapTest.assertSolrFldValue(testFilePath, "6661112", fldName, "Z - Bibliography, Library Science, Information Resources");
		// two char LC classification
		solrFldMapTest.assertSolrFldValue(testFilePath, "999LC22", fldName, "C - Historical Sciences (Archaeology, Genealogy)");
		solrFldMapTest.assertSolrFldValue(testFilePath, "1033119", fldName, "B - Philosophy, Psychology, Religion");
		// mixed one char and two char classification values
		solrFldMapTest.assertSolrFldValue(testFilePath, "999LC1dec", fldName, "D - World History");
		solrFldMapTest.assertSolrFldValue(testFilePath, "2913114", fldName, "D - World History");
		solrFldMapTest.assertSolrFldValue(testFilePath, "3400092", fldName, "D - World History");
		// mixed 2 and 3 three char LC classification
		solrFldMapTest.assertSolrFldValue(testFilePath, "999LC3NoDec", fldName, "K - Law");
		solrFldMapTest.assertSolrFldValue(testFilePath, "999LC3Dec", fldName, "K - Law");
		solrFldMapTest.assertSolrFldValue(testFilePath, "999LC3DecSpace", fldName, "K - Law");
						
		// LCPER
		solrFldMapTest.assertSolrFldValue(testFilePath, "460947", fldName, "E - History of the Americas (General)");		
	}

	/**
	 * lc_alpha_facet contains the first alpha portion of the local LC
	 *  call number along with a user friendly description of the topic  
	 *  indicated by the letters. 
	 */
@Test
	public final void testLCAlphaFacet() 
	{
		String fldName = "lc_alpha_facet";
		
		// single char LC classification
		solrFldMapTest.assertSolrFldValue(testFilePath, "6661112", fldName, "Z - Bibliography, Library Science, Information Resources");
		// LC 999 one letter, space before Cutter
		solrFldMapTest.assertSolrFldValue(testFilePath, "7772223", fldName, "F - History of the Americas (Local)");
		solrFldMapTest.assertSolrFldValue(testFilePath, "999LC1dec", fldName, "D - World History");

		// two char LC classification
		solrFldMapTest.assertSolrFldHasNoValue(testFilePath, "1033119", fldName, "B - Philosophy, Psychology, Religion");
		solrFldMapTest.assertSolrFldValue(testFilePath, "1033119", fldName, "BX - Christian Denominations");
		// LC 999 two letters, space before Cutter
		solrFldMapTest.assertSolrFldValue(testFilePath, "999LC2", fldName, "HG - Finance");
		solrFldMapTest.assertSolrFldHasNoValue(testFilePath, "999LC22", fldName, "C - Auxiliary Sciences of History (General)");
		solrFldMapTest.assertSolrFldValue(testFilePath, "999LC22", fldName, "CB - History of Civilization");
		solrFldMapTest.assertSolrFldValue(testFilePath, "2913114", fldName, "DH - Low Countries (History)");
		solrFldMapTest.assertSolrFldValue(testFilePath, "1732616", fldName, "QA - Mathematics");
		solrFldMapTest.assertSolrFldValue(testFilePath, "115472", fldName, "HC - Economic History & Conditions");
		// mult values for a single doc
		solrFldMapTest.assertSolrFldValue(testFilePath, "3400092", fldName, "DC - France (History)");

		// three char LC classification
		solrFldMapTest.assertSolrFldHasNoValue(testFilePath, "999LC3NoDec", fldName, "K - Law");
		solrFldMapTest.assertSolrFldHasNoValue(testFilePath, "999LC3Dec", fldName, "K - Law");
		solrFldMapTest.assertSolrFldHasNoValue(testFilePath, "999LC3DecSpace", fldName, "K - Law");
		solrFldMapTest.assertSolrFldValue(testFilePath, "999LC3NoDec", fldName, "KJH - Law of Andorra");
		solrFldMapTest.assertSolrFldValue(testFilePath, "999LC3Dec", fldName, "KJH - Law of Andorra");
		solrFldMapTest.assertSolrFldValue(testFilePath, "999LC3DecSpace", fldName, "KJH - Law of Andorra");
		
		// LCPER
		solrFldMapTest.assertSolrFldValue(testFilePath, "460947", fldName, "E - History of the Americas (General)");
	}

	/**
	 * lc_b4cutter_facet contains the portion of local LC call numbers
	 *  before the Cutter.
	 */
@Test
	public final void testLCB4Cutter() 
	{
		String fldName = "lc_b4cutter_facet";
		
		// search for LC values
		solrFldMapTest.assertSolrFldHasNoValue(testFilePath, "6661112", fldName, "Z");
		solrFldMapTest.assertSolrFldValue(testFilePath, "6661112", fldName, "Z3871");
		solrFldMapTest.assertSolrFldValue(testFilePath, "999LC1dec", fldName, "D764.7");
		solrFldMapTest.assertSolrFldHasNoValue(testFilePath, "999LC22", fldName, "C");
		solrFldMapTest.assertSolrFldHasNoValue(testFilePath, "999LC22", fldName, "CB");
		solrFldMapTest.assertSolrFldValue(testFilePath, "999LC22", fldName, "CB3");
		solrFldMapTest.assertSolrFldHasNoValue(testFilePath, "999LC1dec", fldName, "D810");
		solrFldMapTest.assertSolrFldHasNoValue(testFilePath, "2913114", fldName, "D810");
		solrFldMapTest.assertSolrFldHasNoValue(testFilePath, "3400092", fldName, "D810");
		solrFldMapTest.assertSolrFldValue(testFilePath, "2913114", fldName, "DH135");
		solrFldMapTest.assertSolrFldHasNoValue(testFilePath, "999LC3NoDec", fldName, "K");
		solrFldMapTest.assertSolrFldHasNoValue(testFilePath, "999LC3NoDec", fldName, "KJ");
		solrFldMapTest.assertSolrFldHasNoValue(testFilePath, "999LC3NoDec", fldName, "KJH");
		solrFldMapTest.assertSolrFldValue(testFilePath, "999LC3NoDec", fldName, "KJH2678");
		solrFldMapTest.assertSolrFldValue(testFilePath, "999LC3DecSpace", fldName, "KJH66.6");
		solrFldMapTest.assertSolrFldValue(testFilePath, "1033119", fldName, "BX4659");
		// tricky cutter
		solrFldMapTest.assertSolrFldHasNoValue(testFilePath, "115472", fldName, "HC241");
		solrFldMapTest.assertSolrFldValue(testFilePath, "115472", fldName, "HC241.25");
		solrFldMapTest.assertSolrFldValue(testFilePath, "3400092", fldName, "DC34.5");
				
		// LCPER
		solrFldMapTest.assertSolrFldValue(testFilePath, "460947", fldName, "E184");
	}


	/**
	 * callnum_search contains all local call numbers, except those that are 
	 *  ignored, such as "NO CALL NUMBER"  It includes "bad" LC call numbers, 
	 *  such as those beginning with X;  it includes MFILM and MCD call numbers
	 *  and so on.  Testing Dewey call number search is in a separate method.
	 */
@Test
	public final void testSearchLC() 
	{
		String fldName = "callnum_search";
	
		// LC 999 one letter
		solrFldMapTest.assertSolrFldValue(testFilePath, "6661112", fldName, "Z3871.Z8");
		// LC 999 one letter, space before Cutter
		solrFldMapTest.assertSolrFldValue(testFilePath, "7772223", fldName, "F1356 .M464 2005");
		// LC 999 one letter, decimal digits and space before Cutter
		solrFldMapTest.assertSolrFldValue(testFilePath, "999LC1dec", fldName, "D764.7 .K72 1990");
		// LC 999 two letters, space before Cutter
		solrFldMapTest.assertSolrFldValue(testFilePath, "999LC2", fldName, "HG6046 .V28 1986");
		solrFldMapTest.assertSolrFldValue(testFilePath, "999LC22", fldName, "CB3 .A6 SUPPL. V.31");
		// LC 999 two letters, no space before Cutter
		solrFldMapTest.assertSolrFldValue(testFilePath, "999LC2NoDec", fldName, "PQ2678.I26 P54 1992");
		// LC 999 three letters, no space before Cutter
		solrFldMapTest.assertSolrFldValue(testFilePath, "999LC3NoDec", fldName, "KJH2678.I26 P54 1992");
		// LC 999 three letters, decimal digit, no space before Cutter
		solrFldMapTest.assertSolrFldValue(testFilePath, "999LC3Dec", fldName, "KJH666.4.I26 P54 1992");
		// LC 999 three letters, decimal digit, space before Cutter
		solrFldMapTest.assertSolrFldValue(testFilePath, "999LC3DecSpace", fldName, "KJH66.6 .I26 P54 1992");
		// LC 999, LC 050, multiple LC facet values, 082 Dewey
		solrFldMapTest.assertSolrFldValue(testFilePath, "2913114", fldName, "DH135 .P6 I65");
		// LC 999, LC 050, multiple LC facet values, 082 Dewey
		solrFldMapTest.assertSolrFldValue(testFilePath, "3400092", fldName, "DC34.5 .A78 L4 1996");
	
		// LC 999, LC 050, tough cutter
		solrFldMapTest.assertSolrFldValue(testFilePath, "115472", fldName, "HC241.25 .I4 D47");
		solrFldMapTest.assertSolrFldValue(testFilePath, "1033119", fldName, "BX4659.E85 W44");
		solrFldMapTest.assertSolrFldValue(testFilePath, "1033119", fldName, "BX4659 .E85 W44 1982");
		// 082 Dewey, LC 999, 050 (same value)
		solrFldMapTest.assertSolrFldValue(testFilePath, "1732616", fldName, "QA273 .C83 1962");
	
		// Lane invalid LC call number, so it is excluded
		solrFldMapTest.assertSolrFldHasNoValue(testFilePath, "7233951", fldName, "X578 .S64 1851");
		solrFldMapTest.assertNoSolrFld(testFilePath, "7233951", fldName);

		// non-Lane invalid LC call number so it's included
		solrFldMapTest.assertSolrFldValue(testFilePath, "greenX", fldName, "X666 .S666 1666");
		
		// LCPER 999
		solrFldMapTest.assertSolrFldValue(testFilePath, "460947", fldName, "E184.S75 R47A V.1 1980");
		
		// SUDOC 999 
		solrFldMapTest.assertSolrFldValue(testFilePath, "5511738", fldName, "Y 4.AG 8/1:108-16");
		solrFldMapTest.assertSolrFldValue(testFilePath, "2678655", fldName, "GA 1.13:RCED-85-88");
	
		// ALPHANUM 999 
		solrFldMapTest.assertSolrFldValue(testFilePath, "4578538", fldName, "SUSEL-69048");
		solrFldMapTest.assertSolrFldValue(testFilePath, "1261173", fldName, "MFILM N.S. 1350 REEL 230 NO. 3741");
		solrFldMapTest.assertSolrFldValue(testFilePath, "1234673", fldName, "MCD Brendel Plays Beethoven's Eroica variations");
	}


	/**
	 * callnum_top_facet, for dewey, should be DEWEY_TOP_FACET_VAL
	 */
@Test
	public final void testTopFacetDewey() 
	{
		String fldName = "callnum_top_facet";
		solrFldMapTest.assertSolrFldValue(testFilePath, "690002", fldName, edu.stanford.CallNumUtils.DEWEY_TOP_FACET_VAL);
		solrFldMapTest.assertSolrFldValue(testFilePath, "2328381", fldName, edu.stanford.CallNumUtils.DEWEY_TOP_FACET_VAL);
		solrFldMapTest.assertSolrFldValue(testFilePath, "1", fldName, edu.stanford.CallNumUtils.DEWEY_TOP_FACET_VAL);
		solrFldMapTest.assertSolrFldValue(testFilePath, "31", fldName, edu.stanford.CallNumUtils.DEWEY_TOP_FACET_VAL);
		solrFldMapTest.assertSolrFldValue(testFilePath, "DeweyVol", fldName, edu.stanford.CallNumUtils.DEWEY_TOP_FACET_VAL);
	}
	

	/**
	 * dewey_1digit_facet contains the hundreds digit of a Dewey call
	 *  number along with a user friendly description of the broad topic so 
	 *  indicated
	 */
@Test
	public final void testLevel2FacetDewey() 
	{
		String fldName = "dewey_1digit_facet";
		solrFldMapTest.assertSolrFldValue(testFilePath, "690002", fldName, "100s - Philosophy & Psychology");
		solrFldMapTest.assertSolrFldValue(testFilePath, "2328381", fldName, "800s - Literature");
		solrFldMapTest.assertSolrFldValue(testFilePath, "2214009", fldName, "300s - Social Sciences");
		solrFldMapTest.assertSolrFldValue(testFilePath, "1849258", fldName, "300s - Social Sciences");
		solrFldMapTest.assertSolrFldValue(testFilePath, "1", fldName, "000s - Computer Science, Information & General Works");
		solrFldMapTest.assertSolrFldValue(testFilePath, "11", fldName, "000s - Computer Science, Information & General Works");
		solrFldMapTest.assertSolrFldValue(testFilePath, "2", fldName, "000s - Computer Science, Information & General Works");
		solrFldMapTest.assertSolrFldValue(testFilePath, "22", fldName, "000s - Computer Science, Information & General Works");
		solrFldMapTest.assertSolrFldValue(testFilePath, "3", fldName, "900s - History & Geography");
		solrFldMapTest.assertSolrFldValue(testFilePath, "31", fldName, "900s - History & Geography");
	}

	/**
	 * dewey_2digit_facet contains the hundred and tens digits of a 
	 *  Dewey call number (e.g 710s), along with a user friendly description of 
	 *  the topic indicated by the numbers.
	 */
@Test
	public final void testLevel3FacetDewey() 
	{
		String fldName = "dewey_2digit_facet";
		solrFldMapTest.assertSolrFldValue(testFilePath, "690002", fldName, "150s - Psychology");
		solrFldMapTest.assertSolrFldValue(testFilePath, "2328381", fldName, "820s - English & Old English Literatures");
		solrFldMapTest.assertSolrFldValue(testFilePath, "1849258", fldName, "350s - Public Administration");
		solrFldMapTest.assertSolrFldValue(testFilePath, "2214009", fldName, "370s - Education");
		solrFldMapTest.assertSolrFldValue(testFilePath, "1", fldName, "000s - Computer Science, Information & General Works");
		solrFldMapTest.assertSolrFldValue(testFilePath, "11", fldName, "000s - Computer Science, Information & General Works");
		solrFldMapTest.assertSolrFldValue(testFilePath, "2", fldName, "020s - Library & Information Sciences");
		solrFldMapTest.assertSolrFldValue(testFilePath, "22", fldName, "020s - Library & Information Sciences");
		solrFldMapTest.assertSolrFldValue(testFilePath, "3", fldName, "990s - General History of Other Areas");
		solrFldMapTest.assertSolrFldValue(testFilePath, "31", fldName, "990s - General History of Other Areas");
	}


	/**
	 * dewey_b4cutter_facet contains the portion of the Dewey call 
	 * numbers before the Cutter.  
	 */
@Test
	public final void testLevel4FacetDewey() 
	{
		String fldName = "dewey_b4cutter_facet";
		solrFldMapTest.assertSolrFldHasNoValue(testFilePath, "690002", fldName, "159");
		solrFldMapTest.assertSolrFldValue(testFilePath, "690002", fldName, "159.32");
		solrFldMapTest.assertSolrFldHasNoValue(testFilePath, "2328381", fldName, "827");
		solrFldMapTest.assertSolrFldValue(testFilePath, "2328381", fldName, "827.5");
		solrFldMapTest.assertSolrFldHasNoValue(testFilePath, "1849258", fldName, "352");
		solrFldMapTest.assertSolrFldValue(testFilePath, "1849258", fldName, "352.042");
		solrFldMapTest.assertSolrFldHasNoValue(testFilePath, "2214009", fldName, "370");
		solrFldMapTest.assertSolrFldValue(testFilePath, "2214009", fldName, "370.1");
		solrFldMapTest.assertSolrFldValue(testFilePath, "1", fldName, "001");
		solrFldMapTest.assertSolrFldValue(testFilePath, "11", fldName, "001.123");
		solrFldMapTest.assertSolrFldValue(testFilePath, "2", fldName, "022");
		solrFldMapTest.assertSolrFldValue(testFilePath, "22", fldName, "022.456");
		solrFldMapTest.assertSolrFldValue(testFilePath, "3", fldName, "999");
		solrFldMapTest.assertSolrFldValue(testFilePath, "31", fldName, "999.85");
	}

	/**
	 * callnum_search contains local call numbers.  LC and other searching
	 *  are tested in another method.
	 */
@Test
	public final void testSearchDewey() 
	{
		String fldName = "callnum_search";		
		solrFldMapTest.assertSolrFldValue(testFilePath, "690002", fldName, "159.32 .W211");
		solrFldMapTest.assertSolrFldValue(testFilePath, "2328381", fldName, "827.5 .S97TG");
		solrFldMapTest.assertSolrFldValue(testFilePath, "1849258", fldName, "352.042 .C594 ED.2");
		solrFldMapTest.assertSolrFldValue(testFilePath, "2214009", fldName, "370.1 .S655");
		solrFldMapTest.assertSolrFldValue(testFilePath, "1", fldName, "1 .N44");
		solrFldMapTest.assertSolrFldValue(testFilePath, "11", fldName, "1.123 .N44");
		solrFldMapTest.assertSolrFldValue(testFilePath, "2", fldName, "22 .N47");
		solrFldMapTest.assertSolrFldValue(testFilePath, "22", fldName, "22.456 .S655");
		solrFldMapTest.assertSolrFldValue(testFilePath, "3", fldName, "999 .F67");
		solrFldMapTest.assertSolrFldValue(testFilePath, "31", fldName, "999.85 .P84");
	}


	/**
	 * test addition of leading zeros to Dewey call numbers with fewer than
	 *  three digits before the decimal (or implied decimal)
	 */
@Test
	public final void testDeweyLeadingZeros() 
	{
		String fldName = "dewey_1digit_facet";
		solrFldMapTest.assertSolrFldValue(testFilePath, "1", fldName, "000s - Computer Science, Information & General Works");
		solrFldMapTest.assertSolrFldValue(testFilePath, "11", fldName, "000s - Computer Science, Information & General Works");
		solrFldMapTest.assertSolrFldValue(testFilePath, "2", fldName, "000s - Computer Science, Information & General Works");
		solrFldMapTest.assertSolrFldValue(testFilePath, "22", fldName, "000s - Computer Science, Information & General Works");
		solrFldMapTest.assertSolrFldValue(testFilePath, "3", fldName, "900s - History & Geography");
		solrFldMapTest.assertSolrFldValue(testFilePath, "31", fldName, "900s - History & Geography");

		fldName = "dewey_2digit_facet";
		solrFldMapTest.assertSolrFldValue(testFilePath, "1", fldName, "000s - Computer Science, Information & General Works");
		solrFldMapTest.assertSolrFldValue(testFilePath, "11", fldName, "000s - Computer Science, Information & General Works");
		solrFldMapTest.assertSolrFldValue(testFilePath, "2", fldName, "020s - Library & Information Sciences");
		solrFldMapTest.assertSolrFldValue(testFilePath, "22", fldName, "020s - Library & Information Sciences");
		solrFldMapTest.assertSolrFldValue(testFilePath, "3", fldName, "990s - General History of Other Areas");
		solrFldMapTest.assertSolrFldValue(testFilePath, "31", fldName, "990s - General History of Other Areas");

		fldName = "dewey_b4cutter_facet";
		solrFldMapTest.assertSolrFldValue(testFilePath, "1", fldName, "001");
		solrFldMapTest.assertSolrFldValue(testFilePath, "11", fldName, "001.123");
		solrFldMapTest.assertSolrFldValue(testFilePath, "2", fldName, "022");
		solrFldMapTest.assertSolrFldValue(testFilePath, "22", fldName, "022.456");
		solrFldMapTest.assertSolrFldValue(testFilePath, "3", fldName, "999");
		solrFldMapTest.assertSolrFldValue(testFilePath, "31", fldName, "999.85");
	}


	/**
	 * Call number top level facet should be GOV_DOC_TOP_FACET_VAL if the "type" 
	 *  of call number indicated in the 999 is "SUDOC" or if there is an 086 
	 *  present
	 */
@Test
	public final void testGovtDocFromSUDOC() 
	{
		String fldName = "callnum_top_facet";
		solrFldMapTest.assertSolrFldValue(testFilePath, "2557826", fldName, govDocStr);
		solrFldMapTest.assertSolrFldValue(testFilePath, "5511738", fldName, govDocStr);
		solrFldMapTest.assertSolrFldValue(testFilePath, "2678655", fldName, govDocStr);
	}


	/**
	 * Call number top level facet should be "Gov't Doc" if the location is 
	 *  a gov doc location, regardless of the type of call number
	 */
@Test
	public final void testGovDocFromLocation() 
	{
		String fldName = "callnum_top_facet";
	    testFilePath = testDataParentPath + File.separator + "callNumberGovDocTests.mrc";
		solrFldMapTest.assertSolrFldValue(testFilePath, "brit", fldName, edu.stanford.CallNumUtils.GOV_DOC_TOP_FACET_VAL);
		solrFldMapTest.assertSolrFldValue(testFilePath, "calif", fldName, edu.stanford.CallNumUtils.GOV_DOC_TOP_FACET_VAL);
		solrFldMapTest.assertSolrFldValue(testFilePath, "fed", fldName, edu.stanford.CallNumUtils.GOV_DOC_TOP_FACET_VAL);
		solrFldMapTest.assertSolrFldValue(testFilePath, "intl", fldName, edu.stanford.CallNumUtils.GOV_DOC_TOP_FACET_VAL);
		solrFldMapTest.assertSolrFldValue(testFilePath, "ssrcdocs", fldName, edu.stanford.CallNumUtils.GOV_DOC_TOP_FACET_VAL);
		solrFldMapTest.assertSolrFldValue(testFilePath, "ssrcfiche", fldName, edu.stanford.CallNumUtils.GOV_DOC_TOP_FACET_VAL);
		solrFldMapTest.assertSolrFldValue(testFilePath, "ssrcnwdoc", fldName, edu.stanford.CallNumUtils.GOV_DOC_TOP_FACET_VAL);
		solrFldMapTest.assertSolrFldValue(testFilePath, "sudoc", fldName, edu.stanford.CallNumUtils.GOV_DOC_TOP_FACET_VAL);
		
		solrFldMapTest.assertSolrFldHasNoValue(testFilePath, "ssrcfiche", fldName, "300s - Social Sciences");
		
		// item has LC call number AND item has gov doc location
		solrFldMapTest.assertSolrFldValue(testFilePath, "brit", fldName, "Z - Bibliography, Library Science, Information Resources");
	}


	/**
	 * Call number top level facet should be both the LC call number stuff AND
	 *  "Gov't Doc" if the "type" of call number is LC and the location is 
	 *  a gov doc location.
	 * If the call number is labeled LC, but does not parse, and the location is
	 *  a gov doc location, then the top level facet hsould be gov doc only.
	 */
@Test
	public final void testLevel2FacetGovDoc() 
	{
		String fldName = "gov_doc_type_facet";		
	    testFilePath = testDataParentPath + File.separator + "callNumberGovDocTests.mrc";

		solrFldMapTest.assertSolrFldValue(testFilePath, "brit", fldName, edu.stanford.CallNumUtils.GOV_DOC_BRIT_FACET_VAL);
		solrFldMapTest.assertSolrFldValue(testFilePath, "calif", fldName, edu.stanford.CallNumUtils.GOV_DOC_CALIF_FACET_VAL);
		solrFldMapTest.assertSolrFldValue(testFilePath, "intl", fldName, edu.stanford.CallNumUtils.GOV_DOC_INTL_FACET_VAL);		
		solrFldMapTest.assertSolrFldValue(testFilePath, "fed", fldName, edu.stanford.CallNumUtils.GOV_DOC_FED_FACET_VAL);
		solrFldMapTest.assertSolrFldValue(testFilePath, "ssrcdocs", fldName, edu.stanford.CallNumUtils.GOV_DOC_FED_FACET_VAL);
		solrFldMapTest.assertSolrFldValue(testFilePath, "ssrcfiche", fldName, edu.stanford.CallNumUtils.GOV_DOC_FED_FACET_VAL);
		solrFldMapTest.assertSolrFldValue(testFilePath, "ssrcnwdoc", fldName, edu.stanford.CallNumUtils.GOV_DOC_FED_FACET_VAL);
		solrFldMapTest.assertSolrFldValue(testFilePath, "sudoc", fldName, edu.stanford.CallNumUtils.GOV_DOC_UNKNOWN_FACET_VAL);
		
		solrFldMapTest.assertSolrFldHasNoValue(testFilePath, "brit", fldName, govDocStr);
	}


	/**
	 * access facet should be "Online" for call number "INTERNET RESOURCE"
	 */
@Test
	public final void testAccessOnlineFrom999() 
	{
		String fldName = "access_facet";
		String fldVal = Access.ONLINE.toString();
		solrFldMapTest.assertSolrFldValue(testFilePath, "6280316", fldName, fldVal);
		solrFldMapTest.assertSolrFldValue(testFilePath, "7117119", fldName, fldVal);
		solrFldMapTest.assertSolrFldValue(testFilePath, "7531910", fldName, fldVal);
	}


	/**
	 * test that SHELBYTITL, SHELBYSER and STORBYTITL locations cause call 
	 *  numbers to be ignored (not included in facets)
	 */
@Test
	public final void testIgnoreShelbyLocations() 
	{
		String fldName = "lc_b4cutter_facet";
		solrFldMapTest.assertSolrFldHasNoValue(testFilePath, "1111", fldName, "PQ9661");
		solrFldMapTest.assertSolrFldHasNoValue(testFilePath, "2211", fldName, "PQ9661");
		solrFldMapTest.assertSolrFldHasNoValue(testFilePath, "3311", fldName, "PQ9661");
	}


	/**
	 * shelfkey should contain shelving key versions of "lopped" call
	 *  numbers (call numbers without volume info)
	 */
@Test
	public final void testShelfkeysInIx() 
			throws ParserConfigurationException, IOException, SAXException 
	{
		String fldName = "shelfkey";
		String revFldName = "reverse_shelfkey";
		createFreshIx(fileName);
		
		// assert searching works
	
		// LC: no volume info
		String id = "999LC2";
		String callnum = "HG6046 .V28 1986";
		String shelfkey = CallNumberType.LC.getPrefix() + CallNumUtils.getLCShelfkey(callnum, id);
		assertSingleResult(id, fldName, "\"" + shelfkey + "\"");
		// it should be downcased
		assertSingleResult(id, fldName, "\"" + shelfkey.toLowerCase() + "\"");
		String reverseShelfkey = CallNumUtils.getReverseShelfKey(shelfkey);
		assertSingleResult("999LC2", revFldName, "\"" + reverseShelfkey + "\"");
		// it should be downcased
		assertSingleResult("999LC2", revFldName, "\"" + reverseShelfkey.toLowerCase() + "\"");
		
		// LC: volume info to lop off
		id = "999LC22";
		callnum = "CB3 .A6 SUPPL. V.31";
// TODO: suboptimal -  it finds V.31 first, so it doesn't strip suppl.
		shelfkey = CallNumberType.LC.getPrefix() + CallNumUtils.getLCShelfkey("CB3 .A6 SUPPL. ...", id).toLowerCase();
		assertSingleResult(id, fldName, "\"" + shelfkey + "\"");
		reverseShelfkey = CallNumUtils.getReverseShelfKey(shelfkey);
		assertSingleResult("999LC22", revFldName, "\"" + reverseShelfkey + "\"");

		// assert we don't find what we don't expect		
		callnum = "NO CALL NUMBER";
		assertZeroResults(fldName, "\"" + callnum + "\""); 
		shelfkey = CallNumberType.OTHER.getPrefix() + CallNumUtils.normalizeSuffix(callnum);
		assertZeroResults(fldName, "\"" + shelfkey + "\""); 
		reverseShelfkey = CallNumUtils.getReverseShelfKey(shelfkey);
		assertZeroResults(revFldName, "\"" + reverseShelfkey + "\""); 

		//   2009-12:  actually, the whole IN PROCESS record is skipped b/c only one withdrawn item
		callnum = "IN PROCESS";
		assertZeroResults(fldName, "\"" + callnum + "\""); 
		shelfkey = CallNumberType.OTHER.getPrefix() + CallNumUtils.normalizeSuffix(callnum);
		assertZeroResults(fldName, "\"" + shelfkey + "\""); 
		reverseShelfkey = CallNumUtils.getReverseShelfKey(shelfkey);
		assertZeroResults(revFldName, "\"" + reverseShelfkey + "\""); 

		// gov doc 
		assertZeroResults(fldName, "\"" + govDocStr + "\""); 
		shelfkey = CallNumberType.SUDOC.getPrefix() + CallNumUtils.normalizeSuffix(govDocStr);
		assertZeroResults(fldName, "\"" + shelfkey + "\""); 
		reverseShelfkey = CallNumUtils.getReverseShelfKey(shelfkey);
		assertZeroResults(revFldName, "\"" + reverseShelfkey + "\""); 
		
		// ASIS 999 "INTERNET RESOURCE"
		callnum = "INTERNET RESOURCE";
		assertZeroResults(fldName, "\"" + callnum + "\""); 
		shelfkey = CallNumberType.OTHER.getPrefix() + CallNumUtils.normalizeSuffix(callnum);
		assertZeroResults(fldName, "\"" + shelfkey + "\""); 
		reverseShelfkey = CallNumUtils.getReverseShelfKey(shelfkey);
		assertZeroResults(revFldName, "\"" + reverseShelfkey + "\""); 
	}	


	/**
	 * shelfkey should contain shelving key versions of "lopped" call
	 *  numbers (call numbers without volume info)
	 */
@Test
	public final void testShelfkey() 
	{
		String fldName = "shelfkey";

		// LC: no volume info
		String id = "999LC2";
		String callnum = "HG6046 .V28 1986";
		String shelfkey = CallNumberType.LC.getPrefix() + CallNumUtils.getLCShelfkey(callnum, id).toLowerCase();
		solrFldMapTest.assertSolrFldValue(testFilePath, id, fldName, shelfkey);

		// LC: volume info to lop off
		id = "999LC22";
		callnum = "CB3 .A6 SUPPL. V.31";
// TODO: suboptimal -  it finds V.31 first, so it doesn't strip suppl.
		shelfkey = CallNumberType.LC.getPrefix() + CallNumUtils.getLCShelfkey("CB3 .A6 SUPPL. ...", id).toLowerCase();
		solrFldMapTest.assertSolrFldValue(testFilePath, id, fldName, shelfkey);
		
		// LCPER
		id = "460947";
		callnum = "E184.S75 R47A V.1 1980";
		shelfkey = CallNumberType.LC.getPrefix() + CallNumUtils.getLCShelfkey("E184.S75 R47A ...", id).toLowerCase();
		solrFldMapTest.assertSolrFldValue(testFilePath, id, fldName, shelfkey);

		//  bad LC values
		solrFldMapTest.assertNoSolrFld(testFilePath, "7370014", "NO CALL NUMBER");
		solrFldMapTest.assertNoSolrFld(testFilePath, "7370014", "lc NO CALL NUMBER");
		solrFldMapTest.assertNoSolrFld(testFilePath, "7370014", "other NO CALL NUMBER");
		//   2009-12:  actually, the whole record is skipped b/c only one withdrawn item
//		solrFldMapTest.assertNoSolrFld(testFilePath, "3277173", "IN PROCESS");

		// Dewey: no vol info
		id = "31";
		callnum = "999.85 .P84";
		shelfkey = CallNumberType.DEWEY.getPrefix() + CallNumUtils.getDeweyShelfKey(callnum).toLowerCase();
		solrFldMapTest.assertSolrFldValue(testFilePath, id, fldName, shelfkey);
		
		// Dewey: vol info to lop off
		id = "DeweyVol";
		callnum = "666 .F67 VOL. 5";
		shelfkey = CallNumberType.DEWEY.getPrefix() + CallNumUtils.getDeweyShelfKey("666 .F67 ...").toLowerCase();
		solrFldMapTest.assertSolrFldValue(testFilePath, id, fldName, shelfkey);
		
		// SUDOC 999  -  uses raw callno
		id = "5511738";
		callnum = "Y 4.AG 8/1:108-16";
		shelfkey = CallNumberType.SUDOC.getPrefix() + CallNumUtils.normalizeSuffix(callnum).toLowerCase();
		solrFldMapTest.assertSolrFldValue(testFilePath, id, fldName, shelfkey);
		
		callnum = "GA 1.13:RCED-85-88";
		shelfkey = CallNumberType.SUDOC.getPrefix() + CallNumUtils.normalizeSuffix(callnum).toLowerCase();
		solrFldMapTest.assertSolrFldValue(testFilePath, "2678655", fldName, shelfkey);

		solrFldMapTest.assertNoSolrFld(testFilePath, "2557826", govDocStr);
		solrFldMapTest.assertNoSolrFld(testFilePath, "5511738", govDocStr);
		solrFldMapTest.assertNoSolrFld(testFilePath, "2678655", govDocStr);

		// ALPHANUM 999 - uses raw callno
		callnum = "SUSEL-69048";
		shelfkey = CallNumberType.OTHER.getPrefix() + CallNumUtils.normalizeSuffix(callnum).toLowerCase();
		solrFldMapTest.assertSolrFldValue(testFilePath, "4578538", fldName, shelfkey);
		
		callnum = "MFILM N.S. 1350 REEL 230 NO. 3741";
		shelfkey = CallNumberType.OTHER.getPrefix() + CallNumUtils.normalizeSuffix(callnum).toLowerCase();
		solrFldMapTest.assertSolrFldValue(testFilePath, "1261173", fldName, shelfkey);

		callnum = "MCD Brendel Plays Beethoven's Eroica variations";
		shelfkey = CallNumberType.OTHER.getPrefix() + CallNumUtils.normalizeSuffix(callnum).toLowerCase();
		solrFldMapTest.assertSolrFldValue(testFilePath, "1234673", fldName, shelfkey);
		
		// this is a Lane invalid LC callnum
		id = "7233951";
		callnum = "X578 .S64 1851";
		shelfkey = CallNumberType.OTHER.getPrefix() + CallNumUtils.getLCShelfkey(callnum, id).toLowerCase();
		solrFldMapTest.assertSolrFldHasNoValue(testFilePath, id, fldName, shelfkey);
		shelfkey = edu.stanford.CallNumUtils.getShelfKey(callnum, CallNumberType.OTHER, id).toLowerCase();
		solrFldMapTest.assertSolrFldHasNoValue(testFilePath, id, fldName, shelfkey);
		solrFldMapTest.assertNoSolrFld(testFilePath, id, fldName);
		
		id = "greenX";
		callnum = "X666 .S666 1666";
		shelfkey = CallNumberType.LC.getPrefix() + CallNumUtils.getLCShelfkey(callnum, id).toLowerCase();
		solrFldMapTest.assertSolrFldHasNoValue(testFilePath, id, fldName, shelfkey);
		shelfkey = edu.stanford.CallNumUtils.getShelfKey(callnum, CallNumberType.OTHER, id).toLowerCase();
		solrFldMapTest.assertSolrFldValue(testFilePath, id, fldName, shelfkey);

		// ASIS 999 "INTERNET RESOURCE": No call number, but access Online
		solrFldMapTest.assertNoSolrFld(testFilePath, "6280316", "INTERNET RESOURCE");
		solrFldMapTest.assertNoSolrFld(testFilePath, "6280316", "other INTERNET RESOURCE");
		solrFldMapTest.assertNoSolrFld(testFilePath, "7117119", "INTERNET RESOURCE");
		solrFldMapTest.assertNoSolrFld(testFilePath, "7117119", "other INTERNET RESOURCE");
		solrFldMapTest.assertNoSolrFld(testFilePath, "7531910", "INTERNET RESOURCE");
		solrFldMapTest.assertNoSolrFld(testFilePath, "7531910", "other INTERNET RESOURCE");
	}


	/**
	 * reverse_shelfkey should contain reverse shelfkey versions of 
	 *  "lopped" call numbers (call numbers without volume info). Used for
	 *  sorting backwards.
	 */
@Test
	public final void testReverseShelfkey() 
	{
		String fldName = "reverse_shelfkey";

		// LC: no volume info
		String id = "999LC2";
		String callnum = "HG6046 .V28 1986";
		String shelfkey = CallNumberType.LC.getPrefix() + CallNumUtils.getLCShelfkey(callnum, id);
		String reverseShelfkey = CallNumUtils.getReverseShelfKey(shelfkey);
		solrFldMapTest.assertSolrFldHasNoValue(testFilePath, id, fldName, reverseShelfkey);
		reverseShelfkey = reverseShelfkey.toLowerCase();
		solrFldMapTest.assertSolrFldValue(testFilePath, id, fldName, reverseShelfkey);

		// LC: volume info to lop off
		id = "999LC22";
		callnum = "CB3 .A6 SUPPL. V.31";
// TODO: suboptimal -  it finds V.31 first, so it doesn't strip suppl.
		String lopped = "CB3 .A6 SUPPL. ...";
		shelfkey = CallNumberType.LC.getPrefix() + CallNumUtils.getLCShelfkey(lopped, id);
		reverseShelfkey = CallNumUtils.getReverseShelfKey(shelfkey).toLowerCase();
		solrFldMapTest.assertSolrFldValue(testFilePath, id, fldName, reverseShelfkey);
		
		// LCPER
		id = "460947";
		callnum = "E184.S75 R47A V.1 1980";
		lopped = "E184.S75 R47A ...";
		shelfkey = CallNumberType.LC.getPrefix() + CallNumUtils.getLCShelfkey(lopped, id);
		reverseShelfkey = CallNumUtils.getReverseShelfKey(shelfkey).toLowerCase();
		solrFldMapTest.assertSolrFldValue(testFilePath, id, fldName, reverseShelfkey);

		// Dewey: no vol info
		callnum = "999.85 .P84";
		shelfkey = CallNumberType.DEWEY.getPrefix() + CallNumUtils.getDeweyShelfKey(callnum);
		reverseShelfkey = CallNumUtils.getReverseShelfKey(shelfkey).toLowerCase();
		solrFldMapTest.assertSolrFldValue(testFilePath, "31", fldName, reverseShelfkey);
		
		// Dewey: vol info to lop off
		callnum = "352.042 .C594 ED.2";
		lopped = "352.042 .C594 ...";
		shelfkey = CallNumberType.DEWEY.getPrefix() + CallNumUtils.getDeweyShelfKey(lopped);
		reverseShelfkey = CallNumUtils.getReverseShelfKey(shelfkey).toLowerCase();
		solrFldMapTest.assertSolrFldValue(testFilePath, "1849258", fldName, reverseShelfkey);
		
		// SUDOC 999 
		callnum = "Y 4.AG 8/1:108-16";
		shelfkey = CallNumberType.SUDOC.getPrefix() + CallNumUtils.normalizeSuffix(callnum);
		reverseShelfkey = CallNumUtils.getReverseShelfKey(shelfkey).toLowerCase();
		solrFldMapTest.assertSolrFldValue(testFilePath, "5511738", fldName, reverseShelfkey);
		
		callnum = "GA 1.13:RCED-85-88";
		shelfkey = CallNumberType.SUDOC.getPrefix() + CallNumUtils.normalizeSuffix(callnum);
		reverseShelfkey = CallNumUtils.getReverseShelfKey(shelfkey).toLowerCase();
		solrFldMapTest.assertSolrFldValue(testFilePath, "2678655", fldName, reverseShelfkey);

		// this is a Lane invalid LC callnum
		id = "7233951";
		callnum = "X578 .S64 1851";
		shelfkey = CallNumberType.LC.getPrefix() + CallNumUtils.getLCShelfkey(callnum, id);
		reverseShelfkey = CallNumUtils.getReverseShelfKey(shelfkey).toLowerCase();
		solrFldMapTest.assertSolrFldHasNoValue(testFilePath, id, fldName, reverseShelfkey);
		shelfkey = edu.stanford.CallNumUtils.getShelfKey(callnum, CallNumberType.OTHER, id).toLowerCase();
		reverseShelfkey = CallNumUtils.getReverseShelfKey(shelfkey).toLowerCase();
		solrFldMapTest.assertSolrFldHasNoValue(testFilePath, id, fldName, reverseShelfkey);
		solrFldMapTest.assertNoSolrFld(testFilePath, id, fldName);
		
		id = "greenX";
		callnum = "X666 .S666 1666";
		// it's not processed as LC, but as OTHER
		shelfkey = CallNumberType.LC.getPrefix() + CallNumUtils.getLCShelfkey(callnum, id).toLowerCase();
		reverseShelfkey = CallNumUtils.getReverseShelfKey(shelfkey).toLowerCase();
		solrFldMapTest.assertSolrFldHasNoValue(testFilePath, id, fldName, reverseShelfkey);
		// it's not processed as LC, but as OTHER
		shelfkey = edu.stanford.CallNumUtils.getShelfKey(callnum, CallNumberType.OTHER, id).toLowerCase();
		reverseShelfkey = CallNumUtils.getReverseShelfKey(shelfkey).toLowerCase();
		solrFldMapTest.assertSolrFldValue(testFilePath, id, fldName, reverseShelfkey);

		// ALPHANUM 999 - uses raw callno
		callnum = "SUSEL-69048";
		shelfkey = CallNumberType.OTHER.getPrefix() + CallNumUtils.normalizeSuffix(callnum);
		reverseShelfkey = CallNumUtils.getReverseShelfKey(shelfkey).toLowerCase();
		solrFldMapTest.assertSolrFldValue(testFilePath, "4578538", fldName, reverseShelfkey);
		
		callnum = "MFILM N.S. 1350 REEL 230 NO. 3741";
		shelfkey = CallNumberType.OTHER.getPrefix() + CallNumUtils.normalizeSuffix(callnum);
		reverseShelfkey = CallNumUtils.getReverseShelfKey(shelfkey).toLowerCase();
		solrFldMapTest.assertSolrFldValue(testFilePath, "1261173", fldName, reverseShelfkey);

		callnum = "MCD Brendel Plays Beethoven's Eroica variations";
		shelfkey = CallNumberType.OTHER.getPrefix() + CallNumUtils.normalizeSuffix(callnum);
		reverseShelfkey = CallNumUtils.getReverseShelfKey(shelfkey).toLowerCase();
		solrFldMapTest.assertSolrFldValue(testFilePath, "1234673", fldName, reverseShelfkey);
		
		// ASIS 999 "INTERNET RESOURCE": No call number, but access Online
		callnum = "INTERNET RESOURCE";
		shelfkey = CallNumberType.OTHER.getPrefix() + CallNumUtils.normalizeSuffix(callnum);
		reverseShelfkey = CallNumUtils.getReverseShelfKey(shelfkey);
		solrFldMapTest.assertNoSolrFld(testFilePath, "6280316", "INTERNET RESOURCE");
		solrFldMapTest.assertNoSolrFld(testFilePath, "7117119", "INTERNET RESOURCE");
		solrFldMapTest.assertNoSolrFld(testFilePath, "7531910", "INTERNET RESOURCE");
	}

	/**
	 * sort keys for call numbers including any volume information
	 */
@Test
	public final void testVolumeSortCallnum() 
	{
		boolean isSerial = true;
		String reversePeriodStr = new String(CallNumUtils.reverseNonAlphanum('.'));
		String reverseSpaceStr = new String(CallNumUtils.reverseNonAlphanum(' '));
		String reverseHyphenStr = new String(CallNumUtils.reverseNonAlphanum('-'));
		
		// LC
		String callnum = "M453 .Z29 Q1 L V.2"; 
		String lopped = "M453 .Z29 Q1 L ..."; 
		String shelfkey = edu.stanford.CallNumUtils.getShelfKey(lopped, CallNumberType.LC, "fake").toLowerCase();
		assertEquals("lc m   0453.000000 z0.290000 q0.100000 l v.000002", getVolumeSortCallnum(callnum, lopped, shelfkey, CallNumberType.LC, !isSerial, ignoredId));
		String reversePrefix = "lc m   0453.000000 z0.290000 q0.100000 l 4" + reversePeriodStr + "zzzzzx";
		String junk = getVolumeSortCallnum(callnum, lopped, shelfkey, CallNumberType.LC, isSerial, ignoredId);
		assertTrue("serial volume sort incorrect", getVolumeSortCallnum(callnum, lopped, shelfkey, CallNumberType.LC, isSerial, ignoredId).startsWith(reversePrefix));
		
		callnum = "M453 .Z29 Q1 L SER.2"; 
		assertEquals("lc m   0453.000000 z0.290000 q0.100000 l ser.000002", getVolumeSortCallnum(callnum, lopped, shelfkey, CallNumberType.LC, !isSerial, ignoredId));
		reversePrefix = "lc m   0453.000000 z0.290000 q0.100000 l 7l8" + reversePeriodStr + "zzzzzx";
		assertTrue("serial volume sort incorrect", getVolumeSortCallnum(callnum, lopped, shelfkey, CallNumberType.LC, isSerial, ignoredId).startsWith(reversePrefix));
		
		// dewey 
		// suffix year
		callnum = "322.45 .R513 1957";     
		shelfkey = edu.stanford.CallNumUtils.getShelfKey(callnum, CallNumberType.DEWEY, "fake").toLowerCase();
		assertEquals("dewey 322.45000000 r513 001957",  getVolumeSortCallnum(callnum, callnum, shelfkey, CallNumberType.DEWEY, !isSerial, ignoredId));
		assertEquals("dewey 322.45000000 r513 001957",  getVolumeSortCallnum(callnum, callnum, shelfkey, CallNumberType.DEWEY, isSerial, ignoredId));
       // suffix volume		
		callnum = "323.09 .K43 V.1";
		lopped = "323.09 .K43";
		shelfkey = edu.stanford.CallNumUtils.getShelfKey(lopped, CallNumberType.DEWEY, "fake").toLowerCase();
		assertEquals("dewey 323.09000000 k43 v.000001", getVolumeSortCallnum(callnum, lopped, shelfkey, CallNumberType.DEWEY, !isSerial, ignoredId));
		reversePrefix = "dewey 323.09000000 k43 4" + reversePeriodStr + "zzzzzy";
		assertTrue("serial volume sort incorrect", getVolumeSortCallnum(callnum, lopped, shelfkey, CallNumberType.DEWEY, isSerial, ignoredId).startsWith(reversePrefix));
		// suffix - volume and year
		callnum = "322.44 .F816 V.1 1974";  
		lopped = "322.44 .F816"; 
		shelfkey = edu.stanford.CallNumUtils.getShelfKey(lopped, CallNumberType.DEWEY, "fake").toLowerCase();
		assertEquals("dewey 322.44000000 f816 v.000001 001974", getVolumeSortCallnum(callnum, lopped, shelfkey, CallNumberType.DEWEY, !isSerial, ignoredId));
		reversePrefix = "dewey 322.44000000 f816 4" + reversePeriodStr + "zzzzzy" + reverseSpaceStr + "zzyqsv";
		assertTrue("serial volume sort incorrect", getVolumeSortCallnum(callnum, lopped, shelfkey, CallNumberType.DEWEY, isSerial, ignoredId).startsWith(reversePrefix));
		// suffix no.
		callnum = "323 .A512RE NO.23-28";   
		lopped = "323 .A512RE";  
		shelfkey = edu.stanford.CallNumUtils.getShelfKey(lopped, CallNumberType.DEWEY, "fake").toLowerCase();
		assertEquals("dewey 323.00000000 a512re no.000023-000028", getVolumeSortCallnum(callnum, lopped, shelfkey, CallNumberType.DEWEY, !isSerial, ignoredId));
		reversePrefix = "dewey 323.00000000 a512re cb" + reversePeriodStr + "zzzzxw" + reverseHyphenStr + "zzzzxr";
// TODO: problem with dewey call numbers with multiple letters at end of cutter
//		assertTrue("serial volume sort incorrect", getVolumeSortCallnum(callnum, lopped, isSerial).startsWith(reversePrefix));
	}


// NOTE:  Dewey is like LC, except part before cutter is numeric.  Given
// how the code works, there is no need to test Dewey in addition to LC.

// TODO:  test sorting of call numbers that are neither LC nor Dewey ...

	// list of raw call numbers NOT in order to check sorting
	List<String> lcVolumeUnsortedCallnumList = new ArrayList<String>(25);
	{
		lcVolumeUnsortedCallnumList.add("B8.14 L3 V.4");
		lcVolumeUnsortedCallnumList.add("B8.14 L3 V.3 1947");
		lcVolumeUnsortedCallnumList.add("B8.14 L3 V.1");
		lcVolumeUnsortedCallnumList.add("B8.14 L3 V.3");
		lcVolumeUnsortedCallnumList.add("B8.14 L3 V.2");
		lcVolumeUnsortedCallnumList.add("B8.14 L3 V.2 1959");
		lcVolumeUnsortedCallnumList.add("B8.14 L3 V.1 Suppl");
		lcVolumeUnsortedCallnumList.add("B8.14 L3 V.2 1947");
		lcVolumeUnsortedCallnumList.add("B8.14 L3 V.2 1953");
	}
	
	// list of raw call numbers in "proper" order for show view of non-serial
	List<String> sortedLCVolCallnumList = new ArrayList<String>(25);
	{
		sortedLCVolCallnumList.add("B8.14 L3 V.1");
		sortedLCVolCallnumList.add("B8.14 L3 V.1 Suppl");
		sortedLCVolCallnumList.add("B8.14 L3 V.2");
		sortedLCVolCallnumList.add("B8.14 L3 V.2 1947");
		sortedLCVolCallnumList.add("B8.14 L3 V.2 1953");
		sortedLCVolCallnumList.add("B8.14 L3 V.2 1959");
		sortedLCVolCallnumList.add("B8.14 L3 V.3");
		sortedLCVolCallnumList.add("B8.14 L3 V.3 1947");
		sortedLCVolCallnumList.add("B8.14 L3 V.4");
	}

	
	// list of raw call numbers in "proper" order for show view of serial
	List<String> serialSortedLCVolCallnumList = new ArrayList<String>(25);
	{
		serialSortedLCVolCallnumList.add("B8.14 L3 V.4");
		serialSortedLCVolCallnumList.add("B8.14 L3 V.3 1947");
		serialSortedLCVolCallnumList.add("B8.14 L3 V.3");
		serialSortedLCVolCallnumList.add("B8.14 L3 V.2 1959");
		serialSortedLCVolCallnumList.add("B8.14 L3 V.2 1953");
		serialSortedLCVolCallnumList.add("B8.14 L3 V.2 1947");
		serialSortedLCVolCallnumList.add("B8.14 L3 V.2");
		serialSortedLCVolCallnumList.add("B8.14 L3 V.1 Suppl");
		serialSortedLCVolCallnumList.add("B8.14 L3 V.1");
	}
	

	/**
	 * test the sort of call numbers (for non-serials) with volume portion
	 */
@Test
	public void testLCVolumeSorting() 
	{
		String lopped = "B8.14 L3";
		String shelfkey = edu.stanford.CallNumUtils.getShelfKey(lopped, CallNumberType.LC, "fake").toLowerCase();
		// compute list: non-serial volume sorting
		Map<String,String> volSortString2callnum = new HashMap<String,String>(75);
		for (String callnum : lcVolumeUnsortedCallnumList) {
			volSortString2callnum.put(getVolumeSortCallnum(callnum, lopped, shelfkey, CallNumberType.LC, !isSerial, ignoredId), callnum);
		}
		List<String> ordered = new ArrayList<String>(volSortString2callnum.keySet());		
		Collections.sort(ordered);

		for (int i = 0; i < ordered.size(); i++) {
			assertEquals("At position " + i + " in list: ", sortedLCVolCallnumList.get(i), volSortString2callnum.get(ordered.get(i)));
		}
	}

	/**
	 * test the sort of call numbers (for serials) with volume portion
	 */
@Test
	public void testLCSerialVolumeSorting() 
	{
		String lopped = "B8.14 L3";
		String shelfkey = edu.stanford.CallNumUtils.getShelfKey(lopped, CallNumberType.LC, "fake").toLowerCase();
		// compute list: non-serial volume sorting
		Map<String,String> volSortString2callnum = new HashMap<String,String>(75);
		for (String callnum : lcVolumeUnsortedCallnumList) {
			volSortString2callnum.put(getVolumeSortCallnum(callnum, lopped, shelfkey, CallNumberType.LC, isSerial, ignoredId), callnum);
		}
		List<String> ordered = new ArrayList<String>(volSortString2callnum.keySet());		
		Collections.sort(ordered);

		for (int i = 0; i < ordered.size(); i++) {
			assertEquals("At position " + i + " in list: ", serialSortedLCVolCallnumList.get(i), volSortString2callnum.get(ordered.get(i)));
		}
	}
	
	/**
	 * test that the volume sorting is correct
	 */
@Test
	public final void testVolumeSortingCorrect() 
	{
		List<String> unsortedDeweyVolSortList = new ArrayList<String>(25);
		unsortedDeweyVolSortList.add("570.5 .N287 V.34:NO.2 1941");
		unsortedDeweyVolSortList.add("570.5 .N287 V.34:NO.3 1941");
		unsortedDeweyVolSortList.add("570.5 .N287 V.32:NO.4 1939");
		unsortedDeweyVolSortList.add("570.5 .N287 V.34:NO.1 1941");
		unsortedDeweyVolSortList.add("570.5 .N287 V.1-2 1923");
		unsortedDeweyVolSortList.add("570.5 .N287 V.28:NO.2 1936:AUG.");
		unsortedDeweyVolSortList.add("570.5 .N287 V.7-8 1926");
		unsortedDeweyVolSortList.add("570.5 .N287 V.9-10 1927");
		unsortedDeweyVolSortList.add("570.5 .N287 V.11-12 1928");
		unsortedDeweyVolSortList.add("570.5 .N287 V.3-4 1924");
		unsortedDeweyVolSortList.add("570.5 .N287 V.23-24 1934");
		unsortedDeweyVolSortList.add("570.5 .N287 V.25-26 1935");
		unsortedDeweyVolSortList.add("570.5 .N287 V.21-22 1933");
		unsortedDeweyVolSortList.add("570.5 .N287 V.29-30 1937");
		unsortedDeweyVolSortList.add("570.5 .N287 V.17-18 1931");
		unsortedDeweyVolSortList.add("570.5 .N287 V.33:NO.2-10 1940");
		unsortedDeweyVolSortList.add("570.5 .N287 V.5-6 1925");
		unsortedDeweyVolSortList.add("570.5 .N287 V.15-16 1930");
		unsortedDeweyVolSortList.add("570.5 .N287 V.13-14 1929");
		unsortedDeweyVolSortList.add("570.5 .N287 V.19-20 1932");
		
		
		// list of raw call numbers in "proper" order for show view of serial
		List<String> sortedDeweyVolSortList = new ArrayList<String>(25);
		sortedDeweyVolSortList.add("570.5 .N287 V.34:NO.3 1941");
		sortedDeweyVolSortList.add("570.5 .N287 V.34:NO.2 1941");
		sortedDeweyVolSortList.add("570.5 .N287 V.34:NO.1 1941");
		sortedDeweyVolSortList.add("570.5 .N287 V.33:NO.2-10 1940");
		sortedDeweyVolSortList.add("570.5 .N287 V.32:NO.4 1939");
		sortedDeweyVolSortList.add("570.5 .N287 V.29-30 1937");
		sortedDeweyVolSortList.add("570.5 .N287 V.28:NO.2 1936:AUG.");
		sortedDeweyVolSortList.add("570.5 .N287 V.25-26 1935");
		sortedDeweyVolSortList.add("570.5 .N287 V.23-24 1934");
		sortedDeweyVolSortList.add("570.5 .N287 V.21-22 1933");
		sortedDeweyVolSortList.add("570.5 .N287 V.19-20 1932");
		sortedDeweyVolSortList.add("570.5 .N287 V.17-18 1931");
		sortedDeweyVolSortList.add("570.5 .N287 V.15-16 1930");
		sortedDeweyVolSortList.add("570.5 .N287 V.13-14 1929");
		sortedDeweyVolSortList.add("570.5 .N287 V.11-12 1928");
		sortedDeweyVolSortList.add("570.5 .N287 V.9-10 1927");
		sortedDeweyVolSortList.add("570.5 .N287 V.7-8 1926");
		sortedDeweyVolSortList.add("570.5 .N287 V.5-6 1925");
		sortedDeweyVolSortList.add("570.5 .N287 V.3-4 1924");
		sortedDeweyVolSortList.add("570.5 .N287 V.1-2 1923");
		
//		// compute list: non-serial volume sorting
//		Map<String,String> volSortString2callnum = new HashMap<String,String>(75);
//		for (String callnum : unsortedDeweyVolSortList) {
//			volSortString2callnum.put(getVolumeSortCallnum(callnum, lopped, shelfkey, CallNumberType.LC, isSerial, ignoredId), callnum);
//		}
//		
//		String shelfkey = edu.stanford.CallNumUtils.getShelfKey(lopped, CallNumberType.LC, "fake").toLowerCase();

		boolean isSerial = true;
		String lopped = "570.5 .N287 ...";
		String loppedShelfkey = edu.stanford.CallNumUtils.getShelfKey(lopped, CallNumberType.DEWEY, ignoredId);
		// compute list: serial volume sorting
		Map<String,String> volSortString2callnum = new HashMap<String,String>(75);
		for (String callnum : unsortedDeweyVolSortList) {
			volSortString2callnum.put(getVolumeSortCallnum(callnum, lopped, loppedShelfkey, CallNumberType.DEWEY, isSerial, ignoredId), callnum);
		}

		
		
		List<String> ordered = new ArrayList<String>(volSortString2callnum.keySet());		
		Collections.sort(ordered);

		for (int i = 0; i < ordered.size(); i++) {
			assertEquals("At position " + i + " in list: ", sortedDeweyVolSortList.get(i), volSortString2callnum.get(ordered.get(i)));
		}

	}

}
