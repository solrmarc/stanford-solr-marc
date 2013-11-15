package edu.stanford;

import java.io.*;

import org.junit.*;
import org.marc4j.marc.*;

import edu.stanford.enumValues.Format;


/**
 * junit4 tests for Stanford University format fields
 * Database formats are tested separately in FormatDatabaseTests
 * Physical formats are tested separated in FormatPhysicalTests
 * @author Naomi Dushay
 */
public class FormatTests extends AbstractStanfordTest
{
	private final String testDataFname = "formatTests.mrc";
	String testFilePath = testDataParentPath + File.separator + testDataFname;
	private final String fldName = "format";
	MarcFactory factory = MarcFactory.newInstance();

@Before
	public final void setup()
	{
		mappingTestInit();
	}


	/**
	 * Audio Non-Music format tests
	 */
@Test
	public final void testAudioNonMusic()
	{
		String fldVal = Format.SOUND_RECORDING.toString();
		solrFldMapTest.assertSolrFldValue(testFilePath, "leader06i", fldName, fldVal);
	}

	/**
	 * Book format tests
	 *   includes monographic series
	 */
@Test
	public final void testBookFormat()
	{
		String fldVal = Format.BOOK.toString();

		solrFldMapTest.assertSolrFldValue(testFilePath, "leader06a07m", fldName, fldVal);
		solrFldMapTest.assertSolrFldValue(testFilePath, "leader06t07a", fldName, fldVal);
		// monographic series
		solrFldMapTest.assertSolrFldValue(testFilePath, "leader07s00821m", fldName, fldVal);
		solrFldMapTest.assertSolrFldValue(testFilePath, "5987319", fldName, fldVal);
		solrFldMapTest.assertSolrFldValue(testFilePath, "5598989", fldName, fldVal);
		solrFldMapTest.assertSolrFldValue(testFilePath, "223344", fldName, fldVal);
		solrFldMapTest.assertSolrFldValue(testFilePath, "5666387", fldName, fldVal);
		solrFldMapTest.assertSolrFldValue(testFilePath, "666", fldName, fldVal);

		// formerly believed to be monographic series
		solrFldMapTest.assertSolrFldHasNoValue(testFilePath, "leader07b00600s00821m", fldName, fldVal);
	}

	/**
	 * Computer File format tests
	 */
@Test
	public final void testComputerFile()
	{
		String fldVal = Format.COMPUTER_FILE.toString();
		solrFldMapTest.assertSolrFldValue(testFilePath, "leader06m00826u", fldName, fldVal);

		Leader LEADER = factory.newLeader("01529cmi a2200397Ia 4500");
		ControlField cf008 = factory.newControlField("008");
		Record record = factory.newRecord();
		record.setLeader(LEADER);
		cf008.setData("081215c200u9999xx         b        eng d");
		record.addVariableField(cf008);
		solrFldMapTest.assertSolrFldValue(record, fldName, fldVal);
	}

	/**
	 * Computer File and Database -- if both formats, and it is only an
	 *  online resource, then it is NOT a computer file.
	 */
@Test
	public final void testComputerFileAndDatabase()
	{
		Leader LEADER = factory.newLeader("02441cms a2200517 a 4500");
		ControlField cf008 = factory.newControlField("008");
		cf008.setData("920901d19912002pauuu1n    m  0   a0eng  ");

		Record record = factory.newRecord();
		record.setLeader(LEADER);
		record.addVariableField(cf008);
		// online copy only
		DataField df999online = factory.newDataField("999", ' ', ' ');
		df999online.addSubfield(factory.newSubfield('a', "INTERNET RESOURCE"));
		df999online.addSubfield(factory.newSubfield('w', "ASIS"));
		df999online.addSubfield(factory.newSubfield('i', "2475606-5001"));
		df999online.addSubfield(factory.newSubfield('l', "INTERNET"));
		df999online.addSubfield(factory.newSubfield('m', "SUL"));
		df999online.addSubfield(factory.newSubfield('t', "DATABASE"));
		record.addVariableField(df999online);
		solrFldMapTest.assertSolrFldHasNumValues(record, fldName, 1);
		solrFldMapTest.assertSolrFldValue(record, fldName, Format.DATABASE_A_Z.toString());

		// both physical copy and online copy
		DataField df999physical = factory.newDataField("999", ' ', ' ');
		df999physical = factory.newDataField("999", ' ', ' ');
		df999physical.addSubfield(factory.newSubfield('a', "F152 .A28"));
		df999physical.addSubfield(factory.newSubfield('w', "LC"));
		df999physical.addSubfield(factory.newSubfield('i', "36105018746623"));
		df999physical.addSubfield(factory.newSubfield('l', "HAS-DIGIT"));
		df999physical.addSubfield(factory.newSubfield('m', "GREEN"));
		record.addVariableField(df999physical);
		solrFldMapTest.assertSolrFldHasNumValues(record, fldName, 2);
		solrFldMapTest.assertSolrFldValue(record, fldName, Format.COMPUTER_FILE.toString());
		solrFldMapTest.assertSolrFldValue(record, fldName, Format.DATABASE_A_Z.toString());

		// can't have physical copy only or it wouldn't be a database
	}


