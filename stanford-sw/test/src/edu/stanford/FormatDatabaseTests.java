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
	String fldName = "format_main_ssim";
	String dbAZval = Format.DATABASE_A_Z.toString();
	String otherVal = Format.OTHER.toString();
	/** @deprecated temporary */
	String updatingOtherVal = Format.UPDATING_OTHER.toString();

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
		solrFldMapTest.assertSolrFldValue(testFilePath, "one999db", fldName, dbAZval);
		solrFldMapTest.assertSolrFldHasNoValue(testFilePath, "one999db", fldName, otherVal);

		solrFldMapTest.assertSolrFldValue(testFilePath, "one999Notdb", fldName, updatingOtherVal);
		solrFldMapTest.assertSolrFldHasNoValue(testFilePath, "one999Notdb", fldName, dbAZval);

		solrFldMapTest.assertSolrFldValue(testFilePath, "dbVideo", fldName, dbAZval);
		solrFldMapTest.assertSolrFldValue(testFilePath, "dbVideo", fldName, Format.VIDEO.toString());

		solrFldMapTest.assertSolrFldValue(testFilePath, "dbMusicRecording", fldName, dbAZval);
		solrFldMapTest.assertSolrFldValue(testFilePath, "dbMusicRecording", fldName, Format.MUSIC_RECORDING.toString());

		solrFldMapTest.assertSolrFldValue(testFilePath, "mult999oneDb", fldName, dbAZval);
		solrFldMapTest.assertSolrFldValue(testFilePath, "mult999oneDb", fldName, Format.JOURNAL_PERIODICAL.toString());

		solrFldMapTest.assertSolrFldValue(testFilePath, "two99oneShadowWasOther", fldName, dbAZval);
		solrFldMapTest.assertSolrFldHasNoValue(testFilePath, "two99oneShadowWasOther", fldName, otherVal);

		solrFldMapTest.assertSolrFldValue(testFilePath, "DBandMusicRecOne999", fldName, dbAZval);
		solrFldMapTest.assertSolrFldValue(testFilePath, "DBandMusicRecOne999", fldName, Format.MUSIC_RECORDING.toString());

		solrFldMapTest.assertSolrFldValue(testFilePath, "otherBecomesDB", fldName, dbAZval);
		solrFldMapTest.assertSolrFldHasNoValue(testFilePath, "otherBecomesDB", fldName, otherVal);

		solrFldMapTest.assertSolrFldValue(testFilePath, "dbOther", fldName, dbAZval);
		solrFldMapTest.assertSolrFldHasNoValue(testFilePath, "dbOther", fldName, otherVal);

		solrFldMapTest.assertSolrFldValue(testFilePath, "nother", fldName, dbAZval);
		solrFldMapTest.assertSolrFldHasNoValue(testFilePath, "nother", fldName, otherVal);

	}

	/**
	 * test the additional Database format values aren't assigned
	 */
@Test
	public final void testDatabaseAZOnly()
			throws IOException, SAXException, ParserConfigurationException, SolrServerException
	{
		createFreshIx("formatDatabaseTests.mrc");
		assertZeroResults(fldName, "\"Database (Other)\"");
		assertZeroResults(fldName, "\"Database (All)\"");
	}

}
