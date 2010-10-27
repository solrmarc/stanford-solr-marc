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
	String dbAZval = Format.DATABASE_A_Z.toString();
	String dbOtherVal = Format.DATABASE_OTHER.toString();
	String dbAllVal = Format.DATABASE_ALL.toString();
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
	 * test format value "Database (Other)" includes all appropriate cases
	 */
@Test
	public final void testDatabaseOther()
	{
		solrFldMapTest.assertSolrFldValue(testFilePath, "6xxv", facetFldName, dbOtherVal);
		solrFldMapTest.assertSolrFldHasNoValue(testFilePath, "6xxv", facetFldName, otherVal);
		
		solrFldMapTest.assertSolrFldValue(testFilePath, "6xxx", facetFldName, dbOtherVal);
		solrFldMapTest.assertSolrFldHasNoValue(testFilePath, "6xxx", facetFldName, otherVal);
		
		// record is a serial, and type is "updating database" (leader/07 = "s" or "i" and 008/21 = "d") OR (006/00 = "s" and 006/04 = "d")
		
		// leader/07 "i" and 008/21 "d"
		solrFldMapTest.assertSolrFldValue(testFilePath, "ldr07i_008-21d", facetFldName, dbOtherVal);
		// leader/07 "s" and 008/21 "d"
		solrFldMapTest.assertSolrFldValue(testFilePath, "ldr07s_008-21d", facetFldName, dbOtherVal);
		// leader/07 "s" and 008/21 not "d"
		solrFldMapTest.assertSolrFldHasNoValue(testFilePath, "ldr07s_008-21f", facetFldName, dbOtherVal);
		// leader/07 "i" and 008/21 not "d"
		solrFldMapTest.assertSolrFldHasNoValue(testFilePath, "ldr07i_008-21c", facetFldName, dbOtherVal);
		// 008/21 "d" and leader/07 not "i" or "s"
		solrFldMapTest.assertSolrFldHasNoValue(testFilePath, "ldr07b_008-21d", facetFldName, dbOtherVal);
		

		// 006/00 "s" and 006/04 "d"
		solrFldMapTest.assertSolrFldValue(testFilePath, "006-00s-04d", facetFldName, dbOtherVal);
		// 006/00 "s" and 006/04 not "d"
		solrFldMapTest.assertSolrFldHasNoValue(testFilePath, "006-00s-04blank", facetFldName, dbOtherVal);
		// 006/04 not "s" and 006/00 "d"
		solrFldMapTest.assertSolrFldHasNoValue(testFilePath, "006-00m-04d", facetFldName, dbOtherVal);
		
		
		String testFilePath = testDataParentPath + File.separator + "formatTests.mrc";
		// leader/07 s 008/21 d, 006/00 s 006/04 d
		solrFldMapTest.assertSolrFldValue(testFilePath, "112233", facetFldName, dbOtherVal);
		// leader/07 s 008/21 d, 006/00 j 006/04 p
		solrFldMapTest.assertSolrFldValue(testFilePath, "778899", facetFldName, dbOtherVal);
		solrFldMapTest.assertSolrFldValue(testFilePath, "leader07s00600j00821d", facetFldName, dbOtherVal);
		// 006/00 s  006/04 d
		solrFldMapTest.assertSolrFldValue(testFilePath, "321", facetFldName, dbOtherVal);
	}

	/**
	 * test format value "Database (All)" includes all Database A-Z and Database Other
	 */
@Test
	public final void testDatabaseAllIncludesAZ()
	{
		solrFldMapTest.assertSolrFldValue(testFilePath, "one999db", facetFldName, dbAllVal);
		solrFldMapTest.assertSolrFldValue(testFilePath, "dbVideo", facetFldName, dbAllVal);
		solrFldMapTest.assertSolrFldValue(testFilePath, "dbMusicRecording", facetFldName, dbAllVal);
		solrFldMapTest.assertSolrFldValue(testFilePath, "mult999oneDb", facetFldName, dbAllVal);  
		solrFldMapTest.assertSolrFldValue(testFilePath, "two99oneShadowWasOther", facetFldName, dbAllVal);
		solrFldMapTest.assertSolrFldValue(testFilePath, "DBandMusicRecOne999", facetFldName, dbAllVal);
		solrFldMapTest.assertSolrFldValue(testFilePath, "otherBecomesDB", facetFldName, dbAllVal);
		solrFldMapTest.assertSolrFldValue(testFilePath, "dbOther", facetFldName, dbAllVal);
		solrFldMapTest.assertSolrFldValue(testFilePath, "nother", facetFldName, dbAllVal);
		
		
		solrFldMapTest.assertSolrFldValue(testFilePath, "6xxv", facetFldName, dbAllVal);
		solrFldMapTest.assertSolrFldValue(testFilePath, "6xxx", facetFldName, dbAllVal);
		
		solrFldMapTest.assertSolrFldValue(testFilePath, "ldr07i_008-21d", facetFldName, dbAllVal);
		solrFldMapTest.assertSolrFldValue(testFilePath, "ldr07s_008-21d", facetFldName, dbAllVal);
		solrFldMapTest.assertSolrFldValue(testFilePath, "006-00s-04d", facetFldName, dbAllVal);
	}	
	
	
}
