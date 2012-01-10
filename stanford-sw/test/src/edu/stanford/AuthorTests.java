package edu.stanford;

import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.junit.*;
import org.xml.sax.SAXException;

/**
 * junit4 tests for Stanford University author fields for blacklight index
 * @author Naomi Dushay
 */
public class AuthorTests extends AbstractStanfordTest {

@Before
	public final void setup() 
	{
		mappingTestInit();
	}
	
// TODO:  problem with eclipse and encoding for non latin  :-(

	/**
	 * Personal name display field tests.
	 */
@Test
	public final void testPersonalNameDisplay() 
	{
		String fldName = "author_person_display";
		String testFilePath = testDataParentPath + File.separator + "authorTests.mrc";

		// 100a
		// trailing period removed
	    solrFldMapTest.assertSolrFldValue(testFilePath, "345228", fldName, "Bashkov, Vladimir"); 
		// 100ad
		// trailing hyphen retained
		solrFldMapTest.assertSolrFldValue(testFilePath, "919006", fldName, "Oeftering, Michael, 1872-"); 
		// 100ae  (e not indexed)
		// trailing comma should be removed
		solrFldMapTest.assertSolrFldValue(testFilePath, "7651581", fldName, "Coutinho, Frederico dos Reys"); 
		// 100aqd 
		// trailing period removed
		solrFldMapTest.assertSolrFldValue(testFilePath, "690002", fldName, "Wallin, J. E. Wallace (John Edward Wallace), b. 1876");
		// 100aqd 
		solrFldMapTest.assertSolrFldValue(testFilePath, "1261173", fldName, "Johnson, Samuel, 1649-1703");
		// 'nother sort of trailing period - not removed
		solrFldMapTest.assertSolrFldValue(testFilePath, "8634", fldName, "Sallust, 86-34 B.C.");
		// 100 with numeric subfield
		solrFldMapTest.assertSolrFldValue(testFilePath, "1006", fldName, "Sox on Fox");
		// 100 6a x 2  123456 - non latin - not sure how to express here
	}

	/**
	 * Full personal name display field tests.
	 */
@Test
	public final void testFullPersonalNameDisplay() 
	{
		String fldName = "author_person_full_display";
		String testFilePath = testDataParentPath + File.separator + "authorTests.mrc";
		// 100ae 
		solrFldMapTest.assertSolrFldValue(testFilePath, "7651581", fldName, "Coutinho, Frederico dos Reys, ed."); 
		
		testFilePath = testDataParentPath + File.separator + "displayFieldsTests.mrc";
		solrFldMapTest.assertSolrFldValue(testFilePath, "1001", fldName, "Seuss, Dr."); 
		solrFldMapTest.assertSolrFldValue(testFilePath, "1002", fldName, "Fowler, T. M. (Thaddeus Mortimer) 1842-1922."); 
		solrFldMapTest.assertSolrFldValue(testFilePath, "1003", fldName, "Bach, Johann Sebastian."); 
	}

	/**
	 * Corporate name display field tests.
	 */
@Test
	public final void testCorpNameDisplay() 
	{
		String fldName = "author_corp_display";
		String testFilePath = testDataParentPath + File.separator + "authorTests.mrc";
		// 110 
		solrFldMapTest.assertSolrFldValue(testFilePath, "NYPL", fldName, "New York Public Library."); 
		solrFldMapTest.assertSolrFldValue(testFilePath, "5511738", fldName, "United States. Congress. House. Committee on Agriculture. Subcommittee on Department Operations, Oversight, Nutrition, and Forestry."); 
		solrFldMapTest.assertSolrFldValue(testFilePath, "4578538", fldName, "Stanford University. Stanford Electronics Laboratories. SEL-69-048."); 

		testFilePath = testDataParentPath + File.separator + "displayFieldsTests.mrc";
		solrFldMapTest.assertSolrFldValue(testFilePath, "110", fldName, "United States. Congress (97th, 2nd session : 1982). House."); 
	}

	/**
	 * Meeting name display field tests.
	 */
@Test
	public final void testMeetingNameDisplay() 
	{
		String fldName = "author_meeting_display";
		String testFilePath = testDataParentPath + File.separator + "authorTests.mrc";

		// 111a
		solrFldMapTest.assertSolrFldValue(testFilePath, "111faim", fldName, "FAIM (Forum).");
		// 111 andc
		solrFldMapTest.assertSolrFldValue(testFilePath, "5666387", fldName, "International Jean Sibelius Conference (3rd : 2000 : Helsinki, Finland)");
	}


