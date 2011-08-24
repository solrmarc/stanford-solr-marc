package edu.stanford;

import java.io.File;

import org.junit.Before;
import org.junit.Test;

public class MhldMappingTests extends AbstractStanfordTest 
{
	private String fldName = "mhld_display";
	private String testFileName = "mhldDisplay852only.mrc";
	private String testFilePath = testDataParentPath + File.separator;

@Before
	public final void setup() 
	{
		mappingTestInit();
	}	

    /**
     * per spec in email by Naomi Dushay on July 12, 2011, an MHLD is skipped
     *  if 852 sub z  says "All holdings transfered" 
     */
@Test
    public final void testSkippedMhlds() 
    {
		String testDataFile = testFilePath + testFileName;
    	solrFldMapTest.assertSolrFldHasNumValues(testDataFile, "362573", fldName, 3);
    	solrFldMapTest.assertSolrFldValue(testDataFile, "362573", fldName, "GREEN -|- MEDIA-MTXT -|- CDs and DVDs -|-  -|- ");
    	solrFldMapTest.assertSolrFldValue(testDataFile, "362573", fldName, "SAL3 -|- STACKS -|-  -|-  -|- ");
    	solrFldMapTest.assertSolrFldValue(testDataFile, "362573", fldName, "GREEN -|- CURRENTPER -|- Latest yr. (or vol.) in CURRENT PERIODICALS; earlier in STACKS -|-  -|- ");

		solrFldMapTest.assertSolrFldHasNoValue(testDataFile, "3974376", fldName, "GREEN -|- STACKS -|-  -|-  -|- ");
		solrFldMapTest.assertSolrFldHasNumValues(testDataFile, "3974376", fldName, 0);
		solrFldMapTest.assertNoSolrFld(testDataFile, "3974376", fldName);
    }


}
