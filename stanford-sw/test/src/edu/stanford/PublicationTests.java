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
	MarcFactory factory = MarcFactory.newInstance();

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
		solrFldMapTest.assertSolrFldValue(publTestFilePath, "vern260abc", fldName, "vern260a : vern260b,");
		solrFldMapTest.assertSolrFldValue(publTestFilePath, "vern260abcg", fldName, "vern260a : vern260b,");

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
	 * functional test: assure no publication year fields are populated
	 *  when 008/06 wrong byte code or too vague
	 */
@Test
	public void test008IgnoresDates()
	{
		assert008IgnoreDates('b', "    ", "    ");
		assert008IgnoreDates('n', "1uuu", "uuuu");
		assert008IgnoreDates('|', "||||", "||||");
		assert008IgnoreDates('$', "19uu", "19uu");
		assert008IgnoreDates('s', "19uu", "19uu");
	}

	/**
	 * functional test: assure publication_year_isi field ignores dates with u, or wrong 008 byte 6 val
	 */
@Test
	public void test008PublicationYear()
	{
		String solrFldName = "publication_year_isi";
		assert008DateVal('e', "1943", "9999", solrFldName, "1943");
		assert008DateVal('e', "196u", "9999", solrFldName, "1960");
		assert008DateVal('e', "19uu", "9999", solrFldName, null);
		assert008DateVal('e', "uuuu", "uuuu", solrFldName, null);
		assert008DateVal('s', "1943", "2007", solrFldName, "1943");
		assert008DateVal('s', "196u", "2007", solrFldName, "1960");
		assert008DateVal('s', "19uu", "2007", solrFldName, null);
		assert008DateVal('s', "uuuu", "uuuu", solrFldName, null);
		assert008DateVal('t', "1943", "2007", solrFldName, "1943");
		assert008DateVal('t', "196u", "2007", solrFldName, "1960");
		assert008DateVal('t', "19uu", "2007", solrFldName, null);
		assert008DateVal('t', "uuuu", "uuuu", solrFldName, null);

		// none of the following should have a field value
		assert008DateVal('b', "1943", "9999", solrFldName, null);
		assert008DateVal('c', "1943", "9999", solrFldName, null);
		assert008DateVal('d', "1943", "9999", solrFldName, null);
		assert008DateVal('i', "1943", "9999", solrFldName, null);
		assert008DateVal('k', "1943", "9999", solrFldName, null);
		assert008DateVal('m', "1943", "9999", solrFldName, null);
		assert008DateVal('n', "1943", "9999", solrFldName, null);
		assert008DateVal('p', "1943", "9999", solrFldName, null);
		assert008DateVal('q', "1943", "9999", solrFldName, null);
		assert008DateVal('r', "1943", "9999", solrFldName, null);
		assert008DateVal('u', "1943", "9999", solrFldName, null);
		assert008DateVal('|', "1943", "9999", solrFldName, null);
	}

	/**
	 * functional test: assure beginning_year_isi field ignores dates with u, or wrong 008 byte 6 val
	 */
@Test
	public void test008BeginningYear()
	{
		String solrFldName = "beginning_year_isi";
		assert008DateVal('c', "1943", "9999", solrFldName, "1943");
		assert008DateVal('c', "196u", "9999", solrFldName, "1960");
		assert008DateVal('c', "19uu", "9999", solrFldName, null);
		assert008DateVal('c', "uuuu", "uuuu", solrFldName, null);
		assert008DateVal('d', "1943", "2007", solrFldName, "1943");
		assert008DateVal('d', "196u", "2007", solrFldName, "1960");
		assert008DateVal('d', "19uu", "2007", solrFldName, null);
		assert008DateVal('d', "uuuu", "uuuu", solrFldName, null);
		assert008DateVal('m', "1943", "2007", solrFldName, "1943");
		assert008DateVal('m', "196u", "2007", solrFldName, "1960");
		assert008DateVal('m', "19uu", "2007", solrFldName, null);
		assert008DateVal('m', "uuuu", "uuuu", solrFldName, null);
		assert008DateVal('u', "1943", "uuuu", solrFldName, "1943");
		assert008DateVal('u', "196u", "uuuu", solrFldName, "1960");
		assert008DateVal('u', "19uu", "uuuu", solrFldName, null);
		assert008DateVal('u', "uuuu", "uuuu", solrFldName, null);

		// none of the following should have a field value
		assert008DateVal('b', "1943", "9999", solrFldName, null);
		assert008DateVal('e', "1943", "9999", solrFldName, null);
		assert008DateVal('i', "1943", "9999", solrFldName, null);
		assert008DateVal('k', "1943", "9999", solrFldName, null);
		assert008DateVal('n', "1943", "9999", solrFldName, null);
		assert008DateVal('p', "1943", "9999", solrFldName, null);
		assert008DateVal('q', "1943", "9999", solrFldName, null);
		assert008DateVal('r', "1943", "9999", solrFldName, null);
		assert008DateVal('s', "1943", "9999", solrFldName, null);
		assert008DateVal('t', "1943", "9999", solrFldName, null);
		assert008DateVal('|', "1943", "9999", solrFldName, null);
	}

	/**
	 * functional test: assure earliest_year_isi field ignores dates with u, or wrong 008 byte 6 val
	 */
@Test
	public void test008EarliestYear()
	{
		String solrFldName = "earliest_year_isi";
		assert008DateVal('i', "1943", "9999", solrFldName, "1943");
		assert008DateVal('i', "196u", "9999", solrFldName, "1960");
		assert008DateVal('i', "19uu", "9999", solrFldName, null);
		assert008DateVal('i', "uuuu", "uuuu", solrFldName, null);
		assert008DateVal('k', "1943", "2007", solrFldName, "1943");
		assert008DateVal('k', "196u", "2007", solrFldName, "1960");
		assert008DateVal('k', "19uu", "2007", solrFldName, null);
		assert008DateVal('k', "uuuu", "uuuu", solrFldName, null);

		// none of the following should have a field value
		assert008DateVal('b', "1943", "9999", solrFldName, null);
		assert008DateVal('c', "1943", "9999", solrFldName, null);
		assert008DateVal('d', "1943", "9999", solrFldName, null);
		assert008DateVal('e', "1943", "9999", solrFldName, null);
		assert008DateVal('m', "1943", "9999", solrFldName, null);
		assert008DateVal('n', "1943", "9999", solrFldName, null);
		assert008DateVal('p', "1943", "9999", solrFldName, null);
		assert008DateVal('q', "1943", "9999", solrFldName, null);
		assert008DateVal('r', "1943", "9999", solrFldName, null);
		assert008DateVal('s', "1943", "9999", solrFldName, null);
		assert008DateVal('t', "1943", "9999", solrFldName, null);
		assert008DateVal('u', "1943", "9999", solrFldName, null);
		assert008DateVal('|', "1943", "9999", solrFldName, null);
	}

	/**
	 * functional test: assure earliest_poss_year_isi field ignores dates with u, or wrong 008 byte 6 val
	 */