	/**
	 * Conference Proceedings format tests
	 */
@Test
	public final void testConferenceProceedings()
	{
	    String fldVal = Format.CONFERENCE_PROCEEDINGS.toString();
		solrFldMapTest.assertSolrFldValue(testFilePath, "5666387", fldName, fldVal);
		solrFldMapTest.assertSolrFldValue(testFilePath, "666", fldName, fldVal);
	}

	/**
	 * Image format tests
	 */
@Test
	public final void testImage()
	{
		String fldVal = Format.IMAGE.toString();
		solrFldMapTest.assertSolrFldValue(testFilePath, "leader06k00833i", fldName, fldVal);
		solrFldMapTest.assertSolrFldValue(testFilePath, "leader06k00833k", fldName, fldVal);
		solrFldMapTest.assertSolrFldValue(testFilePath, "leader06k00833p", fldName, fldVal);
		solrFldMapTest.assertSolrFldValue(testFilePath, "leader06k00833s", fldName, fldVal);
		solrFldMapTest.assertSolrFldValue(testFilePath, "leader06k00833t", fldName, fldVal);
	}

	/**
	 * Journal/Periodical format tests
	 */
@Test
	public final void testJournalPeriodicalFormat()
	{
        String fldVal = "Journal/Periodical";

     	// leader/07 s 008/21 blank
		solrFldMapTest.assertSolrFldValue(testFilePath, "leader06a07s", fldName, fldVal);
		solrFldMapTest.assertSolrFldValue(testFilePath, "4114632", fldName, fldVal);
		solrFldMapTest.assertSolrFldValue(testFilePath, "123", fldName, fldVal);
		// 006/00 s /04 blank
		solrFldMapTest.assertSolrFldValue(testFilePath, "leader07b00600s00821m", fldName, fldVal);
		// 006/00 s /04 blank
		solrFldMapTest.assertSolrFldValue(testFilePath, "leader07b00600s00821p", fldName, fldVal);
		// even though LCPER in 999 w
		solrFldMapTest.assertSolrFldValue(testFilePath, "460947", fldName, fldVal);
		// even though DEWEYPER in 999 w
		solrFldMapTest.assertSolrFldValue(testFilePath, "446688", fldName, fldVal);

		solrFldMapTest.assertSolrFldValue(testFilePath, "leader07sNo00600821p", fldName, fldVal);
		solrFldMapTest.assertSolrFldValue(testFilePath, "335577", fldName, fldVal);

		// leader/07s 008/21 d   006/00 s  006/04 d -- other
		solrFldMapTest.assertSolrFldHasNoValue(testFilePath, "112233", fldName, fldVal);
	}

	/**
	 * Manuscript/Archive format tests
	 */
@Test
	public final void testManuscriptArchive()
	{
		String fldVal = Format.MANUSCRIPT_ARCHIVE.toString();
		solrFldMapTest.assertSolrFldValue(testFilePath, "leader06b", fldName, fldVal);
		solrFldMapTest.assertSolrFldValue(testFilePath, "leader06p", fldName, fldVal);
	}

