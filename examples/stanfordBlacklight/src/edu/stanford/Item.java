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
	private final String recId;
	private final String barcode;
	private final String library;
	private final String type;
	/** scheme is LC, LCPER, DEWEY, DEWEYPER, SUDOC, ALPHANUM, ASIS ... */
	private String scheme;
	private final boolean shouldBeSkipped;
	private final boolean hasGovDocLoc;
	private final boolean isOnline;
	private final boolean hasShelbyLoc;

	/* normal instance variables */
	private String homeLoc;
	private String currLoc;
	private String normCallnum;
	private boolean isOnOrder = false;
	private boolean isInProcess = false;
	private boolean hasIgnoredCallnum = false;
	private boolean hasBadLcLaneJackCallnum = false;
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
		this.recId = recId;
		barcode = GenericUtils.getSubfieldTrimmed(f999, 'i');
		currLoc = GenericUtils.getSubfieldTrimmed(f999, 'k');
		homeLoc = GenericUtils.getSubfieldTrimmed(f999, 'l');
		library = GenericUtils.getSubfieldTrimmed(f999, 'm');
		type = GenericUtils.getSubfieldTrimmed(f999, 't');
		scheme = GenericUtils.getSubfieldTrimmed(f999, 'w');
		String rawCallnum = GenericUtils.getSubfieldTrimmed(f999, 'a');
				
		if (StanfordIndexer.SKIPPED_LOCS.contains(currLoc)
					|| StanfordIndexer.SKIPPED_LOCS.contains(homeLoc) 
					|| type.equals("EDI-REMOVE"))
			shouldBeSkipped = true;
		else 
			shouldBeSkipped = false;
		
		if (StanfordIndexer.GOV_DOC_LOCS.contains(currLoc) 
				|| StanfordIndexer.GOV_DOC_LOCS.contains(homeLoc) )
			hasGovDocLoc = true;
		else
			hasGovDocLoc = false;
		
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
			if (scheme.startsWith("LC") || scheme.startsWith("DEWEY"))
				normCallnum = CallNumUtils.normalizeCallnum(rawCallnum);
			else
				normCallnum = rawCallnum.trim();
			validateCallnum(recId);
		}
		else
			normCallnum = rawCallnum.trim();

		// isOnline is immutable so must be set here
		if (StanfordIndexer.ONLINE_LOCS.contains(currLoc) 
				|| StanfordIndexer.ONLINE_LOCS.contains(homeLoc) ) {
//				|| normCallnum.equals("INTERNET RESOURCE") ) {
			isOnline = true;
			homeLoc = "INTERNET";
			currLoc = homeLoc;
			normCallnum = "INTERNET RESOURCE";
		}	
		else
			isOnline = false;

		dealWithXXCallnums(recId);
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

	public String getType() {
		return type;
	}

	public String getCallnum() {
		return normCallnum;
	}

	public String getCallnumScheme() {
		return scheme;
	}
	
	/**
	 * @return true if this item has a current or home location indicating it 
	 * should be skipped (e.g. "WITHDRAWN" or a shadowed location) or has
	 * a type of "EDI-REMOVE")
	 */
	public boolean shouldBeSkipped() {
		return shouldBeSkipped;
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
		if (normCallnum.startsWith("INTERNET"))
			return true;
		else
			return isOnline;
	}

	/**
	 * @return true if item is on order
	 */
	public boolean isOnOrder() {
		return isOnOrder;
	}
	
	/**
	 * @return true if item is in process
	 */
	public boolean isInProcess() {
		return isInProcess;
	}
	
	/**
	 * return true if item has a shelby location (current or home)
	 */
	public boolean hasShelbyLoc() {
		return hasShelbyLoc;
	}

	/**
	 * @return true if call number is to be ignored in some contexts
	 *  (e.g. "NO CALL NUMBER" or "XX(blah)")
	 */
	public boolean hasIgnoredCallnum() {
		return hasIgnoredCallnum;
	}

	/**
	 * @return true if call number is Lane or Jackson invalid LC callnum
	 */
	public boolean hasBadLcLaneJackCallnum() {
		return hasBadLcLaneJackCallnum;
	}
	
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
		loppedCallnum = ItemUtils.getLoppedCallnum(normCallnum, scheme, isSerial);
	}

	/**
	 * sets the private field loppedCallnum to the passed value.  Used when
	 *  lopping must be dictated elsewhere.
	 */
	void setLoppedCallnum(String loppedCallnum) {
		this.loppedCallnum = loppedCallnum;
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
		loppedShelfkey = edu.stanford.CallNumUtils.getShelfKey(loppedCallnum, scheme, recId);
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
			loppedShelfkey = getShelfkey(isSerial);
		callnumVolSort = edu.stanford.CallNumUtils.getVolumeSortCallnum(
					normCallnum, loppedCallnum, loppedShelfkey, scheme, isSerial, recId);
	}

	
	
	/** call numbers must start with a letter or digit */
    private static final Pattern STRANGE_CALLNUM_START_CHARS = Pattern.compile("^\\p{Alnum}");
	/**
	 * output an error message if the call number is supposed to be LC or DEWEY
	 *  but is invalid
	 * @param recId the id of the record, used in error message
	 */
	private void validateCallnum(String recId) {
		if (scheme.startsWith("LC") 
				&& !CallNumUtils.isValidLC(normCallnum)) {
			if (!library.equals("LANE-MED") && !library.equals("JACKSON"))
				System.err.println("record " + recId + " has invalid LC callnumber: " + normCallnum);
			adjustLCCallnumScheme(recId);
		}
		if (scheme.startsWith("DEWEY")
				&& !CallNumUtils.isValidDewey(normCallnum)) {
			System.err.println("record " + recId + " has invalid DEWEY callnumber: " + normCallnum);
			scheme = "ALPHANUM";
		}
		else if (STRANGE_CALLNUM_START_CHARS.matcher(normCallnum).matches())
			System.err.println("record " + recId + " has strange callnumber: " + normCallnum);
	}
	
	/**
	 * LC is default call number scheme assigned;  change it if assigned
	 *   incorrectly to a Dewey or ALPHANUM call number.  Called after  
	 *   printMsgIfInvalidCallnum has already found invalid LC call number
	 */
	private void adjustLCCallnumScheme(String id) {
		if (scheme.startsWith("LC")) {
			if (CallNumUtils.isValidDewey(normCallnum))
				scheme = "DEWEY";
			else
			{
				scheme = "ALPHANUM";
				if (library.equals("LANE-MED") || library.equals("JACKSON"))
					hasBadLcLaneJackCallnum = true;
			}
		}
	}

	/**
	 * if item has XX call number
	 *   if home location or current location is INPROCESS or ON-ORDER, do
	 *     the obvious thing
	 *   if no home or current location, item is on order.
	 *   o.w. if the current location isn't shadowed, it is an error;
	 *     print an error message and fake "ON-ORDER"
	 * @param recId - for error message
	 */
	private void dealWithXXCallnums(String recId) {
		if (normCallnum.startsWith("XX"))
		{
			if (currLoc.equals("ON-ORDER"))
				isOnOrder = true;
			else if (currLoc.equals("INPROCESS"))
				isInProcess = true;
			else if (shouldBeSkipped)
				; // we're okay
			else if (currLoc.length() > 0) {
				System.err.println("record " + recId + " has XX callnumber but current location is not ON-ORDER or INPROCESS or shadowy");
				if (homeLoc.equals("ON-ORDER") || homeLoc.equals("INPROCESS")) {
					currLoc = homeLoc;
					homeLoc = "";
					if (currLoc.equals("ON-ORDER"))
						isOnOrder = true;
					else
						isInProcess = true;
				}
				else {
					currLoc = "ON-ORDER";
					isOnOrder = true;
				}
			}
		}
	}

	
}
