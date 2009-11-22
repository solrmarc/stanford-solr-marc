package edu.stanford;

import org.marc4j.marc.DataField;

/**
 * Item object for Stanford SolrMarc
 * @author Naomi Dushay
 */
public class Item {

	/* immutable instance variables */
	private final String barcode;
	private final String library;
	private final String homeLoc;
	private final String currLoc;
	private final String normCallnum;
	/** callnumScheme is LC, LCPER, DEWEY, DEWEYPER, SUDOC, ALPHANUM, ASIS ... */
	private final String callnumScheme;
	private final boolean hasSkippedLoc;
	private final boolean hasGovDocLoc;
	private final boolean isOnline;
	private final boolean hasShelbyLoc;
	private final boolean hasIgnoredCallnum;
	

	/**
	 * initialize object from 999 DataField, which has the following subfields
	 * <ul>
	 *   <li>a - call num</li>
	 *   <li>i - barcode</li>
	 *   <li>k - current location</li>
	 *   <li>l - home location</li>
	 *   <li>m - library code</li>
	 *   <li>o - public note</li>
	 *   <li>t - item type</li>
	 *   <li>w - call num scheme</li>
	 *  </ul>
	 */
	public Item(DataField f999, String recId) {
		// set all the immutable variables
		barcode = GenericUtils.getSubfieldTrimmed(f999, 'i');
		currLoc = GenericUtils.getSubfieldTrimmed(f999, 'k');
		homeLoc = GenericUtils.getSubfieldTrimmed(f999, 'l');
		library = GenericUtils.getSubfieldTrimmed(f999, 'm');
		callnumScheme = GenericUtils.getSubfieldTrimmed(f999, 'w');
		String rawCallnum = GenericUtils.getSubfieldTrimmed(f999, 'a');
		if (callnumScheme.startsWith("LC") || callnumScheme.startsWith("DEWEY"))
			normCallnum = org.solrmarc.tools.CallNumUtils.normalizeCallnum(rawCallnum);
		else
			normCallnum = rawCallnum;
		printMsgIfInvalidCallnum(recId);

		if (StanfordIndexer.SKIPPED_LOCS.contains(currLoc)
					|| StanfordIndexer.SKIPPED_LOCS.contains(homeLoc))
			hasSkippedLoc = true;
		else 
			hasSkippedLoc = false;
		
		if (StanfordIndexer.GOV_DOC_LOCS.contains(currLoc) 
				|| StanfordIndexer.GOV_DOC_LOCS.contains(homeLoc) )
			hasGovDocLoc = true;
		else
			hasGovDocLoc = false;
		
		if (StanfordIndexer.ONLINE_LOCS.contains(currLoc) 
				|| StanfordIndexer.ONLINE_LOCS.contains(homeLoc) )
			isOnline = true;
		else
			isOnline = false;

		if (StanfordIndexer.SHELBY_LOCS.contains(currLoc) 
				|| StanfordIndexer.SHELBY_LOCS.contains(homeLoc) )
			hasShelbyLoc = true;
		else
			hasShelbyLoc = false;

		if (StanfordIndexer.SKIPPED_CALLNUMS.contains(rawCallnum)
				|| rawCallnum.startsWith("XX"))
			hasIgnoredCallnum = true;
		else
			hasIgnoredCallnum = false;
	}

	public String getBarcode() {
		return barcode;
	}

	public String getLibrary() {
		return library;
	}

	public String getHomeLoc() {
		return homeLoc;
	}

	public String getCurrLoc() {
		return currLoc;
	}

	public String getCallnum() {
		return normCallnum;
	}

	public String getCallnumScheme() {
		return callnumScheme;
	}

	/**
	 * @return true if this item has a current or home location indicating it 
	 * should be skipped (e.g. "WITHDRAWN" or a shadowed location)
	 */
	public boolean hasSkipLocation() {
		return hasSkippedLoc;
	}
	
	/**
	 * @return true if item has a government doc location
	 */
	public boolean hasGovDocLoc() {
		return hasGovDocLoc;
	}
	
	/**
	 * return true if item has a location code indicating it is online
	 */
	public boolean isOnline() {
		return isOnline;
	}

	/**
	 * return true if item has a shelby location (current or home)
	 */
	public boolean hasShelbyLoc() {
		return hasShelbyLoc;
	}

	/**
	 * @return true if call number is to be ignored (e.g. "NO CALL NUMBER"
	 *  or XX(blah)")
	 */
	public boolean hasIgnoredCallnum() {
		return hasIgnoredCallnum;
	}

	
//--  what's below must be computed with some record context ----	
	
	// these should be null to show when they haven't yet been assigned
	private String shelfkey = null;
	private String reverseShelfkey = null;
	private String callnumLopped = null;
	private String callnumVolSort = null;


	public String getCallnumLopped() {
		return callnumLopped;
	}

	public void setCallnumLopped(String callnumLopped) {
		this.callnumLopped = callnumLopped;
	}

	public String getCallnumVolSort() {
		return callnumVolSort;
	}

	public void setCallnumVolSort(String callnumVolSort) {
		this.callnumVolSort = callnumVolSort;
	}

	public String getShelfkey() {
		return shelfkey;
	}

	public void setShelfkey(String shelfkey) {
		this.shelfkey = shelfkey;
	}

	public String getReverseShelfkey() {
		return reverseShelfkey;
	}

	public void setReverseShelfkey(String reverseShelfkey) {
		this.reverseShelfkey = reverseShelfkey;
	}
	
	/**
	 * output an error message if the call number is supposed to be LC or DEWEY
	 *  but is invalid
	 * @param recId the id of the record, used in error message
	 */
	private void printMsgIfInvalidCallnum(String recId) {
		if (callnumScheme.startsWith("LC")) {
			if (!org.solrmarc.tools.CallNumUtils.isValidLC(normCallnum))
				System.err.println("record " + recId + " has invalid LC callnumber: " + normCallnum);
		}
		else if (callnumScheme.startsWith("DEWEY")) {
			if (!org.solrmarc.tools.CallNumUtils.isValidDewey(normCallnum))
				System.err.println("record " + recId + " has invalid dewey callnumber: " + normCallnum);
			
		}
	}
}
