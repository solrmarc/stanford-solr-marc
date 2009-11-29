package edu.stanford;

import java.io.File;
import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import org.junit.*;
import org.xml.sax.SAXException;


/**
 * junit4 tests for Stanford University top level call number facet field
 * @author Naomi Dushay
 */
public class CallNumTopFacetTests extends AbstractStanfordBlacklightTest {

	private final String fldName = "callnum_top_facet";
	private String fileName = "callNumberOddityTests.mrc";
    private String testFilePath = testDataParentPath + File.separator + fileName;

@Before
	public final void setup() 
			throws ParserConfigurationException, IOException, SAXException 
	{
		mappingTestInit();
	}	

	
	/**
	 * call numbers that are Dewey, but have scheme listed as LC
	 */
@Test
	public final void testDeweyAsLC() 
			throws IOException, ParserConfigurationException, SAXException 
	{
	    String id = "6276339";
	    // two items:
	    // LC:  180.8 D25 V.1
	    // LC:  219.7 KA193L V.5
	    solrFldMapTest.assertSolrFldHasNoValue(testFilePath, id, fldName, "1");
	    solrFldMapTest.assertSolrFldHasNoValue(testFilePath, id, fldName, "2");
	    solrFldMapTest.assertSolrFldValue(testFilePath, id, fldName, ItemUtils.DEWEY_TOP_FACET_VAL);
	}

	/**
	 * call numbers that are alphanum, but have scheme listed as LC
	 */
@Test
	public final void testAlphanumAsLC() 
			throws IOException, ParserConfigurationException, SAXException 
	{
	    String id = "7575731";
	    // three items:
	    // LC:  1ST AMERICAN BANCORP, INC.
	    // LC:  2 B SYSTEM INC.
	    // LC:  202 DATA SYSTEMS, INC.
	    solrFldMapTest.assertSolrFldHasNoValue(testFilePath, id, fldName, "1");
	    solrFldMapTest.assertSolrFldHasNoValue(testFilePath, id, fldName, "2");
	    solrFldMapTest.assertSolrFldHasNoValue(testFilePath, id, fldName, ItemUtils.DEWEY_TOP_FACET_VAL);
	}

	/**
	 * unusual lane call numbers
	 */
@Test
	public final void testWeirdLaneCallnums() 
			throws IOException, ParserConfigurationException, SAXException 
	{
	    String id = "7278184";
	    // two items:
	    // LC:  1.1
	    // LC:  20.44
	    solrFldMapTest.assertSolrFldHasNoValue(testFilePath, id, fldName, "1");
	    solrFldMapTest.assertSolrFldHasNoValue(testFilePath, id, fldName, "2");
	    solrFldMapTest.assertSolrFldHasNoValue(testFilePath, id, fldName, ItemUtils.DEWEY_TOP_FACET_VAL);
	    
	    // LL barcodes
	    id = "7672538";
	    // LC:   4.15[C]  barcode:  LL220624  home loc:  ASK@LANE
	    // LC:   6.4C-CZ[BC]  barcode:  LL238400  home loc:  ASK@LANE
	    // LC:   8.99    barcode:  LL229390  home loc:  ASK@LANE
	    solrFldMapTest.assertSolrFldHasNoValue(testFilePath, id, fldName, "4");
	    solrFldMapTest.assertSolrFldHasNoValue(testFilePath, id, fldName, "6");
	    solrFldMapTest.assertSolrFldHasNoValue(testFilePath, id, fldName, "8");
	    solrFldMapTest.assertSolrFldHasNoValue(testFilePath, id, fldName, ItemUtils.DEWEY_TOP_FACET_VAL);
	}

	/**
	 * call numbers starting with many digits
	 */
@Test
	public final void testLotsaDigits() 
			throws IOException, ParserConfigurationException, SAXException 
	{
	    String id = "4779032";
	    // two items:
	    // LC:  158613F868 .C45 N37 2000
	    // LC:  5115126059 A17 2004
	    solrFldMapTest.assertSolrFldHasNoValue(testFilePath, id, fldName, "1");
	    solrFldMapTest.assertSolrFldHasNoValue(testFilePath, id, fldName, "5");
	    solrFldMapTest.assertSolrFldHasNoValue(testFilePath, id, fldName, ItemUtils.DEWEY_TOP_FACET_VAL);
	}

