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
	private final String recId_immut;
	private final String barcode_immut;
	private final String library_immut;
	private final String homeLoc_immut;
	private final String currLoc_immut;
	private final String normCallnum_immut;
	/** scheme is LC, LCPER, DEWEY, DEWEYPER, SUDOC, ALPHANUM, ASIS ... */
	private String scheme_immut;
	private final boolean hasSkippedLoc_immut;
	private final boolean hasGovDocLoc_immut;
	private final boolean isOnline_immut;
	private final boolean hasShelbyLoc_immut;
	private final boolean hasIgnoredCallnum_immut;
	

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
		this.recId_immut = recId;
		barcode_immut = GenericUtils.getSubfieldTrimmed(f999, 'i');
// TODO:  if callnum is XX and no locations, then it is ON-ORDER
		currLoc_immut = GenericUtils.getSubfieldTrimmed(f999, 'k');
		homeLoc_immut = GenericUtils.getSubfieldTrimmed(f999, 'l');
		library_immut = GenericUtils.getSubfieldTrimmed(f999, 'm');
		scheme_immut = GenericUtils.getSubfieldTrimmed(f999, 'w');
		String rawCallnum = GenericUtils.getSubfieldTrimmed(f999, 'a');
				
		if (StanfordIndexer.SKIPPED_LOCS.contains(currLoc_immut)
					|| StanfordIndexer.SKIPPED_LOCS.contains(homeLoc_immut) )
			hasSkippedLoc_immut = true;
		else 
			hasSkippedLoc_immut = false;
		
		if (StanfordIndexer.GOV_DOC_LOCS.contains(currLoc_immut) 
				|| StanfordIndexer.GOV_DOC_LOCS.contains(homeLoc_immut) )
			hasGovDocLoc_immut = true;
		else
			hasGovDocLoc_immut = false;
		
		if (StanfordIndexer.ONLINE_LOCS.contains(currLoc_immut) 
				|| StanfordIndexer.ONLINE_LOCS.contains(homeLoc_immut) 
				|| (library_immut.equals("SUL") && homeLoc_immut.equals("INTERNET")) )
			isOnline_immut = true;
		else
			isOnline_immut = false;

		if (StanfordIndexer.SHELBY_LOCS.contains(currLoc_immut) 
				|| StanfordIndexer.SHELBY_LOCS.contains(homeLoc_immut) )
			hasShelbyLoc_immut = true;
		else
			hasShelbyLoc_immut = false;

		if (StanfordIndexer.SKIPPED_CALLNUMS.contains(rawCallnum)
				|| rawCallnum.startsWith("XX"))
			hasIgnoredCallnum_immut = true;
		else
			hasIgnoredCallnum_immut = false;

		if (!hasIgnoredCallnum_immut) {
			if (scheme_immut.startsWith("LC") || scheme_immut.startsWith("DEWEY"))
				normCallnum_immut = org.solrmarc.tools.CallNumUtils.normalizeCallnum(rawCallnum);
			else
				normCallnum_immut = rawCallnum.trim();
			validateCallnum(recId);
		}
		else
			normCallnum_immut = rawCallnum.trim();
