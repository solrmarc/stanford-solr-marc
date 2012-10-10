package edu.stanford;

import java.util.regex.Pattern;

import org.marc4j.marc.DataField;
import org.solrmarc.tools.CallNumUtils;
import org.solrmarc.tools.MarcUtils;

import edu.stanford.enumValues.CallNumberType;

/**
 * Item object for Stanford SolrMarc
 * @author Naomi Dushay
 */
public class Item {

	/** call number for SUL online items */
	public final static String ECALLNUM = "INTERNET RESOURCE";
	/** location code for online items */
	public final static String ELOC = "INTERNET";
	/** temporary call numbers (in process, on order ..) should start with this prefix */
	public final static String TMP_CALLNUM_PREFIX = "XX";

	/* immutable instance variables */
	private final String recId;
	private final String barcode;
	private final String library;
	private final String itemType;
	private final boolean shouldBeSkipped;
	private final boolean hasGovDocLoc;
	private final boolean isOnline;
	private final boolean hasShelbyLoc;

	/* normal instance variables */
	private CallNumberType callnumType;
	private String homeLoc;
	private String currLoc;
	private String normCallnum;
	private boolean isOnOrder = false;
	private boolean isInProcess = false;
	private boolean hasIgnoredCallnum = false;
	private boolean hasBadLcLaneJackCallnum = false;
	private boolean isMissingLost = false;
	private boolean hasSeparateBrowseCallnum = false;
	/** call number with volume suffix lopped off the end.  Used to remove
	 * noise in search results and in browsing */
	private String loppedCallnum = null;
	/** set when there is a callnumber for browsing different from that in the 999 */
	private String browseCallnum = null;

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
		barcode = MarcUtils.getSubfieldTrimmed(f999, 'i');
		currLoc = MarcUtils.getSubfieldTrimmed(f999, 'k');
		homeLoc = MarcUtils.getSubfieldTrimmed(f999, 'l');
		library = MarcUtils.getSubfieldTrimmed(f999, 'm');
		itemType = MarcUtils.getSubfieldTrimmed(f999, 't');
		String scheme = MarcUtils.getSubfieldTrimmed(f999, 'w');
		String rawCallnum = MarcUtils.getSubfieldTrimmed(f999, 'a');

		if (StanfordIndexer.SKIPPED_LOCS.contains(currLoc)
					|| StanfordIndexer.SKIPPED_LOCS.contains(homeLoc)
					|| itemType.equals("EDI-REMOVE"))
			shouldBeSkipped = true;
		else
			shouldBeSkipped = false;

		if (StanfordIndexer.GOV_DOC_LOCS.contains(currLoc)
				|| StanfordIndexer.GOV_DOC_LOCS.contains(homeLoc) )
			hasGovDocLoc = true;
		else
			hasGovDocLoc = false;

		if (StanfordIndexer.MISSING_LOCS.contains(currLoc)
				|| StanfordIndexer.MISSING_LOCS.contains(homeLoc) )
			isMissingLost = true;
		else
			isMissingLost = false;

		if (StanfordIndexer.SHELBY_LOCS.contains(currLoc)
				|| StanfordIndexer.SHELBY_LOCS.contains(homeLoc) )
			hasShelbyLoc = true;
		else
			hasShelbyLoc = false;

		if (StanfordIndexer.SKIPPED_CALLNUMS.contains(rawCallnum)
				|| rawCallnum.startsWith(ECALLNUM)
				|| rawCallnum.startsWith(TMP_CALLNUM_PREFIX))
			hasIgnoredCallnum = true;
		else
			hasIgnoredCallnum = false;

		assignCallnumType(scheme);
		if (!hasIgnoredCallnum) {
			if (callnumType == CallNumberType.LC || callnumType == CallNumberType.DEWEY)
				normCallnum = CallNumUtils.normalizeCallnum(rawCallnum);
			else
				normCallnum = rawCallnum.trim();
			validateCallnum(recId);
		}
		else
			normCallnum = rawCallnum.trim();

		// isOnline is immutable so must be set here
		if (StanfordIndexer.ONLINE_LOCS.contains(currLoc)
				|| StanfordIndexer.ONLINE_LOCS.contains(homeLoc) //) {
				|| normCallnum.startsWith(ECALLNUM) ) {
			isOnline = true;
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
		return itemType;
	}

	public String getCallnum() {
		return normCallnum;
	}

	public CallNumberType getCallnumType() {
		return callnumType;
	}