@Test
	public void test008EarliestPossibleYear()
	{
		String solrFldName = "earliest_poss_year_isi";
		assert008DateVal('q', "1943", "9999", solrFldName, "1943");
		assert008DateVal('q', "196u", "9999", solrFldName, "1960");
		assert008DateVal('q', "19uu", "9999", solrFldName, null);
		assert008DateVal('q', "uuuu", "uuuu", solrFldName, null);

		// none of the following should have a field value
		assert008DateVal('b', "1943", "9999", solrFldName, null);
		assert008DateVal('c', "1943", "9999", solrFldName, null);
		assert008DateVal('d', "1943", "9999", solrFldName, null);
		assert008DateVal('e', "1943", "9999", solrFldName, null);
		assert008DateVal('i', "1943", "2007", solrFldName, null);
		assert008DateVal('k', "1943", "2007", solrFldName, null);
		assert008DateVal('m', "1943", "9999", solrFldName, null);
		assert008DateVal('n', "1943", "9999", solrFldName, null);
		assert008DateVal('p', "1943", "9999", solrFldName, null);
		assert008DateVal('r', "1943", "9999", solrFldName, null);
		assert008DateVal('s', "1943", "9999", solrFldName, null);
		assert008DateVal('t', "1943", "9999", solrFldName, null);
		assert008DateVal('u', "1943", "9999", solrFldName, null);
		assert008DateVal('|', "1943", "9999", solrFldName, null);
	}

	/**
	 * functional test: assure release_year_isi field ignores dates with u, or wrong 008 byte 6 val
	 */
@Test
	public void test008ReleaseYear()
	{
		String solrFldName = "release_year_isi";
		assert008DateVal('p', "1943", "9999", solrFldName, "1943");
		assert008DateVal('p', "196u", "9999", solrFldName, "1960");
		assert008DateVal('p', "19uu", "9999", solrFldName, null);
		assert008DateVal('p', "uuuu", "uuuu", solrFldName, null);

		// none of the following should have a field value
		assert008DateVal('b', "1943", "9999", solrFldName, null);
		assert008DateVal('c', "1943", "9999", solrFldName, null);
		assert008DateVal('d', "1943", "9999", solrFldName, null);
		assert008DateVal('e', "1943", "9999", solrFldName, null);
		assert008DateVal('i', "1943", "2007", solrFldName, null);
		assert008DateVal('k', "1943", "2007", solrFldName, null);
		assert008DateVal('m', "1943", "9999", solrFldName, null);
		assert008DateVal('n', "1943", "9999", solrFldName, null);
		assert008DateVal('q', "1943", "9999", solrFldName, null);
		assert008DateVal('r', "1943", "9999", solrFldName, null);
		assert008DateVal('s', "1943", "9999", solrFldName, null);
		assert008DateVal('t', "1943", "9999", solrFldName, null);
		assert008DateVal('u', "1943", "9999", solrFldName, null);
		assert008DateVal('|', "1943", "9999", solrFldName, null);
	}

	/**
	 * functional test: assure reprint_year_isi field ignores dates with u, or wrong 008 byte 6 val
	 */
@Test
	public void test008ReprintYear()
	{
		String solrFldName = "reprint_year_isi";
		assert008DateVal('r', "1943", "9999", solrFldName, "1943");
		assert008DateVal('r', "196u", "9999", solrFldName, "1960");
		assert008DateVal('r', "19uu", "9999", solrFldName, null);
		assert008DateVal('r', "uuuu", "uuuu", solrFldName, null);

		// none of the following should have a field value
		assert008DateVal('b', "1943", "9999", solrFldName, null);
		assert008DateVal('c', "1943", "9999", solrFldName, null);
		assert008DateVal('d', "1943", "9999", solrFldName, null);
		assert008DateVal('e', "1943", "9999", solrFldName, null);
		assert008DateVal('i', "1943", "2007", solrFldName, null);
		assert008DateVal('k', "1943", "2007", solrFldName, null);
		assert008DateVal('m', "1943", "9999", solrFldName, null);
		assert008DateVal('n', "1943", "9999", solrFldName, null);
		assert008DateVal('p', "1943", "9999", solrFldName, null);
		assert008DateVal('q', "1943", "9999", solrFldName, null);
		assert008DateVal('s', "1943", "9999", solrFldName, null);
		assert008DateVal('t', "1943", "9999", solrFldName, null);
		assert008DateVal('u', "1943", "9999", solrFldName, null);
		assert008DateVal('|', "1943", "9999", solrFldName, null);
	}


	/**
	 * functional test: assure ending_year_isi field ignores dates with u, or wrong 008 byte 6 val
	 */
