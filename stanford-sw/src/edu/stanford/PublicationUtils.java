package edu.stanford;

import java.util.*;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.solrmarc.tools.*;
import org.marc4j.marc.*;

import edu.stanford.enumValues.PubDateGroup;

/**
 * Publication Data Utility methods for StanfordIndexer in SolrMarc project
 *
 * @author Naomi Dushay
 */
public class PublicationUtils {

	private static int currYearAsInt = Calendar.getInstance().get(Calendar.YEAR);
	private static String currYearAsStr = Integer.toString(currYearAsInt);

	/**
	 * Default Constructor: private, so it can't be instantiated by other objects
	 */
	private PublicationUtils(){ }


	/**
	 * Gets 260ab and 264ab but ignore s.l in 260a and s.n. in 260b
	 * @param vf26xList - a List of the 260 and 264 fields as VariableField objects
     * @param fieldSpec - which marc fields / subfields to use as values
	 * @return Set of strings containing values in 260ab and 264ab, without
	 *  s.l in 260a and without s.n. in 260b
	 */
    @SuppressWarnings("unchecked")
	static Set<String> getPublication(List<VariableField> vf26xList)
    {
		Set<String> resultSet = new LinkedHashSet<String>();
		for (VariableField vf260 : vf26xList)
		{
			DataField df260 = (DataField) vf260;
			List<Subfield> subFlds = df260.getSubfields();
			StringBuilder buffer = new StringBuilder("");
			for (Subfield sf : subFlds)
			{
				char sfcode = sf.getCode();
				String sfdata = sf.getData();
				boolean addIt = false;
				if (sfcode == 'a' && !sfdata.matches("(?i).*s\\.l\\..*") && !sfdata.matches("(?i).*place of .* not identified.*"))
					addIt = true;
				else if (sfcode == 'b' && !sfdata.matches("(?i).*s\\.n\\..*") && !sfdata.matches("(?i).*r not identified.*"))
					addIt = true;
				if (addIt)
				{
					if (buffer.length() > 0)
						buffer.append(" ");
					buffer.append(sfdata);
				}
			}
			if (buffer.length() > 0)
				resultSet.add(Utils.cleanData(buffer.toString()));
		}
		return resultSet;
	}

	/**
	 * returns the publication date from a record, if it is present and not
     *  beyond the current year + 1 (and not earlier than 0500 if it is a
     *  4 digit year
     *   four digit years < 0500 trigger an attempt to get a 4 digit date from 260c
     * Side Effects:  errors in pub date are logged
     * @param date008 - characters 7-10 (0 based index) in 008 field
	 * @param date260c - the date string extracted from the 260c field
	 * @param df264list  - a List of 264 fields as DataField objects
	 * @param id - record id for error messages
	 * @param logger - the logger for error messages
	 * @return String containing publication date, or null if none
	 */
	static String getPubDate(final String date008, String date260c, List<DataField> df264list, String id, Logger logger)
	{
		if (date008 != null) {
			String errmsg = "Bad Publication Date in record " + id + " from 008/07-10: " + date008;
			if (PublicationUtils.isdddd(date008)) {
				String result = PublicationUtils.getValidPubDate(date008, currYearAsInt + 1, 500, date260c, df264list);
				if (result != null)
					return result;
				else
					logger.error(errmsg);
			} else if (PublicationUtils.isdddu(date008)) {
				int myFirst3 = Integer.parseInt(date008.substring(0, 3));
				int currFirst3 = Integer.parseInt(currYearAsStr.substring(0, 3));
				if (myFirst3 <= currFirst3)
					return date008.substring(0, 3) + "0s";
				else
					logger.error(errmsg);
			} else if (PublicationUtils.isdduu(date008)) {
				int myFirst2 = Integer.parseInt(date008.substring(0, 2));
				int currFirst2 = Integer.parseInt(currYearAsStr.substring(0, 2));
				if (myFirst2 <= currFirst2)
					return PublicationUtils.getCenturyString(date008.substring(0, 2));
				else
					logger.error(errmsg);
			}
		}

		return null;
	}

