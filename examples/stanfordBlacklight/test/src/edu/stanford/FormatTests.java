package edu.stanford;

import java.io.*;
import java.util.*;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import org.junit.*;

import edu.stanford.enumValues.Format;


/**
 * junit4 tests for Stanford University format fields 
 * Database formats are tested separately in FormatDatabaseTests
 * @author Naomi Dushay
 */
public class FormatTests extends AbstractStanfordBlacklightTest {
	
	private final String testDataFname = "formatTests.mrc";
	String testFilePath = testDataParentPath + File.separator + "formatTests.mrc";
	String displayFldName = "format";
	String facetFldName = "format";

@Before
	public final void setup() 
			throws ParserConfigurationException, IOException, SAXException 
	{
		createIxInitVars(testDataFname);
		mappingTestInit();
	}
	
@Test
	public final void testFormatFieldProperties() 
		throws ParserConfigurationException, IOException, SAXException
	{
        assertStringFieldProperties(facetFldName);
        assertFieldIndexed(facetFldName);
        assertFieldStored(facetFldName);
		assertFieldMultiValued(facetFldName);
	}

	/**
	 * Test assignment of Book format
	 *   includes monographic series
	 */
@Test
	public final void testBookFormat() 
			throws IOException, ParserConfigurationException, SAXException 
	{
		String fldVal = Format.BOOK.toString();
		
		solrFldMapTest.assertSolrFldValue(testFilePath, "leader06a07m", facetFldName, fldVal);
		solrFldMapTest.assertSolrFldValue(testFilePath, "leader06a07m", displayFldName, fldVal);
		solrFldMapTest.assertSolrFldValue(testFilePath, "leader06t07a", facetFldName, fldVal);
		solrFldMapTest.assertSolrFldValue(testFilePath, "leader06t07a", displayFldName, fldVal);
		// monographic series
		solrFldMapTest.assertSolrFldValue(testFilePath, "leader07s00821m", facetFldName, fldVal);
		solrFldMapTest.assertSolrFldValue(testFilePath, "leader07s00821m", displayFldName, fldVal);
		solrFldMapTest.assertSolrFldValue(testFilePath, "5987319", facetFldName, fldVal);
		solrFldMapTest.assertSolrFldValue(testFilePath, "5987319", displayFldName, fldVal);
		solrFldMapTest.assertSolrFldValue(testFilePath, "5598989", facetFldName, fldVal);
		solrFldMapTest.assertSolrFldValue(testFilePath, "5598989", displayFldName, fldVal);
		solrFldMapTest.assertSolrFldValue(testFilePath, "223344", facetFldName, fldVal);
		solrFldMapTest.assertSolrFldValue(testFilePath, "223344", displayFldName, fldVal);
		solrFldMapTest.assertSolrFldValue(testFilePath, "5666387", facetFldName, fldVal);
		solrFldMapTest.assertSolrFldValue(testFilePath, "5666387", displayFldName, fldVal);
		solrFldMapTest.assertSolrFldValue(testFilePath, "666", facetFldName, fldVal);
		solrFldMapTest.assertSolrFldValue(testFilePath, "666", displayFldName, fldVal);

		// formerly believed to be monographic series 
		solrFldMapTest.assertSolrFldHasNoValue(testFilePath, "leader07b00600s00821m", facetFldName, fldVal);
		solrFldMapTest.assertSolrFldHasNoValue(testFilePath, "leader07b00600s00821m", displayFldName, fldVal);		
	}


