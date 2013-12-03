package edu.stanford;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.marc4j.marc.*;
import org.solrmarc.tools.*;

import edu.stanford.enumValues.*;

/**
 * Format utility methods for Stanford solrmarc
 *
 * @author Naomi Dushay
 */
public class FormatUtils {

	/**
	 * Default Constructor: private, so it can't be instantiated by other objects
	 */
	private FormatUtils(){ }


	/**
	 * Assign formats based on leader chars 06, 07 and chars in 008
	 *
	 * Algorithms for formats are currently in email  message from Vitus Tang to
	 *  Naomi Dushay, cc Phil Schreur, Margaret Hughes, and Jennifer Vine
	 *  dated July 23, 2008.
	 *
	 * @param leaderStr - the leader field, as a String
	 * @param cf008 - the 008 field as a ControlField object
	 * @param Set of Strings containing Format enum values per the given data
	 */
	static Set<String> getFormatsPerLdrAnd008(String leaderStr, ControlField cf008)
	{
		Set<String> result = new HashSet<String>();

		// Note: MARC21 documentation refers to char numbers that are 0 based,
		// just like java string indexes, so char "06" is at index 6, and is
		// the seventh character of the field

		// assign formats based on leader chars 06, 07 and chars in 008
		char leaderChar07 = leaderStr.charAt(7);
		char leaderChar06 = leaderStr.charAt(6);
		switch (leaderChar06) {
		case 'a':
			if (leaderChar07 == 'a' || leaderChar07 == 'm')
				result.add(Format.BOOK.toString());
			break;
		case 'b':
		case 'p':
			result.add(Format.MANUSCRIPT_ARCHIVE.toString());
			break;
		case 'c':
		case 'd':
			result.add(Format.MUSIC_SCORE.toString());
			break;
		case 'e':
		case 'f':
			result.add(Format.MAP.toString());
			break;
		case 'g':
			// look for m or v in 008 field, char 33 (count starts at 0)
			if (cf008 != null && cf008.find("^.{33}[mv]"))
				result.add(Format.VIDEO.toString());
			break;
		case 'i':
			result.add(Format.SOUND_RECORDING.toString());
			break;
		case 'j':
			result.add(Format.MUSIC_RECORDING.toString());
			break;
		case 'k':
    		// look for i, k, p, s or t in 008 field, char 33 (count starts at 0)
			if (cf008 != null && cf008.find("^.{33}[ikpst]"))
				result.add(Format.IMAGE.toString());
			break;
		case 'm':
			// look for a in 008 field, char 26 (count starts at 0)
			if (cf008 != null && cf008.find("^.{26}a"))
				result.add(Format.DATASET.toString());
			else
				result.add(Format.COMPUTER_FILE.toString());
			break;
		case 'o': // instructional kit
			result.add(Format.OTHER.toString());
			break;
		case 'r': // object
			result.add(Format.OTHER.toString());
			break;
		case 't':
			if (leaderChar07 == 'a' || leaderChar07 == 'm')
				result.add(Format.BOOK.toString());
			break;
		} // end switch

		return result;
	}


	/**
	 * Assign formats based on leader chars 06, 07 and chars in 008
	 *
	 * Algorithms for formats are currently in email  message from Vitus Tang to
	 *  Naomi Dushay, cc Phil Schreur, Margaret Hughes, and Jennifer Vine
	 *  dated July 23, 2008.
	 *
	 * @param leaderStr - the leader field, as a String
	 * @param cf008 - the 008 field as a ControlField object
	 * @param Set of Strings containing Format enum values per the given data
	 * @deprecated
	 */
	static Set<String> getFormatsPerLdrAnd008Old(String leaderStr, ControlField cf008)
	{
		Set<String> result = new HashSet<String>();

		// Note: MARC21 documentation refers to char numbers that are 0 based,
		// just like java string indexes, so char "06" is at index 6, and is
		// the seventh character of the field

		// assign formats based on leader chars 06, 07 and chars in 008
		char leaderChar07 = leaderStr.charAt(7);
		char leaderChar06 = leaderStr.charAt(6);
		switch (leaderChar06) {
		case 'a':
			if (leaderChar07 == 'a' || leaderChar07 == 'm')
				result.add(FormatOld.BOOK.toString());
			break;
		case 'b':
		case 'p':
			result.add(FormatOld.MANUSCRIPT_ARCHIVE.toString());
			break;
		case 'c':
		case 'd':
			result.add(FormatOld.MUSIC_SCORE.toString());
			break;
		case 'e':
		case 'f':
			result.add(FormatOld.MAP_GLOBE.toString());
			break;
		case 'g':
			// look for m or v in 008 field, char 33 (count starts at 0)
			if (cf008 != null && cf008.find("^.{33}[mv]"))
				result.add(FormatOld.VIDEO.toString());
			break;
		case 'i':
			result.add(FormatOld.SOUND_RECORDING.toString());
			break;
		case 'j':
			result.add(FormatOld.MUSIC_RECORDING.toString());
			break;
		case 'k':
    		// look for i, k, p, s or t in 008 field, char 33 (count starts at 0)
			if (cf008 != null && cf008.find("^.{33}[ikpst]"))
				result.add(FormatOld.IMAGE.toString());
			break;
		case 'm':
			// look for a in 008 field, char 26 (count starts at 0)
			if (cf008 != null && cf008.find("^.{26}a"))
				result.add(FormatOld.COMPUTER_FILE.toString());
			break;
		case 'o': // instructional kit
			result.add(FormatOld.OTHER.toString());
			break;
		case 'r': // object
			result.add(FormatOld.OTHER.toString());
			break;
		case 't':
			if (leaderChar07 == 'a' || leaderChar07 == 'm')
				result.add(FormatOld.BOOK.toString());
			break;
		} // end switch

		return result;
	}


