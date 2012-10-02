/*
 * Copyright (c) 2012.  The Board of Trustees of the Leland Stanford Junior University. All rights reserved.
 *
 * Redistribution and use of this distribution in source and binary forms, with or without modification, are permitted provided that: The above copyright notice and this permission notice appear in all copies and supporting documentation; The name, identifiers, and trademarks of The Board of Trustees of the Leland Stanford Junior University are not used in advertising or publicity without the express prior written permission of The Board of Trustees of the Leland Stanford Junior University; Recipients acknowledge that this distribution is made available as a research courtesy, "as is", potentially with defects, without any obligation on the part of The Board of Trustees of the Leland Stanford Junior University to provide support, services, or repair;
 *
 * THE BOARD OF TRUSTEES OF THE LELAND STANFORD JUNIOR UNIVERSITY DISCLAIMS ALL WARRANTIES, EXPRESS OR IMPLIED, WITH REGARD TO THIS SOFTWARE, INCLUDING WITHOUT LIMITATION ALL IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE, AND IN NO EVENT SHALL THE BOARD OF TRUSTEES OF THE LELAND STANFORD JUNIOR UNIVERSITY BE LIABLE FOR ANY SPECIAL, INDIRECT OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN ACTION OF CONTRACT, TORT (INCLUDING NEGLIGENCE) OR STRICT LIABILITY, ARISING OUT OF OR IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
*/
package edu.stanford;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

import javax.xml.parsers.ParserConfigurationException;
import org.xml.sax.SAXException;
import org.junit.*;
import org.marc4j.marc.*;

import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;

import edu.stanford.enumValues.PubDateGroup;

/**
 * integration/functional tests for publication fields (from 260, 264, etc.)
 * @author Naomi Dushay
 */
public class PublicationTests extends AbstractStanfordTest
{
	String publTestFilePath = testDataParentPath + File.separator + "publicationTests.mrc";

@Before
	public final void setup()
	{
		mappingTestInit();
	}

	/**
	 * integration test: the publication fields (not pub date fields)
	 */
@Test
	public void testPublicationFieldsInIx()
			throws ParserConfigurationException, IOException, SAXException, SolrServerException
	{
		String fldName = "pub_search";
		createFreshIx("publicationTests.mrc");
		// test searching
		assertSingleResult("260aunknown", fldName, "Insight");
		assertSingleResult("260bunknown", fldName, "victoria"); // downcased
		// these codes should be skipped
		assertDocHasNoField("260abunknown", fldName);  // 260a s.l, 260b s.n.
		assertZeroResults(fldName, "s.l.");
		assertZeroResults(fldName, "s.n.");

		fldName = "vern_pub_search";
		// searching
		Set<String> docIds = new HashSet<String>();
		docIds.add("vern260abc");
		docIds.add("vern260abcg");
		assertSearchResults(fldName, "vern260a", docIds);

		fldName = "pub_country";
		// searching
		// these codes should be skipped
		assertDocHasNoField("008vp", fldName);  // "Various places"
		assertDocHasNoField("008xx", fldName);  // "No place, unknown, or undetermined"
	}


	/**
	 * functional test: assure publication field is populated correctly
	 */
@Test
	public void testPublication()
	{
		String fldName = "pub_search";

		// 260ab
		solrFldMapTest.assertSolrFldValue(publTestFilePath, "260ababc", fldName, "Paris : Gauthier-Villars ; Chicago : University of Chicago Press");
		solrFldMapTest.assertSolrFldValue(publTestFilePath, "260abbc", fldName, "Washington, D.C. : first b : second b U.S. G.P.O.");
		solrFldMapTest.assertSolrFldValue(publTestFilePath, "260ab3", fldName, "London : Vogue");
		solrFldMapTest.assertSolrFldHasNoValue(publTestFilePath, "260crightbracket", fldName, "[i.e. Bruxelles : Moens");
		solrFldMapTest.assertSolrFldValue(publTestFilePath, "260crightbracket", fldName, "i.e. Bruxelles : Moens");

		// 260a contains s.l. (unknown - sin location, presumably)
		solrFldMapTest.assertSolrFldHasNoValue(publTestFilePath, "260aunknown", fldName, "[S.l.] : Insight Press");
		solrFldMapTest.assertSolrFldValue(publTestFilePath, "260aunknown", fldName, "Insight Press");
		solrFldMapTest.assertSolrFldHasNoValue(publTestFilePath, "260abaslbc", fldName, "[Philadelphia] : Some name [s.l.] : another name");
		solrFldMapTest.assertSolrFldValue(publTestFilePath, "260abaslbc", fldName, "[Philadelphia] : Some name another name");

		// 260b contains s.n. (unknown - sin name, presumably)
		solrFldMapTest.assertSolrFldHasNoValue(publTestFilePath, "260bunknown", fldName, "Victoria, B.C. : [s.n.]");
		solrFldMapTest.assertSolrFldValue(publTestFilePath, "260bunknown", fldName, "Victoria, B.C.");

		solrFldMapTest.assertSolrFldHasNoValue(publTestFilePath, "260abunknown", fldName, "[S.l. : s.n.");
		solrFldMapTest.assertSolrFldHasNoValue(publTestFilePath, "260abunknown", fldName, "S.l. : s.n.");
	}


