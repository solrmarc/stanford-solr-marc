package edu.stanford;

import org.junit.*;
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
	public void testSlide()
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
//		solrFldMapTest.assertSolrFldValue(record, physFormatFldName, expVal);
		solrFldMapTest.assertSolrFldValue(record, physFormatFldName, expVal + " from 300");
	}

	/**
	 *  Spec (per Vitus 2013-11, email to gryph-search with Excel spreadsheet attachment):
	 *   (007/00 = k AND  007/01 = h)  OR  300a contains "photograph"
	 */
@Test
	public void testPhoto()
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
		solrFldMapTest.assertSolrFldValue(record, physFormatFldName, expVal + " from 300");
	}

	/**
	 *  Spec (per Vitus 2013-11, email to gryph-search with Excel spreadsheet attachment):
	 *   (007/00 = r)  OR  300a contains "remote-sensing image"
	 */
@Test
	public void testRemoteSensingImage()
	{
		String expVal = FormatPhysical.REMOTE_SENSING_IMAGE.toString();
		Leader ldr = factory.newLeader("01103cem a22002777a 4500");
		Record record = factory.newRecord();
		record.setLeader(ldr);

		// 007/00 is not correct for Photo
		cf007.setData("kj boo");
		record.addVariableField(cf007);
		solrFldMapTest.assertSolrFldHasNumValues(record, formatFldName, 1);
		solrFldMapTest.assertSolrFldValue(record, formatFldName, Format.MAP.toString());
		solrFldMapTest.assertNoSolrFld(record, physFormatFldName);

		// 007/00 is r
		record.removeVariableField(cf007);
		cf007.setData("r  uuuuuuuu");
		record.addVariableField(cf007);
		solrFldMapTest.assertSolrFldHasNumValues(record, formatFldName, 1);
		solrFldMapTest.assertSolrFldValue(record, formatFldName, Format.MAP.toString());
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
		solrFldMapTest.assertSolrFldValue(record, formatFldName, Format.MAP.toString());
		solrFldMapTest.assertSolrFldHasNumValues(record, physFormatFldName, 1);
//		solrFldMapTest.assertSolrFldValue(record, physFormatFldName, expVal);
		solrFldMapTest.assertSolrFldValue(record, physFormatFldName, expVal + " from 300");

		// 300a contains remote-sensing image  (with hyphen)
		record.removeVariableField(df300);
		df300 = factory.newDataField("300", ' ', ' ');
		df300.addSubfield(factory.newSubfield('a', "remote-sensing images; "));
		df300.addSubfield(factory.newSubfield('c', "on sheets 61 x 51 cm."));
		record.addVariableField(df300);
		solrFldMapTest.assertSolrFldHasNumValues(record, formatFldName, 1);
		solrFldMapTest.assertSolrFldValue(record, formatFldName, Format.MAP.toString());
		solrFldMapTest.assertSolrFldHasNumValues(record, physFormatFldName, 1);
//		solrFldMapTest.assertSolrFldValue(record, physFormatFldName, expVal);
		solrFldMapTest.assertSolrFldValue(record, physFormatFldName, expVal + " from 300");
	}


// ---------------------------- Recordings -----------------------------------

