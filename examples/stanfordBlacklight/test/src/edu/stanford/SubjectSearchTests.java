package edu.stanford;

import java.io.File;

import org.junit.Before;
import org.junit.Test;

public class SubjectSearchTests extends AbstractStanfordBlacklightTest {

	private final String testDataFname = "subjectSearchTests.mrc";
	String testFilePath = testDataParentPath + File.separator + testDataFname;
	
@Before
	public final void setup() 
	{
		mappingTestInit();
	}

	/**
	 * subject_search_all should contain, for each subject field, a single 
	 *  string of all the alphabetic subfields concatenated together
	 */
@Test
	public void testSubjectSearchAll()
	{
		String fldName = "subject_all_search";
		solrFldMapTest.assertSolrFldValue(testFilePath, "600search", fldName, 
			"600a 600b 600c 600d 600e 600f 600g 600h 600j 600k 600l 600m 600n 600o 600p 600q 600r 600s 600t 600u 600v 600x 600y 600z");
		solrFldMapTest.assertSolrFldValue(testFilePath, "610search", fldName, 
			"610a 610b 610c 610d 610e 610f 610g 610h 610k 610l 610m 610n 610o 610p 610r 610s 610t 610u 610v 610x 610y 610z");
		solrFldMapTest.assertSolrFldValue(testFilePath, "611search", fldName, 
			"611a 611c 611d 611e 611f 611g 611h 611j 611k 611l 611n 611p 611q 611s 611t 611u 611v 611x 611y");
		solrFldMapTest.assertSolrFldValue(testFilePath, "630search", fldName, 
			"630a 630d 630e 630f 630g 630h 630k 630l 630m 630n 630o 630p 630r 630s 630t 630v 630x 630y 630z");
		solrFldMapTest.assertSolrFldValue(testFilePath, "648search", fldName, 
			"648a 648v 648x 648y 648z");
		solrFldMapTest.assertSolrFldValue(testFilePath, "650search", fldName, 
			"650a 650b 650c 650d 650e 650v 650x 650y 650z");
		solrFldMapTest.assertSolrFldValue(testFilePath, "651search", fldName, 
			"651a 651e 651v 651x 651y 651z");
		solrFldMapTest.assertSolrFldValue(testFilePath, "653search", fldName, 
			"653a");
		solrFldMapTest.assertSolrFldValue(testFilePath, "654search", fldName, 
			"654a 654b 654c 654e 654v 654y 654z");
		solrFldMapTest.assertSolrFldValue(testFilePath, "655search", fldName, 
			"655a 655b 655c 655v 655x 655y 655z");
		solrFldMapTest.assertSolrFldValue(testFilePath, "656search", fldName, 
			"656a 656k 656v 656x 656y 656z");
		solrFldMapTest.assertSolrFldValue(testFilePath, "657search", fldName, 
			"657a 657v 657x 657y 657z");
		solrFldMapTest.assertSolrFldValue(testFilePath, "658search", fldName, 
			"658a 658b 658c 658d");
		solrFldMapTest.assertSolrFldValue(testFilePath, "662search", fldName, 
			"662a 662b 662c 662d 662e 662f 662g 662h");
		solrFldMapTest.assertSolrFldValue(testFilePath, "690search", fldName, 
			"690a 690b 690c 690d 690e 690v 690x 690y 690z");
		solrFldMapTest.assertSolrFldValue(testFilePath, "691search", fldName, 
			"691a 691e 691v 691x 691y 691z");
		solrFldMapTest.assertSolrFldValue(testFilePath, "696search", fldName, 
			"696a 696b 696c 696d 696e 696f 696g 696h 696j 696k 696l 696m 696n 696o 696p 696q 696r 696s 696t 696u 696v 696x 696y 696z");
		solrFldMapTest.assertSolrFldValue(testFilePath, "697search", fldName, 
			"697a 697b 697c 697d 697e 697f 697g 697h 697j 697k 697l 697m 697n 697o 697p 697q 697r 697s 697t 697u 697v 697x 697y 697z");
		solrFldMapTest.assertSolrFldValue(testFilePath, "698search", fldName, 
			"698a 698b 698c 698d 698e 698f 698g 698h 698j 698k 698l 698m 698n 698o 698p 698q 698r 698s 698t 698u 698v 698x 698y 698z");
		solrFldMapTest.assertSolrFldValue(testFilePath, "699search", fldName, 
			"699a 699b 699c 699d 699e 699f 699g 699h 699j 699k 699l 699m 699n 699o 699p 699q 699r 699s 699t 699u 699v 699x 699y 699z");
	}

