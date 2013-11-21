package edu.stanford;

import org.junit.Before;
import org.junit.Test;
import org.marc4j.marc.*;

import edu.stanford.enumValues.Format;
import edu.stanford.enumValues.Genre;

/**
 * junit4 tests for Stanford University "genre" field
 * @author Naomi Dushay
 */
public class GenreTests extends AbstractStanfordTest
{
	private final String formatFldName = "format_main_ssim";
	private final String genreFldName = "genre_ssim";
	private final MarcFactory factory = MarcFactory.newInstance();
	private ControlField cf008 = factory.newControlField("008");

@Before
	public final void setup()
	{
		mappingTestInit();
	}

	/**
	 * Conference Proceedings value for a variety of main formats
	 */
@Test
	public final void testConferenceProceedings()
	{
	    String fldVal = Genre.CONFERENCE_PROCEEDINGS.toString();
		Record record = factory.newRecord();
		DataField df650 = factory.newDataField("650", ' ', '0');
		df650.addSubfield(factory.newSubfield('a', "subject"));
		df650.addSubfield(factory.newSubfield('v', "Congresses"));

		// Book
		record.setLeader(factory.newLeader("15069nam a2200409 a 4500"));
		cf008.setData("091123s2014    si a    sb    101 0 eng d");
		record.addVariableField(cf008);
		record.addVariableField(df650);
		solrFldMapTest.assertSolrFldValue(record, formatFldName, Format.BOOK.toString());
		solrFldMapTest.assertSolrFldValue(record, genreFldName, fldVal);

		// Computer File
		record = factory.newRecord();
		record.setLeader(factory.newLeader("03779cmm a2200505 i 4500"));
		cf008.setData("131010t20132013cau        m        eng c");
		record.addVariableField(cf008);
		record.addVariableField(df650);
		solrFldMapTest.assertSolrFldValue(record, formatFldName, Format.COMPUTER_FILE.toString());
		solrFldMapTest.assertSolrFldValue(record, genreFldName, fldVal);

		// Database
		record = factory.newRecord();
		record.setLeader(factory.newLeader("01515cas a2200385Ma 4500"));
		cf008.setData("000208c199u9999nyu x   s     0    0eng d");
		record.addVariableField(cf008);
		record.addVariableField(df650);
		DataField df = factory.newDataField("999", ' ', ' ');
		df.addSubfield(factory.newSubfield('t', "DATABASE"));
		record.addVariableField(df);
		solrFldMapTest.assertSolrFldValue(record, formatFldName, Format.DATABASE_A_Z.toString());
		solrFldMapTest.assertSolrFldValue(record, genreFldName, fldVal);

		// Manuscript
		record = factory.newRecord();
		record.setLeader(factory.newLeader("01443cpcaa2200289   4500"));
		cf008.setData("840706i18701943cau                 ger d");
		record.addVariableField(cf008);
		record.addVariableField(df650);
		solrFldMapTest.assertSolrFldValue(record, formatFldName, Format.MANUSCRIPT_ARCHIVE.toString());
		solrFldMapTest.assertSolrFldValue(record, genreFldName, fldVal);

		// Newspaper
		record = factory.newRecord();
		record.setLeader(factory.newLeader("01102cas a2200277   4500"));
		cf008.setData("870604d19191919njudr ne      1    0eng d");
		record.addVariableField(cf008);
		record.addVariableField(df650);
		solrFldMapTest.assertSolrFldValue(record, formatFldName, Format.NEWSPAPER.toString());
		solrFldMapTest.assertSolrFldValue(record, genreFldName, fldVal);

		// Other
		record = factory.newRecord();
		record.setLeader(factory.newLeader("01482com a2200337 a 4500"));
		cf008.setData("840726s1980    dcu---        1   bneng d");
		record.addVariableField(cf008);
		record.addVariableField(df650);
		solrFldMapTest.assertSolrFldValue(record, formatFldName, Format.OTHER.toString());
		solrFldMapTest.assertSolrFldValue(record, genreFldName, fldVal);

		// Sound Recording
		record = factory.newRecord();
		record.setLeader(factory.newLeader("03701cim a2200421 a 4500"));
		cf008.setData("040802c200u9999cau            l    eng d");
		record.addVariableField(cf008);
		record.addVariableField(df650);
		solrFldMapTest.assertSolrFldValue(record, formatFldName, Format.SOUND_RECORDING.toString());
		solrFldMapTest.assertSolrFldValue(record, genreFldName, fldVal);

		// Video
		record = factory.newRecord();
		record.setLeader(factory.newLeader("02840cgm a2200481 i 4500"));
		cf008.setData("110805t20112011cau074            vleng c");
		record.addVariableField(cf008);
		record.addVariableField(df650);
		solrFldMapTest.assertSolrFldValue(record, formatFldName, Format.VIDEO.toString());
		solrFldMapTest.assertSolrFldValue(record, genreFldName, fldVal);
		record = factory.newRecord();
		record.setLeader(factory.newLeader("02958cgm a2200469Ki 4500"));
		cf008.setData("110504s2011    cau418            vleng d");
		record.addVariableField(cf008);
		record.addVariableField(df650);
		solrFldMapTest.assertSolrFldValue(record, formatFldName, Format.VIDEO.toString());
		solrFldMapTest.assertSolrFldValue(record, genreFldName, fldVal);
	}


