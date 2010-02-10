package edu.stanford;

import java.util.*;

import org.solrmarc.tools.Utils;
import org.marc4j.marc.Record;

import edu.stanford.enumValues.CallNumberType;

/**
 * Utility methods for item information Stanford SolrMarc
 *  
 * @author Naomi Dushay
 */
public class ItemUtils {

	/** separator used in item_display field */
	public static final String SEP = " -|- ";
	
	/**
	 * Default Constructor: private, so it can't be instantiated by other objects
	 */	
	private ItemUtils(){ }
	
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
	static void lopItemCallnums(Set<Item> itemSet, Map<String,String> locationMap, boolean isSerial) 
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
				Set<String> loppedCallnums = new HashSet<String>(items.size());
				
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
					String lopped = CallNumUtils.setLopped2LongestComnPfx(items, 4);
					loppedCallnums.add(lopped);
					ensureCorrectEllipsis(loppedCallnums, items);
				}
				else
				{
					for (Item item : items) {
						String fullCallnum = item.getCallnum();
						String lopped = edu.stanford.CallNumUtils.getLoppedCallnum(fullCallnum, item.getCallnumType(), isSerial);
						if (!lopped.equals(fullCallnum)) {
							item.setLoppedCallnum(lopped);
							loppedCallnums.add(lopped);
						}
					}
					ensureCorrectEllipsis(loppedCallnums, items);
				}
			}
		}
	}
	
	/**
	 * ensure we add ellipsis to item's lopped call number when 
	 * when there is a lopped call number in the set of items that is the
	 * same as a full call number of one of the items
	 * SIDE EFFECT:  may change lopped callnums of item objects
	 */
	private static void ensureCorrectEllipsis(Set<String> loppedCallnums, Set<Item> items) 
	{
		if (loppedCallnums.size() > 0) {
			for (Item item : items) {
				String fullCallnum = item.getCallnum();
				if (loppedCallnums.contains(fullCallnum)) {
					item.setLoppedCallnum(fullCallnum + " ...");
				}
			}
		}
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
					!item.isOnline() && !item.hasShelbyLoc() &&
					!item.isMissingOrLost()) {
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
	 * given a set of non-skipped Item objects, return a set of item_display
	 *   field values
	 * @param itemSet - set of Item objects
	 * @param isSerial - true if the record is a serial, false otherwise
	 * @param id - record id, used for error messages
	 * @return set of fields from non-skipped items:
	 *   barcode + SEP + 
	 *   library + SEP + 
	 *   home location + SEP + 
	 *   current location + SEP + 
	 *   item type + SEP + 
	 *   loppedCallnum + SEP + 
	 *   shelfkey + SEP +
	 *   reversekey + SEP + 
	 *   fullCallnum + SEP +
	 *   volSort
	 */
	static Set<String> getItemDisplay(Set<Item> itemSet, boolean isSerial, String id) 
	{
		Set<String> result = new HashSet<String>();

		// itemSet contains all non-skipped items
		for (Item item : itemSet) {
			String homeLoc = item.getHomeLoc();				

			// full call number & lopped call number
			String fullCallnum = item.getCallnum();
			String loppedCallnum = item.getLoppedCallnum(isSerial);

			// get shelflist pieces
			String shelfkey = "";
			String reversekey = "";
			if ( item.hasSeparateBrowseCallnum() 
				 || !(item.hasIgnoredCallnum() || item.hasBadLcLaneJackCallnum() ) ) {
				shelfkey = item.getShelfkey(isSerial);
				reversekey = item.getReverseShelfkey(isSerial);
			}
			// get sortable call number for record view
			String volSort = "";
			if (!item.hasIgnoredCallnum())
				volSort = item.getCallnumVolSort(isSerial);
			
			// deal with shelved by title locations
			if (item.hasShelbyLoc() && 
					!item.isInProcess() && !item.isOnOrder() && 
					!item.isOnline()) {

				// get volume info to show in record view
				String volSuffix = null;
				// ensure we're using a true lopped call number -- if only
				//   one item, this would have been set to full callnum
				CallNumberType callnumType = item.getCallnumType();
				loppedCallnum = CallNumUtils.getLoppedCallnum(fullCallnum, callnumType, isSerial);
				if (loppedCallnum != null && loppedCallnum.length() > 0)
					volSuffix = fullCallnum.substring(loppedCallnum.length()).trim();
				if ( (volSuffix == null || volSuffix.length() == 0) 
						&& CallNumUtils.callNumIsVolSuffix(fullCallnum))
					volSuffix = fullCallnum;

				if (homeLoc.equals("SHELBYTITL")) {
					loppedCallnum = "Shelved by title";
				}
				if (homeLoc.equals("SHELBYSER")) {
					loppedCallnum = "Shelved by Series title";
				} 
				else if (homeLoc.equals("STORBYTITL")) {
					loppedCallnum = "Shelved by title";
				}
				
				fullCallnum = loppedCallnum + " " + volSuffix;
				shelfkey = loppedCallnum.toLowerCase();
				reversekey = org.solrmarc.tools.CallNumUtils.getReverseShelfKey(shelfkey);
				isSerial = true;
				volSort = edu.stanford.CallNumUtils.getVolumeSortCallnum(fullCallnum, loppedCallnum, shelfkey, edu.stanford.enumValues.CallNumberType.OTHER, isSerial, id);
			}
			
			if (shelfkey == null)
				shelfkey = ""; // avoid NPE
			else
				shelfkey = shelfkey.toLowerCase();
			if (reversekey == null)
				reversekey = "";  // avoid NPE
			else
				reversekey = reversekey.toLowerCase();
			
			// lopped callnum in item_display field is left blank when 
			//   the call number is not to be displayed in search results
			String itemDispCallnum = "";
			if (loppedCallnum == null)
				loppedCallnum = ""; // avoid NPE
			if ( ! (item.hasSeparateBrowseCallnum() 
					|| StanfordIndexer.SKIPPED_CALLNUMS.contains(loppedCallnum) 
					|| loppedCallnum.startsWith(Item.ECALLNUM)
					|| loppedCallnum.startsWith(Item.TMP_CALLNUM_PREFIX)
				   ) )
				itemDispCallnum = loppedCallnum;
			
			if ( item.hasSeparateBrowseCallnum() 
					|| fullCallnum.startsWith(Item.TMP_CALLNUM_PREFIX) )
				fullCallnum = "";
				
			// create field
			result.add( item.getBarcode() + SEP + 
						item.getLibrary() + SEP + 
						homeLoc + SEP +
						item.getCurrLoc() + SEP +
						item.getType() + SEP +
    					itemDispCallnum + SEP + 
    					(item.isMissingOrLost() ? "" : shelfkey) + SEP + 
    					(item.isMissingOrLost() ? "" : reversekey) + SEP + 
    					(fullCallnum == null ? "" : fullCallnum) + SEP + 
    					volSort );
		} // end loop through items

		return result;
	}

	
}