	/**
	 * functional test: assure pub_search field is populated from 264 correctly
	 */
@Test
	public void test264PubSearch()
	{
		MarcFactory factory = MarcFactory.newInstance();
		Record record = factory.newRecord();
        DataField df = factory.newDataField("264", ' ', ' ');
        df.addSubfield(factory.newSubfield('a', "264a"));
        record.addVariableField(df);
        df = factory.newDataField("264", ' ', ' ');
        df.addSubfield(factory.newSubfield('b', "264b"));
        record.addVariableField(df);
        df = factory.newDataField("264", ' ', ' ');
        df.addSubfield(factory.newSubfield('c', "264c"));
        record.addVariableField(df);
        df = factory.newDataField("264", ' ', ' ');
        df.addSubfield(factory.newSubfield('a', "264a"));
        df.addSubfield(factory.newSubfield('b', "264b"));
        record.addVariableField(df);

		String fldName = "pub_search";
        solrFldMapTest.assertSolrFldHasNumValues(record, fldName, 3);
        solrFldMapTest.assertSolrFldValue(record, fldName, "264a");
        solrFldMapTest.assertSolrFldValue(record, fldName, "264b");
        solrFldMapTest.assertSolrFldValue(record, fldName, "264a 264b");

        // and it should still work for 260s also
        df = factory.newDataField("260", ' ', ' ');
        df.addSubfield(factory.newSubfield('a', "260a"));
        record.addVariableField(df);
        solrFldMapTest.assertSolrFldHasNumValues(record, fldName, 4);
        solrFldMapTest.assertSolrFldValue(record, fldName, "260a");
	}


	/**
	 * functional test: assure pub_search field ignores the unknown-ish phrases
	 */
@Test
	public void test264IgnoreUnknownPubSearch()
	{
		MarcFactory factory = MarcFactory.newInstance();
		Record record = factory.newRecord();
	    DataField df = factory.newDataField("264", ' ', ' ');
	    df.addSubfield(factory.newSubfield('a', "[Place of publication not identified] :"));
	    df.addSubfield(factory.newSubfield('b', "b1"));
	    record.addVariableField(df);
	    df = factory.newDataField("264", ' ', ' ');
	    df.addSubfield(factory.newSubfield('a', "[Place of Production not identified]"));
	    df.addSubfield(factory.newSubfield('b', "b2"));
	    record.addVariableField(df);
	    df = factory.newDataField("264", ' ', ' ');
	    df.addSubfield(factory.newSubfield('a', "Place of manufacture Not Identified"));
	    df.addSubfield(factory.newSubfield('b', "b3"));
	    record.addVariableField(df);
	    df = factory.newDataField("264", ' ', ' ');
	    df.addSubfield(factory.newSubfield('a', "[Place of distribution not identified]"));
	    record.addVariableField(df);

		String fldName = "pub_search";
	    solrFldMapTest.assertSolrFldHasNumValues(record, fldName, 3);
	    solrFldMapTest.assertSolrFldValue(record, fldName, "b1");
	    solrFldMapTest.assertSolrFldValue(record, fldName, "b2");
	    solrFldMapTest.assertSolrFldValue(record, fldName, "b3");

		record = factory.newRecord();
	    df = factory.newDataField("264", ' ', ' ');
	    df.addSubfield(factory.newSubfield('a', "a1"));
	    df.addSubfield(factory.newSubfield('b', "[publisher not identified], "));
	    record.addVariableField(df);
	    df = factory.newDataField("264", ' ', ' ');
	    df.addSubfield(factory.newSubfield('a', "a2"));
	    df.addSubfield(factory.newSubfield('b', "[Producer not identified]"));
	    record.addVariableField(df);
	    df = factory.newDataField("264", ' ', ' ');
	    df.addSubfield(factory.newSubfield('a', "a3"));
	    df.addSubfield(factory.newSubfield('b', "Manufacturer Not Identified"));
	    record.addVariableField(df);
	    df = factory.newDataField("264", ' ', ' ');
	    df.addSubfield(factory.newSubfield('b', "[distributor not identified]"));
	    record.addVariableField(df);

	    solrFldMapTest.assertSolrFldHasNumValues(record, fldName, 3);
	    solrFldMapTest.assertSolrFldValue(record, fldName, "a1");
	    solrFldMapTest.assertSolrFldValue(record, fldName, "a2");
	    solrFldMapTest.assertSolrFldValue(record, fldName, "a3");
	}


	/**
	 * functional test: assure publication field is populated correctly
	 */
@Test
	public void testVernPublication()
	{
		String fldName = "vern_pub_search";

		// 260ab from 880
		solrFldMapTest.assertSolrFldValue(publTestFilePath, "vern260abc", fldName, "vern260a : vern260b");
		solrFldMapTest.assertSolrFldValue(publTestFilePath, "vern260abcg", fldName, "vern260a : vern260b");

		MarcFactory factory = MarcFactory.newInstance();
		Record record = factory.newRecord();
        DataField df = factory.newDataField("264", ' ', ' ');
        df.addSubfield(factory.newSubfield('a', "264a"));
        df.addSubfield(factory.newSubfield('6', "880-01"));
        record.addVariableField(df);
        df = factory.newDataField("880", ' ', ' ');
        df.addSubfield(factory.newSubfield('6', "264-01"));
        df.addSubfield(factory.newSubfield('a', "880a for 264"));
        df.addSubfield(factory.newSubfield('b', "880b for 264"));
        df.addSubfield(factory.newSubfield('c', "880c for 264"));
        record.addVariableField(df);

        solrFldMapTest.assertSolrFldHasNumValues(record, fldName, 1);
        solrFldMapTest.assertSolrFldValue(record, fldName, "880a for 264 880b for 264");
	}


