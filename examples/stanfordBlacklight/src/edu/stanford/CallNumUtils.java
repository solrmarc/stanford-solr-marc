package edu.stanford;

import java.util.*;
import java.util.regex.*;

import org.solrmarc.tools.*;

import edu.stanford.enumValues.CallNumberType;

/**
 * Call number utility methods for Stanford solrmarc
 * 
 * @author Naomi Dushay
 */

public class CallNumUtils {
	

// TODO:  should have LCcallnum and DeweyCallnum classes, with the call number
//   pieces as fields.  Then parsing would happen once per call number, not
//   all over the place and some parsing repeated.
	
	/**
	 * Default Constructor: private, so it can't be instantiated by other objects
	 */	
	private CallNumUtils(){ }
		
    private static final String PUNCT_PREFIX = "([\\.:\\/\\(])?";
	private static final String NS_PREFIX = "(n\\.s\\.?\\,? ?)?";
	private static final String MONTHS = "jan|feb|mar|apr|may|jun|jul|aug|sep|sept|oct|nov|dec";
	private static final String VOL_LETTERS = "[\\:\\/]?(bd|ed|hov|iss|issue|jahrg|new ser|no|part|pts?|ser|shanah|[^a-z]t|v|vols?|vyp" + "|" + MONTHS + ")";
	private static final String VOL_NUMBERS = "\\d+([\\/-]\\d+)?( \\d{4}([\\/-]\\d{4})?)?( ?suppl\\.?)?";
	private static final String VOL_NUMBERS_LOOSER = "\\d+.*";
	private static final String VOL_NUM_AS_LETTERS = "[A-Z]([\\/-]\\[A-Z]+)?.*";
	
	private static final Pattern VOL_PATTERN = Pattern.compile(PUNCT_PREFIX + NS_PREFIX + VOL_LETTERS + "\\.? ?" + VOL_NUMBERS, Pattern.CASE_INSENSITIVE);
	private static final Pattern VOL_LOOSE_PATTERN = Pattern.compile(PUNCT_PREFIX + NS_PREFIX + VOL_LETTERS + "\\.? ?" + VOL_NUMBERS_LOOSER, Pattern.CASE_INSENSITIVE);
	private static final Pattern VOL_LETTERS_PATTERN = Pattern.compile(PUNCT_PREFIX + NS_PREFIX + VOL_LETTERS + "[\\/\\. ]" + VOL_NUM_AS_LETTERS , Pattern.CASE_INSENSITIVE);

	private static final String ADDL_VOL_REGEX = "[\\:\\/]?(box|carton|fig|flat box|grade|half box|half carton|index|large folder|large map folder|map folder|mfilm|os box|os folder|pl|reel|sheet|small folder|small map folder|suppl|tube|series)";
	private static final Pattern ADDL_VOL_PATTERN = Pattern.compile(ADDL_VOL_REGEX + ".*", Pattern.CASE_INSENSITIVE);

	private static final String FOUR_DIGIT_YEAR_REGEX = "(20|19|18|17|16|15|14)\\d{2}";
	private static final Pattern FOUR_DIGIT_YEAR_PATTERN = Pattern.compile("\\W *" + FOUR_DIGIT_YEAR_REGEX + "\\D.*", Pattern.CASE_INSENSITIVE);
	private static final Pattern FOUR_DIGIT_YEAR_END_PATTERN = Pattern.compile("\\W *" + FOUR_DIGIT_YEAR_REGEX + "$", Pattern.CASE_INSENSITIVE);
	private static final Pattern LOOSER_MONTHS_PATTERN = Pattern.compile(PUNCT_PREFIX + " *" + MONTHS, Pattern.CASE_INSENSITIVE);

