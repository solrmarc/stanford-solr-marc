package edu.stanford;

import java.io.File;

import org.junit.Before;
import org.junit.Test;

/**
 * junit4 mapping tests for author-title searching fields
 * @author Naomi Dushay
 */
public class AuthorTitleMappingTests extends AbstractStanfordTest 
{
	private String fldName = "author_title_search";
	private String testFileName = "authorTitleMappingTests.mrc";
    private String testFilePath = testDataParentPath + File.separator + testFileName;

@Before
	public final void setup() 
	{
		mappingTestInit();
	}	

	/**
	 * Test author-title searching field values from 100, 110, 111
	 *   1xx - all alpha except e + 240[a-z] ; if no 240, then 245a 
	 */
@Test
	public final void testAuthorTitleSearch1xx() 
	{
		String search240value = " 240a 240d 240f 240g 240h 240k 240l 240m 240n 240o 240p 240r 240s";
		String search245value = " 245a";

		// 1xx - all subfields except e
		
		String search100value = "100a 100b 100c 100d 100f 100g 100j 100k 100l 100n 100p 100q 100t 100u";
		solrFldMapTest.assertSolrFldValue(testFilePath, "100240", fldName, search100value + search240value);
	    solrFldMapTest.assertSolrFldValue(testFilePath, "100no240", fldName, search100value + search245value);

	    String search110value = "110a 110b 110c 110d 110f 110g 110k 110l 110n 110p 110t 110u";
	    solrFldMapTest.assertSolrFldValue(testFilePath, "110240", fldName, search110value + search240value);
	    solrFldMapTest.assertSolrFldValue(testFilePath, "110no240", fldName, search110value + search245value);
		
	    String search111value = "111a 111c 111d 111e 111f 111g 111j 111k 111l 111n 111p 111q 111t 111u";
	    solrFldMapTest.assertSolrFldValue(testFilePath, "111240", fldName, search111value + search240value);
	    solrFldMapTest.assertSolrFldValue(testFilePath, "111no240", fldName, search111value + search245value);
	}
	
	/**
	 * Test author-title searching field values from 700, 710, 711 
	 *   should only have a value if there is a subfield t
	 *   value should include all subfields except e, i, x
	 */
@Test
	public final void testAuthorTitleSearch7xx() 
	{
		// 7xx:  all subfields except e, i, x

		String sub700b4t = "700a 700b 700c 700d 700f 700g 700h 700j 700k 700l 700m 700n 700o 700p 700q 700r 700s";
		String search700value = sub700b4t + " 700t 700u";
	    solrFldMapTest.assertSolrFldValue(testFilePath, "700", fldName, search700value);
	    
	    String sub710b4t = "710a 710b 710c 710d 710f 710g 710h 710k 710l 710m 710n 710o 710p 710r 710s";
	    String search710value = sub710b4t + " 710t 710u";
	    solrFldMapTest.assertSolrFldValue(testFilePath, "710", fldName, search710value);
	    
	    String sub711b4t = "711a 711c 711d 711e 711f 711g 711h 711j 711k 711l 711n 711p 711q 711s";
	    String search711value = sub711b4t + " 711t 711u";
	    solrFldMapTest.assertSolrFldValue(testFilePath, "711", fldName, search711value);   
	    
	    // if no subfield t is present, ignore field
		String search700nosubt = sub700b4t + " 700u";
	    solrFldMapTest.assertSolrFldHasNoValue(testFilePath, "700nosubt", fldName, search700nosubt);

	    String search710nosubt = sub710b4t + " 710u";
	    solrFldMapTest.assertSolrFldHasNoValue(testFilePath, "710nosubt", fldName, search710nosubt);

	    String search711nosubt = sub711b4t + " 711u";
	    solrFldMapTest.assertSolrFldHasNoValue(testFilePath, "711nosubt", fldName, search711nosubt);
	}