	/**
	 * functional test: assure publication country field is populated correctly
	 */
@Test
	public void testPublicationCountry()
	{
		String fldName = "pub_country";

		// 008[15-17]  via translation map
		solrFldMapTest.assertSolrFldValue(publTestFilePath, "008mdu", fldName, "Maryland, United States");
		solrFldMapTest.assertSolrFldValue(publTestFilePath, "008ja", fldName, "Japan");
	}


	/**
	 * functional test: assure pub_date field ignores the unknown-ish phrases
	 */
@Test
	public void test264IgnoreUnknownPubDate()
	{
		MarcFactory factory = MarcFactory.newInstance();
		Record record = factory.newRecord();
	    DataField df = factory.newDataField("264", ' ', ' ');
	    df.addSubfield(factory.newSubfield('c', "[Date of publication not identified] :"));
	    record.addVariableField(df);
	    df = factory.newDataField("264", ' ', ' ');
	    df.addSubfield(factory.newSubfield('c', "[Date of Production not identified]"));
	    record.addVariableField(df);
	    df = factory.newDataField("264", ' ', ' ');
	    df.addSubfield(factory.newSubfield('c', "Date of manufacture Not Identified"));
	    record.addVariableField(df);
	    df = factory.newDataField("264", ' ', ' ');
	    df.addSubfield(factory.newSubfield('c', "[Date of distribution not identified]"));
	    record.addVariableField(df);
	    solrFldMapTest.assertNoSolrFld(record, "pub_date");
	}

	/**
	 * integration test: assure pub dates later than current year +1 are ignored
	 */
@Test
	public void testPubDateTooLate()
			throws ParserConfigurationException, IOException, SAXException, SolrServerException
	{
		String fldName = "pub_date";
		createFreshIx("pubDateTests.mrc");
		assertZeroResults(fldName, "9999");
		assertZeroResults(fldName, "6666");
		assertZeroResults(fldName, "22nd century");
		assertZeroResults(fldName, "23rd century");
		assertZeroResults(fldName, "24th century");
		assertZeroResults(fldName, "8610s");

		MarcFactory factory = MarcFactory.newInstance();
		Record record = factory.newRecord();
	    DataField df = factory.newDataField("264", ' ', ' ');
	    df.addSubfield(factory.newSubfield('c', "9999"));
	    record.addVariableField(df);
	    df = factory.newDataField("264", ' ', ' ');
	    df.addSubfield(factory.newSubfield('c', "6666"));
	    record.addVariableField(df);
	    df = factory.newDataField("264", ' ', ' ');
	    df.addSubfield(factory.newSubfield('c', "22nd century"));
	    record.addVariableField(df);
	    df = factory.newDataField("264", ' ', ' ');
	    df.addSubfield(factory.newSubfield('c', "8610s"));
	    record.addVariableField(df);
	    solrFldMapTest.assertNoSolrFld(record, "pub_date");
	}


	/**
	 * integration test: assure pub dates of < 500 are ignored
	 */
@Test
	public void testPubDateTooEarly()
			throws ParserConfigurationException, IOException, SAXException, SolrServerException
	{
		String fldName = "pub_date";
		createFreshIx("pubDateTests.mrc");
		assertZeroResults(fldName, "0000");
		assertZeroResults(fldName, "0019");
		assertZeroResults(fldName, "0059");
		assertZeroResults(fldName, "0197");
		assertZeroResults(fldName, "0204");

		MarcFactory factory = MarcFactory.newInstance();
		Record record = factory.newRecord();
	    DataField df = factory.newDataField("264", ' ', ' ');
	    df.addSubfield(factory.newSubfield('c', "0000"));
	    record.addVariableField(df);
	    df = factory.newDataField("264", ' ', ' ');
	    df.addSubfield(factory.newSubfield('c', "0036"));
	    record.addVariableField(df);
	    df = factory.newDataField("264", ' ', ' ');
	    df.addSubfield(factory.newSubfield('c', "0197"));
	    record.addVariableField(df);
	    df = factory.newDataField("264", ' ', ' ');
	    df.addSubfield(factory.newSubfield('c', "0204"));
	    record.addVariableField(df);
	    solrFldMapTest.assertNoSolrFld(record, "pub_date");
	}

