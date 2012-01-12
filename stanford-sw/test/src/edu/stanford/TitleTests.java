package edu.stanford;

import java.io.*;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.solr.client.solrj.SolrServerException;
import org.junit.*;
import org.xml.sax.SAXException;

/**
 * junit4 tests for Stanford University's title fields
 * @author Naomi Dushay
 */
public class TitleTests extends AbstractStanfordTest 
{
	private String testFileName = "titleTests.mrc";
    private String testFilePath = testDataParentPath + File.separator + testFileName;
    private String dispFileName = "displayFieldsTests.mrc";
    private String dispTestFilePath = testDataParentPath + File.separator + dispFileName;

@Before
	public final void setup() 
	{
		mappingTestInit();
	}	


    /**
	 * Test title_245a_display field;  trailing punctuation is removed
	 */
@Test
	public final void testTitle245aDisplay() 
	{
		String fldName = "title_245a_display";

	    solrFldMapTest.assertSolrFldValue(testFilePath, "245NoNorP", fldName, "245 no subfield n or p");
	    solrFldMapTest.assertSolrFldValue(testFilePath, "245nAndp", fldName, "245 n and p");
	    solrFldMapTest.assertSolrFldValue(testFilePath, "245multpn", fldName, "245 multiple p, n");

		// trailing punctuation removed
	    solrFldMapTest.assertSolrFldValue(dispTestFilePath, "2451", fldName, "Heritage Books archives");
	    solrFldMapTest.assertSolrFldHasNoValue(dispTestFilePath, "2451", fldName, "Heritage Books archives.");
	    solrFldMapTest.assertSolrFldValue(dispTestFilePath, "2452", fldName, "Ton meionoteton eunoia");
	    solrFldMapTest.assertSolrFldHasNoValue(dispTestFilePath, "2452", fldName, "Ton meionoteton eunoia :");
	}

	/**
	 * Test vern_title_245a_display field;  trailing punctuation is removed
	 */
@Test
	public final void testVernTitle245aDisplay() 
	{
		String fldName = "vern_title_245a_display";
		String filePath = testDataParentPath + File.separator + "vernacularNonSearchTests.mrc";
	
	    solrFldMapTest.assertSolrFldValue(filePath, "allVern", fldName, "vernacular title 245");
		// trailing punctuation removed
	    solrFldMapTest.assertSolrFldValue(filePath, "trailingPunct", fldName, "vernacular ends in slash");
	    solrFldMapTest.assertSolrFldHasNoValue(filePath, "trailingPunct", fldName, "vernacular ends in slash /");
	}
	
	/**
	 * Test title_245c_display field;  trailing punctuation is removed
	 */
@Test
	public final void testTitle245cDisplay() 
	{
		String fldName = "title_245c_display";
	
		// trailing punctuation removed
	    solrFldMapTest.assertSolrFldValue(testFilePath, "245NoNorP", fldName, "by John Sandford");
	    solrFldMapTest.assertSolrFldHasNoValue(testFilePath, "245NoNorP", fldName, "by John Sandford.");
	    
	    solrFldMapTest.assertSolrFldValue(dispTestFilePath, "2451", fldName, "Laverne Galeener-Moore");
	    solrFldMapTest.assertSolrFldHasNoValue(dispTestFilePath, "2451", fldName, "Laverne Galeener-Moore.");
	    solrFldMapTest.assertSolrFldValue(dispTestFilePath, "2453", fldName, "...");
	}

	/**
	 * Test title_245c_display field;  trailing punctuation is removed
	 */
@Test
	public final void testVernTitle245cDisplay() 
	{
		String fldName = "vern_title_245c_display";
		String filePath = testDataParentPath + File.separator + "vernacularNonSearchTests.mrc";

		// trailing punctuation removed
	    solrFldMapTest.assertSolrFldValue(filePath, "RtoL", fldName, "crocodile for is c");
	    solrFldMapTest.assertSolrFldHasNoValue(filePath, "RtoL", fldName, "crocodile for is c,");
	}