	/**
	 * remove volume suffix from LC call number if it is present 
	 * @param rawLCcallnum
	 * @return call number without the volume information, or full call number
	 *   if no volume information is present.
	 */
	static String removeLCVolSuffix(String rawLCcallnum)
	{
		String lopped = rawLCcallnum;
		
		// get suffix to last occurring cutter, if there is one
		String cut2suffix = org.solrmarc.tools.CallNumUtils.getSecondLCcutterSuffix(rawLCcallnum);
		String suffix = cut2suffix;
		if (suffix == null) {
			String cut1suffix = org.solrmarc.tools.CallNumUtils.getFirstLCcutterSuffix(rawLCcallnum);
			if (cut1suffix != null) {
				// first cutter suffix may contain second cutter
				String cut2 = org.solrmarc.tools.CallNumUtils.getSecondLCcutter(rawLCcallnum);
				if (cut2 != null) {
					int ix = cut1suffix.indexOf(cut2);
					if (ix != -1)
						suffix = cut1suffix.substring(0, ix);
					else
						suffix = cut1suffix;
				}
				else
					suffix = cut1suffix;
			}
		}

		// could put last ditch effort with tightest pattern, but don't want to take out too much		

		if (suffix != null) {
			Matcher matcher = VOL_PATTERN.matcher(suffix);
			if (!matcher.find()) {
				matcher = VOL_LOOSE_PATTERN.matcher(suffix);
				if (!matcher.find()) {
					matcher = VOL_LETTERS_PATTERN.matcher(suffix);
					if (!matcher.find()) {
						matcher = ADDL_VOL_PATTERN.matcher(suffix);
					}
				}
			}
// look for first / last match, not any match (subroutine?)?
			if (matcher.find(0)) {
				// return orig call number with matcher part lopped off.
				int ix = rawLCcallnum.indexOf(suffix) + matcher.start();
				if (ix != -1 && ix < rawLCcallnum.length()) {
					lopped = rawLCcallnum.substring(0, ix).trim();
				}
			}
			lopped = removeLooseMonthSuffix(lopped);
		}
		else 
			lopped = removeAddlVolSuffix(rawLCcallnum);

		// make sure lopping wasn't too short - don't lop class 
		//  digits that look like a year
		if (lopped.length() < 4) {
			return rawLCcallnum;
		}
		
		if (lopped.endsWith(":") || lopped.endsWith("("))
			return lopped.substring(0, lopped.length() -1);
		else
			return lopped;
	}

	/**
	 * remove volume suffix from LC call number, if it is present. Call number 
	 *  is for a serial, so if the suffix starts with 4 digits, it can be removed.
	 * @param rawLCcallnum
	 * @return call number without the volume information, or full call number
	 *   if no volume information is present.
	 */
	static String removeLCSerialVolSuffix(String rawLCcallnum)
	{
		String lopped = removeLCVolSuffix(rawLCcallnum);
		String loppedFurther = removeAddlSerialSuffix(lopped);
		
		// make sure lopping wasn't too short - don't lop class 
		//  digits that look like a year
		if (loppedFurther.length() < 4) 
			return lopped;
		else
			return loppedFurther;
	}
	
	/**
	 * remove additional suffixes for serials, like year, or looser month 
	 *   matching
	 */
	static String removeAddlSerialSuffix(String callnum)
	{
		String monthB4Year = removeLooseMonthSuffix(callnum);
		String yearB4Month = removeYearSuffix(callnum);
		if (monthB4Year.length() > yearB4Month.length())
			return yearB4Month;
		else
			return monthB4Year;
	}
	
	/**
	 * remove suffix that begins with year
	 * @param callnum
	 * @return call number without the year suffix, or full call number if no 
	 *  year suffix is present.
	 */
	static String removeYearSuffix(String callnum)
	{
		Matcher matcher = FOUR_DIGIT_YEAR_PATTERN.matcher(callnum);
		if (matcher.find(0)) {
			// return orig call number with matcher part lopped off.
			int ix = matcher.start();
			if (ix != -1 && ix < callnum.length()) {
				return callnum.substring(0, ix).trim();
			}
		}
		// is year is last 4 characters?
		matcher = FOUR_DIGIT_YEAR_END_PATTERN.matcher(callnum);
		if (matcher.find(0)) {
			// return orig call number with matcher part lopped off.
			int ix = matcher.start();
			if (ix != -1 && ix < callnum.length()) {
				return callnum.substring(0, ix).trim();
			}
		}				

		return callnum;
	}