    /**
	 * Conference Proceedings format tests
	 */
@Test
	public final void testConferenceProceedingsAsJournal()
	{
	    String fldVal = Genre.CONFERENCE_PROCEEDINGS.toString();

//		solrFldMapTest.assertSolrFldValue(testFilePath, "5666387", fldName, fldVal);
//		solrFldMapTest.assertSolrFldValue(testFilePath, "666", fldName, fldVal);


		// main format Journal (?)
		Record record = factory.newRecord();

		// 008 byte 21 is p  (Journal / periodical)
		record.setLeader(factory.newLeader("02808cas a22005778a 4500"));
		cf008.setData("050127c20149999enkfr p       |   a0eng c");
		record.addVariableField(cf008);
		solrFldMapTest.assertSolrFldValue(record, formatFldName, Format.JOURNAL_PERIODICAL.toString());
		solrFldMapTest.assertSolrFldValue(record, genreFldName, fldVal);

		// 008 byte 21 is blank
		record = factory.newRecord();
		record.setLeader(factory.newLeader("02393cas a2200421Ki 4500"));
		cf008.setData("130923c20139999un uu         1    0ukr d");
		record.addVariableField(cf008);
		solrFldMapTest.assertSolrFldValue(record, formatFldName, Format.JOURNAL_PERIODICAL.toString());
		solrFldMapTest.assertSolrFldValue(record, genreFldName, fldVal);

		// 008 byte 21 is | (pipe)  Journal
		record = factory.newRecord();
		record.setLeader(factory.newLeader("00756nas a22002175a 4500"));
		cf008.setData("110417s2011    le |||||||||||||| ||ara d");
		record.addVariableField(cf008);
		solrFldMapTest.assertSolrFldValue(record, formatFldName, Format.JOURNAL_PERIODICAL.toString());
		solrFldMapTest.assertSolrFldValue(record, genreFldName, fldVal);

		ControlField cf006 = factory.newControlField("006");

		// recording and conf proceedings
		record = factory.newRecord();
		record.setLeader(factory.newLeader("03701cim a2200421 a 4500"));
		cf006.setData("sar         1    0");
		record.addVariableField(cf006);
		cf008.setData("040802c200u9999cau            l    eng d");
		record.addVariableField(cf008);
		solrFldMapTest.assertSolrFldValue(record, formatFldName, Format.JOURNAL_PERIODICAL.toString());
		solrFldMapTest.assertSolrFldValue(record, genreFldName, fldVal);


		// // look for serial publications in 006/00 and 006/04


		// main format Newspaper
		// 008 byte 21 is n

		// main format Book
		// 008 byte 21 is m

		// main format Video

		// main format Computer File

		// main format Sound Recording (?)

		// ...


	}