	/**
	 * Test title_display field
	 */
@Test
	public final void testTitleDisplay() 
	{
		String fldName = "title_display";
	    solrFldMapTest.assertSolrFldValue(testFilePath, "245NoNorP", fldName, "245 no subfield n or p [electronic resource]");
	    solrFldMapTest.assertSolrFldValue(testFilePath, "245nNotp", fldName, "245 n but no p Part one.");
	    solrFldMapTest.assertSolrFldValue(testFilePath, "245pNotn", fldName, "245 p but no n. subfield b Student handbook");
	    solrFldMapTest.assertSolrFldValue(testFilePath, "245nAndp", fldName, "245 n and p: A, The humanities and social sciences");
	    solrFldMapTest.assertSolrFldValue(testFilePath, "245multpn", fldName, "245 multiple p, n first p subfield first n subfield second p subfield second n subfield");
		
		// trailing slash removed
	    solrFldMapTest.assertSolrFldValue(dispTestFilePath, "2451", fldName, "Heritage Books archives. Underwood biographical dictionary. Volumes 1 & 2 revised [electronic resource]");
	    solrFldMapTest.assertSolrFldHasNoValue(dispTestFilePath, "2451", fldName, "Heritage Books archives. Underwood biographical dictionary. Volumes 1 & 2 revised [electronic resource] /");
	    solrFldMapTest.assertSolrFldValue(dispTestFilePath, "2452", fldName, "Ton meionoteton eunoia : mythistorema");
	    solrFldMapTest.assertSolrFldHasNoValue(dispTestFilePath, "2452", fldName, "Ton meionoteton eunoia : mythistorema /");
	    solrFldMapTest.assertSolrFldValue(dispTestFilePath, "2453", fldName, "Proceedings");
	    solrFldMapTest.assertSolrFldHasNoValue(dispTestFilePath, "2453", fldName, "Proceedings /");
	}

	/**
	 * Test that 245 display field contains non-filing characters and copes with
	 *  trailing punctuation correctly
	 */
@Test
	public final void testTitleDisplayNonFiling() 
	{
		String fldName = "title_display";

		// also check for trailing punctuation handling
	    solrFldMapTest.assertSolrFldValue(testFilePath, "115472", fldName, "India and the European Economic Community");
	    solrFldMapTest.assertSolrFldHasNoValue(testFilePath, "115472", fldName, "India and the European Economic Community.");
	    solrFldMapTest.assertSolrFldValue(testFilePath, "7117119", fldName, "HOUSING CARE AND SUPPORT PUTTING GOOD IDEAS INTO PRACTICE");
		// non-filing characters and trailing punctuation
	    solrFldMapTest.assertSolrFldValue(testFilePath, "1962398", fldName, "A guide to resources in United States libraries");
	    solrFldMapTest.assertSolrFldHasNoValue(testFilePath, "1962398", fldName, "A guide to resources in United States libraries /");
	    solrFldMapTest.assertSolrFldHasNoValue(testFilePath, "1962398", fldName, "guide to resources in United States libraries");
	    solrFldMapTest.assertSolrFldHasNoValue(testFilePath, "1962398", fldName, "guide to resources in United States libraries /");
	    solrFldMapTest.assertSolrFldValue(testFilePath, "4428936", fldName, "Il cinema della transizione");
	    solrFldMapTest.assertSolrFldHasNoValue(testFilePath, "4428936", fldName, "cinema della transizione");
	    solrFldMapTest.assertSolrFldValue(testFilePath, "1261173", fldName, "The second part of the Confutation of the Ballancing letter");
	    solrFldMapTest.assertSolrFldHasNoValue(testFilePath, "1261173", fldName, "second part of the Confutation of the Ballancing letter");
	    solrFldMapTest.assertSolrFldValue(testFilePath, "575946", fldName, "Der Ruckzug der biblischen Prophetie von der neueren Geschichte");
	    solrFldMapTest.assertSolrFldHasNoValue(testFilePath, "575946", fldName, "Der Ruckzug der biblischen Prophetie von der neueren Geschichte.");
	    solrFldMapTest.assertSolrFldHasNoValue(testFilePath, "575946", fldName, "Ruckzug der biblischen Prophetie von der neueren Geschichte");
	    solrFldMapTest.assertSolrFldHasNoValue(testFilePath, "575946", fldName, "Ruckzug der biblischen Prophetie von der neueren Geschichte.");
	    solrFldMapTest.assertSolrFldValue(testFilePath, "666", fldName, "ZZZZ");
	
		// 245 only even though 130 or 240 present.
	    solrFldMapTest.assertSolrFldValue(testFilePath, "2400", fldName, "240 0 non-filing");
	    solrFldMapTest.assertSolrFldValue(testFilePath, "2402", fldName, "240 2 non-filing");
	    solrFldMapTest.assertSolrFldValue(testFilePath, "2407", fldName, "240 7 non-filing");
	    solrFldMapTest.assertSolrFldValue(testFilePath, "130", fldName, "130 4 non-filing");
	    solrFldMapTest.assertSolrFldValue(testFilePath, "130240", fldName, "130 and 240");
		
		// numeric subfields
	    solrFldMapTest.assertSolrFldValue(testFilePath, "2458", fldName, "245 has sub 8");
	    solrFldMapTest.assertSolrFldHasNoValue(testFilePath, "2458", fldName, "1.5\\a 245 has sub 8");
	}