	/**
	 * Map/Globe format tests
	 */
@Test
	public final void testMapGlobe()
	{
		String fldVal = Format.MAP_GLOBE.toString();
		solrFldMapTest.assertSolrFldValue(testFilePath, "leader06e", fldName, fldVal);
		solrFldMapTest.assertSolrFldValue(testFilePath, "leader06f", fldName, fldVal);
	}

	/**
	 * Microformat format tests
	 */
@Test
	public final void testMicroformatIsGone()
	{
		String fldVal = Format.MICROFORMAT.toString();
		solrFldMapTest.assertSolrFldHasNoValue(testFilePath, "245hmicroform", fldName, fldVal);
	}

	/**
	 * Music Recording format tests
	 */
@Test
	public final void testMusicRecording()
	{
		String fldVal = Format.MUSIC_RECORDING.toString();
		solrFldMapTest.assertSolrFldValue(testFilePath, "leader06j", fldName, fldVal);
	}

	/**
	 * Music Score format tests
	 */
@Test
	public final void testMusicScore()
	{
		String fldVal = Format.MUSIC_SCORE.toString();
		solrFldMapTest.assertSolrFldValue(testFilePath, "leader06c", fldName, fldVal);
		solrFldMapTest.assertSolrFldValue(testFilePath, "leader06d", fldName, fldVal);
		solrFldMapTest.assertSolrFldValue(testFilePath, "245hmicroform", fldName, fldVal);
	}

	/**
	 * Newspaper format tests
	 */
@Test
	public final void testNewspaper()
	{
        String fldVal = Format.NEWSPAPER.toString();

		solrFldMapTest.assertSolrFldValue(testFilePath, "newspaper", fldName, fldVal);
		solrFldMapTest.assertSolrFldValue(testFilePath, "leader07sNo00600821n", fldName, fldVal);
		solrFldMapTest.assertSolrFldValue(testFilePath, "334455", fldName, fldVal);

		// leader/07b 006/00s 008/21n - serial publication
		solrFldMapTest.assertSolrFldHasNoValue(testFilePath, "leader07b00600s00821n", fldName, fldVal);
	}

	/**
	 * Thesis format tests
	 */
@Test
	public final void testThesis()
	{
		String fldVal = Format.THESIS.toString();
		solrFldMapTest.assertSolrFldValue(testFilePath, "502", fldName, fldVal);
	}

	/**
	 * Video format tests
	 */
@Test
	public final void testVideo()
	{
		String fldVal = Format.VIDEO.toString();
		solrFldMapTest.assertSolrFldValue(testFilePath, "leader06g00833m", fldName, fldVal);
		solrFldMapTest.assertSolrFldValue(testFilePath, "leader06g00833v", fldName, fldVal);
	}

	/**
	 * Test assignment of Other format
	 */
@Test
	public final void testOtherFormat()
	{
        String fldVal = Format.OTHER.toString();

		solrFldMapTest.assertSolrFldValue(testFilePath, "leader06t07b", fldName, fldVal);
		solrFldMapTest.assertSolrFldValue(testFilePath, "leader06k00833w", fldName, fldVal);
		solrFldMapTest.assertSolrFldValue(testFilePath, "leader06g00833w", fldName, fldVal);
		// 006/00 s /04 w
		solrFldMapTest.assertSolrFldValue(testFilePath, "leader07b00600s00821n", fldName, fldVal);
		// instructional kit
		solrFldMapTest.assertSolrFldValue(testFilePath, "leader06o", fldName, fldVal);
		// object
		solrFldMapTest.assertSolrFldValue(testFilePath, "leader06r", fldName, fldVal);
		// web site
		solrFldMapTest.assertSolrFldValue(testFilePath, "leader07sNo00600821w", fldName, fldVal);
		solrFldMapTest.assertSolrFldValue(testFilePath, "leader07b00600s00821w", fldName, fldVal);
		// leader/07 s, 006/00 m, 008/21 |
		solrFldMapTest.assertSolrFldValue(testFilePath, "7117119", fldName, fldVal);

		// as of 2010-10-03 008/21 d   means database if nothing else is assigned.
		//   See FormatDatabaseTests
		// leader/07 s 008/21 d, 006/00 s 006/04 d
//		solrFldMapTest.assertSolrFldValue(testFilePath, "112233", fldName, fldVal);
		// leader/07 s 008/21 d, 006/00 j 006/04 p
//		solrFldMapTest.assertSolrFldValue(testFilePath, "778899", fldName, fldVal);
//		solrFldMapTest.assertSolrFldValue(testFilePath, "leader07s00600j00821d", fldName, fldVal);
		// 006/00 s  006/04 d
//		solrFldMapTest.assertSolrFldValue(testFilePath, "321", fldName, fldVal);
	}


