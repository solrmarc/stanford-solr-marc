package edu.stanford;

import java.io.IOException;
import java.util.*;
import javax.xml.parsers.ParserConfigurationException;

import org.junit.*;
import org.xml.sax.SAXException;

/**
 * junit4 tests for Stanford University series fields for blacklight index
 * @author Naomi Dushay
 */
public class SeriesTests extends AbstractStanfordBlacklightTest {

	private String fileName = "seriesTests.mrc";

@Before
	public final void setup() 
			throws ParserConfigurationException, IOException, SAXException 
	{
		createIxInitVars(fileName);
	}


// FIXME:  vern flavor fields too?

	/**
	 * Series title (only) search field
	 */
@Test
	public void testSeriesTitleOnly() 
			throws ParserConfigurationException, IOException, SAXException 
	{
		String fldName = "series_title_search";
		assertSearchFldMultValProps(fldName);
		assert440search(fldName);
		assert830search(fldName);
		assertZeroResults(fldName, "800a");
		assertZeroResults(fldName, "810a");
		assertZeroResults(fldName, "811a");
		
		assert440and830(fldName);
	}

	/**
	 * Series personal name (+title) search field
	 */
@Test
	public void testSeriesPersonalNameSearch()
		throws ParserConfigurationException, IOException, SAXException 
	{
		String fldName = "series_person_search";
		assertSearchFldMultValProps(fldName);
		assert440search(fldName);
		assert800search(fldName);
		assertZeroResults(fldName, "810a");
		assertZeroResults(fldName, "811a");
		assertZeroResults(fldName, "830a");
		
		assert440and800(fldName);
	}

	/**
	 * Series organization name (+ title) search field
	 */
@Test
	public void testSeriesOrgName()
		throws ParserConfigurationException, IOException, SAXException 
	{
		String fldName = "series_org_search";
		assertSearchFldMultValProps(fldName);
		assert440search(fldName);
		assert810search(fldName);
		assertZeroResults(fldName, "800a");
		assertZeroResults(fldName, "811a");
		assertZeroResults(fldName, "830a");

		assert440and810(fldName);
		// phrase in 490a only so not included
		assertZeroResults(fldName, "\"Publications of the European Court of Human Rights. Series A, Judgments and decisions\"");
	}

	/**
	 * Series proceedings name (+title) search field
	 */
@Test
	public void testSeriesProcSearch()
		throws ParserConfigurationException, IOException, SAXException 
	{
		String fldName = "series_proc_search";
		assertSearchFldMultValProps(fldName);
		assert440search(fldName);
		assert811search(fldName);
		assertZeroResults(fldName, "800a");
		assertZeroResults(fldName, "810a");
		assertZeroResults(fldName, "830a");
		
		assert440and811(fldName);
	}


	/**
	 * Series anything (w/o 490) search field
	 */
@Test
	public void testSeriesAnything()
		throws ParserConfigurationException, IOException, SAXException 
	{
		String fldName = "series_anything_search";
		assertSearchFldMultValProps(fldName);
		assert440search(fldName);
		assert800search(fldName);
		assert810search(fldName);
		assert811search(fldName);
		assert830search(fldName);
		
		assertZeroResults(fldName, "490a");
		assertZeroResults(fldName, "490v");
		// phrase in 490a only so not included
		assertZeroResults(fldName, "\"Publications of the European Court of Human Rights. Series A, Judgments and decisions\"");
		
		assert440and800(fldName);
		assert440and810(fldName);
		assert440and811(fldName);
		assert440and830(fldName);
	}

	/**
	 * Series anything (including 490) search field
	 */
@Test
	public void testSeriesAnything490()
		throws ParserConfigurationException, IOException, SAXException 
	{
		String fldName = "series_anything490_search";
		assertSearchFldMultValProps(fldName);
		assert440search(fldName);
		assert800search(fldName);
		assert810search(fldName);
		assert811search(fldName);
		assert830search(fldName);
	
		assertSingleResult("490", fldName, "490a");
		assertZeroResults(fldName, "490v");
		// phrase in 490a only
		Set<String> docIds = new HashSet<String>(2);
		docIds.add("1943665");
		docIds.add("1943753");
		assertSearchResults(fldName, "\"Publications of the European Court of Human Rights. Series A, Judgments and decisions\"", docIds);
		
		assert440and800(fldName);
		assert440and810(fldName);
		assert440and811(fldName);
		assert440and830(fldName);
	}


// --------------- private methods -----------------