	/**
	 * remove suffix for looser month matching, for call numbers like:
	 * 
	 * @param callnum
	 * @return call number without the month suffix, or full call number if no 
	 *  month suffix is present.
	 */
	static String removeLooseMonthSuffix(String callnum) 
	{
		Matcher matcher = LOOSER_MONTHS_PATTERN.matcher(callnum);
		if (matcher.find(0)) {
			// return orig call number with matcher part lopped off.
			int ix = matcher.start();
			if (ix != -1 && ix < callnum.length()) {
				return callnum.substring(0, ix).trim();
			}
		}				

		return callnum;
	}
	
	/**
	 * remove volume suffix from Dewey call number if it is present
	 * @param rawDeweyCallnum
	 * @return call number without the volume information, or full call number
	 *   if no volume information was present.
	 */
	static String removeDeweyVolSuffix(String rawDeweyCallnum)
	{
		String lopped = rawDeweyCallnum;
		
		String cutSuffix = org.solrmarc.tools.CallNumUtils.getDeweyCutterSuffix(rawDeweyCallnum);
		if (cutSuffix == null)
			return rawDeweyCallnum;
		
		Matcher matcher = VOL_PATTERN.matcher(cutSuffix);
		if (!matcher.find()) {
			matcher = VOL_LOOSE_PATTERN.matcher(cutSuffix);
			if (!matcher.find()) {
				matcher = VOL_LETTERS_PATTERN.matcher(cutSuffix);
				if (!matcher.find()) {
					matcher = ADDL_VOL_PATTERN.matcher(cutSuffix);
				}
			}
		}
		
		if (matcher.find(0)) {
			// return orig call number with matcher part lopped off.
			int ix = rawDeweyCallnum.indexOf(cutSuffix) + matcher.start();
			if (ix != -1 && ix < rawDeweyCallnum.length()) {
				lopped = rawDeweyCallnum.substring(0, ix).trim();
			}
		}

		lopped = removeLooseMonthSuffix(lopped);

		if (lopped.equals(rawDeweyCallnum))
			lopped = removeAddlVolSuffix(rawDeweyCallnum);

		if (lopped.endsWith(":") || lopped.endsWith("("))
			return lopped.substring(0, lopped.length() -1);
		else
			return lopped;
	}

	/**
	 * remove volume suffix from Dewey call number if it is present.  Call 
	 *  number is for a serial, so if the suffix starts with a year, it can be removed.
	 * @param rawDeweyCallnum
	 * @return call number without the volume information, or full call number
	 *   if no volume information is present.
	 */
	static String removeDeweySerialVolSuffix(String rawDeweyCallnum)
	{
		return removeAddlSerialSuffix(removeDeweyVolSuffix(rawDeweyCallnum));
	}
	
	/** regular expression for beginning of call numbers that shouldn't be lopped */
	private static final String DO_NOT_LOP_START_REGEX = "^([A-Z]DVD \\d|[A-Z]CD \\d|MFILM|V\\.)";
	private static final Pattern DO_NOT_LOP_START_PATTERN = Pattern.compile(DO_NOT_LOP_START_REGEX);
	
	/**
	 * loppable call numbers do not start with the forbidden strings, such as
	 *   "MCD" or "ZDVD" or "V."
	 * this avoids lopping too much off those types of call numbers, which 
	 *  usually don't have suffixes
	 */
	private static boolean isLoppableCallnum(String callnum)
	{
		Matcher matcher = DO_NOT_LOP_START_PATTERN.matcher(callnum);
		if (matcher.find())
			return false;
		else
			return true;
	}
	
