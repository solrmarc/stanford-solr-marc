package org.solrmarc.testUtils;

import java.io.FileNotFoundException;
import java.util.*;

import org.marc4j.marc.Record;

import static org.junit.Assert.*;

//import org.apache.log4j.Logger;

/**
 * Utility Test class to determine if a marc record will map data to the Solr
 * fields as expected.
 *
 * @author Naomi Dushay
 * @version $Id$
 *
 */
public class SolrFieldMappingTest
{
    // Initialize logging category
    // static Logger logger =
    // Logger.getLogger(SolrFieldMappingTest.class.getName());

    /** marcMappingTest instance used to do the field mapping */
    private MarcMappingOnly marcMappingTest = null;

    private String idFldName = null;

    /**
     * Constructor
     *
     * @param configPropsName  name of xxx _config.properties file
     * @param idFldName  name of unique key field in Solr document
     */
    public SolrFieldMappingTest(String configPropsName, String idFldName)
    		throws FileNotFoundException
    {
        marcMappingTest = new MarcMappingOnly();
        marcMappingTest.init(new String[] { configPropsName, idFldName });
        this.idFldName = idFldName;
    }

    /**
     * assert that when the file of marc records is processed, there will be a
     * Solr document with the given id containing at least one instance of the
     * expected Solr field with the expected value
     *
     * @param mrcFileName  absolute path of file of marc records (name must end in .mrc or .marc or .xml)
     * @param solrDocId  value of unique key field for the Solr document to be checked
     * @param expectedFldName  name of the Solr field to be checked
     * @param expectedFldVal  the value expected to be in at least one instance of the Solr field for the indicated Solr document
     */
    public void assertSolrFldValue(String mrcFileName, String solrDocId, String expectedFldName, String expectedFldVal)
    {
    	try
    	{
	        Map<String, Object> solrFldName2ValMap = marcMappingTest.getIndexMapForRecord(solrDocId, mrcFileName);
	        if (solrFldName2ValMap == null)
	        	fail("There is no document with id " + solrDocId);

	        assertSolrFldValue(solrFldName2ValMap, expectedFldName, expectedFldVal);
        }
        catch (FileNotFoundException e)
        {
        	e.printStackTrace();
        	System.exit(666);
        }
    }

    /**
     * assert that when the marc record is processed, the Solr document will
     * containing at least one instance of the expected Solr field with the
     * expected value
     *
     * @param record  the Marc Record object to be mapped
     * @param expectedFldName  name of the Solr field to be checked
     * @param expectedFldVal  the value expected to be in at least one instance of the Solr field for the indicated Solr document
     */
    public void assertSolrFldValue(Record record, String expectedFldName, String expectedFldVal)
    {
        assertSolrFldValue(marcMappingTest.getIndexMapForRecord(record), expectedFldName, expectedFldVal);
    }


    /**
     * assert that the Map of Solr fields contains at least one instance of the
     * expected Solr field with the expected value
     *
	 * @param solrFldName2ValMap   Map of Solr fields (keys are field names,
	 *  values are an Object containing the values (a Set or a String))
     * @param expectedFldName  name of the Solr field to be checked
     * @param expectedFldVal  the value expected to be in at least one instance of the Solr field for the indicated Solr document
     */
    private void assertSolrFldValue(Map<String, Object> solrFldName2ValMap, String expectedFldName, String expectedFldVal)
    {
        String id = (String) solrFldName2ValMap.get(idFldName);

        Object solrFldValObj = solrFldName2ValMap.get(expectedFldName);
        if (solrFldValObj == null)
            fail("Solr doc " + id + " has no value assigned for Solr field " + expectedFldName);
        if (solrFldValObj instanceof String)
            assertEquals("Solr doc " + id + " didn't have expected value for Solr field " + expectedFldName + ": ", expectedFldVal, solrFldValObj.toString());
        else if (solrFldValObj instanceof Collection)
        {
            // look for a match of at least one of the values
            boolean foundIt = false;
            for (String fldVal : (Collection<String>) solrFldValObj)
            {
                if (fldVal.equals(expectedFldVal))
                    foundIt = true;
                // System.out.println("DEBUG: value is [" + fldVal + "]");
            }
            assertTrue("Solr doc " + id + " did not have any " + expectedFldName + " fields with value matching " + expectedFldVal, foundIt);
        }
    }


