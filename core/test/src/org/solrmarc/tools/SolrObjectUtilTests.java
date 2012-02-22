package org.solrmarc.tools;

import static org.junit.Assert.*;

import java.util.*;

import org.apache.solr.common.SolrInputDocument;
import org.junit.Test;
import org.solrmarc.testUtils.IndexTest;
import org.solrmarc.tools.SolrObjectUtils;

/**
 * Tests for org.solrmarc.tools.SolrObjectUtil methods
 * @author Naomi Dushay
 */
public class SolrObjectUtilTests extends IndexTest
{

@Test
	public void createSolrInputDocFromEmptyFieldsMap()
	{
		SolrInputDocument solrInputDoc = SolrObjectUtils.createSolrInputDoc(new HashMap<String, Object>());
		assertTrue("createSolrInputDoc() didn't return empty SolrInputDocument for empty fields2ValuesMap", solrInputDoc.isEmpty());
	}

@Test
	public void createSolrInputDocFromNullFieldsMap()
	{
		SolrInputDocument solrInputDoc = SolrObjectUtils.createSolrInputDoc(null);
		assertNull("createSolrInputDoc() returned non-null object for null fields2ValuesMap", solrInputDoc);
	}

	/**
	 * assert multiple field instances are created when the field value is a
	 *  Collection (of Strings).
	 */
@Test
	public void createSolrInputDocCollectionFieldVals()
	{
        Set<String> fldVals = new LinkedHashSet<String>();
        fldVals.add("val1");
        fldVals.add("val2");
        fldVals.add("val3");
    	Map<String, Object> flds2ValsMap = new HashMap<String, Object>();
    	flds2ValsMap.put("fldname", fldVals);
		SolrInputDocument solrInputDoc = SolrObjectUtils.createSolrInputDoc(flds2ValsMap);
		Collection<Object> solrDocValObjs = solrInputDoc.getFieldValues("fldname");
		assertEquals("createSolrInputDoc() created wrong number of instances of field", 3, solrDocValObjs.size());
		for (Object valObj : solrDocValObjs)
			assertTrue("createSolrInputDoc() created an unexpected value: " + (String) valObj, fldVals.contains(valObj));


		// repeated values only occur once, b/c SolrMarc code doesn't allow dups
        fldVals.add("val1");
    	flds2ValsMap.put("fldname", fldVals);
		solrInputDoc = SolrObjectUtils.createSolrInputDoc(flds2ValsMap);
		solrDocValObjs = solrInputDoc.getFieldValues("fldname");
		assertEquals("createSolrInputDoc() created wrong number of instances of field", 3, solrDocValObjs.size());
		for (Object valObj : solrDocValObjs)
			assertTrue("createSolrInputDoc() created an unexpected value: " + (String) valObj, fldVals.contains(valObj));
	}

	/**
	 * assert correct value is assigned to SolrInputDocument for fldnames2ValuesMap
	 *  entries with a single value
	 */
@Test
	public void createSolrInputDocStringFieldVals()
	{
    	Map<String, Object> flds2ValsMap = new HashMap<String, Object>();
    	flds2ValsMap.put("fldname", "value");
		SolrInputDocument solrInputDoc = SolrObjectUtils.createSolrInputDoc(flds2ValsMap);
		assertEquals("createSolrInputDoc() has wrong value for field fldname", "value", (String) solrInputDoc.getFieldValue("fldname"));
	}

@Test
	public void createSolrInputDocCorrectNumFields()
	{
		Map<String, Object> flds2ValsMap = createTestFldNames2ValsMap();
		SolrInputDocument solrInputDoc = SolrObjectUtils.createSolrInputDoc(flds2ValsMap);
		assertEquals("createSolrInputDoc() created wrong number of fields", flds2ValsMap.size(), solrInputDoc.size());
	}

	/**
	 * when the SolrInputDocument object is null, getXML() should return null
	 */
@Test
	public void getXMLFromNull()
	{
		assertNull("getXML() returned non-null object for null SolrInputDocument", SolrObjectUtils.getXML(null));
	}

	/**
	 * when the SolrInputDocument object is empty, getXML() should return null
	 */
@Test
	public void getXMLFromEmptyDocument()
	{
		SolrInputDocument empty = new SolrInputDocument();
		assertTrue("newly constructed SolrInputDocument isn't empty", empty.isEmpty());
		assertNull("getXML() returned non-null object for empty SolrInputDocument", SolrObjectUtils.getXML(new SolrInputDocument()));
	}


@Test
	public void getXMLFieldsAndWrapperTests()
	{
    	Map<String, Object> fldNames2ValsMap = createTestFldNames2ValsMap();
		SolrInputDocument solrInputDoc = SolrObjectUtils.createSolrInputDoc(fldNames2ValsMap);
		String solrDocXml = SolrObjectUtils.getXML(solrInputDoc);
		assertTrue("XML for Solr doc doesn't start with <doc>", solrDocXml.startsWith("<doc>"));
		assertTrue("XML for Solr doc doesn't end with </doc>", solrDocXml.endsWith("</doc>"));
		for (String fldName : fldNames2ValsMap.keySet())
		{
			assertTrue("XML for solr doc doesn't contain opening tag for field " + fldName, solrDocXml.contains("<field name=\"" + fldName + "\">"));
            Object valObj = fldNames2ValsMap.get(fldName);
            if (valObj instanceof Collection<?>)
            {
                for (Object singleValObj : (Collection<?>) valObj)
                {
                	String fldValStr = (String) singleValObj;
                	assertTrue("XML for solr doc doesn't contain field " + fldName + " with value " + fldValStr, solrDocXml.contains("<field name=\"" + fldName + "\">" + fldValStr + "</field>"));
                }
            }
            else
            {
            	String fldValStr = (String) valObj;
            	assertTrue("XML for solr doc doesn't contain field " + fldName + " with value " + fldValStr, solrDocXml.contains("<field name=\"" + fldName + "\">" + fldValStr + "</field>"));
            }
		}
	}


	/**
	 * XML from SolrInputDocuments should only include a boost value if it
	 *  isn't 1.0
	 */
@Test
	public void getXMLOnlyNonDefaultBoosts()
	{
		Map<String, Object> fldNamess2ValsMap = createTestFldNames2ValsMap();
		SolrInputDocument solrInputDoc = SolrObjectUtils.createSolrInputDoc(fldNamess2ValsMap);
		String solrDocXml = SolrObjectUtils.getXML(solrInputDoc);
		assertFalse("XML for Solr doc contains default boost value and shouldn't", solrDocXml.contains("boost=\"1.0\""));
	}

	// TODO: should test non-string values, such as marc21, integers, boolean, dates?

	private Map<String, Object> createTestFldNames2ValsMap()
	{
		Map<String, Object> flds2ValsMap = new HashMap<String, Object>();
		flds2ValsMap.put("fld1", "val1");
		flds2ValsMap.put("fld2", "val2");
	    Set<String> fldVals = new LinkedHashSet<String>();
	    fldVals.add("val1");
	    fldVals.add("val2");
	    flds2ValsMap.put("fld3", fldVals);
	    return flds2ValsMap;
	}

}