	/**
	 * try to remove volume suffix from call number of unknown type.  It first
	 *  tries it as an LC call number, then as a Dewey call number, then
	 *  just goes for it
	 *   this is called for non-Dewey, non-LC call numbers.
	 * @param rawCallnum
	 * @param callnumType - the type of this call number, such as SUDOC, ALPHANUM
	 * @return call number without the volume information, or full call number
	 *   if no volume information is present.
	 */
	static String removeNonLCDeweyVolSuffix(String rawCallnum, CallNumberType callnumType) 
	{
		if (!isLoppableCallnum(rawCallnum))
			return rawCallnum;

		String lopped = rawCallnum;
		if (callnumType != CallNumberType.SUDOC)
			// look for archive type stuff (flat box, etc.)
			lopped = removeAddlVolSuffix(rawCallnum);
		
		if (lopped.equals(rawCallnum)) 
		{
			Matcher matcher = VOL_PATTERN.matcher(rawCallnum);
			if (!matcher.find()) {
				matcher = VOL_LOOSE_PATTERN.matcher(rawCallnum);
				if (!matcher.find()) {
					matcher = VOL_LETTERS_PATTERN.matcher(rawCallnum);
				}
			}
// look for first / last match, not any match (subroutine?)?
			if (matcher.find(0)) {
				// return orig call number with matcher part lopped off.
				int ix = matcher.start();
				if (ix != -1 && ix < rawCallnum.length()) {
					lopped = rawCallnum.substring(0, ix).trim();
				}
			}
		}
		
		if (lopped.length() < 5)
			return rawCallnum;
		return lopped;
	}
		
	/**
	 * remove volume suffix from call number if it is present.  Call number is
	 *  for a serial, so if the suffix starts with a year, it can be removed.
	 *   this is called for non-Dewey, non-LC call numbers.
	 * @param rawCallnum
	 * @param callnumType - the type of this call number, such as SUDOC, ALPHANUM
	 * @return call number without the volume information, or full call number
	 *   if no volume information is present.
	 */
	static String removeNonLCDeweySerialVolSuffix(String rawCallnum, CallNumberType callnumType)
	{
		if (!isLoppableCallnum(rawCallnum))
			return rawCallnum;

		String lopped = removeNonLCDeweyVolSuffix(rawCallnum, callnumType);
		if (lopped.length() > 10) {
			String loppedMore = removeAddlSerialSuffix(lopped);
			if (loppedMore.length() >= 5)
				lopped = loppedMore;
		}
		if (lopped.length() < 5)
			return rawCallnum;
		return lopped;
	}
	
	/**
	 * go after more localized call number suffixes, such as "box" "carton"
	 *  "series" "index"
	 * @param rawCallnum
	 * @return call number without the volume information, or full call number
	 *   if no volume information is present.
	 */
	static String removeAddlVolSuffix(String rawCallnum) 
	{
		Matcher matcher = ADDL_VOL_PATTERN.matcher(rawCallnum);
		if (matcher.find()) {
			// return orig call number with matcher part lopped off.
			int ix = matcher.start();
			if (ix != -1 && ix < rawCallnum.length()) {
				return rawCallnum.substring(0, ix).trim();
			}
		}
		return rawCallnum;
	}
	
    /**
     * reduce multiple whitespace to single, remove spaces before or after 
     *   periods, remove spaces between letters and class digits
     */
    static String normalizeLCcallnum(String rawLCcallnum) 
    {
      	// change all multiple whitespace chars to a single space
        String normalizedCallnum = rawLCcallnum.trim().replaceAll("\\s\\s+", " ");
        // remove a space before or after a period
        normalizedCallnum = normalizedCallnum.replaceAll("\\s?\\.\\s?", ".");
        // remove space between class letters and digits
        normalizedCallnum = normalizedCallnum.replaceAll("^([A-Z][A-Z]?[A-Z]?) ([0-9])", "$1$2");
        return normalizedCallnum;
    }
    