	/**
	 * functional test: auto-correction of pub date in 008 by checking value in 260c
	 */
@Test
	public void testPubDateAutoCorrect()
	{
		String fldName = "pub_date";
		String testFilePath = testDataParentPath + File.separator + "pubDateTests.mrc";

		solrFldMapTest.assertSolrFldHasNoValue(testFilePath, "pubDate0059", fldName, "0059");
		solrFldMapTest.assertSolrFldValue(testFilePath, "pubDate0059", fldName, "2005");

		solrFldMapTest.assertSolrFldHasNoValue(testFilePath, "pubDate0197-1", fldName, "0197");
		solrFldMapTest.assertSolrFldHasNoValue(testFilePath, "pubDate0197-1", fldName, "1970s");
		solrFldMapTest.assertSolrFldValue(testFilePath, "pubDate0197-1", fldName, "1970");

		solrFldMapTest.assertSolrFldHasNoValue(testFilePath, "pubDate0197-2", fldName, "0197");
		solrFldMapTest.assertSolrFldHasNoValue(testFilePath, "pubDate0197-2", fldName, "1970s");
		solrFldMapTest.assertSolrFldValue(testFilePath, "pubDate0197-2", fldName, "1970");

		// correct
		solrFldMapTest.assertSolrFldValue(testFilePath, "pubDate0500", fldName, "0500");
		solrFldMapTest.assertSolrFldValue(testFilePath, "pubDate0801", fldName, "0801");
		solrFldMapTest.assertSolrFldValue(testFilePath, "pubDate0960", fldName, "0960");
		solrFldMapTest.assertSolrFldValue(testFilePath, "pubDate0963", fldName, "0963");

		solrFldMapTest.assertSolrFldHasNoValue(testFilePath, "pubDate0204", fldName, "0204");
		solrFldMapTest.assertSolrFldValue(testFilePath, "pubDate0204", fldName, "2004");

		solrFldMapTest.assertSolrFldHasNoValue(testFilePath, "pubDate0019", fldName, "0019");

		// TODO: yeah, i wish ...
//		solrFldMapTest.assertSolrFldValue(testFilePath, "pubDate0019", fldName, "20th century");
//		solrFldMapTest.assertSolrFldHasNoValue(testFilePath, "pubDate0965", fldName, "0965");
//		solrFldMapTest.assertSolrFldValue(testFilePath, "pubDate0965", fldName, "1965");
//		solrFldMapTest.assertSolrFldHasNoValue(testFilePath, "pubDate0980", fldName, "0980");
//		solrFldMapTest.assertSolrFldValue(testFilePath, "pubDate0980", fldName, "1980");
//		solrFldMapTest.assertSolrFldHasNoValue(testFilePath, "pubDate0999", fldName, "0999");
//		solrFldMapTest.assertSolrFldValue(testFilePath, "pubDate0999", fldName, "1999");

		solrFldMapTest.assertNoSolrFld(testFilePath, "410024", fldName);
	}



	/**
	 * functional test: assure pub dates later than current year +1 are ignored
	 */
@Test
	public void test264PubDate()
	{
		String fldName = "pub_date";
		MarcFactory factory = MarcFactory.newInstance();
		Record record = factory.newRecord();
		record.addVariableField(factory.newControlField("008", "       0000"));
	    DataField df = factory.newDataField("264", ' ', ' ');
	    df.addSubfield(factory.newSubfield('c', "2002"));
	    record.addVariableField(df);
	    solrFldMapTest.assertSolrFldValue(record, fldName, "2002");

	    // preceding copyright symbol
		record = factory.newRecord();
		record.addVariableField(factory.newControlField("008", "       0000"));
	    df = factory.newDataField("264", ' ', ' ');
	    df.addSubfield(factory.newSubfield('c', "©2002"));
	    record.addVariableField(df);
	    solrFldMapTest.assertSolrFldValue(record, fldName, "2002");

	    // preceding publication symbol
		record = factory.newRecord();
		record.addVariableField(factory.newControlField("008", "       0000"));
	    df = factory.newDataField("264", ' ', ' ');
	    df.addSubfield(factory.newSubfield('c', "Ⓟ1983 "));
	    record.addVariableField(df);
	    solrFldMapTest.assertSolrFldValue(record, fldName, "1983");

	    // square brackets
		record = factory.newRecord();
		record.addVariableField(factory.newControlField("008", "       0000"));
	    df = factory.newDataField("264", ' ', ' ');
	    df.addSubfield(factory.newSubfield('c', "[2011]"));
	    record.addVariableField(df);
	    solrFldMapTest.assertSolrFldValue(record, fldName, "2011");

	    // square brackets with question mark
		record = factory.newRecord();
		record.addVariableField(factory.newControlField("008", "       0000"));
	    df = factory.newDataField("264", ' ', ' ');
	    df.addSubfield(factory.newSubfield('c', "[1940?]"));
	    record.addVariableField(df);
	    solrFldMapTest.assertSolrFldValue(record, fldName, "1940");

	    // preceding text
		record = factory.newRecord();
		record.addVariableField(factory.newControlField("008", "       0000"));
	    df = factory.newDataField("264", ' ', ' ');
	    df.addSubfield(factory.newSubfield('c', "copyright 2005"));
	    record.addVariableField(df);
// FIXME
//	    solrFldMapTest.assertSolrFldValue(record, fldName, "2005");

	    // two values - take the first one?
		record = factory.newRecord();
		record.addVariableField(factory.newControlField("008", "       0000"));
	    df = factory.newDataField("264", ' ', ' ');
	    df.addSubfield(factory.newSubfield('c', "[2011]"));
	    record.addVariableField(df);
	    df = factory.newDataField("264", ' ', '4');
	    df.addSubfield(factory.newSubfield('c', "©2009"));
	    record.addVariableField(df);
	    solrFldMapTest.assertSolrFldValue(record, fldName, "2011");

		record = factory.newRecord();
		record.addVariableField(factory.newControlField("008", "       0000"));
	    df = factory.newDataField("264", ' ', '2');
	    df.addSubfield(factory.newSubfield('c', "2012."));
	    record.addVariableField(df);
	    df = factory.newDataField("264", ' ', '4');
	    df.addSubfield(factory.newSubfield('c', "©2009"));
	    record.addVariableField(df);
	    solrFldMapTest.assertSolrFldValue(record, fldName, "2012");

	    // test  264 handling if not parseable

	    // test  autocorrect on 264
		record = factory.newRecord();
		record.addVariableField(factory.newControlField("008", "       0000"));
	    df = factory.newDataField("264", ' ', ' ');
	    df.addSubfield(factory.newSubfield('c', "197?"));
	    record.addVariableField(df);
	    solrFldMapTest.assertSolrFldValue(record, fldName, "1970");

		record = factory.newRecord();
		record.addVariableField(factory.newControlField("008", "       0000"));
	    df = factory.newDataField("264", ' ', ' ');
	    df.addSubfield(factory.newSubfield('c', "[197?]"));
	    record.addVariableField(df);
	    solrFldMapTest.assertSolrFldValue(record, fldName, "1970");

		record = factory.newRecord();
		record.addVariableField(factory.newControlField("008", "       0000"));
	    df = factory.newDataField("264", ' ', ' ');
	    df.addSubfield(factory.newSubfield('c', "0019"));
	    record.addVariableField(df);
	    solrFldMapTest.assertNoSolrFld(record, fldName);

	    // test:  if both 260 and 264, take 264c if 2nd indicator is 1, else take 260c
		record = factory.newRecord();
		record.addVariableField(factory.newControlField("008", "       0000"));
	    df = factory.newDataField("260", ' ', ' ');
	    df.addSubfield(factory.newSubfield('c', "1260"));
	    record.addVariableField(df);
	    df = factory.newDataField("264", ' ', '1');
	    df.addSubfield(factory.newSubfield('c', "1264"));
	    record.addVariableField(df);
	    solrFldMapTest.assertSolrFldValue(record, fldName, "1264");

		record = factory.newRecord();
		record.addVariableField(factory.newControlField("008", "       0000"));
	    df = factory.newDataField("260", ' ', ' ');
	    df.addSubfield(factory.newSubfield('c', "1560"));
	    record.addVariableField(df);
	    df = factory.newDataField("264", ' ', ' ');
	    df.addSubfield(factory.newSubfield('c', "1564"));
	    record.addVariableField(df);
	    solrFldMapTest.assertSolrFldValue(record, fldName, "1560");
	}


