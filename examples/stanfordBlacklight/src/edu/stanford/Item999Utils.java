package edu.stanford;

import java.util.*;

import org.marc4j.marc.*;

/**
 * Utility functions item information in 999 fields for Stanford SolrNarc
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

// FIXME: lists should populated from a config file
	private static Set<String> currentLocsToIgnore = new HashSet<String>(10);
	static {
		currentLocsToIgnore.add("BILLED-OD");
		currentLocsToIgnore.add("CHECKEDOUT");
		currentLocsToIgnore.add("CHECKSHELF");
		currentLocsToIgnore.add("INTRANSIT");
		currentLocsToIgnore.add("SOUTH-MEZZ");
	}

	/**
	 * a list of locations indicating a 999 field should be skipped, for the
	 * purpose of discoverability.
	 */
	static Set<String> skippedLocs = new HashSet<String>();
	static {
		skippedLocs.add("3FL-REF-S"); // meyer 3rd floor reference shadowed
		skippedLocs.add("ASSMD-LOST"); // Assumed Lost (!skip 999)
		skippedLocs.add("BASECALNUM"); // Serials (!skip 999)
		skippedLocs.add("BENDER-S"); //temporary shadowed location for the Bender Reading Room
		skippedLocs.add("CDPSHADOW"); // All items in CDP which are shadowed
		skippedLocs.add("DISCARD"); // discard shadowed
		skippedLocs.add("DISCARD-NS"); // obsolete location
		skippedLocs.add("EAL-TEMP-S"); // East Asia Library Temporary Shadowed
		skippedLocs.add("EDI"); // In Process (!skip 999)
		skippedLocs.add("E-INPROC-S"); // In Process - shadow (!skip 999)
		skippedLocs.add("E-ORDER-S"); // On Order - shadow (!skip 999)
		skippedLocs.add("E-REQST-S"); // In Process shadow (!skip 999)
		skippedLocs.add("FED-DOCS-S"); //Shadowed location for loading Marcive SLS records
		skippedLocs.add("INSHIPPING"); // (!skip 999)
		skippedLocs.add("INSTRUCTOR"); // Instructor's Copy (!skip 999)
		skippedLocs.add("LOCKSS"); // Locks shadowed copy
		skippedLocs.add("LOST"); // LOST shadowed
		skippedLocs.add("LOST-ASSUM"); // Lost (!skip 999)
		skippedLocs.add("LOST-CLAIM"); // Lost (!skip 999)
		skippedLocs.add("LOST-PAID"); // Lost (!skip 999)
		skippedLocs.add("MANNING"); // Manning Collection: Non-circulating (!skip 999)
		skippedLocs.add("MAPCASES-S"); //Shadowed location for loading Marcive SLS records
		skippedLocs.add("MAPFILE-S"); //Shadowed location for loading Marcive SLS records
		skippedLocs.add("MEDIA-MTXO"); // Media Microtext (Obsolete Loc Code) (!skip 999)
		skippedLocs.add("MISSING"); // Missing (!skip 999)
		skippedLocs.add("MISS-INPRO"); // Missing in-process (!skip 999)
		skippedLocs.add("NEG-PURCH"); // Negative Purchase Decision (!skip 999)
		skippedLocs.add("RESV-URL"); // Internet Reserves (!skip 999)
		skippedLocs.add("SEL-NOTIFY");
		skippedLocs.add("SHADOW"); //Use for all items which are to be shadowed
		skippedLocs.add("SPECA-S"); // Special Collections-- Shadowed Archives
		skippedLocs.add("SPECAX-S"); //Special Collections-- Shadowed Archives, Restricted Access
		skippedLocs.add("SPECB-S"); // Special Collections-- Shadowed Books
		skippedLocs.add("SPECBX-S"); //Special Collections-- Shadowed Books Restricted Access
		skippedLocs.add("SPECM-S"); //Special Collections-- Shadowed Manuscripts
		skippedLocs.add("SPECMED-S"); // Special Collections-- Shadowed Media
		skippedLocs.add("SPECMEDX-S"); //Special Collections-- Shadowed Media, Restricted Access
		skippedLocs.add("SPECMX-S"); //Special Collections-- Shadowed Manuscripts, Restricted Acces
		skippedLocs.add("SSRC-FIC-S"); //Shadowed location for loading Marcive SLS records
		skippedLocs.add("SSRC-SLS"); //Shadowed location for loading Marcive SLS records
		skippedLocs.add("STAFSHADOW"); // All staff items which are shadowed
		skippedLocs.add("SUPERSEDED");
		skippedLocs.add("TECHSHADOW"); // Technical Services Shadowed
		skippedLocs.add("TECH-UNIQ"); // For orderlins with auto callnum (!skip 999)
		skippedLocs.add("WEST-7B"); // Transfer from REF to STK (Obsolete Location Code) (!skip 999)
		skippedLocs.add("WITHDRAWN");
	}

	/**
	 * online location codes that may appear in the 999
	 */
	private static Set<String> onlineLocs = new HashSet<String>();
	static {
		onlineLocs.add("ELECTR-LOC"); // Electronic (!show link only)
		onlineLocs.add("E-RECVD"); // INTERNET (!show link only)
		onlineLocs.add("E-RESV"); // Electronic Reserves (!show link only)
		onlineLocs.add("INTERNET"); // (!show link only)
		onlineLocs.add("KIOSK"); // (!show link only)
		onlineLocs.add("ONLINE-TXT"); // Online (!show link only)
		onlineLocs.add("WORKSTATN"); // Online (!show link only)
	}

	/**
	 * gov doc location codes that may appear in the 999
	 */
	private static Set<String> govDocLocs = new HashSet<String>();
	static {
		govDocLocs.add("BRIT-DOCS");
		govDocLocs.add("CALIF-DOCS");
		govDocLocs.add("FED-DOCS");
		govDocLocs.add("INTL-DOCS");
		govDocLocs.add("SSRC-DOCS");
		govDocLocs.add("SSRC-FICHE");
		govDocLocs.add("SSRC-NWDOC");
	}

	/**
	 * location codes implying call numbers should be ignored
	 */
	private static Set<String> ignoreCallNumLocs = new HashSet<String>();
	static {
		ignoreCallNumLocs.add("SHELBYTITL");
		ignoreCallNumLocs.add("SHELBYSER");
		ignoreCallNumLocs.add("STORBYTITL");
	}


	static boolean isGovDocLoc(String loc) {
		return govDocLocs.contains(loc);
	}
	
	static boolean isCurrLocToIgnore(String loc) {
		return currentLocsToIgnore.contains(loc);
	}
	
	/**
	 * return true if item has a location code indicating it should be skipped.
	 */
	static boolean hasSkippedLoc(DataField f999) {
		String homeLoc = getHomeLocation(f999);
		if (homeLoc != null && skippedLocs.contains(homeLoc))
			return true;
		String currLoc = getCurrentLocation(f999);
		if (currLoc != null && skippedLocs.contains(currLoc))
			return true;

		return false;
	}
	
	/**
	 * return true if item has a location code indicating it is online
	 */
	static boolean hasOnlineLoc(DataField f999) {
		String homeLoc = getHomeLocation(f999);
		if (homeLoc != null && onlineLocs.contains(homeLoc))
			return true;
		String currLoc = getCurrentLocation(f999);
		if (currLoc != null && onlineLocs.contains(currLoc))
			return true;

		return false;
	}

	/**
	 * return true if call number should be ignored per the location code
	 */
	static boolean isIgnoredCallNumLoc(DataField f999) {
		String homeLoc = getHomeLocation(f999);
		if (homeLoc != null && ignoreCallNumLocs.contains(homeLoc))
			return true;
		String currLoc = GenericUtils.getSubfieldTrimmed(f999, 'k');
		if (currLoc != null && ignoreCallNumLocs.contains(currLoc))
			return true;

		return false;
	}

	/**
	 * return the building from the 999m for item if it doesn't have a skipped location
	 */
	static String getBuilding(DataField f999) 
	{
		if (hasSkippedLoc(f999))
			return null;
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
		if (currLoc != null && !isCurrLocToIgnore(currLoc))
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
//		String subk = GenericUtils.getSubfieldTrimmed(f999, 'k');
//		if (subk != null && !isCurrLocToIgnore(subk))
//			return subk;
//		return null;
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
	
// FIXME: lists should populated from a config file
	/**
	 * call numbers to be skipped
	 */
	private static Set<String> skipCallNums = new HashSet<String>(5);
	static {
		skipCallNums.add("INTERNET RESOURCE");
		skipCallNums.add("NO CALL NUMBER");
	}

	/**
	 * return the call number type from the 999 (subfield w).
	 */
	static String getCallNumberScheme(DataField f999) 
	{
		return GenericUtils.getSubfieldTrimmed(f999, 'w');
	}

	/**
	 * @param record
	 * @return
	 */
	private static Map<String, Set<String>> getLibLocScheme2Callnums(List<DataField> list999df) {
		Map<String, Set<String>> libLocScheme2Callnums = new HashMap();
		for (DataField df999 : list999df) {
			// make sure it's not
			if (hasSkippedLoc(df999) || hasOnlineLoc(df999))
				continue;

			String library = getBuilding(df999);
			String homeLoc = getHomeLocation(df999);
			String callnumScheme = getCallNumberScheme(df999);

			String callnum = getCallNum(df999);
			if (callnum == null)
				continue;

			String key = library + ":" + homeLoc;
			if (callnumScheme == null) {
			} else if (callnumScheme.startsWith("LC"))
				key = key + ":LC";
			else if (callnumScheme.startsWith("DEWEY"))
				key = key + ":LC";
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
	 * Use as part of broadest call number facet field. If it is an LC call
	 *  number, then the first letter is returned, otherwise null.
	 */
	static Set<String> getLCCallNumBroadCats(List<DataField> list999df) {
		Set<String> result = new HashSet<String>();

		Set<String> lcSet = getLCforClassification(list999df);
		for (String lc : lcSet) {
			if (lc != null)
				result.add(lc.substring(0, 1).toUpperCase());
		}
		return result;
	}

	/**
	 * get LC call number (portion)s from the bib record: 999
	 * (not currently 050, 051, 090, 099)
	 *  for deriving classifications
	 */
	protected static Set<String> getLCforClassification(List<DataField> list999df) 
	{
		Set<String> result = new HashSet<String>();

		for (DataField df999 : list999df) {
			if (!isIgnoredCallNumLoc(df999)) {
				String callnum = getLCCallNumber(df999);
				if (callnum != null)
					result.add(callnum);
			}
		}
/*
		// look in other LC tags 
		String [] tagsLC = {"050", "051", "090", "099"};
		List<VariableField> listLCfields = record.getVariableFields(tagsLC);
        for (VariableField vf : listLCfields) {
        	String suba = getSubfieldData((DataField) vf, 'a');
        	if (suba != null) {
        		suba = suba.trim();
               	if (isValidLC(suba))
            		result.add(suba);
        	}
        }
*/
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
	 * get Dewey call number (portion)s from the bib record: 999
	 *  (not currently 082, 092, 099)
	 *  for deriving classifications
	 */
	static Set<String> getDeweyforClassification(List<DataField> list999df) 
	{
		Set<String> result = new HashSet<String>();

		for (DataField df999 : list999df) {
			if (!isIgnoredCallNumLoc(df999)) {
				String callnum = getDeweyCallNumber(df999);
				if (callnum != null)
					result.add(callnum);
			}
		}
/*
		// look in other Dewey 
        String [] tagsDewey = {"082", "092", "099"};
		List<VariableField> listDeweyfields = record.getVariableFields(tagsDewey);
        for (VariableField vf : listDeweyfields) {
        	String suba = getSubfieldData((DataField) vf, 'a');
        	if (suba != null) {
        		suba = suba.trim();
	           	if (isValidDewey(suba)) 
	        		result.add(addLeadingZeros(suba));
        	}
        }
*/
		return result;
	}

	/**
	 * return the call number unless item has a skipped location or is an
	 * online item. Otherwise, return null.
	 */
	static String getCallNumExcludingOnline(DataField f999) 
	{
		if (hasOnlineLoc(f999))
			return null;
		return getNonSkippedCallNum(f999);
	}


	/**
	 * return the call number if it is not to be skipped. Otherwise, return null.
	 */
	static String getNonSkippedCallNum(DataField f999) {
		if (hasSkippedLoc(f999))
			return null;

		String callnum = getCallNum(f999);
		if (callnum != null && !skipCallNums.contains(callnum))
			return callnum;

		return null;
	}
		
	/**
	 * return the call number from subfield a
	 */
	static String getCallNum(DataField f999) {
		return GenericUtils.getSubfieldTrimmed(f999, 'a');
	}

	/**
	 * if there is an LC call number in the 999, return it. If Otherwise, return
	 *  null.
	 * N.B.  Government docs are currently lumped in with LC.
	 */
	private static String getLCCallNumber(DataField f999) 
	{
		if (hasSkippedLoc(f999) || hasOnlineLoc(f999) || isIgnoredCallNumLoc(f999))
			return null;

		String callnum = getCallNum(f999);
		String scheme = getCallNumberScheme(f999);
		if (callnum != null && scheme != null 
				&& scheme.startsWith("LC")
				&& org.solrmarc.tools.CallNumUtils.isValidLC(callnum.trim()) )
			return callnum;
		
		return null;
	}

	/**
	 * if there is a Dewey call number in the 999, return it.
	 *  Otherwise, return null
	 */
	static String getDeweyCallNumber(DataField f999)
	{
		if (hasSkippedLoc(f999) || hasOnlineLoc(f999) || isIgnoredCallNumLoc(f999))
			return null;

		String callnum = getCallNum(f999);
		String scheme = getCallNumberScheme(f999);
		if (callnum != null && scheme != null
				&& scheme.startsWith("DEWEY")
				&& org.solrmarc.tools.CallNumUtils.isValidDewey(callnum))
			return org.solrmarc.tools.CallNumUtils.addLeadingZeros(callnum);

		return null;
	}

// Call Number Methods -------------- End ---------------- Call Number Methods

}