	/**
     * returns the sortable publication date from a record, if it is present
     *  and not beyond the current year + 1, and not earlier than 0500 if
     *   a four digit year
     *   four digit years < 0500 trigger an attempt to get a 4 digit date from 260c
     *  NOTE: errors in pub date are not logged;  that is done in getPubDate()
     * @param date008 - characters 7-10 (0 based index) in 008 field
	 * @param date260c - the date string extracted from the 260c field
	 * @param df264list  - a List of 264 fields as DataField objects
	 * @return String containing publication date, or null if none
	 */
	static String getPubDateSort(String date008, String date260c, List<DataField> df264list) {
		if (date008 != null) {
			// hyphens sort before 0, so the lexical sorting will be correct. I
			// think.
			if (PublicationUtils.isdddd(date008))
				return PublicationUtils.getValidPubDate(date008, currYearAsInt + 1, 500, date260c, df264list);
			else if (PublicationUtils.isdddu(date008)) {
				int myFirst3 = Integer.parseInt(date008.substring(0, 3));
				int currFirst3 = Integer.parseInt(currYearAsStr.substring(0, 3));
				if (myFirst3 <= currFirst3)
					return date008.substring(0, 3) + "-";
			} else if (PublicationUtils.isdduu(date008)) {
				int myFirst2 = Integer.parseInt(date008.substring(0, 2));
				int currFirst2 = Integer.parseInt(currYearAsStr.substring(0, 2));
				if (myFirst2 <= currFirst2)
					return date008.substring(0, 2) + "--";
			}
		}

		return null;
	}


	/**
	 * returns the publication date groupings from a record, if pub date is
     *  given and is no later than the current year + 1, and is not earlier
     *  than 0500 if it is a 4 digit year.
     *   four digit years < 0500 trigger an attempt to get a 4 digit date from 260c
     *  NOTE: errors in pub date are not logged;  that is done in getPubDate()
	 * @param date260c - the date string extracted from the 260c field
	 * @param df264list  - a List of 264 fields as DataField objects
	 * @return Set of Strings containing the publication date groupings
	 *         associated with the publish date
	 */
	static Set<String> getPubDateGroups(String date008, String date260c, List<DataField> df264list)
	{
		Set<String> resultSet = new HashSet<String>();

		// get the pub date, with decimals assigned for inclusion in ranges
		if (date008 != null) {
			if (isdddd(date008)) // exact year
			{
				String myDate = getValidPubDate(date008, currYearAsInt + 1, 500, date260c, df264list);
				if (myDate != null) {
					int year = Integer.parseInt(myDate);
					// "this year" and "last three years" are for 4 digits only
					if (year >= (currYearAsInt - 1))
						resultSet.add(PubDateGroup.THIS_YEAR.toString());
					if (year >= (currYearAsInt - 3))
						resultSet.add(PubDateGroup.LAST_3_YEARS.toString());
					resultSet.addAll(getPubDateGroupsForYear(year));
				}
			}
			else if (isdddu(date008)) // decade
			{
				String first3Str = date008.substring(0, 3);
				int first3int = Integer.parseInt(first3Str);
				int currFirst3 = Integer.parseInt(currYearAsStr.substring(0, 3));
				if (first3int <= currFirst3) {
					if (first3Str.equals(currYearAsStr.substring(0, 3))) // this decade?
					{
						resultSet.add(PubDateGroup.LAST_50_YEARS.toString());
						resultSet.add(PubDateGroup.LAST_10_YEARS.toString());
						if (currYearAsInt % 10 <= 3)
							resultSet.add(PubDateGroup.LAST_3_YEARS.toString());
					}
					else
					{ // not current decade
						if (currYearAsInt % 10 <= 4) // which half of decade?
						{
							// first half of decade - current year ends in 0-4
							if (first3int == (currYearAsInt / 10) - 1)
								resultSet.add(PubDateGroup.LAST_10_YEARS.toString());

							if (first3int >= (currYearAsInt / 10) - 5)
								resultSet.add(PubDateGroup.LAST_50_YEARS.toString());
							else
								resultSet.add(PubDateGroup.MORE_THAN_50_YEARS_AGO.toString());
						}
						else {
							// second half of decade - current year ends in 5-9
							if (first3int > (currYearAsInt / 10) - 5)
								resultSet.add(PubDateGroup.LAST_50_YEARS.toString());
							else
								resultSet.add(PubDateGroup.MORE_THAN_50_YEARS_AGO.toString());
						}
					}

				}
			}
			else if (isdduu(date008)) { // century
				String first2Str = date008.substring(0, 2);
				int first2int = Integer.parseInt(first2Str);
				int currFirst2 = Integer.parseInt(currYearAsStr.substring(0, 2));
				if (first2int <= currFirst2) {
					if (first2Str.equals(currYearAsStr.substring(0, 2))) {
						// current century
						resultSet.add(PubDateGroup.LAST_50_YEARS.toString());

						if (currYearAsInt % 100 <= 19)
							resultSet.add(PubDateGroup.LAST_10_YEARS.toString());
					}
					else {
						if (first2int == (currYearAsInt / 100) - 1)
						{
							// previous century
							if (currYearAsInt % 100 <= 25)
								resultSet.add(PubDateGroup.LAST_50_YEARS.toString());
							else
								resultSet.add(PubDateGroup.MORE_THAN_50_YEARS_AGO.toString());
						}
						else
							resultSet.add(PubDateGroup.MORE_THAN_50_YEARS_AGO.toString());
					}
				}
			}
			// we don't work with duuu or uuuu or other date strings
		}

		return resultSet;
	}