	/**
	 * SAL nn(space)nnnnn
	 */
@Test
	public final void testTwoDigitsThenSpace() 
			throws IOException, ParserConfigurationException, SAXException 
	{
	    String id = "229099";
	    // two items:
	    // LC:  70 03126  SAL  home loc UNCAT
	    // LC:  70 35156   SAL3  home loc INPROCESS
	    // LC:  70 45997  SAL  home loc STACKS
	    // ALPHANUM:   71 15446 V.1  home loc RBC-30  library SPEC-COLL
	    solrFldMapTest.assertSolrFldHasNoValue(testFilePath, id, fldName, "7");
	    // LC:  95 05041  GREEN home loc stacks 
	    solrFldMapTest.assertSolrFldHasNoValue(testFilePath, id, fldName, "9");
	    solrFldMapTest.assertSolrFldHasNoValue(testFilePath, id, fldName, ItemUtils.DEWEY_TOP_FACET_VAL);
	}


	/**
	 * call numbers that should NOT create a value in callnum_top_facet
	 */
@Test
	public final void testFourDigitNumeric() 
			throws IOException, ParserConfigurationException, SAXException 
	{
		String id = "8373645";
		// LC:  3781 2009 T
		// LC:  2345 5861 V.3
		// ALPHANUM:  2061 4246 NO.5-6 1936-1937
	    solrFldMapTest.assertSolrFldHasNoValue(testFilePath, id, fldName, "3");
	    solrFldMapTest.assertSolrFldHasNoValue(testFilePath, id, fldName, "2");
	    solrFldMapTest.assertSolrFldHasNoValue(testFilePath, id, fldName, ItemUtils.DEWEY_TOP_FACET_VAL);

	    id = "8215917";
	    // LC:  4362 .S12P2 1965 .C3   (home loc:  MAP-CASES)
	    solrFldMapTest.assertSolrFldHasNoValue(testFilePath, id, fldName, "4");
	    solrFldMapTest.assertSolrFldHasNoValue(testFilePath, id, fldName, ItemUtils.DEWEY_TOP_FACET_VAL);
	}
	

	/**
	 * call numbers that should NOT create a value in callnum_top_facet
	 */
@Test
	public final void testFourDigitDecimal() 
			throws IOException, ParserConfigurationException, SAXException 
	{
	    String id = "5319829";
	    // LC:  4861.1 /3700
	    solrFldMapTest.assertSolrFldHasNoValue(testFilePath, id, fldName, "4");
	    solrFldMapTest.assertSolrFldHasNoValue(testFilePath, id, fldName, ItemUtils.DEWEY_TOP_FACET_VAL);
	    
	    id = "4208298";
	    // ALPHANUM:  4488.301 0300 1999
	    // ALPHANUM:  4030.1 2013 NO.1-9 1958:JAN.-SEPT.
	    // LC:  4030.1 2012
	    // LC:  9698.3 4275.25 F V.4
	    // ALPHANUM:  9701 6587.30 NO.3-6 1959:MAR.-JUNE
	    solrFldMapTest.assertSolrFldHasNoValue(testFilePath, id, fldName, "4");
	    solrFldMapTest.assertSolrFldHasNoValue(testFilePath, id, fldName, "9");
	    solrFldMapTest.assertSolrFldHasNoValue(testFilePath, id, fldName, ItemUtils.DEWEY_TOP_FACET_VAL);
	    
	    id = "6773122";
	    // LC:  1975.1 3772.1
	    // LCPER:   1975.1 3772.1 NO.29 2008
	    solrFldMapTest.assertSolrFldHasNoValue(testFilePath, id, fldName, "1");
	    solrFldMapTest.assertSolrFldHasNoValue(testFilePath, id, fldName, ItemUtils.DEWEY_TOP_FACET_VAL);
	    
	    id = "485907";
	    // ALPHANUM: 8291.209 .A963 V.7 1975/1976  SAL3 home loc STACKS
	    solrFldMapTest.assertSolrFldHasNoValue(testFilePath, id, fldName, "8");
	    solrFldMapTest.assertSolrFldHasNoValue(testFilePath, id, fldName, ItemUtils.DEWEY_TOP_FACET_VAL);
	}    