	/**
	 * Test title_full_display field 
	 */
@Test
	public final void testTitleFullDisplay() 
	{
		String fldName = "title_full_display";

	    solrFldMapTest.assertSolrFldValue(testFilePath, "245NoNorP", fldName, "245 no subfield n or p [electronic resource] / by John Sandford.");
	    solrFldMapTest.assertSolrFldValue(testFilePath, "245nNotp", fldName, "245 n but no p Part one.");
	    solrFldMapTest.assertSolrFldValue(testFilePath, "245pNotn", fldName, "245 p but no n. subfield b Student handbook.");
	    solrFldMapTest.assertSolrFldValue(testFilePath, "245nAndp", fldName, "245 n and p: A, The humanities and social sciences.");
	    solrFldMapTest.assertSolrFldValue(testFilePath, "245multpn", fldName, "245 multiple p, n first p subfield first n subfield second p subfield second n subfield");
		
	    solrFldMapTest.assertSolrFldValue(dispTestFilePath, "2451", fldName, "Heritage Books archives. Underwood biographical dictionary. Volumes 1 & 2 revised [electronic resource] / Laverne Galeener-Moore.");
	    solrFldMapTest.assertSolrFldValue(dispTestFilePath, "2452", fldName, "Ton meionoteton eunoia : mythistorema / Spyrou Gkrintzou.");
	    solrFldMapTest.assertSolrFldValue(dispTestFilePath, "2453", fldName, "Proceedings / ...");
	}