	/**
	 * Test author-title searching field values from 800, 810, 811
	 *   should only have a value if there is a subfield t
	 *   value should include all subfields except e, v, w, x
	 */
@Test
	public final void testAuthorTitleSearch8xx() 
	{	
		// 8xx:  all subfields except e, v, w, x

		String sub800b4t = "800a 800b 800c 800d 800f 800g 800h 800j 800k 800l 800m 800n 800o 800p 800q 800r 800s";
		String search800value = sub800b4t + " 800t 800u";
	    solrFldMapTest.assertSolrFldValue(testFilePath, "800", fldName, search800value);

	    String sub810b4t = "810a 810b 810c 810d 810f 810g 810h 810j 810k 810l 810m 810n 810o 810p 810r 810s";
	    String search810value = sub810b4t + " 810t 810u";
		solrFldMapTest.assertSolrFldValue(testFilePath, "810", fldName, search810value);

		String sub811b4t = "811a 811c 811d 811f 811g 811h 811j 811k 811l 811n 811p 811q 811s";
		String search811value = sub811b4t + " 811t 811u";
	    solrFldMapTest.assertSolrFldValue(testFilePath, "811", fldName, search811value);   
	    
	    // if no subfield t is present, ignore field
	    
		String search800nosubt = sub800b4t + " 800u";
	    solrFldMapTest.assertSolrFldHasNoValue(testFilePath, "800nosubt", fldName, search800nosubt);

	    String search810nosubt = sub810b4t + " 810u";
	    solrFldMapTest.assertSolrFldHasNoValue(testFilePath, "810nosubt", fldName, search810nosubt);

	    String search811nosubt = sub811b4t + " 811u";
	    solrFldMapTest.assertSolrFldHasNoValue(testFilePath, "811nosubt", fldName, search811nosubt);
	}
	


	/**
	 * Test vernacular author-title searching field values from 100, 110, 111
	 *  followed by 240 or 245a
	 */
@Test
	public final void testVernAuthorTitleSearch1xx() 
	{
		String vern240value = " vern240a vern240d vern240f vern240g vern240h vern240k vern240l vern240m vern240n vern240o vern240p vern240r vern240s";
		String vern245value = " vern245a";
		String plain240value = " 240a 240d 240f 240g 240h 240k 240l 240m 240n 240o 240p 240r 240s";
		String plain245a = " 245a";

		// 1xx - all subfields except e

		String vern100value = "vern100a vern100b vern100c vern100d vern100f vern100g vern100j vern100k vern100l vern100n vern100p vern100q vern100t vern100u";

		solrFldMapTest.assertSolrFldValue(testFilePath, "vern100vern240", fldName, vern100value + vern240value);
	    solrFldMapTest.assertSolrFldValue(testFilePath, "vern100vern245", fldName, vern100value + vern245value);

	    // next two records have a minimal 100 field to link to 880
	    solrFldMapTest.assertSolrFldHasNoValue(testFilePath, "vern100no240", fldName, vern100value + plain245a);
	    solrFldMapTest.assertSolrFldHasNumValues(testFilePath, "vern100no240", fldName, 1);
	    solrFldMapTest.assertSolrFldValue(testFilePath, "vern100no240", fldName, "100a" + plain245a);

	    solrFldMapTest.assertSolrFldHasNoValue(testFilePath, "vern100plain240", fldName, vern100value + plain240value);
	    solrFldMapTest.assertSolrFldHasNumValues(testFilePath, "vern100plain240", fldName, 1);
	    solrFldMapTest.assertSolrFldValue(testFilePath, "vern100plain240", fldName, "100a" + plain240value);


	    String vern110value = "vern110a vern110b vern110c vern110d vern110f vern110g vern110k vern110l vern110n vern110p vern110t vern110u";

		solrFldMapTest.assertSolrFldValue(testFilePath, "vern110vern240", fldName, vern110value + vern240value);
	    solrFldMapTest.assertSolrFldValue(testFilePath, "vern110vern245", fldName, vern110value + vern245value);

	    // next record has a minimal 110 field to link to 880
	    solrFldMapTest.assertSolrFldHasNoValue(testFilePath, "vern110no240", fldName, vern110value + plain245a);
	    solrFldMapTest.assertSolrFldHasNumValues(testFilePath, "vern110no240", fldName, 1);
	    solrFldMapTest.assertSolrFldValue(testFilePath, "vern110no240", fldName, "110a"  + plain245a);

	    
	    String vern111value = "vern111a vern111c vern111d vern111e vern111f vern111g vern111j vern111k vern111l vern111n vern111p vern111q vern111t vern111u";
	    
		solrFldMapTest.assertSolrFldValue(testFilePath, "vern111vern240", fldName, vern111value + vern240value);
	    solrFldMapTest.assertSolrFldValue(testFilePath, "vern111vern245", fldName, vern111value + vern245value);

	    // next record has a minimal 111 field to link to 880
	    solrFldMapTest.assertSolrFldHasNoValue(testFilePath, "vern111no240", fldName, vern111value + plain245a);
	    solrFldMapTest.assertSolrFldHasNumValues(testFilePath, "vern111no240", fldName, 1);
	    solrFldMapTest.assertSolrFldValue(testFilePath, "vern111no240", fldName, "111a" + plain245a);
	}

