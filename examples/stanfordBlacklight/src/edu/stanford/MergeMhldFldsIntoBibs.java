package edu.stanford;

import java.io.*;
import java.util.*;

import org.marc4j.*;
import org.marc4j.marc.*;
import org.solrmarc.marc.*;
import org.solrmarc.marcoverride.MarcSplitStreamWriter;
import org.solrmarc.tools.*;

/**
 * Given a file of MARC bib records and another file of MARC (MHLD) records,
 *  read through the bib file and look for matching MHLD records.  If found,
 *  merge the desired fields from the MHLD record into the bib record, first
 *  removing any existing fields in the bib rec matching a desired field tag.
 *  
 * Note that the Bib and MHLD file must have records in StringNaturalCompare 
 *  ascending order.
 *  
 * @author Naomi Dushay, based on org.solrmarc.tools.MergeSummaryHoldings by Bob Haschart
 *
 */
public class MergeMhldFldsIntoBibs  implements MarcReader
{
    /** default list of MHLD fields to be merged into the bib record, separated by '|' char */
    public static String DEFAULT_MHLD_FLDS_TO_MERGE = "852|853|863|866|867|868";
    
    public static Comparator ID_COMPARATOR = new StringNaturalCompare();

    static boolean verbose = false;
    static boolean veryverbose = false;

    /** list of MHLD fields to be merged into the bib record, separated by '|' char */
    private String mhldFldsToMerge = null;


    /** for the file of MARC bib records - it will combine bib recs with the 
     * same id, using the first bib in a set but adding 999s from any 
     * immediately following records with the same id */
    private MarcCombiningReader bibRecsCombiningRdr = null;
    
    /** the name of the file containing MHLD records.  It must be a class variable
     * because the file may need to be read multiple times to match bib records */
    private String mhldRecsFileName;
    
    /** for the file of MARC MHLD records - it will combine mhld recs with the
     * same id, using the first mhld in a set and adding mhldFldsToMerge from
     * any immediately following records with the same id */
    private MarcCombiningReader mhldRecCombiningRdr = null;

    /**
     * the last mhld record read, but not yet compared with a bib record
     */
    private Record currentMhldRec = null;
    
    
    public MergeMhldFldsIntoBibs(String bibRecsFileName, boolean permissive, boolean toUtf8, String defaultEncoding, 
            String mhldRecsFileName, String mhldFldsToMerge)
	{
        String idField = "001";
        String bibFldsToMerge = "999";
		try
		{
	        MarcReader mrcRdr = new MarcPermissiveStreamReader(new FileInputStream(new File(bibRecsFileName)), permissive, toUtf8, defaultEncoding);
	        this.bibRecsCombiningRdr = new MarcCombiningReader(mrcRdr, bibFldsToMerge, idField, idField);
		}
		catch (IOException e)
		{
			e.printStackTrace();
			System.exit(1);
		}

        this.mhldRecsFileName = mhldRecsFileName;
		this.mhldFldsToMerge = mhldFldsToMerge;
		System.setProperty("org.marc4j.marc.MarcFactory", "org.solrmarc.marcoverride.NoSortMarcFactoryImpl");
		readMhldFileFromBeginning(mhldRecsFileName);
	}

    public MergeMhldFldsIntoBibs(String bibRecsFileName, String mhldRecsFileName, String mhldFldsToMerge)
    {
        this (bibRecsFileName, true, false, "MARC8", mhldRecsFileName, mhldFldsToMerge);
    }
        
    
    /**
     * create a new RawRecordReader for the MHLD records file, and reset
     *  prevMhldRecID and unmatchedPrevMhldRec to null
     * @param mhldRecsFileName
     */
    private void readMhldFileFromBeginning(String mhldRecsFileName)
    {
    	try
        {
    		// mhld's must be read in a way that they combine based on 001 fields
            MarcReader mrcRdr = new MarcPermissiveStreamReader(new FileInputStream(new File(mhldRecsFileName)), true, false, "MARC8");
        	mhldRecCombiningRdr = new MarcCombiningReader(mrcRdr, mhldFldsToMerge, "001", "001");
        }
        catch (FileNotFoundException e)
        {
			System.err.println("No file found at " + mhldRecsFileName);
        	mhldRecCombiningRdr = null;           
        }
    	currentMhldRec = getNextMhld();
    }
    
    /**
     * @return true if there is another record in the bib records file
     */
    public boolean hasNext()
    {
    	if (bibRecsCombiningRdr != null)
        	return (bibRecsCombiningRdr.hasNext());
        return(false);
    }
    
