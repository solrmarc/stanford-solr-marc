package edu.stanford;

import java.util.*;

/**
 * Utility methods for item information Stanford SolrMarc
 *  
 * @author Naomi Dushay
 */
public class ItemUtils {

	/**
	 * Default Constructor: private, so it can't be instantiated by other objects
	 */	
	private ItemUtils(){ }
	
	
// Call Number Methods -------------- Begin ---------------- Call Number Methods    

	/** call number facet values */
	protected static final String DEWEY_TOP_FACET_VAL = "Dewey Classification";
	protected static final String GOV_DOC_TOP_FACET_VAL = "Government Document";
	protected static final String GOV_DOC_BRIT_FACET_VAL = "British";
	protected static final String GOV_DOC_CALIF_FACET_VAL = "California";
	protected static final String GOV_DOC_FED_FACET_VAL = "Federal";
	protected static final String GOV_DOC_INTL_FACET_VAL = "International";
	protected static final String GOV_DOC_UNKNOWN_FACET_VAL = "Other";
	
	/**
	 * @param itemSet - set of Item objects that does NOT include any items
	 *  to be skipped
	 * @return
	 */
//	private static Map<String, Set<String>> getLibLocScheme2Callnums(Set<Item> itemSet) {
//		Map<String, Set<String>> libLocScheme2Callnums = new HashMap();
//		for (Item item : itemSet) {
//
//			String library = item.getLibrary();
//			String homeLoc = item.getHomeLoc();
//			String callnumScheme = item.getCallnumScheme();
//
//			String callnum = item.getCallnum();
//			if (callnum.length() == 0)
//				continue;
//
//			String key = library + ":" + homeLoc;
//			if (callnumScheme.startsWith("LC"))
//				key = key + ":LC";
//			else if (callnumScheme.startsWith("DEWEY"))
//				key = key + ":DEWEY";
//			else
//				key = key + ":" + callnumScheme;
//
//			Set<String> currVal = libLocScheme2Callnums.get(key);
//			if (currVal == null)
//				currVal = new HashSet<String>();
//			currVal.add(callnum);
//			libLocScheme2Callnums.put(key, currVal);
//		}
//		return libLocScheme2Callnums;
//	}

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

	/**
	 * returns a list of any LC call numbers present in the items, normalized
	 * @param itemSet - a Set of Item objects
	 */
	protected static Set<String> getLCcallnums(Set<Item> itemSet) 
	{
		Set<String> result = new HashSet<String>();
		for (Item item : itemSet) {
// FIXME:  shelby locations should be checked for by calling routine??
			if (item.getCallnumScheme().startsWith("LC")
					&& !item.hasIgnoredCallnum() && !item.hasShelbyLoc()) {
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
			if (item.getCallnumScheme().startsWith("DEWEY")
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
		if (item.getCallnumScheme().startsWith("DEWEY"))
			return org.solrmarc.tools.CallNumUtils.addLeadingZeros(item.getCallnum());
		else
			return "";
	}

	
	/**
	 * return the call number with the volume part (if it exists) lopped off the
	 *   end of it.
	 * @param fullCallnum
	 * @param scheme - the call number scheme (e.g. LC, DEWEY, SUDOC)
	 * @param isSerial - true if the call number is for a serial, false o.w.
	 * @return the lopped call number
	 */
	public static String getLoppedCallnum(String fullCallnum, String scheme, boolean isSerial) {
		String loppedCallnum = fullCallnum;
		if (scheme.startsWith("LC"))
			if (isSerial)
				loppedCallnum = edu.stanford.CallNumUtils.removeLCSerialVolSuffix(fullCallnum);
			else
				loppedCallnum = edu.stanford.CallNumUtils.removeLCVolSuffix(fullCallnum);
		else if (scheme.startsWith("DEWEY"))
			if (isSerial)
				loppedCallnum = edu.stanford.CallNumUtils.removeDeweySerialVolSuffix(fullCallnum);
			else
				loppedCallnum = edu.stanford.CallNumUtils.removeDeweyVolSuffix(fullCallnum);
		else 
//TODO: needs to be longest common prefix
			if (isSerial)
				loppedCallnum = edu.stanford.CallNumUtils.removeNonLCDeweySerialVolSuffix(fullCallnum, scheme);
			else
				loppedCallnum = edu.stanford.CallNumUtils.removeNonLCDeweyVolSuffix(fullCallnum, scheme);

		return loppedCallnum;
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
			if (item.hasIgnoredCallnum() || item.isOnline())
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
			if (item.hasIgnoredCallnum() || item.isOnline())
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
	 * Return the barcode of the item with the preferred callnumber.  The 
	 *  algorithm is:
	 *   1.  if there is only one item, choose it.
	 *   2.  Select the item with the longest LC call number.
	 *   3.  if no LC call numbers, select the item with the longest Dewey call number.
	 *   4.  if no LC or Dewey call numbers, select the item with the longest
	 *     SUDOC call number.
	 *   5.  otherwise, select the item with the longest call number.
	 * 
	 * @param itemSet - the set of items from which selection will be made
	 * @return the barcode of the item with the preferred callnumber
	 */
	static String getPreferredItemBarcode(Set<Item> itemSet) {
		int longestLCLen = 0;
		String bestLCBarcode = "";
		int longestDeweyLen = 0;
		String bestDeweyBarcode = "";
		int longestSudocLen = 0;
		String bestSudocBarcode = "";
		int longestOtherLen = 0;
		String bestOtherBarcode = "";
		for (Item item : itemSet) {
			if (!item.hasIgnoredCallnum() && !item.isOnline() && 
					!item.hasShelbyLoc()) {
				int callnumLen = item.getCallnum().length();
				String barcode = item.getBarcode();
				if (item.getCallnumScheme().startsWith("LC")
						&& callnumLen > longestLCLen) {
					longestLCLen = callnumLen;
					bestLCBarcode = barcode;
				}
				else if (item.getCallnumScheme().startsWith("DEWEY")
						&& callnumLen > longestDeweyLen) {
					longestDeweyLen = callnumLen;
					bestDeweyBarcode = barcode;
				}
				else if (item.getCallnumScheme().equals("SUDOC")
						&& callnumLen > longestSudocLen) {
					longestSudocLen = callnumLen;
					bestSudocBarcode = barcode;
				}
				else if (callnumLen > longestOtherLen) {
					longestOtherLen = callnumLen;
					bestOtherBarcode = barcode;
				}
			}
		}
		if (bestLCBarcode.length() > 0)
			return bestLCBarcode;
		else if (bestDeweyBarcode.length() > 0)
			return bestDeweyBarcode;
		else if (bestSudocBarcode.length() > 0)
			return bestSudocBarcode;
		else
			return bestOtherBarcode;
	}
	
	
	/**
	 * 
	 * @param itemSet - set of Item objects
	 * @param isSerial - true if the record is a serial, false otherwise
	 * @param id - record id, used for error messages
	 * @return set of fields from non-skipped items:
	 *   barcode + sep + 
	 *   loppedCallnum + sep + 
	 *   shelfkey + sep +
	 *   reversekey + sep + 
	 *   fullCallnum + sep +
	 *   volSort
	 *   
	 *     where sep is " -|- "
	 */
	static Set<String> getItemDisplay(Set<Item> itemSet, boolean isSerial, String id) 
	{
		Set<String> result = new HashSet<String>();
// TODO: can't implement here until using raw building/location codes		
		return result;
	}

	
}
