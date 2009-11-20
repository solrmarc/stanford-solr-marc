package edu.stanford;

import java.io.*;
import java.text.ParseException;
import java.util.*;
import java.util.regex.Pattern;

import org.marc4j.marc.*;
//could import static, but this seems clearer
import org.solrmarc.tools.Utils;

import edu.stanford.enumValues.*;

/**
 * Stanford custom methods for SolrMarc
 * @author Naomi Dushay
 */
public class StanfordIndexer extends org.solrmarc.index.SolrIndexer
{
	/** name of map used to translate raw library code to short display value */
	private String LIBRARY_SHORT_MAP_NAME = null;
	/** name of map used to translate raw location code to display value */
	@Deprecated
	private String LOCATION_MAP_NAME = null;
	
	/** locations indicating item should not be displayed */
	static List<String> SKIPPED_LOCS = null;
	/** locations indicating item is online */
	static List<String> ONLINE_LOCS = null;
	/** locations indicating item is a government document */
	static List<String> GOV_DOC_LOCS = null;
	/** locations indicating item is not shelved by callnum */
	static List<String> SHELBY_LOCS = null;
	/** call numbers that should not be displayed */
	static List<String> SKIPPED_CALLNUMS = null;

	/**
	 * Default constructor
     * @param indexingPropsFile the name of xxx_index.properties file mapping 
     *  solr field names to values in the marc records
     * @param propertyDirs - array of directories holding properties files
	 */
	public StanfordIndexer(String indexingPropsFile, String[] propertyDirs)
    		throws FileNotFoundException, IOException, ParseException 
    {
		super(indexingPropsFile, propertyDirs);
        try
        {
        	LIBRARY_SHORT_MAP_NAME = loadTranslationMap(null, "library_short_map.properties");
        	LOCATION_MAP_NAME = loadTranslationMap(null, "location_map.properties");
        }
        catch (IllegalArgumentException e)
        {
			e.printStackTrace();
		}
        
        SKIPPED_LOCS = GenericUtils.loadPropertiesList(propertyDirs, "locations_skipped_list.properties");
        ONLINE_LOCS = GenericUtils.loadPropertiesList(propertyDirs, "locations_online_list.properties");
        GOV_DOC_LOCS = GenericUtils.loadPropertiesList(propertyDirs, "gov_doc_location_list.properties");
        SHELBY_LOCS = GenericUtils.loadPropertiesList(propertyDirs, "locations_shelby_list.properties");
        SKIPPED_CALLNUMS = GenericUtils.loadPropertiesList(propertyDirs, "callnums_skipped_list.properties");
	}

	// variables used in more than one method
	/** the id of the record - used for error messages in addition to id field */
	String id = null;
	/** the formats of the record - used for item_display in addition to format field */
	Set<String> formats = new HashSet<String>();
	/** sfxUrls are used for access_method in addition to sfxUrl field */
	Set<String> sfxUrls = new HashSet<String>();
	/** fullTextUrls are used for access_method in addition to fullTextUrl field */
	Set<String> fullTextUrls = new HashSet<String>();
	/** buildings are used for topics due to weird law 655s */
	Set<String> buildings = new HashSet<String>();
	/** shelfkeys are used for reverse_shelfkeys */
	Set<String> shelfkeys = new HashSet<String>();
	/** govDocCats are used for top level call number facet */
	Set<String> govDocCats = new HashSet<String>();

	/** 008 field */
	ControlField cf008 = null;
	/** date008 is bytes 7-10 (0 based index) in 008 field */
	String date008 = null;
	/** Set of 020 subfield a */
	Set<String> f020suba = new HashSet<String>();
	/** Set of 020 subfield z */
	Set<String> f020subz = new HashSet<String>();
	/** Set of 655 subfield a */
	Set<String> f655suba = new HashSet<String>();
	/** Set of 956 subfield u */
	Set<String> f956subu = new HashSet<String>();

	/** all 999 fields as a List of DataField objects */
	List<DataField> list999df = null;

    
	/**
	 * Method from superclass allowing processing that can be done once per
	 * record, rather than repeatedly for several indexing specifications,
	 * especially custom methods. The default version does nothing.
	 * @param record - The MARC record that is being indexed.
	 */
	@SuppressWarnings("unchecked")
	protected void perRecordInit(Record record) {
		cf008 = (ControlField) record.getVariableField("008");
		if (cf008 != null)
			date008 = cf008.getData().substring(7, 11);
		else
			date008 = null;
		f020suba = getFieldList(record, "020a");
		f020subz = getFieldList(record, "020z");
		f655suba = getFieldList(record, "655a");
		f956subu = getFieldList(record, "956u");
		list999df = (List<DataField>) record.getVariableFields("999");

		setId(record);
		setFormats(record);
		setSFXUrls(); // doesn't need record b/c they come from 999
		setFullTextUrls(record);
		setBuildings(record);
		// setShelfkeys(record);
		setShelfkeysOrig(record);
		setGovDocCats(record);		
	}

// Id Methods  -------------------- Begin --------------------------- Id Methods

	/**
	 * Get local id for the Marc record.
	 */
	public String getId(final Record record) {
		return id;
	}

	/**
	 * Assign id of record to be the ckey. Our ckeys are in 001 subfield a. 
	 * Marc4j is unhappy with subfields in a control field so this is a kludge 
	 * work around.
	 */
	private void setId(final Record record) 
	{
		id = null;
		ControlField fld = (ControlField) record.getVariableField("001");
		if (fld != null && fld.getData() != null) 
		{
			String rawVal = fld.getData();
			if (rawVal.startsWith("a"))
				id = rawVal.substring(1);
		}
	}

// Id Methods  -------------------- Begin --------------------------- Id Methods


// Format Methods  --------------- Begin ------------------------ Format Methods

	/**
	 * @return Set of strings containing format values for the resource
	 */
	public Set<String> getFormats(final Record record) 
	{
		return formats;
	}