	/**
	 * vern_subject_search_all should contain, for each 880 linked to a subject 
	 * field, a single string of all the alphabetic subfields concatenated together
	 */
@Test
	public void testVernSubjectSearchAll()
	{
		String fldName = "vern_subject_all_search";
		solrFldMapTest.assertSolrFldValue(testFilePath, "Vern600search", fldName, 
			"vern600a vern600b vern600c vern600d vern600e vern600f vern600g vern600h vern600j vern600k vern600l vern600m vern600n vern600o vern600p vern600q vern600r vern600s vern600t vern600u vern600v vern600x vern600y vern600z");
		solrFldMapTest.assertSolrFldValue(testFilePath, "Vern610search", fldName, 
			"vern610a vern610b vern610c vern610d vern610e vern610f vern610g vern610h vern610k vern610l vern610m vern610n vern610o vern610p vern610r vern610s vern610t vern610u vern610v vern610x vern610y vern610z");
		solrFldMapTest.assertSolrFldValue(testFilePath, "Vern611search", fldName, 
			"vern611a vern611c vern611d vern611e vern611f vern611g vern611h vern611j vern611k vern611l vern611n vern611p vern611q vern611s vern611t vern611u vern611v vern611x vern611y");
		solrFldMapTest.assertSolrFldValue(testFilePath, "Vern630search", fldName, 
			"vern630a vern630d vern630e vern630f vern630g vern630h vern630k vern630l vern630m vern630n vern630o vern630p vern630r vern630s vern630t vern630v vern630x vern630y vern630z");
		solrFldMapTest.assertSolrFldValue(testFilePath, "Vern648search", fldName, 
				"vern648a vern648v vern648x vern648y vern648z");
		solrFldMapTest.assertSolrFldValue(testFilePath, "Vern650search", fldName, 
			"vern650a vern650b vern650c vern650d vern650e vern650v vern650x vern650y vern650z");
		solrFldMapTest.assertSolrFldValue(testFilePath, "Vern651search", fldName, 
			"vern651a vern651e vern651v vern651x vern651y vern651z");
		solrFldMapTest.assertSolrFldValue(testFilePath, "Vern653search", fldName, 
			"vern653a");
		solrFldMapTest.assertSolrFldValue(testFilePath, "Vern654search", fldName, 
			"vern654a vern654b vern654c vern654e vern654v vern654y vern654z");
		solrFldMapTest.assertSolrFldValue(testFilePath, "Vern655search", fldName, 
			"vern655a vern655b vern655c vern655v vern655x vern655y vern655z");
		solrFldMapTest.assertSolrFldValue(testFilePath, "Vern656search", fldName, 
			"vern656a vern656k vern656v vern656x vern656y vern656z");
		solrFldMapTest.assertSolrFldValue(testFilePath, "Vern657search", fldName, 
			"vern657a vern657v vern657x vern657y vern657z");
		solrFldMapTest.assertSolrFldValue(testFilePath, "Vern658search", fldName, 
			"vern658a vern658b vern658c vern658d");
		solrFldMapTest.assertSolrFldValue(testFilePath, "Vern662search", fldName, 
			"vern662a vern662b vern662c vern662d vern662e vern662f vern662g vern662h");
		solrFldMapTest.assertSolrFldValue(testFilePath, "Vern690search", fldName, 
			"vern690a vern690b vern690c vern690d vern690e vern690v vern690x vern690y vern690z");
		solrFldMapTest.assertSolrFldValue(testFilePath, "Vern691search", fldName, 
			"vern691a vern691e vern691v vern691x vern691y vern691z");
		solrFldMapTest.assertSolrFldValue(testFilePath, "Vern696search", fldName, 
			"vern696a vern696b vern696c vern696d vern696e vern696f vern696g vern696h vern696j vern696k vern696l vern696m vern696n vern696o vern696p vern696q vern696r vern696s vern696t vern696u vern696v vern696x vern696y vern696z");
		solrFldMapTest.assertSolrFldValue(testFilePath, "Vern697search", fldName, 
			"vern697a vern697b vern697c vern697d vern697e vern697f vern697g vern697h vern697j vern697k vern697l vern697m vern697n vern697o vern697p vern697q vern697r vern697s vern697t vern697u vern697v vern697x vern697y vern697z");
		solrFldMapTest.assertSolrFldValue(testFilePath, "Vern698search", fldName, 
			"vern698a vern698b vern698c vern698d vern698e vern698f vern698g vern698h vern698j vern698k vern698l vern698m vern698n vern698o vern698p vern698q vern698r vern698s vern698t vern698u vern698v vern698x vern698y vern698z");
		solrFldMapTest.assertSolrFldValue(testFilePath, "Vern699search", fldName, 
			"vern699a vern699b vern699c vern699d vern699e vern699f vern699g vern699h vern699j vern699k vern699l vern699m vern699n vern699o vern699p vern699q vern699r vern699s vern699t vern699u vern699v vern699x vern699y vern699z");
	}
	