@Test
	public void testRecordingCD()
	{
		String expVal = FormatPhysical.CD.toString();
		// based on 8833535
		Leader ldr = factory.newLeader("02229cjm a2200409Ia 4500");
		Record record = factory.newRecord();
		record.setLeader(ldr);
		cf007.setData("sd fungnnmmneu");
		record.addVariableField(cf007);
		record.addVariableField(df999atLibrary);
		solrFldMapTest.assertSolrFldValue(record, formatFldName, Format.MUSIC_RECORDING.toString());
		solrFldMapTest.assertSolrFldHasNumValues(record, physFormatFldName, 1);
		solrFldMapTest.assertSolrFldValue(record, physFormatFldName, expVal);
		// not if online only
		record.removeVariableField(df999atLibrary);
		record.addVariableField(df999online);
		solrFldMapTest.assertSolrFldHasNumValues(record, physFormatFldName, 0);

		// 007 but byte 3 is z   (Other)  (based on 5665607)    think there are about 1600 of these
		expVal = expVal + " from 300";
		record = factory.newRecord();
		record.setLeader(ldr);
		cf007.setData("sd zsngnnmmned");
		record.addVariableField(cf007);
		DataField df300 = factory.newDataField("300", ' ', ' ');
		df300.addSubfield(factory.newSubfield('a', "1 sound disc :"));
		df300.addSubfield(factory.newSubfield('b', "digital ;"));
		df300.addSubfield(factory.newSubfield('c', "4 3/4 in."));
		record.addVariableField(df300);
		record.addVariableField(df999atLibrary);
		solrFldMapTest.assertSolrFldValue(record, formatFldName, Format.MUSIC_RECORDING.toString());
		solrFldMapTest.assertSolrFldHasNumValues(record, physFormatFldName, 1);
		solrFldMapTest.assertSolrFldValue(record, physFormatFldName, expVal);
		// not if online only
		record.removeVariableField(df999atLibrary);
		record.addVariableField(df999online);

		// no 007, but 300  (based on 314009)  think there are about 1800 of these
		record = factory.newRecord();
		record.setLeader(ldr);
		df300 = factory.newDataField("300", ' ', ' ');
		df300.addSubfield(factory.newSubfield('a', "1 sound disc :"));
		df300.addSubfield(factory.newSubfield('b', "digital ;"));
		df300.addSubfield(factory.newSubfield('b', "4 3/4 in."));
		record.addVariableField(df300);
		record.addVariableField(df999atLibrary);
		solrFldMapTest.assertSolrFldValue(record, formatFldName, Format.MUSIC_RECORDING.toString());
		solrFldMapTest.assertSolrFldHasNumValues(record, physFormatFldName, 1);
		solrFldMapTest.assertSolrFldValue(record, physFormatFldName, expVal);
		// not if online only
		record.removeVariableField(df999atLibrary);
		record.addVariableField(df999online);
	}


	/** test regex to find CD descriptions in 300 field */