	/**
	 * integration test: pub_date and pub_date_search field properties and searching.
	 */
@Test
	public final void testPubDateFieldsInIx()
			throws ParserConfigurationException, IOException, SAXException, SolrServerException
	{
		createFreshIx("pubDateTests.mrc");
		// for facet
		String fldName = "pub_date";
		pubDateSearchTests(fldName);

		fldName = "pub_date_search";
		pubDateSearchTests(fldName);
	}


	/**
	 * integration test: pub_date_i
	 */
@Test
	public final void testPubDateForSlider()
			throws ParserConfigurationException, IOException, SAXException, SolrServerException
	{
		createFreshIx("pubDateTests.mrc");
		String fldName = "pub_date_i";
		Set<String> docIds = new HashSet<String>();

		assertSingleResult("zpubDate2010", fldName, "2010");

		// multiple dates
		assertSingleResult("pubDate195u", fldName, "1957");
		assertSingleResult("pubDate195u", fldName, "1982");
		docIds.add("pubDate195u");
		docIds.add("bothDates008");
		assertSearchResults(fldName, "1964", docIds);
		docIds.remove("bothDates008");
		docIds.add("s195u");
		assertSearchResults(fldName, "1950", docIds);

		// future dates are ignored/skipped
		assertZeroResults(fldName, "6666");
		assertZeroResults(fldName, "8610");
		assertZeroResults(fldName, "9999");

		// dates before 500 are ignored/skipped
		assertZeroResults(fldName, "0000");
		assertZeroResults(fldName, "0019");

		// corrected values
		docIds.clear();
		docIds.add("pubDate0059");
		docIds.add("j2005");
		assertSearchResults(fldName, "2005", docIds);
		docIds.clear();
		docIds.add("pubDate195u");
		docIds.add("pubDate0197-1");
		docIds.add("pubDate0197-2");
		assertSearchResults(fldName, "1970", docIds);
	}


