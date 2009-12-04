package edu.stanford;

import java.util.*;

import org.solrmarc.tools.Utils;

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
	 * @return true if the location code is on of the "shelve by" locations
	 */
	static boolean isShelbyLocation(String locCode) {
		if (locCode.equals("SHELBYTITL") || 
				locCode.equals("SHELBYSER") ||
				locCode.equals("STORBYTITL")) 
			return true;
		else
			return false;
	}
	
	/**
	 * returns a list of any LC call numbers present in the items, normalized
	 * @param itemSet - a Set of Item objects
	 */
	protected static Set<String> getLCcallnums(Set<Item> itemSet) 
	{
		Set<String> result = new HashSet<String>();
		for (Item item : itemSet) {
// FIXME:  shelby locations should be checked in calling routine??
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
// FIXME:  shelby locations should be checked in calling routine??
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
	static String getNormalizedDeweyCallNumber(Item item)
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
// this is only called by Item object ... should it be here?
	static String getLoppedCallnum(String fullCallnum, String scheme, boolean isSerial) {
		String loppedCallnum = fullCallnum;
		if (scheme.startsWith("LC"))
			if (isSerial)
				loppedCallnum = CallNumUtils.removeLCSerialVolSuffix(fullCallnum);
			else
				loppedCallnum = CallNumUtils.removeLCVolSuffix(fullCallnum);
		else if (scheme.startsWith("DEWEY"))
			if (isSerial)
				loppedCallnum = CallNumUtils.removeDeweySerialVolSuffix(fullCallnum);
			else
				loppedCallnum = CallNumUtils.removeDeweyVolSuffix(fullCallnum);
		else 
//TODO: needs to be longest common prefix
			if (isSerial)
				loppedCallnum = CallNumUtils.removeNonLCDeweySerialVolSuffix(fullCallnum, scheme);
			else
				loppedCallnum = CallNumUtils.removeNonLCDeweyVolSuffix(fullCallnum, scheme);

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
	 * @param shelfkeys - Set of shelfkey strings
	 * @return a set of reverse shelfkeys corresponding to the shelfkeys passed in
	 */
	static Set<String> getReverseShelfkeys(Set<String> shelfkeys)
	{
		Set<String> result = new HashSet<String>();

		for (String shelfkey : shelfkeys) {
			String reversekey = org.solrmarc.tools.CallNumUtils.getReverseShelfKey(shelfkey);
			if (reversekey != null)
				result.add(reversekey.toLowerCase());
		}

		return result;
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
		
		// FIXME: sep should be globally avail constant (for tests also?)
		String sep = " -|- ";

		// itemList is a list of non-skipped items
		for (Item item : itemSet) {
			if (!item.isOnline()) {
				String building = "";
				String translatedLoc = "";
				String homeLoc = item.getHomeLoc();				

				if (item.isOnline()) {
					building = "Online";
					translatedLoc = "Online";
				} 
				else {
					// map building to short name
					String origBldg = item.getLibrary();
//					if (origBldg.length() > 0)
//						building = Utils.remap(origBldg, findMap(LIBRARY_SHORT_MAP_NAME), true);
					if (building == null || building.length() == 0)
						building = origBldg;
					// location --> mapped
// TODO:  stop mapping location (it will be done in UI)					
//					if (homeLoc.length() > 0)
//						translatedLoc = Utils.remap(homeLoc, findMap(LOCATION_MAP_NAME), true);
				}

				// full call number & lopped call number
				String callnumScheme = item.getCallnumScheme();
				String fullCallnum = item.getCallnum();
				String loppedCallnum = item.getLoppedCallnum(isSerial);

				String volSuffix = null;
				if (loppedCallnum != null && loppedCallnum.length() > 0)
					volSuffix = fullCallnum.substring(loppedCallnum.length()).trim();
				if ((volSuffix == null || volSuffix.length() == 0) 
						&& CallNumUtils.callNumIsVolSuffix(fullCallnum))
					volSuffix = fullCallnum;

				// get sortable keys
				String shelfkey = "";
				String reversekey = "";
				String volSort = "";
				if (!item.isOnline()) {				
					shelfkey = item.getShelfkey(isSerial);
					reversekey = item.getReverseShelfkey(isSerial);
					volSort = item.getCallnumVolSort(isSerial);
				}
				
				// if not online, not in process or on order
				// then deal with shelved by title locations
				String currLoc = item.getCurrLoc();
				if (!currLoc.equals("INPROCESS") && !currLoc.equals("ON-ORDER")
						&& !item.isOnline() && ItemUtils.isShelbyLocation(homeLoc)) {
					if (homeLoc.equals("SHELBYTITL")) {
						translatedLoc = "Serials";
						loppedCallnum = "Shelved by title";
					}
					if (homeLoc.equals("SHELBYSER")) {
						translatedLoc = "Serials";
						loppedCallnum = "Shelved by Series title";
					} 
					else if (homeLoc.equals("STORBYTITL")) {
						translatedLoc = "Storage area";
						loppedCallnum = "Shelved by title";
					}
					fullCallnum = loppedCallnum + " " + volSuffix;
					shelfkey = loppedCallnum.toLowerCase();
					reversekey = org.solrmarc.tools.CallNumUtils.getReverseShelfKey(shelfkey);
					isSerial = true;
					volSort = edu.stanford.CallNumUtils.getVolumeSortCallnum(fullCallnum, loppedCallnum, shelfkey, callnumScheme, isSerial, id);
				}

				// create field
				if (loppedCallnum != null)
	    			result.add( item.getBarcode() + sep + 
//item.getLibrary() + sep + 
//homeLoc + sep +
//item.getCurrLoc() + sep +
// TODO:  add item type (subfield t)
		    					building + sep + 
		    					translatedLoc + sep + 
		    					loppedCallnum + sep + 
		    					shelfkey.toLowerCase() + sep + 
		    					reversekey.toLowerCase() + sep + 
		    					fullCallnum + sep + 
		    					volSort );
			}
		} // end loop through items

		return result;
	}

	
}