	/**
	 * Personal name facet field tests, especially removal of trailing 
	 * punctuation
	 */
@Test
	public final void testPersonalNameFacet() 
	{
		String fldName = "author_person_facet";
		String testFilePath = testDataParentPath + File.separator + "authorTests.mrc";

		// 100
		// trailing period that isn't an initial should be removed
		solrFldMapTest.assertSolrFldValue(testFilePath, "345228", fldName, "Bashkov, Vladimir");
		solrFldMapTest.assertSolrFldHasNoValue(testFilePath, "345228", fldName, "Bashkov, Vladimir.");
		solrFldMapTest.assertSolrFldValue(testFilePath, "690002", fldName, "Wallin, J. E. Wallace (John Edward Wallace), b. 1876");
		solrFldMapTest.assertSolrFldHasNoValue(testFilePath, "690002", fldName, "Wallin, J. E. Wallace (John Edward Wallace), b. 1876.");
		// this trailing period should be left in
		solrFldMapTest.assertSolrFldValue(testFilePath, "8634", fldName, "Sallust, 86-34 B.C.");
		solrFldMapTest.assertSolrFldHasNoValue(testFilePath, "8634", fldName, "Sallust, 86-34 B.C");
		// trailing hyphen should be left in
		solrFldMapTest.assertSolrFldValue(testFilePath, "919006", fldName, "Oeftering, Michael, 1872-");
		solrFldMapTest.assertSolrFldHasNoValue(testFilePath, "919006", fldName, "Oeftering, Michael, 1872");
		// trailing comma should be removed
		solrFldMapTest.assertSolrFldValue(testFilePath, "7651581", fldName, "Coutinho, Frederico dos Reys");
		solrFldMapTest.assertSolrFldHasNoValue(testFilePath, "7651581", fldName, "Coutinho, Frederico dos Reys,");
		// 700
		// remove trailing period
		solrFldMapTest.assertSolrFldValue(testFilePath, "4428936", fldName, "Zagarrio, Vito");
		solrFldMapTest.assertSolrFldHasNoValue(testFilePath, "4428936", fldName, "Zagarrio, Vito.");
		// jackpot: comma, period, hyphen
		solrFldMapTest.assertSolrFldValue(testFilePath, "700friedman", fldName, "Friedman, Eli A., 1933-");
		solrFldMapTest.assertSolrFldHasNoValue(testFilePath, "700friedman", fldName, "Friedman, Eli A., 1933-,.");
		// two 700s keep one trailing period, remove one
		solrFldMapTest.assertSolrFldValue(testFilePath, "harrypotter", fldName, "Heyman, David");
		solrFldMapTest.assertSolrFldValue(testFilePath, "harrypotter", fldName, "Rowling, J. K.");
		solrFldMapTest.assertSolrFldHasNoValue(testFilePath, "harrypotter", fldName, "Heyman, David.");
		solrFldMapTest.assertSolrFldHasNoValue(testFilePath, "harrypotter", fldName, "Rowling, J. K");
		// 100 and 700
		solrFldMapTest.assertSolrFldValue(testFilePath, "700sayers", fldName, "Whimsey, Peter");
		solrFldMapTest.assertSolrFldValue(testFilePath, "700sayers", fldName, "Sayers, Dorothy L. (Dorothy Leigh), 1893-1957");
		solrFldMapTest.assertSolrFldHasNoValue(testFilePath, "700sayers", fldName, "Whimsey, Peter,");
		solrFldMapTest.assertSolrFldHasNoValue(testFilePath, "700sayers", fldName, "Sayers, Dorothy L. (Dorothy Leigh), 1893-1957.");
		
		// no 800
		solrFldMapTest.assertSolrFldHasNoValue(testFilePath, "800", fldName, "Darnell, Jack");
		solrFldMapTest.assertNoSolrFld(testFilePath, "800", fldName);
		
		solrFldMapTest.assertSolrFldValue(testFilePath, "1261173", fldName, "Johnson, Samuel, 1649-1703");
	}


	/**
	 * Corporate name facet field tests, especially removal of trailing 
	 * punctuation
	 */
@Test
	public final void testCorporateNameFacet() 
	{
		String fldName = "author_corp_facet";
		String testFilePath = testDataParentPath + File.separator + "authorTests.mrc";
	
		// 110 - trailing period to remove
		solrFldMapTest.assertSolrFldValue(testFilePath, "110foo", fldName, "SAFE Association (U.S.). Symposium");
		solrFldMapTest.assertSolrFldHasNoValue(testFilePath, "110foo", fldName, "SAFE Association (U.S.). Symposium.");
		solrFldMapTest.assertSolrFldValue(testFilePath, "NYPL", fldName, "New York Public Library");
		solrFldMapTest.assertSolrFldHasNoValue(testFilePath, "NYPL", fldName, "New York Public Library.");
    	// 710 - trailing period to leave in
		solrFldMapTest.assertSolrFldValue(testFilePath, "6280316", fldName, "Julius Bien & Co.");
		solrFldMapTest.assertSolrFldHasNoValue(testFilePath, "6280316", fldName, "Julius Bien & Co");
		solrFldMapTest.assertSolrFldValue(testFilePath, "57136914", fldName, "NetLibrary, Inc.");
		solrFldMapTest.assertSolrFldHasNoValue(testFilePath, "57136914", fldName, "NetLibrary, Inc");
    	// 710 - last char paren
		solrFldMapTest.assertSolrFldValue(testFilePath, "987666", fldName, "(this was a value in a non-latin script)");
		solrFldMapTest.assertSolrFldHasNoValue(testFilePath, "987666", fldName, "(this was a value in a non-latin script");
		solrFldMapTest.assertSolrFldValue(testFilePath, "710corpname", fldName, "Warner Bros. Pictures (1969- )");
		solrFldMapTest.assertSolrFldHasNoValue(testFilePath, "710corpname", fldName, "Warner Bros. Pictures (1969- ");
		solrFldMapTest.assertSolrFldHasNoValue(testFilePath, "710corpname", fldName, "Warner Bros. Pictures (1969-");
		// 710 - leading space
		solrFldMapTest.assertSolrFldValue(testFilePath, "710corpname", fldName, "Heyday Films");
		solrFldMapTest.assertSolrFldHasNoValue(testFilePath, "710corpname", fldName, " Heyday Films.");
		solrFldMapTest.assertSolrFldHasNoValue(testFilePath, "710corpname", fldName, "Heyday Films.");
		
		// 110 and 710
		solrFldMapTest.assertSolrFldValue(testFilePath, "110710corpname", fldName, "Thelma");
		solrFldMapTest.assertSolrFldHasNoValue(testFilePath, "110710corpname", fldName, "Thelma.");
		solrFldMapTest.assertSolrFldValue(testFilePath, "110710corpname", fldName, "Roaring Woman, Louise. 2000-2001");
		solrFldMapTest.assertSolrFldHasNoValue(testFilePath, "110710corpname", fldName, "Roaring Woman, Louise");
		solrFldMapTest.assertSolrFldHasNoValue(testFilePath, "110710corpname", fldName, "Roaring Woman, Louise. 2000-2001.");

		// 810 not included
		solrFldMapTest.assertSolrFldHasNoValue(testFilePath, "810", fldName, "American Academy in Rome");
		solrFldMapTest.assertNoSolrFld(testFilePath, "810", fldName);
	}

	/**
	 * Meeting name facet tests, especially removal of trailing punctuation
	 */
@Test
	public final void testMeetingNameFacet() 
	{
		String fldName = "author_meeting_facet";
		String testFilePath = testDataParentPath + File.separator + "authorTests.mrc";
	
		// 111
		solrFldMapTest.assertSolrFldValue(testFilePath, "111faim", fldName, "FAIM (Forum)");
		solrFldMapTest.assertSolrFldHasNoValue(testFilePath, "111faim", fldName, "FAIM (Forum).");
		solrFldMapTest.assertSolrFldHasNoValue(testFilePath, "111faim", fldName, "FAIM (Forum");
		// 111 sub a n d c  - last char paren
		solrFldMapTest.assertSolrFldValue(testFilePath, "5666387", fldName, "International Jean Sibelius Conference (3rd : 2000 : Helsinki, Finland)");
		solrFldMapTest.assertSolrFldHasNoValue(testFilePath, "5666387", fldName, "International Jean Sibelius Conference (3rd : 2000 : Helsinki, Finland");
		
		// 711
		solrFldMapTest.assertSolrFldValue(testFilePath, "711", fldName, "European Conference on Computer Vision (2006 : Graz, Austria)");
		solrFldMapTest.assertSolrFldHasNoValue(testFilePath, "711", fldName, "European Conference on Computer Vision (2006 : Graz, Austria");
		
		// 811 not included
		solrFldMapTest.assertSolrFldHasNoValue(testFilePath, "811", fldName, "Delaware Symposium on Language Studies");
		solrFldMapTest.assertNoSolrFld(testFilePath, "811", fldName);
	}