	/**
	 * ensure 440anpv are searched for the field, and other 440 subfields are
	 *  not searched
	 * @param fldName name of field to search
	 */
	private void assert440search(String fldName)
			throws ParserConfigurationException, IOException, SAXException 
	{
		assertSingleResult("440", fldName, "440a");
		assertSingleResult("440", fldName, "440n");
		assertSingleResult("440", fldName, "440p");
		assertSingleResult("440", fldName, "440v");		
		assertZeroResults(fldName, "440w");
		assertZeroResults(fldName, "440x");
		assertZeroResults(fldName, "nope");
	}
	
	/**
	 * ensure 800a-x are searched for the field, and other 800 subfields are
	 *  not searched
	 * @param fldName name of field to search
	 */
	private void assert800search(String fldName)
			throws ParserConfigurationException, IOException, SAXException 
	{
		assertSingleResult("800", fldName, "800a");
		assertSingleResult("800", fldName, "800d");
		assertSingleResult("800", fldName, "800f");
		assertSingleResult("800", fldName, "800g");
		assertSingleResult("800", fldName, "800h");
		assertSingleResult("800", fldName, "800j");
		assertSingleResult("800", fldName, "800k");
		assertSingleResult("800", fldName, "800l");
		assertSingleResult("800", fldName, "800m");
		assertSingleResult("800", fldName, "800n");
		assertSingleResult("800", fldName, "800o");
		assertSingleResult("800", fldName, "800p");
		assertSingleResult("800", fldName, "800r");
		assertSingleResult("800", fldName, "800s");
		assertSingleResult("800", fldName, "800t");
		assertSingleResult("800", fldName, "800v");
		assertSingleResult("800", fldName, "800x");
	}
	
	/**
	 * ensure 810a-x are searched for the field, and other 810 subfields are
	 *  not searched
	 * @param fldName name of field to search
	 */
	private void assert810search(String fldName)
			throws ParserConfigurationException, IOException, SAXException 
	{
		assertSingleResult("810", fldName, "810a");
		assertSingleResult("810", fldName, "810d");
		assertSingleResult("810", fldName, "810f");
		assertSingleResult("810", fldName, "810g");
		assertSingleResult("810", fldName, "810h");
		assertSingleResult("810", fldName, "810j");
		assertSingleResult("810", fldName, "810k");
		assertSingleResult("810", fldName, "810l");
		assertSingleResult("810", fldName, "810m");
		assertSingleResult("810", fldName, "810n");
		assertSingleResult("810", fldName, "810o");
		assertSingleResult("810", fldName, "810p");
		assertSingleResult("810", fldName, "810r");
		assertSingleResult("810", fldName, "810s");
		assertSingleResult("810", fldName, "810t");
		assertSingleResult("810", fldName, "810v");
		assertSingleResult("810", fldName, "810x");
	}
	
	/**
	 * ensure 830a-x are searched for the field, and other 830 subfields are
	 *  not searched
	 * @param fldName name of field to search
	 */
	private void assert811search(String fldName)
			throws ParserConfigurationException, IOException, SAXException 
	{
		assertSingleResult("811", fldName, "811a");
		assertSingleResult("811", fldName, "811d");
		assertSingleResult("811", fldName, "811f");
		assertSingleResult("811", fldName, "811g");
		assertSingleResult("811", fldName, "811h");
		assertSingleResult("811", fldName, "811j");
		assertSingleResult("811", fldName, "811k");
		assertSingleResult("811", fldName, "811l");
		assertSingleResult("811", fldName, "811m");
		assertSingleResult("811", fldName, "811n");
		assertSingleResult("811", fldName, "811o");
		assertSingleResult("811", fldName, "811p");
		assertSingleResult("811", fldName, "811r");
		assertSingleResult("811", fldName, "811s");
		assertSingleResult("811", fldName, "811t");
		assertSingleResult("811", fldName, "811v");
		assertSingleResult("811", fldName, "811x");
	}

	/**
	 * ensure 830a-x are searched for the field, and other 830 subfields are
	 *  not searched
	 * @param fldName name of field to search
	 */
	private void assert830search(String fldName) 
			throws ParserConfigurationException, IOException, SAXException 
	{
		assertSingleResult("830", fldName, "830a");
		assertSingleResult("830", fldName, "830d");
		assertSingleResult("830", fldName, "830f");
		assertSingleResult("830", fldName, "830g");
		assertSingleResult("830", fldName, "830h");
		assertSingleResult("830", fldName, "830j");
		assertSingleResult("830", fldName, "830k");
		assertSingleResult("830", fldName, "830l");
		assertSingleResult("830", fldName, "830m");
		assertSingleResult("830", fldName, "830n");
		assertSingleResult("830", fldName, "830o");
		assertSingleResult("830", fldName, "830p");
		assertSingleResult("830", fldName, "830r");
		assertSingleResult("830", fldName, "830s");
		assertSingleResult("830", fldName, "830t");
		assertSingleResult("830", fldName, "830v");
		assertSingleResult("830", fldName, "830x");
	}
	
