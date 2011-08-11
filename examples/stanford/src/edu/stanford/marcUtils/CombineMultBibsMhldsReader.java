package edu.stanford.marcUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Comparator;
import java.util.List;

import org.apache.log4j.Logger;
import org.marc4j.MarcPermissiveStreamReader;
import org.marc4j.MarcReader;
import org.marc4j.marc.Record;
import org.marc4j.marc.VariableField;
import org.solrmarc.tools.MarcUtils;
import org.solrmarc.tools.StringNaturalCompare;

/**
 * Binary Marc records have a maximum size of 99999 bytes.  This is true for 
 * both Bibliographic and MHLD records.  In the data dumps from the Sirsi/Dynix 
 * system if a record with all of its holdings information attached would be 
 * greater that that size, the records is written out multiple times with each 
 * subsequent record containing a subset of the total holdings information.
 * 
 * This class is designed for the following type of file:
 *  - there may be more than one bib record to accommodate long bib records 
 *  - corresponding mhld record(s) will follow the bib(s) if there are any mhld
 *      records
 *  - there may be multiple mhld records - one for each lib-loc combo, and if
 *      a single lib/loc combo has lots of holdings, there may be mult mhld 
 *      records for that single lib/loc combo.
 *
 * If any of the desired MHLD fields clash with any existing bib fields, the
 * bib fields are removed before the MHLD fields are added to the record. 
 * 
 * Only the additional holdings information from the multiple records is added
 * - the other fields are not repeated in the result.
 *
 * @author Naomi Dushay  based on Bob Haschart's MarcCombiningReader
 * @see org.solrmarc.marc.MarcCombiningReader
 */
public class CombineMultBibsMhldsReader implements MarcReader 
{
    /** default control field to use for record matching */
	public static final String DEFAULT_FIELD_TO_MATCH = "001";
	
    /** default regular expression (as a string) of Bib fields to be merged when there are multiple bib records */
    public static final String DEFAULT_BIB_FLDS_TO_MERGE = "999";

    /** default regular expression (as a string) of MHLD fields to be merged into the bib record */
    public static final String DEFAULT_MHLD_FLDS_TO_MERGE = "852|853|863|866|867|868";
    
    static Logger logger = Logger.getLogger(CombineMultBibsMhldsReader.class.getName());

    /** used to determine if the match field values are equal */
    static Comparator<String> MATCH_FIELD_COMPARATOR = new StringNaturalCompare();


    /** the field in the current record to use for matching purposes */
    private String currentCntlFldToMatch = DEFAULT_FIELD_TO_MATCH;
    		
    /** the field in the look-ahead record to use for matching purposes */
    private String lookAheadCntlFldToMatch = DEFAULT_FIELD_TO_MATCH;

    /** regular expression (as a string) of Bib fields to be merged into the bib record */
    private String bibFldsToMerge = null;
    
    /** regular expression (as a string) of MHLD fields to be merged into the bib record */
    private String mhldFldsToMerge = null;

    /** what we will use to read the file and look ahead as we go for additional
     *  records that need to be merged into the current one. */
    private MarcReader marcReader = null;

    /** the last Record read, but not yet compared with the following record */
    Record currentRecord = null;
    
    
    Record nextRecord = null;

    
    boolean verbose = false;
    boolean veryverbose = false;
    
    
	/**
	 * 
	 * @param marcRecsFilename - the name of the file to be read
     * @param currentCntlFldToMatch - the field in the current record to use for matching purposes (null for DEFAULT_CONTROL_FIELD).
     * @param lookAheadCntlFldToMatch - the field in the look-ahead record to use for matching purposes (null for DEFAULT_CONTROL_FIELD).
	 * @param bibFldsToMerge - a regular expression (as a string) of Bib fields to be merged when there are multiple bib records (e.g. "998|999"; null for DEFAULT_BIB_FLDS_TO_MERGE)
	 * @param mhldFldsToMerge - a regular expression (as a string) of MHLD fields to be merged into the bib record (e.g. "852|863|866"; null for DEFAULT_MHLD_FLDS_TO_MERGE)
	 * @param permissive - if true, try to recover from errors, including records with errors, when possible
	 * @param defaultEncoding - possible values are MARC8, UTF-8, UNIMARC, BESTGUESS
	 * @param toUtf8 - if true, this will convert records in our import file from (MARC8) encoding into UTF-8 encoding on output to index
	 */
	public CombineMultBibsMhldsReader(String marcRecsFilename, 
										String currentCntlFldToMatch, 
										String lookAheadCntlFldToMatch,
										String bibFldsToMerge,
										String mhldFldsToMerge,
										boolean permissive,
										String defaultEncoding,
										boolean toUtf8
										) 
				throws FileNotFoundException
	{
        marcReader = new MarcPermissiveStreamReader(new FileInputStream(new File(marcRecsFilename)), permissive, toUtf8, defaultEncoding);
		
		if (currentCntlFldToMatch != null)
			this.currentCntlFldToMatch = currentCntlFldToMatch;
		if (lookAheadCntlFldToMatch != null)
			this.lookAheadCntlFldToMatch = lookAheadCntlFldToMatch;
		
		if (bibFldsToMerge == null)
			this.bibFldsToMerge = DEFAULT_BIB_FLDS_TO_MERGE;
		else
			this.bibFldsToMerge = bibFldsToMerge;
		if (mhldFldsToMerge == null)
			this.mhldFldsToMerge = DEFAULT_MHLD_FLDS_TO_MERGE;
		else
			this.mhldFldsToMerge = mhldFldsToMerge;
	}
	
	
    