	/**
	 * Test vernacular author-title searching field values from 700, 710, 711
	 *   should only have a value if there is a subfield t
	 *   value should include all subfields except e, i, x
	 */
@Test
	public final void testVernAuthorTitleSearch7xx() 
	{
	    // (vern) 7xx:  all subfields except e, x

	    String vernSub700b4t = "vern700a vern700b vern700c vern700d vern700f vern700g vern700h vern700j vern700k vern700l vern700m vern700n vern700o vern700p vern700q vern700r vern700s";
	    String vern700value = vernSub700b4t + " vern700t vern700u";
	    solrFldMapTest.assertSolrFldValue(testFilePath, "vern700", fldName, vern700value);
    
	    String vernSub710b4t = "vern710a vern710b vern710c vern710d vern710f vern710g vern710h vern710k vern710l vern710m vern710n vern710o vern710p vern710r vern710s";
	    String vern710value = vernSub710b4t + " vern710t vern710u";
	    solrFldMapTest.assertSolrFldValue(testFilePath, "vern710", fldName, vern710value);
    
	    String vernSub711b4t = "vern711a vern711c vern711d vern711e vern711f vern711g vern711h vern711j vern711k vern711l vern711n vern711p vern711q vern711s";
	    String search711value = vernSub711b4t + " vern711t vern711u";
	    solrFldMapTest.assertSolrFldValue(testFilePath, "vern711", fldName, search711value);   
	    
	    // if no subfield t is present, ignore field
	    solrFldMapTest.assertNoSolrFld(testFilePath, "vern700nosubt", fldName);
	    solrFldMapTest.assertNoSolrFld(testFilePath, "vern710nosubt", fldName);
	    solrFldMapTest.assertNoSolrFld(testFilePath, "vern711nosubt", fldName);
	}


	/**
	 * Test vernacular author-title searching field values from 800, 810, 811
	 *   should only have a value if there is a subfield t
	 *   value should include all subfields except e, v, w, x
	 */
@Test
	public final void testVernAuthorTitleSearch8xx() 
	{
		// (vern) 8xx:  all subfields except e, v, w, x
	
		String vernSub800b4t = "vern800a vern800b vern800c vern800d vern800f vern800g vern800h vern800j vern800k vern800l vern800m vern800n vern800o vern800p vern800q vern800r vern800s";
		String vern800value = vernSub800b4t + " vern800t vern800u";
	    solrFldMapTest.assertSolrFldValue(testFilePath, "vern800", fldName, vern800value);
	
	    String vernSub810b4t = "vern810a vern810b vern810c vern810d vern810f vern810g vern810h vern810k vern810l vern810m vern810n vern810o vern810p vern810r vern810s";
	    String vern810value = vernSub810b4t + " vern810t vern810u";
		solrFldMapTest.assertSolrFldValue(testFilePath, "vern810", fldName, vern810value);
	
		String vernSub811b4t = "vern811a vern811c vern811d vern811f vern811g vern811h vern811j vern811k vern811l vern811n vern811p vern811q vern811s";
		String vern811value = vernSub811b4t + " vern811t vern811u";
	    solrFldMapTest.assertSolrFldValue(testFilePath, "vern811", fldName, vern811value);   
	    
	    // if no subfield t is present, ignore field
	    solrFldMapTest.assertNoSolrFld(testFilePath, "vern800nosubt", fldName);
	    solrFldMapTest.assertNoSolrFld(testFilePath, "vern810nosubt", fldName);
	    solrFldMapTest.assertNoSolrFld(testFilePath, "vern811nosubt", fldName);
	}

}