	public void setCallnumType(CallNumberType callnumType) {
		this.callnumType = callnumType;
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
	 * @return true if item location indicating it is missing or lost
	 */
	public boolean isMissingOrLost() {
		return isMissingLost;
	}

	/**
	 * @return true if item has a government doc location
	 */
	public boolean hasGovDocLoc() {
		return hasGovDocLoc;
	}

	/**
	 * return true if item has a callnumber or location code indicating it is online
	 */
	public boolean isOnline() {
		if (normCallnum.startsWith(ECALLNUM) || homeLoc.equals(ELOC) || currLoc.equals(ELOC))
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
	 * @return true if call number is Lane (Law) or Jackson (Business) invalid LC callnum
	 */
	public boolean hasBadLcLaneJackCallnum() {
		return hasBadLcLaneJackCallnum;
	}

	/**
	 * @return true if item has a call number from the bib fields
	 */
	public boolean hasSeparateBrowseCallnum() {
		return hasSeparateBrowseCallnum;
	}

	/**
	 * return the call number for browsing - it could be a call number provided
	 *  outside of the item record.   This method will NOT set the lopped call
	 *  number if the raw call number is from the item record and no
	 *  lopped call number has been set yet.
	 */
	public String getBrowseCallnum() {
		if (hasSeparateBrowseCallnum)
			return browseCallnum;
		else
			return loppedCallnum;
	}

	/**
	 * return the call number for browsing - it could be a call number provided
	 *  outside of the item record.   This method will SET the lopped call
	 *  number if the raw call number is from the item record and no
	 *  lopped call number has been set yet.
	 */
	public String getBrowseCallnum(boolean isSerial) {
		if (hasSeparateBrowseCallnum)
			return browseCallnum;
		else
			return getLoppedCallnum(isSerial);
	}

	/**
	 * for resources that have items without browsable call numbers
	 * (SUL INTERNET RESOURCE), we look for a call number in the bib record
	 * fields (050, 090, 086 ...) for browse nearby and for call number facets.
	 * If one is found, this method is used.
	 */
	public void setBrowseCallnum(String callnum) {
		hasSeparateBrowseCallnum = true;
		if (callnumType == CallNumberType.LC || callnumType == CallNumberType.DEWEY)
			browseCallnum = CallNumUtils.normalizeCallnum(callnum);
		else
			browseCallnum = callnum.trim();
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
		loppedCallnum = edu.stanford.CallNumUtils.getLoppedCallnum(normCallnum, callnumType, isSerial);
		if (!loppedCallnum.endsWith(" ...") && !loppedCallnum.equals(normCallnum))
			loppedCallnum = loppedCallnum + " ...";
	}

	/**
	 * sets the private field loppedCallnum to the passed value.  Used when
	 *  lopping must be dictated elsewhere.
	 */
	void setLoppedCallnum(String loppedCallnum) {
		this.loppedCallnum = loppedCallnum;
		if (!loppedCallnum.endsWith(" ...") && !loppedCallnum.equals(normCallnum))
			this.loppedCallnum = loppedCallnum + " ...";
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
		if (loppedShelfkey == null) {
			String skeyCallnum = getBrowseCallnum(isSerial);
			if (skeyCallnum != null && skeyCallnum.length() > 0
				&& !StanfordIndexer.SKIPPED_CALLNUMS.contains(skeyCallnum)
				&& !skeyCallnum.startsWith(ECALLNUM)
				&& !skeyCallnum.startsWith(TMP_CALLNUM_PREFIX) )
				loppedShelfkey = edu.stanford.CallNumUtils.getShelfKey(skeyCallnum, callnumType, recId);
		}
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
		if (loppedShelfkey != null && loppedShelfkey.length() > 0)
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
		if (loppedShelfkey != null && loppedShelfkey.length() > 0)
			callnumVolSort = edu.stanford.CallNumUtils.getVolumeSortCallnum(
				normCallnum, loppedCallnum, loppedShelfkey, callnumType, isSerial, recId);
	}


	/** call numbers must start with a letter or digit */
    private static final Pattern STRANGE_CALLNUM_START_CHARS = Pattern.compile("^\\p{Alnum}");

	/**
	 * output an error message if the call number is supposed to be LC or DEWEY
	 *  but is invalid
	 * @param recId the id of the record, used in error message
	 */
	private void validateCallnum(String recId) {
		if (callnumType == CallNumberType.LC
				&& !CallNumUtils.isValidLC(normCallnum)) {
			if (!library.equals("LANE-MED") && !library.equals("JACKSON") && !library.equals("BUSINESS"))
				System.err.println("record " + recId + " has invalid LC callnumber: " + normCallnum);
			adjustLCCallnumType(recId);
		}
		if (callnumType == CallNumberType.DEWEY
				&& !CallNumUtils.isValidDeweyWithCutter(normCallnum)) {
			System.err.println("record " + recId + " has invalid DEWEY callnumber: " + normCallnum);
			callnumType = CallNumberType.OTHER;
		}
		else if (STRANGE_CALLNUM_START_CHARS.matcher(normCallnum).matches())
			System.err.println("record " + recId + " has strange callnumber: " + normCallnum);
	}

	/**
	 * LC is default call number scheme assigned;  change it if assigned
	 *   incorrectly to a Dewey or ALPHANUM call number.  Called after
	 *   printMsgIfInvalidCallnum has already found invalid LC call number
	 */
	private void adjustLCCallnumType(String id) {
		if (callnumType == CallNumberType.LC) {
			if (CallNumUtils.isValidDeweyWithCutter(normCallnum))
				callnumType = CallNumberType.DEWEY;
			else
			{
//  FIXME:   this is no good if the call number is SUDOC but mislabeled LC ...
				callnumType = CallNumberType.OTHER;
				if (library.equals("LANE-MED") || library.equals("JACKSON") || library.equals("BUSINESS"))
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
		if (normCallnum.startsWith(TMP_CALLNUM_PREFIX))
		{
			if (currLoc.equals("ON-ORDER"))
				isOnOrder = true;
			else if (currLoc.equals("INPROCESS"))
				isInProcess = true;
			else if (shouldBeSkipped || currLoc.equals("LAC") || homeLoc.equals("LAC"))
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

	/**
	 * assign a value to callnumType based on scheme ...
	 *   LCPER --> LC;  DEWEYPER --> DEWEY
	 */
	private void assignCallnumType(String scheme) {
		if (scheme.startsWith("LC"))
			callnumType = CallNumberType.LC;
		else if (scheme.startsWith("DEWEY"))
			callnumType = CallNumberType.DEWEY;
		else if (scheme.equals("SUDOC"))
			callnumType = CallNumberType.SUDOC;
		else
			callnumType = CallNumberType.OTHER;
	}

}
