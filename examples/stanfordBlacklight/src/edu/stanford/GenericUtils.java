package edu.stanford;

import java.io.*;
import java.util.*;

import org.marc4j.marc.*;

import org.solrmarc.tools.Utils;

/**
 * Utility functions for StanfordIndexer in SolrMarc project
 * 
 * @author Naomi Dushay
 */
public class GenericUtils {
	
	/**
	 * Default Constructor: private, so it can't be instantiated by other objects
	 */	
	private GenericUtils(){ }

	// Not used?  2009-11-16
	/**
	 * Removes trailing periods or commas at the ends of the value strings
	 * indicated by the fieldSpec argument
	 * @param record
     * @param fieldSpec - which marc fields / subfields to use as values
     * @return Set of strings containing values without trailing commas or periods
	 */
/*
	public static Set<String> removeTrailingPunct(final Record record, final String fieldSpec) 
    {
		Set<String> resultSet = new HashSet<String>();
		for (String val : SolrIndexer.getFieldList(record, fieldSpec)) {
    		if (val.endsWith(",") || val.endsWith(".") || val.endsWith("/")&& val.length() > 1)
				resultSet.add(val.substring(0, val.length() - 1).trim());
			else
				resultSet.add(val.trim());
		}

		return resultSet;
	}
*/
	
	/**
	 * Get the vernacular (880) fields which corresponds to the marc field
	 *  in the 880 subfield 6 linkage 
     * @param marcField - which field to be matched by 880 fields 
	 */
	@SuppressWarnings("unchecked")
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
			String sub6 = getSubfieldTrimmed(df880, '6');
			int dashIx = sub6.indexOf('-');
			if ((dashIx == 3) && marcField.equals(sub6.substring(0, 3)))
				resultSet.add(df880);
		}
		return (resultSet);
	}

	/**
     * Get the specified subfields from the MARC data field, returned as
     *  a string
     * @param df - DataField from which to get the subfields
     * @param subfldsStr - the string containing the desired subfields
     * @param RTL - true if this is a right to left language.  In this case, 
     *  each subfield is prepended due to LTR and MARC end-of-subfield punctuation
     *  is moved from the last character to the first.
     * @returns a set of strings of desired subfields concatenated with space separator
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
     * @param endIx - the endind index of the substring of the subfield value
     * @param RTL - true if this is a right to left language.  In this case, 
     *  each subfield is prepended due to LTR and MARC end-of-subfield punctuation
     *  is moved from the last character to the first.
     * @returns a set of strings of desired subfields concatenated with space separator
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
	static String getSubfieldTrimmed(DataField df, char subcode) {
		String result = Utils.getSubfieldData(df, subcode);
		if (result != null)
			return result.trim();
		else
			return "";
	}
	
	/**
	 * load list contained in the file.  list file should contain a series of 
	 * values (one per line);  comments are preceded by a '#' character
	 * @param listFilename the name of the file containing the data
	 * @param possiblePaths array of paths in which to seek the list file
	 * @return a List of the values read from the file
	 */
	static List<String> loadPropertiesList(String[] possiblePaths, String listFilename)   {
		List<String> result = new ArrayList<String>();
        InputStream propFileIS = Utils.getPropertyFileInputStream(possiblePaths, listFilename);
        BufferedReader propFileBR = new BufferedReader(new InputStreamReader(propFileIS));
        String line;
        try
        {
            while ((line = propFileBR.readLine()) != null)
            {
                String linePieces[] = line.split("#");
                String value = linePieces[0].trim();
                if (value.length() > 0)
                	result.add(value);
            }
        }
        catch (IOException e)
        {
        	System.err.println("error reading " + listFilename);
            e.printStackTrace();
        }
        return result;
    }

}
