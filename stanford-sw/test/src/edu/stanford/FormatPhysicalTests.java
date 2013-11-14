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
	private final String formatFldName = "format";
	private final String physFormatFldName = "format_physical_ssim";
	private final MarcFactory factory = MarcFactory.newInstance();
	private ControlField cf007 = factory.newControlField("007");
	private ControlField cf008 = factory.newControlField("008");

@Before
	public final void setup()
	{
		mappingTestInit();
	}

@Test
	/**
	 *  Spec (per Vitus 2013-11, email to gryph-search with Excel spreadsheet attachment):
	 *   (007/00 = h AND  007/01 = b,c,d,h or j)  OR  300a contains "microfilm"
	 *    Naomi addition:  OR  if  callnum.startsWith("MFILM")
	 *    Question:  (what if 245h has "microform" -- see 9646614 for example)
	 */
	public final void testMicrofilm()
	{
		String expVal = FormatPhysical.MICROFILM.toString();
		Record record = factory.newRecord();
		record.setLeader(factory.newLeader("01543cam a2200325Ka 4500"));

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
		record.setLeader(factory.newLeader("01543cam a2200325Ka 4500"));
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
		Record record = factory.newRecord();
		record.setLeader(factory.newLeader("01543cam a2200325Ka 4500"));

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
		record.setLeader(factory.newLeader("01543cam a2200325Ka 4500"));
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

	// images
	SLIDE,
	PHOTO,
	MICROFILM,
	MICROFICHE,
	REMOTE_SENSING_IMAGE,
	OTHER_IMAGE,

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


}