    /**
     * assert that when the file of marc records is processed, the Solr document
     * with the given id does NOT contain an instance of the indicated field
     * with the indicated value
     *
     * @param mrcFileName  absolute path of file of marc records (name must end in .mrc or .marc or .xml)
     * @param solrDocId  value of unique key field for the Solr document to be checked
     * @param expectedFldName  name of the Solr field to be checked
     * @param expectedFldVal  the value that should be in NO instance of the Solr field for the indicated Solr document
     */
    public void assertSolrFldHasNoValue(String mrcFileName, String solrDocId, String expectedFldName, String expectedFldVal)
    {
    	try
    	{
	        Map<String, Object> solrFldName2ValMap = marcMappingTest.getIndexMapForRecord(solrDocId, mrcFileName);
	        if (solrFldName2ValMap == null)
	        	fail("there is no document with id " + solrDocId);

	        assertSolrFldHasNoValue(solrFldName2ValMap, expectedFldName, expectedFldVal);
        }
        catch (FileNotFoundException e)
        {
        	e.printStackTrace();
        	System.exit(666);
        }
    }

    /**
     * assert that when the marc record is processed, the Solr document does NOT
     * contain an instance of the indicated field with the indicated value
     *
     * @param record  the Marc Record object to be mapped
     * @param expectedFldName  name of the Solr field to be checked
     * @param expectedFldVal  the value that should be in NO instance of the Solr field for the indicated Solr document
     */
    public void assertSolrFldHasNoValue(Record record, String expectedFldName, String expectedFldVal)
    {
        assertSolrFldHasNoValue(marcMappingTest.getIndexMapForRecord(record), expectedFldName, expectedFldVal);
    }

    /**
     * assert that the Map of Solr fields does NOT contain an instance of the
     * indicated field with the indicated value
     *
	 * @param solrFldName2ValMap   Map of Solr fields (keys are field names,
	 *  values are an Object containing the values (a Set or a String))
     * @param expectedFldName  name of the Solr field to be checked
     * @param expectedFldVal  the value that should be in NO instance of the Solr field for the indicated Solr document
     */
    private void assertSolrFldHasNoValue(Map<String, Object> solrFldName2ValMap, String expectedFldName, String expectedFldVal)
    {
        Object solrFldValObj = solrFldName2ValMap.get(expectedFldName);
        if (solrFldValObj instanceof String)
            assertFalse("Solr field " + expectedFldName + " unexpectedly has value [" + expectedFldVal + "]", solrFldValObj.toString().equals(expectedFldVal));
        else if (solrFldValObj instanceof Collection)
        {
            // make sure none of the values match
            for (String fldVal : (Collection<String>) solrFldValObj)
            {
                if (fldVal.equals(expectedFldVal))
                    fail("Solr field " + expectedFldName + " unexpectedly has value [" + expectedFldVal + "]");
            }
        }
    }


    /**
     * assert that when the file of marc records is processed, the Solr document
     * with the given id contains the expected number of instances of the
     * indicated field
     *
     * @param mrcFileName  absolute path of file of marc records (name must end in .mrc or .marc or .xml)
     * @param solrDocId  value of unique key field for the Solr document to be checked
     * @param expectedFldName  name of the Solr field to be checked
     * @param expectedNumVals  the number of values that should be in the Solr field for the indicated Solr document
     */
    public void assertSolrFldHasNumValues(String mrcFileName, String solrDocId, String expectedFldName, int expectedNumVals)
    {
    	try
    	{
	        Map<String, Object> solrFldName2ValMap = marcMappingTest.getIndexMapForRecord(solrDocId, mrcFileName);
	        if (solrFldName2ValMap == null)
	        	fail("there is no document with id " + solrDocId);

	        assertSolrFldHasNumValues(solrFldName2ValMap, expectedFldName, expectedNumVals);
	    }
	    catch (FileNotFoundException e)
	    {
	    	e.printStackTrace();
	    	System.exit(666);
	    }
    }

