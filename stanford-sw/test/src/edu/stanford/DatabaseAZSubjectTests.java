package edu.stanford;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import javax.xml.parsers.ParserConfigurationException;

import org.junit.Before;
import org.junit.Test;
import org.xml.sax.SAXException;


/**
 * junit4 tests for Stanford University format fields for blacklight index
 * 
 * @author Naomi Dushay
 */
public class DatabaseAZSubjectTests extends AbstractStanfordTest {

	String testFilePath = testDataParentPath + File.separator + "databasesAZsubjectTests.mrc";
	String facetFldName = "db_az_subject";

@Before
	public final void setup() 
	{
		mappingTestInit();
	}

	/**
	 * test that an A-Z database with multiple 099 codes has values for all.
	 */
@Test
	public final void testMultGoodSubjects()
	{
		solrFldMapTest.assertSolrFldValue(testFilePath, "2diffsubs", facetFldName, "News");
		solrFldMapTest.assertSolrFldValue(testFilePath, "2diffsubs", facetFldName, "Science (General)");
	}

	/**
	 * test that an A-Z database unknown subject code is ignored
	 */
@Test
	public final void testBadSubjects()
	{
		solrFldMapTest.assertNoSolrFld(testFilePath, "incorrectCode", facetFldName);
		solrFldMapTest.assertNoSolrFld(testFilePath, "no099", facetFldName);
		solrFldMapTest.assertNoSolrFld(testFilePath, "099wrongSub", facetFldName);
	}

	/**
	 * test that an A-Z database unknown subject code is ignored
	 */
@Test
	public final void testGoodAndBadSubject()
	{
		solrFldMapTest.assertSolrFldHasNumValues(testFilePath, "goodAndBadCode", facetFldName, 1);
		solrFldMapTest.assertSolrFldValue(testFilePath, "goodAndBadCode", facetFldName, "News");
	}

	/**
	 * test that an A-Z database with no subjects doesn't get any
	 */
@Test
	public final void testSubjectsWithOtherDatabase()
	{
		solrFldMapTest.assertNoSolrFld(testFilePath, "otherdbW099", facetFldName);
	}

	/**
	 * test that double assigned subject codes get both their values
	 */
@Test
	public final void testDoubleAssigned()
	{
		// XM
		solrFldMapTest.assertSolrFldValue(testFilePath, "6859025", facetFldName, "Government Information: United States");

		// JK is assigned to both American History and Political Science
		solrFldMapTest.assertSolrFldValue(testFilePath, "6859025", facetFldName, "American History");
		solrFldMapTest.assertSolrFldValue(testFilePath, "6859025", facetFldName, "Political Science");

		solrFldMapTest.assertSolrFldHasNumValues(testFilePath, "6859025", facetFldName, 3);
	}

    /**
     *A-Z database subjects should be searchable with terms (not whole String)
     */
@Test
    public final void testSearched() 
    		throws ParserConfigurationException, IOException, SAXException
    {
		createFreshIx("databasesAZsubjectTests.mrc");
		String fldName = "db_az_subject_search";
		
		Set<String> docIds = new HashSet<String>();
		docIds.add("2diffsubs");
		docIds.add("6859025");
		assertSearchResults(fldName, "Science", docIds);

		docIds.remove("6859025");
		docIds.add("goodAndBadCode");
		assertSearchResults(fldName, "News", docIds);

		assertSingleResult("2diffsubs", fldName, "General");
		assertSingleResult("6859025", fldName, "Government");
		// double assigning subject code JK
		assertSingleResult("6859025", fldName, "History");
		assertSingleResult("6859025", fldName, "Political");
    }
    


}