	/**
	 * Assign formats per algorithm and marc bib record
	 */
	@SuppressWarnings("unchecked")
	private void setFormats(final Record record) 
	{
		formats = new HashSet<String>();

		// As of July 28, 2008, algorithms for formats are currently in email
		// message from Vitus Tang to Naomi Dushay, cc Phil Schreur, Margaret
		// Hughes, and Jennifer Vine dated July 23, 2008.

		// Note: MARC21 documentation refers to char numbers that are 0 based,
		// just like java string indexes, so char "06" is at index 6, and is
		// the seventh character of the field

		// assign formats based on leader chars 06, 07 and chars in 008
		String leaderStr = record.getLeader().toString();
		char leaderChar07 = leaderStr.charAt(7);
		char leaderChar06 = leaderStr.charAt(6);
		switch (leaderChar06) {
		case 'a':
			if (leaderChar07 == 'a' || leaderChar07 == 'm')
				formats.add(Format.BOOK.toString());
			break;
		case 'b':
		case 'p':
			formats.add(Format.MANUSCRIPT_ARCHIVE.toString());
			break;
		case 'c':
		case 'd':
			formats.add(Format.MUSIC_SCORE.toString());
			break;
		case 'e':
		case 'f':
			formats.add(Format.MAP_GLOBE.toString());
			break;
		case 'g':
			// look for m or v in 008 field, char 33 (count starts at 0)
			if (cf008 != null && cf008.find("^.{33}[mv]"))
				formats.add(Format.VIDEO.toString());
			break;
		case 'i':
			formats.add(Format.SOUND_RECORDING.toString());
			break;
		case 'j':
			formats.add(Format.MUSIC_RECORDING.toString());
			break;
		case 'k':
    		// look for i, k, p, s or t in 008 field, char 33 (count starts at 0)
			if (cf008 != null && cf008.find("^.{33}[ikpst]"))
				formats.add(Format.IMAGE.toString());
			break;
		case 'm':
			// look for a in 008 field, char 26 (count starts at 0)
			if (cf008 != null && cf008.find("^.*{26}a"))
				formats.add(Format.COMPUTER_FILE.toString());
			break;
		case 'o': // instructional kit
			formats.add(Format.OTHER.toString());
			break;
		case 'r': // object
			formats.add(Format.OTHER.toString());
			break;
		case 't':
			if (leaderChar07 == 'a' || leaderChar07 == 'm')
				formats.add(Format.BOOK.toString());
			break;
		} // end switch

		if (formats.isEmpty() || formats.size() == 0) {
			// look for serial publications - leader/07 s
			if (leaderChar07 == 's') {
				if (cf008 != null) {
					char c21 = ((ControlField) cf008).getData().charAt(21);
					switch (c21) {
					case 'd': // updating database (ignore)
						break;
					case 'l': // updating looseleaf (ignore)
						break;
					case 'm': // monographic series
						formats.add(Format.BOOK.toString());
						break;
					case 'n':
						formats.add(Format.NEWSPAPER.toString());
						break;
					case 'p':
						formats.add(Format.JOURNAL_PERIODICAL.toString());
						break;
					case 'w': // web site
						break;
					}
				}
			}
		}

		// look for serial publications 006/00 s
		if (formats.isEmpty() || formats.size() == 0) {
			VariableField f006 = record.getVariableField("006");
			if (f006 != null && f006.find("^[s]")) {
				char c04 = ((ControlField) f006).getData().charAt(4);
				switch (c04) {
				case 'd': // updating database (ignore)
					break;
				case 'l': // updating looseleaf (ignore)
					break;
				case 'm': // monographic series
					formats.add(Format.BOOK.toString());
					break;
				case 'n':
					formats.add(Format.NEWSPAPER.toString());
					break;
				case 'p':
					formats.add(Format.JOURNAL_PERIODICAL.toString());
					break;
				case 'w': // web site
					break;
				case ' ':
					formats.add(Format.JOURNAL_PERIODICAL.toString());
				}
			}
			// if still nothing, see if 007/00s serial publication by default
			else if ((formats.isEmpty() || formats.size() == 0) && leaderChar07 == 's') {
				if (cf008 != null) 
				{
					char c21 = ((ControlField) cf008).getData().charAt(21);
					switch (c21) {
						case 'd':
						case 'l':
						case 'm':
						case 'n':
						case 'p':
						case 'w':
							break;
						case ' ':
							formats.add(Format.JOURNAL_PERIODICAL.toString());
					}
				}
			}
		}

		// look for conference proceedings in 6xx
		List<DataField> dfList = (List<DataField>) record.getDataFields();
		for (DataField df : dfList) {
			if (df.getTag().startsWith("6")) {
				List<String> subList = Utils.getSubfieldStrings(df, 'x');
				subList.addAll(Utils.getSubfieldStrings(df, 'v'));
				for (String s : subList) {
					if (s.toLowerCase().contains("congresses")) {
						formats.remove(Format.JOURNAL_PERIODICAL.toString());
						formats.add(Format.CONFERENCE_PROCEEDINGS.toString());
					}
				}
			}
		}

		// thesis is determined by the presence of a 502 field.
		Set<String> dissNote = new LinkedHashSet<String>();
		dissNote.addAll(getSubfieldDataAsSet(record, "502", "a", " "));
		if (!dissNote.isEmpty() || dissNote.size() != 0)
			formats.add(Format.THESIS.toString());

		// microfilm is determined by 245 subfield h containing "microform"
		Set<String> titleH = new LinkedHashSet<String>();
		titleH.addAll(getSubfieldDataAsSet(record, "245", "h", " "));
		// check the h subfield of the 245 field
		if (Utils.setItemContains(titleH, "microform"))
			formats.add(Format.MICROFORMAT.toString());

		// check for format information from 999 ALPHANUM call numbers
		for (DataField df999 : list999df) {
			String scheme = Item999Utils.getCallNumberScheme(df999);
			if (scheme.equalsIgnoreCase("ALPHANUM")) {
				String callnum = Item999Utils.getCallNum(df999);
				if (callnum.startsWith("MFILM"))
					formats.add(Format.MICROFORMAT.toString());
				else if (callnum.startsWith("MCD"))
					formats.add(Format.MUSIC_RECORDING.toString());
				else if (callnum.startsWith("ZDVD") || callnum.startsWith("ADVD"))
					formats.add(Format.VIDEO.toString());
			}
		}

		// if we still don't have a format, it's an "other"
		if (formats.isEmpty() || formats.size() == 0)
			formats.add(Format.OTHER.toString());
	}

// Format Methods  --------------- Begin ------------------------ Format Methods

// Standard Number Methods --------- Begin ------------- Standard Number Methods

	/**
	 * returns the ISBN(s) from a record for external lookups (such as Google
	 * Book Search) (rather than the potentially larger set of ISBNs for the end
	 * user to search our index)
	 * @param record
	 * @return Set of strings containing ISBN numbers
	 */
	public Set<String> getISBNs(final Record record) 
	{
		// ISBN algorithm
		// 1. all 020 subfield a starting with 10 or 13 digits (last "digit" may be X). Ignore following text.
		// 2. if no ISBN from any 020 subfield a "yields a search result", use all 020 subfield z starting with 10 or 13 digits (last "digit" may be X). Ignore following text.
		// 
    	// NOTE BENE: there is no way to ensure field order in the retrieved lucene document

		Set<String> isbnSet = new HashSet<String>();
		if (!f020suba.isEmpty())
			isbnSet.addAll(Utils.returnValidISBNs(f020suba));

		if (isbnSet.isEmpty()) {
			isbnSet.addAll(Utils.returnValidISBNs(f020subz));
		}
		return isbnSet;
	}