	/**
	 * weird in process call numbers that should NOT create a value in 
	 * callnum_top_facet
	 */
@Test
	public final void testInProcess() 
			throws IOException, ParserConfigurationException, SAXException 
	{
		String id = "3315407";
		// curr loc "INPROCESS"
		// LC:  001AQJ5818
	    solrFldMapTest.assertSolrFldHasNoValue(testFilePath, id, fldName, "0");
	    solrFldMapTest.assertSolrFldHasNoValue(testFilePath, id, fldName, ItemUtils.DEWEY_TOP_FACET_VAL);
	}

	/**
	 * EDI home loc (in process)
	 */
@Test
	public final void testEdiInProcess() 
			throws IOException, ParserConfigurationException, SAXException 
	{
	    String id = "8366720";
	    // LC:  427331959
	    solrFldMapTest.assertSolrFldHasNoValue(testFilePath, id, fldName, "4");
	    solrFldMapTest.assertSolrFldHasNoValue(testFilePath, id, fldName, ItemUtils.DEWEY_TOP_FACET_VAL);
	}

	/**
	 * Japanese In Process
	 */
@Test
	public final void testJapaneseInProcess() 
			throws IOException, ParserConfigurationException, SAXException 
	{
		String id = "7926635";
		// LC:   7926635  curr loc INPROCESS   home loc JAPANESE  lib EAST-ASIA
		// LC:  7885324-1001-2  curr loc INPROCESS   home loc JAPANESE  lib EAST-ASIA
		// LC:  7890569-1001  curr loc INPROCESS   home loc JAPANESE  lib EAST-ASIA
		solrFldMapTest.assertSolrFldHasNoValue(testFilePath, id, fldName, "7");
	    solrFldMapTest.assertSolrFldHasNoValue(testFilePath, id, fldName, ItemUtils.DEWEY_TOP_FACET_VAL);
	}
	

	/**
	 * Rare In Process
	 */
@Test
	public final void testRareInProcess() 
			throws IOException, ParserConfigurationException, SAXException 
	{
		String id = "3495032";
		// lib SPEC-COLL,  home loc RARE-BOOKS,  curr loc  INPROCESS
		// LC:   741.5 F 
		solrFldMapTest.assertSolrFldHasNoValue(testFilePath, id, fldName, "7");
	    solrFldMapTest.assertSolrFldHasNoValue(testFilePath, id, fldName, ItemUtils.DEWEY_TOP_FACET_VAL);
	
		id = "paren";
		// LC:  (ADL4044.1)XX   curr loc INPROCESS   home loc RARE-BOOKS  lib SPEC-COLL
		// LC:  (XX.4300523)
		solrFldMapTest.assertSolrFldHasNoValue(testFilePath, id, fldName, "(");
	}


	/**
	 * weird in process call numbers that should NOT create a value in 
	 * callnum_top_facet
	 */
@Test
	public final void testMathCSTechReports() 
			throws IOException, ParserConfigurationException, SAXException 
	{
	    // math-cs tech-reports  (home Loc TECH-RPTS)
	    String id = "4759923";
	    // LC:  134776
	    // LC:  262198  
	    solrFldMapTest.assertSolrFldHasNoValue(testFilePath, id, fldName, "1");
	    solrFldMapTest.assertSolrFldHasNoValue(testFilePath, id, fldName, "2");
	    solrFldMapTest.assertSolrFldHasNoValue(testFilePath, id, fldName, ItemUtils.DEWEY_TOP_FACET_VAL);
	}


