package edu.stanford;

import java.io.File;

import org.junit.Before;
import org.junit.Test;

public class GeographicFacetTests  extends AbstractStanfordTest 
{
	private final String testDataFname = "regionFacet.mrc";
	String testFilePath = testDataParentPath + File.separator + testDataFname;
	
@Before
	public final void setup() 
	{
		mappingTestInit();
	}

	/**
	 * geographic_facet should contain all 651a and the first subfield z in 
	 * all 6xx fields.
	 */
@Test
	public void testRegionFacet()
	{
		String fldName = "geographic_facet";
		solrFldMapTest.assertSolrFldHasNumValues(testFilePath, "651only", fldName, 2);
		solrFldMapTest.assertSolrFldValue(testFilePath, "651only", fldName, "651a");
		solrFldMapTest.assertSolrFldValue(testFilePath, "651only", fldName, "651z");

		solrFldMapTest.assertSolrFldHasNumValues(testFilePath, "651twoa", fldName, 2);
		solrFldMapTest.assertSolrFldValue(testFilePath, "651twoa", fldName, "651a1");
		solrFldMapTest.assertSolrFldValue(testFilePath, "651twoa", fldName, "651a2");

		solrFldMapTest.assertSolrFldHasNumValues(testFilePath, "no651twoz600", fldName, 1);
		solrFldMapTest.assertSolrFldValue(testFilePath, "no651twoz600", fldName, "600z1");
		solrFldMapTest.assertSolrFldHasNoValue(testFilePath, "no651twoz600", fldName, "600z2");

		solrFldMapTest.assertSolrFldHasNumValues(testFilePath, "610twoz651twoa", fldName, 3);
		solrFldMapTest.assertSolrFldValue(testFilePath, "610twoz651twoa", fldName, "610z1");
		solrFldMapTest.assertSolrFldValue(testFilePath, "610twoz651twoa", fldName, "651a1");
		solrFldMapTest.assertSolrFldValue(testFilePath, "610twoz651twoa", fldName, "651a2");

		solrFldMapTest.assertSolrFldHasNumValues(testFilePath, "651a630twoz", fldName, 2);
		solrFldMapTest.assertSolrFldValue(testFilePath, "651a630twoz", fldName, "651a");
		solrFldMapTest.assertSolrFldValue(testFilePath, "651a630twoz", fldName, "630z1");

		solrFldMapTest.assertSolrFldHasNumValues(testFilePath, "two648fields", fldName, 2);
		solrFldMapTest.assertSolrFldValue(testFilePath, "two648fields", fldName, "6481z");
		solrFldMapTest.assertSolrFldValue(testFilePath, "two648fields", fldName, "6482z");
		
		solrFldMapTest.assertSolrFldHasNumValues(testFilePath, "two650flds651", fldName, 4);
		solrFldMapTest.assertSolrFldValue(testFilePath, "two650flds651", fldName, "65011z");
		solrFldMapTest.assertSolrFldValue(testFilePath, "two650flds651", fldName, "65021z");
		solrFldMapTest.assertSolrFldValue(testFilePath, "610twoz651twoa", fldName, "651a1");
		solrFldMapTest.assertSolrFldValue(testFilePath, "610twoz651twoa", fldName, "651a2");
		
		solrFldMapTest.assertSolrFldHasNumValues(testFilePath, "6xx", fldName, 6);
		solrFldMapTest.assertSolrFldValue(testFilePath, "6xx", fldName, "655z");
		solrFldMapTest.assertSolrFldValue(testFilePath, "6xx", fldName, "651a");
		solrFldMapTest.assertSolrFldValue(testFilePath, "6xx", fldName, "651z");
		solrFldMapTest.assertSolrFldValue(testFilePath, "6xx", fldName, "6901z");
		solrFldMapTest.assertSolrFldValue(testFilePath, "6xx", fldName, "6902z");
		solrFldMapTest.assertSolrFldValue(testFilePath, "6xx", fldName, "654z");

		solrFldMapTest.assertSolrFldHasNumValues(testFilePath, "656", fldName, 1);
		solrFldMapTest.assertSolrFldValue(testFilePath, "656", fldName, "656z");
		
		solrFldMapTest.assertSolrFldHasNumValues(testFilePath, "657", fldName, 1);
		solrFldMapTest.assertSolrFldValue(testFilePath, "657", fldName, "657z");
		
		solrFldMapTest.assertSolrFldHasNumValues(testFilePath, "691", fldName, 1);
		solrFldMapTest.assertSolrFldValue(testFilePath, "691", fldName, "691z");
		
		solrFldMapTest.assertSolrFldHasNumValues(testFilePath, "696", fldName, 1);
		solrFldMapTest.assertSolrFldValue(testFilePath, "696", fldName, "696z");
		
		solrFldMapTest.assertSolrFldHasNumValues(testFilePath, "697", fldName, 1);
		solrFldMapTest.assertSolrFldValue(testFilePath, "697", fldName, "697z");
		
		solrFldMapTest.assertSolrFldHasNumValues(testFilePath, "698", fldName, 1);
		solrFldMapTest.assertSolrFldValue(testFilePath, "698", fldName, "698z");
		
		solrFldMapTest.assertSolrFldHasNumValues(testFilePath, "699", fldName, 1);
		solrFldMapTest.assertSolrFldValue(testFilePath, "699", fldName, "699z");
	}

}