	/**
	 * Other (meeting and corporate, not person) name facet tests, including 
	 *  removal of trailing punctuation
	 * NOTE:  copy field - must test as search

	 */
@Test
	public final void testAuthorOtherFacet()
			throws ParserConfigurationException, IOException, SAXException, SolrServerException
	{
		String fldName = "author_other_facet";
		createFreshIx("authorTests.mrc");
	
		// 110 - trailing period to remove
		assertSingleResult("110foo", fldName, "\"SAFE Association (U.S.). Symposium\"");
		assertZeroResults(fldName, "\"SAFE Association (U.S.). Symposium.\"");
		assertSingleResult("NYPL", fldName, "\"New York Public Library\"");
		assertZeroResults(fldName, "\"New York Public Library.\"");
		// 111
		assertSingleResult("111faim", fldName, "\"FAIM (Forum)\"");
		assertZeroResults(fldName, "\"FAIM (Forum).\"");
		assertZeroResults(fldName, "\"FAIM (Forum\"");
		// 111 sub a n d c  - last char paren
		assertSingleResult("5666387", fldName, "\"International Jean Sibelius Conference (3rd : 2000 : Helsinki, Finland)\"");
		assertZeroResults(fldName, "\"International Jean Sibelius Conference (3rd : 2000 : Helsinki, Finland\"");
		
    	// 710 - trailing period to leave in
		assertSingleResult("6280316", fldName, "\"Julius Bien & Co.\"");
		assertZeroResults(fldName, "\"Julius Bien & Co\"");
		assertSingleResult("57136914", fldName, "\"NetLibrary, Inc.\"");
		assertZeroResults(fldName, "\"NetLibrary, Inc\"");
    	// 710 - last char paren
		assertSingleResult("987666", fldName, "\"(this was a value in a non-latin script)\"");
		assertZeroResults(fldName, "\"(this was a value in a non-latin script\"");
		assertSingleResult("710corpname", fldName, "\"Warner Bros. Pictures (1969- )\"");
		assertZeroResults(fldName, "\"Warner Bros. Pictures (1969- \"");
		assertZeroResults(fldName, "\"Warner Bros. Pictures (1969-\"");
		// 710 - leading space
		assertSingleResult("710corpname", fldName, "\"Heyday Films\"");
		assertZeroResults(fldName, "\" Heyday Films.\"");
		assertZeroResults(fldName, "\"Heyday Films.\"");
		// 711
		assertSingleResult("711", fldName, "\"European Conference on Computer Vision (2006 : Graz, Austria)\"");
		assertZeroResults(fldName, "\"European Conference on Computer Vision (2006 : Graz, Austria\"");
		
		// 110 and 710
		assertSingleResult("110710corpname", fldName, "\"Thelma\"");
		assertZeroResults(fldName, "\"Thelma.\"");
		assertSingleResult("110710corpname", fldName, "\"Roaring Woman, Louise. 2000-2001\"");
		assertZeroResults(fldName, "\"Roaring Woman, Louise\"");
		assertZeroResults(fldName, "\"Roaring Woman, Louise. 2000-2001.\"");

		// 810 not included
		assertZeroResults(fldName, "\"American Academy in Rome\"");
		// 811 not included
		assertZeroResults(fldName, "\"Delaware Symposium on Language Studies\"");
	}


/**
	 * author_1xx_search:  check all search subfields for 100, 110, 111
	 */
@Test
	public final void test1xxSearchAllSubfields()
			throws ParserConfigurationException, IOException, SAXException, SolrServerException 
	{
		String fldName = "author_1xx_search";
		createFreshIx("authorTests.mrc");
		assertSingleResult("100search", fldName, "100a");
		assertSingleResult("100search", fldName, "100b");
		assertSingleResult("100search", fldName, "100c");
		assertSingleResult("100search", fldName, "100d");
		assertSingleResult("100search", fldName, "100g");
		assertSingleResult("100search", fldName, "100j");
		assertSingleResult("100search", fldName, "100q");
		assertSingleResult("100search", fldName, "100u");

		assertSingleResult("110search", fldName, "110a");
		assertSingleResult("110search", fldName, "110b");
		assertSingleResult("110search", fldName, "110c");
		assertSingleResult("110search", fldName, "110d");
		assertSingleResult("110search", fldName, "110g");
		assertSingleResult("110search", fldName, "110n");
		assertSingleResult("110search", fldName, "110u");

		assertSingleResult("111search", fldName, "111a");
		assertSingleResult("111search", fldName, "111c");
		assertSingleResult("111search", fldName, "111d");
		assertSingleResult("111search", fldName, "111e");
		assertSingleResult("111search", fldName, "111g");
		assertSingleResult("111search", fldName, "111j");
		assertSingleResult("111search", fldName, "111n");
		assertSingleResult("111search", fldName, "111q");
		assertSingleResult("111search", fldName, "111u");
		
		assertZeroResults(fldName, "100e");
		assertZeroResults(fldName, "110e");
		assertZeroResults(fldName, "110f");
		assertZeroResults(fldName, "110k");
		assertZeroResults(fldName, "111i");
		assertZeroResults(fldName, "none");
	}

