package edu.stanford;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

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
    private Map<String, Record> MERGED_BIB_MHLD_RECORDS = new HashMap<String, Record>();
    private Map<String, Record> MERGED_MULT_BIBS_RECORDS = new HashMap<String, Record>();
    private Map<String, Record> MERGED_MULT_BOTH_RECORDS = new HashMap<String, Record>();
    
    {
		try {
			String filePath = testDataParentPath + File.separator + "combineBibMhld_b1b2b3.mrc";
			RawRecordReader rawRecRdr = new RawRecordReader(new FileInputStream(new File(filePath)));
	        while (rawRecRdr.hasNext())
	        {
	        	RawRecord rawRec = rawRecRdr.next();
	        	Record rec = rawRec.getAsRecord(true, false, "999", "MARC8");
	        	String id = MarcUtils.getControlFieldData(rec, "001");
	        	UNMERGED_FIRST_BIBS.put(id, rec);
	        }

			filePath = testDataParentPath + File.separator + "combineBibMhld_mergedBoth123.mrc";
			rawRecRdr = new RawRecordReader(new FileInputStream(new File(filePath)));
	        while (rawRecRdr.hasNext())
	        {
	        	RawRecord rawRec = rawRecRdr.next();
	        	Record rec = rawRec.getAsRecord(true, false, "999", "MARC8");
	        	String id = MarcUtils.getControlFieldData(rec, "001");
	        	MERGED_BIB_MHLD_RECORDS.put(id, rec);
	        }

			filePath = testDataParentPath + File.separator + "combineBibMhld_mergedBibs123.mrc";
			rawRecRdr = new RawRecordReader(new FileInputStream(new File(filePath)));
	        while (rawRecRdr.hasNext())
	        {
	        	RawRecord rawRec = rawRecRdr.next();
	        	Record rec = rawRec.getAsRecord(true, false, "999", "MARC8");
	        	String id = MarcUtils.getControlFieldData(rec, "001");
	        	MERGED_MULT_BIBS_RECORDS.put(id, rec);
	        }

			filePath = testDataParentPath + File.separator + "combineBibMhld_mergedMultBoth123.mrc";
			rawRecRdr = new RawRecordReader(new FileInputStream(new File(filePath)));
	        while (rawRecRdr.hasNext())
	        {
	        	RawRecord rawRec = rawRecRdr.next();
	        	Record rec = rawRec.getAsRecord(true, false, "999", "MARC8");
	        	String id = MarcUtils.getControlFieldData(rec, "001");
	        	MERGED_MULT_BOTH_RECORDS.put(id, rec);
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
	    Map<String, Record> mergedRecs = readIntoRecordMap("combineBibMhld_b1m1b2b3.mrc");
	    Set<String> mergedRecIds = mergedRecs.keySet();
	    assertEquals(3, mergedRecIds.size());

	    // result 1 should have the mhld fields
	    String id = "a1";
       	RecordTestingUtils.assertEqualsIgnoreLeader(MERGED_BIB_MHLD_RECORDS.get(id), mergedRecs.get(id));

       	// results 2 and 3 should be unchanged
       	id = "a2";
       	RecordTestingUtils.assertEquals(UNMERGED_FIRST_BIBS.get(id), mergedRecs.get(id));
	    id = "a3";
       	RecordTestingUtils.assertEquals(UNMERGED_FIRST_BIBS.get(id), mergedRecs.get(id));
    }


    /**
     * a middle record in the file has a bib and an mhld
     */
@Test
    public void middleBibHasMhld() 
    		throws IOException 
    {
	    Map<String, Record> mergedRecs = readIntoRecordMap("combineBibMhld_b1b2m2b3.mrc");
        Set<String> mergedRecIds = mergedRecs.keySet();
        assertEquals(3, mergedRecIds.size());
    
        // result 2 should have the mhld fields
        String id = "a2";
       	RecordTestingUtils.assertEqualsIgnoreLeader(MERGED_BIB_MHLD_RECORDS.get(id), mergedRecs.get(id));
    
       	// results 1 and 3 should be unchanged
       	id = "a1";
       	RecordTestingUtils.assertEquals(UNMERGED_FIRST_BIBS.get(id), mergedRecs.get(id));
        id = "a3";
       	RecordTestingUtils.assertEquals(UNMERGED_FIRST_BIBS.get(id), mergedRecs.get(id));
    }
    
    /**
     * the last record in the file has a bib and an mhld
     */
@Test
    public void lastBibHasMhld() 
    		throws IOException 
    {
	    Map<String, Record> mergedRecs = readIntoRecordMap("combineBibMhld_b1b2b3m3.mrc");
        Set<String> mergedRecIds = mergedRecs.keySet();
        assertEquals(3, mergedRecIds.size());
    
        // result 3 should have the mhld fields
        String id = "a3";
       	RecordTestingUtils.assertEqualsIgnoreLeader(MERGED_BIB_MHLD_RECORDS.get(id), mergedRecs.get(id));
    
       	// results 2 and 3 should be unchanged
       	id = "a1";
       	RecordTestingUtils.assertEquals(UNMERGED_FIRST_BIBS.get(id), mergedRecs.get(id));
        id = "a2";
       	RecordTestingUtils.assertEquals(UNMERGED_FIRST_BIBS.get(id), mergedRecs.get(id));
    }



    /**
     * the first record in the file has multiple bibs (and no mhld)
     */
@Test
    public void firstHasMultipleBibs() 
    		throws IOException 
    {
	    Map<String, Record> mergedRecs = readIntoRecordMap("combineBibMhld_b1b1b2b3.mrc");
        Set<String> mergedRecIds = mergedRecs.keySet();
        assertEquals(3, mergedRecIds.size());
    
        // result 1 should have the mhld fields
        String id = "a1";
       	RecordTestingUtils.assertEqualsIgnoreLeader(MERGED_MULT_BIBS_RECORDS.get(id), mergedRecs.get(id));
    
       	// results 2 and 3 should be unchanged
       	id = "a2";
       	RecordTestingUtils.assertEquals(UNMERGED_FIRST_BIBS.get(id), mergedRecs.get(id));
        id = "a3";
       	RecordTestingUtils.assertEquals(UNMERGED_FIRST_BIBS.get(id), mergedRecs.get(id));
    }
    
    
    /**
     * a middle record in the file has multiple bibs (and no mhld)
     */
@Test
    public void middleHasMultipleBibs() 
    		throws IOException 
    {
	    Map<String, Record> mergedRecs = readIntoRecordMap("combineBibMhld_b1b2b2b3.mrc");
        Set<String> mergedRecIds = mergedRecs.keySet();
        assertEquals(3, mergedRecIds.size());
    
        // result 2 should have the mhld fields
        String id = "a2";
       	RecordTestingUtils.assertEqualsIgnoreLeader(MERGED_MULT_BIBS_RECORDS.get(id), mergedRecs.get(id));
    
       	// results 1 and 3 should be unchanged
       	id = "a1";
       	RecordTestingUtils.assertEquals(UNMERGED_FIRST_BIBS.get(id), mergedRecs.get(id));
        id = "a3";
       	RecordTestingUtils.assertEquals(UNMERGED_FIRST_BIBS.get(id), mergedRecs.get(id));
    }
    
    /**
     * the last record in the file has multiple bibs (and no mhld)
     */
@Test
    public void lastHasMultipleBibs() 
    		throws IOException 
    {
	    Map<String, Record> mergedRecs = readIntoRecordMap("combineBibMhld_b1b2b3b3.mrc");
        Set<String> mergedRecIds = mergedRecs.keySet();
        assertEquals(3, mergedRecIds.size());
    
        // result 3 should have the mhld fields
        String id = "a3";
       	RecordTestingUtils.assertEqualsIgnoreLeader(MERGED_MULT_BIBS_RECORDS.get(id), mergedRecs.get(id));
    
       	// results 2 and 3 should be unchanged
       	id = "a1";
       	RecordTestingUtils.assertEquals(UNMERGED_FIRST_BIBS.get(id), mergedRecs.get(id));
        id = "a2";
       	RecordTestingUtils.assertEquals(UNMERGED_FIRST_BIBS.get(id), mergedRecs.get(id));
    }
    

    /**
     * the first record in the file has multiple bibs and multiple mhlds
     */
@Test
    public void firstHasMultipleBibsAndMhlds() 
    		throws IOException 
    {
        Map<String, Record> mergedRecs = readIntoRecordMap("combineBibMhld_b1b1m1m1b2b3.mrc");
        Set<String> mergedRecIds = mergedRecs.keySet();
        assertEquals(3, mergedRecIds.size());
    
        // result 1 should have the mhld fields
        String id = "a1";
       	RecordTestingUtils.assertEqualsIgnoreLeader(MERGED_MULT_BOTH_RECORDS.get(id), mergedRecs.get(id));
    
       	// results 2 and 3 should be unchanged
       	id = "a2";
       	RecordTestingUtils.assertEquals(UNMERGED_FIRST_BIBS.get(id), mergedRecs.get(id));
        id = "a3";
       	RecordTestingUtils.assertEquals(UNMERGED_FIRST_BIBS.get(id), mergedRecs.get(id));
    }
    
    
    /**
     * a middle record in the file has multiple bibs and multiple mhlds
     */
@Test
    public void middleHasMultipleBibsAndMhlds() 
    		throws IOException 
    {
        Map<String, Record> mergedRecs = readIntoRecordMap("combineBibMhld_b1b2b2m2m2b3.mrc");
        Set<String> mergedRecIds = mergedRecs.keySet();
        assertEquals(3, mergedRecIds.size());
    
        // result 2 should have the mhld fields
        String id = "a2";
       	RecordTestingUtils.assertEqualsIgnoreLeader(MERGED_MULT_BOTH_RECORDS.get(id), mergedRecs.get(id));
    
       	// results 1 and 3 should be unchanged
       	id = "a1";
       	RecordTestingUtils.assertEquals(UNMERGED_FIRST_BIBS.get(id), mergedRecs.get(id));
        id = "a3";
       	RecordTestingUtils.assertEquals(UNMERGED_FIRST_BIBS.get(id), mergedRecs.get(id));
    }
    
    /**
     * the last record in the file has multiple bibs and multiple mhlds
     */
    @Test
    public void lastHasMultipleBibsAndMhlds() 
    		throws IOException 
    {
        Map<String, Record> mergedRecs = readIntoRecordMap("combineBibMhld_b1b2b3b3m3m3.mrc");
        Set<String> mergedRecIds = mergedRecs.keySet();
        assertEquals(3, mergedRecIds.size());
    
        // result 3 should have the mhld fields
        String id = "a3";
       	RecordTestingUtils.assertEqualsIgnoreLeader(MERGED_MULT_BOTH_RECORDS.get(id), mergedRecs.get(id));
    
       	// results 2 and 3 should be unchanged
       	id = "a1";
       	RecordTestingUtils.assertEquals(UNMERGED_FIRST_BIBS.get(id), mergedRecs.get(id));
        id = "a2";
       	RecordTestingUtils.assertEquals(UNMERGED_FIRST_BIBS.get(id), mergedRecs.get(id));
    }
    
    





	// single bib
	// one bib and one mhld
	// mult bibs
	// mult bibs and mult mhlds 

    // one bib and mult mhlds
	// mult bibs and one mhld

    
// test for errors in raw record file ------------------------------------------
    
    /**
     * the last bib record in the file has an id before the previous record
     */
@Test
    public void lastBibOutOfOrder() 
    		throws IOException 
    {
    	// grab error message  (should check logs too?)
    	ByteArrayOutputStream sysBAOS = new ByteArrayOutputStream();
    	PrintStream sysMsgs = new PrintStream(sysBAOS);
    	System.setErr(sysMsgs);
    	System.setOut(sysMsgs);

	    Map<String, Record> mergedRecs = readIntoRecordMap("combineBibMhld_b1b3b2.mrc");

		// ensure correct error message was printed
		assertTrue("Output message not as expected: " + sysBAOS.toString(),  
				sysBAOS.toString().startsWith("Bib record a2 came after bib record a3: file isn't sorted.  Cannot read file further."));
		System.setOut(System.out);
		System.setErr(System.err);
    }


    /**
     * the first record in the file that can be out of order is the second 
     * record.  This tests when teh second bib record in the file has an id 
     * before the previous record
     */
@Test
    public void secondBibOutOfOrder() 
    		throws IOException 
    {
    	// grab error message  (should check logs too?)
    	ByteArrayOutputStream sysBAOS = new ByteArrayOutputStream();
    	PrintStream sysMsgs = new PrintStream(sysBAOS);
    	System.setErr(sysMsgs);
    	System.setOut(sysMsgs);
    
        Map<String, Record> mergedRecs = readIntoRecordMap("combineBibMhld_b2b1b3.mrc");
    
    	// ensure correct error message was printed
    	assertTrue("Output message not as expected: " + sysBAOS.toString(),  
    			sysBAOS.toString().startsWith("Bib record a1 came after bib record a2: file isn't sorted.  Cannot read file further."));
    	System.setOut(System.out);
    	System.setErr(System.err);
    }
    

    

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

    // FIELD-wise testing
	// test crashing bib fields removed
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
    
    
    private Map<String, Record> readIntoRecordMap(String filename) 
    		throws IOException 
    {
    	String filePath = testDataParentPath + File.separator + filename;
        return combineFileRecordsAsMap(filePath);
    }


}