	/**
	 * test format population based on ALPHANUM field values from 999
	 */
@Test
	public final void testFormatsFrom999()
	{
		String testFilePath = testDataParentPath + File.separator + "callNumberTests.mrc";

		String microVal = Format.MICROFORMAT.toString();
		// 999 ALPHANUM starting with MFLIM
		solrFldMapTest.assertSolrFldHasNoValue(testFilePath, "1261173", fldName, microVal);
		// 999 ALPHANUM starting with MFICHE
		solrFldMapTest.assertSolrFldHasNoValue(testFilePath, "mfiche", fldName, microVal);

		// 999 ALPHANUM starting with MCD
		solrFldMapTest.assertSolrFldValue(testFilePath, "1234673", fldName, Format.MUSIC_RECORDING.toString());
	}


	/**
	 * test format population based on Marcit source per 590
	 */
@Test
	public final void testMarcit()
	{
		Leader LEADER = factory.newLeader("00838cas a2200193z  4500");

		Record record = factory.newRecord();
		record.setLeader(LEADER);
		DataField df = factory.newDataField("590", ' ', ' ');
        df.addSubfield(factory.newSubfield('a', "MARCit brief record."));
        record.addVariableField(df);
		solrFldMapTest.assertSolrFldValue(record, fldName, Format.MARCIT.toString());
		// without period
		record = factory.newRecord();
		record.setLeader(LEADER);
		df = factory.newDataField("590", ' ', ' ');
        df.addSubfield(factory.newSubfield('a', "MARCit brief record"));
        record.addVariableField(df);
		solrFldMapTest.assertSolrFldValue(record, fldName, Format.MARCIT.toString());

		// wrong string in 590
		record = factory.newRecord();
		record.setLeader(LEADER);
		df = factory.newDataField("590", ' ', ' ');
		df.addSubfield(factory.newSubfield('a', "incorrect string"));
        record.addVariableField(df);
		solrFldMapTest.assertSolrFldValue(record, fldName, Format.OTHER.toString());
		record = factory.newRecord();
		record.setLeader(LEADER);
		df = factory.newDataField("590", ' ', ' ');
		df.addSubfield(factory.newSubfield('a', "something MARCit something"));
        record.addVariableField(df);
		solrFldMapTest.assertSolrFldValue(record, fldName, Format.OTHER.toString());

		// marcit in wrong field
		record = factory.newRecord();
		record.setLeader(LEADER);
		df = factory.newDataField("580", ' ', ' ');
        df.addSubfield(factory.newSubfield('a', "MARCit brief record."));
        record.addVariableField(df);
		solrFldMapTest.assertSolrFldValue(record, fldName, Format.OTHER.toString());
	}


	/**
	 * test format population of Datasets
	 */
@Test
	public final void testDataset()
	{
		String fldVal = Format.DATASET.toString();

		solrFldMapTest.assertSolrFldValue(testFilePath, "leader06m00826a", fldName, fldVal);

		Leader LEADER = factory.newLeader("01529cmi a2200397Ia 4500");
		ControlField cf008 = factory.newControlField("008");
		Record record = factory.newRecord();
		record.setLeader(LEADER);
		cf008.setData("081215c200u9999xx         a        eng d");
		record.addVariableField(cf008);
		solrFldMapTest.assertSolrFldValue(record, fldName, fldVal);
	}

}