@Test
	public void testDescribesCD()
	{
		Assert.assertTrue(FormatUtils.describesCD("1 sound disc (1 hr., 1 min.) : digital, stereo. ; 4 3/4 in."));
		Assert.assertTrue(FormatUtils.describesCD("1 sound disc (1:06:59) : digital, stereo. ; 4 3/4 in. + pamphlet."));
		Assert.assertTrue(FormatUtils.describesCD("1 sound disc (39:46) : digital, stereo. ; 4 3/4 in."));
		Assert.assertTrue(FormatUtils.describesCD("1 sound disc (40 min., 29 sec.) : digital, stereo. ; 4 3/4 in."));
		Assert.assertTrue(FormatUtils.describesCD("1 sound disc (43 min.) : digital, stereo. ; 4 3/4 in. + pamphlet."));
		Assert.assertTrue(FormatUtils.describesCD("1 sound disc (43 min.) : digital, stereo. ; 4 3/4 in."));
		Assert.assertTrue(FormatUtils.describesCD("1 sound disc (44 min.) digital, stereo. ; 4 3/4 in."));
		Assert.assertTrue(FormatUtils.describesCD("1 sound disc (51 min.) : digital. ; 4 3/4 in."));
		Assert.assertTrue(FormatUtils.describesCD("1 sound disc (68:57 min.) : digital, analog ; 4 3/4 in."));
		Assert.assertTrue(FormatUtils.describesCD("1 sound disc : digital ; 4 3/4 in."));
		Assert.assertTrue(FormatUtils.describesCD("1 sound disc : digital, 4 3/4 in."));
		Assert.assertTrue(FormatUtils.describesCD("1 sound disc : digital, chiefly mono. ; 4 3/4 in."));
		Assert.assertTrue(FormatUtils.describesCD("1 sound disc : digital, monaural ; 4 3/4 in. + pamphlet."));
		Assert.assertTrue(FormatUtils.describesCD("1 sound disc : digital, mono. ; 4 3/4 in."));
		Assert.assertTrue(FormatUtils.describesCD("1 sound disc : digital, stereo ; 4 3/4 in."));
		Assert.assertTrue(FormatUtils.describesCD("1 sound disc : digital, stereo. 4 3/4 in."));
		Assert.assertTrue(FormatUtils.describesCD("1 sound disc : digital, stereo. ; 4 3/4 in. + booklet."));
		Assert.assertTrue(FormatUtils.describesCD("1 sound disc : digital, stereo. ; 4 3/4 in. + pamphlet."));
		Assert.assertTrue(FormatUtils.describesCD("1 sound disc : digital, stereo. ; 4 3/4 in."));
		Assert.assertTrue(FormatUtils.describesCD("2 sound discs : digital ; 4 3/4 in."));
		Assert.assertTrue(FormatUtils.describesCD("2 sound discs : digital, stereo. ; 4 3/4 in."));
		Assert.assertTrue(FormatUtils.describesCD("2 sound discs : digital, stereo., HJ ; 4 3/4 in."));
		Assert.assertTrue(FormatUtils.describesCD("1 sound disc (ca. 1 hr. 6 min.) : digital, stereo. ; 4 3/4 in."));
		Assert.assertTrue(FormatUtils.describesCD("3 sound discs (ca. 151 min.) : digital ; 4 3/4 in."));
		Assert.assertTrue(FormatUtils.describesCD("3 sound discs (ca. 2 hrs., 56 min.) : digital, stereo. ; 4 3/4 in. + 1 booklet (147 p.)."));
		Assert.assertTrue(FormatUtils.describesCD("1 sound disc : digital, mono. ; c 4 3/4in."));
		Assert.assertTrue(FormatUtils.describesCD("1 sound disc ; 4 3/4 in."));
		Assert.assertTrue(FormatUtils.describesCD("1 compact sound disc : digital, stereo. ; 4 3/4 in."));

		// look!  centimeters
		Assert.assertTrue(FormatUtils.describesCD("1 sound disc : digital, mono. ; 12 cm."));
		Assert.assertTrue(FormatUtils.describesCD("2 sound discs : digital, mono. ; 12 cm."));
		Assert.assertTrue(FormatUtils.describesCD("1 sound disc : digital ; 12 cm."));

		// audio disc not sound disc
		Assert.assertTrue(FormatUtils.describesCD("2 audio discs : digital, CD audio ; 4 3/4 in."));
		Assert.assertTrue(FormatUtils.describesCD("1 audio disc : digital, CD audio, 4 3/4 in."));
		Assert.assertTrue(FormatUtils.describesCD("1 audio disc : digital, CD audio, mono ; 4 3/4 in."));

		// CD audio, not digital
		Assert.assertTrue(FormatUtils.describesCD("1 audio disc : 4 3/4 in."));
		Assert.assertTrue(FormatUtils.describesCD("1 audio disc : CD audio ; 4 3/4 in."));
		Assert.assertTrue(FormatUtils.describesCD("1 audio disc : CD audio, 4 3/4 in."));
		Assert.assertTrue(FormatUtils.describesCD("1 audio disc : CD-R, 4 3/4 in."));
		Assert.assertTrue(FormatUtils.describesCD("1 audio disc : CD-R, CD audio ; 4 3/4 in."));
		Assert.assertTrue(FormatUtils.describesCD("1 audio disc : digital ; 4 3/4 in."));
		Assert.assertTrue(FormatUtils.describesCD("1 audio disc : digital, CD audio ; 4 3/4 in."));
		Assert.assertTrue(FormatUtils.describesCD("1 audio disc : digital, CD audio, 4 3/4 in."));
		Assert.assertTrue(FormatUtils.describesCD("1 audio disc : digital, CD audio, mono ; 4 3/4 in."));

		// outliers
//		Assert.assertTrue(FormatUtils.describesCD("1 sound disc ; 12 cm + 1 booklet (116 p.)."));
//		Assert.assertTrue(FormatUtils.describesCD("1 sound disc (4 3/4 in.)"));
//		Assert.assertTrue(FormatUtils.describesCD("1 sound disc : 500 rpm, stereo., digital ; 4 3/4 in."));
//		Assert.assertTrue(FormatUtils.describesCD("1 disc (59 min.) : digital, stereo. ; 4 3/4 in. + 1 booklet."));
//		Assert.assertTrue(FormatUtils.describesCD("127 p. : ill. (some col.), plans ; 24 cm. + 1 sound disc (digital : 4 3/4 in.)"));
//		Assert.assertTrue(FormatUtils.describesCD("1 audio disc : CD audio"));

		// NOT
		Assert.assertFalse(FormatUtils.describesCD("1 sound disc (6 hr.) : DVD audio, digital ; 4 3/4 in."));
		Assert.assertFalse(FormatUtils.describesCD("1 sound disc : digital, DVD ; 4 3/4 in."));
		Assert.assertFalse(FormatUtils.describesCD("1 sound disc : digital, DVD audio ; 4 3/4 in."));
		Assert.assertFalse(FormatUtils.describesCD("1 sound disc : digital, DVD audio; 4 3/4 in."));
		Assert.assertFalse(FormatUtils.describesCD("1 sound disc : digital, SACD ; 4 3/4 in. + 1 BluRay audio disc."));
		Assert.assertFalse(FormatUtils.describesCD("1 online resource (1 sound file)"));
		Assert.assertFalse(FormatUtils.describesCD("2s. 12in. 33.3rpm."));
		Assert.assertFalse(FormatUtils.describesCD("1 sound disc : 33 1/3 rpm, stereo ; 12 in."));
		Assert.assertFalse(FormatUtils.describesCD("1 sound disc : analog, 33 1/3 rpm, stereo. ; 12 in."));
		Assert.assertFalse(FormatUtils.describesCD("1 sound disc : 33 1/3 rpm ; 12 in."));
		Assert.assertFalse(FormatUtils.describesCD("1 sound disc (47 min) : analog, 33 1/3 rpm., stereo. ; 12 in."));
	}


