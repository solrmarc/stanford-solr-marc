package edu.stanford;

import org.junit.Before;
import org.junit.Test;
import org.marc4j.marc.*;

import edu.stanford.enumValues.Format;
import edu.stanford.enumValues.FormatPhysical;

/**
 * junit4 tests for Stanford University physical format field
 * Formats are tested separately in FormatTests
 * @author Naomi Dushay
 */
public class FormatPhysicalTests extends AbstractStanfordTest
{
	private final String formatFldName = "format_main_ssim";
	private final String physFormatFldName = "format_physical_ssim";
	private final MarcFactory factory = MarcFactory.newInstance();
	private ControlField cf007 = factory.newControlField("007");
	private ControlField cf008 = factory.newControlField("008");
	private DataField df999atLibrary = factory.newDataField("999", ' ', ' ');
	private DataField df999online = factory.newDataField("999", ' ', ' ');
	{
		df999online.addSubfield(factory.newSubfield('a', "INTERNET RESOURCE"));
		df999online.addSubfield(factory.newSubfield('w', "ASIS"));
		df999online.addSubfield(factory.newSubfield('i', "2475606-5001"));
		df999online.addSubfield(factory.newSubfield('l', "INTERNET"));
		df999online.addSubfield(factory.newSubfield('m', "SUL"));

		df999atLibrary.addSubfield(factory.newSubfield('a', "F152 .A28"));
		df999atLibrary.addSubfield(factory.newSubfield('w', "LC"));
		df999atLibrary.addSubfield(factory.newSubfield('i', "36105018746623"));
		df999atLibrary.addSubfield(factory.newSubfield('l', "HAS-DIGIT"));
		df999atLibrary.addSubfield(factory.newSubfield('m', "GREEN"));
	}

@Before
	public final void setup()
	{
		mappingTestInit();
	}

// images
//OTHER_IMAGE,

	/**
	 *  Spec (per Vitus 2013-11, email to gryph-search with Excel spreadsheet attachment):
	 *   (007/00 = g AND  007/01 = s)  OR  300a contains "slide"
	 */
@Test
	public final void testSlide()
	{
		String expVal = FormatPhysical.SLIDE.toString();
		Record record = factory.newRecord();
		record.setLeader(factory.newLeader("01291cgm a2200289 a 4500"));

		// 007/01 is not correct for Slide
		cf007.setData("gd|cu  jc");
		record.addVariableField(cf007);
		solrFldMapTest.assertSolrFldHasNumValues(record, formatFldName, 1);
		solrFldMapTest.assertSolrFldValue(record, formatFldName, Format.OTHER.toString());
		solrFldMapTest.assertNoSolrFld(record, physFormatFldName);

		// 007/00 is g, 007/01 is s
		record.removeVariableField(cf007);
		cf007.setData("gs|cu  jc");
		record.addVariableField(cf007);
		solrFldMapTest.assertSolrFldHasNumValues(record, formatFldName, 1);
		solrFldMapTest.assertSolrFldValue(record, formatFldName, Format.OTHER.toString());
		solrFldMapTest.assertSolrFldHasNumValues(record, physFormatFldName, 1);
		solrFldMapTest.assertSolrFldValue(record, physFormatFldName, expVal);

		// 300a contains slide
		record = factory.newRecord();
		record.setLeader(factory.newLeader("02709ckd a2200505Mi 4500"));
		DataField df300 = factory.newDataField("300", ' ', ' ');
		df300.addSubfield(factory.newSubfield('a', "1 pair of stereoscopic slides +"));
		df300.addSubfield(factory.newSubfield('e', "legend and diagram."));
		record.addVariableField(df300);
		solrFldMapTest.assertSolrFldHasNumValues(record, formatFldName, 1);
		solrFldMapTest.assertSolrFldValue(record, formatFldName, Format.OTHER.toString());
		solrFldMapTest.assertSolrFldHasNumValues(record, physFormatFldName, 1);
		solrFldMapTest.assertSolrFldValue(record, physFormatFldName, expVal);
	}