	/**
	 * vern_author_1xx_search:  check all search subfields for 100, 110, 111
	 */
@Test
	public final void vern1xxSearchAllSubfields()
			throws ParserConfigurationException, IOException, SAXException, SolrServerException 
	{
		String fldName = "vern_author_1xx_search";
		createFreshIx("vernacularSearchTests.mrc");
		
		assertSingleResult("100VernSearch", fldName, "vern100a");
		assertSingleResult("100VernSearch", fldName, "vern100b");
		assertSingleResult("100VernSearch", fldName, "vern100c");
		assertSingleResult("100VernSearch", fldName, "vern100d");
		assertSingleResult("100VernSearch", fldName, "vern100g");
		assertSingleResult("100VernSearch", fldName, "vern100j");
		assertSingleResult("100VernSearch", fldName, "vern100q");
		assertSingleResult("100VernSearch", fldName, "vern100u");
	
		assertSingleResult("110VernSearch", fldName, "vern110a");
		assertSingleResult("110VernSearch", fldName, "vern110b");
		assertSingleResult("110VernSearch", fldName, "vern110c");
		assertSingleResult("110VernSearch", fldName, "vern110d");
		assertSingleResult("110VernSearch", fldName, "vern110g");
		assertSingleResult("110VernSearch", fldName, "vern110n");
		assertSingleResult("110VernSearch", fldName, "vern110u");
	
		assertSingleResult("111VernSearch", fldName, "vern111a");
		assertSingleResult("111VernSearch", fldName, "vern111c");
		assertSingleResult("111VernSearch", fldName, "vern111d");
		assertSingleResult("111VernSearch", fldName, "vern111e");
		assertSingleResult("111VernSearch", fldName, "vern111g");
		assertSingleResult("111VernSearch", fldName, "vern111j");
		assertSingleResult("111VernSearch", fldName, "vern111n");
		assertSingleResult("111VernSearch", fldName, "vern111q");
		assertSingleResult("111VernSearch", fldName, "vern111u");
	
		assertZeroResults(fldName, "vern100e");
		assertZeroResults(fldName, "vern110e");
		assertZeroResults(fldName, "vern110f");
		assertZeroResults(fldName, "vern110k");
		assertZeroResults(fldName, "vern111i");
		assertZeroResults(fldName, "none");
	}

	/**
	 * author_7xx_search: personal name fields 
	 *  check all search subfields for 700, 720, 796
	 */
@Test
	public final void test7xxPersonSearchAllSubfields()
			throws ParserConfigurationException, IOException, SAXException, SolrServerException 
	{
		String fldName = "author_7xx_search";
		createFreshIx("authorTests.mrc");
		assertSingleResult("7xxPersonSearch", fldName, "700a");
		assertSingleResult("7xxPersonSearch", fldName, "700b");
		assertSingleResult("7xxPersonSearch", fldName, "700c");
		assertSingleResult("7xxPersonSearch", fldName, "700d");
		assertSingleResult("7xxPersonSearch", fldName, "700g");
		assertSingleResult("7xxPersonSearch", fldName, "700j");
		assertSingleResult("7xxPersonSearch", fldName, "700q");
		assertSingleResult("7xxPersonSearch", fldName, "700u");
			
		assertSingleResult("7xxPersonSearch", fldName, "720a");
		assertSingleResult("7xxPersonSearch", fldName, "720e");

		assertSingleResult("7xxPersonSearch", fldName, "796a");
		assertSingleResult("7xxPersonSearch", fldName, "796b");
		assertSingleResult("7xxPersonSearch", fldName, "796c");
		assertSingleResult("7xxPersonSearch", fldName, "796d");
		assertSingleResult("7xxPersonSearch", fldName, "796g");
		assertSingleResult("7xxPersonSearch", fldName, "796j");
		assertSingleResult("7xxPersonSearch", fldName, "796q");
		assertSingleResult("7xxPersonSearch", fldName, "796u");

		assertZeroResults(fldName, "700e");
		assertZeroResults(fldName, "796e");
		assertZeroResults(fldName, "none");
	}

	/**
	 * vern_author_7xx_search: personal name fields 
	 *  check all search subfields for 700, 720, 796
	 */
@Test
	public final void vern7xxPersonSearchAllSubfields()
			throws ParserConfigurationException, IOException, SAXException, SolrServerException 
	{
		String fldName = "vern_author_7xx_search";
		createFreshIx("vernacularSearchTests.mrc");
		assertSingleResult("7xxVernPersonSearch", fldName, "vern700a");
		assertSingleResult("7xxVernPersonSearch", fldName, "vern700b");
		assertSingleResult("7xxVernPersonSearch", fldName, "vern700c");
		assertSingleResult("7xxVernPersonSearch", fldName, "vern700d");
		assertSingleResult("7xxVernPersonSearch", fldName, "vern700q");
		assertSingleResult("7xxVernPersonSearch", fldName, "vern700u");
			
		Set<String> docIds = new HashSet<String>();
		docIds.add("7xxLowVernSearch");
		docIds.add("7xxVernPersonSearch");
		// overlap title
		assertSearchResults(fldName, "vern700g", docIds); 
		// used to be in title
		assertSearchResults(fldName, "vern700j", docIds);
	
		assertSingleResult("7xxVernPersonSearch", fldName, "vern720a");
		assertSingleResult("7xxVernPersonSearch", fldName, "vern720e");
	
		assertSingleResult("7xxVernPersonSearch", fldName, "vern796a");
		assertSingleResult("7xxVernPersonSearch", fldName, "vern796b");
		assertSingleResult("7xxVernPersonSearch", fldName, "vern796c");
		assertSingleResult("7xxVernPersonSearch", fldName, "vern796d");
		assertSingleResult("7xxVernPersonSearch", fldName, "vern796q");
		assertSingleResult("7xxVernPersonSearch", fldName, "vern796u");
		
		docIds.remove("7xxLowVernSearch");
		docIds.add("79xVernSearch");
		// overlap title
		assertSearchResults(fldName, "vern796g", docIds); 
		// used to be in title
		assertSearchResults(fldName, "vern796j", docIds);

		assertZeroResults(fldName, "vern700e"); // removed per RDA and phrase searching
		assertZeroResults(fldName, "vern796e"); // removed per RDA and phrase searching
		assertZeroResults(fldName, "none");
	}

	/**
	 * author_7xx_search: corporate name fields 
	 *  check all search subfields for 710, 797
	 */
@Test
	public final void test7xxCorpSearchAllSubfields()
			throws ParserConfigurationException, IOException, SAXException, SolrServerException 
	{
		String fldName = "author_7xx_search";
		createFreshIx("authorTests.mrc");
	
		assertSingleResult("7xxCorpSearch", fldName, "710a");
		assertSingleResult("7xxCorpSearch", fldName, "710b");
		assertSingleResult("7xxCorpSearch", fldName, "710c");
		assertSingleResult("7xxCorpSearch", fldName, "710d");
		assertSingleResult("7xxCorpSearch", fldName, "710g");
		assertSingleResult("7xxCorpSearch", fldName, "710n");
		assertSingleResult("7xxCorpSearch", fldName, "710u");

		assertSingleResult("7xxCorpSearch", fldName, "797a");
		assertSingleResult("7xxCorpSearch", fldName, "797b");
		assertSingleResult("7xxCorpSearch", fldName, "797c");
		assertSingleResult("7xxCorpSearch", fldName, "797d");
		assertSingleResult("7xxCorpSearch", fldName, "797g");
		assertSingleResult("7xxCorpSearch", fldName, "797n");
		assertSingleResult("7xxCorpSearch", fldName, "797u");
		
		assertZeroResults(fldName, "710e");
		assertZeroResults(fldName, "710f");
		assertZeroResults(fldName, "710k");
		assertZeroResults(fldName, "797e");
		assertZeroResults(fldName, "797f");
		assertZeroResults(fldName, "797k");
		assertZeroResults(fldName, "none");
	}

