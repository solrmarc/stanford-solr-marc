package org.solrmarc.tools;

import org.marc4j.marc.*;

import java.util.*;

public class MarcUtils {

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
        List<String> vals = new ArrayList(listSubcode.size());
        for (Subfield s : listSubcode) {
            vals.add(s.getData());
        }
        return vals;
    }
    
    /**
     * treats indicator 2 as the number of non-filing indicators to exclude,
     * removes ascii punctuation
     * @param DataField with ind2 containing # non-filing chars, or has value ' '
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
		                	subfieldtag = tag.charAt(4);
		                Subfield sf = df.getSubfield(subfieldtag);
		                if (sf != null) 
		                	return(sf.getData());
		            }
		        }
		    }
		}
	    return(null);
	}


	/**
	 * merge the given fields from nextRecord into resultRecord
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
	        if (vf.getTag().matches(fieldsToCopy))
	            resultRecord.addVariableField(vf);
	    }
	    return(resultRecord);
	}


	/**
	 * merge the given fields from nextRecord into resultRecord
	 * @param resultRecord the record recordToCopyFrom receive more fields
	 * @param recordToCopyFrom the record from which to copy fields
	 * @param fieldsToCopy the fields to be copied, as a regular expression (e.g. "852|866|867")
	 * @param fieldToInsertBefore fields(ToMerge) from recordToCopyFrom should be inserted before occurrences of this field in resultRecord
	 * @return the currentRecord with the matching fields added from the nextRecord
	 */
	public static Record combineRecords(Record resultRecord, Record recordToCopyFrom, String fieldsToCopy, String fieldToInsertBefore)
	{
	    List<VariableField> postInsertFields = new ArrayList<VariableField>();

	    if (fieldToInsertBefore != null && fieldToInsertBefore.length() == 3)
		{
		    // remove existing postInsertFields (temporarily)
		    List<VariableField> resultRecAllFields = resultRecord.getVariableFields();
		    for (VariableField vf : resultRecAllFields)
		    {
		        if (vf.getTag().matches(fieldToInsertBefore))
		        {
		            postInsertFields.add(vf);
		            resultRecord.removeVariableField(vf);
		        }
		    }
		}
	
	    // copy desired fields to resultRecord
	    resultRecord = combineRecords(resultRecord, recordToCopyFrom, fieldsToCopy);
	    
	    // add back the temporarily removed fields
	    for (VariableField vf : postInsertFields)
	    {
	        resultRecord.addVariableField(vf);
	    }

	    return(resultRecord);
	}
	
	
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
		char leaderChar06 = record.getLeader().toString().charAt(6);
		switch (leaderChar06)
		{
			case 'u':
			case 'v':
			case 'x':
			case 'y':
				return true;
		}
		return false;
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

	
}
