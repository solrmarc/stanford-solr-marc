package edu.stanford;

import java.io.File;

import org.junit.Before;
import org.junit.Test;


/**
 * junit4 tests for Stanford University format fields for blacklight index
 * 
 * @author Naomi Dushay
 */
public class DatabaseAZSubjectTests extends AbstractStanfordBlacklightTest {

	String testFilePath = testDataParentPath + File.separator + "databasesAZsubjectTests.mrc";
	String facetFldName = "dbAZsubjects_facet";

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

}