	/**
	 * Test uniform title display - it uses 130 when there is one.
	 *   as of 2009-03-26  first of 130, 240 
	 *   as of 2008-12-10  only uses 130, not 240 (to mirror title_sort field)
	 *  Non-filing characters are included.
	 */
@Test
	public final void testUniformTitleDisplay() 
	{
		String fldName = "title_uniform_display";
	
		// no 240 or 130
		solrFldMapTest.assertNoSolrFld(testFilePath, "115472", fldName);
		solrFldMapTest.assertNoSolrFld(testFilePath, "7117119", fldName);
		solrFldMapTest.assertNoSolrFld(testFilePath, "1962398", fldName);
		solrFldMapTest.assertNoSolrFld(testFilePath, "4428936", fldName);
		solrFldMapTest.assertNoSolrFld(testFilePath, "1261173", fldName);
		
		// 240 only
		String s240 = "De incertitudine et vanitate scientiarum. German";
	    solrFldMapTest.assertSolrFldValue(testFilePath, "575946", fldName, s240);
	    solrFldMapTest.assertSolrFldValue(testFilePath, "666", fldName, s240);
	    solrFldMapTest.assertSolrFldValue(testFilePath, "2400", fldName, "Wacky");
	    solrFldMapTest.assertSolrFldValue(testFilePath, "2402", fldName, "A Wacky");
	    solrFldMapTest.assertSolrFldHasNoValue(testFilePath, "2402", fldName, "Wacky");
	    solrFldMapTest.assertSolrFldValue(testFilePath, "2407", fldName, "A Wacky Tacky");
	    solrFldMapTest.assertSolrFldHasNoValue(testFilePath, "2407", fldName, "Tacky");

		// uniform title 130 if exists, 240 if not.
	    solrFldMapTest.assertSolrFldValue(testFilePath, "130", fldName, "The Snimm.");
	    solrFldMapTest.assertSolrFldHasNoValue(testFilePath, "130", fldName, "Snimm.");
	    solrFldMapTest.assertSolrFldValue(testFilePath, "130240", fldName, "Hoos Foos");
	    solrFldMapTest.assertSolrFldHasNoValue(testFilePath, "130240", fldName, "Marvin O'Gravel Balloon Face");
	    solrFldMapTest.assertSolrFldHasNoValue(testFilePath, "130240", fldName, "Hoos Foos Marvin O'Gravel Balloon Face");
		
		// numeric subfields
	    solrFldMapTest.assertSolrFldValue(testFilePath, "1306", fldName, "Sox on Fox");
	    solrFldMapTest.assertSolrFldHasNoValue(testFilePath, "1306", fldName, "880-01 Sox on Fox");
	    solrFldMapTest.assertSolrFldValue(testFilePath, "0240", fldName, "sleep little fishies");
	    solrFldMapTest.assertSolrFldHasNoValue(testFilePath, "0240", fldName, "(DE-101c)310008891 sleep little fishies");
	    solrFldMapTest.assertSolrFldValue(testFilePath, "24025", fldName, "la di dah");
	    solrFldMapTest.assertSolrFldHasNoValue(testFilePath, "24025", fldName, "ignore me la di dah");
		
	    solrFldMapTest.assertSolrFldValue(dispTestFilePath, "2401", fldName, "Variations, piano, 4 hands, K. 501, G major");
	    solrFldMapTest.assertSolrFldValue(dispTestFilePath, "2402", fldName, "Treaties, etc. Poland, 1948 Mar. 2. Protocols, etc., 1951 Mar. 6");
	    solrFldMapTest.assertSolrFldValue(dispTestFilePath, "130", fldName, "Bible. O.T. Five Scrolls. Hebrew. Biblioteca apostolica vaticana. Manuscript. Urbiniti Hebraicus 1. 1980.");
	    solrFldMapTest.assertSolrFldValue(dispTestFilePath, "11332244", fldName, "Bodkin Van Horn");
	}

	/**
	 * Test multiple occurrences of same field uniform_title_display =
	 * 130abcdefghijklmnopqrstuvwxyz:240abcdefghijklmnopqrstuvwxyz, first
	 */
@Test
	public final void testUniformTitle() 
	{
		String filePath = testDataParentPath + File.separator + "vernacularNonSearchTests.mrc";
	
		String fldName = "title_uniform_display";
	    solrFldMapTest.assertSolrFldValue(filePath, "130only", fldName, "main entry uniform title");
		fldName = "vern_title_uniform_display";
	    solrFldMapTest.assertSolrFldValue(filePath, "130only", fldName, "vernacular main entry uniform title");
	
		// 240 is back in uniform title (despite title_sort being 130 245)
		fldName = "title_uniform_display";
	    solrFldMapTest.assertSolrFldValue(filePath, "240only", fldName, "uniform title");
		fldName = "vern_title_uniform_display";
	    solrFldMapTest.assertSolrFldValue(filePath, "240only", fldName, "vernacular uniform title");
	}