	/**
	 * Test assignment of Journal format
	 */
//no longer using Journal, as of 2008-12-02
//@Test
	public final void testJournalFormat() 
			throws IOException, ParserConfigurationException, SAXException 
	{
        String fldVal = "Journal";

// FIXME:  mapping tests give false positives
        
		solrFldMapTest.assertSolrFldHasNoValue(testFilePath, "335577", facetFldName, fldVal);

		solrFldMapTest.assertSolrFldValue(testFilePath, "leader07sNo00600821p", facetFldName, fldVal);
		solrFldMapTest.assertSolrFldValue(testFilePath, "leader07sNo00600821p", displayFldName, fldVal);
		solrFldMapTest.assertSolrFldValue(testFilePath, "335577", facetFldName, fldVal);
		solrFldMapTest.assertSolrFldValue(testFilePath, "335577", displayFldName, fldVal);

		// LCPER in 999w - but Serial Publication		
		solrFldMapTest.assertSolrFldHasNoValue(testFilePath, "460947", facetFldName, fldVal);
		solrFldMapTest.assertSolrFldHasNoValue(testFilePath, "460947", displayFldName, fldVal);

		// 006/00 s but 006/04 blank  leader/07 b  008/21 p
		solrFldMapTest.assertSolrFldHasNoValue(testFilePath, "leader07b00600s00821p", facetFldName, fldVal);
		solrFldMapTest.assertSolrFldHasNoValue(testFilePath, "leader07b00600s00821p", displayFldName, fldVal);


		Set<String> docIds = new HashSet<String>();
		docIds.add("leader07sNo00600821p");
		docIds.add("335577");
		
		assertFieldValues(displayFldName, fldVal, docIds);

		assertSearchResults(facetFldName, fldVal, docIds);

		// LCPER in 999w - but Serial Publication
		assertDocHasNoFieldValue("460947", displayFldName, fldVal);
		// 006/00 s but 006/04 blank  leader/07 b  008/21 p
		assertDocHasNoFieldValue("leader07b00600s00821p", displayFldName, fldVal);
	}


	/**
	 * Test assignment of Serial Publication format
	 */
//no longer using Serial Publication, as of 2008-12-02
//@Test
	public final void testSerialPubFormat() 
			throws IOException, ParserConfigurationException, SAXException 
	{
        String fldVal = "Serial Publication";

// FIXME:  mapping tests give false positives

     	// leader/07 s 008/21 blank
		solrFldMapTest.assertSolrFldValue(testFilePath, "leader06a07s", facetFldName, fldVal);
		solrFldMapTest.assertSolrFldValue(testFilePath, "leader06a07s", displayFldName, fldVal);
		solrFldMapTest.assertSolrFldValue(testFilePath, "4114632", facetFldName, fldVal);
		solrFldMapTest.assertSolrFldValue(testFilePath, "4114632", displayFldName, fldVal);
		solrFldMapTest.assertSolrFldValue(testFilePath, "123", facetFldName, fldVal);
		solrFldMapTest.assertSolrFldValue(testFilePath, "123", displayFldName, fldVal);
		// 006/00 s /04 blank
		solrFldMapTest.assertSolrFldValue(testFilePath, "leader07b00600s00821m", facetFldName, fldVal);
		solrFldMapTest.assertSolrFldValue(testFilePath, "leader07b00600s00821m", displayFldName, fldVal);
		// 006/00 s /04 blank
		solrFldMapTest.assertSolrFldValue(testFilePath, "leader07b00600s00821p", facetFldName, fldVal);
		solrFldMapTest.assertSolrFldValue(testFilePath, "leader07b00600s00821p", displayFldName, fldVal);
		// even though LCPER in 999 w
		solrFldMapTest.assertSolrFldValue(testFilePath, "460947", facetFldName, fldVal);
		solrFldMapTest.assertSolrFldValue(testFilePath, "460947", displayFldName, fldVal);
		// even though DEWEYPER in 999 w
		solrFldMapTest.assertSolrFldValue(testFilePath, "446688", facetFldName, fldVal);
		solrFldMapTest.assertSolrFldValue(testFilePath, "446688", displayFldName, fldVal);
		
		// leader/07s 008/21 d   006/00 s  006/04 d -- other 
		solrFldMapTest.assertSolrFldHasNoValue(testFilePath, "112233", facetFldName, fldVal);
		solrFldMapTest.assertSolrFldValue(testFilePath, "112233", facetFldName, fldVal);
		solrFldMapTest.assertSolrFldHasNoValue(testFilePath, "112233", displayFldName, fldVal);

/*
		Set<String> docIds = new HashSet<String>();
		docIds.add("leader06a07s"); // leader/07 s 008/21 blank
		docIds.add("4114632");
		docIds.add("123");
		docIds.add("leader07b00600s00821m"); // 006/00 s /04 blank
		docIds.add("leader07b00600s00821p"); // 006/00 s /04 blank 
		docIds.add("460947");  // even though LCPER in 999 w
		docIds.add("446688");  // even though DEWEYPER in 999 w
		
		
		assertFieldValues(displayFldName, fldVal, docIds);
		
		assertSearchResults(facetFldName, "\"" + fldVal + "\"", docIds);

		// leader/07s 008/21 d   006/00 s  006/04 d -- other 
		assertDocHasNoFieldValue("112233", displayFldName, fldVal);
*/		
	}