	/**
	 * integration test: pub_date_sort field population and ascending sort.
	 */
@Test
	public final void testPubDateSortAsc()
			throws ParserConfigurationException, IOException, SAXException, InvocationTargetException, ClassNotFoundException, InstantiationException, IllegalAccessException, NoSuchMethodException, SolrServerException
	{
		createFreshIx("pubDateTests.mrc");

		// list of doc ids in correct publish date sort order
		List<String> expectedOrderList = new ArrayList<String>(50);

		expectedOrderList.add("pubDate00uu");   // "1st century"
		expectedOrderList.add("pubDate01uu");   // "2nd century"
		expectedOrderList.add("pubDate02uu");   // "3rd century"
		expectedOrderList.add("pubDate03uu");   // "4th century"
		expectedOrderList.add("pubDate0500");   // 0500
		expectedOrderList.add("pubDate08uu");   // "9th century"
		expectedOrderList.add("pubDate0801");   // 0801
		expectedOrderList.add("pubDate09uu");   // "10th century"
		expectedOrderList.add("pubDate0960");   // 0960
		expectedOrderList.add("pubDate0963");   // 0963
		expectedOrderList.add("pubDate10uu");   // "11th century"
		expectedOrderList.add("pubDate11uu");   // "12th century"
		expectedOrderList.add("pubDate12uu");   // "13th century"
		expectedOrderList.add("pubDate13uu");   // "14th century"
		expectedOrderList.add("pubDate16uu");   // "17th century"
		expectedOrderList.add("p19uu");   // "20th century"
		expectedOrderList.add("pubDate19uu");   // "20th century"
		expectedOrderList.add("s190u");   // "1900s"
		expectedOrderList.add("r1900");   // "1900"
		expectedOrderList.add("pubDate195u");   // "1950s"
		expectedOrderList.add("s195u");   // "1950s"
		expectedOrderList.add("g1958");   // "1958"
		expectedOrderList.add("w1959");   // "1959"ˇ
		expectedOrderList.add("bothDates008");  // "1964"
		expectedOrderList.add("pubDate0197-1");  // 1970
		expectedOrderList.add("contRes");       // "1984"
		expectedOrderList.add("y1989");   // "1989"
		expectedOrderList.add("b199u");   // "1990s"
		expectedOrderList.add("k1990");   // "1990"
		expectedOrderList.add("m1991");   // "1991"
		expectedOrderList.add("e1997");   // "1997"
		expectedOrderList.add("c1998");   // "1998"
		expectedOrderList.add("w1999");   // "1999"
		expectedOrderList.add("o20uu");   // "21st century"
		expectedOrderList.add("pubDate20uu");   // "21st century"
		expectedOrderList.add("x200u");   // "2000s"
		expectedOrderList.add("f2000");   // "2000"
		expectedOrderList.add("firstDateOnly008");  // "2000"
		expectedOrderList.add("q2001");   // "2001"
		expectedOrderList.add("pubDate0204");  // 2004
		expectedOrderList.add("pubDate0059");  // 2005
		expectedOrderList.add("z2006");   // "2006"
		expectedOrderList.add("v2007");   // "2007"
		expectedOrderList.add("b2008");   // "2008"
		expectedOrderList.add("z2009");   // "2009"
		expectedOrderList.add("zpubDate2010");   // "2010"

		// invalid/missing dates are designated as last in solr schema file
		//  they are in order of occurrence in the raw data
		expectedOrderList.add("pubDate0000");
		expectedOrderList.add("pubDate0019");
		expectedOrderList.add("pubDate1uuu");
		expectedOrderList.add("pubDate6666");
		expectedOrderList.add("pubDate9999");

		// get search results sorted by pub_date_sort field
        SolrDocumentList results = getAscSortDocs("collection", "sirsi", "pub_date_sort");

        SolrDocument firstDoc = results.get(0);
		assertTrue("9999 pub date should not sort first", (String) firstDoc.getFirstValue(docIDfname) != "pubDate9999");

		// we know we have documents that are not in the expected order list,
		//  so we must allow for gaps
		// author_sort isn't stored, so we must look at id field
		int expDocIx = -1;
		for (SolrDocument doc : results)
		{
			if (expDocIx < expectedOrderList.size() - 1)
			{
				String resultDocId = (String) doc.getFirstValue(docIDfname);
				// is it a match?
                if (resultDocId.equals(expectedOrderList.get(expDocIx + 1)))
                	expDocIx++;
			}
			else break;  // we found all the documents in the expected order list
		}

		if (expDocIx != expectedOrderList.size() - 1)
		{
			String lastCorrDocId = expectedOrderList.get(expDocIx);
			fail("Publish Date Sort Order is incorrect.  Last correct document was " + lastCorrDocId);
		}
	}


	/**
	 * integration test: pub date descending sort should start with oldest and go to newest
	 *  (missing dates sort order tested in another method)
	 */
@Test
	public void testPubDateSortDesc()
			throws ParserConfigurationException, IOException, SAXException, NoSuchMethodException, InstantiationException, InvocationTargetException, ClassNotFoundException, IllegalAccessException, SolrServerException
	{
		createFreshIx("pubDateTests.mrc");

		// list of doc ids in correct publish date sort order
		List<String> expectedOrderList = new ArrayList<String>(50);

		expectedOrderList.add("zpubDate2010");   // "2010"
		expectedOrderList.add("z2009");   // "2009"
		expectedOrderList.add("b2008");   // "2008"
		expectedOrderList.add("v2007");   // "2007"
		expectedOrderList.add("z2006");   // "2006"
		expectedOrderList.add("pubDate0059");  // 2005
		expectedOrderList.add("pubDate0204");  // 2004
		expectedOrderList.add("q2001");   // "2001"
		expectedOrderList.add("f2000");   // "2000"
		expectedOrderList.add("firstDateOnly008");  // "2000"
		expectedOrderList.add("x200u");   // "2000s"
		expectedOrderList.add("o20uu");   // "21st century"
		expectedOrderList.add("pubDate20uu");   // "21st century"
		expectedOrderList.add("w1999");   // "1999"
		expectedOrderList.add("c1998");   // "1998"
		expectedOrderList.add("e1997");   // "1997"
		expectedOrderList.add("m1991");   // "1991"
		expectedOrderList.add("k1990");   // "1990"
		expectedOrderList.add("b199u");   // "1990s"
		expectedOrderList.add("y1989");   // "1989"
		expectedOrderList.add("contRes");       // "1984"
		expectedOrderList.add("pubDate0197-1");  // 1970
		expectedOrderList.add("bothDates008");  // "1964"
		expectedOrderList.add("w1959");   // "1959"ˇ
		expectedOrderList.add("g1958");   // "1958"
		expectedOrderList.add("pubDate195u");   // "1950s"
		expectedOrderList.add("s195u");   // "1950s"
		expectedOrderList.add("r1900");   // "1900"
		expectedOrderList.add("s190u");   // "1900s"
		expectedOrderList.add("p19uu");   // "20th century"
		expectedOrderList.add("pubDate19uu");   // "20th century"
		expectedOrderList.add("pubDate16uu");   // "17th century"
		expectedOrderList.add("pubDate13uu");   // "14th century"
		expectedOrderList.add("pubDate12uu");   // "13th century"
		expectedOrderList.add("pubDate11uu");   // "12th century"
		expectedOrderList.add("pubDate10uu");   // "11th century"
		expectedOrderList.add("pubDate0963");   // 0963
		expectedOrderList.add("pubDate0960");   // 0960
		expectedOrderList.add("pubDate09uu");   // "10th century"
		expectedOrderList.add("pubDate0801");   // 0801
		expectedOrderList.add("pubDate08uu");   // "9th century"
		expectedOrderList.add("pubDate0500");   // 0500
		expectedOrderList.add("pubDate03uu");   // "4th century"
		expectedOrderList.add("pubDate02uu");   // "3rd century"
		expectedOrderList.add("pubDate01uu");   // "2nd century"
		expectedOrderList.add("pubDate00uu");   // "1st century"

		// invalid/missing dates are designated as last or first in solr
		//  schema file.
		expectedOrderList.add("pubDate0000");
		expectedOrderList.add("pubDate0019");
		expectedOrderList.add("pubDate1uuu");
		expectedOrderList.add("pubDate6666");
		expectedOrderList.add("pubDate9999");

		// get search results sorted by pub_date_sort field
        SolrDocumentList results = getDescSortDocs("collection", "sirsi", "pub_date_sort");

        SolrDocument firstDoc = results.get(0);
		assertTrue("0000 pub date should not sort first", (String) firstDoc.getFirstValue(docIDfname) != "pubDate0000");

		// we know we have documents that are not in the expected order list,
		//  so we must allow for gaps
		// author_sort isn't stored, so we must look at id field
		int expDocIx = -1;
		for (SolrDocument doc : results)
		{
			if (expDocIx < expectedOrderList.size() - 1)
			{
				String resultDocId = (String) doc.getFirstValue(docIDfname);
				// is it a match?
                if (resultDocId.equals(expectedOrderList.get(expDocIx + 1)))
                	expDocIx++;
			}
			else break;  // we found all the documents in the expected order list
		}

		if (expDocIx != expectedOrderList.size() - 1)
		{
			String lastCorrDocId = expectedOrderList.get(expDocIx);
			fail("Publish Date Sort Order is incorrect.  Last correct document was " + lastCorrDocId);
		}
	}


