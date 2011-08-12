package edu.stanford;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.util.*;

import org.junit.Test;
import org.marc4j.marc.Record;
import org.solrmarc.testUtils.RecordTestingUtils;
import org.solrmarc.tools.MarcUtils;

import edu.stanford.marcUtils.CombineMultBibsMhldsReader;
import edu.stanford.marcUtils.MergeMhldFldsIntoBibsReader;

/**
 * tests for edu.stanford.marcUtils.CombineMultBibsMultMhldsReader
 * @author Naomi Dushay
 */
public class CombineMultBibsMhldsReaderTest extends AbstractStanfordTest
{
	
    /**
     * code should output the unchanged bib records if no mhlds match
     */
@Test
    public void testNoMatches() 
    		throws IOException 
    {
		String marcRecFilePath = testDataParentPath + File.separator + "mhldMergeBibs46.mrc";
	    Map<String, Record> mergedRecs = combineFileRecordsAsMap(marcRecFilePath);
	    Set<String> mergedRecIds = mergedRecs.keySet();
	    assertEquals(2, mergedRecIds.size());

	    // result bibs should match the bib input because there was no merge
	    String id = "a4";
       	RecordTestingUtils.assertEquals(ALL_UNMERGED_BIBS.get(id), mergedRecs.get(id));
	    id = "a6";
       	RecordTestingUtils.assertEquals(ALL_UNMERGED_BIBS.get(id), mergedRecs.get(id));
    }




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