	/**
	 *  Spec (per Vitus 2013-11, email to gryph-search with Excel spreadsheet attachment):
	 *   (007/00 = k AND  007/01 = h)  OR  300a contains "photograph"
	 */
@Test
	public final void testPhoto()
	{
		String expVal = FormatPhysical.PHOTO.toString();
		Leader ldr = factory.newLeader("01427ckm a2200265 a 4500");
		Record record = factory.newRecord();
		record.setLeader(ldr);

		// 007/01 is not correct for Photo
		cf007.setData("kj boo");
		record.addVariableField(cf007);
		solrFldMapTest.assertSolrFldHasNumValues(record, formatFldName, 1);
		solrFldMapTest.assertSolrFldValue(record, formatFldName, Format.OTHER.toString());
		solrFldMapTest.assertNoSolrFld(record, physFormatFldName);

		// 007/00 is k, 007/01 is h
		record.removeVariableField(cf007);
		cf007.setData("kh boo");
		record.addVariableField(cf007);
		solrFldMapTest.assertSolrFldHasNumValues(record, formatFldName, 1);
		solrFldMapTest.assertSolrFldValue(record, formatFldName, Format.OTHER.toString());
		solrFldMapTest.assertSolrFldHasNumValues(record, physFormatFldName, 1);
		solrFldMapTest.assertSolrFldValue(record, physFormatFldName, expVal);

		// 300a contains photograph
		record = factory.newRecord();
		record.setLeader(ldr);
		DataField df300 = factory.newDataField("300", ' ', ' ');
		df300.addSubfield(factory.newSubfield('a', "1 photograph (1 leaf)."));
		record.addVariableField(df300);
		solrFldMapTest.assertSolrFldHasNumValues(record, formatFldName, 1);
		solrFldMapTest.assertSolrFldValue(record, formatFldName, Format.OTHER.toString());
		solrFldMapTest.assertSolrFldHasNumValues(record, physFormatFldName, 1);
		solrFldMapTest.assertSolrFldValue(record, physFormatFldName, expVal);
	}