@Test
	public void testRecording78()
	{
		String expVal = FormatPhysical.SHELLAC_78.toString();
		Leader ldr = factory.newLeader("01002cjm a2200313Ma 4500");
		Record record = factory.newRecord();
		record.setLeader(ldr);
		cf007.setData("sd dmsdnnmslne");
		record.addVariableField(cf007);
		record.addVariableField(df999atLibrary);
		solrFldMapTest.assertSolrFldValue(record, formatFldName, Format.MUSIC_RECORDING.toString());
		solrFldMapTest.assertSolrFldHasNumValues(record, physFormatFldName, 1);
		solrFldMapTest.assertSolrFldValue(record, physFormatFldName, expVal);
		// not if online only
		record.removeVariableField(df999atLibrary);
		record.addVariableField(df999online);
		solrFldMapTest.assertSolrFldHasNumValues(record, physFormatFldName, 0);

//		// no 007, but 300   (based on 8101257)    think there are about 200 of these
//		record = factory.newRecord();
//		record.addVariableField(df999atLibrary);
//		DataField df300 = factory.newDataField("300", ' ', ' ');
//		df300.addSubfield(factory.newSubfield('b', "78 rpm ;"));
//		record.addVariableField(df300);
//		solrFldMapTest.assertSolrFldValue(record, formatFldName, Format.MUSIC_RECORDING.toString());
//		solrFldMapTest.assertSolrFldHasNumValues(record, physFormatFldName, 1);
//		solrFldMapTest.assertSolrFldValue(record, physFormatFldName, expVal);
//		// not if online only
//		record.removeVariableField(df999atLibrary);
//		record.addVariableField(df999online);
	}

