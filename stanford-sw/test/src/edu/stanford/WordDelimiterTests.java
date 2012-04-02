package edu.stanford;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.solr.client.solrj.SolrServerException;
import org.junit.Test;
import org.xml.sax.SAXException;

/**
 * junit4 tests for WordDelimiterFilter settings
 * @author Naomi Dushay
 */
public class WordDelimiterTests extends AbstractStanfordTest 
{

	/**
	 * SW-388  red-rose chain   vs.  prisoner in a red-rose chain
	 */
@Test
	public final void testRedRoseHyphenSearch()
		throws ParserConfigurationException, IOException, SAXException, SolrServerException 
	{
		String fldName = "title_245a_search";
		createFreshIx("wdfSearchTest.mrc");
	
		Set<String> docIds = new HashSet<String>(2);
		docIds.add("5335304");
		docIds.add("8702148");
		
		assertSearchResults(fldName, "red-rose chain", docIds);
	}

}
