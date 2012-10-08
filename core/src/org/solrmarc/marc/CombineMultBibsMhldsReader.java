package org.solrmarc.marc;

import java.io.*;
import java.util.Comparator;


import org.apache.log4j.Logger;
import org.marc4j.*;
import org.marc4j.marc.*;
import org.solrmarc.tools.*;

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
 * e.g. :    bib1 bib2 mhld2 mhld2 bib3 bib4 bib4 mhld4 mhld4 bib5 ...
 *
 * If any of the desired MHLD fields clash with any existing bib fields, the
 * bib fields are removed before the MHLD fields are added to the record.
 *
 * Only the additional holdings information from the multiple records is added
 * - the other fields are not repeated in the result.
 *
 * Algorithm:
 * start new record when:
 * - a bib follows a bib or mhld with a different id
 * - a bib is the first record of a file
 *
 * combine when:
 * - a bib follows a bib with the same id
 * - an mhld follows a bib or mhld with the same id
 *
 * Error conditions (report and continue) when:
 * - an mhld follows a bib or mhld with a different id (skip record)
 * - an mhld is the first record of a file (skip record)
 * - a bib follows an mhld with the same id (fatal error -> upstream sirsi dump issues)
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

    /** insert any MHLD fields before the first occurrence of this bib field */
    public static final String DEFAULT_BIB_FLD_TO_INSERT_BEFORE = "999";

    /** used to determine if the match field values are equal */
    static Comparator<String> MATCH_FIELD_COMPARATOR = new StringNaturalCompare();

    public static Logger logger = Logger.getLogger(CombineMultBibsMhldsReader.class.getName());


    /** the field in the first bib rec of a possible set of records to use for matching purposes */
    private String firstBibFldToMatch = DEFAULT_FIELD_TO_MATCH;

    /** the field in a bib look-ahead record to use for matching purposes */
    private String lookAheadBibFldToMatch = DEFAULT_FIELD_TO_MATCH;

    /** the field in an MHLD look-ahead record to use for matching purposes */
    private String mhldFldToMatch = DEFAULT_FIELD_TO_MATCH;

    /** regular expression (as a string) of Bib fields to be merged into the bib record */
    private String bibFldsToMerge = DEFAULT_BIB_FLDS_TO_MERGE;

    /** regular expression (as a string) of MHLD fields to be merged into the bib record */
    private String mhldFldsToMerge = DEFAULT_MHLD_FLDS_TO_MERGE;

    /** mhld fields will be inserted before the first occurrence of this field */
    private String insertMhldB4bibFld = DEFAULT_BIB_FLD_TO_INSERT_BEFORE;

    /** what we will use to read the file and look ahead as we go for additional
     *  records that need to be merged into the current one. */
    private MarcReader marcReader = null;

    /** the most recent bib Record read with a new id */
    Record currentFirstBibRecord = null;

    /** the last record that has been read (so far) in the marc file. Set to null before first record is read and after last record is read. */
    Record lastRecordRead = null;


	/**
	 *
	 * @param marcRecsFilename - the name of the file to be read
     * @param firstBibCntlFldToMatch - the field in the current record to use for matching purposes (null for DEFAULT_CONTROL_FIELD).
     * @param lookAheadBibFldToMatch - the field in the look-ahead bib record to use for matching purposes (null for DEFAULT_CONTROL_FIELD).
     * @param lookAheadMhldFldToMatch - the field in the look-ahead mhld record to use for matching purposes (null for DEFAULT_CONTROL_FIELD).
	 * @param bibFldsToMerge - a regular expression (as a string) of Bib fields to be merged when there are multiple bib records (e.g. "998|999"; null for DEFAULT_BIB_FLDS_TO_MERGE)
	 * @param mhldFldsToMerge - a regular expression (as a string) of MHLD fields to be merged into the bib record (e.g. "852|863|866"; null for DEFAULT_MHLD_FLDS_TO_MERGE)
	 * @param insertMhldB4bibFld - mhld fields will be inserted before the first occurrence of this field (null for DEFAULT_BIB_FLD_TO_INSERT_BEFORE)
	 * @param permissive - if true, try to recover from errors, including records with errors, when possible
	 * @param defaultEncoding - possible values are MARC8, UTF-8, UNIMARC, BESTGUESS
	 * @param toUtf8 - if true, this will convert records in our import file from (MARC8) encoding into UTF-8 encoding on output to index
	 */
	public CombineMultBibsMhldsReader(String marcRecsFilename,
										String firstBibCntlFldToMatch,
										String lookAheadBibFldToMatch,
										String lookAheadMhldFldToMatch,
										String bibFldsToMerge,
										String mhldFldsToMerge,
										String insertMhldB4bibFld,
										boolean permissive,
										String defaultEncoding,
										boolean toUtf8
										)
				throws FileNotFoundException
	{
        marcReader = new MarcPermissiveStreamReader(new FileInputStream(new File(marcRecsFilename)), permissive, toUtf8, defaultEncoding);

        // all of the below are set to defaults at instantiation, so they
        //   only need to be changed here if non-null value is passed in.

		if (firstBibCntlFldToMatch != null)
			this.firstBibFldToMatch = firstBibCntlFldToMatch;
		if (lookAheadBibFldToMatch != null)
			this.lookAheadBibFldToMatch = lookAheadBibFldToMatch;
		if (lookAheadMhldFldToMatch != null)
			this.mhldFldToMatch = lookAheadMhldFldToMatch;

		if (bibFldsToMerge != null)
			this.bibFldsToMerge = bibFldsToMerge;
		if (mhldFldsToMerge != null)
			this.mhldFldsToMerge = mhldFldsToMerge;
		if (insertMhldB4bibFld != null)
			this.insertMhldB4bibFld = insertMhldB4bibFld;
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
		this (marcRecsFilename, null, null, null, null, null, null, permissive, defaultEncoding, toUtf8);
	}

	/**
	 * @param marcRecsFilename - the name of the file to be read
	 */
	public CombineMultBibsMhldsReader(String marcRecsFilename)
				throws FileNotFoundException
	{
		this (marcRecsFilename, null, null, null, null, null, null, true, "MARC8", false);
	}



	/**
	 *
	 * @param marcReader - the marcReader to be wrapped by this combining reader
	 * @param bibFldsToMerge - a regular expression (as a string) of Bib fields to be merged when there are multiple bib records (e.g. "998|999"; null for DEFAULT_BIB_FLDS_TO_MERGE)
	 */
	public CombineMultBibsMhldsReader(MarcReader marcReader,
										String bibFldsToMerge
										)
				throws FileNotFoundException
	{
        this.marcReader = marcReader;

        // all of the below are set to defaults at instantiation, so they
        //   only need to be changed here if non-null value is passed in.

		if (bibFldsToMerge != null)
			this.bibFldsToMerge = bibFldsToMerge;
	}




