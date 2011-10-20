package edu.stanford;

import java.util.*;

import org.apache.log4j.Logger;
import org.marc4j.marc.*;
import org.solrmarc.tools.MarcUtils;

/**
 * Use this class to get mhld_display field values
 * 
 * @author Naomi Dushay
 */
public class MhldDisplayUtil
{
	/** separator used in mhld_display field */
	public static final String SEP = " -|- ";

	public static Logger logger = Logger.getLogger(MhldDisplayUtil.class.getName());

	private Record record = null;
	private String id = null;

	/** if an 852 has subfield '=', then 86x fields with ind2=0 are ignored */
	private boolean df852hasEqualsSubfield = false;
	/** the part of a result string derived from the 852 */
	private String resultPrefixFrom852 = "";

	private boolean haveOpenHoldings = false;
	private boolean have866for852 = false;

	/** helps track when to capture a result string */
	private boolean have867for852 = false;

	/** helps track when to capture a result string */
	private boolean have868for852 = false;

	/** for each 852, we need to have all the patterns in the 853 fields
	 * available so we can turn the correct 863 field into a sensible
	 * "Latest Received" string. 
	 *   key: linkage number from 853 sub 8 
	 *   value: 853 DataField object
	 */
	private Map<Integer, DataField> patternFieldMap = new HashMap<Integer, DataField>();

	/** if there is a "Latest Received" portion to the mhld_display value, it
	 * comes from the most recent 863 for the mhld record.
	 */
	private DataField mostRecent863 = null;
	/** the link number from sub 8 of the most recent 863 (the most recent 863
	 * will have the highest link number and the highest seq number of any 863
	 * for the given mhld)
	 */
	private int mostRecent863linkNum = 0;
	/** the sequence number from sub 8 of the most recent 863 (the most recent
	 * 863 will have the highest link number and the highest seq number of any
	 * 863 for the given mhld)
	 */
	private int mostRecent863seqNum = 0;

	String resultStrFromProcess86x = "";

	/** (ordered) set of mhld_display field values to be returned by getMhldDislayValues() */
	Set<String> result = new LinkedHashSet<String>();

	/**
	 * @param id - used for easier error reporting
	 */
	public MhldDisplayUtil(Record record, String id)
	{
		this.record = record;
		this.id = id;
	}

	/**
	 * return the set of mhld_display values based on mhld fields 
	 *  (852, 853, 863, 866, 867, 868 ...)
	 * 
	 * @return Set of strings in format: 
	 *    library + SEP + 
	 *    location + SEP + 
	 *    comment + SEP + 
	 *    library has + SEP + 
	 *    latest received
	 */
	Set<String> getMhldDisplayValues()
	{
		result = new LinkedHashSet<String>();

		// note: have to read fields sequentially to associate mhld 8xx fields with preceding 852.
		List<DataField> allDataFieldsList = record.getDataFields();
		for (DataField df : allDataFieldsList)
		{
			if (df.getTag().equals("852"))
				process852(df);

			// 853 gives pattern for 863 for "Latest Received"
			else if (df.getTag().equals("853"))
				addFieldToPatternFieldsMap(df);
			else if (df.getTag().equals("863"))
				setMostRecent863(df);

			// 86x fields for "Library Has"
			else if (df.getTag().equals("866"))
				process86x(df, "866");
			else if (df.getTag().equals("867"))
				process86x(df, "867");
			else if (df.getTag().equals("868"))
				process86x(df, "868");
		}

		addValueToResult();

		return result;
	}

	/**
	 * adds a value to the result set, if conditions are met
	 */
	private void addValueToResult()
	{
		// 866?
		if (have866for852 && resultStrFromProcess86x.length() > 0)
			result.add(resultStrFromProcess86x + getLatestReceivedStr());
		// 867 or 868?
		else if ((have867for852 || have868for852) && resultStrFromProcess86x.length() > 0)
			result.add(resultStrFromProcess86x);
		// don't output the first 852
		else if (resultPrefixFrom852.length() > 0)
		{
			// no 866, 852 has sub =
			if (!have866for852 && df852hasEqualsSubfield)
				result.add(resultPrefixFrom852 + SEP + getLatestReceivedStr());
			else
				result.add(resultPrefixFrom852 + SEP);
		}
//System.out.println("DEBUG: just added result " + String.valueOf(result.size()) + " " + result.toString());
	}