	/**
	 * returns the ISBN(s) from a record for the end user to search our index
	 * (not the potentially smaller set of ISBNs for us to use for external
	 * lookups such as Google Book Search)
	 * @param record
	 * @return Set of strings containing ISBN numbers
	 */
	public Set<String> getUserISBNs(final Record record) 
	{
		// ISBN algorithm - more inclusive
    	// 1. all 020 subfield a starting with 10 or 13 digits (last "digit" may be X). Ignore following text.
		// AND
		// 2. all 020 subfield z starting with 10 or 13 digits (last "digit" may be X). Ignore following text.

		Set<String> isbnSet = new HashSet<String>();

		Set<String> aAndz = new HashSet<String>(f020suba);
		aAndz.addAll(f020subz);
		isbnSet.addAll(Utils.returnValidISBNs(aAndz));
		return isbnSet;
	}

	/**
     * returns the ISSN(s) from a record.  As ISSN is rarely multivalued, but
     *  MAY be multivalued, Naomi has decreed
     * This is a custom routine because we want multiple ISSNs only if they are 
     * subfield a.
	 * @param record
	 * @return Set of strings containing ISSN numbers
	 */
    public Set<String> getISSNs(final Record record)
    {
		// ISSN algorithm - rare but possible to have multiple ISSNs for an item
		// 1. 022 subfield a with ISSN
		// 2. if no ISSN from any 022 subfields a, use 022 subfield z

		// NOTE 1: the ISSN is always an eight digit number divided into two halves by a hyphen.
    	// NOTE 2: the last digit of an ISSN is a check digit and could be an uppercase X.

		Set<String> issnSet = new HashSet<String>();

		Set<String> set = getFieldList(record, "022a");
		if (set.isEmpty())
			set.addAll(getFieldList(record, "022z"));

		Pattern p = Pattern.compile("^\\d{4}-\\d{3}[X\\d]$");
		Iterator<String> iter = set.iterator();
		while (iter.hasNext()) {
			String value = (String) iter.next().trim();
			// check we have the right pattern
			if (p.matcher(value).matches())
				issnSet.add(value);
		}
		return issnSet;
	}

	/**
	 * returns the OCLC numbers from a record, if they exist. Note that this
	 * method does NOT pad with leading zeros. (Who needs 'em?)
	 * @param record
	 * @return Set of Strings containing OCLC numbers. There could be none.
	 */
    public Set<String> getOCLCNums(final Record record)
    {
		// OCLC number algorithm
		// 1. 035 subfield a, value prefixed "(OCoLC-M)" - remove prefix
		// 2. if no 035 subfield a prefixed "(OCoLC-M)",
    	//      use 079 field subfield a, value prefixed "ocm" or "ocn" - remove prefix
    	//      (If the id is eight digits in length, the prefix is "ocm", if 9 digits, "ocn")
    	//      Id's that are smaller than eight digits are padded with leading zeros.
    	// 3. if no "(OCoLC-M)" 035 subfield a and no "ocm" or "ocn" 079 field subfield a, 
		// use 035 subfield a, value prefixed "(OCoLC)" - remove prefix

		Set<String> oclcSet = new HashSet<String>();

		Set<String> set035a = getFieldList(record, "035a");
		oclcSet = Utils.getPrefixedVals(set035a, "(OCoLC-M)");
		if (oclcSet.isEmpty()) {
			// check for 079 prefixed "ocm" or "ocn"
			// 079 is not repeatable
			String val = getFirstFieldVal(record, "079a");
			if (val != null && val.length() != 0) 
			{
				String good = null;
				if (val.startsWith("ocm"))
					good = Utils.removePrefix(val, "ocm");
				else if (val.startsWith("ocn"))
					good = Utils.removePrefix(val, "ocn");
				if (good != null && good.length() != 0) 
				{
					oclcSet.add(good.trim());
					return oclcSet;
				}
			}
			// check for 035a prefixed "(OCoLC)"
			oclcSet = Utils.getPrefixedVals(set035a, "(OCoLC)");
		}
		return oclcSet;
	}

// Standard Number Methods --------- End --------------- Standard Number Methods    


// Title Methods ------------------- Begin ----------------------- Title Methods    
        
	/**
     * returns string for title sort:  a string containing
     *  1. the uniform title (130), if there is one - not including non-filing chars 
     *      as noted in 2nd indicator
     * followed by
     *  2.  the 245 title, not including non-filing chars as noted in ind 2
	 */
	@SuppressWarnings("unchecked")
	public String getSortTitle(final Record record) 
    {
		StringBuilder resultBuf = new StringBuilder();

		// uniform title
		DataField df = (DataField) record.getVariableField("130");
		if (df != null)
			resultBuf.append(getAlphaSubfldsAsSortStr(df, false));

		// 245 (required) title statement
		df = (DataField) record.getVariableField("245");
		if (df != null)
			resultBuf.append(getAlphaSubfldsAsSortStr(df, true));

		return resultBuf.toString().trim();
	}

// Title Methods -------------------- End ------------------------ Title Methods    


// Subject Methods ----------------- Begin --------------------- Subject Methods    

	/**
	 * Gets the value strings, but skips over 655a values when Lane is one of
	 * the locations. Also ignores 650a with value "nomesh".
	 * @param record
     * @param fieldSpec - which marc fields / subfields to use as values
	 * @return Set of strings containing values without Lane 655a or 650a nomesh
	 */
    public Set<String> getTopicAllAlphaExcept(final Record record, final String fieldSpec) 
    {
		Set<String> resultSet = getAllAlphaExcept(record, fieldSpec);
		if (buildings.contains("LANE-MED"))
			resultSet.removeAll(f655suba);
		resultSet.remove("nomesh");
		return resultSet;
	}

	/**
	 * Gets the value strings, but skips over 655a values when Lane is one of
	 * the locations. Also ignores 650a with value "nomesh". Removes trailing
	 * characters indicated in regular expression, PLUS trailing period if it is
	 * preceded by its regular expression.
	 * 
	 * @param record
     * @param fieldSpec - which marc fields / subfields to use as values
     * @param trailingCharsRegEx a regular expression of trailing chars to be
     *   replaced (see java Pattern class).  Note that the regular expression
     *   should NOT have '$' at the end.
     *   (e.g. " *[,/;:]" replaces any commas, slashes, semicolons or colons
     *     at the end of the string, and these chars may optionally be preceded
     *     by a space)
     * @param charsB4periodRegEx a regular expression that must immediately 
     *  precede a trailing period IN ORDER FOR THE PERIOD TO BE REMOVED. 
     *  Note that the regular expression will NOT have the period or '$' at 
     *  the end. 
     *   (e.g. "[a-zA-Z]{3,}" means at least three letters must immediately 
     *   precede the period for it to be removed.) 
	 * @return Set of strings containing values without trailing characters and
	 *         without Lane 655a or 650a nomesh
	 */
    public Set<String> getTopicWithoutTrailingPunct(final Record record, final String fieldSpec, String charsToReplaceRegEx, String charsB4periodRegEx) 
    {
    	Set<String> resultSet = removeTrailingPunct(record, fieldSpec, charsToReplaceRegEx, charsB4periodRegEx);
		if (buildings.contains("LANE-MED"))
			resultSet.removeAll(f655suba);
		resultSet.remove("nomesh");
		return resultSet;
	}


// Subject Methods ----------------- End ----------------------- Subject Methods    

// Access Methods ----------------- Begin ----------------------- Access Methods    