	/**
	 *  Spec (per Vitus 2013-11, email to gryph-search with Excel spreadsheet attachment):
	 *   (007/00 = r)  OR  300a contains "remote-sensing image"
	 */
@Test
	public final void testRemoteSensingImage()
	{
		String expVal = FormatPhysical.REMOTE_SENSING_IMAGE.toString();
		Leader ldr = factory.newLeader("01103cem a22002777a 4500");
		Record record = factory.newRecord();
		record.setLeader(ldr);

		// 007/00 is not correct for Photo
		cf007.setData("kj boo");
		record.addVariableField(cf007);
		solrFldMapTest.assertSolrFldHasNumValues(record, formatFldName, 1);
		solrFldMapTest.assertSolrFldValue(record, formatFldName, Format.MAP_GLOBE.toString());
		solrFldMapTest.assertNoSolrFld(record, physFormatFldName);

		// 007/00 is r
		record.removeVariableField(cf007);
		cf007.setData("r  uuuuuuuu");
		record.addVariableField(cf007);
		solrFldMapTest.assertSolrFldHasNumValues(record, formatFldName, 1);
		solrFldMapTest.assertSolrFldValue(record, formatFldName, Format.MAP_GLOBE.toString());
		solrFldMapTest.assertSolrFldHasNumValues(record, physFormatFldName, 1);
		solrFldMapTest.assertSolrFldValue(record, physFormatFldName, expVal);

		// 300a contains remote sensing image  (no hyphen)
		record = factory.newRecord();
		record.setLeader(ldr);
		DataField df300 = factory.newDataField("300", ' ', ' ');
		df300.addSubfield(factory.newSubfield('a', "1 remote sensing image ;"));
		df300.addSubfield(factory.newSubfield('c', "18 x 20 cm."));
		record.addVariableField(df300);
		solrFldMapTest.assertSolrFldHasNumValues(record, formatFldName, 1);
		solrFldMapTest.assertSolrFldValue(record, formatFldName, Format.MAP_GLOBE.toString());
		solrFldMapTest.assertSolrFldHasNumValues(record, physFormatFldName, 1);
		solrFldMapTest.assertSolrFldValue(record, physFormatFldName, expVal);

		// 300a contains remote-sensing image  (with hyphen)
		record.removeVariableField(df300);
		df300 = factory.newDataField("300", ' ', ' ');
		df300.addSubfield(factory.newSubfield('a', "remote-sensing images; "));
		df300.addSubfield(factory.newSubfield('c', "on sheets 61 x 51 cm."));
		record.addVariableField(df300);
		solrFldMapTest.assertSolrFldHasNumValues(record, formatFldName, 1);
		solrFldMapTest.assertSolrFldValue(record, formatFldName, Format.MAP_GLOBE.toString());
		solrFldMapTest.assertSolrFldHasNumValues(record, physFormatFldName, 1);
		solrFldMapTest.assertSolrFldValue(record, physFormatFldName, expVal);
	}


// ---------------------------- Recordings -----------------------------------

//@Test
//	public final void testCylinderRecording()
//	{
//		String expVal = FormatPhysical.CYLINDER.toString();
//		Leader ldr = factory.newLeader("01102cjm a22002657a 4500");
//		Record record = factory.newRecord();
//		record.setLeader(ldr);
//		cf007.setData("ss lsnjlcmnnua");
//		record.addVariableField(cf007);
//		solrFldMapTest.assertSolrFldHasNumValues(record, formatFldName, 1);
//		solrFldMapTest.assertSolrFldValue(record, formatFldName, Format.MUSIC_RECORDING.toString());
//		solrFldMapTest.assertSolrFldValue(record, physFormatFldName, expVal);
//	}

@Test
	public final void testRecording78()
	{
		String expVal = FormatPhysical.SHELLAC_78.toString();
		Leader ldr = factory.newLeader("01002cjm a2200313Ma 4500");
		Record record = factory.newRecord();
		record.setLeader(ldr);
		cf007.setData("sd dmsdnnmslne");
		record.addVariableField(cf007);
		record.addVariableField(df999atLibrary);
		solrFldMapTest.assertSolrFldHasNumValues(record, formatFldName, 1);
		solrFldMapTest.assertSolrFldValue(record, formatFldName, Format.MUSIC_RECORDING.toString());
		solrFldMapTest.assertSolrFldValue(record, physFormatFldName, expVal);
		// only if at the library, not if online only
		record.removeVariableField(df999atLibrary);
		record.addVariableField(df999online);
		solrFldMapTest.assertSolrFldHasNumValues(record, physFormatFldName, 0);

//		// no 007, but 300   (based on 7845911)
//		record = factory.newRecord();
//		record.addVariableField(df999atLibrary);
//		DataField df300 = factory.newDataField("300", ' ', ' ');
//		df300.addSubfield(factory.newSubfield('a', "78 rpm"));
//		record.addVariableField(df300);
//		solrFldMapTest.assertSolrFldHasNumValues(record, physFormatFldName, 0);
//		solrFldMapTest.assertSolrFldHasNumValues(record, formatFldName, 1);
//		solrFldMapTest.assertSolrFldValue(record, formatFldName, Format.MUSIC_RECORDING.toString());
//		solrFldMapTest.assertSolrFldValue(record, physFormatFldName, expVal);
//		// only if at the library, not if online only
//		record.removeVariableField(df999atLibrary);
//		record.addVariableField(df999online);
	}


/*
	// recordings
	CD,
	VINYL,
	VINYL_45,
	SHELLAC_78,
	WAX_CYLINDER,
	INSTANTANEOUS_DISC,
	CASSETTE,
	CARTRIDGE_8_TRACK,
	DAT,
	REEL_TO_REEL,
	OTHER_RECORDING,


	// videos
	FILM,
	DVD,
	BLURAY,
	VHS,
	BETA,
	BETA_SP,
	MP4,
	OTHER_VIDEO,

	// maps
	ATLAS,
	GLOBE,
	OTHER_MAPS,

	OTHER;
*/

	// then do actual integration test -- send record all the way through indexing




