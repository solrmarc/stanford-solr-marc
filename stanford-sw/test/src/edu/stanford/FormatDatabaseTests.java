package edu.stanford;

import java.io.*;

import javax.xml.parsers.ParserConfigurationException;
import org.xml.sax.SAXException;

import org.apache.solr.client.solrj.SolrServerException;
import org.junit.Before;
import org.junit.Test;

import edu.stanford.enumValues.Format;

/**
 * junit4 tests for Stanford University format field for blacklight index
 * @author Naomi Dushay
 */
public class FormatDatabaseTests extends AbstractStanfordTest {
	
	String testFilePath = testDataParentPath + File.separator + "formatDatabaseTests.mrc";
	String facetFldName = "format";
	String dbAZval = Format.DATABASE_A_Z.toString();
	String otherVal = Format.OTHER.toString();

@Before
	public final void setup() 
	{
		mappingTestInit();
	}

	/**
	 * test format value Database A-Z population based on item type from 999
	 */
@Test
	public final void testDatabaseAZ()
	{
		// when it has no other format (would have been "Other"), then Database is the only value
		solrFldMapTest.assertSolrFldValue(testFilePath, "one999db", facetFldName, dbAZval);
		solrFldMapTest.assertSolrFldHasNoValue(testFilePath, "one999db", facetFldName, otherVal);
		
		solrFldMapTest.assertSolrFldValue(testFilePath, "one999Notdb", facetFldName, otherVal);
		solrFldMapTest.assertSolrFldHasNoValue(testFilePath, "one999Notdb", facetFldName, dbAZval);

		solrFldMapTest.assertSolrFldValue(testFilePath, "dbVideo", facetFldName, dbAZval);
		solrFldMapTest.assertSolrFldValue(testFilePath, "dbVideo", facetFldName, Format.VIDEO.toString()); 

		solrFldMapTest.assertSolrFldValue(testFilePath, "dbMusicRecording", facetFldName, dbAZval);
		solrFldMapTest.assertSolrFldValue(testFilePath, "dbMusicRecording", facetFldName, Format.MUSIC_RECORDING.toString());

		solrFldMapTest.assertSolrFldValue(testFilePath, "mult999oneDb", facetFldName, dbAZval);  
		solrFldMapTest.assertSolrFldValue(testFilePath, "mult999oneDb", facetFldName, Format.JOURNAL_PERIODICAL.toString());

		solrFldMapTest.assertSolrFldValue(testFilePath, "two99oneShadowWasOther", facetFldName, dbAZval);
		solrFldMapTest.assertSolrFldHasNoValue(testFilePath, "two99oneShadowWasOther", facetFldName, otherVal);
		
		solrFldMapTest.assertSolrFldValue(testFilePath, "DBandMusicRecOne999", facetFldName, dbAZval);
		solrFldMapTest.assertSolrFldValue(testFilePath, "DBandMusicRecOne999", facetFldName, Format.MUSIC_RECORDING.toString());

		solrFldMapTest.assertSolrFldValue(testFilePath, "otherBecomesDB", facetFldName, dbAZval);
		solrFldMapTest.assertSolrFldHasNoValue(testFilePath, "otherBecomesDB", facetFldName, otherVal); 

		solrFldMapTest.assertSolrFldValue(testFilePath, "dbOther", facetFldName, dbAZval);
		solrFldMapTest.assertSolrFldHasNoValue(testFilePath, "dbOther", facetFldName, otherVal);

		solrFldMapTest.assertSolrFldValue(testFilePath, "nother", facetFldName, dbAZval);
		solrFldMapTest.assertSolrFldHasNoValue(testFilePath, "nother", facetFldName, otherVal);
		
	}	

	/**
	 * test the additional Database format values aren't assigned
	 */
@Test
	public final void testDatabaseAZOnly() 
			throws IOException, SAXException, ParserConfigurationException, SolrServerException
	{
		createFreshIx("formatDatabaseTests.mrc");
		assertZeroResults(facetFldName, "\"Database (Other)\"");
		assertZeroResults(facetFldName, "\"Database (All)\"");
	}
	
}