	/**
	 * return main format for continuing resource
	 *
	 * @param leaderStr - the leader field, as a String
	 * @param cf008c21 - the 21st byte (starting w 0) of 008 field
	 * @param Set of Strings containing Format enum values per the given data
	 * @return main format for continuing resource, or null if undetermined
	 */
	static String getMainFormatSerial(char leaderChar07, char cf008c21, ControlField f006)
	{
		// look for serial format per leader/07 and 008/21
		if (leaderChar07 == 's'  &&  cf008c21 != '\u0000')
			return getSerialMainFormatFromChar(cf008c21);

		return FormatUtils.getSerialMainFormatFrom006(f006);

//		// look for serial format per leader/07 and 008/21
//		if (leaderChar07 == 's')
//			result = getSerialMainFormatFromCharLimited(cf008c21);
//		if (result == null)
//		{
//			result = FormatUtils.getSerialMainFormatFrom006(f006);
//			if ((result == null) && (leaderChar07 == 's'))
//				// default to journal if 008/21 can be used at all
//				result = getSerialMainFormatFromChar(cf008c21);
//		}
//		return result;
	}

	/**
	 * return format if 006 starts with 's' and 4th char has a desirable
	 *  value.
	 *
	 * @param f006 - 006 as a VariableField object
	 * @return String containing Format enum value per the given data, or null
	 * @return main format for continuing resource, or null if undetermined
	 */
	static String getSerialMainFormatFrom006(ControlField f006)
	{
		if (f006 != null && f006.find("^s")) {
			char c04 = f006.getData().charAt(4);
			return getSerialMainFormatFromChar(c04);
		}
		return null;
	}

	/**
	 * only looks for values of m, n and p
	 * given a character assumed to be the 21st character (zero-based) from
	 *  the 008 field or the 4th char from an 006 field, return the format
	 *  (assuming that there is an indication that the record is for a serial).
	 *  return null if no format is determined.
	 */
	private static String getSerialMainFormatFromCharLimited(char ch) {
		if (ch != '\u0000')
			switch (ch) {
				case 'm': // monographic series
					return Format.BOOK_SERIES.toString();  // FIXME: temporary format
//					return Format.BOOK.toString();
				case 'n':
					return Format.NEWSPAPER.toString();
				case 'p':
					return Format.JOURNAL_PERIODICAL.toString();
			}
		return null;
	}


	/**
	 * given a character assumed to be the 21st character (zero-based) from
	 *  the 008 field or the 4th char from an 006 field, return the format
	 *  (assuming that there is an indication that the record is for a serial).
	 *  return null if no format is determined.
	 */
	private static String getSerialMainFormatFromChar(char ch) {
		if (ch != '\u0000')
			switch (ch) {
				case 'm': // monographic series
					return Format.BOOK_SERIES.toString();  // FIXME: temporary format
//					return Format.BOOK.toString();
				case 'n':
					return Format.NEWSPAPER.toString();
				case 'p':
				case ' ':  // blank
				case '|':  // pipe
				case '#':  // marc documentation uses this to indicate blank
					return Format.JOURNAL_PERIODICAL.toString();
			}
		return getIntegratingMainFormatFromChar(ch);
//		return null;
	}