@Test
	public void testRecordingVinyl()
	{
		String expVal = FormatPhysical.VINYL.toString();
		// based on 309570
		Leader ldr = factory.newLeader("02683cjm a2200565ua 4500");
		Record record = factory.newRecord();
		record.setLeader(ldr);
		cf007.setData("sdubsmennmplue");
		record.addVariableField(cf007);
		record.addVariableField(df999atLibrary);
		solrFldMapTest.assertSolrFldValue(record, formatFldName, Format.MUSIC_RECORDING.toString());
		solrFldMapTest.assertSolrFldHasNumValues(record, physFormatFldName, 1);
		solrFldMapTest.assertSolrFldValue(record, physFormatFldName, expVal);
		// not if online only
		record.removeVariableField(df999atLibrary);
		record.addVariableField(df999online);
		solrFldMapTest.assertSolrFldHasNumValues(record, physFormatFldName, 0);

		// around 4000 have no 007 but have a 300  with 33 in it

		// no 007, but 300 (based on 6594)   there are 873 of these, with this exact 300 value
		expVal = expVal + " from 300";
		record = factory.newRecord();
		record.setLeader(ldr);
		DataField df300 = factory.newDataField("300", ' ', ' ');
		df300.addSubfield(factory.newSubfield('a', "2s. 12in. 33.3rpm."));
		record.addVariableField(df300);
		record.addVariableField(df999atLibrary);
		solrFldMapTest.assertSolrFldValue(record, formatFldName, Format.MUSIC_RECORDING.toString());
		solrFldMapTest.assertSolrFldHasNumValues(record, physFormatFldName, 1);
		solrFldMapTest.assertSolrFldValue(record, physFormatFldName, expVal);
		// not if online only
		record.removeVariableField(df999atLibrary);
		record.addVariableField(df999online);

		// (based on 307863)   500 with approx this 300 value
		record = factory.newRecord();
		record.setLeader(ldr);
		df300 = factory.newDataField("300", ' ', ' ');
		df300.addSubfield(factory.newSubfield('a', "1 sound disc :"));
		df300.addSubfield(factory.newSubfield('b', "analog, 33 1/3 rpm, stereo. ;"));
		df300.addSubfield(factory.newSubfield('c', "12 in."));
		record.addVariableField(df300);
		record.addVariableField(df999atLibrary);
		solrFldMapTest.assertSolrFldValue(record, formatFldName, Format.MUSIC_RECORDING.toString());
		solrFldMapTest.assertSolrFldHasNumValues(record, physFormatFldName, 1);
		solrFldMapTest.assertSolrFldValue(record, physFormatFldName, expVal);
		// not if online only
		record.removeVariableField(df999atLibrary);
		record.addVariableField(df999online);
	}

	/** test regex to find Vinyl 33 1/3 descriptions in 300 field */