	/**
	 * vern_author_7xx_search: corporate name fields 
	 *  check all search subfields for 710, 797
	 */
@Test
	public final void vern7xxCorpSearchAllSubfields()
			throws ParserConfigurationException, IOException, SAXException, SolrServerException 
	{
		String fldName = "vern_author_7xx_search";
		createFreshIx("vernacularSearchTests.mrc");
	
		assertSingleResult("7xxVernCorpSearch", fldName, "vern710a");
		assertSingleResult("7xxVernCorpSearch", fldName, "vern710b");
		assertSingleResult("7xxVernCorpSearch", fldName, "vern710c");
		assertSingleResult("7xxVernCorpSearch", fldName, "vern710u");
	
		Set<String> docIds = new HashSet<String>();
		docIds.add("7xxLowVernSearch");
		docIds.add("7xxVernCorpSearch");
		// overlap title
		assertSearchResults(fldName, "vern710d", docIds); 
		assertSearchResults(fldName, "vern710g", docIds); 
		assertSearchResults(fldName, "vern710n", docIds);
	
		assertSingleResult("7xxVernCorpSearch", fldName, "vern797a");
		assertSingleResult("7xxVernCorpSearch", fldName, "vern797b");
		assertSingleResult("7xxVernCorpSearch", fldName, "vern797c");
		assertSingleResult("7xxVernCorpSearch", fldName, "vern797u");
		
		docIds.remove("7xxLowVernSearch");
		docIds.add("79xVernSearch");
		// overlap title
		assertSearchResults(fldName, "vern797d", docIds); 
		assertSearchResults(fldName, "vern797g", docIds); 
		assertSearchResults(fldName, "vern797n", docIds);
		
		assertZeroResults(fldName, "vern710e");
		assertZeroResults(fldName, "vern710f");
		assertZeroResults(fldName, "vern710k");
		assertZeroResults(fldName, "vern797e");
		assertZeroResults(fldName, "vern797f");
		assertZeroResults(fldName, "vern797k");
		assertZeroResults(fldName, "none");
	}


	/**
	 * author_7xx_search: meeting name fields 
	 *  check all search subfields for 711, 798
	 */
@Test
	public final void test7xxMeetingSearchAllSubfields()
			throws ParserConfigurationException, IOException, SAXException, SolrServerException 
	{
		String fldName = "author_7xx_search";
		createFreshIx("authorTests.mrc");
	
		assertSingleResult("7xxMeetingSearch", fldName, "711a");
		assertSingleResult("7xxMeetingSearch", fldName, "711c");
		assertSingleResult("7xxMeetingSearch", fldName, "711d");
		assertSingleResult("7xxMeetingSearch", fldName, "711e");
		assertSingleResult("7xxMeetingSearch", fldName, "711g");
		assertSingleResult("7xxMeetingSearch", fldName, "711j");
		assertSingleResult("7xxMeetingSearch", fldName, "711n");
		assertSingleResult("7xxMeetingSearch", fldName, "711q");
		assertSingleResult("7xxMeetingSearch", fldName, "711u");
		
		assertSingleResult("7xxMeetingSearch", fldName, "798a");
		assertSingleResult("7xxMeetingSearch", fldName, "798c");
		assertSingleResult("7xxMeetingSearch", fldName, "798d");
		assertSingleResult("7xxMeetingSearch", fldName, "798e");
		assertSingleResult("7xxMeetingSearch", fldName, "798g");
		assertSingleResult("7xxMeetingSearch", fldName, "798j");
		assertSingleResult("7xxMeetingSearch", fldName, "798n");
		assertSingleResult("7xxMeetingSearch", fldName, "798q");
		assertSingleResult("7xxMeetingSearch", fldName, "798u");

		assertZeroResults(fldName, "711i");
		assertZeroResults(fldName, "798i");
		assertZeroResults(fldName, "none");
	}

	/**
	 * vern_author_7xx_search: meeting name fields 
	 *  check all search subfields for 711, 798
	 */
@Test
	public final void vern7xxMeetingSearchAllSubfields()
			throws ParserConfigurationException, IOException, SAXException, SolrServerException 
	{
		String fldName = "vern_author_7xx_search";
		createFreshIx("vernacularSearchTests.mrc");
	
		assertSingleResult("7xxVernMeetingSearch", fldName, "vern711a");
		assertSingleResult("7xxVernMeetingSearch", fldName, "vern711c");
		assertSingleResult("7xxVernMeetingSearch", fldName, "vern711d");
		assertSingleResult("7xxVernMeetingSearch", fldName, "vern711e");
		assertSingleResult("7xxVernMeetingSearch", fldName, "vern711j");
		assertSingleResult("7xxVernMeetingSearch", fldName, "vern711q");
		assertSingleResult("7xxVernMeetingSearch", fldName, "vern711u");
		
		Set<String> docIds = new HashSet<String>();
		docIds.add("7xxLowVernSearch");
		docIds.add("7xxVernMeetingSearch");
		// overlap title
		assertSearchResults(fldName, "vern711g", docIds); 
		assertSearchResults(fldName, "vern711n", docIds);
	
		assertSingleResult("7xxVernMeetingSearch", fldName, "vern798a");
		assertSingleResult("7xxVernMeetingSearch", fldName, "vern798c");
		assertSingleResult("7xxVernMeetingSearch", fldName, "vern798d");
		assertSingleResult("7xxVernMeetingSearch", fldName, "vern798j");
		assertSingleResult("7xxVernMeetingSearch", fldName, "vern798q");
		assertSingleResult("7xxVernMeetingSearch", fldName, "vern798u");
	
		docIds.remove("7xxLowVernSearch");
		docIds.add("79xVernSearch");
		// overlap title
		assertSearchResults(fldName, "vern798g", docIds); 
		assertSearchResults(fldName, "vern798n", docIds);
		assertSearchResults(fldName, "vern798e", docIds);
		
		assertZeroResults(fldName, "711i"); 
		assertZeroResults(fldName, "vern798i"); 
		assertZeroResults(fldName, "none");
	}

