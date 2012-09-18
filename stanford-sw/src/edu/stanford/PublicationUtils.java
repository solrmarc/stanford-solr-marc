/*
 * Copyright (c) 2012.  The Board of Trustees of the Leland Stanford Junior University. All rights reserved.
 *
 * Redistribution and use of this distribution in source and binary forms, with or without modification, are permitted provided that: The above copyright notice and this permission notice appear in all copies and supporting documentation; The name, identifiers, and trademarks of The Board of Trustees of the Leland Stanford Junior University are not used in advertising or publicity without the express prior written permission of The Board of Trustees of the Leland Stanford Junior University; Recipients acknowledge that this distribution is made available as a research courtesy, "as is", potentially with defects, without any obligation on the part of The Board of Trustees of the Leland Stanford Junior University to provide support, services, or repair;
 *
 * THE BOARD OF TRUSTEES OF THE LELAND STANFORD JUNIOR UNIVERSITY DISCLAIMS ALL WARRANTIES, EXPRESS OR IMPLIED, WITH REGARD TO THIS SOFTWARE, INCLUDING WITHOUT LIMITATION ALL IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE, AND IN NO EVENT SHALL THE BOARD OF TRUSTEES OF THE LELAND STANFORD JUNIOR UNIVERSITY BE LIABLE FOR ANY SPECIAL, INDIRECT OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN ACTION OF CONTRACT, TORT (INCLUDING NEGLIGENCE) OR STRICT LIABILITY, ARISING OUT OF OR IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
*/
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

	private static int CURRENT_YEAR_AS_INT = Calendar.getInstance().get(Calendar.YEAR);
	private static String CURRENT_YEAR_AS_STR = Integer.toString(CURRENT_YEAR_AS_INT);

	private static int EARLIEST_VALID_YEAR = 500;
	private static int LATEST_VALID_YEAR = CURRENT_YEAR_AS_INT + 1;

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
     *  beyond the current year + 1 (and not earlier than EARLIEST_VALID_YEAR if it is a
     *  4 digit year
     *   four digit years < EARLIEST_VALID_YEAR trigger an attempt to get a 4 digit date from 260c
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
				String result = PublicationUtils.getValidPubDateStr(date008, date260c, df264list);
				if (result != null)
					return result;
				else
					logger.error(errmsg);
			} else if (PublicationUtils.isdddu(date008)) {
				int myFirst3 = Integer.parseInt(date008.substring(0, 3));
				int currFirst3 = Integer.parseInt(CURRENT_YEAR_AS_STR.substring(0, 3));
				if (myFirst3 <= currFirst3)
					return date008.substring(0, 3) + "0s";
				else
					logger.error(errmsg);
			} else if (PublicationUtils.isdduu(date008)) {
				int myFirst2 = Integer.parseInt(date008.substring(0, 2));
				int currFirst2 = Integer.parseInt(CURRENT_YEAR_AS_STR.substring(0, 2));
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
     *  and not beyond the current year + 1, and not earlier than EARLIEST_VALID_YEAR if
     *   a four digit year
     *   four digit years < EARLIEST_VALID_YEAR trigger an attempt to get a 4 digit date from 260c
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
				return PublicationUtils.getValidPubDateStr(date008, date260c, df264list);
			else if (PublicationUtils.isdddu(date008)) {
				int myFirst3 = Integer.parseInt(date008.substring(0, 3));
				int currFirst3 = Integer.parseInt(CURRENT_YEAR_AS_STR.substring(0, 3));
				if (myFirst3 <= currFirst3)
					return date008.substring(0, 3) + "-";
			} else if (PublicationUtils.isdduu(date008)) {
				int myFirst2 = Integer.parseInt(date008.substring(0, 2));
				int currFirst2 = Integer.parseInt(CURRENT_YEAR_AS_STR.substring(0, 2));
				if (myFirst2 <= currFirst2)
					return date008.substring(0, 2) + "--";
			}
		}

		return null;
	}


	/**
     * returns the sortable publication date from a record, if it is present
     *  and not beyond the current year + 1, and not earlier than EARLIEST_VALID_YEAR if
     *   a four digit year
     *   four digit years < EARLIEST_VALID_YEAR trigger an attempt to get a 4 digit date from 260c
     *  NOTE: errors in pub date are not logged;  that is done in getPubDate()
     * @param date008 - characters 7-10 (0 based index) in 008 field
	 * @param date260c - the date string extracted from the 260c field
	 * @param df264list  - a List of 264 fields as DataField objects
	 * @return String containing publication date, or null if none
	 */
	static Set<String> getPubDateSliderVals(ControlField cf008, String date260c, List<DataField> df264list)
	{
		Set<String> results = new HashSet<String>();
		if (cf008 != null && cf008.getData().length() >= 15)
		{
			char f008char6 = cf008.getData().charAt(6);
			String date1 = getValidPubYearStrOrNull(cf008.getData().substring(7, 11), date260c, df264list);
			int date1Int = -1;
			if (date1 != null)
				date1Int = Integer.valueOf(date1);
			String rawDate2 = cf008.getData().substring(11, 15);
			String date2 = getValidPubYearStrOrNull(rawDate2);
			int date2Int = -1;
			if (date2 != null)
				date2Int = Integer.valueOf(date2);

			switch (f008char6)
			{
				case 'd':
				case 'i':
				case 'k':
				case 'q':
					// index start, end and years in between
					if (date1 != null)
						results.add(date1);
					if (date2 != null)
						results.add(date2);
					if (date1Int != -1 && date2Int != -1)
					{
						for (int year = date1Int; year < date2Int; year++)
							results.add(String.valueOf(year));
					}
					break;
				case 'm':
					if (date1 != null)
						results.add(date1);
					if (!rawDate2.equals("9999") && date2 != null)
					{
						// index end date and dates between
						results.add(date2);
						if (date1Int != -1 && date2Int != -1)
						{
							for (int year = date1Int; year < date2Int; year++)
								results.add(String.valueOf(year));
						}
					}
					break;
				case 'p':
				case 'r':
				case 't':
					// index only start and end
					if (date1 != null)
						results.add(date1);
					if (date2 != null)
						results.add(date2);
					break;
				case 'b':
				case 'c':
				case 'e':
				case 'n':
				case 's':
				case 'u':
				default:
					if (date1 != null)
						results.add(date1);
					break;
			}

		}

		return results;
	}




	/**
	 * returns the publication date groupings from a record, if pub date is
     *  given and is no later than the current year + 1, and is not earlier
     *  than EARLIEST_VALID_YEAR if it is a 4 digit year.
     *   four digit years < EARLIEST_VALID_YEAR trigger an attempt to get a 4 digit date from 260c
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
				String myDate = getValidPubDateStr(date008, date260c, df264list);
				if (myDate != null) {
					int year = Integer.parseInt(myDate);
					// "this year" and "last three years" are for 4 digits only
					if (year >= (CURRENT_YEAR_AS_INT - 1))
						resultSet.add(PubDateGroup.THIS_YEAR.toString());
					if (year >= (CURRENT_YEAR_AS_INT - 3))
						resultSet.add(PubDateGroup.LAST_3_YEARS.toString());
					resultSet.addAll(getPubDateGroupsForYear(year));
				}
			}
			else if (isdddu(date008)) // decade
			{
				String first3Str = date008.substring(0, 3);
				int first3int = Integer.parseInt(first3Str);
				int currFirst3 = Integer.parseInt(CURRENT_YEAR_AS_STR.substring(0, 3));
				if (first3int <= currFirst3) {
					if (first3Str.equals(CURRENT_YEAR_AS_STR.substring(0, 3))) // this decade?
					{
						resultSet.add(PubDateGroup.LAST_50_YEARS.toString());
						resultSet.add(PubDateGroup.LAST_10_YEARS.toString());
						if (CURRENT_YEAR_AS_INT % 10 <= 3)
							resultSet.add(PubDateGroup.LAST_3_YEARS.toString());
					}
					else
					{ // not current decade
						if (CURRENT_YEAR_AS_INT % 10 <= 4) // which half of decade?
						{
							// first half of decade - current year ends in 0-4
							if (first3int == (CURRENT_YEAR_AS_INT / 10) - 1)
								resultSet.add(PubDateGroup.LAST_10_YEARS.toString());

							if (first3int >= (CURRENT_YEAR_AS_INT / 10) - 5)
								resultSet.add(PubDateGroup.LAST_50_YEARS.toString());
							else
								resultSet.add(PubDateGroup.MORE_THAN_50_YEARS_AGO.toString());
						}
						else {
							// second half of decade - current year ends in 5-9
							if (first3int > (CURRENT_YEAR_AS_INT / 10) - 5)
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
				int currFirst2 = Integer.parseInt(CURRENT_YEAR_AS_STR.substring(0, 2));
				if (first2int <= currFirst2) {
					if (first2Str.equals(CURRENT_YEAR_AS_STR.substring(0, 2))) {
						// current century
						resultSet.add(PubDateGroup.LAST_50_YEARS.toString());

						if (CURRENT_YEAR_AS_INT % 100 <= 19)
							resultSet.add(PubDateGroup.LAST_10_YEARS.toString());
					}
					else {
						if (first2int == (CURRENT_YEAR_AS_INT / 100) - 1)
						{
							// previous century
							if (CURRENT_YEAR_AS_INT % 100 <= 25)
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
     * returns the publication date from a record, if it is present
     *  and not beyond the LATEST_VALID_YEAR, and not earlier than EARLIEST_VALID_YEAR if
     *   a four digit year
     *   four digit years < EARLIEST_VALID_YEAR trigger an attempt to get a 4 digit date from 260c
     *  NOTE: errors in pub date are not logged;  that is done in getPubDate()
     * @param dateFrom008 - 4 character date from characters 7-10 or 11-14  in 008 field
	 * @param date260c - the date string extracted from the 260c field
	 * @param df264list  - a List of 264 fields as DataField objects
	 * @return String containing publication date, or null if none
	 */
	private static String getValidPubYearStrOrNull(String dateFrom008, String date260c, List<DataField> df264list)
	{
		if (PublicationUtils.isdddd(dateFrom008))
			return PublicationUtils.getValidPubDateStr(dateFrom008, date260c, df264list);
		else if (PublicationUtils.isdddu(dateFrom008)) {
			int myFirst3 = Integer.parseInt(dateFrom008.substring(0, 3));
			int currFirst3 = Integer.parseInt(CURRENT_YEAR_AS_STR.substring(0, 3));
			if (myFirst3 <= currFirst3)
				return dateFrom008.substring(0, 3) + "0";
		} else if (PublicationUtils.isdduu(dateFrom008)) {
			int myFirst2 = Integer.parseInt(dateFrom008.substring(0, 2));
			int currFirst2 = Integer.parseInt(CURRENT_YEAR_AS_STR.substring(0, 2));
			if (myFirst2 <= currFirst2)
				return dateFrom008.substring(0, 2) + "00";
		} else {
			// last ditch try from 264 and 260c
			String validDate = PublicationUtils.getValidPubDateStr("-1", date260c, df264list);
			if (validDate != null)
				return validDate;
		}

		return null;
	}

	/**
     * returns the publication date from a record, if it is present
     *  and not beyond the LATEST_VALID_YEAR, and not earlier than EARLIEST_VALID_YEAR if
     *   a four digit year
     *   four digit years < EARLIEST_VALID_YEAR trigger an attempt to get a 4 digit date from 260c
     *  NOTE: errors in pub date are not logged;  that is done in getPubDate()
     * @param dateFrom008 - 4 character date from characters 7-10 or 11-14  in 008 field
	 * @param date260c - the date string extracted from the 260c field
	 * @param df264list  - a List of 264 fields as DataField objects
	 * @return String containing publication date, or null if none
	 */
	private static String getValidPubYearStrOrNull(String dateStr)
	{
		String resultStr = null;
		if (PublicationUtils.isdddd(dateStr))
			resultStr = dateStr;
		else if (PublicationUtils.isdddu(dateStr)) {
			int myFirst3 = Integer.parseInt(dateStr.substring(0, 3));
			int currFirst3 = Integer.parseInt(CURRENT_YEAR_AS_STR.substring(0, 3));
			if (myFirst3 <= currFirst3)
				resultStr = dateStr.substring(0, 3) + "0";
		} else if (PublicationUtils.isdduu(dateStr)) {
			int myFirst2 = Integer.parseInt(dateStr.substring(0, 2));
			int currFirst2 = Integer.parseInt(CURRENT_YEAR_AS_STR.substring(0, 2));
			if (myFirst2 <= currFirst2)
				resultStr = dateStr.substring(0, 2) + "00";
		}

		if (yearIsValid(resultStr))
			return resultStr;

		return null;
	}

	private static boolean yearIsValid(String dateStr)
	{
    	try
    	{
    		int dateInt = Integer.parseInt(dateStr);
    		if (dateInt <= LATEST_VALID_YEAR && dateInt >= EARLIEST_VALID_YEAR)
    			return true;
    	} catch (NumberFormatException e) {
    		return false;
    	}
    	return false;
	}

	/**
     * check if a 4 digit year for a pub date is within the range.  If not,
     *  check for a 4 digit date in the 260c that is in range
	 * @param dateToCheck - String containing 4 digit date to check
	 * @param date260c - the date string extracted from the 260c field
	 * @param df264list  - a List of 264 fields as DataField objects
	 * @return String containing a 4 digit valid publication date, or null
	 */
	static String getValidPubDateStr(String dateToCheck, String date260c, List<DataField> df264list)
	{
		return getValidPubDateStr(dateToCheck, LATEST_VALID_YEAR, EARLIEST_VALID_YEAR, date260c, df264list);
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
    static String getValidPubDateStr(String dateToCheck, int upperLimit, int lowerLimit, String date260c, List<DataField> df264list)
    {
    	try
    	{
    		int dateInt = Integer.parseInt(dateToCheck);
    		if (dateInt <= upperLimit && dateInt >= lowerLimit)
    			return dateToCheck;
    	} catch (NumberFormatException e) {
    	}

		// try to get date from 260 or 264
		String usable264cdateStr = null;
		if (df264list != null)
		{
			for (DataField df264 : df264list)
			{
				char ind2 = df264.getIndicator2();
				List<String> subcList = MarcUtils.getSubfieldStrings(df264, 'c');
				for (String date264cStr : subcList)
				{
					try
					{
						String possYear = DateUtils.getYearFromString(date264cStr);
						if (possYear != null)
						{
							int date264int = Integer.parseInt(possYear);
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
					}
					catch (NumberFormatException e)
					{
					}
				}
			}
		}
		if (date260c != null) {
			String possYear = DateUtils.getYearFromString(date260c);
			if (possYear != null)
			{
				int date260int = Integer.parseInt(possYear);
				if (date260int != 0 &&
					date260int <= upperLimit && date260int >= lowerLimit)
					return String.valueOf(date260int);
			}
		}
		if (usable264cdateStr != null)
			return usable264cdateStr;

		return null;
	}


	static int getCurrentYearAsInt() {
		return CURRENT_YEAR_AS_INT;
	}

	static Set<String> getPubDateGroupsForYear(int year)
	{
		Set<String> resultSet = new HashSet<String>();

		if (year >= (CURRENT_YEAR_AS_INT - 10))
			resultSet.add(PubDateGroup.LAST_10_YEARS.toString());
		if (year >= (CURRENT_YEAR_AS_INT - 50))
			resultSet.add(PubDateGroup.LAST_50_YEARS.toString());
		if (year < (CURRENT_YEAR_AS_INT - 50) && (year > -1.0))
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
