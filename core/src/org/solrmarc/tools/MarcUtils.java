package org.solrmarc.tools;

import org.apache.log4j.Logger;
import org.marc4j.*;
import org.marc4j.marc.*;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MarcUtils {

    static Logger logger = Logger.getLogger(MarcUtils.class.getName());
    /**
     * Default Constructor,  private, so it can't be instantiated by other objects
     */
    private MarcUtils(){ }


 // --------------------------- field methods ----------------------------------

    /**
     * Return a List of VariableField objects as indicated by fieldSpec.
     *
     * @param record - the marc record object
     * @param fieldSpec - string containing which field(s) to retrieve.  This
     *   string contains one or more:  marc "tags" (3 chars identifying a marc
     *   field, e.g. 245).  If more than one marc tag is desired, they need to
     *   be separated by a colon (e.g. 100:110:111)
     * @return a List of VariableFields corresponding to the marc field(s)
     *   specified in the fieldSpec
     */
    @SuppressWarnings("unchecked")
	public static List<VariableField> getVariableFields(Record record, String fieldSpec)
    {
        List<VariableField> result = new ArrayList<VariableField>();

        String[] tags = fieldSpec.split(":");
        for (String tag : tags)
        {
            // Check to ensure tag length is at least 3 characters
            if (tag.length() < 3)
            {
                System.err.println("Invalid tag specified: " + tag);
                continue;
            }

            result.addAll(record.getVariableFields(tag));
        }
        return result;
    }

    /**
	 * remove the specified fields from result
	 *
	 * FIXME:  the following can be changed if there's a utility to copy
	 *  record objects
	 * Side Effect:
	 *   NOTE:  the method changes the first param's value in addition to
	 *   providing the result (which is the same object as the first param's new value)
	 *
	 * @param record MARC record object
	 * @param fieldsToRemove the fields to be removed, as a regular expression (e.g. "852|866|867")
	 * @return the Record object without the specifiedfields
	 */
	public static Record removeFields(Record record, String fieldsToRemove)
	{
	    List<VariableField> recVFList = record.getVariableFields();
	    for (VariableField vf : recVFList)
	    {
	    	// FIXME:  it would be good to have some error checking on the fieldsToCopy expression passed in
	        if (vf.getTag().matches(fieldsToRemove))
	            record.removeVariableField(vf);
	    }
	    return record;
	}


	/**
     * Return an ordered list of marc4j DataField objects for the range of marc tags
     *  indicated by the (inclusive) bounds.  Tags must be 010 or higher.
     * @param record - marc record object
     * @param lowerFieldBoundStr - the "lowest" marc field to include (e.g. 600) - inclusive
     * @param upperFieldBoundStr - the "highest" marc field to include (e.g. 699) - inclusive
     * @return List of DataField objects matching the field range.
     */
    @SuppressWarnings("unchecked")
    public static List<DataField> getDataFieldsInRange(final Record record, String lowerFieldBoundStr, String upperFieldBoundStr)
    {
    	List<DataField> resultList = new ArrayList<DataField>();
        int lowerBound = Utils.parseIntNoNFE(lowerFieldBoundStr, -1);
        int upperBound = Utils.parseIntNoNFE(upperFieldBoundStr, -1);

        List<DataField> fields = record.getDataFields();
        for (DataField field : fields)
        {
            // This will ignore any "code" fields and only use textual fields
            int tag = Utils.parseIntNoNFE(field.getTag(), -1);
            if ((tag >= lowerBound) && (tag <= upperBound))
            {
            	resultList.add(field);
            }
        }
        return resultList;
    }


/**
	 * For each occurrence of a marc field in the fieldSpec list, extract the
	 * contents of all subfields except the ones specified, concatenate the
	 * subfield contents with a space separator and add the string to the result
	 * set.
	 *
	 * @param record -
	 *            the marc record
	 * @param fieldSpec -
	 *            the marc fields (e.g. 600:655) in which we will grab the
	 *            alphabetic subfield contents for the result set. The field may
	 *            not be a control field (must be 010 or greater)
	 * @return a set of strings, where each string is the concatenated values of
	 *         all the alphabetic subfields.
	 */
	@SuppressWarnings("unchecked")
	public static Set<String> getAllAlphaExcept(final Record record, String fieldSpec)
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

	        List<VariableField> varFlds = record.getVariableFields(fldTag);
	        for (VariableField vf : varFlds)
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
	                            buffer.append(' ' + sf.getData().trim());
	                        else
	                            buffer.append(sf.getData().trim());
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
	 * Loops through all datafields and creates a field for "all fields"
	 * searching. Shameless stolen from Vufind Indexer Custom Code
	 *
	 * @param record
	 *            marc record object
	 * @param lowerBoundStr -
	 *            the "lowest" marc field to include (e.g. 100). defaults to 100
	 *            if value passed doesn't parse as an integer
	 * @param upperBoundStr -
	 *            one more than the "highest" marc field to include (e.g. 900
	 *            will include up to 899). Defaults to 900 if value passed
	 *            doesn't parse as an integer
	 * @return a string containing ALL subfields of ALL marc fields within the
	 *         range indicated by the bound string arguments.
	 */
	public static String getAllSearchableFields(final Record record, String lowerBoundStr, String upperBoundStr)
	{
	    StringBuilder buffer = new StringBuilder("");
	    int lowerBound = Utils.parseIntNoNFE(lowerBoundStr, 100);
	    int upperBound = Utils.parseIntNoNFE(upperBoundStr, 900);

	    List<DataField> fields = record.getDataFields();
	    for (DataField field : fields)
	    {
	        // This will ignore any "code" fields and only use textual fields
	        int tag = Utils.parseIntNoNFE(field.getTag(), -1);
	        if ((tag >= lowerBound) && (tag < upperBound))
	        {
	            // Loop through subfields
	            List<Subfield> subfields = field.getSubfields();
	            for (Subfield subfield : subfields)
	            {
	                if (buffer.length() > 0)
	                    buffer.append(" ");
	                buffer.append(subfield.getData());
	            }
	        }
	    }
	    return buffer.toString();
	}


	/**
	 * Return a List of VariableField objects corresponding to 880 fields linked
	 *  to the fields indicated by fieldSpec.
	 *
	 * @param record - the marc record object
	 * @param fieldSpec - string containing one or more marc "tags" for which
	 *   linked 880 will be returned.  For example, 610 means return all 880
	 *   field(s) linked to any 610 field.  If more than one marc tag is
	 *   desired, they need to be separated by a colon (e.g. 100:110:111)
	 * @return a List of VariableFields corresponding to 880 fields linked to
	 *   the marc field(s) specified in the fieldSpec
	 */
	public static List<VariableField> getLinkedVariableFields(final Record record, String fieldSpec)
	{
	    List<VariableField> result = new ArrayList<VariableField>();

	    List<String> desiredTags = Arrays.asList(fieldSpec.split(":"));

	    List<VariableField> linkedFlds = getVariableFields(record, "880");
	    for (VariableField lnkFld : linkedFlds) {
			DataField df = (DataField) lnkFld;
			Subfield sub6 = df.getSubfield('6');
			if (sub6 != null && sub6.getData().length() >= 3 && desiredTags.contains(sub6.getData().substring(0, 3)))
				result.add(lnkFld);
	    }

	    return result;
	}


	/**
	 * Given a fieldSpec, get any linked 880 fields and include the appropriate
	 * subfields as a String value in the result set.
	 *
	 * @param record
	 *            marc record object
	 * @param fieldSpec -
	 *            the marc field(s)/subfield(s) for which 880s are sought.
	 *            Separator of colon indicates a separate value, rather than
	 *            concatenation. 008[5-7] denotes bytes 5-7 of the linked 008
	 *            field (0 based counting) 100[a-cf-z] denotes the bracket
	 *            pattern is a regular expression indicating which subfields to
	 *            include from the linked 880. Note: if the characters in the
	 *            brackets are digits, it will be interpreted as particular
	 *            bytes, NOT a pattern 100abcd denotes subfields a, b, c, d are
	 *            desired from the linked 880.
	 *
	 * @return set of Strings containing the values of the designated 880
	 *         field(s)/subfield(s)
	 */
	public static Set<String> getLinkedField(final Record record, String fieldSpec)
	{
	    Set<String> set = getFieldList(record, "8806");

	    if (set.isEmpty())
	        return set;

	    String[] tags = fieldSpec.split(":");
	    Set<String> result = new LinkedHashSet<String>();
	    for (int i = 0; i < tags.length; i++)
	    {
	        // Check to ensure tag length is at least 3 characters
	        if (tags[i].length() < 3)
	        {
	            System.err.println("Invalid tag specified: " + tags[i]);
	            continue;
	        }

	        // Get Field Tag
	        String tag = tags[i].substring(0, 3);

	        // Process Subfields
	        String subfield = tags[i].substring(3);

	        String separator = null;
	        if (subfield.indexOf('\'') != -1)
	        {
	            separator = subfield.substring(subfield.indexOf('\'') + 1, subfield.length() - 1);
	            subfield = subfield.substring(0, subfield.indexOf('\''));
	        }

	        result.addAll(getLinkedFieldValue(record, tag, subfield, separator));
	    }
	    return result;
	}


	/**
	 * Given a tag for a field, and a list (or regex) of one or more subfields
	 * get any linked 880 fields and include the appropriate subfields as a String value
	 * in the result set.
	 *
	 * @param record - marc record object
	 * @param tag -  the marc field for which 880s are sought.
	 * @param subfield -
	 *           The subfield(s) within the 880 linked field that should be returned
	 *            [a-cf-z] denotes the bracket pattern is a regular expression indicating
	 *            which subfields to include from the linked 880. Note: if the characters
	 *            in the brackets are digits, it will be interpreted as particular
	 *            bytes, NOT a pattern 100abcd denotes subfields a, b, c, d are
	 *            desired from the linked 880.
	 * @param separator - the separator string to insert between subfield items (if null, a " " will be used)
	 *
	 * @return set of Strings containing the values of the designated 880 field(s)/subfield(s)
	 */
	public static Set<String> getLinkedFieldValue(final Record record, String tag, String subfield, String separator)
	{
	    // assume brackets expression is a pattern such as [a-z]
	    Set<String> result = new LinkedHashSet<String>();
	    boolean havePattern = false;
	    Pattern subfieldPattern = null;
	    if (subfield.indexOf('[') != -1)
	    {
	        havePattern = true;
	        subfieldPattern = Pattern.compile(subfield);
	    }
	    List<VariableField> fields = record.getVariableFields("880");
	    for (VariableField vf : fields)
	    {
	        DataField dfield = (DataField) vf;
	        Subfield link = dfield.getSubfield('6');
	        if (link != null && link.getData().startsWith(tag))
	        {
	            List<Subfield> subList = dfield.getSubfields();
	            StringBuilder buf = new StringBuilder("");
	            for (Subfield subF : subList)
	            {
	                boolean addIt = false;
	                if (havePattern)
	                {
	                    Matcher matcher = subfieldPattern.matcher("" + subF.getCode());
	                    // matcher needs a string, hence concat with empty
	                    // string
	                    if (matcher.matches())
	                        addIt = true;
	                }
	                else
	                // a list a subfields
	                {
	                    if (subfield.indexOf(subF.getCode()) != -1)
	                        addIt = true;
	                }
	                if (addIt)
	                {
	                    if (buf.length() > 0)
	                        buf.append(separator != null ? separator : " ");
	                    buf.append(subF.getData().trim());
	                }
	            }
	            if (buf.length() > 0)
	                result.add(Utils.cleanData(buf.toString()));
	        }
	    }
	    return(result);
	}


	/**
	 * Given a fieldSpec, get the field(s)/subfield(s) values, PLUS any linked
	 * 880 fields and return these values as a set.
	 * @param record marc record object
	 * @param fieldSpec - the marc field(s)/subfield(s)
	 * @return set of Strings containing the values of the indicated field(s)/
	 *         subfields(s) plus linked 880 field(s)/subfield(s)
	 */
	public static Set<String> getLinkedFieldCombined(final Record record, String fieldSpec)
	{
	    Set<String> result1 = getLinkedField(record, fieldSpec);
	    Set<String> result2 = getFieldList(record, fieldSpec);

	    if (result1 != null)
	        result2.addAll(result1);
	    return result2;
	}


	/**
	 * Get the vernacular (880) fields which corresponds to the marc field
	 *  in the 880 subfield 6 linkage
	 * @param marcField - which field to be matched by 880 fields
	 */
	@SuppressWarnings("unchecked")
	public
	static Set<VariableField> getVernacularFields(final Record record, String marcField)
	{
		if (marcField.length() != 3)
	        System.err.println("marc field tag must be three characters: " + marcField);

		Set<VariableField> resultSet = new LinkedHashSet<VariableField>();

		List<VariableField> list880s = record.getVariableFields("880");
		if (list880s == null || list880s.size() == 0)
			return resultSet;

		// we know which 880s we're looking for by matching the marc field and
		// subfield 6 (linkage info) in the 880
		for (VariableField vf : list880s) {
			DataField df880 = (DataField) vf;
			String sub6 = MarcUtils.getSubfieldTrimmed(df880, '6');
			int dashIx = sub6.indexOf('-');
			if ((dashIx == 3) && marcField.equals(sub6.substring(0, 3)))
				resultSet.add(df880);
		}
		return (resultSet);
	}


	public static boolean isControlField(String fieldTag)
	{
	    if (fieldTag.matches("00[0-9]"))
	    {
	        return (true);
	    }
	    return (false);
	}


	// --------------------------- subfield methods --------------------------------
    /**
     * return the ordered List of strings from the indicated subfields from the given
     *  set of marc DataField objects.
     * @param dataFields a Set of DataField objects to be processed
     * @param subfldsStr a string of characters identifying which subfield
     *  values to put in the result set.
     * @return List of String objects containing values in desired subfields
     */
    @SuppressWarnings("unchecked")
	public static List<String> getSubfieldStrings(final List<DataField> dataFields, String subfldsStr)
    {
    	List<String> resultSet = new ArrayList<String>();
    	for (DataField df : dataFields) {
    		if (subfldsStr.length() == 1)
    		{
    			// get all instances of the single subfield
	            List<Subfield> subFlds = df.getSubfields(subfldsStr.charAt(0));
	            for (Subfield sf : subFlds)
	            {
	                resultSet.add(sf.getData().trim());
	            }
    		}
    		else
	        {
	            List<Subfield> subFlds = df.getSubfields();
	            for (Subfield sf : subFlds)
	            {
	                if (subfldsStr.indexOf(sf.getCode()) != -1)
	                {
	                	resultSet.add(sf.getData().trim());
	                }
	            }
	        }
    	}
    	return resultSet;
    }


    /**
     * For each occurrence of a marc field in the tags list, extract all
     * subfield data from the field, place it in a single string (individual
     * subfield data separated by spaces) and add the string to the result set.
     */
    @SuppressWarnings("unchecked")
    public static Set<String> getAllSubfields(final Record record, String[] tags)
    {
        Set<String> result = new LinkedHashSet<String>();

        List<VariableField> varFlds = record.getVariableFields(tags);
        for (VariableField vf : varFlds) {

            StringBuilder buffer = new StringBuilder(500);

            DataField df = (DataField) vf;
            if (df != null) {
                List<Subfield> subfields = df.getSubfields();
                for (Subfield sf : subfields) {
                    if (buffer.length() > 0) {
                        buffer.append(" " + sf.getData());
                    } else {
                        buffer.append(sf.getData());
                    }
                }
            }
            if (buffer.length() > 0)
                result.add(buffer.toString());
        }

        return result;
    }

    /**
	 * extract all the subfields requested in requested marc fields. Each
	 * instance of each marc field will be put in a separate result (but the
	 * subfields will be concatenated into a single value for each marc field)
	 *
	 * @param record
	 *            marc record object
	 * @param fieldSpec -
	 *            the desired marc fields and subfields as given in the
	 *            xxx_index.properties file
	 * @param separator -
	 *            the character to use between subfield values in the solr field
	 *            contents
	 * @return Set of values (as strings) for solr field
	 */
	public static Set<String> getAllSubfields(final Record record, String fieldSpec, String separator)
	{
	    Set<String> result = new LinkedHashSet<String>();

	    String[] fldTags = fieldSpec.split(":");
	    for (int i = 0; i < fldTags.length; i++)
	    {
	        // Check to ensure tag length is at least 3 characters
	        if (fldTags[i].length() < 3)
	        {
	            System.err.println("Invalid tag specified: " + fldTags[i]);
	            continue;
	        }

	        String fldTag = fldTags[i].substring(0, 3);

	        String subfldTags = fldTags[i].substring(3);

	        List<VariableField> marcFieldList = record.getVariableFields(fldTag);
	        if (!marcFieldList.isEmpty())
	        {
	            Pattern subfieldPattern = Pattern.compile(subfldTags.length() == 0 ? "." : subfldTags);
	            for (VariableField vf : marcFieldList)
	            {
	                DataField marcField = (DataField) vf;
	                StringBuilder buffer = new StringBuilder("");
	                List<Subfield> subfields = marcField.getSubfields();
	                for (Subfield subfield : subfields)
	                {
	                    Matcher matcher = subfieldPattern.matcher("" + subfield.getCode());
	                    if (matcher.matches())
	                    {
	                        if (buffer.length() > 0)
	                            buffer.append(separator != null ? separator : " ");
	                        buffer.append(subfield.getData().trim());
	                    }
	                }
	                if (buffer.length() > 0)
	                    result.add(Utils.cleanData(buffer.toString()));
	            }
	        }
	    }

	    return result;
	}


	/**
     * get the contents of a subfield, rigorously ensuring no NPE
     * @param df - DataField of interest
     * @param code - code of subfield of interest
     * @return the contents of the subfield, if it exists; null otherwise
     */
    public static String getSubfieldData(DataField df, char code) {
        String result = null;
        if (df != null) {
            Subfield sf = df.getSubfield(code);
            if (sf != null && sf.getData() != null) {
                result = sf.getData();
            }
        }
        return result;
    }

    /** returns all values of subfield strings of a particular code
     *  contained in the data field
     */
    @SuppressWarnings("unchecked")
    public static List<String> getSubfieldStrings(DataField df, char code) {
        List<Subfield> listSubcode = df.getSubfields(code);
        List<String> vals = new ArrayList<String>(listSubcode.size());
        for (Subfield s : listSubcode) {
            vals.add(s.getData());
        }
        return vals;
    }

	/**
	 * Get the specified subfields from the specified MARC field, returned as a
	 * set of strings to become lucene document field values
	 *
	 * @param record - the marc record object
	 * @param fldTag - the field name, e.g. 245
	 * @param subfldsStr - the string containing the desired subfields
	 * @param separator - the separator string to insert between subfield items (if null, a " " will be used)
	 * @return a Set of String, where each string is the concatenated contents
	 *          of all the desired subfield values from a single instance of the
	 *          fldTag
	 */
	@SuppressWarnings("unchecked")
	public static Set<String> getSubfieldDataAsSet(Record record, String fldTag, String subfldsStr, String separator)
	{
	    Set<String> resultSet = new LinkedHashSet<String>();

	    // Process Leader
	    if (fldTag.equals("000"))
	    {
	        resultSet.add(record.getLeader().marshal());
	        return resultSet;
	    }

	    // Loop through Data and Control Fields
	    // int iTag = new Integer(fldTag).intValue();
	    List<VariableField> varFlds = record.getVariableFields(fldTag);
	    for (VariableField vf : varFlds)
	    {
	        if (!isControlField(fldTag) && subfldsStr != null)
	        {
	            // DataField
	            DataField dfield = (DataField) vf;

	            if (subfldsStr.length() > 1 || separator != null)
	            {
	                // concatenate subfields using specified separator or space
	                StringBuilder buffer = new StringBuilder("");
	                List<Subfield> subFlds = dfield.getSubfields();
	                for (Subfield sf : subFlds)
	                {
	                    if (subfldsStr.indexOf(sf.getCode()) != -1)
	                    {
	                        if (buffer.length() > 0)
	                            buffer.append(separator != null ? separator : " ");
	                        buffer.append(sf.getData().trim());
	                    }
	                }
	                if (buffer.length() > 0)
	                    resultSet.add(buffer.toString());
	            }
	            else
	            {
	                // get all instances of the single subfield
	                List<Subfield> subFlds = dfield.getSubfields(subfldsStr.charAt(0));
	                for (Subfield sf : subFlds)
	                {
	                    resultSet.add(sf.getData().trim());
	                }
	            }
	        }
	        else
	        {
	            // Control Field
	            resultSet.add(((ControlField) vf).getData().trim());
	        }
	    }
	    return resultSet;
	}


	/**
	 * Get the specified substring of subfield values from the specified MARC
	 * field, returned as  a set of strings to become lucene document field values
	 * @param record - the marc record object
	 * @param fldTag - the field name, e.g. 008
	 * @param subfield - the string containing the desired subfields
	 * @param beginIx - the beginning index of the substring of the subfield value
	 * @param endIx - the ending index of the substring of the subfield value
	 * @return the result set of strings
	 */
	@SuppressWarnings("unchecked")
	protected static Set<String> getSubfieldDataAsSet(Record record, String fldTag, String subfield, int beginIx, int endIx)
	{
	    Set<String> resultSet = new LinkedHashSet<String>();

	    // Process Leader
	    if (fldTag.equals("000"))
	    {
	        resultSet.add(record.getLeader().marshal().substring(beginIx, endIx));
	        return resultSet;
	    }

	    // Loop through Data and Control Fields
	    List<VariableField> varFlds = record.getVariableFields(fldTag);
	    for (VariableField vf : varFlds)
	    {
	        if (!isControlField(fldTag) && subfield != null)
	        {
	            // Data Field
	            DataField dfield = (DataField) vf;
	            if (subfield.length() > 1)
	            {
	                // automatic concatenation of grouped subfields
	                StringBuilder buffer = new StringBuilder("");
	                List<Subfield> subFlds = dfield.getSubfields();
	                for (Subfield sf : subFlds)
	                {
	                    if (subfield.indexOf(sf.getCode()) != -1 &&
	                            sf.getData().length() >= endIx)
	                    {
	                        if (buffer.length() > 0)
	                            buffer.append(" ");
	                        buffer.append(sf.getData().substring(beginIx, endIx));
	                    }
	                }
	                resultSet.add(buffer.toString());
	            }
	            else
	            {
	                // get all instances of the single subfield
	                List<Subfield> subFlds = dfield.getSubfields(subfield.charAt(0));
	                for (Subfield sf : subFlds)
	                {
	                    if (sf.getData().length() >= endIx)
	                        resultSet.add(sf.getData().substring(beginIx, endIx));
	                }
	            }
	        }
	        else  // Control Field
	        {
	            String cfldData = ((ControlField) vf).getData();
	            if (cfldData.length() >= endIx)
	                resultSet.add(cfldData.substring(beginIx, endIx));
	        }
	    }
	    return resultSet;
	}


    /**
     * Get the specified subfields from the MARC data field, returned as
     *  a string
     * @param df - DataField from which to get the subfields
     * @param subfldsStr - the string containing the desired subfields
     * @param RTL - true if this is a right to left language.  In this case,
     *  each subfield is prepended due to LTR and MARC end-of-subfield punctuation
     *  is moved from the last character to the first.
     * @return a set of strings of desired subfields concatenated with space separator
	 */
	@SuppressWarnings("unchecked")
	static Set<String> getSubfieldsAsSet(DataField df, String subfldsStr, boolean RTL)
    {
		Set<String> resultSet = new LinkedHashSet<String>();

		if (subfldsStr.length() > 1) {
			// concatenate desired subfields with space separator
			StringBuilder buffer = new StringBuilder();
			List<Subfield> subFlds = df.getSubfields();
			for (Subfield sf : subFlds) {
				if (subfldsStr.contains(String.valueOf(sf.getCode()))) {
// TODO:  clean this up, if this works, or find a way to test it
//            		if (RTL) { // right to left language, but this is LTR field+
//	                    if (buffer.length() > 0)
//	                        buffer.insert(0, ' ');
//	                    buffer.insert(0, sf.getData().trim());
//            		} else { // left to right language
					if (buffer.length() > 0)
						buffer.append(' ');
					buffer.append(sf.getData().trim());
//            		}
				}
			}
			resultSet.add(buffer.toString());
		} else {
        	// for single subfield, each occurrence is separate field in lucene doc
			List<Subfield> subFlds = df.getSubfields(subfldsStr.charAt(0));
			for (Subfield sf : subFlds) {
				resultSet.add(sf.getData().trim());
			}
		}
		return resultSet;
	}


	/**
     * Get the specified subfields from the MARC data field, returned as
     *  a string
     * @param df - DataField from which to get the subfields
     * @param subfldsStr - the string containing the desired subfields
     * @param beginIx - the beginning index of the substring of the subfield value
     * @param endIx - the end index of the substring of the subfield value
     * @param RTL - true if this is a right to left language.  In this case,
     *  each subfield is prepended due to LTR and MARC end-of-subfield punctuation
     *  is moved from the last character to the first.
     * @return a set of strings of desired subfields concatenated with space separator
	 */
	@SuppressWarnings("unchecked")
	static Set<String> getSubfieldsAsSet(DataField df, String subfldsStr, int beginIx, int endIx, boolean RTL)
    {
		Set<String> resultSet = new LinkedHashSet<String>();
		if (subfldsStr.length() > 1) {
			// concatenate desired subfields with space separator
			StringBuilder buffer = new StringBuilder();
			List<Subfield> subFlds = df.getSubfields();
			for (Subfield sf : subFlds) {
				if (subfldsStr.contains(String.valueOf(sf.getCode()))) {
					if (sf.getData().length() >= endIx) {
// TODO:  clean this up, if this works, or find a way to test it
						// if (RTL) { // right to left language
						// if (buffer.length() > 0)
						// buffer.insert(0, ' ');
//                            buffer.insert(0, sf.getData().trim().substring(beginIx, endIx));
						// } else { // left to right language
						if (buffer.length() > 0)
							buffer.append(' ');
                            buffer.append(sf.getData().trim().substring(beginIx, endIx));
						// }
					}
				}
			}
			resultSet.add(buffer.toString());
		} else {
        	// for single subfield, each occurrence is separate field in lucene doc
			List<Subfield> subFlds = df.getSubfields(subfldsStr.charAt(0));
			for (Subfield sf : subFlds) {
				if (sf.getData().length() >= endIx)
            		resultSet.add(sf.getData().trim().substring(beginIx, endIx));
			}
		}
		return resultSet;
	}


	/**
	 * return the value of a subfield, trimmed, or empty string if there is no
	 *  subfield value.
	 */
	public static String getSubfieldTrimmed(DataField df, char subcode) {
		String result = getSubfieldData(df, subcode);
		if (result != null)
			return result.trim();
		else
			return "";
	}


	/**
	 * For each occurrence of a marc field in the fieldSpec list, extract the
	 * contents of all alphabetical subfields, concatenate them with a space
	 * separator and add the string to the result set. Each instance of each
	 * marc field will be put in a separate result.
	 *
	 * @param record - the marc record
	 * @param fieldSpec -
	 *            the marc fields (e.g. 600:655) in which we will grab the
	 *            alphabetic subfield contents for the result set. The field may
	 *            not be a control field (must be 010 or greater)
	 * @return a set of strings, where each string is the concatenated values of
	 *         all the alphabetic subfields.
	 */
	@SuppressWarnings("unchecked")
	public static Set<String> getAllAlphaSubfields(final Record record, String fieldSpec)
	{
	    Set<String> resultSet = new LinkedHashSet<String>();

	    String[] fldTags = fieldSpec.split(":");
	    for (int i = 0; i < fldTags.length; i++)
	    {
	        String fldTag = fldTags[i];
	        if (fldTag.length() < 3 || Integer.parseInt(fldTag) < 10)
	        {
	            System.err.println("Invalid marc field specified for getAllAlphaSubfields: " + fldTag);
	            continue;
	        }

	        List<VariableField> varFlds = record.getVariableFields(fldTag);
	        for (VariableField vf : varFlds)
	        {

	            StringBuilder buffer = new StringBuilder(500);

	            DataField df = (DataField) vf;
	            if (df != null)
	            {
	                List<Subfield> subfields = df.getSubfields();
	                for (Subfield sf : subfields)
	                {
	                    if (Character.isLetter(sf.getCode()))
	                    {
	                        if (buffer.length() > 0) {
	                            buffer.append(" " + sf.getData().trim());
	                        } else {
	                            buffer.append(sf.getData().trim());
	                        }
	                    }
	                }
	            }
	            if (buffer.length() > 0)
	                resultSet.add(buffer.toString());
	        }
	    }

	    return resultSet;
	}


	/**
	 * For each occurrence of a marc field in the fieldSpec list, extract the
	 * contents of all alphabetical subfields, concatenate them with a space
	 * separator and add the string to the result set, handling multiple
	 * occurrences as indicated
	 *
	 * @param record - the marc record
	 * @param fieldSpec -
	 *            the marc fields (e.g. 600:655) in which we will grab the
	 *            alphabetic subfield contents for the result set. The field may
	 *            not be a control field (must be 010 or greater)
	 * @param multOccurs -
	 *            "first", "join" or "all" indicating how to handle multiple
	 *            occurrences of field values
	 * @return a set of strings, where each string is the concatenated values of
	 *         all the alphabetic subfields.
	 */
	public static final Set<String> getAllAlphaSubfields(final Record record, String fieldSpec, String multOccurs)
	{
	    Set<String> result = getAllAlphaSubfields(record, fieldSpec);

	    if (multOccurs.equals("first"))
	    {
	        Set<String> first = new HashSet<String>();
	        for (String r : result)
	        {
	            first.add(r);
	            return first;
	        }
	    }
	    else if (multOccurs.equals("join"))
	    {
	        StringBuilder resultBuf = new StringBuilder();
	        for (String r : result)
	        {
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
     * treats indicator 2 as the number of non-filing indicators to exclude,
     * removes ascii punctuation
     * @param df DataField with ind2 containing # non-filing chars, or has value ' '
     * @param skipSubFldc true if subfield c contents should be skipped
     * @return StringBuilder of the contents of the subfields - with a trailing
     *         space
     */
    @SuppressWarnings("unchecked")
	public static StringBuilder getAlphaSubfldsAsSortStr(DataField df, boolean skipSubFldc)
    {
        StringBuilder result = new StringBuilder();
        int nonFilingInt = getInd2AsInt(df);
        boolean firstSubfld = true;

        List<Subfield> subList = df.getSubfields();
        for (Subfield sub : subList)
        {
            char subcode = sub.getCode();
            if (Character.isLetter(subcode) && (!skipSubFldc || subcode != 'c'))
            {
                String data = sub.getData();
                if (firstSubfld)
                {
                    if (nonFilingInt < data.length() - 1)
                        data = data.substring(nonFilingInt);
                    firstSubfld = false;
                }
                // eliminate ascii punctuation marks from sorting as well
                result.append(data.replaceAll("\\p{Punct}*", "").trim() + ' ');
            }
        }
        return result;
    }

	/**
	 * find the first instance of the control field within a record and return
	 * its contents as a string. If the field is a DataField, return the
	 *  contents of the specified subfield, or, if unspecified, of subfield 'a'
	 * @param record - record to search
	 * @param tag - tag number to search for
	 */
	public static String getControlFieldData(Record record, String tag)
	{
		if (tag != null && tag.length() >= 3)
		{
		    String fieldTag = tag.substring(0,3);
		    List<VariableField> vfList = record.getVariableFields(fieldTag);
		    for (VariableField vf : vfList)
		    {
		        if (vf instanceof ControlField)
		        {
		            ControlField cf = (ControlField) vf;
		            if (cf.getTag().matches(fieldTag))
		                return((String)cf.getData());
		        }
		        else if (vf instanceof DataField)
		        {
		            DataField df = (DataField)vf;
		            if (df.getTag().matches(fieldTag))
		            {
		                char subfieldtag = 'a';
		                if (tag.length() > 3)
		                	subfieldtag = tag.charAt(3);
		                Subfield sf = df.getSubfield(subfieldtag);
		                if (sf != null)
		                	return(sf.getData());
		            }
		        }
		    }
		}
	    return(null);
	}


// -------------------------- indicator methods --------------------------------

	    /**
	 * @param df a DataField
	 * @return the integer (0-9, 0 if blank or other) in the 2nd indicator
	 */
	public static int getInd2AsInt(DataField df)
	{
	    char ind2char = df.getIndicator2();
	    int result = 0;
	    if (Character.isDigit(ind2char))
	        result = Integer.valueOf(String.valueOf(ind2char));
	    return result;
	}


		/**
	 * remove trailing punctuation (default trailing characters to be removed)
	 *    See org.solrmarc.tools.Utils.cleanData() for details on the
	 *     punctuation removal
	 * @param record marc record object
	 * @param fieldSpec - the field to have trailing punctuation removed
	 * @return Set of strings containing the field values with trailing
	 *         punctuation removed
	 */
	public static Set<String> removeTrailingPunct(Record record, String fieldSpec)
	{
	    Set<String> result = getFieldList(record, fieldSpec);
	    Set<String> newResult = new LinkedHashSet<String>();
	    for (String s : result)
	    {
	        newResult.add(Utils.cleanData(s));
	    }
	    return newResult;
	}





// ----------------- combining record methods ----------------------------------


	/**
	 * merge the given fields from nextRecord into resultRecord
	 *
	 * FIXME:  the following can be changed if there's a utility to copy
	 *  record objects
	 * Side Effect:
	 *   NOTE:  the method changes the first param's value in addition to
	 *   providing the result (which is the same object as the first param's new value)
	 *
	 * @param resultRecord the record recordToCopyFrom receive more fields
	 * @param recordToCopyFrom the record from which to copy fields
	 * @param fieldsToCopy the fields to be copied, as a regular expression (e.g. "852|866|867")
	 * @return the currentRecord with the matching fields added from the nextRecord
	 */
	public static Record combineRecords(Record resultRecord, Record recordToCopyFrom, String fieldsToCopy)
	{
	    List<VariableField> recToCopyFromAllFields = recordToCopyFrom.getVariableFields();
	    for (VariableField vf : recToCopyFromAllFields)
	    {
	    	// FIXME:  it would be good to have some error checking on the fieldsToCopy expression passed in
	        if (vf.getTag().matches(fieldsToCopy))
	            resultRecord.addVariableField(vf);
	    }
	    return(resultRecord);
	}


	/**
	 * merge the given fields from nextRecord into resultRecord
	 *
	 * Side Effect:
	 *   NOTE:  the method changes the first param's value in addition to
	 *   providing the result (which is the same object as the first param's new value)
	 *
	 * @param resultRecord - receives the fields from recordToCopyFrom
	 * @param recordToCopyFrom - the record from which to copy fields
	 * @param fieldsToCopy the fields to be copied, as a regular expression (e.g. "852|866|867")
	 * @param fieldToInsertBefore the recordToCopyFrom fields should be inserted before the first occurrence of this field in resultRecord.  Must be a tag as a 3 character String or null.
	 * @return the currentRecord with the matching fields added from the nextRecord
	 */
	public static Record combineRecords(Record resultRecord, Record recordToCopyFrom, String fieldsToCopy, String fieldToInsertBefore)
	{
	    List<VariableField> postInsertFields = new ArrayList<VariableField>();

	    if (fieldToInsertBefore != null && fieldToInsertBefore.length() == 3)
		{
		    // remove existing postInsertFields (temporarily)
		    List<VariableField> resultRecAllFields = resultRecord.getVariableFields();
		    boolean foundMatch = false;
		    for (VariableField vf : resultRecAllFields)
		    {
		    	// FIXME:  it would be good to have some error checking on the fieldsToCopy expression passed in
		        if (foundMatch || vf.getTag().matches(fieldToInsertBefore))
		        {
		        	foundMatch = true;
		            postInsertFields.add(vf);
		            resultRecord.removeVariableField(vf);
		        }
		    }
		}
	    else
		    logger.info("fieldToInsertBefore argument not usable; will put record2's fields at end of record1");

	    // copy desired fields to resultRecord
	    resultRecord = combineRecords(resultRecord, recordToCopyFrom, fieldsToCopy);

	    // add back the temporarily removed fields
	    for (VariableField vf : postInsertFields)
	    {
	        resultRecord.addVariableField(vf);
	    }

	    return(resultRecord);
	}


// -------------------------- general methods ----------------------------------

	/**
	 * An MHLD record is identified by the Leader/06 value. If leader/06 is any of these:
	 *	u - Unknown
	 *	v - Multipart item holdings
	 *	x - Single-part item holdings
	 *	y - Serial item holdings
	 *
	 *	then the record is a MHLD record.
	 */
	public static boolean isMHLDRecord(Record record)
	{
		// this should be the same as record.getLeader().marshal().charAt(6);
		char typeOfRec = record.getLeader().getTypeOfRecord();
		switch (typeOfRec)
		{
			case 'u':
			case 'v':
			case 'x':
			case 'y':
				return true;
		}
		return false;
	}


	/**
	 * get the era field values from 045a as a Set of Strings
	 */
	public static Set<String> getEra(Record record)
	{
	    Set<String> result = new LinkedHashSet<String>();
	    String eraField = getFirstFieldVal(record, "045a");
	    if (eraField == null)
	        return result;

	    if (eraField.length() == 4)
	    {
	        eraField = eraField.toLowerCase();
	        char eraStart1 = eraField.charAt(0);
	        char eraStart2 = eraField.charAt(1);
	        char eraEnd1 = eraField.charAt(2);
	        char eraEnd2 = eraField.charAt(3);
	        if (eraStart2 == 'l')
	            eraEnd2 = '1';
	        if (eraEnd2 == 'l')
	            eraEnd2 = '1';
	        if (eraStart2 == 'o')
	            eraEnd2 = '0';
	        if (eraEnd2 == 'o')
	            eraEnd2 = '0';
	        return getEra(eraStart1, eraStart2, eraEnd1, eraEnd2);
	    }
	    else if (eraField.length() == 5)
	    {
	        char eraStart1 = eraField.charAt(0);
	        char eraStart2 = eraField.charAt(1);

	        char eraEnd1 = eraField.charAt(3);
	        char eraEnd2 = eraField.charAt(4);
	        char gap = eraField.charAt(2);
	        if (gap == ' ' || gap == '-')
	            return getEra(eraStart1, eraStart2, eraEnd1, eraEnd2);
	    }
	    else if (eraField.length() == 2)
	    {
	        char eraStart1 = eraField.charAt(0);
	        char eraStart2 = eraField.charAt(1);
	        if (eraStart1 >= 'a' && eraStart1 <= 'y' &&
	                eraStart2 >= '0' && eraStart2 <= '9')
	            return getEra(eraStart1, eraStart2, eraStart1, eraStart2);
	    }
	    return result;
	}


	/**
	 * get the two eras indicated by the four passed characters, and add them
	 *  to the result parameter (which is a set).  The characters passed in are
	 *  from the 045a.
	 */
	private static Set<String> getEra(char eraStart1, char eraStart2, char eraEnd1, char eraEnd2)
	{
	    Set<String> result = new LinkedHashSet<String>();
	    if (eraStart1 >= 'a' && eraStart1 <= 'y' && eraEnd1 >= 'a' && eraEnd1 <= 'y')
	    {
	        for (char eraVal = eraStart1; eraVal <= eraEnd1; eraVal++)
	        {
	            if (eraStart2 != '-' || eraEnd2 != '-')
	            {
	                char loopStart = (eraVal != eraStart1) ? '0' : Character.isDigit(eraStart2) ? eraStart2 : '0';
	                char loopEnd = (eraVal != eraEnd1) ? '9' : Character.isDigit(eraEnd2) ? eraEnd2 : '9';
	                for (char eraVal2 = loopStart; eraVal2 <= loopEnd; eraVal2++)
	                {
	                    result.add("" + eraVal + eraVal2);
	                }
	            }
	            result.add("" + eraVal);
	        }
	    }
	    return result;
	}


	/**
	 * Get Set of Strings as indicated by tagStr. For each field spec in the
	 * tagStr that is NOT about bytes (i.e. not a 008[7-12] type fieldspec), the
	 * result string is the concatenation of all the specific subfields.
	 *
	 * @param record -
	 *            the marc record object
	 * @param tagStr
	 *            string containing which field(s)/subfield(s) to use. This is a
	 *            series of: marc "tag" string (3 chars identifying a marc
	 *            field, e.g. 245) optionally followed by characters identifying
	 *            which subfields to use. Separator of colon indicates a
	 *            separate value, rather than concatenation. 008[5-7] denotes
	 *            bytes 5-7 of the 008 field (0 based counting) 100[a-cf-z]
	 *            denotes the bracket pattern is a regular expression indicating
	 *            which subfields to include. Note: if the characters in the
	 *            brackets are digits, it will be interpreted as particular
	 *            bytes, NOT a pattern. 100abcd denotes subfields a, b, c, d are
	 *            desired.
	 * @return the contents of the indicated marc field(s)/subfield(s), as a set
	 *         of Strings.
	 */
	public static Set<String> getFieldList(Record record, String tagStr)
	{
	    String[] tags = tagStr.split(":");
	    Set<String> result = new LinkedHashSet<String>();
	    for (int i = 0; i < tags.length; i++)
	    {
	        // Check to ensure tag length is at least 3 characters
	        if (tags[i].length() < 3)
	        {
	            System.err.println("Invalid tag specified: " + tags[i]);
	            continue;
	        }

	        // Get Field Tag
	        String tag = tags[i].substring(0, 3);
	        boolean linkedField = false;
	        if (tag.equals("LNK"))
	        {
	            tag = tags[i].substring(3, 6);
	            linkedField = true;
	        }
	        // Process Subfields
	        String subfield = tags[i].substring(3);
	        boolean havePattern = false;
	        int subend = 0;
	        // brackets indicate parsing for individual characters or as pattern
	        int bracket = tags[i].indexOf('[');
	        if (bracket != -1)
	        {
	            String sub[] = tags[i].substring(bracket + 1).split("[\\]\\[\\-, ]+");
	            try
	            {
	                // if bracket expression is digits, expression is treated as character positions
	                int substart = Integer.parseInt(sub[0]);
	                subend = (sub.length > 1) ? Integer.parseInt(sub[1]) + 1 : substart + 1;
	                String subfieldWObracket = subfield.substring(0, bracket-3);
	                result.addAll(getSubfieldDataAsSet(record, tag, subfieldWObracket, substart, subend));
	            }
	            catch (NumberFormatException e)
	            {
	                // assume brackets expression is a pattern such as [a-z]
	                havePattern = true;
	            }
	        }
	        if (subend == 0) // don't want specific characters.
	        {
	            String separator = null;
	            if (subfield.indexOf('\'') != -1)
	            {
	                separator = subfield.substring(subfield.indexOf('\'') + 1, subfield.length() - 1);
	                subfield = subfield.substring(0, subfield.indexOf('\''));
	            }

	            if (havePattern)
	                if (linkedField)
	                    result.addAll(getLinkedFieldValue(record, tag, subfield, separator));
	                else
	                    result.addAll(getAllSubfields(record, tag + subfield, separator));
	            else if (linkedField)
	                result.addAll(getLinkedFieldValue(record, tag, subfield, separator));
	            else
	                result.addAll(getSubfieldDataAsSet(record, tag, subfield, separator));
	        }
	    }
	    return result;
	}


	/**
	 * Get all field values specified by tagStr, joined as a single string.
	 * @param record - the marc record object
	 * @param tagStr string containing which field(s)/subfield(s) to use. This
	 *  is a series of: marc "tag" string (3 chars identifying a marc field,
	 *  e.g. 245) optionally followed by characters identifying which subfields
	 *  to use.
	 * @param separator string separating values in the result string
	 * @return single string containing all values of the indicated marc
	 *         field(s)/subfield(s) concatenated with separator string
	 */
	public static String getFieldVals(Record record, String tagStr, String separator)
	{
	    Set<String> result = getFieldList(record, tagStr);
	    return org.solrmarc.tools.Utils.join(result, separator);
	}


	/**
	 * Get the first value specified by the tagStr
	 * @param record - the marc record object
	 * @param tagStr string containing which field(s)/subfield(s) to use. This
	 *  is a series of: marc "tag" string (3 chars identifying a marc field,
	 *  e.g. 245) optionally followed by characters identifying which subfields
	 *  to use.
	 * @return first value of the indicated marc field(s)/subfield(s) as a string
	 */
	public static String getFirstFieldVal(Record record, String tagStr)
	{
	    Set<String> result = getFieldList(record, tagStr);
	    Iterator<String> iter = result.iterator();
	    if (iter.hasNext())
	        return iter.next();
	    else
	        return null;
	}


	/**
	 * Get the 245a (and 245b, if it exists, concatenated with a space between
	 *  the two subfield values), with trailing punctuation removed.
	 *    See org.solrmarc.tools.Utils.cleanData() for details on the
	 *     punctuation removal
	 * @param record - the marc record object
	 * @return 245a, b, and k values concatenated in order found, with trailing punct removed. Returns empty string if no suitable title found.
	 */
	public static String getTitle(Record record)
	{
	    DataField titleField = (DataField) record.getVariableField("245");
	    if ( titleField == null) {
	      return "";
	    }

	    StringBuilder titleBuilder = new StringBuilder();

	    Iterator<Subfield> iter = titleField.getSubfields().iterator();
	    while ( iter.hasNext() ) {
	      Subfield f = iter.next();
	      char code = f.getCode();
	      if ( code == 'a' || code == 'b' || code == 'k' ) {
	         titleBuilder.append(f.getData());
	      }
	    }

	    return Utils.cleanData(titleBuilder.toString());
	}


	/**
	 * Get the title (245ab) from a record, without non-filing chars as
	 * specified in 245 2nd indicator, and lowercased.
	 * see org.solrmarc.index.SolrIndexer.getTitle(Record)
	 * @param record - the marc record object
	 * @return 245a and 245b values concatenated, with trailing punct removed,
	 *         and with non-filing characters omitted. Null returned if no
	 *         title can be found.
	 */
	public static String getSortableTitle(Record record)
	{
	    DataField titleField = (DataField) record.getVariableField("245");
	    if (titleField == null)
	        return "";

	    int nonFilingInt = getInd2AsInt(titleField);

	    String title = getTitle(record);
	    title = title.toLowerCase();

	    //Skip non-filing chars, if possible.
	    if ( title.length() > nonFilingInt )  {
	      title = title.substring(nonFilingInt);
	    }

	    if ( title.length() == 0) {
	      return null;
	    }

	    return title;
	}


	/**
	 * returns string for author sort:  a string containing
	 *  1. the main entry author, if there is one
	 *  2. the main entry uniform title (240), if there is one - not including
	 *    non-filing chars as noted in 2nd indicator
	 * followed by
	 *  3.  the 245 title, not including non-filing chars as noted in ind 2
	 */
	public static String getSortableAuthor(final Record record)
	{
	    StringBuilder resultBuf = new StringBuilder();

	    DataField df = (DataField) record.getVariableField("100");
	    // main entry personal name
	    if (df != null)
	        resultBuf.append(getAlphaSubfldsAsSortStr(df, false));

	    df = (DataField) record.getVariableField("110");
	    // main entry corporate name
	    if (df != null)
	        resultBuf.append(getAlphaSubfldsAsSortStr(df, false));

	    df = (DataField) record.getVariableField("111");
	    // main entry meeting name
	    if (df != null)
	        resultBuf.append(getAlphaSubfldsAsSortStr(df, false));

	    // need to sort fields missing 100/110/111 last
	    if (resultBuf.length() == 0)
	    {
	        resultBuf.append(Character.toChars(Character.MAX_CODE_POINT));
	        resultBuf.append(' '); // for legibility in luke
	    }

	    // uniform title, main entry
	    df = (DataField) record.getVariableField("240");
	    if (df != null)
	        resultBuf.append(getAlphaSubfldsAsSortStr(df, false));

	    // 245 (required) title statement
	    df = (DataField) record.getVariableField("245");
	    if (df != null)
	        resultBuf.append(getAlphaSubfldsAsSortStr(df, true));

	    // Solr field properties should convert to lowercase
	    return resultBuf.toString().trim();
	}


	/**
	 * Return the date in 260c as a string
	 * @param record - the marc record object
	 * @return 260c, "cleaned" per org.solrmarc.tools.Utils.cleanDate()
	 */
	public static String getDate(Record record)
	{
	    String date = getFieldVals(record, "260c", ", ");
	    if (date == null || date.length() == 0)
	        return (null);
	    return DateUtils.getYearFromString(date);
	}


	/**
	 * Stub (to be overridden) default simply calls getDate()
	 * @param record - the marc record object
	 * @return 260c, "cleaned" per org.solrmarc.tools.Utils.cleanDate()
	 */
	public static String getPublicationDate(final Record record)
	{
	    return(getDate(record));
	}


    /**
     * returns the URLs for the full text of a resource described by the record
     *
     * @param record
     * @return Set of Strings containing full text urls, or empty set if none
     */
    @SuppressWarnings("unchecked")
    public static Set<String> getFullTextUrls(final Record record)
    {
        Set<String> resultSet = new LinkedHashSet<String>();

        List<VariableField> list856 = record.getVariableFields("856");
        for (VariableField vf : list856)
        {
            DataField df = (DataField) vf;
            List<String> possUrls = MarcUtils.getSubfieldStrings(df, 'u');
            if (possUrls.size() > 0)
            {
                char ind2 = df.getIndicator2();
                switch (ind2)
                {
                    case '0':
                        resultSet.addAll(possUrls);
                        break;
                    case '2':
                        break;
                    default:
                        if (!MarcUtils.isSupplementalUrl(df))
                            resultSet.addAll(possUrls);
                        break;
                }
            }
        }

        return resultSet;
    }

   /**
	 * returns the URLs for supplementary information (rather than fulltext)
	 *
	 * @param record
	 * @return Set of Strings containing supplementary urls, or empty string if
	 *         none
	 */
	@SuppressWarnings("unchecked")
	public static Set<String> getSupplUrls(final Record record)
	{
	    Set<String> resultSet = new LinkedHashSet<String>();

	    List<VariableField> list856 = record.getVariableFields("856");
	    for (VariableField vf : list856)
	    {
	        DataField df = (DataField) vf;
	        List<String> possUrls = getSubfieldStrings(df, 'u');
	        char ind2 = df.getIndicator2();
	        switch (ind2)
	        {
	            case '2':
	                resultSet.addAll(possUrls);
	                break;
	            case '0':
	                break;
	            default:
	                if (isSupplementalUrl(df))
	                    resultSet.addAll(possUrls);
	                break;
	        }
	    }
	    return resultSet;
	}


	/**
	 * return true if passed 856 field contains a supplementary url (rather than
	 * a fulltext URL. Determine by presence of "table of contents" or "sample
	 * text" string (ignoring case) in subfield 3 or z. Note: Called only when
	 * second indicator is not 0 or 2.
	 */
	public static boolean isSupplementalUrl(DataField f856)
	{
	    boolean supplmntl = false;
	    List<String> list3z = getSubfieldStrings(f856, '3');
	    list3z.addAll(getSubfieldStrings(f856, 'z'));
	    for (String s : list3z)
	    {
	        if (s.toLowerCase().contains("table of contents")
	                || s.toLowerCase().contains("abstract")
	                || s.toLowerCase().contains("description")
	                || s.toLowerCase().contains("sample text"))
	            supplmntl = true;
	    }
	    return supplmntl;
	}

    /**
     * Return a binary string representation of the marc Record object
     * @param record marc record object to be written
     * @return string containing binary (UTF-8 encoded) representation of marc
     *         record object.
     */
    public static String getRecordAsBinaryStr(Record record)
    {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        MarcWriter writer = new MarcStreamWriter(out, "UTF-8", true);
        writer.write(record);
        writer.close();

        String result = null;
        try
        {
            result = out.toString("UTF-8");
        }
        catch (UnsupportedEncodingException e)
        {
            // e.printStackTrace();
            logger.error(e.getCause());
        }
        return result;
    }

    /**
     * Return a json string representation of the marc Record object
     * @param record marc record object to be written
     * @return string containing binary (UTF-8 encoded) representation of marc
     *         record object.
     */
    public static String getRecordAsJsonStr(Record record, boolean MARCinJSON)
    {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        MarcWriter writer = new MarcJsonWriter(out, (MARCinJSON) ? MarcJsonWriter.MARC_IN_JSON : MarcJsonWriter.MARC_JSON);
        writer.write(record);
        writer.close();

        String result = null;
        try
        {
            result = out.toString("UTF-8");
        }
        catch (UnsupportedEncodingException e)
        {
            // e.printStackTrace();
            logger.error(e.getCause());
        }
        return result;
    }

    /**
     * Return a marcxml (http://www.loc.gov/standards/marcxml/schema/MARC21slim.xsd) string representation of the marc Record object
     * @param record marc record object to be written
     * @return String containing MarcXML representation of marc record object
     */
    public static String getRecordAsMarcXmlStr(Record record)
    {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        // TODO: see if this works better
        // MarcWriter writer = new MarcXmlWriter(out, false);
        MarcWriter writer = new MarcXmlWriter(out, "UTF-8");
        writer.write(record);
        writer.close();

        String tmp = null;
        try
        {
            tmp = out.toString("UTF-8");
        }
        catch (UnsupportedEncodingException e)
        {
            // e.printStackTrace();
            logger.error(e.getCause());
        }
        return tmp;
    }



}
