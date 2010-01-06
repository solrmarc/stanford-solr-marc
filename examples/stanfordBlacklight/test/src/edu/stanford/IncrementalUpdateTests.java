package edu.stanford;

import java.io.*;

import javax.xml.parsers.ParserConfigurationException;

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
	public final void testDeleteButEmptyIndex() 
		throws ParserConfigurationException, SAXException, IOException
	{
		createIxInitVars(null);
		deleteIxDocs(testDataParentPath + File.separator + "deleteFirstKey.txt");
	}

	/**
	 * Test when deleted record is the first one in the index
	 */
@Test 
	public final void testDeleteFirstRecord() 
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
	public final void testDeleteLastRecord() 
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
	public final void testDeletedMultRecords() 
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
	public final void testDeleteNonExistentRecords() 
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
	public final void testNewRecordsEmptyIx() 
			throws ParserConfigurationException, IOException, SAXException 
	{
		createIxInitVars("incrementalIxTests1.mrc");
	}

	
	/**
	 * Test that new records are added properly to an existing index
	 */
@Test
	public final void testNewRecordsExistingIx() 
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
	public final void testUpdatedRecords() 
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


}