	/**
	 * shelbytitle weirdness
	 */
//@Test   false positive!
	public final void testShelbyTitleWeird() 
			throws IOException, ParserConfigurationException, SAXException 
	{
	    String id = "404891";
	    // LCPER:  (space)1976  home loc SHELBYTITL
	    solrFldMapTest.assertSolrFldHasNoValue(testFilePath, id, fldName, "1");
	    solrFldMapTest.assertSolrFldHasNoValue(testFilePath, id, fldName, " ");
	    solrFldMapTest.assertSolrFldHasNoValue(testFilePath, id, fldName, ItemUtils.DEWEY_TOP_FACET_VAL);
	}
	
	
	/**
	 * call numbers starting with quote "
	 *   (Jackson JL barcodes??)
	 */
@Test
	public final void testQuoteCallNum() 
			throws IOException, ParserConfigurationException, SAXException 
	{
	    String id = "quote";
	    // home loc:  ASK@GSB   barcode starts JL
	    // LC:   "LA CONSOLIDADA", S.A.  barcode: JL20264S 
	    // LC:  "NEW BEGINNING" INVESTMENT RESERVE FUND,   barcode: JL36924S
	    // LC:  "21" BRANDS, INCORPORATED   barcode:  JL41534S
	    solrFldMapTest.assertSolrFldHasNoValue(testFilePath, id, fldName, "\"");
	}


	/**
	 * call numbers starting with left paren  (
	 */
@Test
	public final void testParenCallNums() 
			throws IOException, ParserConfigurationException, SAXException 
	{
	    String id = "paren";
		// LC:  (OCOLC)65536925  curr loc B&F-HOLD home loc STACKS lib GREEN
	    // LC:  (V) JN6695 .I28 1999 COPY 2  home loc: VAULT  lib: HOOVER
	    
	    // LC:  (ADL4044.1)XX   curr loc INPROCESS   home loc RARE-BOOKS  lib SPEC-COLL
	    // LC:  (XX.4300523)
	    
	    // LC: (THE) NWNL COMPANIES, INC.  barcode:  JL8237S  home loc: ASK@GSB

	    solrFldMapTest.assertSolrFldHasNoValue(testFilePath, id, fldName, "(");
	}


	/**
	 * call numbers starting with hyphen
	 */
@Test
	public final void testHyphenCallNums() 
			throws IOException, ParserConfigurationException, SAXException 
	{
	    String id = "hyphen";
		// LC:   ---   curr loc B&F-HOLD  home loc STACKS  lib GREEN
	    solrFldMapTest.assertSolrFldHasNoValue(testFilePath, id, fldName, "-");	
	}

	/**
	 * call numbers starting with period
	 */
@Test
	public final void testPeriodCallNums() 
			throws IOException, ParserConfigurationException, SAXException 
	{
	    String id = "period";
		// LC:   .W42 1996   barcode  LL90670  home loc: ASK@LANE
	    // LC:  .M1620 .N7 N53 1985 V.1:PT.1  home loc CHINESE  lib  EAST-ASIA
	    // LC:  .G59   barcode  LL89612  home loc ASK@LANE
	    solrFldMapTest.assertSolrFldHasNoValue(testFilePath, id, fldName, ".");	
	}

	/**
	 * question mark call numbers "???"
	 */
@Test
	public final void testQuestionMarkCallNums() 
			throws IOException, ParserConfigurationException, SAXException 
	{
	    String id = "7603175";
		// LC:   ???   barcode:  LL205659  home loc ASK@LANE
	    solrFldMapTest.assertSolrFldHasNoValue(testFilePath, id, fldName, "?");	
	    solrFldMapTest.assertSolrFldHasNoValue(testFilePath, id, fldName, ItemUtils.DEWEY_TOP_FACET_VAL);
	}