@Test
	public void test008EndingYear()
	{
		String solrFldName = "ending_year_isi";
		assert008DateVal('d', "1943", "2007", solrFldName, "2007");
		assert008DateVal('d', "1943", "200u", solrFldName, "2009");
		assert008DateVal('d', "1943", "20uu", solrFldName, null);
		assert008DateVal('d', "1943", "2uuu", solrFldName, null);
		assert008DateVal('m', "1943", "2007", solrFldName, "2007");
		assert008DateVal('m', "1943", "200u", solrFldName, "2009");
		assert008DateVal('m', "1943", "20uu", solrFldName, null);
		assert008DateVal('m', "1943", "2uuu", solrFldName, null);
		assert008DateVal('m', "1943", "9999", solrFldName, null);

		// none of the following should have a field value
		assert008DateVal('b', "1943", "2007", solrFldName, null);
		assert008DateVal('c', "1943", "9999", solrFldName, null);
		assert008DateVal('e', "1943", "2007", solrFldName, null);
		assert008DateVal('i', "1943", "2007", solrFldName, null);
		assert008DateVal('k', "1943", "2007", solrFldName, null);
		assert008DateVal('n', "1943", "2007", solrFldName, null);
		assert008DateVal('p', "1943", "2007", solrFldName, null);
		assert008DateVal('q', "1943", "2007", solrFldName, null);
		assert008DateVal('r', "1943", "2007", solrFldName, null);
		assert008DateVal('s', "1943", "2007", solrFldName, null);
		assert008DateVal('t', "1943", "2007", solrFldName, null);
		assert008DateVal('u', "1943", "2007", solrFldName, null);
		assert008DateVal('|', "1943", "2007", solrFldName, null);
	}


	/**
	 * functional test: assure latest_year_isi field ignores dates with u, or wrong 008 byte 6 val
	 */
@Test
	public void test008LatestYear()
	{
		String solrFldName = "latest_year_isi";
		assert008DateVal('i', "1943", "2007", solrFldName, "2007");
		assert008DateVal('i', "1943", "200u", solrFldName, "2009");
		assert008DateVal('i', "1943", "20uu", solrFldName, null);
		assert008DateVal('i', "1943", "2uuu", solrFldName, null);
		assert008DateVal('k', "1943", "2007", solrFldName, "2007");
		assert008DateVal('k', "1943", "200u", solrFldName, "2009");
		assert008DateVal('k', "1943", "20uu", solrFldName, null);
		assert008DateVal('k', "1943", "2uuu", solrFldName, null);

		// none of the following should have a field value
		assert008DateVal('b', "1943", "2007", solrFldName, null);
		assert008DateVal('c', "1943", "9999", solrFldName, null);
		assert008DateVal('d', "1943", "2007", solrFldName, null);
		assert008DateVal('e', "1943", "2007", solrFldName, null);
		assert008DateVal('m', "1943", "2007", solrFldName, null);
		assert008DateVal('n', "1943", "2007", solrFldName, null);
		assert008DateVal('p', "1943", "2007", solrFldName, null);
		assert008DateVal('q', "1943", "2007", solrFldName, null);
		assert008DateVal('r', "1943", "2007", solrFldName, null);
		assert008DateVal('s', "1943", "2007", solrFldName, null);
		assert008DateVal('t', "1943", "2007", solrFldName, null);
		assert008DateVal('u', "1943", "2007", solrFldName, null);
		assert008DateVal('|', "1943", "2007", solrFldName, null);
	}

	/**
	 * functional test: assure latest_poss_year_isi field ignores dates with u, or wrong 008 byte 6 val
	 */
@Test
	public void test008LatestPossibleYear()
	{
		String solrFldName = "latest_poss_year_isi";
		assert008DateVal('q', "1943", "2007", solrFldName, "2007");
		assert008DateVal('q', "1943", "200u", solrFldName, "2009");
		assert008DateVal('q', "1943", "20uu", solrFldName, null);
		assert008DateVal('q', "1943", "2uuu", solrFldName, null);

		// none of the following should have a field value
		assert008DateVal('b', "1943", "2007", solrFldName, null);
		assert008DateVal('c', "1943", "9999", solrFldName, null);
		assert008DateVal('d', "1943", "2007", solrFldName, null);
		assert008DateVal('e', "1943", "2007", solrFldName, null);
		assert008DateVal('i', "1943", "2007", solrFldName, null);
		assert008DateVal('k', "1943", "2007", solrFldName, null);
		assert008DateVal('m', "1943", "2007", solrFldName, null);
		assert008DateVal('n', "1943", "2007", solrFldName, null);
		assert008DateVal('p', "1943", "2007", solrFldName, null);
		assert008DateVal('r', "1943", "2007", solrFldName, null);
		assert008DateVal('s', "1943", "2007", solrFldName, null);
		assert008DateVal('t', "1943", "2007", solrFldName, null);
		assert008DateVal('u', "1943", "2007", solrFldName, null);
		assert008DateVal('|', "1943", "2007", solrFldName, null);
	}

	/**
	 * functional test: assure production_year_isi field ignores dates with u, or wrong 008 byte 6 val
	 */
@Test
	public void test008ProductionYear()
	{
		String solrFldName = "production_year_isi";
		assert008DateVal('p', "1943", "2007", solrFldName, "2007");
		assert008DateVal('p', "1943", "200u", solrFldName, "2009");
		assert008DateVal('p', "1943", "20uu", solrFldName, null);
		assert008DateVal('p', "1943", "2uuu", solrFldName, null);

		// none of the following should have a field value
		assert008DateVal('b', "1943", "2007", solrFldName, null);
		assert008DateVal('c', "1943", "9999", solrFldName, null);
		assert008DateVal('d', "1943", "2007", solrFldName, null);
		assert008DateVal('e', "1943", "2007", solrFldName, null);
		assert008DateVal('i', "1943", "2007", solrFldName, null);
		assert008DateVal('k', "1943", "2007", solrFldName, null);
		assert008DateVal('m', "1943", "2007", solrFldName, null);
		assert008DateVal('n', "1943", "2007", solrFldName, null);
		assert008DateVal('q', "1943", "2007", solrFldName, null);
		assert008DateVal('r', "1943", "2007", solrFldName, null);
		assert008DateVal('s', "1943", "2007", solrFldName, null);
		assert008DateVal('t', "1943", "2007", solrFldName, null);
		assert008DateVal('u', "1943", "2007", solrFldName, null);
		assert008DateVal('|', "1943", "2007", solrFldName, null);
	}


	/**
	 * functional test: assure original_year_isi field ignores dates with u, or wrong 008 byte 6 val
	 */
