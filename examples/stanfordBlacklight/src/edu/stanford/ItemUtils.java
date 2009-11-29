package edu.stanford;

import java.util.*;

/**
 * Utility functions for item information Stanford SolrMarc
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
