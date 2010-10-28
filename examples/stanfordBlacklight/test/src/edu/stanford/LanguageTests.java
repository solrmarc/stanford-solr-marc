package edu.stanford;

import java.io.File;
import java.io.IOException;
import java.util.*;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import org.junit.*;


/**
 * junit4 tests for Stanford University revisions to solrmarc
 * @author Naomi Dushay
 */
public class LanguageTests extends AbstractStanfordBlacklightTest {
	
	String fldName = "language";
	String fileName = "langTests.mrc";
	String testFilePath = testDataParentPath + File.separator + fileName;

@Before
	public void setup() 
		throws ParserConfigurationException, IOException, SAXException 
	{
		createIxInitVars(fileName);
		mappingTestInit();
	}


	/**
	 * Test language field properties
	 */
@Test
	public void testLangFieldProps() 
			throws ParserConfigurationException, IOException, SAXException 
	{
	    assertStringFieldProperties(fldName);
	    assertFieldIndexed(fldName);
	    assertFieldStored(fldName);
		assertFieldMultiValued(fldName);
	}

	
	/**
	 * Test population of language field
	 */
@Test
	public void testLanguages() 
			throws ParserConfigurationException, IOException, SAXException 
	{
		assertSingleResult("008mul041atha", fldName, "Thai"); 
		assertSingleResult("008eng3041a", fldName, "German"); 
		assertSingleResult("008eng3041a", fldName, "Russian");  // not 041h: id 008eng2041a041h 
		assertSingleResult("008eng2041a041h", fldName, "\"Greek, Ancient (to 1453)\""); 
		assertSingleResult("008nor041ad", fldName, "Norwegian"); 
		assertSingleResult("008nor041ad", fldName, "Swedish"); 

		assertZeroResults(fldName, "Italian");  // not 041k:  id 008mis041ak

		Set<String> docIds = new HashSet<String>();
		docIds.add("008eng3041a");
		docIds.add("008eng2041a041h");
		assertSearchResults(fldName, "English", docIds);
		docIds.clear();
		docIds.add("008spa");
		docIds.add("008fre041d");
		docIds.add("041aHas3");
		assertSearchResults(fldName, "Spanish", docIds);
		docIds.clear();
		docIds.add("008fre041d");
		docIds.add("041aHas3");
		assertSearchResults(fldName, "French", docIds);
	}

	/**
	 * Test that there is no field created when the map is missing
	 *  the value to be mapped and when the map has value set to null
	 */
@Test
	public void testMapMissingValue() 
			throws ParserConfigurationException, IOException, SAXException 
	{
		assertZeroResults(fldName, "null");
		assertZeroResults(fldName, "\\?\\?\\?");
		assertZeroResults(fldName, "mis");     // 008mis041ak
		assertZeroResults(fldName, "Miscellaneous languages");
		assertZeroResults(fldName, "mul");     // 008mul041atha
		assertZeroResults(fldName, "Multiple languages"); 
		assertZeroResults(fldName, "und");
		assertZeroResults(fldName, "zxx");
	}

	/**
	 * when 041a contains multiple language codes smushed together, they should
	 * be parsed out into separate language values.
	 */
@Test
	public void test041aMultMushed() 
	{
		// raw value:  041a: catfrespa  (mul in 008)
		solrFldMapTest.assertSolrFldHasNumValues(testFilePath, "041aHas3", fldName, 3);
		solrFldMapTest.assertSolrFldValue(testFilePath, "041aHas3", fldName, "Catalan");
		solrFldMapTest.assertSolrFldValue(testFilePath, "041aHas3", fldName, "French");
		solrFldMapTest.assertSolrFldValue(testFilePath, "041aHas3", fldName, "Spanish");
	}

}