	/**
	 * given a character assumed to be the 21st character (zero-based) from
	 *  the 008 field or the 4th char from an 006 field, return the format
	 *  (assuming that there is an indication that the record is for a serial).
	 *  return null if no format is determined.
	 */
	protected static String getIntegratingMainFormatFromChar(char ch) {
		if (ch != '\u0000')
			switch (ch) {
				case 'd':
					return Format.UPDATING_DATABASE.toString();  // FIXME: temporary format
				case 'l':
					return "Updating Looseleaf";    // FIXME: temporary format
				case 'w':
					return Format.UPDATING_WEBSITE.toString();  // FIXME: temporary format?
				default:
					return Format.UPDATING_OTHER.toString();  // FIXME: temporary format
			}
		return null;
	}


	/**
	 * Assign format based on Serial publications - leader/07 s
	 *
	 * Algorithms for formats are currently in email  message from Vitus Tang to
	 *  Naomi Dushay, cc Phil Schreur, Margaret Hughes, and Jennifer Vine
	 *  dated July 23, 2008.
	 *
	 * @param leaderStr - the leader field, as a String
	 * @param cf008 - the 008 field as a ControlField object
	 * @param Set of Strings containing Format enum values per the given data
	 * @deprecated (used for old format only)
	 */
	static String getSerialFormat(char leaderChar07, ControlField cf008, VariableField f006)
	{
		String result = null;
		char c21 = '\u0000';
		if (cf008 != null)
			c21 = ((ControlField) cf008).getData().charAt(21);

		// look for serial format per leader/07 and 008/21
		if (leaderChar07 == 's')
			result = getSerialFormatFromChar(c21);
		if (result != null)
			return result;

		// look for serial publications in 006/00 and 006/04
		result = FormatUtils.getSerialFormat006(f006);
		if (result != null)
			return result;

		// default to journal if leader/07 s and 008/21 is blank
		if (leaderChar07 == 's' && cf008 != null && c21 == ' ')
			return FormatOld.JOURNAL_PERIODICAL.toString();

		return null;
	}

	/**
	 * Assign format if 006 starts with 's' and 4th char has a desirable value.
	 *
	 * @param f006 - 006 as a VariableField object
	 * @return String containing Format enum value per the given data, or null
	 * @deprecated (used for old format only)
	 */
	static String getSerialFormat006(VariableField f006)
	{
		if (f006 != null && f006.find("^s")) {
			char c04 = ((ControlField) f006).getData().charAt(4);
			String format = getSerialFormatFromChar(c04);
			if (format != null)
				return format;
			if (c04 == ' ')
				return FormatOld.JOURNAL_PERIODICAL.toString();
		}
		return null;
	}


	/**
	 * given a character assumed to be the 21st character (zero-based) from
	 *  the 008 field or the 4th char from an 006 field, return the format
	 *  (assuming that there is an indication that the record is for a serial).
	 *  return null if no format is determined.
	 * @deprecated (used for old format only)
	 */
	private static String getSerialFormatFromChar(char ch) {
		if (ch != '\u0000')
			switch (ch) {
				case 'm': // monographic series
					return FormatOld.BOOK.toString();
				case 'n':
					return FormatOld.NEWSPAPER.toString();
				case 'p':
					return FormatOld.JOURNAL_PERIODICAL.toString();
			}
		return null;
	}

	/**
	 * this is kept for continuity with the original format values
	 * @param record - marc4j record object
	 * @return true if there is a 245h that contains the string "microform",
	 *  false otherwise
	 * @deprecated
	 */
	static boolean isMicroformatOld(Record record) {
		Set<String> titleH = MarcUtils.getSubfieldDataAsSet(record, "245", "h", " ");
		if (Utils.setItemContains(titleH, "microform"))
			return true;
		else
			return false;
	}

	/**
	 * return true if it is a MARCit record
	 * @param record - marc4j record object
	 * @return true if there is a 590a that contains the string "MARCit brief record",
	 *  false otherwise
	 */
	static boolean isMarcit(Record record) {
		Set<String> f590a = MarcUtils.getSubfieldDataAsSet(record, "590", "a", "");
		if (Utils.setItemContains(f590a, "MARCit brief record"))
			return true;
		else
			return false;
	}