@Test
	public void test008OriginalYear()
	{
		String solrFldName = "original_year_isi";
		assert008DateVal('r', "1943", "2007", solrFldName, "2007");
		assert008DateVal('r', "1943", "200u", solrFldName, "2009");
		assert008DateVal('r', "1943", "20uu", solrFldName, null);
		assert008DateVal('r', "1943", "2uuu", solrFldName, null);

		// none of the following should have a field value
		assert008DateVal('b', "1943", "2007", solrFldName, null);
		assert008DateVal('c', "1943", "9999", solrFldName, null);
		assert008DateVal('d', "1943", "2007", solrFldName, null);
		assert008DateVal('e', "1943", "2007", solrFldName, null);
		assert008DateVal('i', "1943", "2007", solrFldName, null);
		assert008DateVal('k', "1943", "2007", solrFldName, null);
		assert008DateVal('m', "1943", "2007", solrFldName, null);
		assert008DateVal('n', "1943", "2007", solrFldName, null);
		assert008DateVal('p', "1943", "2007", solrFldName, null);
		assert008DateVal('q', "1943", "2007", solrFldName, null);
		assert008DateVal('s', "1943", "2007", solrFldName, null);
		assert008DateVal('t', "1943", "2007", solrFldName, null);
		assert008DateVal('u', "1943", "2007", solrFldName, null);
		assert008DateVal('|', "1943", "2007", solrFldName, null);
	}

	/**
	 * functional test: assure copyright_year_isi field ignores dates with u, or wrong 008 byte 6 val
	 */
@Test
	public void test008CopyrightYear()
	{
		String solrFldName = "copyright_year_isi";
		assert008DateVal('t', "1943", "2007", solrFldName, "2007");
		assert008DateVal('t', "1943", "200u", solrFldName, "2009");
		assert008DateVal('t', "1943", "20uu", solrFldName, null);
		assert008DateVal('t', "1943", "2uuu", solrFldName, null);

		// none of the following should have a field value
		assert008DateVal('b', "1943", "2007", solrFldName, null);
		assert008DateVal('c', "1943", "9999", solrFldName, null);
		assert008DateVal('d', "1943", "2007", solrFldName, null);
		assert008DateVal('e', "1943", "2007", solrFldName, null);
		assert008DateVal('i', "1943", "2007", solrFldName, null);
		assert008DateVal('k', "1943", "2007", solrFldName, null);
		assert008DateVal('m', "1943", "2007", solrFldName, null);
		assert008DateVal('n', "1943", "2007", solrFldName, null);
		assert008DateVal('p', "1943", "2007", solrFldName, null);
		assert008DateVal('q', "1943", "2007", solrFldName, null);
		assert008DateVal('r', "1943", "2007", solrFldName, null);
		assert008DateVal('s', "1943", "2007", solrFldName, null);
		assert008DateVal('u', "1943", "2007", solrFldName, null);
		assert008DateVal('|', "1943", "2007", solrFldName, null);
	}

	/**
	 * functional test: assure year_isi field ignores dates with too many u
	 *   and logs error messages for bad match with 008 byte 6 val
	 */
@Test
	public void test008OtherYear()
	{
		String solrFldName = "other_year_isi";
		assert008DateVal('b', "1943", "    ", solrFldName, "1943");
		assert008DateVal('b', "194u", "    ", solrFldName, "1940");
		assert008DateVal('b', "19uu", "    ", solrFldName, null);
		assert008DateVal('b', "    ", "    ", solrFldName, null);
		assert008DateVal('n', "1943", "    ", solrFldName, "1943");
		assert008DateVal('n', "194u", "    ", solrFldName, "1940");
		assert008DateVal('n', "19uu", "    ", solrFldName, null);
		assert008DateVal('n', "||||", "    ", solrFldName, null);
		assert008DateVal('|', "1943", "    ", solrFldName, "1943");
		assert008DateVal('|', "194u", "    ", solrFldName, "1940");
		assert008DateVal('|', "19uu", "    ", solrFldName, null);
		assert008DateVal('|', "||||", "    ", solrFldName, null);
		assert008DateVal('$', "1943", "    ", solrFldName, "1943");
		assert008DateVal('$', "194u", "    ", solrFldName, "1940");
		assert008DateVal('$', "19uu", "    ", solrFldName, null);
		assert008DateVal('$', "||||", "    ", solrFldName, null);

		// none of the following should have a field value
		assert008DateVal('c', "1943", "9999", solrFldName, null);
		assert008DateVal('d', "1943", "2007", solrFldName, null);
		assert008DateVal('e', "1943", "2007", solrFldName, null);
		assert008DateVal('i', "1943", "2007", solrFldName, null);
		assert008DateVal('k', "1943", "2007", solrFldName, null);
		assert008DateVal('m', "1943", "2007", solrFldName, null);
		assert008DateVal('p', "1943", "2007", solrFldName, null);
		assert008DateVal('q', "1943", "2007", solrFldName, null);
		assert008DateVal('r', "1943", "2007", solrFldName, null);
		assert008DateVal('s', "1943", "2007", solrFldName, null);
		assert008DateVal('u', "1943", "2007", solrFldName, null);
	}


	/**
	 * functional test: assure date slider pub_year_tisim field is populated correctly from single value in single 260c
	 */
@Test
	public void test260SingleValueInDateSlider()
	{
		String solrFldName = "pub_year_tisim";
	    assertSingleSolrFldValFromMarcSubfld("260", 'c', "1973", solrFldName, "1973");
	    assertSingleSolrFldValFromMarcSubfld("260", 'c', "[1973]", solrFldName, "1973");
	    assertSingleSolrFldValFromMarcSubfld("260", 'c', "1973]", solrFldName, "1973");
	    assertSingleSolrFldValFromMarcSubfld("260", 'c', "[1973?]", solrFldName, "1973");
	    assertSingleSolrFldValFromMarcSubfld("260", 'c', "[196-?]", solrFldName, "1960");
//	    assertSingleSolrFldValFromMarcSubfld("260", 'c', "March 1987.", solrFldName, "1987");
	    // copyright year
	    assertSingleSolrFldValFromMarcSubfld("260", 'c', "c1975.", solrFldName, "1975");
	    assertSingleSolrFldValFromMarcSubfld("260", 'c', "[c1973]", solrFldName, "1973");
	    assertSingleSolrFldValFromMarcSubfld("260", 'c', "c1973]", solrFldName, "1973");
		// with corrected date
	    assertSingleSolrFldValFromMarcSubfld("260", 'c', "1973 [i.e. 1974]", solrFldName, "1974");
	    assertSingleSolrFldValFromMarcSubfld("260", 'c', "1971[i.e.1972]", solrFldName, "1972");
	    assertSingleSolrFldValFromMarcSubfld("260", 'c', "1973 [i.e.1974]", solrFldName, "1974");
	    assertSingleSolrFldValFromMarcSubfld("260", 'c', "1967 [i. e. 1968]", solrFldName, "1968");
	}

	/**
	 * functional test: assure date slider pub_year_tisim field is not populated when no 008 or 260c usable value
	 */