	/**
	 * Test that title sort field uses the correct fields in the correct order
	 */
@Test
	public final void testTitleSortIncludedFields() 
	{
		String fldName = "title_sort";
		
		// 130 (with non-filing)
	    solrFldMapTest.assertSolrFldValue(testFilePath, "130", fldName, "Snimm 130 4 nonfiling");
	    solrFldMapTest.assertSolrFldValue(testFilePath, "1306", fldName, "Sox on Fox 130 has sub 6");
	    solrFldMapTest.assertSolrFldValue(testFilePath, "888", fldName, "interspersed punctuation here");
		
		// 240
	    solrFldMapTest.assertSolrFldHasNoValue(testFilePath, "0240", fldName, "sleep little fishies 240 has sub 0");
	    solrFldMapTest.assertSolrFldValue(testFilePath, "0240", fldName, "240 has sub 0");

	    solrFldMapTest.assertSolrFldHasNoValue(testFilePath, "24025", fldName, "la di dah 240 has sub 2 and 5");
	    solrFldMapTest.assertSolrFldValue(testFilePath, "24025", fldName, "240 has sub 2 and 5");

		// 130 and 240
	    solrFldMapTest.assertSolrFldValue(testFilePath, "130240", fldName, "Hoos Foos 130 and 240");
	    solrFldMapTest.assertSolrFldHasNoValue(testFilePath, "130240", fldName, "Hoos Foos Marvin OGravel Balloon Face 130 and 240");
	    solrFldMapTest.assertSolrFldHasNoValue(testFilePath, "130240", fldName, "Marvin OGravel Balloon Face 130 and 240");
	}

	/**
	 * Test that title sort field ignores non-filing characters in 245 
	 *  and uniform title fields
	 */
@Test
	public final void testTitleSortNonFiling() 
		throws ParserConfigurationException, IOException, SAXException, SolrServerException
	{
		String fldName = "title_sort";
		createFreshIx(testFileName);
		
		// sort field is indexed (but not tokenized) - search for documents		
		assertSingleResult("115472", fldName, "\"India and the European Economic Community\"");
		assertSingleResult("115472", fldName, "\"india and the european economic community\"");
		assertSingleResult("7117119", fldName, "\"HOUSING CARE AND SUPPORT PUTTING GOOD IDEAS INTO PRACTICE\"");
		assertSingleResult("7117119", fldName, "\"housing care and support putting good ideas into practice\"");
		assertSingleResult("1962398", fldName, "\"guide to resources in United States libraries\"");
		assertZeroResults(fldName, "\"a guide to resources in United States libraries\"");
		assertSingleResult("4428936", fldName, "\"cinema della transizione\"");
		assertZeroResults(fldName, "\"Il cinema della transizione\"");
		assertSingleResult("1261173", fldName, "\"second part of the Confutation of the Ballancing letter\"");
		assertZeroResults(fldName, "\"The second part of the Confutation of the Ballancing letter\"");
		
		// 130 (with non-filing)
		assertSingleResult("130", fldName, "\"Snimm 130 4 nonfiling\""); 
		assertZeroResults(fldName, "\"The Snimm 130 4 nonfiling\""); 
		// 130 and 240
		assertSingleResult("130240", fldName, "\"Hoos Foos 130 and 240\""); 
		assertZeroResults(fldName, "\"Hoos Foos Marvin OGravel Balloon Face 130 and 240\""); 
		assertZeroResults(fldName, "\"Marvin OGravel Balloon Face 130 and 240\""); 

		// NOTE: 240 is no longer in title_sort field
		//  search for 240
		String s240 = "De incertitudine et vanitate scientiarum German ";
		assertZeroResults(fldName, s240);  // needs 245
		// search for 240 and 245 
		assertSingleResult("666", fldName, "ZZZZ");
		assertZeroResults(fldName, "\"" + s240 + "ZZZZ\""); 
		
		// non filing chars in 245
		assertSingleResult("575946", fldName, "\"Ruckzug der biblischen Prophetie von der neueren Geschichte\"");
		assertZeroResults(fldName, "\"Der Ruckzug der biblischen Prophetie von der neueren Geschichte\"");	
		assertZeroResults(fldName, "\"" + s240 + "Ruckzug der biblischen Prophetie von der neueren Geschichte\"");
		assertZeroResults(fldName, "\"" + s240 + "Der Ruckzug der biblischen Prophetie von der neueren Geschichte\"");	
		
		// 240 has non-filing
		assertSingleResult("2400", fldName, "\"240 0 nonfiling\""); 
		assertZeroResults(fldName, "\"Wacky 240 0 nonfiling\""); 
		
		assertSingleResult("2402", fldName, "\"240 2 nonfiling\""); 
		assertZeroResults(fldName, "\"Wacky 240 2 nonfiling\""); 
		assertZeroResults(fldName, "\"A Wacky 240 2 nonfiling\""); 
		
		assertSingleResult("2407", fldName, "\"240 7 nonfiling\""); 
		assertZeroResults(fldName, "\"Tacky 240 7 nonfiling\""); 
		assertZeroResults(fldName, "\"A Wacky Tacky 240 7 nonfiling\""); 
		
		//TODO:  is there a way to test the sorting??
	}