	/**
	 * Thesis value for a variety of main formats
	 */
@Test
	public final void testThesis()
	{
	    String fldVal = Genre.THESIS.toString();
		Record record = factory.newRecord();
		DataField df502 = factory.newDataField("502", ' ', ' ');
		df502.addSubfield(factory.newSubfield('a', "I exist"));

		// Book
		record.setLeader(factory.newLeader("15069nam a2200409 a 4500"));
		cf008.setData("091123s2014    si a    sb    101 0 eng d");
		record.addVariableField(cf008);
		record.addVariableField(df502);
		solrFldMapTest.assertSolrFldValue(record, formatFldName, Format.BOOK.toString());
		solrFldMapTest.assertSolrFldValue(record, genreFldName, fldVal);

		// Map/Globe
		// based on 4673069
		record = factory.newRecord();
		record.setLeader(factory.newLeader("01168cem a22002777  4500"));
		cf008.setData("020417s1981    caua, g  b    000 0 eng u");
		record.addVariableField(cf008);
		record.addVariableField(df502);
		solrFldMapTest.assertSolrFldValue(record, formatFldName, Format.MAP_GLOBE.toString());
		solrFldMapTest.assertSolrFldValue(record, genreFldName, fldVal);

		// Manuscript
		// based on 4822393
		record = factory.newRecord();
		record.setLeader(factory.newLeader("01038npcaa2200265   4500"));
		cf008.setData("020812s2002    cau                 eng d");
		record.addVariableField(cf008);
		record.addVariableField(df502);
		solrFldMapTest.assertSolrFldValue(record, formatFldName, Format.MANUSCRIPT_ARCHIVE.toString());
		solrFldMapTest.assertSolrFldValue(record, genreFldName, fldVal);

		// Music Recording
		// based on 297799
		record = factory.newRecord();
		record.setLeader(factory.newLeader("00979cjm a2200265   4500"));
		cf008.setData("790807s1979    xx zzz                  d");
		record.addVariableField(cf008);
		record.addVariableField(df502);
		solrFldMapTest.assertSolrFldValue(record, formatFldName, Format.MUSIC_RECORDING.toString());
		solrFldMapTest.assertSolrFldValue(record, genreFldName, fldVal);

		// Music Score
		// based on 7620611
		record = factory.newRecord();
		record.setLeader(factory.newLeader("01350cdm a2200337La 4500"));
		cf008.setData("010712r20082000xxumsa  rbehi  n    lat d");
		record.addVariableField(cf008);
		record.addVariableField(df502);
		solrFldMapTest.assertSolrFldValue(record, formatFldName, Format.MUSIC_SCORE.toString());
		solrFldMapTest.assertSolrFldValue(record, genreFldName, fldVal);

		// Other
		// based on 10208984; likely a mistake in main format
		record = factory.newRecord();
		record.setLeader(factory.newLeader("01165crm a2200313Ia 4500"));
		cf008.setData("840712r1983    xx a          0   0neng d");
		record.addVariableField(cf008);
		record.addVariableField(df502);
		solrFldMapTest.assertSolrFldValue(record, formatFldName, Format.OTHER.toString());
		solrFldMapTest.assertSolrFldValue(record, genreFldName, fldVal);

		// Video
		// based on 10169038
		record = factory.newRecord();
		record.setLeader(factory.newLeader("02245cgm a2200409Ia 4500"));
		cf008.setData("130215s2012    nyu050            vleng d");
		record.addVariableField(cf008);
		record.addVariableField(df502);
		solrFldMapTest.assertSolrFldValue(record, formatFldName, Format.VIDEO.toString());
		solrFldMapTest.assertSolrFldValue(record, genreFldName, fldVal);
	}

	/**
	 * something is marked as both a proceedings and a thesis
	 */
@Test
	public final void ThesisAndProceedings()
	{
    	String thesisFldVal = Genre.THESIS.toString();
	    String procFldVal = Genre.CONFERENCE_PROCEEDINGS.toString();
	    // based on 3743956
		Record record = factory.newRecord();
		record = factory.newRecord();
		record.setLeader(factory.newLeader("01360cam a22003011  4500"));
		cf008.setData("890928s1929    mdu           000 0 eng c");
		record.addVariableField(cf008);
		DataField df = factory.newDataField("650", ' ', '0');
		df.addSubfield(factory.newSubfield('a', "subject"));
		df.addSubfield(factory.newSubfield('v', "Congresses"));
		record.addVariableField(df);
		df = factory.newDataField("502", ' ', ' ');
		df.addSubfield(factory.newSubfield('a', "Thesis (Ph. D.)--Johns Hopkins, 1928."));
		record.addVariableField(df);
		solrFldMapTest.assertSolrFldValue(record, formatFldName, Format.BOOK.toString());
		solrFldMapTest.assertSolrFldValue(record, genreFldName, thesisFldVal);
		solrFldMapTest.assertSolrFldValue(record, genreFldName, procFldVal);
	}



}