	/**
	 * @param marcRecsFilename - the name of the file to be read
	 * @param permissive - if true, try to recover from errors, including records with errors, when possible
	 * @param defaultEncoding - possible values are MARC8, UTF-8, UNIMARC, BESTGUESS
	 * @param toUtf8 - if true, this will convert records in our import file from (MARC8) encoding into UTF-8 encoding on output to index
	 */
	public CombineMultBibsMhldsReader(String marcRecsFilename, 
										boolean permissive,
										String defaultEncoding,
										boolean toUtf8
										) 
				throws FileNotFoundException
	{
		this (marcRecsFilename, null, null, null, null, permissive, defaultEncoding, toUtf8);
	}
	

	/**
	 * @param marcRecsFilename - the name of the file to be read
	 */
	public CombineMultBibsMhldsReader(String marcRecsFilename) 
				throws FileNotFoundException
	{
		this (marcRecsFilename, null, null, null, null, true, "MARC8", false);
	}

	
	
	/**
	 * Returns true if the iteration has more records, false otherwise.
	 */
	public boolean hasNext()
	{
        if (currentRecord == null) 
            currentRecord = next(); 

        return (currentRecord != null);
    }

	
    /**
     * Returns the next record in the iteration.
     *  
     * @return Record - the record object
     */
	public Record next()
    {
        Record bibRec = null;
        /*
        if (marcReader != null) 
        {
// for ALL matching bib records until no match, or until matching mhld or EOF      	
        	while (marcReader.hasNext())
        	{
        		
        	}
// do this for ALL bib records until no match, or until matching mhld or EOF      	
            bibRec = marcReader.next();
            if (bibRec != null)
            {
        		Record matchingBibRec = getMatchingBibRec(bibRec.getControlNumber());
        		if (matchingBibRec != null)
                    bibRec = addBibMergeFields(bibRec, matchingBibRec);
            }
// then do it for all matching mhld recs until next bib rec or EOF
        }
*/
        
        return(bibRec);  
    }
    
    
    
    /**
     * Look for a record in the MHLD file that matches the bibId.  Note that "matching"
     *  means the ids match, where id is from Record.getControlNumber()
     *  
     * @param bibRecID - the id to match
     * @return Set of RawRecord objects corresponding to MHLD records that match
     *  the bibId
     */
    private Record getMatchingMhldRec(String bibRecID)
    {
    	Record result = null;
/*    	
    	int compareResult = ID_COMPARATOR.compare(currentMhldRec.getControlNumber(), bibRecID);

// String currMhldId = currentMhldRec.getControlNumber();   // useful for Debugging
    	
    	if (compareResult > 0)
    		// MHLD id is after bib id:  we're done and we do not advance in MHLD file
    		return result;
    	else
    	{
        	if (compareResult == 0)
            	// current MHLD matches the bibRec - we're done
        		return currentMhldRec;

    		// proceed to next MHLD record if it's not the last MHLD in the file
        	// NOTE:  THIS is where the assumption that the bib file is in ascending ID order is made
    		if (mhldRecCombiningRdr.hasNext())
    		{
	    		currentMhldRec = getNextMhld();
	    		return getMatchingMhldRec(bibRecID);
    		}
    	}
*/
    	return result;

    }

    
// FIXME: only remove bib fields Once    
    /**
     * given a MARC bib record as a Record object, and a MARC MHLD record as
     *  a Record object, merge the MHLD fields (indicated in class var
     *  mhldFldsToMerge) into the bib record, first removing any of those fields
     *  already existing in the bib record.
     * @param bibRecord
     * @param mhldRecord
     * @return the bib record with the MHLD fields merged in prior to the 999
     */
    private Record addMhldFieldsToBibRec(Record bibRecord, Record mhldRecord)
    {
        List<VariableField> lvf = bibRecord.getVariableFields(mhldFldsToMerge.split("[|]"));
        for (VariableField vf : lvf)
        {
            bibRecord.removeVariableField(vf);
        }
        bibRecord = MarcUtils.combineRecords(bibRecord, mhldRecord, mhldFldsToMerge, "999");
        return(bibRecord);
    }
    

    
	
}
