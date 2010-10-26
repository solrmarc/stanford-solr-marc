package edu.stanford;

import java.io.File;

import org.junit.Before;
import org.junit.Test;

import edu.stanford.enumValues.Format;

/**
 * junit4 tests for Stanford University format field for blacklight index
 * @author Naomi Dushay
 */
public class FormatDatabaseTests extends AbstractStanfordBlacklightTest {
	
	String testFilePath = testDataParentPath + File.separator + "formatDatabaseTests.mrc";
	String facetFldName = "format";

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
		solrFldMapTest.assertSolrFldValue(testFilePath, "one999db", facetFldName, Format.DATABASE_A_Z.toString());
		solrFldMapTest.assertSolrFldHasNoValue(testFilePath, "one999db", facetFldName, Format.OTHER.toString());
		
		solrFldMapTest.assertSolrFldValue(testFilePath, "one999Notdb", facetFldName, Format.OTHER.toString());
		solrFldMapTest.assertSolrFldHasNoValue(testFilePath, "one999Notdb", facetFldName, Format.DATABASE_A_Z.toString());

		solrFldMapTest.assertSolrFldValue(testFilePath, "dbVideo", facetFldName, Format.DATABASE_A_Z.toString());
		solrFldMapTest.assertSolrFldValue(testFilePath, "dbVideo", facetFldName, Format.VIDEO.toString()); 

		solrFldMapTest.assertSolrFldValue(testFilePath, "dbMusicRecording", facetFldName, Format.DATABASE_A_Z.toString());
		solrFldMapTest.assertSolrFldValue(testFilePath, "dbMusicRecording", facetFldName, Format.MUSIC_RECORDING.toString());

		solrFldMapTest.assertSolrFldValue(testFilePath, "mult999oneDb", facetFldName, Format.DATABASE_A_Z.toString());  
		solrFldMapTest.assertSolrFldValue(testFilePath, "mult999oneDb", facetFldName, Format.JOURNAL_PERIODICAL.toString());

		solrFldMapTest.assertSolrFldValue(testFilePath, "two99oneShadowWasOther", facetFldName, Format.DATABASE_A_Z.toString());
		solrFldMapTest.assertSolrFldHasNoValue(testFilePath, "two99oneShadowWasOther", facetFldName, Format.OTHER.toString());
		
		solrFldMapTest.assertSolrFldValue(testFilePath, "DBandMusicRecOne999", facetFldName, Format.DATABASE_A_Z.toString());
		solrFldMapTest.assertSolrFldValue(testFilePath, "DBandMusicRecOne999", facetFldName, Format.MUSIC_RECORDING.toString());

		solrFldMapTest.assertSolrFldValue(testFilePath, "otherBecomesDB", facetFldName, Format.DATABASE_A_Z.toString());
		solrFldMapTest.assertSolrFldHasNoValue(testFilePath, "otherBecomesDB", facetFldName, Format.OTHER.toString()); 

		solrFldMapTest.assertSolrFldValue(testFilePath, "dbOther", facetFldName, Format.DATABASE_A_Z.toString());
		solrFldMapTest.assertSolrFldHasNoValue(testFilePath, "dbOther", facetFldName, Format.OTHER.toString());

		solrFldMapTest.assertSolrFldValue(testFilePath, "nother", facetFldName, Format.DATABASE_A_Z.toString());
		solrFldMapTest.assertSolrFldHasNoValue(testFilePath, "nother", facetFldName, Format.OTHER.toString());
		
	}	


	/**
	 * test format value "Database (Other)" includes all appropriate cases
	 */