	/**
	 * returns true if the entire call number is a volume suffix
	 * @param rawCallnum
	 */
	static boolean callNumIsVolSuffix(String rawCallnum) {
		if (rawCallnum != null && rawCallnum.length() > 0) {
			Matcher matcher = VOL_PATTERN.matcher(rawCallnum);
			if (!matcher.find()) {
				matcher = VOL_LOOSE_PATTERN.matcher(rawCallnum);
				if (!matcher.find()) {
					matcher = VOL_LETTERS_PATTERN.matcher(rawCallnum);
					if (!matcher.find()) {
						matcher = ADDL_VOL_PATTERN.matcher(rawCallnum);
					}
				}
			}
			if (matcher.find(0)) 
				return true;
		}
		return false;
	}
	
	/**
	 * return a sortable shelving key for the call number (which is 
	 *  neither an LC nor a Dewey callnum)
	 * @param rawCallnum - the call number for which a shelfkey is desired
	 * @param recId - record id, for error messages
	 */
	static String getNonLCDeweyShelfKey(String rawCallnum, String recId) {
		return org.solrmarc.tools.CallNumUtils.normalizeSuffix(rawCallnum);
	}

	
	/**
	 * returns a list of any LC call numbers present in the items, normalized
	 * @param itemSet - a Set of Item objects
	 */
	protected static Set<String> getLCcallnums(Set<Item> itemSet) 
	{
		Set<String> result = new HashSet<String>();
		for (Item item : itemSet) {
// FIXME:  shelby locations should be checked for by calling routine??
			if (item.getCallnumType() == CallNumberType.LC
					&& !item.hasIgnoredCallnum() && !item.hasBadLcLaneJackCallnum()
					&& !item.hasShelbyLoc()) {
				String callnum = edu.stanford.CallNumUtils.normalizeLCcallnum(item.getCallnum());
				if (callnum.length() > 0)
					result.add(callnum);
			}
		}
		return result;
	}
	
	/**
	 * returns a list of any Dewey call numbers present in the items, normalized
	 *  with leading zeroes as necessary.
	 * @param itemSet - a Set of Item objects
	 */
	static Set<String> getDeweyNormCallnums(Set<Item> itemSet) 
	{
		Set<String> result = new HashSet<String>();
		for (Item item : itemSet) {
// FIXME:  shelby locations should be checked for by calling routine??
			if (item.getCallnumType() == CallNumberType.DEWEY
					&& !item.hasIgnoredCallnum() && !item.hasShelbyLoc()) {
				String callnum = getNormalizedDeweyCallNumber(item);
				if (callnum.length() > 0)
					result.add(callnum);
			}
		}
		return result;
	}
	
	/**
	 * if the item has a Dewey call number, add leading zeroes to normalized it,
	 *  if necessary, and return it. Otherwise, return empty string
	 */
	private static String getNormalizedDeweyCallNumber(Item item)
	{
		if (item.getCallnumType() == CallNumberType.DEWEY)
			return org.solrmarc.tools.CallNumUtils.addLeadingZeros(item.getCallnum());
		else
			return "";
	}

	
	/**
	 * @param itemSet - set of Item objects
	 * @param id - record id, used for error messages
	 * @param isSerial - true if document is a serial, false otherwise
	 * @return a set of shelfkeys for the lopped call numbers in the items
	 */
	static Set<String> getShelfkeys(Set<Item> itemSet, String id, boolean isSerial)
	{
		Set<String> result = new HashSet<String>();
		for (Item item : itemSet) 
		{
			if (item.hasIgnoredCallnum() || item.hasBadLcLaneJackCallnum() || item.isOnline())
				continue;

			if (item.getCallnum().length() == 0)
				continue;

			String shelfkey = item.getShelfkey(isSerial);
			
			if (shelfkey.length() > 0)
				result.add(shelfkey.toLowerCase());
		}
		return result;
	}