	/**
	 * back tick call number  (actually it's some weird diacritic accent ..)
	 */
//@Test   false positive!
	public final void testBackTickCallNums() 
			throws IOException, ParserConfigurationException, SAXException 
	{
	    String id = "7859555";
		// LC:   >̀PE1130 .H5 H64 1840 HELEK 1  home loc RARE-BOOKS
	    solrFldMapTest.assertSolrFldHasNoValue(testFilePath, id, fldName, "`");	
	}


	/**
	 * call numbers beginning with the letter I
	 */
@Test
	public final void testLetterICallNums() 
			throws IOException, ParserConfigurationException, SAXException 
	{
	    String id = "letterI";
	    // LC:  ISHII  barcode:  JJ182093  home loc ASK@GSB
	    // LC:  ICAO DOC 4444/15 ED  home loc INTL-DOCS  lib GREEN
	    solrFldMapTest.assertSolrFldHasNoValue(testFilePath, id, fldName, "I");	
	}

	/**
	 * "INTERNET RESOURCE" call number
	 */
@Test
	public final void testInternetResourceCallNum() 
			throws IOException, ParserConfigurationException, SAXException 
	{
	    String id = "4759923";
	    // ASIS:  INTERNET RESOURCE
	    solrFldMapTest.assertSolrFldHasNoValue(testFilePath, id, fldName, "I");
	
	    id = "letterI";
	    // home loc  INTERNET,   lib  LAW
	    // LC:   INTERNET RESOURCE KF3400 .S36 2009  
	    // LC:   INTERNET RESOURCE GALE EZPROXY</subfield
	    solrFldMapTest.assertSolrFldHasNoValue(testFilePath, id, fldName, "I");
	}

	/**
	 * call numbers beginning with the letter O
	 */
@Test
	public final void testLetterOCallNums() 
			throws IOException, ParserConfigurationException, SAXException 
	{
	    String id = "letterO";
	    // LC:  OYER  barcode:  JJ183012  home loc:  ASK@GSB
	    // LC:  O'REILLY   barcode:  JJ175237  home loc ASK@GSB
	    // LC:  ONLINE RESOURCE   barcode:  JJ183188  home loc ASK@GSB<
	    // LC:  ORNL-6371  home loc  STACKS  lib EARTH-SCI<	    
	    solrFldMapTest.assertSolrFldHasNoValue(testFilePath, id, fldName, "O");	
	}

	/**
	 * call numbers beginning with the letter W
	 */
@Test
	public final void testLetterWCallNums() 
			throws IOException, ParserConfigurationException, SAXException 
	{
	    String id = "letterW";
	    // LC:  WILLIAMS   barcode JJ182091  home loc ASK@GSB
	    // LC:  WHEELER/HARTMANN/NARAYANAN  barcode JJ175811 home loc ASK@GSB
	    solrFldMapTest.assertSolrFldHasNoValue(testFilePath, id, fldName, "W");	
	}
	
	/**
	 * call numbers beginning with the letter X
	 */
@Test
	public final void testLetterXCallNums() 
			throws IOException, ParserConfigurationException, SAXException 
	{
	    String id = "letterX";
	    // home loc JAPANESE
	    // LC:  X X  
	    // LC:  X XXCIN=MXI; OID=MXI
	    // LC:  XV 852  home loc: OPEN-RES lib LAW
	    // LC:  X(4775659.1)  home loc STACKS  lib GREEN
	    // LC:  X897 .C87 Z55 2001  home loc STACKS  lib GREEN
	    // LC:  XM98-1 NO.3<  home loc MAP-CASES  lib EARTH-SCI
	    // LC:  X  home loc CHINESE
	    solrFldMapTest.assertSolrFldHasNoValue(testFilePath, id, fldName, "X");
	}
	
	/**
	 * call numbers beginning with the letter Y
	 */
@Test
	public final void testLetterYCallNums() 
			throws IOException, ParserConfigurationException, SAXException 
	{
	    String id = "letterY";
	    // LC:  YILMAZ  barcode: JJ181738  home loc ASK@GSB
	    // LC:  YBP1834690  barcode: 4761801-4001  curr loc INPROCESS home loc  RARE-BOOKS
	    // LC:  Y210 .A3F6 1973  barcode LL41857 home loc: ASK@LANE
	    // ALPHANUM:  YUGOSLAV SERIAL 1973 NO.1-6  home loc STACKS lib HOOVER
	    solrFldMapTest.assertSolrFldHasNoValue(testFilePath, id, fldName, "Y");	
	}

