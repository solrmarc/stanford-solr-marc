package edu.stanford;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.solr.client.solrj.SolrServerException;
import org.junit.*;
import org.xml.sax.SAXException;

/**
 * tests for Notes fields, such as TOC, Summary, Context, Awards ...
 * 
 * @author Naomi Dushay
 */
public class NoteFieldsTests extends AbstractStanfordTest 
{
    /**
     * test population of table of contents search field
     */
@Test
    public final void testTOCSearchField() 
        throws ParserConfigurationException, IOException, SAXException, SolrServerException
    {
        String fldName = "toc_search";
	    createFreshIx("summaryTests.mrc");

        assertSingleResult("505", fldName, "505a");
        assertSingleResult("505", fldName, "505r");
        assertSingleResult("505", fldName, "505t");
		
		assertZeroResults(fldName, "nope");
    }

	/**
	 * vern_toc_search:  check all vernacular search subfields for 505
	 */
@Test
	public final void vernTocSearchAllSubfields()
			throws ParserConfigurationException, IOException, SAXException, SolrServerException 
	{
		String fldName = "vern_toc_search";
		createFreshIx("summaryTests.mrc");

		assertSingleResult("505", fldName, "vern505a");
        assertSingleResult("505", fldName, "vern505r");
        assertSingleResult("505", fldName, "vern505t");
		
		assertZeroResults(fldName, "nope");
	}

	/**
	 * Nielsen data is in the 905 we want to index both 905 and 505
	 */
@Test
	public final void nielsenTOCSearchField() 
	    throws ParserConfigurationException, IOException, SAXException, SolrServerException
	{
	    String fldName = "toc_search";
	    createFreshIx("nielsenTests.mrc");
	    
		Set<String> docIds = new HashSet<String>();
		docIds.add("505");
		docIds.add("bothx05");
		assertSearchResults(fldName, "505a", docIds);
		assertSearchResults(fldName, "505r", docIds);
		assertSearchResults(fldName, "505t", docIds);
	    
		docIds = new HashSet<String>();
		docIds.add("905");
		docIds.add("bothx05");
		assertSearchResults(fldName, "905a", docIds);
		assertSearchResults(fldName, "905r", docIds);
		assertSearchResults(fldName, "905t", docIds);

	    // don't index subfields g or u
		assertZeroResults(fldName, "505g");
		assertZeroResults(fldName, "505u");
		assertZeroResults(fldName, "905g");
		assertZeroResults(fldName, "905u");
	}


	/**
	 * test population of context search field
	 */
@Test
	public final void testContextSearchField() 
	    throws ParserConfigurationException, IOException, SAXException, SolrServerException
	{
	    String fldName = "context_search";
	    createFreshIx("summaryTests.mrc");

	    assertSingleResult("518", fldName, "518a");

		assertZeroResults(fldName, "nope");
	}
	
	/**
	 * test population of context search field
	 */
@Test
	public final void testVernContextSearchField() 
	    throws ParserConfigurationException, IOException, SAXException, SolrServerException
	{
	    String fldName = "vern_context_search";
	    createFreshIx("summaryTests.mrc");

	    assertSingleResult("518", fldName, "vern518a");

		assertZeroResults(fldName, "nope");
	}
	
	/**
	 * test population of summary search field
	 */
@Test
	public final void testSummarySearchField() 
	    throws ParserConfigurationException, IOException, SAXException, SolrServerException
	{
	    String fldName = "summary_search";
	    createFreshIx("summaryTests.mrc");
	    
		assertSingleResult("520", fldName, "520a");
		assertSingleResult("520", fldName, "520b");

		assertZeroResults(fldName, "nope");
	}
	
	/**
	 * test population of vernacular summary search field 
	 */
@Test
	public final void testVernSummarySearchField() 
	    throws ParserConfigurationException, IOException, SAXException, SolrServerException
	{
	    String fldName = "vern_summary_search";
	    createFreshIx("summaryTests.mrc");

	    assertSingleResult("520", fldName, "vern520a");
		assertSingleResult("520", fldName, "vern520b");

		assertZeroResults(fldName, "nope");
	}

	/**
	 * Nielsen data is in the 920; we want to index both 920 and 520
	 */
@Test
	public final void nielsenSummarySearchField() 
	    throws ParserConfigurationException, IOException, SAXException, SolrServerException
	{
	    String fldName = "summary_search";
	    createFreshIx("nielsenTests.mrc");
	    
		Set<String> docIds = new HashSet<String>();
		docIds.add("520");
		docIds.add("bothx20");
		assertSearchResults(fldName, "520a", docIds);
		assertSearchResults(fldName, "520b", docIds);
	    
		docIds = new HashSet<String>();
		docIds.add("920");
		docIds.add("bothx20");
		assertSearchResults(fldName, "920a", docIds);
		assertSearchResults(fldName, "920b", docIds);
	
	    // don't index subfields g or u
		assertZeroResults(fldName, "520c");
		assertZeroResults(fldName, "520u");
		assertZeroResults(fldName, "920c");
		assertZeroResults(fldName, "920u");
	}


	/**
	 * Nielsen data is in the 986;  we want to index both 986 and 586
	 */
@Test
	public final void nielsenAwardSearchField() 
	    throws ParserConfigurationException, IOException, SAXException, SolrServerException
	{
	    String fldName = "award_search";
	    createFreshIx("nielsenTests.mrc");
	    
	    assertSingleResult("586", fldName, "New Zealand Post book awards winner");
	    assertSingleResult("586", fldName, "\"586 second award\"");
	    assertSingleResult("986", fldName, "\"Shortlisted for Montana New Zealand Book Awards\\: History Category 2006.\"");
	    assertSingleResult("986", fldName, "\"986 second award\"");
	    assertSingleResult("one586two986", fldName, "\"986 award1\"");
	    assertSingleResult("one586two986", fldName, "\"986 award2\"");
	    assertSingleResult("one586two986", fldName, "\"586 award\"");
	    assertSingleResult("two586one986", fldName, "\"986 single award\"");
	    assertSingleResult("two586one986", fldName, "\"586 1award\"");
	    assertSingleResult("two586one986", fldName, "\"586 1award\"");
	}

}
