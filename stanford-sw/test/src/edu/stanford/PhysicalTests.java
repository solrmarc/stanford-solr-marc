package edu.stanford;

import java.io.File;
import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import org.junit.*;

/**
 * junit4 tests for Stanford University physical fields for blacklight index
 * @author Naomi Dushay
 */
public class PhysicalTests extends AbstractStanfordTest {
	
@Before
	public final void setup() 
	{
		mappingTestInit();
	}	

	/**
	 * physical:  test population of field for search and display
	 */
@Test
	public final void testPhysicalSearch() 
			throws IOException, ParserConfigurationException, SAXException 
	{
		String fldName = "physical";
		createIxInitVars("physicalTests.mrc");

		assertSingleResult("300111", fldName, "sound disc");
		assertSingleResult("300111", fldName, "\"1 sound disc (20 min.); analog, 33 1/3 rpm, stereo. ; 12 in.\"");
		assertSingleResult("300222", fldName, "answer");
		assertSingleResult("300222", fldName, "\"271 p. : ill. ; 21 cm. + answer book.\"");
		assertSingleResult("300333", fldName, "\"1 box 2 x 4 x 3 1/2 ft.\"");
		assertSingleResult("300444", fldName, "\"diary 1 volume (463 pages) ; 17 cm. x 34.5 cm.\"");	    
	}


	/**
	 * vern_physical:  test population of field for display
	 */
@Test
	public final void testPhysicalStored()
	{
		String testFilePath = testDataParentPath + File.separator + "displayFieldsTests.mrc";
		String fldName = "physical";
		
	    solrFldMapTest.assertSolrFldValue(testFilePath, "3001", fldName, "1 sound disc (20 min.); analog, 33 1/3 rpm, stereo. ; 12 in.");
	    solrFldMapTest.assertSolrFldValue(testFilePath, "3002", fldName, "1 box 2 x 4 x 3 1/2 ft.");
	    solrFldMapTest.assertSolrFldValue(testFilePath, "3003", fldName, "17 boxes (7 linear ft.)");
	    solrFldMapTest.assertSolrFldValue(testFilePath, "3004", fldName, "1 page ; 108 cm. x 34.5 cm.");
	    solrFldMapTest.assertSolrFldValue(testFilePath, "3005", fldName, "65 prints : relief process ; 29 x 22 cm.");
	    solrFldMapTest.assertSolrFldValue(testFilePath, "3005", fldName, "8 albums (550 photoprints) ; 51 x 46 cm. or smaller.");
	    
	    testFilePath = testDataParentPath + File.separator + "physicalTests.mrc";
	    // 300abc
	    solrFldMapTest.assertSolrFldValue(testFilePath, "300111", fldName, "1 sound disc (20 min.); analog, 33 1/3 rpm, stereo. ; 12 in.");
	    // 300abce
	    solrFldMapTest.assertSolrFldValue(testFilePath, "300222", fldName, "271 p. : ill. ; 21 cm. + answer book.");
	    // 300 3afg
	    solrFldMapTest.assertSolrFldValue(testFilePath, "300333", fldName, "1 box 2 x 4 x 3 1/2 ft.");
	    // 300aafafc - in order ...
	    solrFldMapTest.assertSolrFldValue(testFilePath, "300444", fldName, "diary 1 volume (463 pages) ; 17 cm. x 34.5 cm.");
	}


	/**
	 * vern_physical:  test population of field for search and display
	 */
@Test
	public final void testVernPhysical()
	{
		String testFilePath = testDataParentPath + File.separator + "vernacularSearchTests.mrc";
		String fldName = "vern_physical";
		
	    solrFldMapTest.assertSolrFldValue(testFilePath, "300VernSearch", fldName, "vern300a vern300b vern300c vern300e vern300f vern300g");
	    solrFldMapTest.assertSolrFldHasNoValue(testFilePath, "300VernSearch", fldName, "none");
	}

}