	/**
	 * @param itemSet - set of Item objects
	 * @param id - record id, used for error messages
	 * @param isSerial - true if document is a serial, false otherwise
	 * @return a set of shelfkeys for the lopped call numbers in the items
	 */
	static Set<String> getReverseShelfkeys(Set<Item> itemSet, boolean isSerial)
	{
		Set<String> result = new HashSet<String>();
		for (Item item : itemSet) 
		{
			if (item.hasIgnoredCallnum() || item.hasBadLcLaneJackCallnum() || item.isOnline())
				continue;

			if (item.getCallnum().length() == 0)
				continue;

			String reverseShelfkey = item.getReverseShelfkey(isSerial);
			
			if (reverseShelfkey.length() > 0)
				result.add(reverseShelfkey.toLowerCase());
		}
		return result;
	}

	
	/**
	 * return the call number with the volume part (if it exists) lopped off the
	 *   end of it.
	 * @param fullCallnum
	 * @param callnumType - the call number type (e.g. LC, DEWEY, SUDOC)
	 * @param isSerial - true if the call number is for a serial, false o.w.
	 * @return the lopped call number
	 */
	static String getLoppedCallnum(String fullCallnum, CallNumberType callnumType, boolean isSerial) {
		String loppedCallnum = fullCallnum;
		if (callnumType == CallNumberType.LC)
			if (isSerial)
				loppedCallnum = edu.stanford.CallNumUtils.removeLCSerialVolSuffix(fullCallnum);
			else
				loppedCallnum = edu.stanford.CallNumUtils.removeLCVolSuffix(fullCallnum);
		else if (callnumType == CallNumberType.DEWEY)
			if (isSerial)
				loppedCallnum = edu.stanford.CallNumUtils.removeDeweySerialVolSuffix(fullCallnum);
			else
				loppedCallnum = edu.stanford.CallNumUtils.removeDeweyVolSuffix(fullCallnum);
		else 
//TODO: needs to be longest common prefix
			if (isSerial)
				loppedCallnum = edu.stanford.CallNumUtils.removeNonLCDeweySerialVolSuffix(fullCallnum, callnumType);
			else
				loppedCallnum = edu.stanford.CallNumUtils.removeNonLCDeweyVolSuffix(fullCallnum, callnumType);

		return loppedCallnum;
	}
	
	/**
	 * return a sortable shelving key for the call number
	 * @param rawCallnum - the call number for which a shelfkey is desired
	 * @param type - what kind of call number it is (LC, DEWEY ...)
	 * @param recId - record id, for error messages
	 */
	static String getShelfKey(String rawCallnum, CallNumberType type, String recId) {
		String result = "";
		if (rawCallnum.equals(""))
			return result;
		try {
			if (type == CallNumberType.LC)
				result = type.getPrefix() + org.solrmarc.tools.CallNumUtils.getLCShelfkey(rawCallnum, recId);
			else if (type == CallNumberType.DEWEY)
				result = type.getPrefix() + org.solrmarc.tools.CallNumUtils.getDeweyShelfKey(rawCallnum);
		}
		catch (Exception e) {
		}
		if (result == null || result.equals("") 
				|| result.equals(rawCallnum)) {
			result = getNonLCDeweyShelfKey(rawCallnum, recId);
			if (type != CallNumberType.LC && type != CallNumberType.DEWEY)
				result = type.getPrefix() + result;
			else
				result = CallNumberType.OTHER.getPrefix() + result;
		}
			
		if (result == null || result.equals(""))
			result = CallNumberType.OTHER.getPrefix() + rawCallnum;
		
		return result;
	}

	
// FIXME:  this should just get the item object passed!	
	/**
	 * returns a sortable call number.  If it is the call number for a serial,
	 *  the lexical sort will be in ascending order, but will have the most 
	 *  recent volumes first.  If it's not the call number for a serial, the
	 *  sort will be strictly in ascending order.
	 *  
	 * @param rawCallnum
	 * @param loppedCallnum - the call number with volume/part information lopped off
	 * @param loppedShelfkey - shelfkey for the lopped callnum
	 * @param callnumType - the call number type (e.g. LC, DEWEY, SUDOC ...)
	 * @param isSerial - true if the call number is for a serial 
	 * @param recId - record id, for error messages
	 * @return empty string if given empty string or null, o.w. the goods
	 */
	static String getVolumeSortCallnum(String rawCallnum, String loppedCallnum, String loppedShelfkey, CallNumberType callnumType, boolean isSerial, String recId) 
	{
		if (rawCallnum == null || rawCallnum.length() == 0)
			return "";

		if (rawCallnum.equals(loppedCallnum))
			return loppedShelfkey.toLowerCase();

		if (isSerial) 
		{  
			//   basic call num sorts as shelfkey, volume suffix sorts as reverse key

			// remove ellipsis if they are present
			String volSuffix;
			if (loppedCallnum.endsWith(" ..."))
				volSuffix = rawCallnum.substring(loppedCallnum.length()-4).trim();
			else
				volSuffix = rawCallnum.substring(loppedCallnum.length()).trim();
				
			String volSortString = org.solrmarc.tools.CallNumUtils.getReverseShelfKey(org.solrmarc.tools.CallNumUtils.normalizeSuffix(volSuffix));
			return loppedShelfkey.toLowerCase() + " " + volSortString.toLowerCase();
		}
		else
			// regular shelfkey is correct for sort
			return getShelfKey(rawCallnum, callnumType, recId).toLowerCase();
	}
	
	
	
