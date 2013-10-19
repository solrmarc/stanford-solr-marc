package edu.stanford;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.solr.client.solrj.SolrServerException;
import org.junit.Before;
import org.junit.Test;
import org.marc4j.marc.*;
import org.xml.sax.SAXException;

/**
 * junit4 tests for all_search field
 * @author Naomi Dushay
 */
public class AllSearchTests extends AbstractStanfordTest
{
	// NOTE:  see VernFieldsTests for  testVernCatchallField

	private static String fldName = "all_search";
	private static MarcFactory factory = MarcFactory.newInstance();

@Before
	public void setup()
	{
		mappingTestInit();
	}

	/**
	 * Test population of all_search
	 */
@Test
	public void testAllSearch()
			throws ParserConfigurationException, IOException, SAXException, SolrServerException
	{
		createFreshIx("allfieldsTests.mrc");

		String docId = "allfields1";

		// 245 just for good measure
        assertSingleResult(docId, fldName, "should");

        // 0xx fields are not included except 024, 027, 028
        assertSingleResult(docId, fldName, "2777802000"); // 024
        assertSingleResult(docId, fldName, "90620"); // 024
        assertSingleResult(docId, fldName, "technical"); // 027
        assertSingleResult(docId, fldName, "vibrations"); // 027
        assertZeroResults(fldName, "ocolcm");  // 035
        assertZeroResults(fldName, "orlob");  // 040

        // 3xx fields ARE included
        assertSingleResult(docId, fldName, "sound"); // 300
        assertSingleResult(docId, fldName, "annual");  // 310

        // 6xx subject fields - we're including them, even though
        // fulltopic is all subfields of all 600, 610, 630, 650, 655
        // fullgeographic is all subfields of all 651
        //   b/c otherwise standard numbers and other things are doubled here,
        //   but topics are not.

        // 9xx fields are NOT included
        assertZeroResults(fldName, "EDATA");  // 946
        assertZeroResults(fldName, "pamphlet");  // 947
        assertZeroResults(fldName, "stacks");  // 999

        // Except for 905, 920 and 986 (SW-814)
		Record record = factory.newRecord();
        DataField df = factory.newDataField("905", ' ', ' ');
        df.addSubfield(factory.newSubfield('a', "905a"));
        df.addSubfield(factory.newSubfield('r', "905r"));
        df.addSubfield(factory.newSubfield('t', "905t"));
        record.addVariableField(df);
        df = factory.newDataField("908", ' ', ' ');
        df.addSubfield(factory.newSubfield('a', "908a"));
        df.addSubfield(factory.newSubfield('b', "908b"));
        record.addVariableField(df);
        df = factory.newDataField("920", ' ', ' ');
        df.addSubfield(factory.newSubfield('a', "920a"));
        df.addSubfield(factory.newSubfield('b', "920b"));
        record.addVariableField(df);
        df = factory.newDataField("986", ' ', ' ');
        df.addSubfield(factory.newSubfield('1', "986a"));
        record.addVariableField(df);
        solrFldMapTest.assertSolrFldValue(record, fldName, "905a 905r 905t 908a 908b 920a 920b 986a");
	}


	/**
	 * all_search should include 033a
	 */
@Test
	public final void test033()
	{
	    // Except for 905, 920 and 986 (SW-814)
		Record record = factory.newRecord();
		DataField df = factory.newDataField("033", '0', ' ');
	    df.addSubfield(factory.newSubfield('a', "19710101"));
	    record.addVariableField(df);
		df = factory.newDataField("033", '0', ' ');
	    df.addSubfield(factory.newSubfield('a', "19710109"));
	    record.addVariableField(df);
		solrFldMapTest.assertSolrFldValue(record, fldName, "19710101 19710109");
	}


}