@Test
	public void testDescribesVinyl()
	{
		// sorted
		Assert.assertTrue(FormatUtils.describesVinyl("1 disc.  33 1/3 rpm. stereo. 12 in."));
		Assert.assertTrue(FormatUtils.describesVinyl("1 disc.  33.3 rpm. stereo. 12 in."));
		Assert.assertTrue(FormatUtils.describesVinyl("1 disc. 33 1/3 rpm.  quad. 12 in."));
		Assert.assertTrue(FormatUtils.describesVinyl("1 disc. 33 1/3 rpm. 12 in."));
		Assert.assertTrue(FormatUtils.describesVinyl("1 disc. 33 1/3 rpm. quad. 12 in."));
		Assert.assertTrue(FormatUtils.describesVinyl("1 disc. 33 1/3 rpm. stereo. 12 in."));
		Assert.assertTrue(FormatUtils.describesVinyl("1 s.  12 in.  33 1/3 rpm.  stereophonic."));
		Assert.assertTrue(FormatUtils.describesVinyl("1 s. 12 in. 33 1/3 rpm. microgroove."));
		Assert.assertTrue(FormatUtils.describesVinyl("1 sound disc (38 min.) : 33 1/3 rpm, mono. ; 12 in."));
		Assert.assertTrue(FormatUtils.describesVinyl("1 sound disc : 33 1/3 rpm ; 12 in. + insert."));
		Assert.assertTrue(FormatUtils.describesVinyl("1 sound disc : 33 1/3 rpm ; 12 in."));
		Assert.assertTrue(FormatUtils.describesVinyl("1 sound disc : 33 1/3 rpm, ; 12 in."));
		Assert.assertTrue(FormatUtils.describesVinyl("1 sound disc : 33 1/3 rpm, monaural ; 12 in."));
		Assert.assertTrue(FormatUtils.describesVinyl("1 sound disc : 33 1/3 rpm, stereo ; 12 in. + insert ([4] p.)"));
		Assert.assertTrue(FormatUtils.describesVinyl("1 sound disc : 33 1/3 rpm, stereo. ; 12 in."));
		Assert.assertTrue(FormatUtils.describesVinyl("1 sound disc : analog, 33 1/3 rpm ; 12 in."));
		Assert.assertTrue(FormatUtils.describesVinyl("1 sound disc : analog, 33 1/3 rpm, mono. ; 12 in."));
		Assert.assertTrue(FormatUtils.describesVinyl("1 sound disc : analog, 33 1/3 rpm, stereo ; 12 in."));
		Assert.assertTrue(FormatUtils.describesVinyl("1 sound disc : analog, 33 1/3 rpm, stereo. ; 12 in. + insert."));
		Assert.assertTrue(FormatUtils.describesVinyl("1 sound disc analog, 33 1/3 rpm, stereo. ; 12 in."));
		Assert.assertTrue(FormatUtils.describesVinyl("1 sound disc: analog, stereo, 33 1/3 rpm, 12 in."));
		Assert.assertTrue(FormatUtils.describesVinyl("1-1/4s. 12in. 33.3rpm."));
		Assert.assertTrue(FormatUtils.describesVinyl("1/2 s. 12in. 33.3rpm. stereophonic."));
		Assert.assertTrue(FormatUtils.describesVinyl("1/2 s. 33 1/3 rpm. stereophonic. 12 in."));
		Assert.assertTrue(FormatUtils.describesVinyl("1/3s. 12in.  33.3rpm. stereophonic."));
		Assert.assertTrue(FormatUtils.describesVinyl("1/6s. 12in. 33.3rpm."));
		Assert.assertTrue(FormatUtils.describesVinyl("10s. 12in. 33.3rpm."));
		Assert.assertTrue(FormatUtils.describesVinyl("1s. 10in. 33.3rpm."));
		Assert.assertTrue(FormatUtils.describesVinyl("1s. 12in. 33.3rpm."));
		Assert.assertTrue(FormatUtils.describesVinyl("2 discs. 33 1/3 rpm.  stereo. 12 in."));
		Assert.assertTrue(FormatUtils.describesVinyl("2 discs. 33 1/3 rpm. stereo. 12 in."));
		Assert.assertTrue(FormatUtils.describesVinyl("2 s.  12 in.  33 1/3 rpm.  microgroove.  stereophonic."));
		Assert.assertTrue(FormatUtils.describesVinyl("2 s.  12 in.  33 1/3 rpm. stereophonic."));
		Assert.assertTrue(FormatUtils.describesVinyl("2 s. 12 in. 33.3 rpm."));
		Assert.assertTrue(FormatUtils.describesVinyl("2s.  12in.  33 1/3rpm. stereophonic."));
		Assert.assertTrue(FormatUtils.describesVinyl("2s. 12in. 33 1/3rpm. stereophonic."));
		Assert.assertTrue(FormatUtils.describesVinyl("3 discs. 33 1/3 rpm.  stereo. 12 in."));
		Assert.assertTrue(FormatUtils.describesVinyl("4s.  12in.  33.3rpm. stereophonic."));
		Assert.assertTrue(FormatUtils.describesVinyl("4s. 12in. 33.3rpm. stereophonic."));
		Assert.assertTrue(FormatUtils.describesVinyl("5 sound discs : 33 1/3 rpm ; 12 in."));
		Assert.assertTrue(FormatUtils.describesVinyl("on side 1 of 1 disc. 33 1/3 rpm. stereo. 12 in."));

		// NOT
		Assert.assertFalse(FormatUtils.describesVinyl("1 sound disc : digital ; 4 3/4 in."));
		Assert.assertFalse(FormatUtils.describesVinyl("1 sound disc : digital, stereo. ; 4 3/4 in."));
		Assert.assertFalse(FormatUtils.describesVinyl("1 videodisc (133 min.) : sd., col. ; 4 3/4 in."));
		Assert.assertFalse(FormatUtils.describesVinyl("1 score (18 p.) ; 22 x 28 cm. + 4 parts ; 33 cm. + 1 sound disc (digital ; 4 3/4 in.)"));
		Assert.assertFalse(FormatUtils.describesVinyl("1 sound disc (33 min.) : digital, stereo. ; 4 3/4 in."));
		Assert.assertFalse(FormatUtils.describesVinyl("1 sound disc (6 hr.) : DVD audio, digital ; 4 3/4 in."));
		Assert.assertFalse(FormatUtils.describesVinyl("1 sound disc : digital, DVD ; 4 3/4 in."));
		Assert.assertFalse(FormatUtils.describesVinyl("1 sound disc : digital, DVD audio ; 4 3/4 in."));
		Assert.assertFalse(FormatUtils.describesVinyl("1 sound disc : digital, DVD audio; 4 3/4 in."));
		Assert.assertFalse(FormatUtils.describesVinyl("1 sound disc : digital, SACD ; 4 3/4 in. + 1 BluRay audio disc."));
		Assert.assertFalse(FormatUtils.describesVinyl("1 online resource (1 sound file)"));
	}