	/**
	 * returns the access facet values for a record. A record can have multiple
	 * values: online, on campus and upon request are not mutually exclusive.
	 * @param record
	 * @return Set of Strings containing access facet values.
	 */
	public Set<String> getAccessMethods(final Record record) 
	{
		Set<String> resultSet = new HashSet<String>();

		for (DataField df999 : list999df) {
			if (!Item999Utils.hasSkippedLoc(df999)) {
				if (Item999Utils.hasOnlineLoc(df999))
					resultSet.add(Access.ONLINE.toString());
				else
					resultSet.add(Access.AT_LIBRARY.toString());
			}
		}

		if (fullTextUrls.size() > 0)
			resultSet.add(Access.ONLINE.toString());

		if (sfxUrls.size() > 0)
			resultSet.add(Access.ONLINE.toString());

		return resultSet;
	}

// Access Methods -----------------  End  ----------------------- Access Methods    

// URL Methods -------------------- Begin -------------------------- URL Methods    

    /**
     * returns a set of strings containing the sfx urls in a record.  Returns
     *   empty set if none.
     */
    public Set<String> getSFXUrls(final Record record)
    {
    	return sfxUrls;
	}

	/**
	 * assign sfxUrls to be strings containing the sfx urls in a record.
	 */
	private void setSFXUrls() 
	{
		sfxUrls = new HashSet<String>();
		// all 956 subfield u contain fulltext urls that aren't SFX
		for (String url : f956subu) {
			if (isSFXUrl(url))
				sfxUrls.add(url);
		}
	}

	/**
	 * returns the URLs for the full text of a resource described by the 856u
	 */
	public Set<String> getFullTextUrls(final Record record) 
	{
		return fullTextUrls;
	}

	/**
	 * assign fullTextUrls to be the URLs for the full text of a resource as
	 *  described by the 856u
	 */
	private void setFullTextUrls(final Record record) {
		fullTextUrls = new HashSet<String>();

		// get full text urls from 856, then check for gsb forms
		fullTextUrls = super.getFullTextUrls(record);
		for (String possUrl : fullTextUrls) {
       		if (possUrl.startsWith("http://www.gsb.stanford.edu/jacksonlibrary/services/") ||
          		     possUrl.startsWith("https://www.gsb.stanford.edu/jacksonlibrary/services/"))
				fullTextUrls.remove(possUrl);
		}
		fullTextUrls.addAll(fullTextUrls);

		// get all 956 subfield u containing fulltext urls that aren't SFX
		for (String url : f956subu) {
			if (!isSFXUrl(url))
				fullTextUrls.add(url);
		}
	}

	private boolean isSFXUrl(String urlStr) {
    	if (urlStr.startsWith("http://caslon.stanford.edu:3210/sfxlcl3?") ||
        	 urlStr.startsWith("http://library.stanford.edu/sfx?") )
			return true;
		else
			return false;
	}

// URL Methods --------------------  End  -------------------------- URL Methods    


// Publication Methods  -------------- Begin --------------- Publication Methods    
    
	/**
	 * Gets the value strings, but skips over 655a values when Lane is one of
	 * the locations. Also ignores 650a with value "nomesh".
	 * @param record
     * @param fieldSpec - which marc fields / subfields to use as values
	 * @return Set of strings containing values without Lane 655a or 650a nomesh
	 */
    @SuppressWarnings("unchecked")
	public Set<String> getPublication(final Record record) 
    { 
		Set<String> resultSet = new LinkedHashSet<String>();
		// 260ab but ignore s.l in 260a and s.n. in 260b
		List<VariableField> vf260List = record.getVariableFields("260");
		for (VariableField vf260 : vf260List) 
		{
			DataField df260 = (DataField) vf260;
			List<Subfield> subFlds = df260.getSubfields();
			StringBuilder buffer = new StringBuilder("");
			for (Subfield sf : subFlds) 
			{
				char sfcode = sf.getCode();
				String sfdata = sf.getData();
				boolean addIt = false;
				if (sfcode == 'a' && !sfdata.matches("(?i).*s\\.l\\..*"))
					addIt = true;
				else if (sfcode == 'b' && !sfdata.matches("(?i).*s\\.n\\..*"))
					addIt = true;
				if (addIt) 
				{
					if (buffer.length() > 0)
						buffer.append(" ");
					buffer.append(sfdata);
				}
			}
			if (buffer.length() > 0)
				resultSet.add(Utils.cleanData(buffer.toString()));
		}
		return resultSet;
	}

	private int currYearAsInt = PubDateUtils.getCurrentYearAsInt();
	private String currYearAsStr = Integer.toString(currYearAsInt);

	/**
	 * returns the publication date from a record, if it is present and not
     *  beyond the current year + 1 (and not earlier than 0500 if it is a 
     *  4 digit year
     *   four digit years < 0500 trigger an attempt to get a 4 digit date from 260c
     * Side Effects:  errors in pub date are logged 
	 * @param record
	 * @return String containing publication date, or null if none
	 */
	public String getPubDate(final Record record) 
	{
		if (date008 != null) {
			String errmsg = "Bad Publication Date in record " + id + " from 008/07-10: " + date008;
			if (PubDateUtils.isdddd(date008)) {
				String result = PubDateUtils.getValidPubDate(date008, currYearAsInt + 1, 500,
						record);
				if (result != null)
					return result;
				else
					logger.error(errmsg);
			} else if (PubDateUtils.isdddu(date008)) {
				int myFirst3 = Integer.parseInt(date008.substring(0, 3));
				int currFirst3 = Integer.parseInt(currYearAsStr.substring(0, 3));
				if (myFirst3 <= currFirst3)
					return date008.substring(0, 3) + "0s";
				else
					logger.error(errmsg);
			} else if (PubDateUtils.isdduu(date008)) {
				int myFirst2 = Integer.parseInt(date008.substring(0, 2));
				int currFirst2 = Integer.parseInt(currYearAsStr.substring(0, 2));
				if (myFirst2 <= currFirst2)
					return PubDateUtils.getCenturyString(date008.substring(0, 2));
				else
					logger.error(errmsg);
			}
		}

		return null;
	}