	/**
	 * integration test: pub_date_group_facet field
	 *   NOTE:  This test has to be changed when the year changes!
	 */
@Test
	public final void testPubDateGroupFacet()
			throws ParserConfigurationException, IOException, SAXException, SolrServerException
	{
		String fldName = "pub_date_group_facet";
		createFreshIx("pubDateTests.mrc");

		Set<String> docIds = new HashSet<String>();
		docIds.add("zpubDate2013");
		docIds.add("zpubDate2012");
		docIds.add("zpubDate2011");
		assertSearchResults(fldName, "\"" + PubDateGroup.THIS_YEAR.toString() + "\"", docIds);
		docIds.add("zpubDate2010");
		docIds.add("z2009");
		assertSearchResults(fldName, "\"" + PubDateGroup.LAST_3_YEARS.toString() + "\"", docIds);
		docIds.add("b2008");
		docIds.add("v2007");
		docIds.add("z2006");
		docIds.add("j2005");
		docIds.add("x200u");
		docIds.add("pubDate20uu");
		docIds.add("o20uu");
		docIds.add("pubDate0059");  // 2005
		docIds.add("pubDate0204");  // 2004
		assertSearchResults(fldName, "\"" + PubDateGroup.LAST_10_YEARS.toString() + "\"", docIds);
		docIds.add("q2001");
		docIds.add("f2000");
		docIds.add("firstDateOnly008"); //2000
		docIds.add("w1999");
		docIds.add("c1998");
		docIds.add("e1997");
		docIds.add("m1991");
		docIds.add("k1990");
		docIds.add("b199u");
		docIds.add("y1989");
		docIds.add("contRes");  // 1984
		docIds.add("bothDates008"); // 1964
		docIds.add("pubDate19uu");
		docIds.add("p19uu");
		docIds.add("pubDate0197-1");
		docIds.add("pubDate0197-2");
		assertSearchResults(fldName, "\"" + PubDateGroup.LAST_50_YEARS.toString() + "\"", docIds);

		docIds.clear();
		docIds.add("pubDate00uu");   // "1st century"
		docIds.add("pubDate01uu");   // "2nd century"
		docIds.add("pubDate02uu");   // "3rd century"
		docIds.add("pubDate03uu");   // "4th century"
		docIds.add("pubDate08uu");   // "9th century"
		docIds.add("pubDate09uu");   // "10th century"
		docIds.add("pubDate10uu");   // "11th century"
		docIds.add("pubDate11uu");   // "12th century"
		docIds.add("pubDate12uu");   // "13th century"
		docIds.add("pubDate13uu");   // "14th century"
		docIds.add("pubDate16uu");   // "17th century"
		docIds.add("s190u");   // "1900s"
		docIds.add("b1899");
		docIds.add("r1900");
		docIds.add("pubDate195u");   // "1950s"
		docIds.add("s195u");   // "1950s"
		docIds.add("g1958");
		docIds.add("w1959");
		docIds.add("pubDate0500");
		docIds.add("pubDate0801");
		docIds.add("pubDate0960");
		docIds.add("pubDate0963");
		// TODO: would like to correct these (see autocorrect test)
		docIds.add("pubDate0965"); // should be 1965
		docIds.add("pubDate0980"); // should be 1980
		docIds.add("pubDate0999"); // should be 1999
		assertSearchResults(fldName, "\"" + PubDateGroup.MORE_THAN_50_YEARS_AGO.toString() + "\"", docIds);
	}