@Test
	public void testRecordingCassette()
	{
		String expVal = FormatPhysical.CASSETTE.toString();
		// with 007: based on 4730355
		Leader ldr = factory.newLeader("01205cim a2200337Ia 4500");
		Record record = factory.newRecord();
		record.setLeader(ldr);
		cf007.setData("ss lunjlc-----");
		record.addVariableField(cf007);
		record.addVariableField(df999atLibrary);
		solrFldMapTest.assertSolrFldValue(record, formatFldName, Format.SOUND_RECORDING.toString());
		solrFldMapTest.assertSolrFldHasNumValues(record, physFormatFldName, 1);
		solrFldMapTest.assertSolrFldValue(record, physFormatFldName, expVal);
		// not if online only
		record.removeVariableField(df999atLibrary);
		record.addVariableField(df999online);
		solrFldMapTest.assertSolrFldHasNumValues(record, physFormatFldName, 0);
	}

/*
	// recordings
	VINYL_45,
	WAX_CYLINDER,
	INSTANTANEOUS_DISC,
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

	// then do actual integration tests -- send record all the way through indexing


	/**
	 *  Spec (per Vitus 2013-11, email to gryph-search with Excel spreadsheet attachment):
	 *   (007/00 = h AND  007/01 = b,c,d,h or j)  OR  300a contains "microfilm"
	 *    Naomi addition:  OR  if  callnum.startsWith("MFILM")
	 *    Question:  (what if 245h has "microform" -- see 9646614 for example)
	 */
@Test
	public void testMicrofilm()
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
//		solrFldMapTest.assertSolrFldValue(record, physFormatFldName, expVal);
		solrFldMapTest.assertSolrFldValue(record, physFormatFldName, expVal + " from callnum");

		// 300
		record.removeVariableField(df999);
		DataField df300 = factory.newDataField("300", ' ', ' ');
		df300.addSubfield(factory.newSubfield('a', "21 microfilm reels ;"));
		df300.addSubfield(factory.newSubfield('c', "35 mm."));
		record.addVariableField(df300);
		solrFldMapTest.assertSolrFldHasNumValues(record, formatFldName, 1);
		solrFldMapTest.assertSolrFldValue(record, formatFldName, Format.BOOK.toString());
		solrFldMapTest.assertSolrFldHasNumValues(record, physFormatFldName, 1);
//		solrFldMapTest.assertSolrFldValue(record, physFormatFldName, expVal);
		solrFldMapTest.assertSolrFldValue(record, physFormatFldName, expVal + " from 300");
	}


	/**
	 *  Spec (per Vitus 2013-11, email to gryph-search with Excel spreadsheet attachment):
	 *   (007/00 = h AND  007/01 = e,f or g)  OR  300a contains "microfiche"
	 *    Naomi addition:  OR  if  callnum.startsWith("MFICHE")
	 *    Question:  (what if 245h has "microform" -- see 9646614 for example)
	 */
@Test
	public void testMicrofiche()
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
//		solrFldMapTest.assertSolrFldValue(record, physFormatFldName, expVal);
		solrFldMapTest.assertSolrFldValue(record, physFormatFldName, expVal + " from callnum");

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
//		solrFldMapTest.assertSolrFldValue(record, physFormatFldName, expVal);
		solrFldMapTest.assertSolrFldValue(record, physFormatFldName, expVal + " from 300");
	}
}