	/**
	 * call number facets should be assigned to those Online resources that
	 *   have call numbers
	 */
@Test
	public final void testOnlineResources()
			throws IOException, ParserConfigurationException, SAXException 
	{
		String myTestFilePath = testDataParentPath + File.separator + "locationTests.mrc";

// FIXME: we'll want real call numbers for online resources in facets ... RSN

		// ELECTR-LOC
		String id = "115472";
		// String callnum = "HC241.25 .I4 D47";
//	    solrFldMapTest.assertSolrFldValue(myTestFilePath, id, fldName, "H");
	    solrFldMapTest.assertSolrFldHasNoValue(myTestFilePath, id, fldName, "H");
		
	    // INTERNET (but not SUL)
		id = "229800";
		// callnum = "HG6046 .V28 1986";
//	    solrFldMapTest.assertSolrFldValue(myTestFilePath, id, fldName, "H");
	    solrFldMapTest.assertSolrFldHasNoValue(myTestFilePath, id, fldName, "H");

		// ONLINE-TXT
		id = "460947";
		// callnum = "E184.S75 R47A V.1 1980";
//	    solrFldMapTest.assertSolrFldValue(myTestFilePath, id, fldName, "E");
	    solrFldMapTest.assertSolrFldHasNoValue(myTestFilePath, id, fldName, "E");
		
		// RESV-URL is no longer skipped
		id = "690002";
		// callnum = "159.32 .W211";
	    solrFldMapTest.assertSolrFldValue(myTestFilePath, id, fldName, ItemUtils.DEWEY_TOP_FACET_VAL);
	
	    // SUL library - INTERNET
		myTestFilePath = testDataParentPath + File.separator + "callNumberTests.mrc";
//	    solrFldMapTest.assertSolrFldValue(myTestFilePath, "7117119", fldName, "I");	
	    solrFldMapTest.assertSolrFldHasNoValue(myTestFilePath, "7117119", fldName, "INTERNET");	
	}

	/**
	 * check for absence of searchable starting digits
	 */
@Test
	public final void testSearchNoStartingDigits()
			throws IOException, ParserConfigurationException, SAXException 
	{
		createIxInitVars(fileName);
		assertZeroResults(fldName, "0*"); 
		assertZeroResults(fldName, "1*"); 
		assertZeroResults(fldName, "2*"); 
		assertZeroResults(fldName, "3*"); 
		assertZeroResults(fldName, "4*"); 
		assertZeroResults(fldName, "5*"); 
		assertZeroResults(fldName, "6*"); 
		assertZeroResults(fldName, "7*"); 
		assertZeroResults(fldName, "8*"); 
		assertZeroResults(fldName, "9*"); 
	}
	
	/**
	 * check for absence of searchable forbidden letters
	 */
@Test
	public final void testSearchNoForbiddenLetters()
			throws IOException, ParserConfigurationException, SAXException 
	{
		createIxInitVars(fileName);
		assertZeroResults(fldName, "I*"); 
		assertZeroResults(fldName, "O*"); 
		assertZeroResults(fldName, "W*"); 
		assertZeroResults(fldName, "X*"); 
		assertZeroResults(fldName, "Y*"); 
	}
	
	/**
	 * check for absence of searchable weird chars
	 */
@Test
	public final void testSearchNoBadChars()
			throws IOException, ParserConfigurationException, SAXException 
	{
		createIxInitVars(fileName);
		assertZeroResults(fldName, "\\\""); 
		assertZeroResults(fldName, "\\("); 
		assertZeroResults(fldName, "\\-"); 
		assertZeroResults(fldName, "\\."); 
		assertZeroResults(fldName, "\\?"); 
		assertZeroResults(fldName, "`"); 
	}

}
