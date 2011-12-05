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
public class LanguageTests extends AbstractStanfordTest {
	
	String fldName = "language";
	String fileName = "langTests.mrc";
	String testFilePath = testDataParentPath + File.separator + fileName;

@Before
	public void setup() 
	{
		mappingTestInit();
	}

	
	/**
	 * Test population of language field
	 */
@Test
	public void testLanguages() 
			throws ParserConfigurationException, IOException, SAXException 
	{
		createIxInitVars(fileName);

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
	 * when 041a contains multiple language codes smushed together, they should
	 * be parsed out into separate language values.  SW-364
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


	/**
	 * lang facet should contain values in subfields a, d, e, j  of 041.  SW-392
	 */
@Test
	public void test041includedSubfields() 
	{
		solrFldMapTest.assertSolrFldHasNumValues(testFilePath, "041subfields", fldName, 8);
		// a
		solrFldMapTest.assertSolrFldValue(testFilePath, "041subfields", fldName, "Afar");
		solrFldMapTest.assertSolrFldValue(testFilePath, "041subfields", fldName, "Abkhaz");
		// b
		solrFldMapTest.assertSolrFldHasNoValue(testFilePath, "041subfields", fldName, "Achinese");
		solrFldMapTest.assertSolrFldHasNoValue(testFilePath, "041subfields", fldName, "Acoli");
		// d
		solrFldMapTest.assertSolrFldValue(testFilePath, "041subfields", fldName, "Adangme");
		solrFldMapTest.assertSolrFldValue(testFilePath, "041subfields", fldName, "Adygei");
		// e
		solrFldMapTest.assertSolrFldValue(testFilePath, "041subfields", fldName, "Afrikaans");
		solrFldMapTest.assertSolrFldValue(testFilePath, "041subfields", fldName, "Ainu");
		// f
		solrFldMapTest.assertSolrFldHasNoValue(testFilePath, "041subfields", fldName, "Aljamia");
		solrFldMapTest.assertSolrFldHasNoValue(testFilePath, "041subfields", fldName, "Akan");
		// g
		solrFldMapTest.assertSolrFldHasNoValue(testFilePath, "041subfields", fldName, "Akkadian");
		solrFldMapTest.assertSolrFldHasNoValue(testFilePath, "041subfields", fldName, "Albanian");
		// h
		solrFldMapTest.assertSolrFldHasNoValue(testFilePath, "041subfields", fldName, "Aleut");
		solrFldMapTest.assertSolrFldHasNoValue(testFilePath, "041subfields", fldName, "Altai");
		// j
		solrFldMapTest.assertSolrFldValue(testFilePath, "041subfields", fldName, "Amharic");
		solrFldMapTest.assertSolrFldValue(testFilePath, "041subfields", fldName, "Angika");
	}


}