	/**
	 * Test assignment of Journal/Periodical format
	 */
@Test
	public final void testJournalPeriodicalFormat() 
			throws IOException, ParserConfigurationException, SAXException 
	{
        String fldVal = "Journal/Periodical";
		
		Set<String> docIds = new HashSet<String>();
		docIds.add("leader06a07s"); // leader/07 s 008/21 blank
		docIds.add("4114632");
		docIds.add("123");
		docIds.add("leader07b00600s00821m"); // 006/00 s /04 blank
		docIds.add("leader07b00600s00821p"); // 006/00 s /04 blank 
		docIds.add("460947");  // even though LCPER in 999 w
		docIds.add("446688");  // even though DEWEYPER in 999 w
		docIds.add("leader07sNo00600821p");
		docIds.add("335577");
		
		assertFieldValues(displayFldName, fldVal, docIds);
		
		assertSearchResults(facetFldName, "\"" + fldVal + "\"", docIds);
	
		// leader/07s 008/21 d   006/00 s  006/04 d -- other 
		assertDocHasNoFieldValue("112233", displayFldName, fldVal);
	}


	/**
	 * Test assignment of Newspaper format
	 */
@Test
	public final void testNewspaper() 
			throws IOException, ParserConfigurationException, SAXException 
	{
        String fldVal = Format.NEWSPAPER.toString();
		
		Set<String> docIds = new HashSet<String>();
		docIds.add("newspaper");
		docIds.add("leader07sNo00600821n");
		docIds.add("334455");
		
		assertFieldValues(displayFldName, fldVal, docIds);
		
		assertSearchResults(facetFldName, fldVal, docIds);
		
		// leader/07b 006/00s 008/21n - serial publication
		assertDocHasNoFieldValue("leader07b00600s00821n", displayFldName, fldVal);
	}

	/**
	 * Test assignment of Conference Proceedings format
	 * @throws IOException 
	 * @throws SAXException 
	 * @throws ParserConfigurationException 
	 */
@Test
	public final void testConferenceProceedings() 
			throws ParserConfigurationException, SAXException, IOException
	{
        String fldVal = Format.CONFERENCE_PROCEEDINGS.toString();
		
		Set<String> docIds = new HashSet<String>();
		docIds.add("5666387");
		docIds.add("666");
		
		assertFieldValues(displayFldName, fldVal, docIds);
		
		assertSearchResults(facetFldName, "\"" + fldVal + "\"", docIds);
	}


	/**
	 * Test assignment of Other format
	 */
@Test
	public final void testOtherFormat() 
			throws IOException, ParserConfigurationException, SAXException 
	{
        String fldVal = Format.OTHER.toString();

		Set<String> docIds = new HashSet<String>();
		docIds.add("leader06t07b");
		docIds.add("leader06k00833w"); 
		docIds.add("leader06g00833w"); 
		docIds.add("leader06m00826u"); 
		docIds.add("leader07b00600s00821n"); // 006/00 s /04 w
		// instructional kit 
		docIds.add("leader06o"); 
		// object 
		docIds.add("leader06r"); 
		// web site 
		docIds.add("leader07sNo00600821w"); 
		docIds.add("leader07b00600s00821w"); 
		docIds.add("7117119"); // leader/07 s, 006/00 m, 008/21 |
		// as of 2010-10-03 008/21 d   means database if nothing else is assigned.
//		docIds.add("112233");  // leader/07 s 008/21 d, 006/00 s 006/04 d
//		docIds.add("778899");  // leader/07 s 008/21 d, 006/00 j 006/04 p
//		docIds.add("leader07s00600j00821d"); 
//		docIds.add("321");  // 006/00 s  006/04 d

		
		assertFieldValues(displayFldName, fldVal, docIds);
		
		assertSearchResults(facetFldName, fldVal, docIds);
	}