    /**
     * assert that when the marc record is processed, the Solr document
     * contains the expected number of instances of the indicated field
     *
     * @param record  the Marc Record object to be mapped
     * @param expectedFldName  name of the Solr field to be checked
     * @param expectedNumVals  the number of values that should be in the Solr field for the indicated Solr document
     */
    public void assertSolrFldHasNumValues(Record record, String expectedFldName, int expectedNumVals)
    {
	    assertSolrFldHasNumValues(marcMappingTest.getIndexMapForRecord(record), expectedFldName, expectedNumVals);
    }

    /**
     * assert that the Map of Solr fields contains the expected number of
     * instances of the indicated field
     *
	 * @param solrFldName2ValMap   Map of Solr fields (keys are field names,
	 *  values are an Object containing the values (a Set or a String))
     * @param expectedFldName  name of the Solr field to be checked
     * @param expectedNumVals  the number of values that should be in the Solr field for the indicated Solr document
     */
    private void assertSolrFldHasNumValues(Map<String, Object> solrFldName2ValMap, String expectedFldName, int expectedNumVals)
    {
        Object solrFldValObj = solrFldName2ValMap.get(expectedFldName);
        if (solrFldValObj == null && expectedNumVals != 0)
        	fail("Solr field "+ expectedFldName + " unexpectedly has no values; expected " + String.valueOf(expectedNumVals));
        if (solrFldValObj instanceof String)
        {
        	if (expectedNumVals != 1)
        		fail("Solr field " + expectedFldName + " unexpectedly has a single value " + solrFldValObj.toString() + "; expected " + String.valueOf(expectedNumVals));
       	}
        else if (solrFldValObj instanceof Collection)
        {
        	int numVals = ((Collection<String>) solrFldValObj).size();
            assertTrue("Solr field " + expectedFldName + " unexpectedly has " + numVals + " values; expected " + expectedNumVals, expectedNumVals == numVals);
        }
    }


    /**
     * assert that when the file of marc records is processed, the Solr document
     * with the given id will not have the named field
     *
     * @param mrcFileName  absolute path of file of marc records (name must end in .mrc or .marc or .xml)
     * @param solrDocId  value of unique key field for the Solr document to be checked
     * @param fldName  name of the Solr field to be checked
     */
    public void assertNoSolrFld(String mrcFileName, String solrDocId, String fldName)
    {
    	try
    	{
	        Map<String, Object> solrFldName2ValMap = marcMappingTest.getIndexMapForRecord(solrDocId, mrcFileName);
	        if (solrFldName2ValMap == null)
	        	fail("there is no document with id " + solrDocId);

		    assertNoSolrFldInMap(solrFldName2ValMap, fldName, solrDocId);
	    }
	    catch (FileNotFoundException e)
	    {
	    	e.printStackTrace();
	    	System.exit(666);
	    }
    }

    /**
     * assert that when the marc record is processed, the Solr document
     * with the given id will not have the named field
     *
     * @param record  the Marc Record object to be mapped
     * @param fldName  name of the Solr field to be checked
     */
    public void assertNoSolrFld(Record record, String fldName)
    {
    	Map<String, Object> solrFldName2ValMap = marcMappingTest.getIndexMapForRecord(record);

	    String recid = (String) solrFldName2ValMap.get(idFldName);
	    if (recid == null)
	    	recid = record.getControlNumber();

	    assertNoSolrFldInMap(solrFldName2ValMap, fldName, recid);
    }

	/**
	 * assert that the Map of Solr fields does NOT have the named field
	 *
	 * @param solrFldName2ValMap   Map of Solr fields (keys are field names,
	 *  values are an Object containing the values (a Set or a String))
	 * @param fldName  name of the Solr field to be checked
	 * @param id  the id of the Solr doc
	 */
	private void assertNoSolrFldInMap(Map<String, Object> solrFldName2ValMap, String fldName, String id)
	{
	    Object solrFldValObj = solrFldName2ValMap.get(fldName);
	    if (solrFldValObj != null)
	        fail("There is a value assigned for Solr field " + fldName + " in Solr document " + id);
	}


}