@Test
	public final void testDatabaseOther()
	{
		solrFldMapTest.assertSolrFldValue(testFilePath, "6xxv", facetFldName, Format.DATABASE_OTHER.toString());
		solrFldMapTest.assertSolrFldHasNoValue(testFilePath, "6xxv", facetFldName, Format.OTHER.toString());
		
		solrFldMapTest.assertSolrFldValue(testFilePath, "6xxx", facetFldName, Format.DATABASE_OTHER.toString());
		solrFldMapTest.assertSolrFldHasNoValue(testFilePath, "6xxx", facetFldName, Format.OTHER.toString());
		
		// record is a serial, and type is "updating database" (leader/07 = "s" or "i" and 008/21 = "d") OR (006/00 = "s" and 006/04 = "d")
		
		// leader/07 "i" and 008/21 "d"
		solrFldMapTest.assertSolrFldValue(testFilePath, "ldr07i_008-21d", facetFldName, Format.DATABASE_OTHER.toString());
		// leader/07 "s" and 008/21 "d"
		solrFldMapTest.assertSolrFldValue(testFilePath, "ldr07s_008-21d", facetFldName, Format.DATABASE_OTHER.toString());
		// leader/07 "s" and 008/21 not "d"
		solrFldMapTest.assertSolrFldHasNoValue(testFilePath, "ldr07s_008-21f", facetFldName, Format.DATABASE_OTHER.toString());
		// leader/07 "i" and 008/21 not "d"
		solrFldMapTest.assertSolrFldHasNoValue(testFilePath, "ldr07i_008-21c", facetFldName, Format.DATABASE_OTHER.toString());
		// 008/21 "d" and leader/07 not "i" or "s"
		solrFldMapTest.assertSolrFldHasNoValue(testFilePath, "ldr07b_008-21d", facetFldName, Format.DATABASE_OTHER.toString());
		

		// 006/00 "s" and 006/04 "d"
		solrFldMapTest.assertSolrFldValue(testFilePath, "006-00s-04d", facetFldName, Format.DATABASE_OTHER.toString());
		// 006/00 "s" and 006/04 not "d"
		solrFldMapTest.assertSolrFldHasNoValue(testFilePath, "006-00s-04blank", facetFldName, Format.DATABASE_OTHER.toString());
		// 006/04 not "s" and 006/00 "d"
		solrFldMapTest.assertSolrFldHasNoValue(testFilePath, "006-00m-04d", facetFldName, Format.DATABASE_OTHER.toString());
	}

	/**
	 * test format value "Database (All)" includes all Database A-Z and Database Other
	 */
@Test
	public final void testDatabaseAllIncludesAZ()
	{
		solrFldMapTest.assertSolrFldValue(testFilePath, "one999db", facetFldName, Format.DATABASE_ALL.toString());
		solrFldMapTest.assertSolrFldValue(testFilePath, "dbVideo", facetFldName, Format.DATABASE_ALL.toString());
		solrFldMapTest.assertSolrFldValue(testFilePath, "dbMusicRecording", facetFldName, Format.DATABASE_ALL.toString());
		solrFldMapTest.assertSolrFldValue(testFilePath, "mult999oneDb", facetFldName, Format.DATABASE_ALL.toString());  
		solrFldMapTest.assertSolrFldValue(testFilePath, "two99oneShadowWasOther", facetFldName, Format.DATABASE_ALL.toString());
		solrFldMapTest.assertSolrFldValue(testFilePath, "DBandMusicRecOne999", facetFldName, Format.DATABASE_ALL.toString());
		solrFldMapTest.assertSolrFldValue(testFilePath, "otherBecomesDB", facetFldName, Format.DATABASE_ALL.toString());
		solrFldMapTest.assertSolrFldValue(testFilePath, "dbOther", facetFldName, Format.DATABASE_ALL.toString());
		solrFldMapTest.assertSolrFldValue(testFilePath, "nother", facetFldName, Format.DATABASE_ALL.toString());
		
		
		solrFldMapTest.assertSolrFldValue(testFilePath, "6xxv", facetFldName, Format.DATABASE_ALL.toString());
		solrFldMapTest.assertSolrFldValue(testFilePath, "6xxx", facetFldName, Format.DATABASE_ALL.toString());
		
		solrFldMapTest.assertSolrFldValue(testFilePath, "ldr07i_008-21d", facetFldName, Format.DATABASE_ALL.toString());
		solrFldMapTest.assertSolrFldValue(testFilePath, "ldr07s_008-21d", facetFldName, Format.DATABASE_ALL.toString());
		solrFldMapTest.assertSolrFldValue(testFilePath, "006-00s-04d", facetFldName, Format.DATABASE_ALL.toString());
	}	
	
	
	}