	/**
	 * given a set of items, assign the lopped call number to be the longest
	 *  prefix common to the items' call numbers, adjusted for expected 
	 *  volume strings ("VOL", "ED", etc.)
	 * @param items
	 */
	static void setLopped2LongestComnPfx(Set<Item> items, int minLen) 
	{
		// single items are not lopped
		Item[] itemArray = new Item[items.size()];
		itemArray = items.toArray(itemArray);

		// find prefix common to all the callnums
		String commonPrefix = itemArray[0].getCallnum();
		for (int i = 1; i < itemArray.length; i++)
		{
			commonPrefix = Utils.getCommonPrefix(commonPrefix, itemArray[i].getCallnum(), Utils.compareNoPeriodsOrSpaces);
		}
		commonPrefix.trim();
		
		// watch for ending years (where "19" or "20" is common ...)
		String yearRegex = "(20|19|18)\\d{2}";
		Pattern yearPatternAtEnd = Pattern.compile(" " + yearRegex + "$");
		Pattern yearPatternThenChar = Pattern.compile(" " + yearRegex + "[ -:]$");
		
		String partialYearRegex = "(20|19|18)\\d{0,1}";
		Pattern partialYearPattern = Pattern.compile(" " + partialYearRegex + "$");
		Matcher matcher = partialYearPattern.matcher(commonPrefix);
		if (matcher.find()) {
			String callnum = itemArray[0].getCallnum();
			// grab common prefix + 3 chars from call number
			int lenToCheck = commonPrefix.length() + 3;
			boolean matchedYear = false;
			if (callnum.length() >= lenToCheck) {
				matcher = yearPatternThenChar.matcher(callnum.substring(0, lenToCheck));
				if (matcher.find()) {
					matchedYear = true;
					commonPrefix = commonPrefix.substring(0, matcher.start()).trim();					
				}
			}
			// did common prefix end in 2 digits and call number has 2 more chars?
			if (!matchedYear) {
				lenToCheck = lenToCheck - 1;
				if (callnum.length() >= lenToCheck) {
					matcher = yearPatternAtEnd.matcher(callnum.substring(0, lenToCheck));
					if (matcher.find()) {
						matchedYear = true;
						commonPrefix = commonPrefix.substring(0, matcher.start()).trim();
					}
				}
			}
			// did common prefix end in 3 digits and call number has 1 more char?
			if (!matchedYear) {
				lenToCheck = lenToCheck - 1;
				if (callnum.length() >= lenToCheck) {
					matcher = yearPatternAtEnd.matcher(callnum.substring(0, lenToCheck));
					if (matcher.find()) {
						commonPrefix = commonPrefix.substring(0, matcher.start()).trim();
					}
				}
			}
		}

		// adjust the common prefix for volume string endings
		String prefix = "[ \\.\\(\\:\\/]";
		String volLettersRegex = "(bd|ed|jahrg|new ser|no|pts?|series|[^a-z]t|v|vols?|vyp)";
		Pattern volLettersPattern = Pattern.compile(prefix + volLettersRegex, Pattern.CASE_INSENSITIVE);
		String addlVolRegex = "(box|carton|disc|flat box|grade|half box|half carton|index|large folder|large map folder|map folder|reel|os box|os folder|small folder|small map folder|suppl|tube|series)";
		Pattern addlVolPattern = Pattern.compile(prefix + addlVolRegex + ".*", Pattern.CASE_INSENSITIVE);

		matcher = volLettersPattern.matcher(commonPrefix);
		if (!matcher.find()) 
			matcher = addlVolPattern.matcher(commonPrefix);
			if (matcher.find(0)) {
				commonPrefix = commonPrefix.substring(0, matcher.start()).trim();
		}

		// remove trailing hyphens, colons, left parens, slashes
		if (commonPrefix.endsWith("-") || commonPrefix.endsWith(":") || 
				commonPrefix.endsWith("(") || commonPrefix.endsWith("/"))
			commonPrefix = commonPrefix.substring(0, commonPrefix.length() - 1).trim();

		String tooShortRegex = "^(mcd|mdvd|zdvd|mfilm)$";
		Pattern tooShortPattern = Pattern.compile(tooShortRegex, Pattern.CASE_INSENSITIVE);
		matcher = tooShortPattern.matcher(commonPrefix);
		boolean tooShort = false;
		if (matcher.find() || commonPrefix.length() <= minLen) { 
			tooShort = true;
		}
		for (int i = 0; i < itemArray.length; i++)
		{
			if (tooShort) 
				itemArray[i].setLoppedCallnum(itemArray[i].getCallnum());
			else {
				itemArray[i].setLoppedCallnum(commonPrefix.trim());
			}
		}
	}
	