//System.err.println("  rec " + recId + " normCallnum is " + normCallnum);
		printMsgIfXXCallnumHasBadLocation(recId);
	}

	public String getBarcode() {
		return barcode_immut;
	}

	public String getLibrary() {
		return library_immut;
	}

	public String getHomeLoc() {
		return homeLoc_immut;
	}

	public String getCurrLoc() {
		return currLoc_immut;
	}

	public String getCallnum() {
		return normCallnum_immut;
	}

	public String getCallnumScheme() {
		return scheme_immut;
	}
	
	/**
	 * LC is default call number scheme assigned;  change it if assigned
	 *   incorrectly to a Dewey or ALPHANUM call number.  Called after  
	 *   printMsgIfInvalidCallnum has already found invalid LC call number
	 */
	public void adjustLCCallnumScheme(String id) {
// TODO:  need a test
		if (scheme_immut.startsWith("LC")) {
			if (org.solrmarc.tools.CallNumUtils.isValidDewey(normCallnum_immut))
				scheme_immut = "DEWEY";
			else
// FIXME:  practice is ASIS if all letters or all numbers, otherwise ALPHNUM
//  http://www-sul.stanford.edu/depts/ts/tsdepts/cat/docs/unicorn/callno.html
				
// FIXME:  what's the deal with Law call numbers?  See above.				
				scheme_immut = "INCORRECTLC";		
		}
	}

	/**
	 * @return true if this item has a current or home location indicating it 
	 * should be skipped (e.g. "WITHDRAWN" or a shadowed location)
	 */
	public boolean hasSkipLocation() {
		return hasSkippedLoc_immut;
	}
	
	/**
	 * @return true if item has a government doc location
	 */
	public boolean hasGovDocLoc() {
		return hasGovDocLoc_immut;
	}
	
	/**
	 * return true if item has a location code indicating it is online
	 */
	public boolean isOnline() {
// FIXME:  need test
		if (normCallnum_immut.startsWith("INTERNET"))
			return true;
		else
			return isOnline_immut;
	}

	/**
	 * return true if item has a shelby location (current or home)
	 */
	public boolean hasShelbyLoc() {
		return hasShelbyLoc_immut;
	}

	/**
	 * @return true if call number is to be ignored (e.g. "NO CALL NUMBER"
	 *  or XX(blah)")
	 */
	public boolean hasIgnoredCallnum() {
		return hasIgnoredCallnum_immut;
	}

	
	/** call number with volume suffix lopped off the end.  Used to remove
	 * noise in search results and in browsing */
	private String loppedCallnum = null;

	/** sortable version of lopped call number */
	private String loppedShelfkey = null;

	/** reverse sorted version of loppedShelfkey - the last call number shall
	 * be first, etc. */
	private String reverseLoppedShelfkey = null;

	/** sortable full call number, where, for serials, any volume suffix will sort 
	 * in descending order.  Non-serial volumes will sort in ascending order. */
	private String callnumVolSort = null;

	
	/**
	 * get the lopped call number (any volume suffix is lopped off the end.) 
	 * This will remove noise in search results and in browsing.
	 * @param isSerial - true if item is for a serial.  Used to determine if 
	 *   year suffix should be lopped in addition to regular volume lopping.
	 */
	public String getLoppedCallnum(boolean isSerial) {
		if (loppedCallnum == null)
			setLoppedCallnum(isSerial);
		return loppedCallnum;
	}
	
	/**
	 * sets the private field loppedCallnum to contain the call number without
	 *  any volume suffix information.  
	 * @param isSerial - true if item is for a serial.  Used to determine if 
	 *   year suffix should be lopped in addition to regular volume lopping.
	 */
	private void setLoppedCallnum(boolean isSerial) {
		loppedCallnum = ItemUtils.getLoppedCallnum(normCallnum_immut, scheme_immut, isSerial);
	}

	
	/**
	 * get the sortable version of the lopped call number.
	 * @param isSerial - true if item is for a serial.
	 */
	public String getShelfkey(boolean isSerial) {
		if (loppedShelfkey == null)
			setShelfkey(isSerial);
		return loppedShelfkey;
	}

	/**
	 * sets the private field loppedShelfkey (and loppedCallnum if it's not
	 *  already set).  loppedShelfkey will contain the sortable version of the 
	 *  lopped call number
	 * @param isSerial - true if item is for a serial.
	 */
	private void setShelfkey(boolean isSerial) {
		if (loppedCallnum == null)
			setLoppedCallnum(isSerial);
		loppedShelfkey = edu.stanford.CallNumUtils.getShelfKey(loppedCallnum, scheme_immut, recId_immut);
	}

	
	/**
	 * get the reverse sortable version of the lopped call number.
	 * @param isSerial - true if item is for a serial.
	 */
	public String getReverseShelfkey(boolean isSerial) {
		if (reverseLoppedShelfkey == null)
			setReverseShelfkey(isSerial);
		return reverseLoppedShelfkey;
	}

	/**
	 * sets the private field reverseLoppedShelfkey (and loppedShelfkey and 
	 *  loppedCallnum if they're not already set).  reverseLoppedShelfkey will 
	 *  contain the reverse sortable version of the lopped call number.
	 * @param isSerial - true if item is for a serial.
	 */
	private void setReverseShelfkey(boolean isSerial) {
		if (loppedShelfkey == null)
			setShelfkey(isSerial);
		reverseLoppedShelfkey = CallNumUtils.getReverseShelfKey(loppedShelfkey);
	}

	
	/**
	 * get the sortable full call number, where, for serials, any volume suffix 
	 * will sort in descending order.  Non-serial volumes will sort in ascending 
	 * order.
	 * @param isSerial - true if item is for a serial.
	 */
	public String getCallnumVolSort(boolean isSerial) {
		if (callnumVolSort == null)
			setCallnumVolSort(isSerial);
		return callnumVolSort;
	}

	/**
	 * sets the private field callnumVolSort (and loppedShelfkey and 
	 * loppedCallnum if they're not already set.)  callnumVolSort will contain 
	 * the sortable full call number, where, for serials, any volume suffix 
	 * will sort in descending order.  
	 * @param isSerial - true if item is for a serial.
	 */
	private void setCallnumVolSort(boolean isSerial) {
		if (loppedShelfkey == null)
			// note:  setting loppedShelfkey will also set loppedCallnum
			loppedShelfkey = edu.stanford.CallNumUtils.getShelfKey(normCallnum_immut, scheme_immut, recId_immut);
		callnumVolSort = edu.stanford.CallNumUtils.getVolumeSortCallnum(
					normCallnum_immut, loppedCallnum, loppedShelfkey, scheme_immut, isSerial, recId_immut);
	}

	
	
	/** call numbers must start with a letter or digit */
    private static final Pattern STRANGE_CALLNUM_START_CHARS = Pattern.compile("^\\p{Alnum}");
	/**
	 * output an error message if the call number is supposed to be LC or DEWEY
	 *  but is invalid
	 * @param recId the id of the record, used in error message
	 */
	private void validateCallnum(String recId) {
		if (scheme_immut.startsWith("LC") 
				&& !org.solrmarc.tools.CallNumUtils.isValidLC(normCallnum_immut)) {
			System.err.println("record " + recId + " has invalid LC callnumber: " + normCallnum_immut);
			adjustLCCallnumScheme(recId);
		}
		if (scheme_immut.startsWith("DEWEY")
				&& !org.solrmarc.tools.CallNumUtils.isValidDewey(normCallnum_immut)) {
			System.err.println("record " + recId + " has invalid DEWEY callnumber: " + normCallnum_immut);
			scheme_immut = "INCORRECTDEWEY";
		}
		else if (STRANGE_CALLNUM_START_CHARS.matcher(normCallnum_immut).matches())
// TODO:  need a test
			System.err.println("record " + recId + " has strange callnumber: " + normCallnum_immut);
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
