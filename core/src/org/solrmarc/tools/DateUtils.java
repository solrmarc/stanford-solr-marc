package org.solrmarc.tools;

import java.text.*;
import java.util.Calendar;
import java.util.regex.*;

import org.apache.log4j.Logger;

public class DateUtils
{

	/**
	 * Default Constructor, private, so it can't be instantiated by other
	 * objects
	 */
	private DateUtils()
	{
	}

	// possible strings for year
	private final static String VALID_YR_REGEX_STR = "(20|19|18|17|16|15|14|13|12|11|10|09|08|07|06|05)[0-9][0-9]";
	private final static Pattern YEAR_PATTERN_STARTS_05_THRU_20 = Pattern.compile(VALID_YR_REGEX_STR + "\\D{0,2}");
	private final static Pattern YEAR_PATTERN_PREC_C_OR_P = Pattern.compile("\\[?[©Ⓟcp]" + VALID_YR_REGEX_STR + "\\D?");
	private final static Pattern YEAR_PATTERN_LAST_DIG_UNCLEAR = Pattern.compile("(20|19|18|17|16|15)[0-9][-?]");
	private final static Pattern YEAR_PATTERN_FIRST_LET_L = Pattern.compile("l(9|8|7|6|5)\\d{2,2}\\D?");

	private final static Pattern YEAR_PATTERN_PREC_IE = Pattern.compile(".*i\\. ?e\\. ?" + VALID_YR_REGEX_STR + "\\D?.*");
	private final static Pattern BC_DATE_PATTERN = Pattern.compile("[0-9]+ [Bb][.]?[Cc][.]?");

	// square bracket fun
	private final static Pattern YEAR_PATTERN_BRACES = Pattern.compile("\\[" + VALID_YR_REGEX_STR + "\\]");
	private final static Pattern YEAR_PATTERN_W_TEXT_IN_BRACES = Pattern.compile("\\[" + VALID_YR_REGEX_STR + "\\D+.*\\]");
	private final static Pattern YEAR_DIGIT_PATTERN_BRACE_19 = Pattern.compile("\\[19\\]\\d{2,2}\\D?");


	private final static DecimalFormat timeFormat = new DecimalFormat("00.00");
	protected static Logger logger = Logger.getLogger(Utils.class.getName());

	/**
	 * Retrieves the four digit year from a string, accommodating outer braces
	 *  and a few other patterns
	 *
	 * @param dateStr String to parse for four digit year
	 * @return Numeric part of date String (or null)
	 */
	public static String getYearFromString(final String dateStr)
	{
		Matcher starts05thru20Matcher = YEAR_PATTERN_STARTS_05_THRU_20.matcher(dateStr);
		Matcher outerBracesTightMatcher = YEAR_PATTERN_BRACES.matcher(dateStr);
		Matcher outerBracesLooseMatcher = YEAR_PATTERN_W_TEXT_IN_BRACES.matcher(dateStr);
		Matcher precSymMatcher = YEAR_PATTERN_PREC_C_OR_P.matcher(dateStr);
		Matcher startsLetLMatcher = YEAR_PATTERN_FIRST_LET_L.matcher(dateStr);
		Matcher bracesAround19Matcher = YEAR_DIGIT_PATTERN_BRACE_19.matcher(dateStr);
		Matcher unclearLastDigitMatcher = YEAR_PATTERN_LAST_DIG_UNCLEAR.matcher(dateStr);
		Matcher ieMatcher = YEAR_PATTERN_PREC_IE.matcher(dateStr);
		Matcher bcMatcher = BC_DATE_PATTERN.matcher(dateStr);

		String cleanDate = null; // raises DD-anomaly

		if (starts05thru20Matcher.matches())  // starts with four digit match
			cleanDate = starts05thru20Matcher.group().substring(0, 4);
		else if (outerBracesTightMatcher.find())
			cleanDate = Utils.removeOuterBrackets(outerBracesTightMatcher.group());
		else if (precSymMatcher.matches())
		{
			cleanDate = precSymMatcher.group().replaceAll("\\[?[©Ⓟcp]", "");
			cleanDate = Utils.removeOuterBrackets(cleanDate.substring(0,4));
		}
		else if (ieMatcher.find())
		{
			cleanDate = ieMatcher.group().replaceAll(".*i\\. ?e\\. ?", "");
			cleanDate = Utils.removeOuterBrackets(cleanDate);
		}
		else if (outerBracesLooseMatcher.find())
			cleanDate = Utils.removeOuterBrackets(outerBracesLooseMatcher.group().substring(1, 5));
		else if (bcMatcher.find())
			cleanDate = null; // ignore b.c. dates
		else if (startsLetLMatcher.find())
			cleanDate = startsLetLMatcher.group().replaceAll("l", "1");
		else if (bracesAround19Matcher.find())
			cleanDate = bracesAround19Matcher.group().replaceAll("\\[", "").replaceAll("\\]", "");
		else if (unclearLastDigitMatcher.find())
			cleanDate = unclearLastDigitMatcher.group().replaceAll("[-?]", "0");

		// is the date no more than 1 year in the future?
		if (cleanDate != null)
		{
			Calendar calendar = Calendar.getInstance();
			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy");
			String thisYear = dateFormat.format(calendar.getTime());
			try
			{
				if (Integer.parseInt(cleanDate) > Integer.parseInt(thisYear) + 1)
					cleanDate = null;
			}
			catch ( NumberFormatException nfe)
			{
				cleanDate = null;
			}
		}

		if (cleanDate != null)
			logger.debug("Date : " + dateStr + " mapped to : " + cleanDate);
		else
			logger.debug("No Date match: " + dateStr);
		return cleanDate;
	}


	/**
	 * Calculate time from milliseconds
	 *
	 * @param totalTime Time in milliseconds
	 * @return Time in the format mm:ss.ss
	 */
	public static String calcTime(final long totalTime)
	{
		return totalTime / 60000 + ":" + timeFormat.format((totalTime % 60000) / 1000);
	}


	/**
     * given a string containing two digits representing the year, return
     *  the century in a string, including "century":
     *    00 --> 1st century   11 --> 12th century   etc.
	 */
	public static String getCenturyString(String yearDigits)
	{
		int centuryYearInt = Integer.parseInt(yearDigits) + 1;
		String centuryYearStr = String.valueOf(centuryYearInt);
		return centuryYearStr + getNumberSuffix(centuryYearStr) + " century";
	}

	/**
	 * given a positive number, return the correct adjective suffix for that number
	 *   e.g.:  1 -->  "st"  3 --> "rd"  11 --> "th" 22 --> "nd"
	 */
	public static String getNumberSuffix(String numberStr)
	{
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
	private static Pattern ddduPattern = Pattern.compile("^\\d{3}[u-]$");
	private static Pattern dduuPattern = Pattern.compile("^\\d{2}[u-]{2}$");
	private static Pattern duuuPattern = Pattern.compile("^\\d[u-]{3}$");

	public static boolean isdddd(String str) {
		return ddddPattern.matcher(str).matches();
	}

	/** true if dddu or ddd- */
	public static boolean isdddu(String str) {
		return ddduPattern.matcher(str).matches();
	}

	/** true if dduu or dd-- */
	public static boolean isdduu(String str) {
		return dduuPattern.matcher(str).matches();
	}

	/** true if duuu or d--- */
	public static boolean isduuu(String str) {
		return duuuPattern.matcher(str).matches();
	}

}
