package edu.stanford;

import java.util.*;

import org.solrmarc.tools.Utils;

import edu.stanford.enumValues.CallNumberType;

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
	 * lop call numbers in Item objects if there is more than one Item with
	 *  the same library-translated home loc-scheme combination; otherwise
	 *  don't lop the call numbers.  (Don't lop skipped callnumbers)
	 * SIDE EFFECT: changes state of passed Item objects to reflect lopping
	 *  as indicated
	 * @param itemSet - set of Item objects that does NOT include any items
	 *  to be skipped
	 * @param locationMap - mapping from raw locations to translated location
	 */
	static void lopItemCallnums(Set<Item> itemSet, Map<String,String> locationMap) 
	{
		if (itemSet.size() == 0)
			return;
		if (itemSet.size() == 1) {
			Item[] array = new Item[1];
			Item item = itemSet.toArray(array)[0];
			item.setLoppedCallnum(item.getCallnum());
		}
		else {
			// set up data structure grouping items by lib/loc/callnum scheme
			Map<String, Set<Item>> libLocScheme2Items = new HashMap<String, Set<Item>>();
			for (Item item : itemSet) {
				if (item.hasIgnoredCallnum())
					continue;
				String library = item.getLibrary();
				String homeLoc = item.getHomeLoc();
				String translatedHomeLoc = Utils.remap(homeLoc, locationMap, true);
				String callnumTypePrefix = item.getCallnumType().getPrefix();

				String key = library + ":" + translatedHomeLoc + ":" + callnumTypePrefix;
				
				Set<Item> currVal = libLocScheme2Items.get(key);
				if (currVal == null)
					currVal = new HashSet<Item>();
				currVal.add(item);
				libLocScheme2Items.put(key, currVal);
			}
			
			// process Item objects as necessary
			for (String key : libLocScheme2Items.keySet()) {
				Set<Item> items = libLocScheme2Items.get(key);
				if (items.size() == 1) {
					// single items are not lopped
					Item[] array = new Item[1];
					Item item = items.toArray(array)[0];
					item.setLoppedCallnum(item.getCallnum());
				}
				else if (!key.contains(":" + CallNumberType.LC.getPrefix()) &&
						!key.contains(":" + CallNumberType.DEWEY.getPrefix()) ) {
					// non-LC, non-Dewey call numbers are lopped longest common
					//  prefix
					CallNumUtils.setLopped2LongestComnPfx(items, 4);
				}
				// otherwise, normal lopping will occur
			}
		}
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
	 * return the call number with the volume part (if it exists) lopped off the
	 *   end of it.
	 * @param fullCallnum
	 * @param callnumType - the call number type (e.g. LC, DEWEY, SUDOC)
	 * @param isSerial - true if the call number is for a serial, false o.w.
	 * @return the lopped call number
	 */
	public static String getLoppedCallnum(String fullCallnum, CallNumberType callnumType, boolean isSerial) {
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
			if (!item.hasIgnoredCallnum() && !item.hasBadLcLaneJackCallnum() &&
					!item.isOnline() && !item.hasShelbyLoc()) {
				int callnumLen = item.getCallnum().length();
				String barcode = item.getBarcode();
				if (item.getCallnumType() == CallNumberType.LC
						&& callnumLen > longestLCLen) {
					longestLCLen = callnumLen;
					bestLCBarcode = barcode;
				}
				else if (item.getCallnumType() == CallNumberType.DEWEY
						&& callnumLen > longestDeweyLen) {
					longestDeweyLen = callnumLen;
					bestDeweyBarcode = barcode;
				}
				else if (item.getCallnumType() == CallNumberType.SUDOC
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
