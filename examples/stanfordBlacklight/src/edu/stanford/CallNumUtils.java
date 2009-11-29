package edu.stanford;

import java.util.*;
import java.util.regex.*;

//import org.solrmarc.tools.CallNumUtils;
import org.solrmarc.tools.StringNaturalCompare;

/**
 * Call number utility functions for Stanford solrmarc
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
		
    private static final String PUNCT_PREFIX = "([\\.:\\/])?";
	private static final String NS_PREFIX = "(n\\.s\\.?\\,? ?)?";
	private static final String MONTHS = "jan|feb|mar|apr|may|jun|jul|aug|sep|oct|nov|dec";
	private static final String VOL_LETTERS = "[\\:\\/]?(bd|ed|jahrg|new ser|no|pts?|ser|t|v|vols?|vyp" + "|" + MONTHS + ")";
	private static final String VOL_NUMBERS = "\\d+([\\/-]\\d+)?( \\d{4}([\\/-]\\d{4})?)?( ?suppl\\.?)?";
	private static final String VOL_NUMBERS_LOOSER = "\\d+.*";
	private static final String VOL_NUM_AS_LETTERS = "[A-Z]([\\/-]\\[A-Z]+)?.*";
	
	private static final Pattern volPattern = Pattern.compile(PUNCT_PREFIX + NS_PREFIX + VOL_LETTERS + "\\.? ?" + VOL_NUMBERS, Pattern.CASE_INSENSITIVE);
	private static final Pattern volPatternLoose = Pattern.compile(PUNCT_PREFIX + NS_PREFIX + VOL_LETTERS + "\\.? ?" + VOL_NUMBERS_LOOSER, Pattern.CASE_INSENSITIVE);
	private static final Pattern volPatLetters = Pattern.compile(PUNCT_PREFIX + NS_PREFIX + VOL_LETTERS + "[\\/\\. ]" + VOL_NUM_AS_LETTERS , Pattern.CASE_INSENSITIVE);

	private static final String MORE_VOL = "[\\:\\/]?(box|carton|flat box|grade|half box|half carton|index|large folder|large map folder|map folder|mfilm|reel|os box|os folder|small folder|small map folder|suppl|tube|series)";
	private static final Pattern moreVolPattern = Pattern.compile(MORE_VOL + ".*", Pattern.CASE_INSENSITIVE);

	private static final String FOUR_DIGIT_YEAR = " \\d{4}\\D";
	private static final Pattern fourDigitYearPattern = Pattern.compile(FOUR_DIGIT_YEAR + ".*", Pattern.CASE_INSENSITIVE);

	/**
	 * remove volume suffix from LC call number if it is present 
	 * @param rawLCcallnum
	 * @return call number without the volume information, or full call number
	 *   if no volume information is present.
	 */
	static String removeLCVolSuffix(String rawLCcallnum)
	{
		// get suffix to last occurring cutter, if there is one
		String cut2suffix = org.solrmarc.tools.CallNumUtils.getSecondLCcutterSuffix(rawLCcallnum);
		String lastSuffix = cut2suffix;
		if (lastSuffix == null) {
			String cut1suffix = org.solrmarc.tools.CallNumUtils.getFirstLCcutterSuffix(rawLCcallnum);
			if (cut1suffix != null) {
				// first cutter suffix may contain second cutter
				String cut2 = org.solrmarc.tools.CallNumUtils.getSecondLCcutter(rawLCcallnum);
				if (cut2 != null) {
					int ix = cut1suffix.indexOf(cut2);
					if (ix != -1)
						lastSuffix = cut1suffix.substring(0, ix);
					else
						lastSuffix = cut1suffix;
				}
				else
					lastSuffix = cut1suffix;
			}
		}

		// could put last ditch effort with tightest pattern, but don't want to take out too much		

		if (lastSuffix != null) {
			Matcher matcher = volPattern.matcher(lastSuffix);
			if (!matcher.find()) {
				matcher = volPatternLoose.matcher(lastSuffix);
				if (!matcher.find()) {
					matcher = volPatLetters.matcher(lastSuffix);
					if (!matcher.find()) {
						matcher = moreVolPattern.matcher(lastSuffix);
					}
				}
			}
// look for first / last match, not any match (subroutine?)?
			if (matcher.find(0)) {
				// return orig call number with matcher part lopped off.
				int ix = rawLCcallnum.indexOf(lastSuffix) + matcher.start();
				if (ix != -1 && ix < rawLCcallnum.length()) {
					return rawLCcallnum.substring(0, ix).trim();
				}
			}				
		}
		else {
			return removeMoreVolSuffix(rawLCcallnum);
		}

		return rawLCcallnum;
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
		String try1 = removeLCVolSuffix(rawLCcallnum);
		if (!try1.equals(rawLCcallnum))
			return try1;
		else
			return removeYearSuffix(rawLCcallnum);
	}
	
	/**
	 * remove suffix that begins with a space followed by four digits followed
	 *  by a non-digit.  (4 digits usually mean a year)
	 * @param rawCallnum
	 * @return call number without the year suffix, or full call number if no 
	 *  year suffix is present.
	 */
	static String removeYearSuffix(String rawCallnum)
	{
		Matcher matcher = fourDigitYearPattern.matcher(rawCallnum);
		if (matcher.find(0)) {
			// return orig call number with matcher part lopped off.
			int ix = matcher.start();
			if (ix != -1 && ix < rawCallnum.length()) {
				return rawCallnum.substring(0, ix).trim();
			}
		}				

		return rawCallnum;
	}

	
	/**
	 * remove volume suffix from Dewey call number if it is present
	 * @param rawDeweyCallnum
	 * @return call number without the volume information, or full call number
	 *   if no volume information was present.
	 */
	static String removeDeweyVolSuffix(String rawDeweyCallnum)
	{
		String cutSuffix = org.solrmarc.tools.CallNumUtils.getDeweyCutterSuffix(rawDeweyCallnum);
		if (cutSuffix == null)
			return rawDeweyCallnum;
		
		Matcher matcher = volPattern.matcher(cutSuffix);
		if (!matcher.find()) {
			matcher = volPatternLoose.matcher(cutSuffix);
			if (!matcher.find()) {
				matcher = volPatLetters.matcher(cutSuffix);
				if (!matcher.find()) {
					matcher = moreVolPattern.matcher(cutSuffix);
				}
			}
		}
		
		if (matcher.find(0)) {
			// return orig call number with matcher part lopped off.
			int ix = rawDeweyCallnum.indexOf(cutSuffix) + matcher.start();
			if (ix != -1 && ix < rawDeweyCallnum.length()) {
				return rawDeweyCallnum.substring(0, ix).trim();
			}
		}
		return removeMoreVolSuffix(rawDeweyCallnum);
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
		String try1 = removeDeweyVolSuffix(rawDeweyCallnum);
		if (!try1.equals(rawDeweyCallnum))
			return try1;
		else
			return removeYearSuffix(rawDeweyCallnum);
	}
	
	
	/**
	 * try to remove volume suffix from call number of unknown type.  It first
	 *  tries it as an LC call number, then as a Dewey call number, then
	 *  just goes for it
	 *   this is called for non-Dewey, non-LC call numbers.
	 * @param rawCallnum
	 * @param callnumScheme - the scheme of this call number, such as SUDOC, ALPHANUM
	 * @return call number without the volume information, or full call number
	 *   if no volume information is present.
	 */
	static String removeNonLCDeweyVolSuffix(String rawCallnum, String callnumScheme) 
	{
		String lopped = rawCallnum;
		if (!callnumScheme.equals("SUDOC"))
			// look for archive type stuff (flat box, etc.)
			lopped = removeMoreVolSuffix(rawCallnum);
		
		if (lopped.equals(rawCallnum)) 
		{
			Matcher matcher = volPattern.matcher(rawCallnum);
			if (!matcher.find()) {
				matcher = volPatternLoose.matcher(rawCallnum);
				if (!matcher.find()) {
					matcher = volPatLetters.matcher(rawCallnum);
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
			// last ditch attempt for SUDOC, which wasn't tried above
//			else if (callnumScheme.equals("SUDOC"))
//				lopped = removeMoreVolSuffix(rawCallnum);
		}
		return lopped;
	}
	
	
	/**
	 * remove volume suffix from call number if it is present.  Call number is
	 *  for a serial, so if the suffix starts with a year, it can be removed.
	 *   this is called for non-Dewey, non-LC call numbers.
	 * @param rawCallnum
	 * @param callnumScheme - the scheme of this call number, such as SUDOC, ALPHANUM
	 * @return call number without the volume information, or full call number
	 *   if no volume information is present.
	 */
	static String removeNonLCDeweySerialVolSuffix(String rawCallnum, String callnumScheme)
	{
		String try1 = removeNonLCDeweyVolSuffix(rawCallnum, callnumScheme);
		if (!try1.equals(rawCallnum))
			return try1;
		else
			return removeYearSuffix(rawCallnum);
	}

	
	/**
	 * go after more localized call number suffixes, such as "box" "carton"
	 *  "series" "index"
	 * @param rawCallnum
	 * @return call number without the volume information, or full call number
	 *   if no volume information is present.
	 */
	static String removeMoreVolSuffix(String rawCallnum) 
	{
		Matcher matcher = moreVolPattern.matcher(rawCallnum);
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
	 * given a list of Strings, return the longest common prefix
	 */
	static String getLongestCommonPrefix(String[] callnums) 
	{
	   	 String result = "";
		 //   where that item contains the common prefix followed by the volume information
	     if (callnums == null || callnums.length == 0) 
	    	 return "";
	     if (callnums.length == 1)
	    	 result = callnums[0];
	     else
	     {
             String commonPrefix = callnums[0];
             for (int i = 1; i < callnums.length; i++)
             {
                 commonPrefix = getCommonPrefix(commonPrefix, callnums[i], compareNoPeriodsOrSpaces);
             }
             result = commonPrefix.trim();
	     }
	     return result;
	}
	
	/**
	 * return the longest prefix string common to both strings by checking each
	 *  character from the beginning for equality.
	 * @param string1 - first string to be compared
	 * @param string2 - second string to be compared
	 * @param comp - comparator used to determine equality of strings
	 * @return longest prefix common to both strings 
	 */
    private static String getCommonPrefix(String string1, String string2, Comparator comp)
    {
        int len1 = string1.length();
        int len2 = string2.length();
        int shortestLen = Math.min(len1, len2);
        int prefixLen = shortestLen;
        for (int i = 0; i < shortestLen; i++)
        {
            if (comp.compare( string1.substring(i, i+1), string2.substring(i, i+1) ) != 0)
            {
                prefixLen = i;
                break;
            }
        }
        return (string1.substring(0, prefixLen));
    }
    
    
    /**
     * Return single value containing he common prefix followed by the 
     * 	 summarized volume information
     * @param TreeMap of LC normalized call numbers (map guaranteed to be in ascending key order)
     */
     String getConflatedLCcallnum(Map<String, Set<String>> lcNormalizedCallnumTree)
     {
    	 String result = null;
    	 //   where that item contains the common prefix followed by the volume information
         if (lcNormalizedCallnumTree == null || lcNormalizedCallnumTree.size() == 0) 
        	 return null;
         Set<String> keys = lcNormalizedCallnumTree.keySet();
         for (String key : keys)
         {
             Set<String> values = lcNormalizedCallnumTree.get(key);
             String valueArr[] = values.toArray(new String[0]);
             if (valueArr.length == 1)
                 result = valueArr[0];
             else
             {
            	 // find prefix common to all the callnums
                 String commonPrefix = valueArr[0];
                 for (int i = 1; i < valueArr.length; i++)
                 {
                     commonPrefix = getCommonPrefix(commonPrefix, valueArr[i], compareNoPeriodsOrSpaces);
                 }
                 commonPrefix.trim();

                 // we now have the common prefix;  use the array values for the "volume" part of the callnums
                 for (int i = 0; i < valueArr.length; i++)
                 {
                     valueArr[i] = valueArr[i].substring(commonPrefix.length());
                 }
                 Arrays.sort(valueArr, new StringNaturalCompare());

                 // make a string of all of the "volume" parts
                 StringBuilder sb = new StringBuilder(commonPrefix);
                 String sep = " ";
                 for (int i = 0; i < valueArr.length; i++)
                 {
                     if (valueArr[i].length() > 0) 
                     {
                         sb.append(sep + valueArr[i]);
                         sep = ",";
                     }
                 }
                 
                 if (sb.length() > 100 || valueArr.length > 10)
// FIXME:  they're not always volumes ...                    	 
                     result = commonPrefix + " (" + valueArr.length + " volumes)";
                 else
                     result = sb.toString();
             }
         }
         return result;
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
	
	
    private static String getBestSingleCallNumber(Map<String, Set<String>> lcCallnumTreeMap)
    {
        if (lcCallnumTreeMap == null || lcCallnumTreeMap.size() == 0)
            return(null);

        String[] bestSet = getLargestCallNumberSubset(lcCallnumTreeMap);
        if (bestSet.length == 0) 
        	return(null);
        
        String result = bestSet[0];
        // replace any character that is not a letter, digit, or period with a space
        result = result.trim().replaceAll("[^A-Za-z0-9.]", " ");
    	// change all multiple whitespace chars to a single space
        result = result.replaceAll("\\s\\s+", " ");
        // remove a space before or after a period
        result = result.replaceAll("\\s?\\.\\s?", ".");
        
        return(result);
    }
    

    /**
     * Given a tree map of LC call numbers (keyed on first 5 chars of callnum),
     *  return a sorted array of the largest set of call numbers assigned to a
     *  single key
     * @param tree map of LC call numbers, keyed on the first 5 chars of the callnum
     *   (a map guaranteed to be in ascending key order)
     * @return a sorted array of LC call numbers that are the largest set of LC 
     *  call numbers associated with a single key in the treemap
     */
    private static String[] getLargestCallNumberSubset(Map<String, Set<String>> lcCallnumTreeMap)
    {
        if (lcCallnumTreeMap == null || lcCallnumTreeMap.size() == 0)
            return(null);
        
        int maxNumValues = 0;
        Set<String> maxValueSet = null;
        int maxNumLCvalues = 0;
        Set<String> maxValidLCvalueSet = null;

        Set<String> keys = lcCallnumTreeMap.keySet();
        for (String key : keys)
        {
            Set<String> values = lcCallnumTreeMap.get(key);
            int numValues = values.size();
            if (numValues > maxNumValues)
            {
                maxNumValues = numValues;
                maxValueSet = values;
            }
            String firstCallnum = values.iterator().next();
            if (org.solrmarc.tools.CallNumUtils.isValidLC(firstCallnum) && values.size() > maxNumLCvalues)
            {
                maxNumLCvalues = numValues;
                maxValidLCvalueSet = values;
            }
        }
        if (maxValidLCvalueSet == null)
            maxValidLCvalueSet = maxValueSet;

        String valueArr[] = maxValidLCvalueSet.toArray(new String[0]);
        Arrays.sort(valueArr, new StringNaturalCompare());
        return(valueArr);
    }
    
	
    /**
     * Extract a set of normalized LC call numbers, as a tree, from a list
     *  of raw LC call numbers
     * @param set of LC call numbers as string
     * @return tree map of call numbers, keyed on the first 5 chars of the callnum
     *   (a map guaranteed to be in ascending key order)
     */
    static Map<String, Set<String>> getLCNormalizedCallnumTree(Set<String> rawLCcallnums)
    {
        Map<String, Set<String>> resultTreeMap = new TreeMap<String, Set<String>>();
        if (rawLCcallnums == null || rawLCcallnums.size() == 0)
        	return(null);
        for (String rawLCcallnum : rawLCcallnums)
        {
            String normalizedCallnum = normalizeLCcallnum(rawLCcallnum);

            // key is first 5 chars, or length of callnum if < 5, of callnum as upper case
            String key = normalizedCallnum.substring(0, Math.min(normalizedCallnum.length(), 5)).toUpperCase();

            if (resultTreeMap.containsKey(key))
            {
                Set<String> set = resultTreeMap.get(key);
                set.add(normalizedCallnum);
                resultTreeMap.put(key, set);
            }
            else
            {
            	// normed comparator
                Set<String> set = new TreeSet<String>(compareNoPeriodsOrSpaces);
                set.add(normalizedCallnum);
                resultTreeMap.put(key, set);
            }
        }
        return(resultTreeMap);
    }
    
    /** compares two strings after removing periods and spaces */
    private static Comparator<String> compareNoPeriodsOrSpaces = new Comparator<String>() 
    {
        public int compare(String s1, String s2)
        {
            String s1Norm = s1.replaceAll("[. ]", "");
            String s2Norm = s2.replaceAll("[. ]", "");
            return s1Norm.compareToIgnoreCase(s2Norm);
        }
    };
    
	
	/**
	 * returns true if the entire call number is a volume suffix
	 * @param rawCallnum
	 */
	static boolean callNumIsVolSuffix(String rawCallnum) {
		if (rawCallnum != null && rawCallnum.length() > 0) {
			Matcher matcher = volPattern.matcher(rawCallnum);
			if (!matcher.find()) {
				matcher = volPatternLoose.matcher(rawCallnum);
				if (!matcher.find()) {
					matcher = volPatLetters.matcher(rawCallnum);
					if (!matcher.find()) {
						matcher = moreVolPattern.matcher(rawCallnum);
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
	 * return a sortable shelving key for the call number
	 * @param rawCallnum - the call number for which a shelfkey is desired
	 * @param scheme - what kind of call number it is
	 * @param recId - record id, for error messages
	 */
	static String getShelfKey(String rawCallnum, String scheme, String recId) {
		String result = "";
		if (rawCallnum.equals(""))
			return result;
		try {
			if (scheme.startsWith("LC"))
				result = org.solrmarc.tools.CallNumUtils.getLCShelfkey(rawCallnum, recId);
			else if (scheme.startsWith("DEWEY"))
				result = org.solrmarc.tools.CallNumUtils.getDeweyShelfKey(rawCallnum);
		}
		catch (Exception e) {
		}
		if (result == null || result.equals("") 
				|| result.equals(rawCallnum))
			result = getNonLCDeweyShelfKey(rawCallnum, recId);
			
		if (result == null || result.equals(""))
			result = rawCallnum;
		
		return result;
	}

	/**
	 * returns a sortable call number.  If it is the call number for a serial,
	 *  the lexical sort will be in ascending order, but will have the most 
	 *  recent volumes first.  If it's not the call number for a serial, the
	 *  sort will be strictly in ascending order.
	 *  
	 * @param rawCallnum
	 * @param loppedCallnum - the call number with volume/part information lopped off
	 * @param scheme - the call number scheme (e.g. LC, DEWEY, SUDOC ...)
	 * @param isSerial - true if the call number is for a serial 
	 * @param recId - record id, for error messages
	 * @return empty string if given empty string or null, o.w. the goods
	 */
	static String getVolumeSortCallnum(String rawCallnum, String loppedCallnum, String scheme, boolean isSerial, String recId) 
	{
		if (rawCallnum == null || rawCallnum.length() == 0)
			return "";

		if (isSerial && !rawCallnum.equals(loppedCallnum)) 
		{  
			// it's a serial and call number has a part/volume suffix
			//   basic call num sorts as shelfkey, volume suffix sorts as reverse key
			String loppedShelfkey = getShelfKey(loppedCallnum, scheme, recId);
			String volSuffix = rawCallnum.substring(loppedCallnum.length()).trim();
			String volSortString = org.solrmarc.tools.CallNumUtils.getReverseShelfKey(org.solrmarc.tools.CallNumUtils.normalizeSuffix(volSuffix));
			return loppedShelfkey + " " + volSortString;
		}
		else
			// regular shelfkey is correct for sort
			return getShelfKey(rawCallnum, scheme, recId);
	}

}