	/**
	 * Assign physical formats based on 007, leader chars and 008 chars
	 *
	 * @param cf007List - a list of 007 fields as ControlField objects
	 * @param accessMethods - set of Strings that can be Online or 'At the Library' or both
	 * @param Set of Strings containing Physical Format enum values as Strings per the given data
	 */
	static Set<String> getPhysicalFormatsPer007(List<ControlField> cf007List, Set<String> accessMethods)
	{
		Set<String> result = new HashSet<String>();

		// Note: MARC21 documentation refers to char numbers that are 0 based,
		// just like java string indexes, so char "6" is at index 6, and is
		// the seventh character of the field

		for (ControlField cf007 : cf007List)
		{
			String cf007data = cf007.getData();
			char cf007_0 = cf007data.charAt(0);
			char cf007_1 = cf007data.charAt(1);
			switch (cf007_0)
			{
				case 'g':
					if (cf007_1 == 's')
						result.add(FormatPhysical.SLIDE.toString());
					break;
				case 'h':
					if ("bcdhj".contains(String.valueOf(cf007_1)))
						result.add(FormatPhysical.MICROFILM.toString());
					else if ("efg".contains(String.valueOf(cf007_1)))
						result.add(FormatPhysical.MICROFICHE.toString());
					break;
				case 'k':
					if (cf007_1 == 'h')
						result.add(FormatPhysical.PHOTO.toString());
					break;
				case 'r':
					result.add(FormatPhysical.REMOTE_SENSING_IMAGE.toString());
					break;
				case 's':
					if (cf007_1 == 'd' && accessMethods.contains(Access.AT_LIBRARY.toString()))
					{
						switch (cf007data.charAt(3))
						{
							case 'b':
								result.add(FormatPhysical.VINYL.toString());
								break;
							case 'd':
								result.add(FormatPhysical.SHELLAC_78.toString());
								break;
							case 'f':
								result.add(FormatPhysical.CD.toString());
								break;
						}
					}
					else if (cf007data.charAt(6) == 'j' && accessMethods.contains(Access.AT_LIBRARY.toString()))
						result.add(FormatPhysical.CASSETTE.toString());
					break;
				default:
					break;
			} // end switch
		} // end for each 007


		return result;
	}


	/**
	 * use regex to find audio CD descriptions in 300 field
	 *   values like
	 *     1 sound disc : digital, stereo ; 4 3/4 in.
	 *     2 sound discs : digital, mono. ; 12 cm.
	 *   see also the test in FormatPhysicalTests
	 * @param str
	 * @return true if it matches
	 */
	public static boolean describesCD(String str)
	{
		Pattern cdPattern = Pattern.compile(".*(sound|audio) discs? (\\((ca. )?\\d+.*\\))?\\D+((digital|CD audio)\\D*[,;.])? (c )?(4 3/4|12 c).*", Pattern.CASE_INSENSITIVE);
		Matcher cdMatcher = cdPattern.matcher(str);
		Pattern dvdPattern = Pattern.compile(".*DVD.*", Pattern.CASE_INSENSITIVE);
		Matcher dvdMatcher = dvdPattern.matcher(str);
		Pattern sacdPattern = Pattern.compile(".*SACD.*", Pattern.CASE_INSENSITIVE);
		Matcher sacdMatcher = sacdPattern.matcher(str);
		Pattern blurayPattern = Pattern.compile(".*blu[- ]?ray.*", Pattern.CASE_INSENSITIVE);
		Matcher blurayMatcher = blurayPattern.matcher(str);
		if (cdMatcher.matches() && !dvdMatcher.matches() && !sacdMatcher.matches() && !blurayMatcher.matches())
			return true;
		else
			return false;
	}

	/**
	 * use regex to find vinyl LP  descriptions in 300 field
	 *   values like
	 *     2s. 12in. 33.3rpm.
	 *     1 sound disc : 33 1/3 rpm, stereo ; 12 in.
	 *     1 sound disc : analog, 33 1/3 rpm, stereo. ; 12 in.
	 *   see also the test in FormatPhysicalTests
	 * @param str
	 * @return true if it matches
	 */
	public static boolean describesVinyl(String str)
	{
		Pattern rpmPattern = Pattern.compile(".*33(\\.3| 1/3) ?rpm.*", Pattern.CASE_INSENSITIVE);
		Matcher rpmMatcher = rpmPattern.matcher(str);
		Pattern sizePattern = Pattern.compile(".*(10|12) ?in.*", Pattern.CASE_INSENSITIVE);
		Matcher sizeMatcher = sizePattern.matcher(str);
		if (rpmMatcher.matches() && sizeMatcher.matches())
			return true;
		else
			return false;
	}
}