	/**
     * check if a 4 digit year for a pub date is within the range.  If not,
     *  check for a 4 digit date in the 260c that is in range
	 * @param dateToCheck - String containing 4 digit date to check
	 * @param upperLimit - highest valid year (inclusive)
	 * @param lowerLimit - lowest valid year (inclusive)
	 * @param date260c - the date string extracted from the 260c field
	 * @param df264list  - a List of 264 fields as DataField objects
	 * @return String containing a 4 digit valid publication date, or null
	 */
    static String getValidPubDate(String dateToCheck, int upperLimit, int lowerLimit, String date260c, List<DataField> df264list)
    {
		int dateInt = Integer.parseInt(dateToCheck);
		if (dateInt <= upperLimit) {
			if (dateInt >= lowerLimit)
				return dateToCheck;
			else {
				// try to correct year < lowerLimit
				String usable264cdateStr = null;
				for (DataField df264 : df264list)
				{
					char ind2 = df264.getIndicator2();
					List<String> subcList = MarcUtils.getSubfieldStrings(df264, 'c');
					for (String date264cStr : subcList)
					{
						try
						{
							int date264int = Integer.parseInt(DateUtils.getYearFromString(date264cStr));
		    				if (date264int != 0 &&
		    					date264int <= upperLimit && date264int >= lowerLimit)
		    				{
		    					String yearStr = String.valueOf(date264int);
		    					if (ind2 == '1')
			    					return yearStr;
		    					else if (usable264cdateStr == null)
		    						usable264cdateStr = yearStr;
		    				}
						}
						catch (NumberFormatException e)
						{
						}
					}
				}
				if (date260c != null) {
					int date260int = Integer.parseInt(DateUtils.getYearFromString(date260c));
    				if (date260int != 0 &&
    					date260int <= upperLimit && date260int >= lowerLimit)
						return String.valueOf(date260int);
				}
				if (usable264cdateStr != null)
					return usable264cdateStr;
			}
		}
		return null;
	}


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