	/**
	 *  Spec (per Vitus 2013-11, email to gryph-search with Excel spreadsheet attachment):
	 *   (007/00 = h AND  007/01 = b,c,d,h or j)  OR  300a contains "microfilm"
	 *    Naomi addition:  OR  if  callnum.startsWith("MFILM")
	 *    Question:  (what if 245h has "microform" -- see 9646614 for example)
	 */
@Test
	public final void testMicrofilm()
	{
		String expVal = FormatPhysical.MICROFILM.toString();
		Leader ldr = factory.newLeader("01543cam a2200325Ka 4500");
		Record record = factory.newRecord();
		record.setLeader(ldr);

		// 007/01 is not correct for Microfilm
		cf007.setData("ha afu   buca");
		record.addVariableField(cf007);
		solrFldMapTest.assertSolrFldHasNumValues(record, formatFldName, 1);
		solrFldMapTest.assertSolrFldValue(record, formatFldName, Format.BOOK.toString());
		solrFldMapTest.assertNoSolrFld(record, physFormatFldName);

		// 007/01 is b
		record.removeVariableField(cf007);
		cf007.setData("hb afu   buca");
		record.addVariableField(cf007);
		solrFldMapTest.assertSolrFldHasNumValues(record, formatFldName, 1);
		solrFldMapTest.assertSolrFldValue(record, formatFldName, Format.BOOK.toString());
		solrFldMapTest.assertSolrFldHasNumValues(record, physFormatFldName, 1);
		solrFldMapTest.assertSolrFldValue(record, physFormatFldName, expVal);

		// 007/01 is c
		record.removeVariableField(cf007);
		cf007.setData("hc afu   buca");
		record.addVariableField(cf007);
		solrFldMapTest.assertSolrFldHasNumValues(record, formatFldName, 1);
		solrFldMapTest.assertSolrFldValue(record, formatFldName, Format.BOOK.toString());
		solrFldMapTest.assertSolrFldHasNumValues(record, physFormatFldName, 1);
		solrFldMapTest.assertSolrFldValue(record, physFormatFldName, expVal);

		// 007/01 is d
		record.removeVariableField(cf007);
		cf007.setData("hd afu   buca");
		record.addVariableField(cf007);
		solrFldMapTest.assertSolrFldHasNumValues(record, formatFldName, 1);
		solrFldMapTest.assertSolrFldValue(record, formatFldName, Format.BOOK.toString());
		solrFldMapTest.assertSolrFldHasNumValues(record, physFormatFldName, 1);
		solrFldMapTest.assertSolrFldValue(record, physFormatFldName, expVal);

		// 007/01 is h
		record.removeVariableField(cf007);
		cf007.setData("hh afu   buca");
		record.addVariableField(cf007);
		solrFldMapTest.assertSolrFldHasNumValues(record, formatFldName, 1);
		solrFldMapTest.assertSolrFldValue(record, formatFldName, Format.BOOK.toString());
		solrFldMapTest.assertSolrFldHasNumValues(record, physFormatFldName, 1);
		solrFldMapTest.assertSolrFldValue(record, physFormatFldName, expVal);

		// 007/01 is j
		record.removeVariableField(cf007);
		cf007.setData("hj afu   buca");
		record.addVariableField(cf007);
		solrFldMapTest.assertSolrFldHasNumValues(record, formatFldName, 1);
		solrFldMapTest.assertSolrFldValue(record, formatFldName, Format.BOOK.toString());
		solrFldMapTest.assertSolrFldHasNumValues(record, physFormatFldName, 1);
		solrFldMapTest.assertSolrFldValue(record, physFormatFldName, expVal);

		// callnum in 999
		record = factory.newRecord();
		record.setLeader(ldr);
		DataField df999 = factory.newDataField("999", ' ', ' ');
		df999.addSubfield(factory.newSubfield('a', "MFILM N.S. 17443"));
		df999.addSubfield(factory.newSubfield('w', "ALPHANUM"));
		df999.addSubfield(factory.newSubfield('i', "9636901-1001"));
		df999.addSubfield(factory.newSubfield('l', "MEDIA-MTXT"));
		df999.addSubfield(factory.newSubfield('m', "GREEN"));
		df999.addSubfield(factory.newSubfield('t', "NH-MICR"));
		record.addVariableField(df999);
		solrFldMapTest.assertSolrFldHasNumValues(record, formatFldName, 1);
		solrFldMapTest.assertSolrFldValue(record, formatFldName, Format.BOOK.toString());
		solrFldMapTest.assertSolrFldHasNumValues(record, physFormatFldName, 1);
		solrFldMapTest.assertSolrFldValue(record, physFormatFldName, expVal);

		// 300
		record.removeVariableField(df999);
		DataField df300 = factory.newDataField("300", ' ', ' ');
		df300.addSubfield(factory.newSubfield('a', "21 microfilm reels ;"));
		df300.addSubfield(factory.newSubfield('c', "35 mm."));
		record.addVariableField(df300);
		solrFldMapTest.assertSolrFldHasNumValues(record, formatFldName, 1);
		solrFldMapTest.assertSolrFldValue(record, formatFldName, Format.BOOK.toString());
		solrFldMapTest.assertSolrFldHasNumValues(record, physFormatFldName, 1);
		solrFldMapTest.assertSolrFldValue(record, physFormatFldName, expVal);
	}


