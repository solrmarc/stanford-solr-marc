package edu.stanford;

import java.io.File;
import java.io.IOException;
import java.util.*;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.solr.client.solrj.SolrServerException;
import org.junit.Before;
import org.junit.Test;
import org.xml.sax.SAXException;

/**
 * tests for MarcCombiningReader to ensure it indexes all the records it should
 *   (it doesn't stop indexing when it hits a bad record)
 * @author Naomi Dushay
 */
public class MarcCombiningReaderTests extends AbstractStanfordTest {

@Before
	public final void setup() 
	{
		mappingTestInit();
	}	
	
	/**
	 * test record can be indexed
	 */
@Test
	public void testRecord6024816()
			throws ParserConfigurationException, IOException, SAXException, SolrServerException 
	{
		assertSingleRecordFileIndexes("rec6024816.mrc", "6024816");
	}

	/**
	 * test bad record doesn't crash the program
	 */
//@Test  
// not working;  it seems the "bad" record now loads (parsing code now accommodates the error?)
// prior to 2011-05-12, this file was not part of AllTests, so this could have been broken for a long time
	public void testRecord6024817Bad()
			throws ParserConfigurationException, IOException, SAXException, SolrServerException 
	{
		String filename = "rec6024817.mrc";
		String recid = "6024817";
		
		createFreshIx(filename);
		assertDocNotPresent(recid);
	}

	/**
	 * test record can be indexed
	 */
@Test
	public void testRecord6024818()
			throws ParserConfigurationException, IOException, SAXException, SolrServerException 
	{
		assertSingleRecordFileIndexes("rec6024818.mrc", "6024818");
	}


	/**
	 * assert that the file containing a single record cleanly indexes
	 */
	private void assertSingleRecordFileIndexes(String filename, String recid)
			throws ParserConfigurationException, IOException, SAXException, SolrServerException
	{
		String testFilePath = testDataParentPath + File.separator + filename;
		solrFldMapTest.assertSolrFldValue(testFilePath, recid, "id", recid);
		
		createFreshIx(filename);
		assertDocPresent(recid);
	}

	/**
	 * test tiny file without bad record can be indexed
	 */
@Test
	public void testRecord6024816and8()
			throws ParserConfigurationException, IOException, SAXException, SolrServerException 
	{
		String filename = "rec6024816and8.mrc";

		Set<String> ids = new HashSet<String>();
		ids.clear();
		ids.add("6024816");
		ids.add("6024818");
		
		String testFilePath = testDataParentPath + File.separator + filename;
		assertRecordsInMap(testFilePath, ids);

		createFreshIx(filename);
		assertRecordsinIndex(ids);
	}

	/**
	 * test tiny file with bad record skips over the bad record
	 */
@Test
	public void testRecord6024816to8()
			throws ParserConfigurationException, IOException, SAXException, SolrServerException 
	{
		String filename = "rec6024816-8.mrc";

		Set<String> ids = new HashSet<String>();
		ids.clear();
		ids.add("6024816");
		ids.add("6024818");
		
		String testFilePath = testDataParentPath + File.separator + filename;
		assertRecordsInMap(testFilePath, ids);
	
		createFreshIx(filename);
		assertRecordsinIndex(ids);
	}

	/**
	 * assert records are in map to be written to index
	 */
	private void assertRecordsInMap(String testFilePath, Set<String> expectedIds)
	{
		for (String id : expectedIds)
		{
			solrFldMapTest.assertSolrFldValue(testFilePath, id, "id", id);
		}
	}
	
	/**
	 * assert index has expected records
	 */
	private void assertRecordsinIndex(Set<String> expectedIds)
			throws ParserConfigurationException, IOException, SAXException 
	{
		for (String id : expectedIds)
		{
			assertDocPresent(id);
		}
	}
	


}