	/**
	 * functional test: pub_date_display field
	 */
@Test
	public final void testPubDateForDisplay()
	{
		String fldName = "pub_date";
		String testFilePath = testDataParentPath + File.separator + "pubDateTests.mrc";

		solrFldMapTest.assertSolrFldValue(testFilePath, "firstDateOnly008", fldName, "2000");
		solrFldMapTest.assertSolrFldValue(testFilePath, "bothDates008", fldName, "1964");
		solrFldMapTest.assertSolrFldValue(testFilePath, "contRes", fldName, "1984");
		solrFldMapTest.assertSolrFldValue(testFilePath, "pubDate195u", fldName, "1950s");
		solrFldMapTest.assertSolrFldValue(testFilePath, "pubDate00uu", fldName, "1st century");
		solrFldMapTest.assertSolrFldValue(testFilePath, "pubDate01uu", fldName, "2nd century");
		solrFldMapTest.assertSolrFldValue(testFilePath, "pubDate02uu", fldName, "3rd century");
		solrFldMapTest.assertSolrFldValue(testFilePath, "pubDate03uu", fldName, "4th century");
		solrFldMapTest.assertSolrFldValue(testFilePath, "pubDate08uu", fldName, "9th century");
		solrFldMapTest.assertSolrFldValue(testFilePath, "pubDate09uu", fldName, "10th century");
		solrFldMapTest.assertSolrFldValue(testFilePath, "pubDate10uu", fldName, "11th century");
		solrFldMapTest.assertSolrFldValue(testFilePath, "pubDate11uu", fldName, "12th century");
		solrFldMapTest.assertSolrFldValue(testFilePath, "pubDate12uu", fldName, "13th century");
		solrFldMapTest.assertSolrFldValue(testFilePath, "pubDate13uu", fldName, "14th century");
		solrFldMapTest.assertSolrFldValue(testFilePath, "pubDate16uu", fldName, "17th century");
		solrFldMapTest.assertSolrFldValue(testFilePath, "pubDate19uu", fldName, "20th century");
		solrFldMapTest.assertSolrFldValue(testFilePath, "pubDate20uu", fldName, "21st century");

		// No pub date when unknown
		solrFldMapTest.assertNoSolrFld(testFilePath, "bothDatesBlank", fldName);
		solrFldMapTest.assertNoSolrFld(testFilePath, "pubDateuuuu", fldName);
		// xuuu is unassigned
		solrFldMapTest.assertSolrFldHasNoValue(testFilePath, "pubDate1uuu", fldName, "after 1000");
		solrFldMapTest.assertNoSolrFld(testFilePath, "pubDate1uuu", fldName);

		// future dates are ignored
		solrFldMapTest.assertNoSolrFld(testFilePath, "pubDate21uu", fldName);   // ignored, not "22nd century"
		solrFldMapTest.assertNoSolrFld(testFilePath, "pubDate22uu", fldName);   // ignored, not "23rd century"
		solrFldMapTest.assertNoSolrFld(testFilePath, "pubDate23uu", fldName);   // ignored, not "24th century"
		solrFldMapTest.assertNoSolrFld(testFilePath, "pubDate9999", fldName);   // ignored, not 9999
		solrFldMapTest.assertNoSolrFld(testFilePath, "pubDate99uu", fldName);   // ignored, not "100th century'
		solrFldMapTest.assertNoSolrFld(testFilePath, "pubDate6666", fldName);   // ignored, not 6666
		solrFldMapTest.assertNoSolrFld(testFilePath, "pubDate861u", fldName);   // ignored, not 8610s
	}


	private void pubDateSearchTests(String fldName)
			throws ParserConfigurationException, IOException, SAXException
	{
		assertSingleResult("bothDates008", fldName, "\"1964\"");
		assertSingleResult("pubDate01uu", fldName, "\"2nd century\"");
		assertSingleResult("zpubDate2010", fldName, "\"2010\"");
		Set<String> docIds = new HashSet<String>();
		docIds.add("s195u");
		docIds.add("pubDate195u");
		assertSearchResults(fldName, "\"1950s\"", docIds);
		docIds.clear();
		docIds.add("p19uu");
		docIds.add("pubDate19uu");
		assertSearchResults(fldName, "\"20th century\"", docIds);

		assertZeroResults(fldName, "\"after 1000\"");
		// future dates are ignored/skipped
		assertZeroResults(fldName, "\"6666\"");
		assertZeroResults(fldName, "\"8610s\"");
		assertZeroResults(fldName, "\"9999\"");
		assertZeroResults(fldName, "\"23rd century\"");
		assertZeroResults(fldName, "\"22nd century\"");

		// dates before 500 are ignored/skipped
		assertZeroResults(fldName, "\"0000\"");
		assertZeroResults(fldName, "\"0019\"");

		// corrected values
		docIds.clear();
		docIds.add("pubDate0059");
		docIds.add("j2005");
		assertSearchResults(fldName, "2005", docIds);
		docIds.clear();
		docIds.add("pubDate0197-1");
		docIds.add("pubDate0197-2");
		assertSearchResults(fldName, "1970", docIds);
	}

}