	/**
	 *  Spec (per Vitus 2013-11, email to gryph-search with Excel spreadsheet attachment):
	 *   (007/00 = h AND  007/01 = e,f or g)  OR  300a contains "microfiche"
	 *    Naomi addition:  OR  if  callnum.startsWith("MFICHE")
	 *    Question:  (what if 245h has "microform" -- see 9646614 for example)
	 */
@Test
	public final void testMicrofiche()
	{
		String expVal = FormatPhysical.MICROFICHE.toString();
		Leader ldr = factory.newLeader("01543cam a2200325Ka 4500");
		Record record = factory.newRecord();
		record.setLeader(ldr);

		// 007/01 is not correct for Microfilm
		cf007.setData("ha afu   buca");
		record.addVariableField(cf007);
		solrFldMapTest.assertSolrFldHasNumValues(record, formatFldName, 1);
		solrFldMapTest.assertSolrFldValue(record, formatFldName, Format.BOOK.toString());
		solrFldMapTest.assertNoSolrFld(record, physFormatFldName);

		// 007/01 is e
		record.removeVariableField(cf007);
		cf007.setData("he bmb024bbca");
		record.addVariableField(cf007);
		solrFldMapTest.assertSolrFldHasNumValues(record, formatFldName, 1);
		solrFldMapTest.assertSolrFldValue(record, formatFldName, Format.BOOK.toString());
		solrFldMapTest.assertSolrFldHasNumValues(record, physFormatFldName, 1);
		solrFldMapTest.assertSolrFldValue(record, physFormatFldName, expVal);

		// 007/01 is f
		record.removeVariableField(cf007);
		cf007.setData("hf bmb024bbca");
		record.addVariableField(cf007);
		solrFldMapTest.assertSolrFldHasNumValues(record, formatFldName, 1);
		solrFldMapTest.assertSolrFldValue(record, formatFldName, Format.BOOK.toString());
		solrFldMapTest.assertSolrFldHasNumValues(record, physFormatFldName, 1);
		solrFldMapTest.assertSolrFldValue(record, physFormatFldName, expVal);

		// 007/01 is g
		record.removeVariableField(cf007);
		cf007.setData("hg bmb024bbca");
		record.addVariableField(cf007);
		solrFldMapTest.assertSolrFldHasNumValues(record, formatFldName, 1);
		solrFldMapTest.assertSolrFldValue(record, formatFldName, Format.BOOK.toString());
		solrFldMapTest.assertSolrFldHasNumValues(record, physFormatFldName, 1);
		solrFldMapTest.assertSolrFldValue(record, physFormatFldName, expVal);

		// callnum in 999
		record = factory.newRecord();
		record.setLeader(ldr);
		DataField df999 = factory.newDataField("999", ' ', ' ');
		df999.addSubfield(factory.newSubfield('a', "MFICHE 1183 N.5.1.7205"));
		df999.addSubfield(factory.newSubfield('w', "ALPHANUM"));
		df999.addSubfield(factory.newSubfield('i', "9664812-1001"));
		df999.addSubfield(factory.newSubfield('l', "MEDIA-MTXT"));
		df999.addSubfield(factory.newSubfield('m', "GREEN"));
		df999.addSubfield(factory.newSubfield('t', "NH-MICR"));
		record.addVariableField(df999);
		solrFldMapTest.assertSolrFldHasNumValues(record, formatFldName, 1);
		solrFldMapTest.assertSolrFldValue(record, formatFldName, Format.BOOK.toString());
		solrFldMapTest.assertSolrFldHasNumValues(record, physFormatFldName, 1);
		solrFldMapTest.assertSolrFldValue(record, physFormatFldName, expVal);

		// 300
		record.removeVariableField(df999);
		DataField df300 = factory.newDataField("300", ' ', ' ');
		df300.addSubfield(factory.newSubfield('a', "microfiches :"));
		df300.addSubfield(factory.newSubfield('b', "ill. ;"));
		df300.addSubfield(factory.newSubfield('c', "11 x 15 cm."));
		record.addVariableField(df300);
		solrFldMapTest.assertSolrFldHasNumValues(record, formatFldName, 1);
		solrFldMapTest.assertSolrFldValue(record, formatFldName, Format.BOOK.toString());
		solrFldMapTest.assertSolrFldHasNumValues(record, physFormatFldName, 1);
		solrFldMapTest.assertSolrFldValue(record, physFormatFldName, expVal);
	}


}