	/** call number facet values */
	protected static final String DEWEY_TOP_FACET_VAL = "Dewey Classification";
	protected static final String GOV_DOC_TOP_FACET_VAL = "Government Document";
	protected static final String GOV_DOC_BRIT_FACET_VAL = "British";
	protected static final String GOV_DOC_CALIF_FACET_VAL = "California";
	protected static final String GOV_DOC_FED_FACET_VAL = "Federal";
	protected static final String GOV_DOC_INTL_FACET_VAL = "International";
	protected static final String GOV_DOC_UNKNOWN_FACET_VAL = "Other";
	
	/**
	 * get the type of government document given a location code for a
	 * government document.  
	 * This method should only be called when the location code is known to
	 *  belong to a government document item.
	 * @param govDocLocCode - government document location code
	 * @return user friendly string of the type of gov doc.
	 */
	static String getGovDocTypeFromLocCode(String govDocLocCode) {
		if (govDocLocCode.equals("BRIT-DOCS"))
			return GOV_DOC_BRIT_FACET_VAL;
		if (govDocLocCode.equals("CALIF-DOCS"))
			return GOV_DOC_CALIF_FACET_VAL;
		if (govDocLocCode.equals("FED-DOCS"))
			return GOV_DOC_FED_FACET_VAL;
		if (govDocLocCode.equals("INTL-DOCS"))
			return GOV_DOC_INTL_FACET_VAL;

// TODO: should all the SSRC ones be federal?
		if (govDocLocCode.equals("SSRC-DOCS")
				|| govDocLocCode.equals("SSRC-FICHE")
				|| govDocLocCode.equals("SSRC-NWDOC"))
			return GOV_DOC_FED_FACET_VAL;

		else
			return GOV_DOC_UNKNOWN_FACET_VAL;
	}


}
