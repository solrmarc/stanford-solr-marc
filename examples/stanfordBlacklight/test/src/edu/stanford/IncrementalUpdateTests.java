package edu.stanford;

import java.io.*;

import javax.xml.parsers.ParserConfigurationException;

import org.solrmarc.solr.DocumentProxy;
import org.xml.sax.SAXException;

import org.junit.*;


/**
 * junit4 tests for Stanford University revisions to solrmarc
 * @author Naomi Dushay
 */
public class IncrementalUpdateTests extends AbstractStanfordBlacklightTest {
	
	
	/**
	 * Test deleting record when there is no index
	 */
@Test 
	public void testDeleteButEmptyIndex() 
		throws ParserConfigurationException, SAXException, IOException
	{
		createIxInitVars(null);
		deleteIxDocs(testDataParentPath + File.separator + "deleteFirstKey.txt");
	}

	/**
	 * Test when deleted record is the first one in the index
	 */
@Test 
	public void testDeleteFirstRecord() 
		throws ParserConfigurationException, SAXException, IOException
	{
		createIxInitVars("incrementalIxTests1.mrc");
		assertDocPresent("1");
		deleteIxDocs(testDataParentPath + File.separator + "deleteFirstKey.txt");
		assertDocNotPresent("1");
	}

	
	/**
	 * Test when deleted record is the last one in the index
	 */
@Test 
	public void testDeleteLastRecord() 
		throws ParserConfigurationException, SAXException, IOException
	{
		createIxInitVars("incrementalIxTests1.mrc");
		assertDocPresent("9");
		deleteIxDocs(testDataParentPath + File.separator + "deleteLastKey.txt");
		assertDocNotPresent("9");
	}

	/**
	 * Test when deleted records include the first and last and some middle ones,
	 *   and they're not in order 
	 */
@Test 
	public void testDeletedMultRecords() 
		throws ParserConfigurationException, SAXException, IOException
	{
		createIxInitVars("incrementalIxTests1.mrc");
		assertDocPresent("1");
		assertDocPresent("2");
		assertDocPresent("5");
		assertDocPresent("8");
		assertDocPresent("9");
		// these keys are NOT in this order in the delete file
		deleteIxDocs(testDataParentPath + File.separator + "deleteMultKeys.txt");
		assertDocNotPresent("1");
		assertDocNotPresent("2");
		assertDocNotPresent("5");
		assertDocNotPresent("8");
		assertDocNotPresent("9");
	}
	

/**
	 * Test that deletion of non-existing records smoothly reports error and
	 *  causes no other problems
	 */
@Test
	public void testDeleteNonExistentRecords() 
	    throws ParserConfigurationException, IOException, SAXException
	{
		// file also has key that exists
		// file is also out of numeric order
		createIxInitVars("incrementalIxTests1.mrc");
		assertDocNotPresent("666");
		deleteIxDocs(testDataParentPath + File.separator + "deleteMissingKey.txt");
		assertDocNotPresent("666");
	}


	/**
	 * Test that new records are added properly to an empty index
	 */
@Test
	public void testNewRecordsEmptyIx() 
			throws ParserConfigurationException, IOException, SAXException 
	{
		createIxInitVars("incrementalIxTests1.mrc");
	}

	
	/**
	 * Test that new records are added properly to an existing index
	 */
@Test
	public void testNewRecordsExistingIx() 
			throws ParserConfigurationException, IOException, SAXException 
	{
		createIxInitVars("incrementalIxTests1.mrc");
		assertDocPresent("1");
		assertDocNotPresent("99");
		updateIx("incrementalIxTests2.mrc");
		assertDocPresent("1");
		assertDocPresent("99");
	}
	
	
	/**
	 * Test that records are updated properly in an existing index
	 *   when deleted then added
	 *   when just updated record present (not deleted)
	 */
@Test
	public void testUpdatedRecords() 
			throws ParserConfigurationException, IOException, SAXException 
	{
		String titleFldName = "title_245a_display";
		String authorFldName = "author_person_display";
		createIxInitVars("incrementalIxTests1.mrc");

		// record 2: field that exists in old and records, but changed
		// record 3: field in new record that wasn't in old record
		// record 4: field in old record that isn't in new record

		assertDocHasFieldValue("2", titleFldName, "record 2");
		assertDocHasNoField("3", authorFldName);
		assertDocHasFieldValue("4", authorFldName, "original 100 field in record 4");
		
		updateIx("incrementalIxTests2.mrc");
		assertDocHasFieldValue("2", titleFldName, "updated record 2 - changed title field");
		assertDocHasFieldValue("3", authorFldName, "added author field");
		assertDocHasNoField("4", authorFldName);

		// TODO: check multiple occurrences of fields
	}

	// TODO:  test when multiple occurrences of marc record with same bib key in file


	/**
	 * created date field needs to retain the original value, while 
	 *  last_updated date field should use latest indexing time
	 */
//@Test
	public void testDates()
			throws ParserConfigurationException, IOException, SAXException 
	{
		String createDateFldName = "created";
		String updateDateFldName = "last_updated";
		createIxInitVars("incrementalIxTests1.mrc");
		
		// assert created and last_updated are the same
		assertFieldValuesEqual("1", createDateFldName, updateDateFldName);
		assertFieldValuesEqual("2", createDateFldName, updateDateFldName);
		assertFieldValuesEqual("3", createDateFldName, updateDateFldName);
		assertFieldValuesEqual("4", createDateFldName, updateDateFldName);

		updateIx("incrementalIxTests2.mrc");
		// assert created and last_updated are the same
		//   new record
		assertFieldValuesEqual("99", createDateFldName, updateDateFldName);
		//   old unchanged record
		assertFieldValuesEqual("1", createDateFldName, updateDateFldName);
		
		// assert created < last_updated  2, 3, 4
		int solrDocNum = getSingleDocNum(docIDfname, "2");
		DocumentProxy doc = getSearcherProxy().getDocumentProxyBySolrDocNum(solrDocNum);
		String[] createVals = doc.getValues(createDateFldName);
		String[] updateVals = doc.getValues(updateDateFldName);
		
/* won't compile with current build.xml  2010-07-31
test commented out anyway b/c it's ahead of the actual code being written
		org.apache.solr.schema.DateField d = new org.apache.solr.schema.DateField();
	
		java.util.Date createDate = d.parseMath(null, createVals[0]);
		java.util.Date updateDate = d.parseMath(null, updateVals[0]);
		org.junit.Assert.assertTrue(createDate.before(updateDate));
*/		
	}

}