	/**
     * returns the sortable publication date from a record, if it is present
     *  and not beyond the current year + 1, and not earlier than 0500 if
     *   a four digit year
     *   four digit years < 0500 trigger an attempt to get a 4 digit date from 260c
     *  NOTE: errors in pub date are not logged;  that is done in getPubDate()
	 * @param record
	 * @return String containing publication date, or null if none
	 */
	public String getPubDateSort(final Record record) {
		if (date008 != null) {
			// hyphens sort before 0, so the lexical sorting will be correct. I
			// think.
			if (PubDateUtils.isdddd(date008))
				return PubDateUtils.getValidPubDate(date008, currYearAsInt + 1, 500, record);
			else if (PubDateUtils.isdddu(date008)) {
				int myFirst3 = Integer.parseInt(date008.substring(0, 3));
				int currFirst3 = Integer.parseInt(currYearAsStr.substring(0, 3));
				if (myFirst3 <= currFirst3)
					return date008.substring(0, 3) + "-";
			} else if (PubDateUtils.isdduu(date008)) {
				int myFirst2 = Integer.parseInt(date008.substring(0, 2));
				int currFirst2 = Integer.parseInt(currYearAsStr.substring(0, 2));
				if (myFirst2 <= currFirst2)
					return date008.substring(0, 2) + "--";
			}
		}

		return null;
	}

	/**
	 * returns the publication date groupings from a record, if pub date is
     *  given and is no later than the current year + 1, and is not earlier 
     *  than 0500 if it is a 4 digit year.
     *   four digit years < 0500 trigger an attempt to get a 4 digit date from 260c
     *  NOTE: errors in pub date are not logged;  that is done in getPubDate()
	 * @param record
	 * @return Set of Strings containing the publication date groupings
	 *         associated with the publish date
	 */
	public Set<String> getPubDateGroups(final Record record) 
	{
		Set<String> resultSet = new HashSet<String>();

		// get the pub date, with decimals assigned for inclusion in ranges
		if (date008 != null) {
			if (PubDateUtils.isdddd(date008)) // exact year
			{
				String myDate = PubDateUtils.getValidPubDate(date008, currYearAsInt + 1, 500, record);
				if (myDate != null) {
					int year = Integer.parseInt(myDate);
					// "this year" and "last three years" are for 4 digits only
					if (year >= (currYearAsInt - 1))
						resultSet.add(PubDateGroup.THIS_YEAR.toString());
					if (year >= (currYearAsInt - 3))
						resultSet.add(PubDateGroup.LAST_3_YEARS.toString());
					resultSet.addAll(PubDateUtils.getPubDateGroupsForYear(year));
				}
			} 
			else if (PubDateUtils.isdddu(date008)) // decade
			{
				String first3Str = date008.substring(0, 3);
				int first3int = Integer.parseInt(first3Str);
				int currFirst3 = Integer.parseInt(currYearAsStr.substring(0, 3));
				if (first3int <= currFirst3) {
					if (first3Str.equals(currYearAsStr.substring(0, 3))) // this decade?
					{
						resultSet.add(PubDateGroup.LAST_50_YEARS.toString());
						resultSet.add(PubDateGroup.LAST_10_YEARS.toString());
						if (currYearAsInt % 10 <= 3)
							resultSet.add(PubDateGroup.LAST_3_YEARS.toString());
					} 
					else 
					{ // not current decade
						if (currYearAsInt % 10 <= 4) // which half of decade?
						{
							// first half of decade - current year ends in 0-4
							if (first3int == (currYearAsInt / 10) - 1)
								resultSet.add(PubDateGroup.LAST_10_YEARS.toString());

							if (first3int >= (currYearAsInt / 10) - 6)
								resultSet.add(PubDateGroup.LAST_50_YEARS.toString());
							else
								resultSet.add(PubDateGroup.MORE_THAN_50_YEARS_AGO.toString());
						} 
						else {
							// second half of decade - current year ends in 5-9
							if (first3int > (currYearAsInt / 10) - 5)
								resultSet.add(PubDateGroup.LAST_50_YEARS.toString());
							else
								resultSet.add(PubDateGroup.MORE_THAN_50_YEARS_AGO.toString());
						}
					}

				}
			} 
			else if (PubDateUtils.isdduu(date008)) { // century
				String first2Str = date008.substring(0, 2);
				int first2int = Integer.parseInt(first2Str);
				int currFirst2 = Integer.parseInt(currYearAsStr.substring(0, 2));
				if (first2int <= currFirst2) {
					if (first2Str.equals(currYearAsStr.substring(0, 2))) {
						// current century
						resultSet.add(PubDateGroup.LAST_50_YEARS.toString());

						if (currYearAsInt % 100 <= 19)
							resultSet.add(PubDateGroup.LAST_10_YEARS.toString());
					} 
					else {
						if (first2int == (currYearAsInt / 100) - 1) 
						{
							// previous century
							if (currYearAsInt % 100 <= 25)
								resultSet.add(PubDateGroup.LAST_50_YEARS.toString());
							else
								resultSet.add(PubDateGroup.MORE_THAN_50_YEARS_AGO.toString());
						} 
						else
							resultSet.add(PubDateGroup.MORE_THAN_50_YEARS_AGO.toString());
					}
				}
			}
			// we don't work with duuu or uuuu or other date strings
		}

		return resultSet;
	}

// Pub Date Methods  --------------  End  --------------------- Pub Date Methods    


// AllFields Methods  --------------- Begin ------------------ AllFields Methods    
		
	/**
	 * fields in the 0xx range (not including control fields) that should be
	 * indexed in allfields
	 */
	Set<String> keepers0xx = new HashSet<String>();
	{
		keepers0xx.add("024");
		keepers0xx.add("027");
		keepers0xx.add("028");
	}

	/**
	 * Returns all subfield contents of all the data fields (non control fields)
	 *  between 100 and 899 inclusive, as a single string 
	 *  plus the "keeper" fields
	 * @param record Marc record to extract data from
	 */
	@SuppressWarnings("unchecked")
	public String getAllFields(final Record record) 
	{
		StringBuilder result = new StringBuilder(5000);
		List<DataField> dataFieldList = record.getDataFields();
		for (DataField df : dataFieldList) {
			String tag = df.getTag();
			if (!tag.startsWith("9") && !tag.startsWith("0")
					|| (tag.startsWith("0") && keepers0xx.contains(tag))) {
				List<Subfield> subfieldList = df.getSubfields();
				for (Subfield sf : subfieldList) {
					result.append(sf.getData() + " ");
				}
			}
		}
		return result.toString().trim();
	}

// AllFields Methods  ---------------  End  ------------------ AllFields Methods    


// Item Related Methods ------------- Begin --------------- Item Related Methods    
	
	/**
	 * get buildings holding a copy of this resource
	 */
	public Set<String> getBuildings(final Record record) {
		return buildings;
	}

	/**
	 * set buildings from the 999 subfield m
	 */
	private void setBuildings(final Record record) 
	{
		buildings = new HashSet<String>();
		for (DataField df999 : list999df) {
			String buildingStr = Item999Utils.getBuilding(df999);
			if (buildingStr.length() != 0)
				buildings.add(buildingStr);
		}
	}

