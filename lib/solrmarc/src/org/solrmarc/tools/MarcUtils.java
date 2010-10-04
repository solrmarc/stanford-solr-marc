package org.solrmarc.tools;

import java.util.*;

import org.marc4j.marc.*;

public class MarcUtils {

    /**
     * Default Constructor,  private, so it can't be instantiated by other objects
     */    
    private MarcUtils(){ }

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
