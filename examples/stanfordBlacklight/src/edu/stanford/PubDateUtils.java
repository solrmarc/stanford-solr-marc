package edu.stanford;

import java.util.*;
import java.util.regex.Pattern;

import org.solrmarc.index.SolrIndexer;
import org.marc4j.marc.Record;

import edu.stanford.enumValues.PubDateGroup;

/**
 * Publication Date Utility functions for StanfordIndexer in SolrMarc project
 * 
 * @author Naomi Dushay
 */
public class PubDateUtils {

	/**
	 * Default Constructor: private, so it can't be instantiated by other objects
	 */	
	private PubDateUtils(){ }

	/**
     * check if a 4 digit year for a pub date is within the range.  If not, 
     *  check for a 4 digit date in the 260c that is in range
	 * @param dateToCheck - String containing 4 digit date to check
	 * @param upperLimit - highest valid year (inclusive)
	 * @param lowerLimit - lowest valid year (inclusive)
	 * @param record - the marc record
	 * @return String containing a 4 digit valid publication date, or null
	 */
    static String getValidPubDate(String dateToCheck, int upperLimit, int lowerLimit, Record record) {
		int dateInt = Integer.parseInt(dateToCheck);
		if (dateInt <= upperLimit) {
			if (dateInt >= lowerLimit)
				return dateToCheck;
			else {
				// try to correct year < lowerLimit
				String date260c = SolrIndexer.getDate(record);
				if (date260c != null) {
					int date260int = Integer.parseInt(date260c);
    				if (date260int != 0 &&
    					date260int <= upperLimit && date260int >= lowerLimit)
						return date260c;
				}
			}
		}
		return null;
	}

	
	private static int currYearAsInt = Calendar.getInstance().get(Calendar.YEAR);
	
	static int getCurrentYearAsInt() {
		return currYearAsInt;
	}
	
	static Set<String> getPubDateGroupsForYear(int year) 
	{
		Set<String> resultSet = new HashSet<String>();

		if (year >= (currYearAsInt - 10))
			resultSet.add(PubDateGroup.LAST_10_YEARS.toString());
		if (year >= (currYearAsInt - 50))
			resultSet.add(PubDateGroup.LAST_50_YEARS.toString());
		if (year < (currYearAsInt - 50) && (year > -1.0))
			resultSet.add(PubDateGroup.MORE_THAN_50_YEARS_AGO.toString());
		return resultSet;
	}

	/**
     * given a string containing two digits representing the year, return
     *  the century in a sting, including "century":
     *    00 --> 1st century   11 --> 12th century   etc.
	 */
	static String getCenturyString(String yearDigits) {
		int centuryYearInt = Integer.parseInt(yearDigits) + 1;
		String centuryYearStr = String.valueOf(centuryYearInt);
		return centuryYearStr + getNumberSuffix(centuryYearStr) + " century";
	}

	/**
	 * given a positive number, return the correct adjective suffix for that number
	 *   e.g.:  1 -->  "st"  3 --> "rd"  11 --> "th" 22 --> "nd"
	 */
	static String getNumberSuffix(String numberStr) {
		int len = numberStr.length();
		// teens are a special case
		if (len == 2 && numberStr.charAt(0) == '1')
			return ("th");

		switch (numberStr.charAt(len - 1)) {
		case '1':
			return ("st");
		case '2':
			return ("nd");
		case '3':
			return ("rd");
		default:
			return ("th");
		}
	}

	private static Pattern ddddPattern = Pattern.compile("^\\d{4}$");
	private static Pattern ddduPattern = Pattern.compile("^\\d{3}u$");
	private static Pattern dduuPattern = Pattern.compile("^\\d{2}uu$");
	private static Pattern duuuPattern = Pattern.compile("^\\duuu$");

	static boolean isdddd(String str) {
		return ddddPattern.matcher(str).matches();
	}
	
	static boolean isdddu(String str) {
		return ddduPattern.matcher(str).matches();
	}

	static boolean isdduu(String str) {
		return dduuPattern.matcher(str).matches();
	}

	static boolean isduuu(String str) {
		return duuuPattern.matcher(str).matches();
	}

}
