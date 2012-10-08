package org.solrmarc.marc;

import static org.junit.Assert.*;

import java.io.*;
import java.util.*;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.Test;
import org.marc4j.marc.*;
import org.solrmarc.AbstractCoreTest;
import org.solrmarc.marc.CombineMultBibsMhldsReader;
import org.solrmarc.marc.RawRecordReader;
import org.solrmarc.testUtils.LoggerAppender4Testing;
import org.solrmarc.testUtils.RecordTestingUtils;
import org.solrmarc.tools.*;


/**
 * tests for org.solrmarc.marc.CombineMultBibsMultMhldsReader
 * @author Naomi Dushay
 */
public class CombineMultBibsMhldsReaderTest extends AbstractCoreTest
{
    private Map<String, Record> UNMERGED_FIRST_BIBS = new HashMap<String, Record>();
    private Map<String, Record> MERGED_BIB_MHLD_RECORDS = new HashMap<String, Record>();
    private Map<String, Record> MERGED_MULT_BIBS_RECORDS = new HashMap<String, Record>();
    private Map<String, Record> MERGED_MULT_BOTH_RECORDS = new HashMap<String, Record>();

    {
		try
		{
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
	    assertEquals("Wrong number of read records: ", 3, mergedRecIds.size());

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
        assertEquals("Wrong number of read records: ", 3, mergedRecIds.size());

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
        assertEquals("Wrong number of read records: ", 3, mergedRecIds.size());

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
        assertEquals("Wrong number of read records: ", 3, mergedRecIds.size());

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
        assertEquals("Wrong number of read records: ", 3, mergedRecIds.size());

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
        assertEquals("Wrong number of read records: ", 3, mergedRecIds.size());

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
        assertEquals("Wrong number of read records: ", 3, mergedRecIds.size());

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
        assertEquals("Wrong number of read records: ", 3, mergedRecIds.size());

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
        assertEquals("Wrong number of read records: ", 3, mergedRecIds.size());

        // result 3 should have the mhld fields
        String id = "a3";
       	RecordTestingUtils.assertEqualsIgnoreLeader(MERGED_MULT_BOTH_RECORDS.get(id), mergedRecs.get(id));

       	// results 2 and 3 should be unchanged
       	id = "a1";
       	RecordTestingUtils.assertEquals(UNMERGED_FIRST_BIBS.get(id), mergedRecs.get(id));
        id = "a2";
       	RecordTestingUtils.assertEquals(UNMERGED_FIRST_BIBS.get(id), mergedRecs.get(id));
    }


// test for errors in raw record file ------------------------------------------

    /**
     * the last bib record in the file has an id before the previous record
     */
@Test
    public void lastBibOutOfOrderError()
    		throws IOException
    {
	    LoggerAppender4Testing appender = new LoggerAppender4Testing();
		CombineMultBibsMhldsReader.logger.addAppender(appender);
		CombineMultBibsMhldsReader.logger.setLevel(Level.DEBUG);
	    try
	    {
	        Logger.getLogger(CombineMultBibsMhldsReaderTest.class).info("Test lastBibOutOfOrderError");
	    	Map<String, Record> mergedRecs = readIntoRecordMap("combineBibMhld_b1b3b2.mrc");
	        assertEquals("Wrong number of read records: ", 3,  mergedRecs.keySet().size());
	        appender.assertLogContains(Level.INFO, "bib record a2 came after bib record a3: file isn't sorted.");
	    }
		catch (SolrMarcRuntimeException e)
		{
			fail("processing should continue if bib recs are out of order");
		}
	    finally
	    {
	    	CombineMultBibsMhldsReader.logger.removeAppender(appender);
	    }
    }

    /**
     * as test lastBibOutOfOrderError() except second to last bib record is followed by a matching mhld
     */
@Test
    public void lastBibOutOfOrderErrorAfterMhld()
    		throws IOException
    {
	    LoggerAppender4Testing appender = new LoggerAppender4Testing();
		CombineMultBibsMhldsReader.logger.addAppender(appender);
		CombineMultBibsMhldsReader.logger.setLevel(Level.DEBUG);
	    try
	    {
	        Logger.getLogger(CombineMultBibsMhldsReaderTest.class).info("Test lastBibOutOfOrderErrorAfterMhld");
	    	Map<String, Record> mergedRecs = readIntoRecordMap("combineBibMhld_b1b3m3b2.mrc");
	        assertEquals("Wrong number of read records: ", 3,  mergedRecs.keySet().size());
	        appender.assertLogContains(Level.INFO, "bib record a2 came after bib record a3: file isn't sorted.");
	    }
		catch (SolrMarcRuntimeException e)
		{
			fail("processing should continue if bib recs are out of order");
		}
	    finally
	    {
	    	CombineMultBibsMhldsReader.logger.removeAppender(appender);
	    }
    }

    /**
     * the first record in the file that can be out of order is the second
     * record.  This tests when the second bib record in the file has an id
     * before the previous record
     */
@Test
    public void secondBibOutOfOrderError()
    		throws IOException
    {
	    LoggerAppender4Testing appender = new LoggerAppender4Testing();
		CombineMultBibsMhldsReader.logger.addAppender(appender);
		CombineMultBibsMhldsReader.logger.setLevel(Level.DEBUG);
	    try
	    {
	        Logger.getLogger(CombineMultBibsMhldsReaderTest.class).info("Test secondBibOutOfOrderError");
	    	Map<String, Record> mergedRecs = readIntoRecordMap("combineBibMhld_b2b1b3.mrc");
	        assertEquals("Wrong number of read records: ", 3,  mergedRecs.keySet().size());
	        appender.assertLogContains(Level.INFO, "bib record a1 came after bib record a2: file isn't sorted.");
	    }
		catch (SolrMarcRuntimeException e)
		{
			fail("processing should continue if bib recs are out of order");
		}
	    finally
	    {
	    	CombineMultBibsMhldsReader.logger.removeAppender(appender);
	    }
    }

    /**
     * as test secondBibOutOfOrderError() except first bib record is followed
     *  by a matching mhld
     */
@Test
    public void secondBibOutOfOrderErrorAfterMhld()
    		throws IOException
    {
	    LoggerAppender4Testing appender = new LoggerAppender4Testing();
		CombineMultBibsMhldsReader.logger.addAppender(appender);
		CombineMultBibsMhldsReader.logger.setLevel(Level.DEBUG);
	    try
	    {
	        Logger.getLogger(CombineMultBibsMhldsReaderTest.class).info("Test secondBibOutOfOrderErrorAfterMhld");
	    	Map<String, Record> mergedRecs = readIntoRecordMap("combineBibMhld_b2m2b1b3.mrc");
	        assertEquals("Wrong number of read records: ", 3,  mergedRecs.keySet().size());
	        appender.assertLogContains(Level.INFO, "bib record a1 came after bib record a2: file isn't sorted.");
	    }
		catch (SolrMarcRuntimeException e)
		{
			fail("processing should continue if bib recs are out of order");
		}
	    finally
	    {
	    	CombineMultBibsMhldsReader.logger.removeAppender(appender);
	    }
    }


    /**
     * mhld doesn't match previous bib record
     */
@Test
    public void mhldDoesntMatchError()
    		throws IOException
    {
	    LoggerAppender4Testing appender = new LoggerAppender4Testing();
		CombineMultBibsMhldsReader.logger.addAppender(appender);
	    try
	    {
	        Logger.getLogger(CombineMultBibsMhldsReaderTest.class).info("Test mhldDoesntMatchError");
	    	Map<String, Record> mergedRecs = readIntoRecordMap("combineBibMhld_b1m2b2.mrc");
	        assertEquals("Wrong number of read records: ", 2,  mergedRecs.keySet().size());
	        appender.assertLogContains(Level.ERROR, "mhld id mismatch: mhld record a2 came after bib or mhld record a1: skipping mhld record a2.");
	    }
		catch (SolrMarcRuntimeException e)
		{
			fail("processing should continue if there are unmatched mhld recs (orphaned mhlds are skipped)");
		}
	    finally
	    {
	    	CombineMultBibsMhldsReader.logger.removeAppender(appender);
	    }
    }


    /**
     * last mhld doesn't match last bib record
     */
@Test
    public void lastMhldDoesntMatchError()
    		throws IOException
    {
	    LoggerAppender4Testing appender = new LoggerAppender4Testing();
		CombineMultBibsMhldsReader.logger.addAppender(appender);
	    try
	    {
	        Logger.getLogger(CombineMultBibsMhldsReaderTest.class).info("Test lastMhldDoesntMatchError");
	    	Map<String, Record> mergedRecs = readIntoRecordMap("combineBibMhld_b1b2m1.mrc");
	        assertEquals("Wrong number of read records: ", 2,  mergedRecs.keySet().size());
	        appender.assertLogContains(Level.ERROR, "mhld id mismatch: mhld record a1 came after bib or mhld record a2: skipping mhld record a1.");
	    }
		catch (SolrMarcRuntimeException e)
		{
			fail("processing should continue if there are unmatched mhld recs (orphaned mhlds are skipped)");
		}
	    finally
	    {
	    	CombineMultBibsMhldsReader.logger.removeAppender(appender);
	    }
    }


	/**
	 * mhld doesn't match immediately previous mhld record
	 */
@Test
	public void mhldDoesntMatchPrevMhldError()
			throws IOException
	{
	    LoggerAppender4Testing appender = new LoggerAppender4Testing();
		CombineMultBibsMhldsReader.logger.addAppender(appender);
	    try
	    {
	        Logger.getLogger(CombineMultBibsMhldsReaderTest.class).info("Test mhldDoesntMatchPrevMhldError");
	    	Map<String, Record> mergedRecs = readIntoRecordMap("combineBibMhld_b1b2b2m2m1b3.mrc");
	        assertEquals("Wrong number of read records: ", 3,  mergedRecs.keySet().size());
	        appender.assertLogContains(Level.ERROR, "mhld record a1 came after bib or mhld record a2: skipping mhld record a1.");
	    }
		catch (SolrMarcRuntimeException e)
		{
			fail("processing should continue if there are unmatched mhld recs (orphaned mhlds are skipped)");
		}
	    finally
	    {
	    	CombineMultBibsMhldsReader.logger.removeAppender(appender);
	    }
	}

	/**
	 * file starts with mhld
	 */
@Test
	public void fileStartsWithMhld()
			throws IOException
	{
	    LoggerAppender4Testing appender = new LoggerAppender4Testing();
		CombineMultBibsMhldsReader.logger.addAppender(appender);
	    try
	    {
	        Logger.getLogger(CombineMultBibsMhldsReaderTest.class).info("Test fileStartsWithMhld");
	    	Map<String, Record> mergedRecs = readIntoRecordMap("combineBibMhldStartsMhld.mrc");
	        assertEquals("Wrong number of read records: ", 1,  mergedRecs.keySet().size());
	        appender.assertLogContains(Level.ERROR, "First record in file is mhld (not bib): Skipping record amhld1");
	    }
		catch (SolrMarcRuntimeException e)
		{
			fail("processing should continue if the first record in the file is an mhld (orphaned mhlds are skipped)");
		}
	    finally
	    {
	    	CombineMultBibsMhldsReader.logger.removeAppender(appender);
	    }

		// FIXME: do this with more than one record in the file?
		//  - mhld then bib
		//  - mhld then another mhld (same id)
		// mhld then another mhld (diff id)
	}


	/**
	 * bib follows matching mhld record
	 */
@Test
	public void bibMatchesPrevMhldError()
			throws IOException
	{
		try
		{
	    	readIntoRecordMap("combineBibMhld_b1m1b1.mrc");
	    	fail ("processing should fail if bib follows matching mhld");
		}
		catch (SolrMarcRuntimeException e)
		{
	    	assertTrue("Output message not as expected: " + e.getMessage(),
	    			e.getMessage().startsWith("CombineMultBibsMhldsReader: bib record a1 came after matching mhld record: assuming error with upstream marc records."));
	    	assertTrue("Output message not as expected: " + e.getMessage(),
	    			e.getMessage().endsWith("STOPPING PROCESSING."));
		}
	}


//------- tests for unreadable records -----------------------------------------

    /**
     * first record in the file is unreadable (single bibs only)
     */
@Test
    public void unreadableFirstRecord()
    		throws IOException
    {
        LoggerAppender4Testing appender = new LoggerAppender4Testing();
    	CombineMultBibsMhldsReader.logger.addAppender(appender);
        try
        {
            Logger.getLogger(CombineMultBibsMhldsReaderTest.class).info("Test unreadableFirstRecord");
    		Map<String, Record> mergedRecs = readIntoRecordMap("combineBibMhld_badb1b2b3.mrc");
            Set<String> mergedRecIds = mergedRecs.keySet();
            assertEquals("Wrong number of read records: ", 3, mergedRecIds.size());
            assertTrue("Expected a1 in results", mergedRecIds.contains("a1"));
            assertTrue("Expected a2 in results", mergedRecIds.contains("a2"));
            assertTrue("Expected a3 in results", mergedRecIds.contains("a3"));
            // message goes to logger ...
            appender.assertLogContains("Skipping record; Couldn't read it:");
        }
        finally
        {
        	CombineMultBibsMhldsReader.logger.removeAppender(appender);
        }
    }

    /**
     * last record in the file is unreadable (single bibs only)
     */
@Test
    public void unreadableLastRecord()
    		throws IOException
    {
        LoggerAppender4Testing appender = new LoggerAppender4Testing();
    	CombineMultBibsMhldsReader.logger.addAppender(appender);
        try
        {
            Logger.getLogger(CombineMultBibsMhldsReaderTest.class).info("Test unreadableLastRecord");
        	Map<String, Record> mergedRecs = readIntoRecordMap("combineBibMhld_b1b2b3bad.mrc");
            Set<String> mergedRecIds = mergedRecs.keySet();
            assertEquals("Wrong number of read records: ", 3, mergedRecIds.size());
            assertTrue("Expected a1 in results", mergedRecIds.contains("a1"));
            assertTrue("Expected a2 in results", mergedRecIds.contains("a2"));
            assertTrue("Expected a3 in results", mergedRecIds.contains("a3"));

            // did message go to logger?
            appender.assertLogContains("Skipping record after a3; Couldn't read it:");
        }
        finally
        {
        	CombineMultBibsMhldsReader.logger.removeAppender(appender);
        }
    }

    /**
     * middle record in the file is unreadable (single bibs only)
     */
@Test
    public void unreadableMiddleRecord()
    		throws IOException
    {
        LoggerAppender4Testing appender = new LoggerAppender4Testing();
    	CombineMultBibsMhldsReader.logger.addAppender(appender);
        try
        {
            Logger.getLogger(CombineMultBibsMhldsReaderTest.class).info("Test unreadableMiddleRecord");
        	Map<String, Record> mergedRecs = readIntoRecordMap("combineBibMhld_b1badb3.mrc");
            Set<String> mergedRecIds = mergedRecs.keySet();
            assertEquals("Wrong number of read records: ", 2, mergedRecIds.size());
            assertTrue("Expected a1 in results", mergedRecIds.contains("a1"));
            assertTrue("Expected a3 in results", mergedRecIds.contains("a3"));

            // did message go to logger?
            appender.assertLogContains("Skipping record after a1; Couldn't read it:");
        }
        finally
        {
        	CombineMultBibsMhldsReader.logger.removeAppender(appender);
        }
    }

    /**
     * unreadable record in middle of multiple bibs for same id
     */
@Test
    public void unreadableBetweenMultBibRecord()
    		throws IOException
    {
        LoggerAppender4Testing appender = new LoggerAppender4Testing();
    	CombineMultBibsMhldsReader.logger.addAppender(appender);
        try
        {
            Logger.getLogger(CombineMultBibsMhldsReaderTest.class).info("Test unreadableBetweenMultBibRecord");
        	Map<String, Record> mergedRecs = readIntoRecordMap("combineBibMhld_b11badb13m11.mrc");
            Set<String> mergedRecIds = mergedRecs.keySet();
            assertEquals("Wrong number of read records: ", 1, mergedRecIds.size());

            // test if we got all the bib and all the mhld records that were readable
            assertTrue("Expected a1 in results", mergedRecIds.contains("a1"));
            Record mergedRec = mergedRecs.get("a1");

            Set<String> expectedVals = new HashSet<String>();
            expectedVals.add("999a1-1");
            expectedVals.add("999a1-3");
        	RecordTestingUtils.assertSubfieldHasExpectedValues(mergedRec, "999", 'a', expectedVals);
        	expectedVals.clear();
        	expectedVals.add("mhld1-1");
        	RecordTestingUtils.assertSubfieldHasExpectedValues(mergedRec, "852", 'b', expectedVals);

            // did message go to logger?
            appender.assertLogContains("Skipping record after a1; Couldn't read it:");
        }
        finally
        {
        	CombineMultBibsMhldsReader.logger.removeAppender(appender);
        }
    }

    /**
     * unreadable record between multiple bibs and multiple mhlds for same id
     */
@Test
    public void unreadableBetweenBibMhldMultRecord()
    		throws IOException
    {
        LoggerAppender4Testing appender = new LoggerAppender4Testing();
    	CombineMultBibsMhldsReader.logger.addAppender(appender);
        try
        {
            Logger.getLogger(CombineMultBibsMhldsReaderTest.class).info("Test unreadableBetweenBibMhldMultRecord");
        	Map<String, Record> mergedRecs = readIntoRecordMap("combineBibMhld_b11b12badm11m12.mrc");
            Set<String> mergedRecIds = mergedRecs.keySet();
            assertEquals("Wrong number of read records: ", 1, mergedRecIds.size());

            // test if we got all the bib and all the mhld records that were readable
            assertTrue("Expected a1 in results", mergedRecIds.contains("a1"));
            Record mergedRec = mergedRecs.get("a1");

            Set<String> expectedVals = new HashSet<String>();
            expectedVals.add("999a1-1");
            expectedVals.add("999a1-2");
        	RecordTestingUtils.assertSubfieldHasExpectedValues(mergedRec, "999", 'a', expectedVals);
        	expectedVals.clear();
        	expectedVals.add("mhld1-1");
        	expectedVals.add("mhld1-2");
        	RecordTestingUtils.assertSubfieldHasExpectedValues(mergedRec, "852", 'b', expectedVals);

            // did message go to logger?
            appender.assertLogContains("Skipping record after a1; Couldn't read it:");
        }
        finally
        {
        	CombineMultBibsMhldsReader.logger.removeAppender(appender);
        }
    }

    /**
     * unreadable record between multiple mhlds for same id
     */
@Test
    public void unreadableBetweenMultMhldRecord()
    		throws IOException
    {
        LoggerAppender4Testing appender = new LoggerAppender4Testing();
    	CombineMultBibsMhldsReader.logger.addAppender(appender);
        try
        {
            Logger.getLogger(CombineMultBibsMhldsReaderTest.class).info("Test unreadableBetweenMultMhldRecord");
        	Map<String, Record> mergedRecs = readIntoRecordMap("combineBibMhld_b11b12m11badm13.mrc");
            Set<String> mergedRecIds = mergedRecs.keySet();
            assertEquals("Wrong number of read records: ", 1, mergedRecIds.size());

            // test if we got all the bib and all the mhld records that were readable
            assertTrue("Expected a1 in results", mergedRecIds.contains("a1"));
            Record mergedRec = mergedRecs.get("a1");

            Set<String> expectedVals = new HashSet<String>();
            expectedVals.add("999a1-1");
            expectedVals.add("999a1-2");
        	RecordTestingUtils.assertSubfieldHasExpectedValues(mergedRec, "999", 'a', expectedVals);
        	expectedVals.clear();
        	expectedVals.add("mhld1-1");
        	expectedVals.add("mhld1-3");
        	RecordTestingUtils.assertSubfieldHasExpectedValues(mergedRec, "852", 'b', expectedVals);

            // did message go to logger?
            appender.assertLogContains("Skipping record after a1; Couldn't read it:");
        }
        finally
        {
        	CombineMultBibsMhldsReader.logger.removeAppender(appender);
        }
    }

//------- tests for fields that merge ------------------------------------------

    /**
     * if the bib rec has existing fields included in the mhldFldsToMerge list,
     *  then those bib fields should be removed before adding the MHLD fields
     */
@Test
    public void testCrashingBibFieldsRemoved()
    		throws IOException
    {
        Map<String, Record> mergedRecs = readIntoRecordMap("combineBibMhld_mergeFldTests.mrc");

        Record mergedRec = mergedRecs.get("aallMhldFlds");

        Set<String> unexpectedVals = new HashSet<String>();
        unexpectedVals.add("bibLoc");
        RecordTestingUtils.assertSubfieldDoesNotHaveValues(mergedRec, "852", 'b', unexpectedVals);

        unexpectedVals.clear();
        unexpectedVals.add("bib853a");
        RecordTestingUtils.assertSubfieldDoesNotHaveValues(mergedRec, "853", 'a', unexpectedVals);

        unexpectedVals.clear();
        unexpectedVals.add("bib863a");
        RecordTestingUtils.assertSubfieldDoesNotHaveValues(mergedRec, "863", 'a', unexpectedVals);

        unexpectedVals.clear();
        unexpectedVals.add("bib866a");
        RecordTestingUtils.assertSubfieldDoesNotHaveValues(mergedRec, "866", 'a', unexpectedVals);

        unexpectedVals.clear();
        unexpectedVals.add("bib867a");
        unexpectedVals.add("bib867a2");
        RecordTestingUtils.assertSubfieldDoesNotHaveValues(mergedRec, "867", 'a', unexpectedVals);

        unexpectedVals.clear();
        unexpectedVals.add("bib868a");
        RecordTestingUtils.assertSubfieldDoesNotHaveValues(mergedRec, "868", 'a', unexpectedVals);
    }


    /**
     * the bib record should get all mhld fields specified
     */
@Test
    public void testFieldsToMerge()
    		throws IOException
    {
        Map<String, Record> mergedRecs = readIntoRecordMap("combineBibMhld_mergeFldTests.mrc");
        Record mergedRec = mergedRecs.get("aallMhldFlds");

        // mhld flds merged in
        assertEquals("Wrong number of 852s ", 1, mergedRec.getVariableFields("852").size());
        Set<String> expectedVals = new HashSet<String>();
        expectedVals.add("mhldLoc");
        RecordTestingUtils.assertSubfieldHasExpectedValues(mergedRec, "852", 'b', expectedVals);

        assertEquals("Wrong number of 853s ", 1, mergedRec.getVariableFields("853").size());
        expectedVals.clear();
        expectedVals.add("mhld853a");
        RecordTestingUtils.assertSubfieldHasExpectedValues(mergedRec, "853", 'a', expectedVals);

        assertEquals("Wrong number of 863s ", 1, mergedRec.getVariableFields("863").size());
        expectedVals.clear();
        expectedVals.add("mhld863a");
        RecordTestingUtils.assertSubfieldHasExpectedValues(mergedRec, "863", 'a', expectedVals);

        assertEquals("Wrong number of 866s", 3, mergedRec.getVariableFields("866").size());
        expectedVals.clear();
        expectedVals.add("V. 417 NO. 1A (JAN 2011)");
        expectedVals.add("mhld866a ind1 blank ind2 0");
        expectedVals.add("2009-");
        expectedVals.add("mhld866a ind1 3 ind2 1");
        expectedVals.add("mhld866a ind1 4 ind2 1");
        RecordTestingUtils.assertSubfieldHasExpectedValues(mergedRec, "866", 'a', expectedVals);

        assertEquals("Wrong number of 867s ", 1, mergedRec.getVariableFields("867").size());
        expectedVals.clear();
        expectedVals.add("mhld867a ind1 blank ind2 0");
        RecordTestingUtils.assertSubfieldHasExpectedValues(mergedRec, "867", 'a', expectedVals);

        assertEquals("Wrong number of 868s ", 3, mergedRec.getVariableFields("868").size());
        expectedVals.clear();
        expectedVals.add("mhld868a ind1 blank ind2 0");
        expectedVals.add("mhld868a ind1 3 ind2 1");
        expectedVals.add("1957-1993, 1995-1998");
        expectedVals.add("mhld868a ind1 4 ind2 1");
        RecordTestingUtils.assertSubfieldHasExpectedValues(mergedRec, "868", 'a', expectedVals);
    }

    /**
     * the bib record should not get any MHLD fields that aren't indicated for the merge
     */
@Test
    public void testFieldsNotToMerge()
    		throws IOException
    {
        Map<String, Record> mergedRecs = readIntoRecordMap("combineBibMhld_mergeFldTests.mrc");
        Record mergedRec = mergedRecs.get("aallMhldFlds");

        // 901 should not be merged
        assertEquals("Wrong number of 901s ", 1, mergedRec.getVariableFields("901").size());
        Set<String> expectedVals = new HashSet<String>();
        expectedVals.add("bib901a");
        RecordTestingUtils.assertSubfieldHasExpectedValues(mergedRec, "901", 'a', expectedVals);
        Set<String> unexpectedVals = new HashSet<String>();
        unexpectedVals.add("mhld901a");
        RecordTestingUtils.assertSubfieldDoesNotHaveValues(mergedRec, "901", 'a', unexpectedVals);
    }

    /**
     * if the MHLD has more than one instance of a field, all instances should be put in the bib record
     *   (duplicated by testFieldsToMerge)
     */
//@Test
    public void testMultOccurFieldsToMerge()
    		throws IOException
    {
//    	testFieldsToMerge();
    }

    /**
     * the first record should not be assumed to be a bib
     */
//@Test
    public void testFirstRecordTypeAssessed()
    		throws IOException
    {
    	fileStartsWithMhld();
    }



// supporting methods for testing ---------------------------------------------

    /**
     * run a file through CombineMultBibsMhldsReader2 and get the results as a map for easy testing
     * @param marcRecsFilename - the name of the file containing MARC records, in the form of:
     *    bib1 bib2 mhld2 mhld2 bib3 bib4 bib4 mhld4 mhld4 bib5 ...
     * @return Map of ids -> Record objects for a single bib record comprising desired fields of multiple bibs and of following mhlds
     */
    public static Map<String, Record> combineFileRecordsAsMap(String marcRecsFilename, boolean permissive)
    	throws IOException
    {
    	Map<String, Record> results = new HashMap<String, Record>();

        String idField = "001";
        String bibFldsToMerge = "999";
        String mhldFldsToMerge = null;  // use default
        String insertMhldB4bibFld = "999";
        boolean toUtf8 = true;
        String defaultEncoding = "MARC8";

        CombineMultBibsMhldsReader merger = new CombineMultBibsMhldsReader(marcRecsFilename,
        		idField, idField, idField,
        		bibFldsToMerge, mhldFldsToMerge, insertMhldB4bibFld,
        		permissive, defaultEncoding, toUtf8);

        while (merger.hasNext())
        {
        	Record bibRecWithPossChanges = merger.next();

            String id = null;
            try {  id = MarcUtils.getControlFieldData(bibRecWithPossChanges, "001"); }
            catch (NullPointerException npe) { /* ignore */ }

			if (id != null)
				results.put(MarcUtils.getControlFieldData(bibRecWithPossChanges, "001"), bibRecWithPossChanges);
        }
        return results;
    }

    private Map<String, Record> readIntoRecordMap(String filename)
    		throws IOException
    {
    	return readIntoRecordMap(filename, true);
    }

    private Map<String, Record> readIntoRecordMap(String filename, boolean permissive)
    		throws IOException
    {
    	String filePath = testDataParentPath + File.separator + filename;
        return combineFileRecordsAsMap(filePath, permissive);
    }

}
