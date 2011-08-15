package edu.stanford;

import static org.junit.Assert.assertEquals;

import java.io.*;
import java.util.*;

import org.junit.Test;
import org.marc4j.marc.Record;
import org.solrmarc.marc.RawRecordReader;
import org.solrmarc.testUtils.RecordTestingUtils;
import org.solrmarc.tools.MarcUtils;
import org.solrmarc.tools.RawRecord;

import edu.stanford.marcUtils.CombineMultBibsMhldsReader;

/**
 * tests for edu.stanford.marcUtils.CombineMultBibsMultMhldsReader
 * @author Naomi Dushay
 */
public class CombineMultBibsMhldsReaderTest extends AbstractStanfordTest
{
	
    private Map<String, Record> UNMERGED_FIRST_BIBS = new HashMap<String, Record>();
    private Map<String, Record> MERGED_RECORDS = new HashMap<String, Record>();
    {
		String filePath = testDataParentPath + File.separator + "combineBibMhld_b1b2b3.mrc";
		try {
			RawRecordReader rawRecRdr = new RawRecordReader(new FileInputStream(new File(filePath)));
	        while (rawRecRdr.hasNext())
	        {
	        	RawRecord rawRec = rawRecRdr.next();
	        	Record rec = rawRec.getAsRecord(true, false, "999", "MARC8");
	        	String id = MarcUtils.getControlFieldData(rec, "001");
	        	UNMERGED_FIRST_BIBS.put(id, rec);
	        }
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		filePath = testDataParentPath + File.separator + "combineBibMhld_merged123.mrc";
		try {
			RawRecordReader rawRecRdr = new RawRecordReader(new FileInputStream(new File(filePath)));
	        while (rawRecRdr.hasNext())
	        {
	        	RawRecord rawRec = rawRecRdr.next();
	        	Record rec = rawRec.getAsRecord(true, false, "999", "MARC8");
	        	String id = MarcUtils.getControlFieldData(rec, "001");
	        	MERGED_RECORDS.put(id, rec);
	        }
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
    }

	
	
    /**
     * the first record in the file has a bib and an mhld
     */
@Test
    public void firstBibHasMhld() 
    		throws IOException 
    {
		String filePath = testDataParentPath + File.separator + "combineBibMhld_b1m1b2b3.mrc";
	    Map<String, Record> mergedRecs = combineFileRecordsAsMap(filePath);
	    Set<String> mergedRecIds = mergedRecs.keySet();
	    assertEquals(3, mergedRecIds.size());

	    // result 1 should have the mhld fields
	    String id = "a1";
//       	RecordTestingUtils.assertEqualsIgnoreLeader(MERGED_RECORDS.get(id), mergedRecs.get(id));
       	RecordTestingUtils.assertEqualsIgnoreLeader(MERGED_RECORDS.get(id), mergedRecs.get(id));

       	// results 2 and 3 should be unchanged
       	id = "a2";
       	RecordTestingUtils.assertEquals(UNMERGED_FIRST_BIBS.get(id), mergedRecs.get(id));
       	// result bib 6 should not be changed
	    id = "a3";
       	RecordTestingUtils.assertEquals(UNMERGED_FIRST_BIBS.get(id), mergedRecs.get(id));
    }



	// single bib
	// mult bibs
	// one bib and one mhld
	// one bib and mult mhlds
	// mult bibs and mult mhlds 

	// test crashing bib fields removed


	// first record
	// last record
	// middle record

	// errors:
	//  records out of order (bib)
	//  mhld that doesn't match
	//  unreadable record 
	//    first bib
	//      followed by bibs, mhlds, and both
	//    subsequent bib
	//	  first mhld
	//    subsequent mhld
	//   first, last, middle record group in file


	// fields to merge:  bib, mhld
	//   present, missing
	// fields not to merge
	//   present
	

// supporting methods for testing ---------------------------------------------
	
    /**
     * run a file through CombineMultBibsMhldsReader and get the results as a map for easy testing
     * @param marcRecsFilename - the name of the file containing MARC records, in the form of:
     *    bib1 bib2 mhld2 mhld2 bib3 bib4 bib4 mhld4 mhld4 bib5 ... 
     * @return Map of ids -> Record objects for a single bib record comprising desired fields of multiple bibs and of following mhlds
     */
    public static Map<String, Record> combineFileRecordsAsMap(String marcRecsFilename)
    	throws IOException
    {
    	Map<String, Record> results = new HashMap<String, Record>();

        CombineMultBibsMhldsReader merger = new CombineMultBibsMhldsReader(marcRecsFilename);
        
        String idField = "001";
        String bibFldsToMerge = "999";
        String mhldFldsToMerge = null;  // use default
        String insertMhldB4bibFld = "999";
    	boolean permissive = true;
        boolean toUtf8 = true;
        String defaultEncoding = "MARC8";
        
        merger = new CombineMultBibsMhldsReader(marcRecsFilename, 
        		idField, idField, idField, 
        		bibFldsToMerge, mhldFldsToMerge, insertMhldB4bibFld, 
        		permissive, defaultEncoding, toUtf8);

        while (merger.hasNext()) 
        {
        	Record bibRecWithPossChanges = merger.next();
        	results.put(MarcUtils.getControlFieldData(bibRecWithPossChanges, "001"), bibRecWithPossChanges);
        }
        return results;
    }

}