	/**
	 * author_8xx_search:  check all search subfields for 800, 810, 811
	 */
@Test
	public final void test8xxSearchAllSubfields()
			throws ParserConfigurationException, IOException, SAXException, SolrServerException 
	{
		String fldName = "author_8xx_search";
		createFreshIx("authorTests.mrc");
		assertSingleResult("800search", fldName, "800a");
		assertSingleResult("800search", fldName, "800b");
		assertSingleResult("800search", fldName, "800c");
		assertSingleResult("800search", fldName, "800d");
		assertSingleResult("800search", fldName, "800e");
		assertSingleResult("800search", fldName, "800g");
		assertSingleResult("800search", fldName, "800j");
		assertSingleResult("800search", fldName, "800q");
		assertSingleResult("800search", fldName, "800u");
	
		assertSingleResult("810search", fldName, "810a");
		assertSingleResult("810search", fldName, "810b");
		assertSingleResult("810search", fldName, "810c");
		assertSingleResult("810search", fldName, "810d");
		assertSingleResult("810search", fldName, "810e");
		assertSingleResult("810search", fldName, "810g");
		assertSingleResult("810search", fldName, "810n");
		assertSingleResult("810search", fldName, "810u");
	
		assertSingleResult("811search", fldName, "811a");
		assertSingleResult("811search", fldName, "811c");
		assertSingleResult("811search", fldName, "811d");
		assertSingleResult("811search", fldName, "811e");
		assertSingleResult("811search", fldName, "811g");
		assertSingleResult("811search", fldName, "811j");
		assertSingleResult("811search", fldName, "811n");
		assertSingleResult("811search", fldName, "811q");
		assertSingleResult("811search", fldName, "811u");
		
		assertZeroResults(fldName, "810f");
		assertZeroResults(fldName, "810k");
		assertZeroResults(fldName, "none");
	}

	/**
	 * vern_author_8xx_search:  check all search subfields for 800, 810, 811
	 */
@Test
	public final void vern8xxSearchAllSubfields()
			throws ParserConfigurationException, IOException, SAXException, SolrServerException
	{
		String fldName = "vern_author_8xx_search";
		createFreshIx("vernacularSearchTests.mrc");
		assertSingleResult("800VernSearch", fldName, "vern800a");
		assertSingleResult("800VernSearch", fldName, "vern800b");
		assertSingleResult("800VernSearch", fldName, "vern800c");
		assertSingleResult("800VernSearch", fldName, "vern800d");
		assertSingleResult("800VernSearch", fldName, "vern800e");
		assertSingleResult("800VernSearch", fldName, "vern800q");
		assertSingleResult("800VernSearch", fldName, "vern800u");
	
		Set<String> docIds = new HashSet<String>();
		docIds.add("800VernSearch");
		docIds.add("8xxVernSearch");
		// overlap title
		assertSearchResults(fldName, "vern800g", docIds); 
		// used to be in title
		assertSearchResults(fldName, "vern800j", docIds);
	
		assertSingleResult("810VernSearch", fldName, "vern810a");
		assertSingleResult("810VernSearch", fldName, "vern810b");
		assertSingleResult("810VernSearch", fldName, "vern810c");
		assertSingleResult("810VernSearch", fldName, "vern810e");
		assertSingleResult("810VernSearch", fldName, "vern810u");
	
		docIds.remove("800VernSearch");
		docIds.add("810VernSearch");
		// overlap title
		assertSearchResults(fldName, "vern810d", docIds); 
		assertSearchResults(fldName, "vern810g", docIds); 
		assertSearchResults(fldName, "vern810n", docIds); 
		
		assertSingleResult("811VernSearch", fldName, "vern811a");
		assertSingleResult("811VernSearch", fldName, "vern811c");
		assertSingleResult("811VernSearch", fldName, "vern811d");
		assertSingleResult("811VernSearch", fldName, "vern811e");
		assertSingleResult("811VernSearch", fldName, "vern811j");
		assertSingleResult("811VernSearch", fldName, "vern811q");
		assertSingleResult("811VernSearch", fldName, "vern811u");
	
		docIds.remove("810VernSearch");
		docIds.add("811VernSearch");
		// overlap title
		assertSearchResults(fldName, "vern811g", docIds); 
		assertSearchResults(fldName, "vern811n", docIds); 
		
		
		assertZeroResults(fldName, "vern810f");
		assertZeroResults(fldName, "vern810k");
		assertZeroResults(fldName, "none");
	}

	String sortLastPrefixStr = String.valueOf(Character.toChars(Character.MAX_CODE_POINT)) + " ";