	/**
	 * for search result display:
	 * @return set of barcode - lib - location - callnum fields from 999s
	 */
	public Set<String> getItemDisplay(final Record record) 
	{
		Set<String> result = new HashSet<String>();
		// FIXME: sep should be globally avail constant (for tests also?)
		String sep = " -|- ";

		// is it a serial?
		boolean isSerial = false;
		if (formats.contains(Format.JOURNAL_PERIODICAL.toString()))
			isSerial = true;

		for (DataField df999 : list999df) {
			if (!Item999Utils.hasSkippedLoc(df999)) {
				String building = "";
				String location = "";
				String rawLoc = Item999Utils.getLocationFrom999(df999);				

				if (Item999Utils.hasOnlineLoc(df999)) {
					building = "Online";
					location = "Online";
				} 
				else {
					// building --> short name from mapping
					String origBldg = Item999Utils.getBuilding(df999);
					if (origBldg.length() > 0)
						building = Utils.remap(origBldg, findMap(LIBRARY_SHORT_MAP_NAME), true);
					if (building == null || building.length() == 0)
						building = origBldg;
					// location --> mapped
					if (rawLoc.length() > 0)
						location = Utils.remap(rawLoc, findMap(LOCATION_MAP_NAME), true);
				}

				// full call number & lopped call number
				String callnumScheme = Item999Utils.getCallNumberScheme(df999);
				String fullCallnum = Item999Utils.getNonSkippedCallNum(df999);
				String loppedCallnum = null;
				if (fullCallnum.length() > 0) {
					if (callnumScheme.startsWith("LC"))
						if (isSerial)
							loppedCallnum = CallNumUtils.removeLCSerialVolSuffix(fullCallnum);
						else
							loppedCallnum = CallNumUtils.removeLCVolSuffix(fullCallnum);
					else if (callnumScheme.startsWith("DEWEY"))
						if (isSerial)
							loppedCallnum = CallNumUtils.removeDeweySerialVolSuffix(fullCallnum);
						else
							loppedCallnum = CallNumUtils.removeDeweyVolSuffix(fullCallnum);
					else 
						if (isSerial)
							loppedCallnum = CallNumUtils.removeSerialVolSuffix(fullCallnum);
						else
							loppedCallnum = CallNumUtils.removeVolSuffix(fullCallnum);
				}

				// deal with shelved by title locations
				String volSuffix = null;
				if (loppedCallnum != null && loppedCallnum.length() > 0)
					volSuffix = fullCallnum.substring(loppedCallnum.length()).trim();
				if ((volSuffix == null || volSuffix.length() == 0) && CallNumUtils.callNumIsVolSuffix(fullCallnum))
					volSuffix = fullCallnum;
				if (rawLoc.length() > 0) {
					if (rawLoc.equals("SHELBYTITL")) {
						isSerial = true;
						location = "Serials";
						loppedCallnum = "Shelved by title";
						fullCallnum = loppedCallnum + " " + volSuffix;
					}
					if (rawLoc.equals("SHELBYSER")) {
						isSerial = true;
						location = "Serials";
						loppedCallnum = "Shelved by Series title";
						fullCallnum = loppedCallnum + " " + volSuffix;
					} 
					else if (rawLoc.equals("STORBYTITL")) {
						isSerial = true;
						location = "Storage area";
						loppedCallnum = "Shelved by title";
						fullCallnum = loppedCallnum + " " + volSuffix;
					}
				}

				// shelfkey for lopped callnumber
				String shelfkey = null;
				if (callnumScheme.length() > 0)
					shelfkey = edu.stanford.CallNumUtils.getShelfKey(loppedCallnum, callnumScheme, id);
				else
					shelfkey = edu.stanford.CallNumUtils.getShelfKey(loppedCallnum, id);

				// reversekey for lopped callnumber
				String reversekey = org.solrmarc.tools.CallNumUtils.getReverseShelfKey(shelfkey);

				// sortable call number for show view
				String volSort = edu.stanford.CallNumUtils.getVolumeSortCallnum(fullCallnum, loppedCallnum,isSerial, id);

				String barcode = Item999Utils.getBarcode(df999);

				// create field
				if (loppedCallnum != null)
	    			result.add( barcode + sep + 
		    					building + sep + 
		    					location + sep + 
		    					loppedCallnum + sep + 
		    					shelfkey.toLowerCase() + sep + 
		    					reversekey.toLowerCase() + sep + 
		    					fullCallnum + sep + 
		    					volSort );
			}
		} // end loop through 999s

		return result;
	}

// Item Related Methods -------------  End  --------------- Item Related Methods    

	
// Call Number Methods -------------- Begin ---------------- Call Number Methods    

	/**
	 * Get our local call numbers from subfield a in 999. Does not get call
	 * number when item or callnum should be ignored, or for online items.
	 */
	public Set<String> getLocalCallNums(final Record record) 
	{
		Set<String> result = new HashSet<String>();

		for (DataField df999 : list999df) {
			if (!Item999Utils.isIgnoredCallNumLoc(df999)) {
				String callnum = Item999Utils.getNonSkippedCallNum(df999);
				if (callnum != null)
					result.add(callnum);
			}
		}

		return result;
	}

	/**
	 * Get values for top level call number facet:  
	 *   for LC, the first character + description
	 *   for Dewey, DEWEY
	 *   for Gov Doc, GOV_DOC_FACET_VAL
	 */
	public Set<String> getCallNumsLevel1(final Record record) 
	{
		// LC
		Set<String> result = Item999Utils.getLCCallNumBroadCats(list999df);

		// TODO: need to REMOVE LC callnum if it's a gov doc location? not sure.
		if (govDocCats.size() != 0)
			result.add(Item999Utils.GOV_DOC_TOP_FACET_VAL);

		// check for Dewey 
		for (DataField df999 : list999df) {
			if (!Item999Utils.isIgnoredCallNumLoc(df999)
					&& Item999Utils.getDeweyCallNumber(df999) != null)
				result.add(Item999Utils.DEWEY_TOP_FACET_VAL);
		}

		return result;
	}

	/**
	 * Assign shelfkeys to sortable versions of "lopped" call numbers (call 
	 * numbers without volume info)
	 */
	public void setShelfkeysOrig(final Record record) 
	{
		shelfkeys = new HashSet<String>();

		for (DataField df999 : list999df) 
		{
			// make sure it's not ignored
			if (Item999Utils.hasSkippedLoc(df999) || Item999Utils.isIgnoredCallNumLoc(df999)
					|| Item999Utils.hasOnlineLoc(df999))
				continue;

			String callnum = GenericUtils.getSubfieldTrimmed(df999, 'a');
			if (callnum.length() == 0)
				continue;
			String callnumScheme = Item999Utils.getCallNumberScheme(df999);

			String shelfkey = null;
			if (callnumScheme.startsWith("LC")) 
			{
				String lopped = CallNumUtils.removeLCVolSuffix(callnum);
				shelfkey = edu.stanford.CallNumUtils.getShelfKey(lopped, "LC", id);
			} 
			else if (callnumScheme.startsWith("DEWEY")) 
			{
				String lopped = CallNumUtils.removeDeweyVolSuffix(callnum);
				shelfkey = edu.stanford.CallNumUtils.getShelfKey(lopped, "DEWEY", id);
				if (shelfkey.equals(callnum.toUpperCase()))
					System.err.println("Problem creating shelfkey for record " + id + ": " + callnum);
			} 
			else
				shelfkey = org.solrmarc.tools.CallNumUtils.normalizeSuffix(callnum);

			if (shelfkey.length() > 0)
				shelfkeys.add(shelfkey);
		}
	}

