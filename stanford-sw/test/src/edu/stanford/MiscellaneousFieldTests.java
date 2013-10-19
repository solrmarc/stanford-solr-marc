package edu.stanford;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.common.SolrDocument;
import org.xml.sax.SAXException;

import org.junit.*;
import org.marc4j.marc.*;

import static org.junit.Assert.*;


/**
 * junit4 tests for Stanford University revisions to solrmarc
 * @author Naomi Dushay
 */
public class MiscellaneousFieldTests extends AbstractStanfordTest
{
	/**
	 * Test correct document id - the id is from 001 with an a in front
	 */
@Test
	public final void testId()
		throws ParserConfigurationException, SAXException, IOException, SolrServerException
	{
		closeSolrProxy();  // need to reset the solrProxy to get the right request handling
		createFreshIx("idTests.mrc", true, false);
		String fldName = "id";

        int numDocs = getNumMatchingDocs("collection", "sirsi");
        assertEquals("Number of documents in index incorrect: ", 3, numDocs);
        assertDocNotPresent("001noSubNo004");
        assertDocPresent("001suba");
        assertDocNotPresent("001and004nosub");
        assertDocNotPresent("004noSuba");
        assertDocPresent("001subaAnd004nosub");
        assertDocNotPresent("004noSuba");
        assertDocPresent("001subaAnd004suba");
        assertDocNotPresent("004suba");

        assertSingleResult("001suba", fldName, "\"001suba\"");
        assertSingleResult("001subaAnd004nosub", fldName, "\"001subaAnd004nosub\"");
        assertSingleResult("001subaAnd004suba", fldName, "\"001subaAnd004suba\"");
	}


	/**
	 * Test that there is no field created when the translation map is missing
	 *  the value to be mapped and when the map has value set to null
	 */
@Test
	public final void testMapMissingValue()
			throws ParserConfigurationException, IOException, SAXException, SolrServerException
	{
		String fldName = "language";
		createFreshIx("langTests.mrc");

		assertZeroResults(fldName, "null");
		assertZeroResults(fldName, "\\?\\?\\?");
		assertZeroResults(fldName, "mis");     // 008mis041ak
		assertZeroResults(fldName, "Miscellaneous languages");
		assertZeroResults(fldName, "mul");     // 008mul041atha
		assertZeroResults(fldName, "Multiple languages");
		assertZeroResults(fldName, "und");
		assertZeroResults(fldName, "zxx");
	}


	/**
	 * display_type is supposed to be a sort of "hidden" facet to allow UI
	 *  to look at appropriate types of records for different "views"
	 *  (e.g.  Images, Maps, Book Reader ...)
	 */
@Test
	public final void testDisplayTypeField()
	    throws ParserConfigurationException, IOException, SAXException, SolrServerException
	{
		closeSolrProxy();  // need to reset the solrProxy to get the right request handling
		createFreshIx("idTests.mrc", true, false);
	    String fldName = "display_type";

	    // all MARC records from Symphony
        assertEquals("docs aren't all display_type sirsi", 3, getNumMatchingDocs(fldName, "sirsi"));
	}


	/**
	 * test preservation of field ordering from marc input to marc stored in record
	 */
@Test
	public final void testFieldOrdering()
			throws ParserConfigurationException, IOException, SAXException, SolrServerException
	{
		createFreshIx("fieldOrdering.mrc");
		SolrDocument doc = getDocument("1");
		String marc21 = (String) doc.getFirstValue("marcxml");
		int ix650 = marc21.indexOf("650first");
		int ix600 = marc21.indexOf("600second");
		assertTrue("fields are NOT in the original order", ix650 < ix600);
	}

}