	/**
	 * Author sort field must use the correct fields in the correct order
	 */
@Test
	public final void testAuthorSortIncludedFields() 
	{
		String fldName = "author_sort";
		String testFilePath = testDataParentPath + File.separator + "authorTests.mrc";
	
		// 100 (then 240) then 245
		solrFldMapTest.assertSolrFldValue(testFilePath, "345228", fldName, "Bashkov Vladimir 100a only"); 
		solrFldMapTest.assertSolrFldHasNoValue(testFilePath, "345228", fldName, "Bashkov Vladimir");  // needs 245
		solrFldMapTest.assertSolrFldHasNoValue(testFilePath, "345228", fldName, "100a only");  // needs 100
		
		// 110 (then 240) then 245
		solrFldMapTest.assertSolrFldValue(testFilePath, "110710corpname", fldName, "Thelma facets from 110 and 710"); 
		solrFldMapTest.assertSolrFldHasNoValue(testFilePath, "110710corpname", fldName, "Thelma");  // needs 245
		solrFldMapTest.assertSolrFldHasNoValue(testFilePath, "110710corpname", fldName, "Thelma.");  // needs 245
		solrFldMapTest.assertSolrFldHasNoValue(testFilePath, "110710corpname", fldName, "facets from 110 and 710");  // needs 110
		
		// 111 (then 240) then 245
		solrFldMapTest.assertSolrFldValue(testFilePath, "111faim", fldName, "FAIM Forum mtg name facet from 111 should be FAIM Forum");
		solrFldMapTest.assertSolrFldHasNoValue(testFilePath, "111faim", fldName, "FAIM Forum"); // needs 245
		solrFldMapTest.assertSolrFldHasNoValue(testFilePath, "111faim", fldName, "FAIM (Forum)"); // needs 245
		solrFldMapTest.assertSolrFldHasNoValue(testFilePath, "111faim", fldName, "FAIM (Forum)."); // needs 245
		solrFldMapTest.assertSolrFldHasNoValue(testFilePath, "111faim", fldName, "mtg name facet from 111 should be: FAIM Forum"); // needs 111
		solrFldMapTest.assertSolrFldHasNoValue(testFilePath, "111faim", fldName, "mtg name facet from 111 should be: FAIM (Forum)"); // needs 111
		solrFldMapTest.assertSolrFldHasNoValue(testFilePath, "111faim", fldName, "FAIM (Forum). mtg name facet from 111 should be: FAIM (Forum)"); // needs punctuation stripped
		
		// no 100 but 240 (then 245)
		String s240 = "De incertitudine et vanitate scientiarum German ";
		solrFldMapTest.assertSolrFldHasNoValue(testFilePath, "666", fldName, s240);  // needs 245
		solrFldMapTest.assertSolrFldHasNoValue(testFilePath, "666", fldName, sortLastPrefixStr + s240);  // needs 245
		solrFldMapTest.assertSolrFldValue(testFilePath, "666", fldName, sortLastPrefixStr + s240 + "ZZZZ");
		solrFldMapTest.assertSolrFldHasNoValue(testFilePath, "666", fldName, "ZZZZ"); // needs 240
		solrFldMapTest.assertSolrFldHasNoValue(testFilePath, "666", fldName, sortLastPrefixStr + "ZZZZ"); // needs 240

		// 100 and 240
		solrFldMapTest.assertSolrFldValue(testFilePath, "100240", fldName, "Hoos Foos Marvin OGravel Balloon Face 100 and 240"); 
		solrFldMapTest.assertSolrFldHasNoValue(testFilePath, "100240", fldName, "Hoos Foos 100 and 240"); 
		solrFldMapTest.assertSolrFldHasNoValue(testFilePath, "100240", fldName, "Marvin OGravel Balloon Face 100 and 240"); 

		// no 100 no 240 (then 245)
		solrFldMapTest.assertSolrFldValue(testFilePath, "245only", fldName, "" + sortLastPrefixStr + "245 no 100 or 240"); 
	}


	/**
	 * Author sort field must ignore non-filing characters in 240 and 245 fields
	 */
@Test
	public final void testAuthorSortNonFilingChars() 
	{
		String fldName = "author_sort";
		String testFilePath = testDataParentPath + File.separator + "authorTests.mrc";
		
		// NOTE: 100 does not allow non-filing chars
		
		// no 100 but 240 w non-filing
		solrFldMapTest.assertSolrFldValue(testFilePath, "2400", fldName, "" + sortLastPrefixStr + "Wacky 240 0 nonfiling"); 
		
		solrFldMapTest.assertSolrFldValue(testFilePath, "2402", fldName, "" + sortLastPrefixStr + "Wacky 240 2 nonfiling"); 
		solrFldMapTest.assertSolrFldHasNoValue(testFilePath, "2402", fldName, "A Wacky 240 2 nonfiling"); 
		
		solrFldMapTest.assertSolrFldValue(testFilePath, "2407", fldName, "" + sortLastPrefixStr + "Tacky 240 7 nonfiling"); 
		solrFldMapTest.assertSolrFldHasNoValue(testFilePath, "2407", fldName, "A Wacky Tacky 240 7 nonfiling"); 
		
		// no 100 but 240 (no non-filing), 245 with non-filing
		String s240 = sortLastPrefixStr + "De incertitudine et vanitate scientiarum German ";
		solrFldMapTest.assertSolrFldValue(testFilePath, "575946", fldName, "" + s240 + "Ruckzug der biblischen Prophetie von der neueren Geschichte");
		solrFldMapTest.assertSolrFldHasNoValue(testFilePath, "575946", fldName, "" + s240 + "Der Ruckzug der biblischen Prophetie von der neueren Geschichte");	
		solrFldMapTest.assertSolrFldHasNoValue(testFilePath, "575946", fldName, "Ruckzug der biblischen Prophetie von der neueren Geschichte"); // needs 240
		
		// no 100 no 240, 245 with non-filing
		solrFldMapTest.assertSolrFldValue(testFilePath, "1261174", fldName, "" + sortLastPrefixStr + "second part of the Confutation of the Ballancing letter");
		solrFldMapTest.assertSolrFldHasNoValue(testFilePath, "1261174", fldName, "The second part of the Confutation of the Ballancing letter");

		// no 100, but 240, 245 both with non-filing
		solrFldMapTest.assertSolrFldValue(testFilePath, "892452", fldName, "" + sortLastPrefixStr + "Wacky 240 245 nonfiling"); 
		solrFldMapTest.assertSolrFldHasNoValue(testFilePath, "892452", fldName, "A Wacky In 240 245 nonfiling"); 
		solrFldMapTest.assertSolrFldHasNoValue(testFilePath, "892452", fldName, "Wacky In 240 245 nonfiling"); 
		solrFldMapTest.assertSolrFldHasNoValue(testFilePath, "892452", fldName, "A Wacky 240 245 nonfiling"); 
	}


	/**
	 * Author sort field must properly cope with numeric subfields
	 */
@Test
	public final void testAuthorSortNumericSubflds() 
	{
		String fldName = "author_sort";
		String testFilePath = testDataParentPath + File.separator + "authorTests.mrc";
//	    assertSortFldProps(fldName);
	
		// 100 
		solrFldMapTest.assertSolrFldValue(testFilePath, "1006", fldName, "Sox on Fox 100 has sub 6");
		solrFldMapTest.assertSolrFldHasNoValue(testFilePath, "1006", fldName, "880\\-01 Sox on Fox 100 has sub 6");
		
		// 240
		solrFldMapTest.assertSolrFldValue(testFilePath, "0240", fldName, "" + sortLastPrefixStr + "sleep little fishies 240 has sub 0");
		solrFldMapTest.assertSolrFldHasNoValue(testFilePath, "0240", fldName, "(DE-101c)310008891 sleep little fishies 240 has sub 0");
		
		// 240 mult numeric subfields
		solrFldMapTest.assertSolrFldValue(testFilePath, "24025", fldName, "" + sortLastPrefixStr + "la di dah 240 has sub 2 and 5");
		solrFldMapTest.assertSolrFldHasNoValue(testFilePath, "24025", fldName, "ignore me la di dah NjP 240 has sub 2 and 5");
		solrFldMapTest.assertSolrFldHasNoValue(testFilePath, "24025", fldName, "la di dah NjP 240 has sub 2 and 5");
		solrFldMapTest.assertSolrFldHasNoValue(testFilePath, "24025", fldName, "ignore me la di dah 240 has sub 2 and 5");

		// 245
		solrFldMapTest.assertSolrFldValue(testFilePath, "2458", fldName, "" + sortLastPrefixStr + "245 has sub 8");
		solrFldMapTest.assertSolrFldHasNoValue(testFilePath, "2458", fldName, "1.5\\a 245 has sub 8");	
	}
	