	/**
	 * given an 852 field, process it, changing class variables as appropriate
	 *  if the 852 is not skipped, sets resultPrefixFrom852, a portion of a
	 *  result string.
	 */
	private void process852(DataField df852)
	{
		// if there were no intervening fields between the previous 852
		//   and this one, then output the previous 852 information
		addValueToResult();

		resetVarsForNew852();

		String comment = "";
		String sub3 = MarcUtils.getSubfieldData(df852, '3');
		if (sub3 != null && sub3.length() > 0)
			comment = sub3;
		String subz = MarcUtils.getSubfieldData(df852, 'z');
		if (subz != null && subz.length() > 0)
		{
			// skip mhld if 852z has "All holdings transferred"
			if (subz.toLowerCase().contains("all holdings transferred"))
				return;
			else
			{
				if (comment.length() > 0)
					comment = comment + " " + subz;
				else
					comment = subz;
			}
		}

		String libraryCode = MarcUtils.getSubfieldData(df852, 'b');
		String locationCode = MarcUtils.getSubfieldData(df852, 'c');

		resultPrefixFrom852 = libraryCode + SEP + locationCode + SEP + comment + SEP;
//System.out.println("\nDEBUG:   process852: " + id + " resultPrefixFrom852: " + resultPrefixFrom852);

		String subEquals = MarcUtils.getSubfieldData(df852, '=');
		if (subEquals != null && subEquals.length() > 0)
			df852hasEqualsSubfield = true;

	}

	/**
	 * reset class variables for a new 852 field
	 */
	private void resetVarsForNew852()
	{
		resultStrFromProcess86x = "";

		// from 852
		df852hasEqualsSubfield = false;
		resultPrefixFrom852 = "";

		// from 853
		patternFieldMap.clear();
		// from 863
		mostRecent863 = null;
		mostRecent863linkNum = 0;
		mostRecent863seqNum = 0;

		// from 866
		have866for852 = false;
		haveOpenHoldings = false;
		have867for852 = false;
		have868for852 = false;
	}

	/**
	 * for each 852, we need to have all the patterns in the 853 fields
	 *  available so we can turn the 863 field into a sensible "Latest Received" string.
	 * 
	 * @param df853 - an 853 field as a DataField object
	 */
	private void addFieldToPatternFieldsMap(DataField df853)
	{
		String linkSeqNum = MarcUtils.getSubfieldTrimmed(df853, '8');
		try
		{
			patternFieldMap.put(Integer.valueOf(linkSeqNum), df853);
		} 
		catch (NumberFormatException e)
		{
			logger.error(id	+ " has mhld 853 with a non-integer value in sub 8: " + linkSeqNum);
			return;
		}
	}

	/**
	 * we need mostRecent863 to be the 863 field with the highest link and
	 *  sequence number. 
	 * this method can assign: 
	 *   mostRecent863linkNum
	 *   mostRecent863seqNum 
	 *   mostRecent863
	 * 
	 * @param df863
	 */
	private void setMostRecent863(DataField df863)
	{
		String sub8 = MarcUtils.getSubfieldTrimmed(df863, '8');
		int periodPos = sub8.indexOf('.');
		if (periodPos == -1)
		{
			logger.error(id + " has mhld 863 without a period in sub 8: " + sub8);
			return;
		}
		String dfLinkNumStr = sub8.substring(0, periodPos);
		String dfSeqNumStr = sub8.substring(periodPos + 1);
		int dfLinkNum;
		int dfSeqNum;
		try
		{
			dfLinkNum = Integer.valueOf(dfLinkNumStr);
			dfSeqNum = Integer.valueOf(dfSeqNumStr);
		} 
		catch (NumberFormatException e)
		{
			logger.error(id + " has mhld 863 with a non-integer value for link or sequence number: " + sub8);
			return;
		}

		// the df is more recent if the link number is greater, or if the link
		// number is the same and the sequence number is greater
		if ((mostRecent863linkNum < dfLinkNum) ||
			(mostRecent863linkNum == dfLinkNum && mostRecent863seqNum < dfSeqNum))
		{
			mostRecent863linkNum = dfLinkNum;
			mostRecent863seqNum = dfSeqNum;
			mostRecent863 = df863;
		}
	}

	/**
	 * given an 86x field, process it, assigning class variables as appropriate
	 * 
	 * @param df86x - the DataField
	 * @param tag - a string for the tag; either 866, 867 or 868
	 */
	private void process86x(DataField df86x, String tag)
	{
		resultStrFromProcess86x = "";
		// set up result string for this one
		String suba = MarcUtils.getSubfieldData(df86x, 'a');
		if (suba == null)
			suba = "";

		String prefix = "";
		if (tag.equals("866") && !have866for852)
		{
			have866for852 = true;
			if (suba.endsWith("-"))
				haveOpenHoldings = true;
		}
		else if (tag.equals("867"))
		{
			prefix = "Supplement: ";
			if (!have867for852)
				have867for852 = true;
		}
		else if (tag.equals("868"))
		{
			prefix = "Index: ";
			if (!have868for852)
				have868for852 = true;
		}

		resultStrFromProcess86x = resultPrefixFrom852 + prefix + suba + SEP;
		
		addValueToResult();
//System.out.println("DEBUG:     process86x: " + id + "   has resultStr: " + resultStrFromProcess86x);
	}

	/**
	 * @return a string for "Latest Received" based on mostRecent863 and the
	 *         matching pattern field retrieved from the patternFieldMap
	 */
	private String getLatestReceivedStr()
	{
		String result = "";
		if (haveOpenHoldings || (!have866for852 && df852hasEqualsSubfield))
		{
			if (mostRecent863 != null && mostRecent863linkNum != 0)
			{
				DataField pattern853df = patternFieldMap.get(Integer.valueOf(mostRecent863linkNum));
				result = get863DisplayValue(mostRecent863, pattern853df);
			}
		}
//System.out.println("DEBUG:       getLatestReceived: " + id + "    has latest received: " + result);
		return result;
	}