    /**
     * Get the next bib record from the file of MARC bib records, then look 
     *  for a matching MARC MHLD record in the MHLD recs file, and if found, 
     *  merge the MHLD fields specified in mhldFldsToMerge into the bib 
     *  record and then return the bib record.
     * @return Record object containing fields merged from matching mhld 
     *  records, if there are any
     */
    public Record next()
    {
        Record bibRec = null;
        if (bibRecsCombiningRdr != null && bibRecsCombiningRdr.hasNext()) 
        {
            bibRec = bibRecsCombiningRdr.next();
            if (bibRec != null)
            {
        		Record matchingMhldRec = getMatchingMhldRec(bibRec.getControlNumber());
        		if (matchingMhldRec != null)
                    bibRec = addMhldFieldsToBibRec(bibRec, matchingMhldRec);
            }
        }
        
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

    	return result;
    }

    
    /**
     * NOTE: only call this method if:
     *  1) you are sure there is a next record in the file
     *    OR
     *  2) you want to start over from the beginning of the MHLD file if there
     *    are no more records to read from the file 
     * @return the next record in the MHLD file, if there is one.  Otherwise
     *  start reading the mhld file from the beginning, and return the first record.
     */
    private Record getNextMhld()
    {
    	if (mhldRecCombiningRdr != null)
    	{
        	if (mhldRecCombiningRdr.hasNext())
        		// there is another record
                currentMhldRec = mhldRecCombiningRdr.next(); 
        	else
        		readMhldFileFromBeginning(mhldRecsFileName); // sets currentMhldRec

        	return currentMhldRec;
    	}

    	return null;
    }

    
    /**
     * NOTE: not used by main() - only used by next()
     * 
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
        List<VariableField> lvf = (List<VariableField>) bibRecord.getVariableFields(mhldFldsToMerge.split("[|]"));
        for (VariableField vf : lvf)
        {
            bibRecord.removeVariableField(vf);
        }
        bibRecord = MarcCombiningReader.combineRecords(bibRecord, mhldRecord, mhldFldsToMerge, "999");
        return(bibRecord);
    }
    
    /**
     * basically for testing 
     * for each bib record in the bib rec file 
     *  look for a corresponding mhld record.  If a match is found, 
     *    1) remove any existing fields in the bib record that duplicate the mhld fields to be merged into the bib record
     *    2) merge the mhld fields into the bib record
     * then add the bib record (whether it had a match or not) to the List of records
     * @param bibRecsFileName - the name of the file containing MARC Bibliographic records
     * @param mhldRecsFileName - the name of the file containing MARC MHLD records
     * @return Map of ids -> Record objects for the bib records, which will include mhld fields if a match was found
     */
    public static Map<String, Record> mergeMhldsIntoBibRecordsAsMap(String bibRecsFileName, String mhldRecsFileName)
    	throws IOException
    {
    	Map<String, Record> results = new HashMap<String, Record>();

    	boolean permissive = true;
        boolean toUtf8 = false;
        String defaultEncoding = "MARC8";
        MergeMhldFldsIntoBibs merger = new MergeMhldFldsIntoBibs(bibRecsFileName, permissive, toUtf8, defaultEncoding, 
                                                               mhldRecsFileName, DEFAULT_MHLD_FLDS_TO_MERGE);

        verbose = true;
        veryverbose = true;
        while (merger.hasNext()) 
        {
        	Record bibRecWithPossChanges = merger.next();
        	results.put(GenericUtils.getRecordIdFrom001(bibRecWithPossChanges), bibRecWithPossChanges);
        }
        return results;
    }

    
	/**
     * for each bib record in the bib rec file 
     *  look for a corresponding mhld record.  If a match is found, 
     *    1) remove any existing fields in the bib record that duplicate the mhld fields to be merged into the bib record
     *    2) merge the mhld fields into the bib record
     * then write the bib record (whether it had a match or not) to stdout
     * @param bibRecsFileName - the name of the file containing MARC Bibliographic records
     * @param mhldRecsFileName - the name of the file containing MARC MHLD records
     * @return void, but the bib records will be written to standard out
     */
    public static void mergeMhldRecsIntoBibRecsAsStdOut(String bibRecsFileName, String mhldRecsFileName)
    	throws IOException
    {
        boolean permissive = true;
        boolean toUtf8 = false;
        String defaultEncoding = "MARC8";
        MergeMhldFldsIntoBibs merger = new MergeMhldFldsIntoBibs(bibRecsFileName, permissive, toUtf8, defaultEncoding, 
                                                               mhldRecsFileName, DEFAULT_MHLD_FLDS_TO_MERGE);
        verbose = true;
        veryverbose = true;
        MarcWriter writer = new MarcSplitStreamWriter(System.out, "ISO-8859-1", 70000, "999");
        while (merger.hasNext()) 
        {
        	Record bibRecWithPossChanges = merger.next();
            writer.write(bibRecWithPossChanges);
            System.out.flush();
        }
    }

    
    
    /**
     * Given a file of MARC MHLD records and a file of MARC Bibliographic records,
     *  merge selected fields from the MHLD records into matching MARC Bib records.  
     *  Ignores MHLD records with no matching bib record.
     *  Selected fields are defined in class constant mhldFldsToMerge.
     * Note that the MHLD file must have records in StringNaturalCompare ascending order.
     * @param args - command line arguments
     */
    public static void main(String[] args)
    {
        String bibRecsFileName = null;
    	String mhldRecsFileName = null;
        
        int argoffset = 0;
        if (args.length == 0)
        {
            System.err.println("Usage: edu.stanford.MergeMhldFldsIntoBibs [-v] [-vv] -s marcMhldFile.mrc  marcBibsFile.mrc");
        }
        while (argoffset < args.length && args[argoffset].startsWith("-"))
        {
            if (args[argoffset].equals("-v"))
            {
                verbose = true;
                argoffset++;
            }
            if (args[argoffset].equals("-vv"))
            {
                verbose = true;
                veryverbose = true;
                argoffset++;
            }
            if (args[argoffset].equals("-s"))
            {
                mhldRecsFileName = args[1+argoffset];
                argoffset += 2;
            }
        }

        // last argument should be the name of a file containing marc bib records
        if (args.length > argoffset && (args[argoffset].endsWith(".mrc") || args[argoffset].endsWith(".marc") || args[argoffset].endsWith(".xml")))
        	bibRecsFileName = args[argoffset];
        
        System.setProperty("org.marc4j.marc.MarcFactory", "org.solrmarc.marcoverride.NoSortMarcFactoryImpl");

        try 
        {
			mergeMhldRecsIntoBibRecsAsStdOut(bibRecsFileName, mhldRecsFileName);
		} 
        catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
        System.exit(0);
    }

}
