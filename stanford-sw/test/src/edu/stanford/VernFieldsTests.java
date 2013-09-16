package edu.stanford;

import java.io.*;
import java.util.HashSet;
import java.util.Set;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.solr.client.solrj.SolrServerException;
import org.junit.*;
import org.marc4j.marc.*;
import org.xml.sax.SAXException;

/**
 * junit4 tests for Stanford University's handling of vernacular fields (880s)
 *  (non-search vernacular fields, that is)
 *
 * @author Naomi Dushay
 */
public class VernFieldsTests extends AbstractStanfordTest
{
	String unMatched880File = "unmatched880sTests.mrc";
	String vernNonSearchTestFile = "vernacularNonSearchTests.mrc";
    String unMatched800FilePath = testDataParentPath + File.separator + unMatched880File;
	MarcFactory factory = MarcFactory.newInstance();

@Before
	public final void setup()
	{
		mappingTestInit();
	}


	/**
	 * Test 880 field that's not used for display
	 */
@Test
	public final void testIgnored880()
			throws ParserConfigurationException, IOException, SAXException, SolrServerException
	{
		createFreshIx(vernNonSearchTestFile);
		assertSingleResult("allVern", "toc_search", "contents");
		assertDocHasNoField("allVern", "vern_toc_search");
		assertDocHasNoField("allVern", "vern_toc_display");
	}

	/**
	 * Test multiple occurrences of same field
	 */
@Test
	public final void testFieldDups()
			throws ParserConfigurationException, IOException, SAXException, SolrServerException
	{
		String fldName = "author_7xx_search";
		createFreshIx(vernNonSearchTestFile);

		assertSingleResult("two700", fldName, "\"first 700\"");

		fldName = "vern_author_7xx_search";
		assertSingleResult("two700", fldName, "\"vernacular first 700\"");
		assertSingleResult("two700", fldName, "\"vernacular second 700\"");
	}

	/**
	 * Test multiple occurrences of same subfield
	 */
@Test
	public final void testSubFieldDups()
			throws ParserConfigurationException, IOException, SAXException, SolrServerException
	{
		String fldName = "author_8xx_search";
		createFreshIx(vernNonSearchTestFile);

		assertSingleResult("DupSubflds", fldName, "\"Wellington, New Zealand\"");
		fldName = "vern_author_8xx_search";
		assertSingleResult("DupSubflds", fldName, "\"Naomi in Wellington, in New Zealand\"");
	}

	/**
	 * Test trailing punctuation removal
	 */
@Test
	public final void testTrailingPunct()
			throws ParserConfigurationException, IOException, SAXException, SolrServerException
	{
		String fldName = "author_person_display";
		createFreshIx(vernNonSearchTestFile);

		assertDocHasFieldValue("trailingPunct", fldName, "internal colon : ending period");
		assertDocHasNoFieldValue("trailingPunct", fldName, "internal colon : ending period.");
		fldName = "vern_author_person_display";

		assertDocHasNoFieldValue("trailingPunct", fldName, "vernacular internal colon : vernacular ending period.");
		assertDocHasFieldValue("trailingPunct", fldName, "vernacular internal colon : vernacular ending period");

		fldName = "title_display";
		assertDocHasFieldValue("trailingPunct", fldName, "ends in slash");
		assertDocHasNoFieldValue("trailingPunct", fldName, "ends in slash /");
		fldName = "vern_title_display";
		assertDocHasFieldValue("trailingPunct", fldName, "vernacular ends in slash");
		assertDocHasNoFieldValue("trailingPunct", fldName, "vernacular ends in slash /");
	}

	/**
	 * Test indexing of unmatched 880s with getLinkedField method
	 */
@Test
	public final void testUnmatched880sLinkedField()
			throws ParserConfigurationException, IOException, SAXException, SolrServerException
	{
		String fldName = "vern_toc_search";
	    createFreshIx(unMatched880File);

	    Set<String> docIds = new HashSet<String>();
		docIds.add("1");
		docIds.add("2");
		docIds.add("3");
		assertSearchResults(fldName, "vern505a", docIds);

		// subfields to be ignored
        assertZeroResults(fldName, "vern505b");
        assertZeroResults(fldName, "vern505c");
        assertZeroResults(fldName, "vern505g");
	}

	/**
	 * Test indexing of unmatched 880s using Stanford's getVernacular method
	 */
@Test
	public final void testUnmatched880sVernacular()
	{
		String fldName = "vern_title_uniform_display";
		solrFldMapTest.assertSolrFldValue(unMatched800FilePath, "4", fldName, "vern130a");
		solrFldMapTest.assertSolrFldValue(unMatched800FilePath, "5", fldName, "vern240a");
	}

	/**
	 * Test indexing of unmatched 880s using Stanford's getVernAllAlphaExcept method
	 */
@Test
	public final void testUnmatched880sVernAllAlphaExcept()
	{
		String fldName = "vern_topic_search";
		solrFldMapTest.assertSolrFldValue(unMatched800FilePath, "6", fldName, "vern650a");
	}

	/**
	 * Test indexing of unmatched 880s using Stanford's vernRemoveTrailingPunct method
	 */
@Test
	public final void testUnmatched880sVernRemovePunct()
	{
		String fldName = "vern_author_person_display";
		solrFldMapTest.assertSolrFldValue(unMatched800FilePath, "7", fldName, "vern100a");
	}

