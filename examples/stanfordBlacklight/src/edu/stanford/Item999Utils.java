package edu.stanford;

import java.util.*;

import org.marc4j.marc.*;

/**
 * Utility functions item information in 999 fields for Stanford SolrMarc
 * 
 * <ul> 999 subfields:
 *   <li>a - call num</li>
 *   <li>i - barcode</li>
 *   <li>k - current location</li>
 *   <li>l - home location</li>
 *   <li>m - library code</li>
 *   <li>o - public note</li>
 *   <li>t - item type</li>
 *   <li>w - call num scheme</li>
 *  </ul>
 *  
 * @author Naomi Dushay
 */
public class Item999Utils {

	/**
	 * Default Constructor: private, so it can't be instantiated by other objects
	 */	
	private Item999Utils(){ }
	
	/**
	 * return the barcode from the 999i for item if it doesn't have a skipped location
	 */
	static String getBarcode(DataField f999) 
	{
		if (hasSkippedLoc(f999))
			return null;
		return GenericUtils.getSubfieldTrimmed(f999, 'i');
	}

	
// Location Methods ----------------- Begin ------------------- Location Methods    

	@Deprecated
	private static Set<String> currentLocsToIgnore = new HashSet<String>(10);
	static {
		currentLocsToIgnore.add("BILLED-OD");
		currentLocsToIgnore.add("CHECKEDOUT");
		currentLocsToIgnore.add("CHECKSHELF");
		currentLocsToIgnore.add("INTRANSIT");
		currentLocsToIgnore.add("SOUTH-MEZZ");
	}

	static boolean isCurrLocToIgnore(String loc) {
		return currentLocsToIgnore.contains(loc);
	}
	
	/**
	 * return true if item has a location code indicating it should be skipped.
	 */
	static boolean hasSkippedLoc(DataField f999) {
		if (StanfordIndexer.SKIPPED_LOCS.contains(getHomeLocation(f999))
				|| StanfordIndexer.SKIPPED_LOCS.contains(getCurrentLocation(f999)))
			return true;
		else
			return false;
	}
	
	/**
	 * return true if item has a location code indicating it is online
	 */
	static boolean hasOnlineLoc(DataField f999) {
		if (StanfordIndexer.ONLINE_LOCS.contains(getHomeLocation(f999)) 
				|| StanfordIndexer.ONLINE_LOCS.contains(getCurrentLocation(f999)) )
			return true;
		else
			return false;
	}

	/**
	 * return true if call number should be ignored per the location code
	 */
	static boolean isIgnoredCallNumLoc(DataField f999) {
		if (StanfordIndexer.SHELBY_LOCS.contains(getHomeLocation(f999)) 
				|| StanfordIndexer.SHELBY_LOCS.contains(getCurrentLocation(f999)) )
			return true;
		else
			return false;
	}

	/**
	 * return the building from the 999m for item if it doesn't have a skipped location,
	 * otherwise return empty string.
	 */
	static String getBuilding(DataField f999) 
	{
		if (hasSkippedLoc(f999))
			return "";
		return GenericUtils.getSubfieldTrimmed(f999, 'm');
	}

	/**
	 * return the location from the 999k ("current" location), or if there is
	 * none, from the 999l (that's L) for item  f it doesn't have a skipped location
	 * @deprecated  use home loc and current loc separately
	 */
	static String getLocationFrom999(DataField f999) 
	{
		if (hasSkippedLoc(f999))
			return null;

		// subfield k is the "current location" which is only present if it is
		// different from the "home location" in subfield l (letter L).
		String currLoc = getCurrentLocation(f999);
		if (currLoc.length() > 0 && !isCurrLocToIgnore(currLoc))
			return currLoc;

		return getHomeLocation(f999);
	}

	/**
	 * return the home location from the 999l (that's L) for item,
	 */
	static String getHomeLocation(DataField f999) 
	{
		return GenericUtils.getSubfieldTrimmed(f999, 'l');
	}

	/**
	 * return the current location from the 999k for item,
	 *   if it doesn't have a current location to be ignored
	 */
	private static String getCurrentLocation(DataField f999) 
	{
		return GenericUtils.getSubfieldTrimmed(f999, 'k');
	}

// Location Methods ------------------ End -------------------- Location Methods    

	
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
	 * return the call number type from the 999 (subfield w).
	 */
	static String getCallNumberScheme(DataField f999) 
	{
		return GenericUtils.getSubfieldTrimmed(f999, 'w');
	}

	/**
	 * @param itemList - list of Item objects that does NOT include any items
	 *  to be skipped
	 * @return
	 */
	private static Map<String, Set<String>> getLibLocScheme2Callnums(List<Item> itemList) {
		Map<String, Set<String>> libLocScheme2Callnums = new HashMap();
		for (Item item : itemList) {

			String library = item.getLibrary();
			String homeLoc = item.getHomeLoc();
			String callnumScheme = item.getCallnumScheme();

			String callnum = item.getCallnum();
			if (callnum.length() == 0)
				continue;

			String key = library + ":" + homeLoc;
			if (callnumScheme.startsWith("LC"))
				key = key + ":LC";
			else if (callnumScheme.startsWith("DEWEY"))
				key = key + ":DEWEY";
			else
				key = key + ":" + callnumScheme;

			Set<String> currVal = libLocScheme2Callnums.get(key);
			if (currVal == null)
				currVal = new HashSet<String>();
			currVal.add(callnum);
			libLocScheme2Callnums.put(key, currVal);
		}
		return libLocScheme2Callnums;
	}

	/**
	 * returns a list of any LC call numbers present in the items, normalized
	 * @param itemList - a List of item objects
	 */
	protected static Set<String> getLCcallnums(List<Item> itemList) 
	{
		Set<String> result = new HashSet<String>();
		for (Item item : itemList) {
// FIXME:  shelby locations should checked in calling routine??
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
	 * returns a list of any Dewey call numbers present in the items, normalized
	 *  with leading zeroes as necessary.
	 * @param itemList - a List of item objects
	 */
	static Set<String> getDeweyNormCallnums(List<Item> itemList) 
	{
		Set<String> result = new HashSet<String>();
		for (Item item : itemList) {
// FIXME:  shelby locations should checked in calling routine??
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
	 * return the call number if it is not to be skipped. Otherwise, return 
	 * empty string.
	 */
	static String getNonSkippedCallNum(DataField f999) {
		if (hasSkippedLoc(f999))
			return "";

		String callnum = getCallNum(f999);
		if (!StanfordIndexer.SKIPPED_CALLNUMS.contains(callnum))
			return callnum;
		else
			return "";
	}
		
	/**
	 * return the call number from subfield a
	 */
	static String getCallNum(DataField f999) {
		return GenericUtils.getSubfieldTrimmed(f999, 'a');
	}

	/**
	 * if the item has a Dewey call number, add leading zeroes to normalized it,
	 *  if necessary, and return it. Otherwise, return empty string
	 */
	static String getNormalizedDeweyCallNumber(Item item)
	{
		if (item.getCallnumScheme().startsWith("DEWEY"))
			return org.solrmarc.tools.CallNumUtils.addLeadingZeros(item.getCallnum());
		else
			return "";
	}

// Call Number Methods -------------- End ---------------- Call Number Methods

}
