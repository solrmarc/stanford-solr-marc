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