@Test
	public void test260NoValueInDateSlider()
	{
		String solrFldName = "pub_year_tisim";
	    assertNoSolrFldFromMarcSubfld("260", 'c', "[19--]", solrFldName);
	}

	/**
	 * functional test: assure date slider pub_year_tisim field is populated correctly from single value in single 260c
	 */
//@Test
	public void test260MultSingleValsInDateSlider()
	{
		String solrFldName = "pub_year_tisim";
	    assertMultSolrFldValFromMarcSubfld("260", 'c', "[1974, c1973]", solrFldName, new String[]{"1974", "1973"});
	    assertMultSolrFldValFromMarcSubfld("260", 'c', "1975, c1974.", solrFldName, new String[]{"1975", "1974"});
	    assertMultSolrFldValFromMarcSubfld("260", 'c', "1974 [c1973]", solrFldName, new String[]{"1974", "1973"});
	    assertMultSolrFldValFromMarcSubfld("260", 'c', "[1975] c1974.", solrFldName, new String[]{"1975", "1974"});

	    assertMultSolrFldValFromMarcSubfld("260", 'c', "1965[c1966]", solrFldName, new String[]{"1965", "1966"});
	    assertMultSolrFldValFromMarcSubfld("260", 'c', "1967, c1966]", solrFldName, new String[]{"1967", "1966"});
	    assertMultSolrFldValFromMarcSubfld("260", 'c', "[1974?] c1973.", solrFldName, new String[]{"1974", "1973"});
	}


	/**
	 * functional test: assure beginning_year_isi field is populated correctly from 260 as needed
	 */
//@Test
	public void test260OpenYearRangeInDateSlider()
	{
		String solrFldName = "beginning_year_isi";
		// as only date
		// [dddd-
		// [dddd]-
		// dddd-
		// [dddd?-
		// [dddd?]-
		// [ddd-?-
		// [ddd-?]-
		//  beginning copyright year
		// [cdddd-
		// [cdddd]-
		// cdddd-
	}

	/**
	 * functional test: assure beginning_year_isi and ending_year_isi field is
	 *  populated correctly from 260 when there is a rang present
	 */