// ------- abstract class methods --------------------

	/**
	 * Returns true if the iteration has more records, false otherwise.
	 */
	public boolean hasNext()
	{
    	if (marcReader != null)
        	return (lastRecordRead != null || marcReader.hasNext());
        return(false);
    }


    /**
     * Returns the next record in the iteration.
     *
     * @return Record - the record object
     */
	public Record next()
    {
		String idToMatch = null;
		boolean currRecHasMhld = false;
		try
        {
    		if (hasNext())
    		{
    			// is it the beginning of the file?
    			if (lastRecordRead == null && currentFirstBibRecord == null)
    			{
    				// we are at the beginning of the file
    				lastRecordRead = marcReader.next();
    				if (MarcUtils.isMHLDRecord(lastRecordRead))
    	        	{
    	        		// try to get record identifier
    	                String recCntlNum = null;
    	                try {  recCntlNum = lastRecordRead.getControlNumber(); }
    	                catch (NullPointerException npe) { /* ignore */ }
	                	String errmsg = "First record in file is mhld (not bib): Skipping record";
    	                if (recCntlNum != null)
    	                	logger.error(errmsg + " " + recCntlNum);
    	                else
    	                	logger.error(errmsg + ".");
    	                this.next(); // move on to the next record
    	        	}
    			}

                currentFirstBibRecord = lastRecordRead;

                // look for following bib or mhld records that need to be merged in
    			idToMatch = MarcUtils.getControlFieldData(currentFirstBibRecord, firstBibFldToMatch);
    			currRecHasMhld = mergeFollowingRecs(idToMatch, currRecHasMhld);
    				//    marcReader is moved to the next record
    				//    lastRecordRead gets a new value
    				//    currentFirstBibRecord can get more fields merged in
    		}
        }
        catch (Exception e)
        {
        	if (e instanceof SolrMarcRuntimeException)
        			throw (RuntimeException) e;
        	else
        	{
        		// try to get record identifier
                String recCntlNum = null;
                try {  recCntlNum = currentFirstBibRecord.getControlNumber(); }
                catch (NullPointerException npe) { /* ignore */ }

                if (recCntlNum != null)
                	logger.error("Skipping record after " + recCntlNum + "; Couldn't read it:  " + e.toString(), e);
                else
                	logger.error("Skipping record; Couldn't read it: " + e.toString(), e);

                if (idToMatch != null)
                	currRecHasMhld = mergeFollowingRecs(idToMatch, currRecHasMhld);
        	}
        }

		return currentFirstBibRecord;
    }