	/**
	 * Test population of vern_all_search
	 *   it should include ALL 880 fields
	 */
@Test
	public final void testVernCatchallField()
			throws ParserConfigurationException, IOException, SAXException, SolrServerException
	{
		String fldName = "vern_all_search";

	    // Except for 905, 920 and 986 (SW-814)
		mappingTestInit();
		Record record = factory.newRecord();
		record = addLinkedField(record, "010", 'a', "1");
		record = addLinkedField(record, "024", 'a', "2");
		record = addLinkedField(record, "100", 'a', "3");
		record = addLinkedField(record, "245", 'b', "4");
		record = addLinkedField(record, "782", 'c', "5");
		record = addLinkedField(record, "946", 'a', "6");
		record = addLinkedField(record, "666", '9', "7"); //numeric subfield - ignore
		record = addLinkedField(record, "666", '=', "8"); //non-alphnum subfield - ignore
		record = addLinkedField(record, "650", 'a', ""); // unlinked subfield - include
		// no data - ignore
		DataField df = factory.newDataField("880", ' ', ' ');
        df.addSubfield(factory.newSubfield('6', "666-9"));
        df.addSubfield(factory.newSubfield('m', ""));
        record.addVariableField(df);
		record = addLinkedField(record, "105", '\u0000', "10"); // no subfield code - ignore

		solrFldMapTest.assertSolrFldValue(record, fldName, "880a for 010 880a for 024 880a for 100 880b for 245 880c for 782 880a for 946 880a for 650");
	}


	/**
	 * add a linked field with one subfield to a Marc Record object
	 * @param record the marc record object
	 * @param tag the field (e.g. "245")
	 * @param subfld the subfld character (e.g. 'a')
	 * @param linkNum the linkage number for the linked 880 field
	 * @return the marc record object with the new fields (regular + linked) added
	 */
	private Record addLinkedField(Record record, String tag, char subfld, String linkNum)
	{
	    DataField df = factory.newDataField(tag, ' ', ' ');
        df.addSubfield(factory.newSubfield(subfld, tag + "a"));
        df.addSubfield(factory.newSubfield('6', "880-" + linkNum));
        record.addVariableField(df);
        df = factory.newDataField("880", ' ', ' ');
        df.addSubfield(factory.newSubfield('6', tag + "-" + linkNum));
        df.addSubfield(factory.newSubfield(subfld, "880" + subfld + " for " + tag));
        record.addVariableField(df);
	    return record;
	}


	/**
	 * Test right to left concatenation of subfields for right to left languages
	 */
// @Test
	public final void testR2LConcat()
			throws ParserConfigurationException, IOException, SAXException, SolrServerException
	{
		String fldName = "title_full_display";
		createFreshIx(vernNonSearchTestFile);

		assertDocHasFieldValue("RtoL", fldName, "a is for alligator / c is for crocodile, 1980");
		fldName = "vern_title_full_display";
		assertDocHasFieldValue("RtoL", fldName, "1980 ,crocodile for is c / alligator for is a");


		fldName = "author_person_full_display";
		assertDocHasFieldValue("RtoL2", fldName, "LTR a : LTR b, LTR c");
		fldName = "vern_author_person_full_display";
		assertDocHasFieldValue("RtoL2", fldName, "vern (RTL?) c (third) ,vern (RTL?) b (second) : vern (RTL?) a (first)");
		fldName = "title_full_display";
		assertDocHasFieldValue("RtoL2", fldName, "a first / c second, 1980");
		fldName = "vern_title_full_display";
		assertDocHasFieldValue("RtoL2", fldName, "1980 ,vern (RTL?) c followed by number / vern (RTL?) a");
	}

	/**
	 * Test punctuation changes for right to left concatenation of subfields for
	 * right to left languages
	 */
// @Test
	public final void testR2LConcatPunct()
			throws ParserConfigurationException, IOException, SAXException
	{
		org.junit.Assert.fail("not yet implemented");
		String fldName = "";
		assertDocHasFieldValue("2099904", fldName, "");
	}

	/**
	 * Test right to left concatenation of subfields for hebrew
	 */
// @Test
	public final void testHebrew()
			throws ParserConfigurationException, IOException, SAXException, SolrServerException
	{
		String fldName = "title_full_display";
		createFreshIx(vernNonSearchTestFile);

		assertDocHasFieldValue("hebrew1", fldName, "Alef bet shel Yahadut.");
		fldName = "vern_title_full_display";

//PrintStream ps = new PrintStream(System.out, true, "UTF-16");
//ps.println("DEBUG:  vern_title_full_display contains: " + getDocument("hebrew1").getValues("vern_full_title_display")[0]);
								assertDocHasFieldValue("hebrew1", fldName, "אל״ף בי״ת של יהדות הלל צייטלין ; תירגם וערך מנחם ברש־רועי /");
								assertDocHasFieldValue("hebrew1", fldName, "אל״ף בי״ת של יהדות / הלל צייטלין ; תירגם וערך מנחם ברש־רועי");

		fldName = "publication_display";
		assertDocHasFieldValue("hebrew1", fldName, "Yerushalayim : Mosad ha-Rav Ḳuḳ, c1983");
		fldName = "vern_publication_display";
								assertDocHasFieldValue("hebrew1", fldName, "c1983 ,ירושלים : מוסד הרב קוק");
	}

}