//@Test
	public void test260ClosedYearRangeInDateSlider()
	{
		String solrFldName = "beginning_year_isi";
		String solrFldName2 = "ending_year_isi";
		// [dddd-dddd]
		// [dddd]-[dddd]
		// [dddd]-dddd
		// dddd-[dddd]
		// dddd-dddd
		// [between 1973 and 1975]
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
	 * functional test:  imprint_year_isim from 260c - easy to parse
	 */
//@Test
	public void testImprintYearEasy()
	{
		String solrFldName = "imprint_year_isim";
	    assertSingleSolrFldValFromMarcSubfld("260", 'c', "1862", solrFldName, "1862");
	    assertSingleSolrFldValFromMarcSubfld("260", 'c', "1973.", solrFldName, "1973");

	    // two values from two subfield c
	    Record record = factory.newRecord();
		DataField df = factory.newDataField("260", ' ', ' ');
	    df.addSubfield(factory.newSubfield('c', "1798"));
	    df.addSubfield(factory.newSubfield('a', "[i.e. Bruxelles :"));
	    df.addSubfield(factory.newSubfield('c', "Moens,"));
	    df.addSubfield(factory.newSubfield('c', "1883]"));
	    record.addVariableField(df);
	    solrFldMapTest.assertSolrFldHasNumValues(record, solrFldName, 2);
	    solrFldMapTest.assertSolrFldValue(record, solrFldName, "1798");
	    solrFldMapTest.assertSolrFldValue(record, solrFldName, "1883");
	}

	/**
	 * functional test:  imprint_year_isim from 260c - hard to parse
	 */
//@Test
	public void testImprintYearHard()
	{
		String solrFldName = "imprint_year_isim";
	    assertSingleSolrFldValFromMarcSubfld("260", 'c', "April 15, 1977.", solrFldName, "1977");
	    assertSingleSolrFldValFromMarcSubfld("260", 'c', "<1981- >", solrFldName, "1981");

	    // two values from one subfield c
		Record record = factory.newRecord();
		DataField df = factory.newDataField("260", ' ', ' ');
	    df.addSubfield(factory.newSubfield('c', "1967, c1965."));
	    record.addVariableField(df);
	    solrFldMapTest.assertSolrFldHasNumValues(record, solrFldName, 2);
	    solrFldMapTest.assertSolrFldValue(record, solrFldName, "1967");
	    solrFldMapTest.assertSolrFldValue(record, solrFldName, "1965");
	    record = factory.newRecord();
		df = factory.newDataField("260", ' ', ' ');
	    df.addSubfield(factory.newSubfield('c', "1968 [i.e. 1971]"));
	    record.addVariableField(df);
	    solrFldMapTest.assertSolrFldHasNumValues(record, solrFldName, 2);
	    solrFldMapTest.assertSolrFldValue(record, solrFldName, "1968");
	    solrFldMapTest.assertSolrFldValue(record, solrFldName, "1971");

	    // ranges
	    record = factory.newRecord();
		df = factory.newDataField("260", ' ', ' ');
	    df.addSubfield(factory.newSubfield('c', "1908-1924."));
	    record.addVariableField(df);
	    solrFldMapTest.assertSolrFldHasNumValues(record, solrFldName, 17);
	    solrFldMapTest.assertSolrFldValue(record, solrFldName, "1908");
	    solrFldMapTest.assertSolrFldValue(record, solrFldName, "1916");
	    solrFldMapTest.assertSolrFldValue(record, solrFldName, "1924");
	    record = factory.newRecord();
		df = factory.newDataField("260", ' ', ' ');
	    df.addSubfield(factory.newSubfield('c', "1889-1912."));
	    record.addVariableField(df);
	    solrFldMapTest.assertSolrFldHasNumValues(record, solrFldName, 24);
	    solrFldMapTest.assertSolrFldValue(record, solrFldName, "1889");
	    solrFldMapTest.assertSolrFldValue(record, solrFldName, "1900");
	    solrFldMapTest.assertSolrFldValue(record, solrFldName, "1912");
	}


	/**
	 * functional test:  imprint_display from 260
	 */
@Test
	public void testImprintDisplay()
	{
		// see https://jirasul.stanford.edu/jira/browse/SW-928
		String solrFldName = "imprint_display";

		// 250 + 260
	    Record record = factory.newRecord();
		DataField df = factory.newDataField("250", ' ', ' ');
	    df.addSubfield(factory.newSubfield('a', "Special ed."));
	    record.addVariableField(df);
		df = factory.newDataField("260", ' ', ' ');
		df.addSubfield(factory.newSubfield('a', "London :"));
		df.addSubfield(factory.newSubfield('b', "William Heinemann,"));
	    df.addSubfield(factory.newSubfield('c', "2012"));
	    record.addVariableField(df);
	    solrFldMapTest.assertSolrFldValue(record, solrFldName, "Special ed. - London : William Heinemann, 2012");

	    // 250 alone
	    record = factory.newRecord();
		df = factory.newDataField("250", ' ', ' ');
	    df.addSubfield(factory.newSubfield('a', "Canadian ed. ="));
	    df.addSubfield(factory.newSubfield('b', "Éd. canadienne."));
	    record.addVariableField(df);
	    solrFldMapTest.assertSolrFldValue(record, solrFldName, "Canadian ed. = Éd. canadienne.");

	    record = factory.newRecord();
		df = factory.newDataField("250", ' ', ' ');
	    df.addSubfield(factory.newSubfield('a', "Rev. as of Jan. 1, 1958."));
	    record.addVariableField(df);
	    solrFldMapTest.assertSolrFldValue(record, solrFldName, "Rev. as of Jan. 1, 1958.");

	    // 250 + 260
	    record = factory.newRecord();
		df = factory.newDataField("250", ' ', ' ');
	    df.addSubfield(factory.newSubfield('a', "3rd draft /"));
	    df.addSubfield(factory.newSubfield('b', "edited by Paul Watson."));
	    record.addVariableField(df);
		df = factory.newDataField("260", ' ', ' ');
		df.addSubfield(factory.newSubfield('a', "London"));
	    record.addVariableField(df);
	    solrFldMapTest.assertSolrFldValue(record, solrFldName, "3rd draft / edited by Paul Watson. - London");

	    // 250 linked
	    record = factory.newRecord();
		df = factory.newDataField("250", ' ', ' ');
	    df.addSubfield(factory.newSubfield('6', "880-04"));
	    df.addSubfield(factory.newSubfield('a', "Di 1 ban."));
	    record.addVariableField(df);
		df = factory.newDataField("880", ' ', ' ');
	    df.addSubfield(factory.newSubfield('6', "250-04"));
	    df.addSubfield(factory.newSubfield('a', "第1版."));
	    record.addVariableField(df);
	    solrFldMapTest.assertSolrFldValue(record, solrFldName, "Di 1 ban. 第1版.");

	    // 260 linked
	    record = factory.newRecord();
		df = factory.newDataField("260", ' ', ' ');
	    df.addSubfield(factory.newSubfield('6', "880-03"));
		df.addSubfield(factory.newSubfield('a', "Or Yehudah :"));
		df.addSubfield(factory.newSubfield('b', "Kineret :"));
		df.addSubfield(factory.newSubfield('b', "Zemorah-Bitan,"));
		df.addSubfield(factory.newSubfield('c', "c2013."));
	    record.addVariableField(df);
		df = factory.newDataField("880", ' ', ' ');
	    df.addSubfield(factory.newSubfield('6', "260-03//r"));
	    df.addSubfield(factory.newSubfield('a', "אור יהודה :"));
	    df.addSubfield(factory.newSubfield('b', "כנרת :"));
	    df.addSubfield(factory.newSubfield('b', "זמורה־ביתן,"));
	    df.addSubfield(factory.newSubfield('c', "c2013."));
	    record.addVariableField(df);
	    solrFldMapTest.assertSolrFldValue(record, solrFldName, "Or Yehudah : Kineret : Zemorah-Bitan, c2013. אור יהודה : כנרת : זמורה־ביתן, c2013.");

	    record = factory.newRecord();
		df = factory.newDataField("260", ' ', ' ');
	    df.addSubfield(factory.newSubfield('6', "880-02"));
		df.addSubfield(factory.newSubfield('a', "[Taibei] :"));
		df.addSubfield(factory.newSubfield('b', " Gai hui,"));
		df.addSubfield(factory.newSubfield('c', "Minguo 69 [1980]"));
	    record.addVariableField(df);
		df = factory.newDataField("880", ' ', ' ');
	    df.addSubfield(factory.newSubfield('6', "260-02"));
	    df.addSubfield(factory.newSubfield('a', "[台北] :"));
	    df.addSubfield(factory.newSubfield('b', "該會,"));
	    df.addSubfield(factory.newSubfield('c', "民國69 [1980]"));
	    record.addVariableField(df);
	    solrFldMapTest.assertSolrFldValue(record, solrFldName, "[Taibei] : Gai hui, Minguo 69 [1980] [台北] : 該會, 民國69 [1980]");

	    // 250 + 260 both linked
	    record = factory.newRecord();
		df = factory.newDataField("250", ' ', ' ');
	    df.addSubfield(factory.newSubfield('6', "880-04"));
	    df.addSubfield(factory.newSubfield('a', "Di 1 ban."));
	    record.addVariableField(df);
		df = factory.newDataField("260", ' ', ' ');
	    df.addSubfield(factory.newSubfield('6', "880-05"));
		df.addSubfield(factory.newSubfield('a', "Shanghai Shi :"));
		df.addSubfield(factory.newSubfield('b', "Shanghai shu dian chu ban she,"));
		df.addSubfield(factory.newSubfield('c', "2013."));
	    record.addVariableField(df);
		df = factory.newDataField("880", ' ', ' ');
	    df.addSubfield(factory.newSubfield('6', "250-04"));
	    df.addSubfield(factory.newSubfield('a', "第1版."));
	    record.addVariableField(df);
		df = factory.newDataField("880", ' ', ' ');
	    df.addSubfield(factory.newSubfield('6', "260-05"));
	    df.addSubfield(factory.newSubfield('a', "上海市 :"));
	    df.addSubfield(factory.newSubfield('b', "上海书店出版社,"));
	    df.addSubfield(factory.newSubfield('c', "2013."));
	    record.addVariableField(df);
	    solrFldMapTest.assertSolrFldValue(record, solrFldName, "Di 1 ban. 第1版. - Shanghai Shi : Shanghai shu dian chu ban she, 2013. 上海市 : 上海书店出版社, 2013.");


	    // more specific date
	    // 260 	##$aOak Ridge, Tenn. :$bU.S. Dept. of Energy,$cApril 15, 1977.

	    // angle brackets
	    // 260 	##$aStuttgart :$bKlett-Cotta,$c<1981- >

	    // c date
	    // 260 	##$aNew York :$bPublished by W. Schaus,$cc1860$e(Boston :$fPrinted at J.H. Bufford's)

	    // multiple 260

	    // open date?
	    // 260 	##$a[Reston, Va.?] :$bU.S. Geological Survey ;$aWashington, D.C. :$bFor sale by the Supt. of Docs., U.S. G.P.O.,$c1986-
	    // 260 	##$a[Philadelphia] :$bUnited States Pharacopeial Convention ;$a[s.l.] :$bDistributed by Mack Pub. Co.,$c1980-

	    // s.l.
	    // 260 	##$a[S.l. :$bs.n.,$c15--?]
	    // 260 	##$a[S.l. :$bs.n.],$c1970$e(London :$fHigh Fidelity Sound Studios)

	    // s.n.
	    // 260 	##$aVictoria, B.C. :$b[s.n.],$c1898-1945.
	    // 260 	##$aBelfast [i.e. Dublin :$bs.n.],$c1946 [reprinted 1965]

	    // mult dates single 260c
	    // 260 	##$aWashington, D.C. (1649 K St., N.W., Washington 20006) :$bWider Opportunities for Women,$c1979 printing, c1975.
	    // 260 	##$aBelfast [i.e. Dublin :$bs.n.],$c1946 [reprinted 1965]
	    // 260 	##$aLondon :$bCollins,$c1967, c1965.
	    // 260 	##$aLondon :$bSussex Tapes,$c1968 [i.e. 1971]

	    // plus sub g
	    // 260 	##$aLondon :$bMacmillan,$c1971$g(1973 printing)


	    // mult 260c
	    // 260 	##$aParis :$bImpr. Vincent,$c1798$a[i.e. Bruxelles :$bMoens,$c1883]

	    // date range
	    // 260 	##$aVictoria, B.C. :$b[s.n.],$c1898-1945.
	    // 260 	##$c1908-1924.
	    // 260 	##$aLondon :$b[s.n.],$c1889-1912.
	    // 260 	##$a[Pennsylvania :$bs.n.],$c1878-[1927?]$e(Gettysburg :$fJ.E. Wible, Printer)
	    // 260 	##$aLondon :$bHoward League for Penal Reform,$c[c1965-c1983]

	    // no date
	    // 260 	2#$31980-May 1993$aLondon :$bVogue
	}

	/**
	 * functional test: assure pub dates later than current year +1 are ignored
	 */
@Test
	public void test264PubDate()
	{
		String fldName = "pub_date";
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
	 * integration test: pub_year_tisim
	 */
@Test
	public final void testPubDateForSlider()
			throws ParserConfigurationException, IOException, SAXException, SolrServerException
	{
		createFreshIx("pubDateTests.mrc");
		String fldName = "pub_year_tisim";
		Set<String> docIds = new HashSet<String>();

//		assertSingleResult("zpubDate2010", fldName, "2010");

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
		docIds.add("contRes");
		assertSearchResults(fldName, "2005", docIds);
		docIds.clear();
		docIds.add("pubDate195u");  // it's a range including 1970
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
		expectedOrderList.add("r1900");   // "1900"
		expectedOrderList.add("s190u");   // "1900s"
		expectedOrderList.add("pubDate195u");   // "1950s"
		expectedOrderList.add("s195u");   // "1950s"
		expectedOrderList.add("g1958");   // "1958"
		expectedOrderList.add("w1959");   // "1959"ˇ
		expectedOrderList.add("bothDates008");  // "1964"
//		expectedOrderList.add("pubDate0197-1");  // 1970
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
		expectedOrderList.add("f2000");   // "2000"
		expectedOrderList.add("firstDateOnly008");  // "2000"
		expectedOrderList.add("x200u");   // "2000s"
		expectedOrderList.add("q2001");   // "2001"
//		expectedOrderList.add("pubDate0204");  // 2004
//		expectedOrderList.add("pubDate0059");  // 2005
		expectedOrderList.add("z2006");   // "2006"
		expectedOrderList.add("v2007");   // "2007"
		expectedOrderList.add("b2008");   // "2008"
		expectedOrderList.add("z2009");   // "2009"
		expectedOrderList.add("zpubDate2010");   // "2010"

		// invalid/missing dates are designated as last in solr schema file
		//  they are in order of occurrence in the raw data
		expectedOrderList.add("pubDate0000");
		expectedOrderList.add("pubDate0019");
//		expectedOrderList.add("pubDate0059");  // 2005 not in 008
//		expectedOrderList.add("pubDate0197-1");
//		expectedOrderList.add("pubDate0204");  // 2004  not in 008
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
//		expectedOrderList.add("pubDate0059");  // 2005
//		expectedOrderList.add("pubDate0204");  // 2004
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
		expectedOrderList.add("b199u");   // "1990s"
		expectedOrderList.add("k1990");   // "1990"
		expectedOrderList.add("y1989");   // "1989"
		expectedOrderList.add("contRes");       // "1984"
//		expectedOrderList.add("pubDate0197-1");  // 1970
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
//		expectedOrderList.add("pubDate0059");  // 2005 not in 008
//		expectedOrderList.add("pubDate0197-1");
//		expectedOrderList.add("pubDate0204");  // 2004  not in 008
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
		docIds.add("zpubDate2014");
		docIds.add("zpubDate2013");
		docIds.add("zpubDate2012");
		assertSearchResults(fldName, "\"" + PubDateGroup.THIS_YEAR.toString() + "\"", docIds);
		docIds.add("zpubDate2011");
		docIds.add("zpubDate2010");
		assertSearchResults(fldName, "\"" + PubDateGroup.LAST_3_YEARS.toString() + "\"", docIds);
		docIds.add("z2009");
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

	/**
	 * assert that no Solr field populated from an 008 date field value is assigned
	 * @param byte06 - value of 008/6
	 * @param date1str - value of 008/7-10
	 * @param date2str - value of 008/11-14
	 */
	private void assert008IgnoreDates(char byte06, String date1str, String date2str)
	{
		Record record = factory.newRecord();
		record.addVariableField(factory.newControlField("008", "      " + byte06 + date1str + date2str));
		solrFldMapTest.assertSolrFldValue(record, "date_1_008_raw_ssi", date1str);
		solrFldMapTest.assertSolrFldValue(record, "date_2_008_raw_ssi", date2str);
	    solrFldMapTest.assertNoSolrFld(record, "beginning_year_isi");
	    solrFldMapTest.assertNoSolrFld(record, "earliest_year_isi");
	    solrFldMapTest.assertNoSolrFld(record, "earliest_poss_year_isi");
	    solrFldMapTest.assertNoSolrFld(record, "publication_year_isi");
	    solrFldMapTest.assertNoSolrFld(record, "release_year_isi");
	    solrFldMapTest.assertNoSolrFld(record, "reprint_year_isi");
	    solrFldMapTest.assertNoSolrFld(record, "ending_year_isi");
	    solrFldMapTest.assertNoSolrFld(record, "latest_year_isi");
	    solrFldMapTest.assertNoSolrFld(record, "latest_poss_year_isi");
	    solrFldMapTest.assertNoSolrFld(record, "production_year_isi");
	    solrFldMapTest.assertNoSolrFld(record, "orig_year_isi");
	    solrFldMapTest.assertNoSolrFld(record, "copyright_year_isi");
	    solrFldMapTest.assertNoSolrFld(record, "other_year_isi");
	}

	/**
	 * assert that Solr field has expected value (or is not populated) given the
	 *  008 values.
	 * @param byte06 - value of 008/6
	 * @param date1str - value of 008/7-10
	 * @param date2str - value of 008/11-14
	 * @param solrFldName - name of Solr field
	 * @param expFldVal - expected value of Solr field, or null if Solr field should not be populated
	 */
	private void assert008DateVal(char byte06, String date1str, String date2str, String solrFldName, String expFldVal)
	{
		Record record = factory.newRecord();
		record.addVariableField(factory.newControlField("008", "      " + byte06 + date1str + date2str));
		record.addVariableField(factory.newControlField("001", "aassert008DateVal"));
		if (expFldVal != null)
			solrFldMapTest.assertSolrFldValue(record, solrFldName, expFldVal);
		else
			solrFldMapTest.assertNoSolrFld(record, solrFldName);
	}

	/**
	 * assert that Marc record with the field/subfield indicated populates SolrFldMap with expected Solr field value
	 * @param fldTag marc field tag (3 chars)
	 * @param subFld marc subfield (char)
	 * @param rawValue raw value to go in marc subfield
	 * @param solrFldName name of solr field expecting value
	 * @param expSolrFldVal value expected in Solr field
	 */
	private void assertSingleSolrFldValFromMarcSubfld(String fldTag, char subFld, String rawValue, String solrFldName, String expSolrFldVal)
	{
		Record record = factory.newRecord();
		ControlField cf008 = factory.newControlField("008", "      |||||||||");
		record.addVariableField(cf008);
		DataField df = factory.newDataField(fldTag, ' ', ' ');
	    df.addSubfield(factory.newSubfield(subFld, rawValue));
	    record.addVariableField(df);
	    solrFldMapTest.assertSolrFldHasNumValues(record, solrFldName, 1);
	    solrFldMapTest.assertSolrFldValue(record, solrFldName, expSolrFldVal);
	}

	/**
	 * assert that Marc record with the field/subfield indicated populates SolrFldMap with expected Solr field value
	 * @param fldTag marc field tag (3 chars)
	 * @param subFld marc subfield (char)
	 * @param rawValue raw value to go in marc subfield
	 * @param solrFldName name of solr field expecting value
	 * @param expSolrFldVal value expected in Solr field
	 */
	private void assertMultSolrFldValFromMarcSubfld(String fldTag, char subFld, String rawValue, String solrFldName, String[] expSolrFldVals)
	{
		Record record = factory.newRecord();
		ControlField cf008 = factory.newControlField("008", "      |||||||||");
		record.addVariableField(cf008);
		DataField df = factory.newDataField(fldTag, ' ', ' ');
	    df.addSubfield(factory.newSubfield(subFld, rawValue));
	    record.addVariableField(df);
	    solrFldMapTest.assertSolrFldHasNumValues(record, solrFldName, expSolrFldVals.length);
	    for (int i = 0; i < expSolrFldVals.length; i++)
		{
			solrFldMapTest.assertSolrFldValue(record, solrFldName, expSolrFldVals[i]);
		}
	}

	/**
	 * assert that Marc record with the field/subfield indicated populates SolrFldMap with expected Solr field value
	 * @param fldTag marc field tag (3 chars)
	 * @param subFld marc subfield (char)
	 * @param rawValue raw value to go in marc subfield
	 * @param solrFldName name of solr field expecting value
	 * @param expSolrFldVal value expected in Solr field
	 */
	private void assertNoSolrFldFromMarcSubfld(String fldTag, char subFld, String rawValue, String solrFldName)
	{
		Record record = factory.newRecord();
		ControlField cf008 = factory.newControlField("008", "      |||||||||");
		record.addVariableField(cf008);
		DataField df = factory.newDataField(fldTag, ' ', ' ');
	    df.addSubfield(factory.newSubfield(subFld, rawValue));
	    record.addVariableField(df);
	    solrFldMapTest.assertNoSolrFld(record, solrFldName);
	}

}
