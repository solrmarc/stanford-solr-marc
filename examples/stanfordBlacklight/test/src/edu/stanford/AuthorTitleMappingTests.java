package edu.stanford;

import java.io.File;

import org.junit.Before;
import org.junit.Test;

/**
 * junit4 mapping tests for author-title searching fields
 * @author Naomi Dushay
 */
public class AuthorTitleMappingTests extends AbstractStanfordBlacklightTest {

	private String testFileName = "authorTitleMappingTests.mrc";
    private String testFilePath = testDataParentPath + File.separator + testFileName;

@Before
	public final void setup() 
	{
		mappingTestInit();
	}	

	/**
	 * Test author-title searching field - mapping test
	 */
@Test
	public final void testAuthorTitleSearch() 
	{
		String fldName = "author_title_search";
	
		String search100subfldStr = "100a 100b 100c 100d 100e 100g 100j 100q 100u";
		String search240subfldStr = " 240a 240d 240f 240g 240k 240l 240m 240n 240o 240p 240r 240s";
		String search245subfldStr = " 245a";
	    solrFldMapTest.assertSolrFldValue(testFilePath, "100240", fldName, search100subfldStr + search240subfldStr);
	    solrFldMapTest.assertSolrFldValue(testFilePath, "100no240", fldName, search100subfldStr + search245subfldStr);
		String search110subfldStr = "110a 110b 110c 110d 110e 110g 110n 110u";
	    solrFldMapTest.assertSolrFldValue(testFilePath, "110240", fldName, search110subfldStr + search240subfldStr);
	    solrFldMapTest.assertSolrFldValue(testFilePath, "110no240", fldName, search110subfldStr + search245subfldStr);
		String search111subfldStr = "111a 111c 111d 111e 111g 111j 111n 111q 111u";
	    solrFldMapTest.assertSolrFldValue(testFilePath, "111240", fldName, search111subfldStr + search240subfldStr);
	    solrFldMapTest.assertSolrFldValue(testFilePath, "111no240", fldName, search111subfldStr + search245subfldStr);
	    
		String search700subfldStr = "700a 700b 700c 700d 700e 700f 700g 700j 700k 700l 700m 700n 700o 700p 700q 700r 700s 700t 700u";
	    solrFldMapTest.assertSolrFldValue(testFilePath, "700", fldName, search700subfldStr);
		String search710subfldStr = "710a 710b 710c 710d 710e 710f 710g 710k 710l 710m 710n 710o 710p 710r 710s 710t 710u";
	    solrFldMapTest.assertSolrFldValue(testFilePath, "710", fldName, search710subfldStr);
		String search711subfldStr = "711a 711c 711d 711e 711f 711g 711j 711k 711l 711n 711p 711q 711s 711t 711u";
	    solrFldMapTest.assertSolrFldValue(testFilePath, "711", fldName, search711subfldStr);   
	}
	

	/**
	 * Test vernacular author-title searching field - mapping test
	 */
@Test
	public final void testVernAuthorTitleSearch() 
	{
		String fldName = "author_title_search";
	
		String searchVern100subfldStr = "vern100a vern100b vern100c vern100d vern100e vern100g vern100j vern100q vern100u";
		String searchVern240subfldStr = " vern240a vern240d vern240f vern240g vern240k vern240l vern240m vern240n vern240o vern240p vern240r vern240s";
		String searchVern245subfldStr = " vern245a";
		String plain240subfldStr = " 240a 240d 240f 240g 240k 240l 240m 240n 240o 240p 240r 240s";
		String plain245subfldStr = " 245a";
	    solrFldMapTest.assertSolrFldValue(testFilePath, "vern100vern240", fldName, searchVern100subfldStr + searchVern240subfldStr);
	    solrFldMapTest.assertSolrFldValue(testFilePath, "vern100vern245", fldName, searchVern100subfldStr + searchVern245subfldStr);
	    solrFldMapTest.assertSolrFldValue(testFilePath, "vern100no240", fldName, searchVern100subfldStr + plain245subfldStr);
	    solrFldMapTest.assertSolrFldValue(testFilePath, "vern100plain240", fldName, searchVern100subfldStr + plain240subfldStr);
	    
		String searchVern110subfldStr = "vern110a vern110b vern110c vern110d vern110e vern110g vern110n vern110u";
	    solrFldMapTest.assertSolrFldValue(testFilePath, "vern110vern240", fldName, searchVern110subfldStr + searchVern240subfldStr);
	    solrFldMapTest.assertSolrFldValue(testFilePath, "vern110vern245", fldName, searchVern110subfldStr + searchVern245subfldStr);
	    solrFldMapTest.assertSolrFldValue(testFilePath, "vern110no240", fldName, searchVern110subfldStr + plain245subfldStr);
		String searchVern111subfldStr = "vern111a vern111c vern111d vern111e vern111g vern111j vern111n vern111q vern111u";
	    solrFldMapTest.assertSolrFldValue(testFilePath, "vern111vern240", fldName, searchVern111subfldStr + searchVern240subfldStr);
	    solrFldMapTest.assertSolrFldValue(testFilePath, "vern111vern245", fldName, searchVern111subfldStr + searchVern245subfldStr);
	    solrFldMapTest.assertSolrFldValue(testFilePath, "vern111no240", fldName, searchVern111subfldStr + plain245subfldStr);
	    
		String searchVern700subfldStr = "vern700a vern700b vern700c vern700d vern700e vern700f vern700g vern700j vern700k vern700l vern700m vern700n vern700o vern700p vern700q vern700r vern700s vern700t vern700u";
	    solrFldMapTest.assertSolrFldValue(testFilePath, "vern700", fldName, searchVern700subfldStr);
		String searchVern710subfldStr = "vern710a vern710b vern710c vern710d vern710e vern710f vern710g vern710k vern710l vern710m vern710n vern710o vern710p vern710r vern710s vern710t vern710u";
	    solrFldMapTest.assertSolrFldValue(testFilePath, "vern710", fldName, searchVern710subfldStr);
		String searchVern711subfldStr = "vern711a vern711c vern711d vern711e vern711f vern711g vern711j vern711k vern711l vern711n vern711p vern711q vern711s vern711t vern711u";
	    solrFldMapTest.assertSolrFldValue(testFilePath, "vern711", fldName, searchVern711subfldStr);   
	}

}