	/**
	 * assert proper subfields in 440 and 800 are searchable in given field
	 * @param fldName name of field to search
	 */
	private void assert440and800(String fldName)
			throws ParserConfigurationException, IOException, SAXException
	{
		// "Joyce, James, 1882-1941. James Joyce archive."  490, 800
		Set<String> docIds = new HashSet<String>();
		docIds.clear();
		docIds.add("797438");  // 490a (part of phrase), 800adt
		docIds.add("798059");   // 440a (full phrase), 800adt
		assertSearchResults(fldName, "\"Joyce, James, 1882-1941. James Joyce archive\"", docIds);
		// phrase in title, no series fields
		assertDocHasNoField("222", fldName);
	}

	/**
	 * assert proper subfields in 440 and 810 are searchable in given field
	 * @param fldName name of field to search
	 */
	private void assert440and810(String fldName)
			throws ParserConfigurationException, IOException, SAXException
	{
		// "European Court of Human Rights. Publications de la Cour européenne des droits de l'homme. Série A, Arrêts et décisions ;"  490, 810
		Set<String> docIds = new HashSet<String>();
		docIds.clear();
		docIds.add("1943665");  // 490aav, 810atnpv
		docIds.add("1943753");   // 490aav, 810atnpv
		assertSearchResults(fldName, "\"European Court of Human Rights.\"", docIds);  // 810a only
		assertSearchResults(fldName, "\"European Court of Human Rights. Publications de la Cour europeenne des droits de l'homme. Serie A, Arrets et decisions\"", docIds);
		// phrase in title, no series fields
		assertDocHasNoField("111", fldName);
		// sub v included
		assertSingleResult("1943665", fldName, "138"); 
		assertSingleResult("1943753", fldName, "132"); 
	}

	/**
	 * assert proper subfields in 440 and 811 are searchable in given field
	 * @param fldName name of field to search
	 */
	private void assert440and811(String fldName)
			throws ParserConfigurationException, IOException, SAXException
	{
		// "Delaware Symposium on Language Studies"  490, 811
		Set<String> docIds = new HashSet<String>();
		docIds.clear();
		docIds.add("1588366");  // 490av, 811atv
		docIds.add("253693");   // 490av, 811atv
		assertSearchResults(fldName, "\"Delaware Symposium on Language Studies\"", docIds);
		// phrase in title, no series fields
		assertDocHasNoField("999", fldName);
		// sub v included
		assertSingleResult("1588366", fldName, "4"); 
		assertSingleResult("253693", fldName, "7"); 
	}

	/**
	 * assert proper subfields in 440 and 830 are searchable in given field
	 * @param fldName name of field to search
	 */
	private void assert440and830(String fldName)
			throws ParserConfigurationException, IOException, SAXException
	{
		// "Lecture notes in computer science"  440, 490, 830
		Set<String> docIds = new HashSet<String>();
		docIds.add("1964873");  // 490av, 830av
		docIds.add("4489006");  // 490av, 830av
		docIds.add("408434");   // 440av
		docIds.add("488433");	// 830a
		assertSearchResults(fldName, "\"Lecture notes in computer science\"", docIds);
		// phrase in title, no series fields
		assertDocHasNoField("444", fldName);
		// sub v included
		assertSingleResult("1964873", fldName, "240"); 
		assertSingleResult("4489006", fldName, "1658"); 
		assertSingleResult("408434", fldName, "28"); 

		// "Beitrage zur Afrikakunde"  440, 490, 830
		docIds.clear();
		docIds.add("1025630");  // 490av, 830av
		docIds.add("1554950");   // 440axv
		assertSearchResults(fldName, "\"Beitrage zur Afrikakunde\"", docIds);
		// subfield x only in 440 or 490 so not included
		assertZeroResults(fldName, "0171-1660");
		assertZeroResults(fldName, "0171");
		assertZeroResults(fldName, "1660");
		// phrase in title, no series fields
		assertDocHasNoField("666", fldName);
		// sub v included
		assertSingleResult("1025630", fldName, "3"); 
		assertSingleResult("1554950", fldName, "6");  
		
		// "Macmillan series in applied computer science"  490, 830
		assertSingleResult("1173521", fldName, "\"Macmillan series in applied computer science\"");
		// phrase in title, no series fields
		assertDocHasNoField("333", fldName);	
	}
	
}