	/**
	 * Author sort field must ignore punctuation
	 */
@Test
	public final void testAuthorSortPunct() 
	{
		String fldName = "author_sort";
		String testFilePath = testDataParentPath + File.separator + "authorTests.mrc";
	
		solrFldMapTest.assertSolrFldValue(testFilePath, "111", fldName, "ind 0 leading quotes in 100");
		solrFldMapTest.assertSolrFldHasNoValue(testFilePath, "111", fldName, "ind 0 leading quotes\" in 100");
		solrFldMapTest.assertSolrFldHasNoValue(testFilePath, "111", fldName, "ind 0 leading quotes\\\" in 100");
		solrFldMapTest.assertSolrFldValue(testFilePath, "333", fldName, "" + sortLastPrefixStr + "ind 0 leading hyphens in 240");
		solrFldMapTest.assertSolrFldHasNoValue(testFilePath, "333", fldName, "--ind 0 leading hyphens in 240");
		solrFldMapTest.assertSolrFldValue(testFilePath, "444", fldName, "" + sortLastPrefixStr + "ind 0 leading elipsis in 240");
		solrFldMapTest.assertSolrFldHasNoValue(testFilePath, "444", fldName, "...ind 0 leading elipsis in 240");
		solrFldMapTest.assertSolrFldValue(testFilePath, "555", fldName, "ind 0 leading quote elipsis in 100");
		solrFldMapTest.assertSolrFldHasNoValue(testFilePath, "555", fldName, "\\\"...ind 0 leading quote elipsis in 100");
		solrFldMapTest.assertSolrFldValue(testFilePath, "777", fldName, "" + sortLastPrefixStr + "ind 4 leading quote elipsis in 240");
		solrFldMapTest.assertSolrFldHasNoValue(testFilePath, "777", fldName, "\\\"...ind 4 leading quote elipsis in 240");
		solrFldMapTest.assertSolrFldValue(testFilePath, "888", fldName, "interspersed punctuation here");
		solrFldMapTest.assertSolrFldHasNoValue(testFilePath, "888", fldName, "interspersed *(punctua@#$@#$tion \"here--");
		solrFldMapTest.assertSolrFldHasNoValue(testFilePath, "888", fldName, "Boo! interspersed *(punctua@#$@#$tion \"here--");
		solrFldMapTest.assertSolrFldValue(testFilePath, "999", fldName, "everything in 100");
		// lucene special chars:  + - && || ! ( ) { } [ ] ^ " ~ * ? : \
		solrFldMapTest.assertSolrFldHasNoValue(testFilePath, "999", fldName, "every!\\\"#$%\\&'\\(\\)\\*\\+,\\-./\\:;<=>\\?@\\[\\\\\\]\\^_`\\{|\\}\\~thing in 100");
	}


	/**
	 * Author sort order must be correct
	 */
@Test
	public final void testAuthorSortOrder() 
		throws ParserConfigurationException, IOException, SAXException, ClassNotFoundException, IllegalAccessException, InvocationTargetException, InstantiationException, NoSuchMethodException, SolrServerException
	{
		createFreshIx("authorTests.mrc");

		// list of doc ids in correct author sort order
		List<String> expectedOrderList = new ArrayList<String>(30);
		expectedOrderList.add("345228");  // Bashkov Vladimir 100a only
		expectedOrderList.add("999");  // everything in 100
		expectedOrderList.add("111faim");  // FAIM Forum mtg name facet from 111 should be FAIM Forum
		expectedOrderList.add("100240");  // Hoos Foos Marvin OGravel Balloon Face 100 and 240
		expectedOrderList.add("555");  // ind 0 leading quote elipsis in 100
		expectedOrderList.add("111");  // ind 0 leading quotes in 100
		expectedOrderList.add("888");  // interspersed punctuation here
		expectedOrderList.add("1006");  // Sox on Fox 100 has sub 6
		expectedOrderList.add("110710corpname");  // Thelma facets from 110 and 710
// 100 / 110 / 111 is missing;  sort last, THEN sort by title		
		expectedOrderList.add("2458");  // 245 has sub 8
		expectedOrderList.add("245only");  // 245 no 100 or 240
		expectedOrderList.add("575946");  // De incertitudine et vanitate scientiarum German Ruckzug der biblischen Prophetie von der neueren Geschichte
		expectedOrderList.add("666");  // De incertitudine et vanitate scientiarum German ZZZZ
		expectedOrderList.add("444");  // ind 0 leading elipsis in 240
		expectedOrderList.add("333");  // ind 0 leading hyphens in 240
		expectedOrderList.add("777");  // ind 4 leading quote elipsis in 240
		expectedOrderList.add("24025");  // la di dah 240 has sub 2 and 5
		expectedOrderList.add("1261174");  // second part of the Confutation of the Ballancing letter
		expectedOrderList.add("0240");  // sleep little fishies 240 has sub 0
		expectedOrderList.add("2407");  // Tacky 240 7 nonfiling
		expectedOrderList.add("2400");  // Wacky 240 0 nonfiling
		expectedOrderList.add("2402");  // Wacky 240 2 nonfiling
		expectedOrderList.add("892452");  // Wacky 240 245 nonfiling

		
		// get search results sorted by author_sort field
		SolrDocumentList resultDocs = getAscSortDocs("collection", "sirsi", "author_sort");

		// we know we have documents that are not in the expected order list, 
		//  so we must allow for gaps
		// author_sort isn't stored, so we must look at id field
		int expDocIx = -1;
		for (SolrDocument doc : resultDocs)
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
		
		if (expDocIx != expectedOrderList.size() - 1) {
			String lastCorrDocId = expectedOrderList.get(expDocIx);
			fail("Author Sort Order is incorrect.  Last correct document was " + lastCorrDocId);
		}
	}

}