	/**
	 * Get shelfkey versions of "lopped" call numbers (call numbers without
	 * volume info).  Can access shelfkeys in lexigraphical order for browsing
	 */
	public Set<String> getShelfkeys(final Record record) {
		return shelfkeys;
	}

	/**
	 * Assign shelfkeys to sortable versions of "lopped" call numbers (call 
	 * numbers without volume info)
	 */
	private Set<String> setShelfkeys(final Record record) {
		shelfkeys = new HashSet<String>();

		Map<String, String[]> libLoc2callnums = new HashMap();

		for (DataField df999 : list999df) {
			// make sure it's not ignored
			if (Item999Utils.hasSkippedLoc(df999) || Item999Utils.isIgnoredCallNumLoc(df999)
					|| Item999Utils.hasOnlineLoc(df999))
				continue;

			String callnum = Item999Utils.getCallNum(df999);
			if (callnum.length() == 0)
				continue;

			String library = Item999Utils.getBuilding(df999);
			String homeLoc = Item999Utils.getHomeLocation(df999);
			String callnumScheme = Item999Utils.getCallNumberScheme(df999);

			String shelfkey = "";
			if (callnumScheme.startsWith("LC")) {
				String lopped = CallNumUtils.removeLCVolSuffix(callnum);
				shelfkey = edu.stanford.CallNumUtils.getShelfKey(lopped, "LC", id);
			} 
			else if (callnumScheme.startsWith("DEWEY")) {
				String lopped = CallNumUtils.removeDeweyVolSuffix(callnum);
				shelfkey = edu.stanford.CallNumUtils.getShelfKey(lopped, "DEWEY", id);
				if (shelfkey.equals(callnum.toUpperCase()))
					System.err.println("Problem creating shelfkey for record " + id + ": " + callnum);
			} else
				shelfkey = org.solrmarc.tools.CallNumUtils.normalizeSuffix(callnum);

			if (shelfkey.length() > 0)
				shelfkeys.add(shelfkey);
		}

		return shelfkeys;
	}

	/**
	 * Get reverse shelfkey versions of "lopped" call numbers (call numbers
	 * without volume info). Can access in lexigraphical order for browsing
	 * (used to get previous callnums ...)
	 */
	public Set<String> getReverseShelfkeys(final Record record) 
	{
		Set<String> result = new HashSet<String>();

		for (String shelfkey : shelfkeys) {
			String reversekey = org.solrmarc.tools.CallNumUtils.getReverseShelfKey(shelfkey);
			if (reversekey != null)
				result.add(reversekey);
		}

		return result;
	}

	/**
	 * This is a facet field to enable discovery by subject, as designated by
	 * call number. It looks at our local values in the 999, and returns the
	 * broad category strings ("x00s");
	 */
	@SuppressWarnings("unchecked")
	public Set<String> getDeweyCallNumBroadCats(final Record record) {
		Set<String> result = new HashSet<String>();

		Set<String> deweySet = Item999Utils.getDeweyforClassification(list999df);
		for (String dewey : deweySet) {
			if (dewey != null && dewey.length() > 2)
				result.add(dewey.substring(0, 1) + "00s");
		}

		return result;
	}

	/**
	 * This is for a facet field to enable discovery by subject, as designated
	 * by call number. It looks at our local values in the 999 and returns the
	 * secondary level category strings (for LC, the 1-3 letters at the
	 * beginning)
	 */
	public Set<String> getLCCallNumCats(final Record record) {
		Set<String> result = new HashSet<String>();

		Set<String> lcSet = Item999Utils.getLCforClassification(list999df);
		for (String lc : lcSet) {
			String letters = org.solrmarc.tools.CallNumUtils.getLCstartLetters(lc);
			if (letters != null)
				result.add(letters);
		}

		return result;
	}

	/**
	 * Get type(s) of government doc based on location.
	 */
	public Set<String> getGovDocCats(final Record record) {
		return govDocCats;
	}

	/**
	 * Assign type of government doc based on:
	 *   callnumber scheme of SUDOC
	 *   location in 999
	 *   presence of 086 field (use all 99s that aren't to be skipped)
	 */
	private void setGovDocCats(final Record record) 
	{
		govDocCats = new HashSet<String>();

		boolean has086 = !record.getVariableFields("086").isEmpty();

		for (DataField df999 : list999df) {
			if (!Item999Utils.hasSkippedLoc(df999) && !Item999Utils.isIgnoredCallNumLoc(df999)
					&& !Item999Utils.hasOnlineLoc(df999)) {
				String rawLoc = Item999Utils.getLocationFrom999(df999);
				if (Item999Utils.isGovDocLoc(rawLoc) || has086)
					govDocCats.add(Item999Utils.getGovDocTypeFromLocCode(rawLoc));
				else { // is it SUDOC call number?
					String scheme = Item999Utils.getCallNumberScheme(df999);
					if (scheme.equalsIgnoreCase("SUDOC"))
						govDocCats.add(Item999Utils.getGovDocTypeFromLocCode(rawLoc));
				}
			}
		}
	}

	/**
	 * This is for a facet field to enable discovery by subject, as designated
	 * by call number. It looks at our local values in the 999, and returns the
	 * secondary level category strings (for Dewey, "xx0s")
	 */
	public Set<String> getDeweyCallNumCats(final Record record) {
		Set<String> result = new HashSet<String>();

		Set<String> deweySet = Item999Utils.getDeweyforClassification(list999df);
		for (String dewey : deweySet) {
			if (dewey != null && dewey.length() > 2)
				result.add(dewey.substring(0, 2) + "0s");
		}

		return result;
	}

	/**
	 * This is for a facet field to enable discovery by subject, as designated
	 * by call number. It looks at our local LC values in the 999 and returns
	 * the Strings before the Cutters in the call numbers (LC only)
	 */
	public Set<String> getLCCallNumsB4Cutter(final Record record) {
		Set<String> result = new HashSet<String>();

		Set<String> lcSet = Item999Utils.getLCforClassification(list999df);
		for (String lc : lcSet) {
			result.add(org.solrmarc.tools.CallNumUtils.getPortionBeforeCutter(lc));
		}

		return result;
	}