	/**
	 * MHLD records put the pattern of the enumeration in an 853, and the values
	 * for each issue received into the 863. To get a user friendly string, the
	 * captions from the 853 must be applied to the values in the 863. NOTE: the
	 * match between the 853 and 863 linkage numbers should be done before
	 * calling this method.
	 * 
	 * @author Bob Haschart, with some revisions by Naomi Dushay
	 * 
	 * @param df863 - the 863 DataField object to be transformed
	 * @param pattern853df - the 853 DataField containing the pattern for the 863 field.
	 * @return a user friendly string representation of the information in the
	 *         863 field.
	 */
	private String get863DisplayValue(DataField df863, DataField pattern853df)
	{
		StringBuffer result = new StringBuffer();

		if (pattern853df == null)
			return null;

		// subfields a-f contain enumeration information (volume, issue ...)
		// or may have chronology information (year, month ...) if there is
		// no enumeration
		for (char code = 'a'; code <= 'f'; code++)
		{
			String caption = MarcUtils.getSubfieldData(pattern853df, code);
			String value = MarcUtils.getSubfieldData(df863, code);
			if (caption == null || value == null)
				break;
			if (result.length() > 0)
				result.append(" ");
			result.append(getCaptionedStr(caption, value));
		}

		// subfields g-h may contain alternative enumeration schemes
		StringBuffer altSchemeStr = new StringBuffer();
		for (char code = 'g'; code <= 'h'; code++)
		{
			String caption = MarcUtils.getSubfieldData(pattern853df, code);
			String value = MarcUtils.getSubfieldData(df863, code);
			if (caption == null || value == null)
				break;
			if (code != 'g')
				altSchemeStr.append(", ");
			altSchemeStr.append(caption + value);
		}
		// append the alternative enumeration to the result within parens.
		if (altSchemeStr.length() != 0)
			result.append(" (" + altSchemeStr + ")");

		// subfields i-l contain chronology information (year, month ...)
		// subfield m contains alternative chronology info
		StringBuffer chronologyStr = new StringBuffer();
		boolean prependStr = false;
		String strToPrepend = "";
		for (char code = 'i'; code <= 'm'; code++)
		{
			String caption = MarcUtils.getSubfieldData(pattern853df, code);
			String value = MarcUtils.getSubfieldData(df863, code);
			if (caption == null || value == null)
				break;
			if (caption.equalsIgnoreCase("(month)")
					|| caption.equalsIgnoreCase("(season)"))
			{
				value = translateMonthOrSeason(value);
				strToPrepend = ":";
			}
			else if (caption.equalsIgnoreCase("(day)"))
				strToPrepend = " ";

			if (prependStr)
				chronologyStr.append(strToPrepend).append(value);
			else
				chronologyStr.append(value);

			prependStr = true;
		}
		if (chronologyStr.length() > 0)
		{
			// append the chronology info to an existing result within parens.
			if (result.length() > 0)
				result.append(" (").append(chronologyStr).append(")");
			else
				result.append(chronologyStr);
		}

		return result.toString();
	}

	/**
	 * Given a caption string from an 853 subfield and a value string from a
	 * corresponding 863 subfield, return the appropriate display value. Per
	 * http://www.loc.gov/marc/holdings/hd853855.html, captions within parens
	 * should not be output, and the values for (month) and (season) captions
	 * should be translated to an appropriate display value.
	 * 
	 * @param caption
	 *            - string from an 853 subfield
	 * @param value
	 *            - value from an 863 subfield correlated to the 853 subfield
	 * @return string to be displayed
	 */
	private String getCaptionedStr(String caption, String value)
	{
		if (caption.equalsIgnoreCase("(month)")
				|| caption.equalsIgnoreCase("(season)"))
			value = translateMonthOrSeason(value);

		if (caption.startsWith("(") && caption.endsWith(")"))
			caption = "";

		return caption + value;
	}

	/**
	 * turns 01-12 into three letter string for Month; turns 21-24 into six
	 * letter string for season. Otherwise, returns the original value.
	 */
	private String translateMonthOrSeason(String value)
	{
		value = value.replaceAll("01", "January");
		value = value.replaceAll("02", "February");
		value = value.replaceAll("03", "March");
		value = value.replaceAll("04", "April");
		value = value.replaceAll("05", "May");
		value = value.replaceAll("06", "June");
		value = value.replaceAll("07", "July");
		value = value.replaceAll("08", "August");
		value = value.replaceAll("09", "September");
		value = value.replaceAll("10", "October");
		value = value.replaceAll("11", "November");
		value = value.replaceAll("12", "December");
		value = value.replaceAll("21", "Spring");
		value = value.replaceAll("22", "Summer");
		value = value.replaceAll("23", "Autumn");
		value = value.replaceAll("24", "Winter");
		return (value);
	}

}