	/**
	 * Test population of format field (values not in individual test methods)
	 */
@Test
	public final void testRemainingFormats() 
			throws IOException, ParserConfigurationException, SAXException 
	{
        // map/globe
		assertDocHasFieldValue("leader06e", displayFldName, "Map/Globe"); 
		assertDocHasFieldValue("leader06f", displayFldName, Format.MAP_GLOBE.toString()); 
		// image
		String imgVal = "Image";
		assertDocHasFieldValue("leader06k00833i", displayFldName, imgVal); 
		assertDocHasFieldValue("leader06k00833k", displayFldName, imgVal); 
		assertDocHasFieldValue("leader06k00833p", displayFldName, imgVal); 
		assertDocHasFieldValue("leader06k00833s", displayFldName, imgVal); 
		assertDocHasFieldValue("leader06k00833t", displayFldName, imgVal); 
		// video
		assertDocHasFieldValue("leader06g00833m", displayFldName, "Video"); 
		assertDocHasFieldValue("leader06g00833v", displayFldName, Format.VIDEO.toString()); 
		// audio - non-music
		assertDocHasFieldValue("leader06i", displayFldName, "Sound Recording"); 
		// music - audio
		assertDocHasFieldValue("leader06j", displayFldName, "Music - Recording"); 
		// music - score
		assertDocHasFieldValue("leader06c", displayFldName, "Music - Score"); 
		assertDocHasFieldValue("leader06d", displayFldName, Format.MUSIC_SCORE.toString()); 
		assertDocHasFieldValue("245hmicroform", displayFldName, Format.MUSIC_SCORE.toString()); 
		// manuscript/archive
		assertDocHasFieldValue("leader06b", displayFldName, "Manuscript/Archive"); 
		assertDocHasFieldValue("leader06p", displayFldName, Format.MANUSCRIPT_ARCHIVE.toString()); 
		// thesis
		assertDocHasFieldValue("502", displayFldName, "Thesis"); 
		// computer file
		assertDocHasFieldValue("leader06m00826a", displayFldName, "Computer File"); 
		// microfilm
		assertDocHasFieldValue("245hmicroform", displayFldName, "Microformat"); 
		
		String scoreVal = "\"" + "Music - Score" + "\"";
		Set<String> docIds = new HashSet<String>();
		docIds.add("leader06c");
		docIds.add("leader06d");
		docIds.add("245hmicroform");
		assertSearchResults(facetFldName, scoreVal, docIds);		
	}


	/**
	 * test format population based on ALPHANUM field values from 999
	 */
@Test
	public final void testFormatsFrom999()
			throws IOException, ParserConfigurationException, SAXException
	{
		// test formats assigned by strings in ALPHANUM call numbers
		String testFilePath = testDataParentPath + File.separator + "callNumberTests.mrc";
		
		String microVal = Format.MICROFORMAT.toString();
		// 999 ALPHANUM starting with MFLIM
		solrFldMapTest.assertSolrFldValue(testFilePath, "1261173", displayFldName, microVal);
		// 999 ALPHANUM starting with MFICHE
		solrFldMapTest.assertSolrFldValue(testFilePath, "mfiche", displayFldName, microVal);

		// 999 ALPHANUM starting with MCD
		solrFldMapTest.assertSolrFldValue(testFilePath, "1234673", displayFldName, Format.MUSIC_RECORDING.toString());
	}


}