// ------- private methods --------------------

    /**
     * Get next record in the file.  If the designated fields match
     *   (firstBibFldToMatch, lookAheadBibFldToMatch, mhldFldToMatch), then
     *   merge the matching record fields into the currentFirstBibRecord
     *   (bibFldsToMerge, mhldFldsToMerge) and return true.  Otherwise, return
     *   false.
     *
     *  Side Effects:
     *    marcReader is moved to the next record
     *    lastRecordRead is set to the next bib record that does NOT match the current first bib id,
     *      or to null, if there are no more records to be read.
     *    currentFirstBibRecord can get more fields
     *
     * @param idToMatch  the id to match the next bib or mhld
     * @param currBibHasMhld  true if currentFirstBibRecord already includes an mhld record
     * @return true if currentFirstBibRecord includes an mhld by the end of processing
     * @throws SolrMarcRuntimeException - if we're unable to continue
     *  where ID is determined from the firstBibFldToMatch, the lookAheadBibFldToMatch and the mhldFldToMatch
     */
    private boolean mergeFollowingRecs(String idToMatch, boolean currBibHasMhld)
    {
    	boolean stillLooking = true;
    	boolean mergedSome = false;
    	boolean bibHasMhld = currBibHasMhld;
    	lastRecordRead = null;
		while (stillLooking)
		{
			if (!marcReader.hasNext())
			{
				// if we already read the last record in the file and we're still
				//  looking, then we're done
				lastRecordRead = null;
				break;
			}

			try
			{
				lastRecordRead = marcReader.next();
			}
			catch (Exception e)
			{
				// we're unable to continue
	        	if (e instanceof SolrMarcRuntimeException)
        			throw (RuntimeException) e;

	        	// we can continue by just skipping record
	    		// try to get record identifier
	            String recCntlNum = null;
	            try {  recCntlNum = currentFirstBibRecord.getControlNumber(); }
	            catch (NullPointerException npe) { /* ignore */ }

	            if (recCntlNum != null)
	            	logger.error("Skipping record after " + recCntlNum + "; Couldn't read it:  " + e.toString(), e);
	            else
	            	logger.error("Skipping record; Couldn't read it: " + e.toString(), e);
	            return mergeFollowingRecs(idToMatch, bibHasMhld);
			}

// FIXME: what if lastRecordRead is null?

    		if (MarcUtils.isMHLDRecord(lastRecordRead))
    		{
    			String mhldMatchId = MarcUtils.getControlFieldData(lastRecordRead, mhldFldToMatch);
            	int compareResult = MATCH_FIELD_COMPARATOR.compare(mhldMatchId, idToMatch);
            	// if it's an mhld, it should match, otherwise it's an error
            	if (compareResult == 0)
            	{
                	// we have a match - merge the mhld into the bib
               		if (!bibHasMhld)
            		{
            			currentFirstBibRecord = MarcUtils.removeFields(currentFirstBibRecord, mhldFldsToMerge);
            			bibHasMhld = true;
            		}

// FIXME:  is there any reason the 999s need to be last?
            		currentFirstBibRecord = MarcUtils.combineRecords(currentFirstBibRecord, lastRecordRead, mhldFldsToMerge, insertMhldB4bibFld);
            		mergedSome = true;
            	}
            	else
               	{
   				    String errmsg = "CombineMultBibsMhldsReader2: mhld id mismatch: mhld record " + mhldMatchId + " came after bib or mhld record " + idToMatch + ": skipping mhld record " + mhldMatchId + ".";
   				    logger.error(errmsg);
               	}
    		}
    		else // bib record
    		{
    			String lookAheadBibRecId = MarcUtils.getControlFieldData(lastRecordRead, lookAheadBibFldToMatch);
            	int compareResult = MATCH_FIELD_COMPARATOR.compare(lookAheadBibRecId, idToMatch);
            	if (compareResult > 0)
            	{
            		// the new bib id sorts after the current bib id; we're done
            		stillLooking = false;
            	}
            	else
            	{
                	if (compareResult == 0)
                	{
                    	// we have a match
                		if (bibHasMhld)
                		{
                			// it's a fatal error if a bib matches the previous mhld
                            String errmsg = "CombineMultBibsMhldsReader: bib record " + lookAheadBibRecId + " came after matching mhld record: assuming error with upstream marc records.  STOPPING PROCESSING.";
                            logger.fatal(errmsg);
                            throw new SolrMarcRuntimeException(errmsg);
                		}

                		// bib follows matching bib: merge the second bib into the first
                		currentFirstBibRecord = MarcUtils.combineRecords(currentFirstBibRecord, lastRecordRead, bibFldsToMerge);
                		mergedSome = true;
                	}
                	else // the new bib id sorts before the current one - the records in the file aren't ordered
                	{
    				    String errmsg = "CombineMultBibsMhldsReader2: bib record " + lookAheadBibRecId + " came after bib record " + idToMatch + ": file isn't sorted.";
    				    logger.info(errmsg);
                		stillLooking = false;
                	}
            	}
    		}
		}

    	return bibHasMhld;
    }

}
