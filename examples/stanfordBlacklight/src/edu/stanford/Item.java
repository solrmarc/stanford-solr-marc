package edu.stanford;

import java.util.regex.Pattern;

import org.marc4j.marc.DataField;
import org.solrmarc.tools.CallNumUtils;

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
	private String callnumScheme;
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
// TODO:  if callnum is XX and no locations, then it is ON-ORDER
		currLoc = GenericUtils.getSubfieldTrimmed(f999, 'k');
		homeLoc = GenericUtils.getSubfieldTrimmed(f999, 'l');
		library = GenericUtils.getSubfieldTrimmed(f999, 'm');
		callnumScheme = GenericUtils.getSubfieldTrimmed(f999, 'w');
		String rawCallnum = GenericUtils.getSubfieldTrimmed(f999, 'a');
		
// SUL loc  INTERNET call num
//   online item
//   not call number facet
//   not shelf key
		
// other online items:
//   if decent call number, yes, call number facet, yes shelf key
		
		if (StanfordIndexer.SKIPPED_LOCS.contains(currLoc)
					|| StanfordIndexer.SKIPPED_LOCS.contains(homeLoc) )
			hasSkippedLoc = true;
		else 
			hasSkippedLoc = false;
		
		if (StanfordIndexer.GOV_DOC_LOCS.contains(currLoc) 
				|| StanfordIndexer.GOV_DOC_LOCS.contains(homeLoc) )
			hasGovDocLoc = true;
		else
			hasGovDocLoc = false;
		
		if (StanfordIndexer.ONLINE_LOCS.contains(currLoc) 
				|| StanfordIndexer.ONLINE_LOCS.contains(homeLoc) 
				|| (library.equals("SUL") && homeLoc.equals("INTERNET")) )
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

		if (!hasIgnoredCallnum) {
			if (callnumScheme.startsWith("LC") || callnumScheme.startsWith("DEWEY"))
				normCallnum = org.solrmarc.tools.CallNumUtils.normalizeCallnum(rawCallnum);
			else
				normCallnum = rawCallnum.trim();
			printMsgIfInvalidCallnum(recId);
		}
		else
			normCallnum = rawCallnum.trim();
//System.err.println("  rec " + recId + " normCallnum is " + normCallnum);
		printMsgIfXXCallnumHasBadLocation(recId);
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
	 * LC is default call number scheme assigned;  change it if assigned
	 *   incorrectly to a Dewey or ALPHANUM call number.  Called after  
	 *   printMsgIfInvalidCallnum has already found invalid LC call number
	 */
	public void adjustLCCallnumScheme(String id) {
// TODO:  need a test
		if (callnumScheme.startsWith("LC")) {
			if (org.solrmarc.tools.CallNumUtils.isValidDewey(normCallnum))
				callnumScheme = "DEWEY";
			else
// FIXME:  practice is ASIS if all letters or all numbers, otherwise ALPHNUM
//  http://www-sul.stanford.edu/depts/ts/tsdepts/cat/docs/unicorn/callno.html
				
// FIXME:  what's the deal with Law call numbers?  See above.				
				callnumScheme = "INCORRECTLC";		
		}
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
// FIXME:  need test
		if (normCallnum.startsWith("INTERNET"))
			return true;
		else
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
		if (reverseShelfkey == null)
			setReverseShelfkey();
		return reverseShelfkey;
	}

	public void setReverseShelfkey() {
		if (shelfkey != null)
			reverseShelfkey = CallNumUtils.getReverseShelfKey(shelfkey);
	}
	
	/** call numbers must start with a letter or digit */
    private static final Pattern STRANGE_CALLNUM_START_CHARS = Pattern.compile("^\\p{Alnum}");
	/**
	 * output an error message if the call number is supposed to be LC or DEWEY
	 *  but is invalid
	 * @param recId the id of the record, used in error message
	 */
	private void printMsgIfInvalidCallnum(String recId) {
		if (callnumScheme.startsWith("LC") 
				&& !org.solrmarc.tools.CallNumUtils.isValidLC(normCallnum)) {
			System.err.println("record " + recId + " has invalid LC callnumber: " + normCallnum);
			adjustLCCallnumScheme(recId);
		}
		if (callnumScheme.startsWith("DEWEY")
				&& !org.solrmarc.tools.CallNumUtils.isValidDewey(normCallnum)) {
			System.err.println("record " + recId + " has invalid DEWEY callnumber: " + normCallnum);
			callnumScheme = "INCORRECTDEWEY";
		}
		else if (STRANGE_CALLNUM_START_CHARS.matcher(normCallnum).matches())
// TODO:  need a test
			System.err.println("record " + recId + " has strange callnumber: " + normCallnum);
	}
	
	/**
	 * output an error message if the call number starts XX, and there is
	 *  a location (home or current) and the current location is not 
	 *  "ON-ORDER" or "INPROCESS"  (or shadowed)
	 * @param recId
	 */
	private void printMsgIfXXCallnumHasBadLocation(String recId) {
// TODO: implement me		
	}
}