	/**
	 * Test that title sort field deals properly with numeric subfields
	 */
@Test
	public final void testTitleSortNumericSubflds() 
	{
		String fldName = "title_sort";

	    solrFldMapTest.assertSolrFldValue(testFilePath, "2458", fldName, "245 has sub 8");
	    solrFldMapTest.assertSolrFldHasNoValue(testFilePath, "2458", fldName, "1.5\\a 245 has sub 8");
		
	    solrFldMapTest.assertSolrFldValue(testFilePath, "1306", fldName, "Sox on Fox 130 has sub 6");
	    solrFldMapTest.assertSolrFldHasNoValue(testFilePath, "1306", fldName, "880\\-01 Sox on Fox 130 has sub 6");

		// 240 no longer in title_sort
	    solrFldMapTest.assertSolrFldValue(testFilePath, "0240", fldName, "240 has sub 0");
	    solrFldMapTest.assertSolrFldHasNoValue(testFilePath, "0240", fldName, "sleep little fishies 240 has sub 0");
	    solrFldMapTest.assertSolrFldHasNoValue(testFilePath, "0240", fldName, "(DE-101c)310008891 sleep little fishies 240 has sub 0");

	    solrFldMapTest.assertSolrFldValue(testFilePath, "24025", fldName, "240 has sub 2 and 5");
	    solrFldMapTest.assertSolrFldHasNoValue(testFilePath, "24025", fldName, "la di dah 240 has sub 2 and 5");
	    solrFldMapTest.assertSolrFldHasNoValue(testFilePath, "24025", fldName, "ignore me la di dah NjP 240 has sub 2 and 5");
	}

	/**
	 * Test that search result title sort field ignores punctuation
	 */
@Test
	public final void testTitleSortPunct()
		throws ParserConfigurationException, IOException, SAXException, SolrServerException
	{
		String fldName = "title_sort";
		createFreshIx(testFileName);
	
		assertSingleResult("111", fldName, "\"ind 0 leading quotes\"");
		assertZeroResults(fldName, "\"\"ind 0 leading quotes\"\"");
		assertZeroResults(fldName, "\"ind 0 leading quotes\\\"\"");
		assertSingleResult("222", fldName, "\"required field\"");
		assertZeroResults(fldName, "\"**required field**\"");
		assertZeroResults(fldName, "\"required field**\"");
		assertSingleResult("333", fldName, "\"ind 0 leading hyphens\"");
		assertZeroResults(fldName, "\"--ind 0 leading hyphens\"");
		assertSingleResult("444", fldName, "\"ind 0 leading elipsis\"");
		assertZeroResults(fldName, "\"...ind 0 leading elipsis\"");
		assertSingleResult("555", fldName, "\"ind 0 leading quote elipsis\"");
		assertZeroResults(fldName, "\"\\\"...ind 0 leading quote elipsis\"");
		assertSingleResult("777", fldName, "\"ind 4 leading quote elipsis\"");
		assertZeroResults(fldName, "\"\\\"...ind 4 leading quote elipsis\"");
		assertSingleResult("888", fldName, "\"interspersed punctuation here\"");
		assertZeroResults(fldName, "\"interspersed *(punctua@#$@#$tion \"here--");
		assertZeroResults(fldName, "\"Boo! interspersed *(punctua@#$@#$tion \"here--");
		assertSingleResult("999", fldName, "everything");
		// lucene special chars:  + - && || ! ( ) { } [ ] ^ " ~ * ? : \
		assertZeroResults(fldName, "every!\\\"#$%\\&'\\(\\)\\*\\+,\\-./\\:;<=>\\?@\\[\\\\\\]\\^_`\\{|\\}\\~thing");
	}

}