	/**
	 * This is for a facet field to enable discovery by subject, as designated
	 * by call number. It looks at our local Dewey values in the 999 and returns
	 * the Strings before the Cutters in the call numbers (Dewey only)
	 */
	public Set<String> getDeweyCallNumsB4Cutter(final Record record) {
		Set<String> result = new HashSet<String>();

		Set<String> deweySet = Item999Utils.getDeweyforClassification(list999df);
		for (String dewey : deweySet) {
			result.add(org.solrmarc.tools.CallNumUtils.getPortionBeforeCutter(org.solrmarc.tools.CallNumUtils.addLeadingZeros(dewey)));
		}
		return result;
	}

// Call Number Methods -------------- End ---------------- Call Number Methods


// Vernacular Methods --------------- Begin ----------------- Vernacular Methods    

	/**
	 * Get the vernacular (880) field based which corresponds to the fieldSpec
	 * in the subfield 6 linkage, handling multiple occurrences as indicated
     * @param fieldSpec - which marc fields / subfields need to be sought in 
     *  880 fields (via linkages)
     * @param multOccurs - "first", "join" or "all" indicating how to handle
     *  multiple occurrences of field values
	 */
	@SuppressWarnings("unchecked")
	public final Set<String> getVernacular(final Record record, String fieldSpec, String multOccurs) 
	{
		Set<String> result = getLinkedField(record, fieldSpec);

		if (multOccurs.equals("first")) {
			Set<String> first = new HashSet<String>();
			for (String r : result) {
				first.add(r);
				return first;
			}
		} else if (multOccurs.equals("join")) {
			StringBuilder resultBuf = new StringBuilder();
			for (String r : result) {
				if (resultBuf.length() > 0)
					resultBuf.append(' ');
				resultBuf.append(r);
			}
			Set<String> resultAsSet = new HashSet<String>();
			resultAsSet.add(resultBuf.toString());
			return resultAsSet;
		}
		// "all" is default

		return result;
	}

	/**
	 * 
	 * For each occurrence of a marc field in the fieldSpec list, get the
     * matching vernacular (880) field (per subfield 6) and extract the
     * contents of all subfields except the ones specified, concatenate the 
     * subfield contents with a space separator and add the string to the result
     * set.
     * @param record - the marc record
     * @param fieldSpec - the marc fields (e.g. 600:655) for which we will grab
     *  the corresponding 880 field containing subfields other than the ones
     *  indicated.  
     * @return a set of strings, where each string is the concatenated values
     *  of all the alphabetic subfields in the 880s except those specified.
	 */
	@SuppressWarnings("unchecked")
	public final Set<String> getVernAllAlphaExcept(final Record record, String fieldSpec)
	{
		Set<String> resultSet = new LinkedHashSet<String>();

		String[] fldTags = fieldSpec.split(":");
		for (int i = 0; i < fldTags.length; i++) 
		{
			String fldTag = fldTags[i].substring(0, 3);
			if (fldTag.length() < 3 || Integer.parseInt(fldTag) < 10) 
			{
				System.err.println("Invalid marc field specified for getAllAlphaExcept: " + fldTag);
				continue;
			}

			String tabooSubfldTags = fldTags[i].substring(3);

			Set<VariableField> vernFlds = GenericUtils.getVernacularFields(record, fldTag);

			for (VariableField vf : vernFlds) 
			{
				StringBuilder buffer = new StringBuilder(500);
				DataField df = (DataField) vf;
				if (df != null) 
				{
					List<Subfield> subfields = df.getSubfields();
					for (Subfield sf : subfields) 
					{
						if (Character.isLetter(sf.getCode())
								&& tabooSubfldTags.indexOf(sf.getCode()) == -1) 
						{
							if (buffer.length() > 0)
								buffer.append(' ' + sf.getData());
							else
								buffer.append(sf.getData());
						}
					}
					if (buffer.length() > 0)
						resultSet.add(buffer.toString());
				}
			}
		}

		return resultSet;
	}

	/**
	 * Get the vernacular (880) field based which corresponds to the fieldSpec
	 * in the subfield 6 linkage, handling trailing punctuation as incidated
     * @param fieldSpec - which marc fields / subfields need to be sought in 
     *  880 fields (via linkages)
     * @param trailingCharsRegEx a regular expression of trailing chars to be
     *   replaced (see java Pattern class).  Note that the regular expression
     *   should NOT have '$' at the end.
     *   (e.g. " *[,/;:]" replaces any commas, slashes, semicolons or colons
     *     at the end of the string, and these chars may optionally be preceded
     *     by a space)
     * @param charsB4periodRegEx a regular expression that must immediately 
     *  precede a trailing period IN ORDER FOR THE PERIOD TO BE REMOVED. 
     *  Note that the regular expression will NOT have the period or '$' at 
     *  the end. 
     *   (e.g. "[a-zA-Z]{3,}" means at least three letters must immediately 
     *   precede the period for it to be removed.) 
	 */
	@SuppressWarnings("unchecked")
	public final Set<String> vernRemoveTrailingPunc(final Record record, String fieldSpec, String charsToReplaceRegEx, String charsB4periodRegEx) 
	{
		Set<String> origVals = getLinkedField(record, fieldSpec);
		Set<String> result = new LinkedHashSet<String>();

		for (String val : origVals) {
			result.add(Utils.removeAllTrailingCharAndPeriod(val, 
					"(" + charsToReplaceRegEx + ")+", charsB4periodRegEx));
		}
		return result;
	}

// Vernacular Methods ---------------  End  ----------------- Vernacular Methods    
    
// Generic Methods ---------------- Begin ---------------------- Generic Methods    
    
	/**
	 * Removes trailing characters indicated in regular expression, PLUS
	 * trailing period if it is preceded by its regular expression.
	 * 
	 * @param record
     * @param fieldSpec - which marc fields / subfields to use as values
     * @param trailingCharsRegEx a regular expression of trailing chars to be
     *   replaced (see java Pattern class).  Note that the regular expression
     *   should NOT have '$' at the end.
     *   (e.g. " *[,/;:]" replaces any commas, slashes, semicolons or colons
     *     at the end of the string, and these chars may optionally be preceded
     *     by a space)
     * @param charsB4periodRegEx a regular expression that must immediately 
     *  precede a trailing period IN ORDER FOR THE PERIOD TO BE REMOVED. 
     *  Note that the regular expression will NOT have the period or '$' at 
     *  the end. 
     *   (e.g. "[a-zA-Z]{3,}" means at least three letters must immediately 
     *   precede the period for it to be removed.) 
	 * 
	 * @return Set of strings containing values without trailing characters
	 */
    public Set<String> removeTrailingPunct(final Record record, final String fieldSpec, String charsToReplaceRegEx, String charsB4periodRegEx) 
    {
		Set<String> resultSet = new HashSet<String>();
		for (String val : getFieldList(record, fieldSpec)) {
    		String result = Utils.removeAllTrailingCharAndPeriod(val, "(" + charsToReplaceRegEx + ")+", charsB4periodRegEx);
			resultSet.add(result);
		}

		return resultSet;
	}

// Generic Methods ------------------ End ---------------------- Generic Methods

}
