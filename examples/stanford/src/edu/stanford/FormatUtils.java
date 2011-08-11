package edu.stanford;

import java.util.*;

import org.marc4j.marc.*;
import org.solrmarc.index.SolrIndexer;
import org.solrmarc.tools.*;

import edu.stanford.enumValues.Format;

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
			result.add(Format.MAP_GLOBE.toString());
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
			if (cf008 != null && cf008.find("^.*{26}a"))
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

/* not yet vetted  2010-10-30					
		// is it an updating database? (leader/07 = "s" or "i" and 008/21 = "d") OR (006/00 = "s" and 006/04 = "d") 
		// (leader/07 = "s" and 008/21 = "d" handled by getSerialFormat()
		// (006/00 = "s" and 006/04 = "d") is handled by getSerialFormat() which calls getSerialFormat006()
		
		if (leaderChar07 == 'i') {
			// check if it's a database based on 008 char 21
			char c21 = '\u0000';
			if (cf008 != null) {
				c21 = ((ControlField) cf008).getData().charAt(21);
				if (c21 == 'd')
					result.add(Format.DATABASE_OTHER.toString());
			}
		}
*/
		
		return result;
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
			return Format.JOURNAL_PERIODICAL.toString();
		
		return null;
	}
	
	/**
	 * Assign format if 006 starts with 's' and 4th char has a desirable
	 *  value.
	 * 
	 * @param f006 - 006 as a VariableField object
	 * @return String containing Format enum value per the given data, or null
	 */
	static String getSerialFormat006(VariableField f006) 
	{
		if (f006 != null && f006.find("^s")) {
			char c04 = ((ControlField) f006).getData().charAt(4);
			String format = getSerialFormatFromChar(c04);
			if (format != null)
				return format;
			if (c04 == ' ')
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
	private static String getSerialFormatFromChar(char ch) {
		if (ch != '\u0000') 
			switch (ch) {
/* not yet vetted  2010-10-30	
				case 'd': // updating database 
					return Format.DATABASE_OTHER.toString();
*/
//				case 'l': // updating looseleaf (ignore)
//					break;
				case 'm': // monographic series
					return Format.BOOK.toString();
				case 'n':
					return Format.NEWSPAPER.toString();
				case 'p':
					return Format.JOURNAL_PERIODICAL.toString();
//				case 'w': // web site
//					break;
			}
		return null;		
	}
	
	/**
	 * @param record - marc4j record object
	 * @return true if there is a 245h that contains the string "microform", 
	 *  false otherwise
	 */
	static boolean isMicroformat(Record record) {
		Set<String> titleH = SolrIndexer.getSubfieldDataAsSet(record, "245", "h", " ");
		if (Utils.setItemContains(titleH, "microform"))
			return true;
		else
			return false;
	}
	
	/**
	 * thesis is determined by the presence of a 502 field.
	 * @param record - marc4j record object
	 * @return true if there is a 502 field, false otherwise
	 */
	static boolean isThesis(Record record) {
        if (record.getVariableFields("502").isEmpty())
			return false;
		else
			return true;
	}
		
}