	/**
	 * Test population and properties of topic_search field
	 */
/*
@Test
	public final void testTopicSearch() 
	{
        createIxInitVars(searchTestDataFname);
		String fldName = "topic_search";
		assertSearchFldMultValProps(fldName);

		// all subfields except v, x, y and z from  650, 690, 653, 654
		
		Set<String> docIds = new HashSet<String>();
		docIds.add("650search");
		docIds.add("Vern650search");
		assertSearchResults(fldName, "650a", docIds);
		assertSingleResult("650search", fldName, "650b");
		assertSingleResult("650search", fldName, "650c");
		assertSingleResult("650search", fldName, "650d");
		assertSingleResult("650search", fldName, "650e");		
		assertZeroResults(fldName, "650v");
		assertZeroResults(fldName, "650x");
		assertZeroResults(fldName, "650y");
		assertZeroResults(fldName, "650z");

		docIds.clear();
		docIds.add("690search");
		docIds.add("Vern690search");
		assertSearchResults(fldName, "690a", docIds);
		assertSingleResult("690search", fldName, "690b");
		assertSingleResult("690search", fldName, "690c");
		assertSingleResult("690search", fldName, "690d");
		assertSingleResult("690search", fldName, "690e");		
		assertZeroResults(fldName, "690v");
		assertZeroResults(fldName, "690x");
		assertZeroResults(fldName, "690y");
		assertZeroResults(fldName, "690z");

		docIds.clear();
		docIds.add("653search");
		docIds.add("Vern653search");
		assertSearchResults(fldName, "653a", docIds);
		assertZeroResults(fldName, "653v");
		assertZeroResults(fldName, "653x");
		assertZeroResults(fldName, "653y");
		assertZeroResults(fldName, "653z");

		docIds.clear();
		docIds.add("654search");
		docIds.add("Vern654search");
		assertSearchResults(fldName, "654a", docIds);
		assertSingleResult("654search", fldName, "654b");
		assertSingleResult("654search", fldName, "654c");
		assertSingleResult("654search", fldName, "654e");		
		assertZeroResults(fldName, "654v");
		assertZeroResults(fldName, "654x");
		assertZeroResults(fldName, "654y");
		assertZeroResults(fldName, "654z");

        createIxInitVars(testDataFname);
		assertSingleResult("1261173", fldName, "army");  // 650a
		assertSingleResult("4698973", fldName, "Multiculturalism");  // 650a
		assertSingleResult("4698973", fldName, "\"Flyby missions\"");  // 650-2
		assertSingleResult("919006", fldName, "\"Literature, Comparative\"");  // 650a
		assertSingleResult("4698973", fldName, "Multiculturalism");  // 650a
		// multiple occurrences when there are multiple MARC fields with the same tag
		assertSingleResult("229800", fldName, "Commodity exchanges."); 
		assertSingleResult("229800", fldName, "Foreign exchange."); 
		assertZeroResults(fldName, "nasat"); // 650-2
	}
*/
}
